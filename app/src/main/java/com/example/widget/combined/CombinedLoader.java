package com.example.widget.combined;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

/**
 * Create time 2023/7/18 16:42
 * Email: shuaiwang@grandstream.cn
 *
 * @author wangshuai
 * @version 1.0
 */
public class CombinedLoader {
    public static final String TAG = "CombinedLoader";
    public static final int SUCCESS = 0;
    public static final int FAILED = -1;


    OnCombinedLoadCallback onCombinedLoadCallback;

    private CombinedLoader() {

    }

    public static CombinedLoader getInstance() {
        return new CombinedLoader();
    }

    /**
     * 使用glide加载头像
     *
     * @param context  Context
     * @param width    int width  加载头像的目标宽度
     * @param height   int height  加载头像的目标高度
     * @param index    index    加载头像的序列
     * @param url      String   头像链接
     * @param callback OnCombinedLoadCallback 回调接口
     */
    public void loadImage(Context context, int width, int height, int index, String url, OnCombinedLoadCallback callback) {
        this.onCombinedLoadCallback = callback;
        Glide.with(context)
                .asBitmap()
                .override(width, height)
                .apply(new RequestOptions().transform(new CenterCrop()))
                .load(url)
                .into(new CustomTargetImpl(index));
    }

    class CustomTargetImpl extends CustomTarget<Bitmap> {
        int index;

        public CustomTargetImpl(int index) {
            this.index = index;
        }

        @Override
        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
            if (onCombinedLoadCallback != null) {
                onCombinedLoadCallback.onCombinedLoad(SUCCESS, index, resource);
            }
        }

        @Override
        public void onLoadCleared(@Nullable Drawable placeholder) {

        }

        @Override
        public void onLoadFailed(@Nullable Drawable errorDrawable) {
            super.onLoadFailed(errorDrawable);
            Log.w(TAG, "onLoadFailed: ");
            if (onCombinedLoadCallback != null) {
                onCombinedLoadCallback.onCombinedLoad(FAILED, index, null);
            }
        }
    }

    public interface OnCombinedLoadCallback {
        void onCombinedLoad(int retCode, int index, Bitmap newBitmap);
    }
}
