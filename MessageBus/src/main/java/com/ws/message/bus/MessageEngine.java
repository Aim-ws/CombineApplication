package com.ws.message.bus;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.ws.message.bus.annotation.Subscribe;
import com.ws.message.bus.annotation.ThreadMode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Create time 2023/11/9 9:36
 * Email: shuaiwang@grandstream.cn
 *
 * @author wangshuai
 * @version 1.0
 */
class MessageEngine implements Handler.Callback {
    public static final String TAG = "MessageEngine";
    Map<Class<?>, ArrayList<SubscribeMethod>> mSubscriberMethodMap;
    LinkedList<MessageEvent> mMessageQueue;
    Handler mHandler;

    MessageEngine() {
        mSubscriberMethodMap = new ConcurrentHashMap<>();
        mMessageQueue = new LinkedList<>();
        mHandler = new Handler(this);
    }


    public void register(@NonNull Object subscriber) {
        findSubscriberMethod(subscriber);
    }

    public void unregister(@NonNull Object subscriber) {
        mSubscriberMethodMap.remove(subscriber.getClass());
    }

    public void post(MessageEvent event) {
        mMessageQueue.offer(event);
        post();

    }

    private void post() {
        while (!mMessageQueue.isEmpty()) {
            MessageEvent event = mMessageQueue.poll();
            if (event != null) {
                postMessage(event);
            }
        }
    }

    private void postMessage(MessageEvent event) {
        List<SubscribeMethod> subscribeMethods = findPostSubscriberMethod(event.getAction());
        List<SubscribeMethod> uiSubscribeMethods = new ArrayList<>();
        List<SubscribeMethod> threadMethods = new ArrayList<>();
        for (SubscribeMethod subscribeMethod : subscribeMethods) {
            if (ThreadMode.MAIN == subscribeMethod.getThreadMode()) {
                if (isUIThread()) {
                    uiSubscribeMethods.add(subscribeMethod);
                } else {
                    threadMethods.add(subscribeMethod);
                }
            } else {
                threadMethods.add(subscribeMethod);
            }
        }
        if (!uiSubscribeMethods.isEmpty()) {
            publishMessageSubscriberMethod(event, uiSubscribeMethods);
        }
        if (!threadMethods.isEmpty()) {
            SubscribePost post = new SubscribePost();
            post.event = event;
            post.subscribeMethods = threadMethods;
            Message message = Message.obtain(mHandler);
            message.what = 0;
            message.obj = post;
            message.sendToTarget();
        }
    }

    /**
     * @param subscriber Object
     */
    private void findSubscriberMethod(@NonNull Object subscriber) {
        Class<?> clz = subscriber.getClass();
        Method[] methods = clz.getMethods();
        ArrayList<SubscribeMethod> subscribeMethods = new ArrayList<>();
        for (Method method : methods) {
            Subscribe methodAnnotation = method.getAnnotation(Subscribe.class);
            Class<?>[] parameterTypes = method.getParameterTypes();
            int modifiers = method.getModifiers();
            Class<?> returnType = method.getReturnType();
            if (methodAnnotation != null && checkMethodParameterType(parameterTypes)
                    && modifiers == Modifier.PUBLIC && returnType == void.class) {
                Class<?> parameterType = parameterTypes.length == 0 ? null : parameterTypes[0];
                SubscribeMethod subscribeMethod = new SubscribeMethod();
                subscribeMethod.setAction(methodAnnotation.action());
                subscribeMethod.setThreadMode(methodAnnotation.threadMode());
                subscribeMethod.setMethod(method);
                subscribeMethod.setSubscriber(subscriber);
                subscribeMethod.setParameterType(parameterType);
                subscribeMethods.add(subscribeMethod);
            }
        }
        mSubscriberMethodMap.put(clz, subscribeMethods);
    }

    /**
     * @param action String
     * @return List<SubscribeMethod>
     */
    private List<SubscribeMethod> findPostSubscriberMethod(String action) {
        List<SubscribeMethod> subscribeMethods = new ArrayList<>();
        for (Map.Entry<Class<?>, ArrayList<SubscribeMethod>> entry : mSubscriberMethodMap.entrySet()) {
            ArrayList<SubscribeMethod> methods = entry.getValue();
            if (methods != null) {
                for (SubscribeMethod method : methods) {
                    if (method.getAction().equals(action)) {
                        subscribeMethods.add(method);
                    }
                }
            }
        }
        return subscribeMethods;
    }

    /**
     * @param event   MessageEvent
     * @param methods List<SubscribeMethod>
     */
    private void publishMessageSubscriberMethod(MessageEvent event, List<SubscribeMethod> methods) {
        for (SubscribeMethod subscribeMethod : methods) {
            if (findSubscribeMethod(subscribeMethod, event)) {
                invokeSubscriberMethod(subscribeMethod.getSubscriber(), subscribeMethod.getMethod(), event.getEvent());
            } else {
                Log.d(TAG, "subscribe method parameter type is " + subscribeMethod.getParameterType() +
                        " not match event type " + (event.getEvent() == null ? "" : event.getEvent().getClass()));
            }
        }
    }

    /**
     * @param subscribeMethod SubscribeMethod
     * @param event            MessageEvent
     * @return boolean
     */
    private boolean findSubscribeMethod(SubscribeMethod subscribeMethod, MessageEvent event) {
        Class<?> parameterType = subscribeMethod.getParameterType();
        if (event.getEvent() == null) {
            return parameterType == null;
        }
        Class<?> clz = event.getEvent().getClass();
        Log.d(TAG, "findSubscribeMethod parameterType: " + parameterType + ", clz: " + clz);
        if (int.class == parameterType && clz == Integer.class) {
            return true;
        }
        if (long.class == parameterType && clz == Long.class) {
            return true;
        }
        if (float.class == parameterType && clz == Float.class) {
            return true;
        }
        if (double.class == parameterType && clz == Double.class) {
            return true;
        }
        return parameterType == clz;
    }

    /**
     * @param subscriber Object
     * @param method     Method
     * @param event      Object
     */
    private void invokeSubscriberMethod(Object subscriber, Method method, Object event) {
        try {
            method.setAccessible(true);
            if (event == null) {
                method.invoke(subscriber);
            } else {
                method.invoke(subscriber, event);
            }
        } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
            Log.e(TAG, "publishMessageSubscriberMethod: ", e);
        }
    }

    /**
     * the thread is main thread
     *
     * @return boolean
     */
    private boolean isUIThread() {
        Looper mainLooper = Looper.getMainLooper();
        Looper looper = Looper.myLooper();
        return mainLooper == looper;
    }

    /**
     * subscriber method parameters max 1
     *
     * @param parameterTypes Class<?>[]
     * @return boolean
     */
    private boolean checkMethodParameterType(Class<?>[] parameterTypes) {
        return parameterTypes == null || parameterTypes.length < 2;
    }

    @Override
    public boolean handleMessage(@NonNull Message message) {
        SubscribePost post = (SubscribePost) message.obj;
        publishMessageSubscriberMethod(post.event, post.subscribeMethods);
        return true;
    }
}
