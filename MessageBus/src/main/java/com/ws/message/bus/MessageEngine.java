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
    Map<Class<?>, ArrayList<SubscriberMethod>> mSubscriberMethodMap;
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
        List<SubscriberMethod> subscriberMethods = findPostSubscriberMethod(event.getAction());
        List<SubscriberMethod> uiSubscriberMethods = new ArrayList<>();
        List<SubscriberMethod> threadMethods = new ArrayList<>();
        for (SubscriberMethod subscriberMethod : subscriberMethods) {
            if (ThreadMode.MAIN == subscriberMethod.getThreadMode()) {
                if (isUIThread()) {
                    uiSubscriberMethods.add(subscriberMethod);
                } else {
                    threadMethods.add(subscriberMethod);
                }
            } else {
                threadMethods.add(subscriberMethod);
            }
        }
        if (!uiSubscriberMethods.isEmpty()) {
            publishMessageSubscriberMethod(event, uiSubscriberMethods);
        }
        if (!threadMethods.isEmpty()) {
            SubscriberPost post = new SubscriberPost();
            post.event = event;
            post.subscriberMethods = threadMethods;
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
        ArrayList<SubscriberMethod> subscriberMethods = new ArrayList<>();
        for (Method method : methods) {
            Subscribe methodAnnotation = method.getAnnotation(Subscribe.class);
            Class<?>[] parameterTypes = method.getParameterTypes();
            int modifiers = method.getModifiers();
            Class<?> returnType = method.getReturnType();
            if (methodAnnotation != null && checkMethodParameterType(parameterTypes)
                    && modifiers == Modifier.PUBLIC && returnType == void.class) {
                Class<?> parameterType = parameterTypes.length == 0 ? null : parameterTypes[0];
                SubscriberMethod subscriberMethod = new SubscriberMethod();
                subscriberMethod.setAction(methodAnnotation.action());
                subscriberMethod.setThreadMode(methodAnnotation.threadMode());
                subscriberMethod.setMethod(method);
                subscriberMethod.setSubscriber(subscriber);
                subscriberMethod.setParameterType(parameterType);
                subscriberMethods.add(subscriberMethod);
            }
        }
        mSubscriberMethodMap.put(clz, subscriberMethods);
    }

    /**
     * @param action String
     * @return List<SubscriberMethod>
     */
    private List<SubscriberMethod> findPostSubscriberMethod(String action) {
        List<SubscriberMethod> subscriberMethods = new ArrayList<>();
        for (Map.Entry<Class<?>, ArrayList<SubscriberMethod>> entry : mSubscriberMethodMap.entrySet()) {
            ArrayList<SubscriberMethod> methods = entry.getValue();
            if (methods != null) {
                for (SubscriberMethod method : methods) {
                    if (method.getAction().equals(action)) {
                        subscriberMethods.add(method);
                    }
                }
            }
        }
        return subscriberMethods;
    }

    /**
     * @param event   MessageEvent
     * @param methods List<SubscriberMethod>
     */
    private void publishMessageSubscriberMethod(MessageEvent event, List<SubscriberMethod> methods) {
        for (SubscriberMethod subscriberMethod : methods) {
            if (findSubscribeMethod(subscriberMethod, event)) {
                invokeSubscriberMethod(subscriberMethod.getSubscriber(), subscriberMethod.getMethod(), event.getEvent());
            } else {
                Log.d(TAG, "subscribe method parameter type is " + subscriberMethod.getParameterType() +
                        " not match event type " + (event.getEvent() == null ? "" : event.getEvent().getClass()));
            }
        }
    }

    /**
     * @param subscriberMethod SubscriberMethod
     * @param event            MessageEvent
     * @return boolean
     */
    private boolean findSubscribeMethod(SubscriberMethod subscriberMethod, MessageEvent event) {
        Class<?> parameterType = subscriberMethod.getParameterType();
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
        SubscriberPost post = (SubscriberPost) message.obj;
        publishMessageSubscriberMethod(post.event, post.subscriberMethods);
        return true;
    }
}
