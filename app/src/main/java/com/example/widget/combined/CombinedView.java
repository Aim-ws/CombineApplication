package com.example.widget.combined;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.combined.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Create time 2023/7/18 16:03
 * Email: shuaiwang@grandstream.cn
 *
 * @author wangshuai
 * @version 1.0
 */
public class CombinedView extends View {
    public static final String TAG = "CombinedView";
    /**
     * 自定义属性
     */
    CombinedAttrs mCombinedAttrs;
    CombinedCanvas mCombinedCanvas;
    Bitmap mCacheBitmap;
    Rect srcRect = new Rect();
    Rect dstRect = new Rect();
    List<Combined> combinedList = new ArrayList<>();

    public CombinedView(Context context) {
        this(context, null);
    }

    public CombinedView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CombinedView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 读取自定义属性
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.CombinedView);
        int shape = attributes.getInteger(R.styleable.CombinedView_combined_shape, CombinedShape.ROUND);
        int dividerColor = attributes.getColor(R.styleable.CombinedView_combined_dividerColor, Color.WHITE);
        int backgroundColor = attributes.getColor(R.styleable.CombinedView_combined_backgroundColor, Color.GRAY);
        float roundRadius = attributes.getDimension(R.styleable.CombinedView_combined_roundRadius, 8f);
        float dividerWidth = attributes.getDimension(R.styleable.CombinedView_combined_dividerWidth, 1f);
        float textSize = attributes.getDimension(R.styleable.CombinedView_combined_textSize, 16f);
        int textColor = attributes.getColor(R.styleable.CombinedView_combined_textColor, Color.WHITE);
        mCombinedAttrs = new CombinedAttrs(shape, roundRadius, dividerWidth, dividerColor, backgroundColor, textSize, textColor);
        // 回收资源
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            attributes.close();
        } else {
            attributes.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        newCombinedCanvas();
        performDraw();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT);
        if (mCacheBitmap != null) {
            // bitmap 的位置大小
            srcRect.left = 0;
            srcRect.top = 0;
            srcRect.right = mCacheBitmap.getWidth();
            srcRect.bottom = mCacheBitmap.getHeight();

            // 目标位置的大小
            dstRect.left = 0;
            dstRect.top = 0;
            dstRect.right = getWidth();
            dstRect.bottom = getHeight();
            canvas.drawBitmap(mCacheBitmap, srcRect, dstRect, null);
        }
    }

    /**
     * 对外开发的调用方法
     *
     * @param list List<Combined> 宫格数据源
     */
    public void drawCombined(List<Combined> list) {
        this.combinedList.clear();
        this.combinedList.addAll(list);
        newCombinedCanvas();
        performDraw();
    }

    private void performDraw() {
        mCombinedCanvas.draw(getContext(), getMeasuredWidth(), getMeasuredHeight(), combinedList, (list, bitmap) -> {
            Log.d(TAG, "onCombinedCanvas: ");
            mCacheBitmap = bitmap;
            postInvalidate();
        });
    }

    /**
     * 初始化
     */
    private void newCombinedCanvas() {
        if (mCombinedCanvas == null) {
            mCombinedCanvas = new CombinedCanvas(mCombinedAttrs);
        }
    }
}
