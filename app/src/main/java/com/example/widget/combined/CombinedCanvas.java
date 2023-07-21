package com.example.widget.combined;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Create time 2023/7/18 15:50
 * Email: shuaiwang@grandstream.cn
 *
 * @author wangshuai
 * @version 1.0
 */
public class CombinedCanvas {
    public static final String TAG = "CombinedCanvas";
    Context mContext;
    OnCombinedCanvasCallback mCallback;
    /**
     * 画布的宽高
     */
    int width, height;
    /**
     * 画笔
     */
    Paint mPaint;
    /**
     * 裁剪图层的大小范围
     */
    RectF mOverlayRectF;
    /**
     * Bitmap 的大小范围
     */
    Rect srcRect = new Rect();
    /**
     * 图层裁剪模式 PorterDuff.Mode.DST_IN
     */
    PorterDuffXfermode mPorterMode;
    /**
     * 主图层和裁剪图层画布
     */
    Canvas mCanvas, mOverlayCanvas;
    /**
     * 主图层Bitmap和裁剪图层Bitmap
     */
    Bitmap mCanvasBitmap, mOverlayBitmap;
    /**
     * 宫格数据源
     */
    List<Combined> mCombinedList = new ArrayList<>();
    /**
     * 宫格的位置数据源
     */
    List<RectF> mCombinedCells = new ArrayList<>();
    /**
     * 链接头像的缓存数据源
     */
    Map<Integer, Bitmap> mBitmapCache = new HashMap<>();
    /**
     * 自定义属性
     */
    CombinedAttrs mCombinedAttrs;

    public CombinedCanvas(CombinedAttrs attrs) {
        this.mCombinedAttrs = attrs;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mPorterMode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
        mOverlayRectF = new RectF();
    }

    /**
     * 对外提供的绘制调用方法
     *
     * @param context      Context 上下文环境
     * @param width        int 画布宽度
     * @param height       int 画布高度
     * @param combinedList List<Combined> 宫格数据源
     * @param callback     OnCombinedCanvasCallback 头像回调接口
     */
    public void draw(Context context, int width, int height, List<Combined> combinedList, OnCombinedCanvasCallback callback) {
        if (width == 0 || height == 0) {
            Log.w(TAG, "the canvas width and height must be > 0 ");
            return;
        }
        this.mContext = context;
        this.width = width;
        this.height = height;
        this.mCallback = callback;
        this.mCombinedList.clear();
        this.mCombinedList.addAll(combinedList);

        newCombinedCanvas();
        newOverlayCanvas();
        loadCellImage();
    }

    /**
     * 判断加载头像链接
     */
    private void loadCellImage() {
        computeCombinedCellLocation();
        mBitmapCache.clear();
        for (int index = 0; index < mCombinedList.size(); index++) {
            Combined combined = mCombinedList.get(index);
            RectF rectF = mCombinedCells.get(index);
            if (hasURL(combined)) {
                Log.d(TAG, "loadCellImage: ");
                CombinedLoader.getInstance().loadImage(mContext, (int) rectF.width(), (int) rectF.height(), index,
                        combined.getUrl(), (retCode, index1, newBitmap) -> {
                            Log.d(TAG, "loadCellImage index:" + index1 + " draw new bitmap: " + newBitmap);
                            mBitmapCache.put(index1, newBitmap);
                            performDraw();
                        });
            }
        }
        performDraw();
    }

    /**
     * 创建主图层
     */
    private void newCombinedCanvas() {
        if (mCanvas == null) {
            mCanvasBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mCanvasBitmap);
        }
        mCanvas.drawColor(mCombinedAttrs.backgroundColor);
    }

    /**
     * 创建裁剪图层
     */
    private void newOverlayCanvas() {
        if (mOverlayCanvas == null) {
            mOverlayBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            mOverlayCanvas = new Canvas(mOverlayBitmap);
        }
        mPaint.setXfermode(null);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mCombinedAttrs.backgroundColor);

        mOverlayRectF.left = 0;
        mOverlayRectF.top = 0;
        mOverlayRectF.right = getWidth();
        mOverlayRectF.bottom = getHeight();

        if (mCombinedAttrs.shape == CombinedShape.ROUND) {
            mOverlayCanvas.drawRoundRect(mOverlayRectF, mCombinedAttrs.roundRadius, mCombinedAttrs.roundRadius, mPaint);
        } else if (mCombinedAttrs.shape == CombinedShape.CIRCLE) {
            float centerX = getWidth() * 1f / 2;
            float centerY = getHeight() * 1f / 2;
            float circleRadius = Math.min(centerX, centerY);
            mOverlayCanvas.drawCircle(centerX, centerY, circleRadius, mPaint);
        }
    }

    /**
     * 绘制头像
     */
    private void performDraw() {
        this.mPaint.setXfermode(null);
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStyle(Paint.Style.STROKE);

        drawCombined();
        drawDividerLine();
        drawOverlay();
        onResultCallback();
    }

    /**
     * 绘制每个宫格的头像
     */
    private void drawCombined() {
        mCanvas.drawColor(mCombinedAttrs.backgroundColor);
        for (int i = 0; i < mCombinedCells.size(); i++) {
            RectF rectF = mCombinedCells.get(i);
            Combined combined = mCombinedList.get(i);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(combined.getBackgroundColor());
            mCanvas.drawRect(rectF, mPaint);

            if (hasURL(combined)) {
                Bitmap bitmap = mBitmapCache.get(i);
                if (bitmap != null) {
                    srcRect.left = 0;
                    srcRect.top = 0;
                    srcRect.right = bitmap.getWidth();
                    srcRect.bottom = bitmap.getHeight();
                    mCanvas.drawBitmap(bitmap, srcRect, rectF, null);
                }
            } else {
                drawText(combined, rectF, i);
            }
        }
    }

    /**
     * 绘制宫格的文本头像
     *
     * @param combined Combined 宫格信息
     * @param rectF    RectF 坐位置
     * @param index    int 宫格下标
     */
    private void drawText(Combined combined, RectF rectF, int index) {
        float left = rectF.left;
        float top = rectF.top;
        float textSize = computeTextSize(index);
        String text = getText(combined);

        mPaint.setTextSize(textSize);
        mPaint.setColor(mCombinedAttrs.textColor);

        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        float textWidth = mPaint.measureText(text);


        float textTop = fontMetrics.top;
        float textBottom = fontMetrics.bottom;
        float textHeight = textBottom + textTop;

        float x = left + rectF.width() / 2 - textWidth / 2;
        float y = top + rectF.height() / 2 - textHeight / 2;
        mCanvas.drawText(text, x, y, mPaint);
    }

    /**
     * 绘制宫格间的分割线
     */
    private void drawDividerLine() {
        int size = mCombinedList.size();
        float dividerWidth = mCombinedAttrs.dividerWidth;
        float offset = dividerWidth / 2;
        float startX, startY;
        float endX, endY;

        mPaint.setColor(mCombinedAttrs.dividerColor);
        mPaint.setStrokeWidth(dividerWidth);

        if (size == 2) {
            startY = 0;
            startX = getWidth() * 1f / 2 - offset;
            endX = getWidth() * 1f / 2 - offset;
            endY = getHeight();
            mCanvas.drawLine(startX, startY, endX, endY, mPaint);
        } else if (size == 3) {
            startX = getWidth() * 1f / 2 - offset;
            startY = 0;
            endX = getWidth() * 1f / 2 - offset;
            endY = getHeight();
            mCanvas.drawLine(startX, startY, endX, endY, mPaint);

            startX = getWidth() * 1f / 2;
            startY = getHeight() * 1f / 2 - offset;
            endX = getWidth();
            endY = getHeight() * 1f / 2 - offset;
            mCanvas.drawLine(startX, startY, endX, endY, mPaint);
        } else if (size == 4) {
            startX = getWidth() * 1f / 2 - offset;
            startY = 0;
            endX = getWidth() * 1f / 2 - offset;
            endY = getHeight();
            mCanvas.drawLine(startX, startY, endX, endY, mPaint);

            startX = 0;
            startY = getHeight() * 1f / 2 - offset;
            endX = getWidth();
            endY = getHeight() * 1f / 2 - offset;
            mCanvas.drawLine(startX, startY, endX, endY, mPaint);
        } else {
            List<float[]> cellLocations = computeCells(size);
            float stepX = getWidth() * 1f / 3;
            float stepY = getHeight() * 1f / 3;
            if (size == 5) {
                float[] location = cellLocations.get(0);

                startX = location[0] + stepX - offset;
                endX = location[0] + stepX - offset;
                startY = location[1];
                endY = location[1] + stepY;
                mCanvas.drawLine(startX, startY, endX, endY, mPaint);


                location = cellLocations.get(2);
                startX = location[0];
                endX = location[0] + getHeight();
                startY = location[1] - offset;
                endY = location[1] - offset;
                mCanvas.drawLine(startX, startY, endX, endY, mPaint);


                startX = location[0] + stepX - offset;
                endX = location[0] + stepX - offset;
                startY = location[1];
                endY = location[1] + stepY;
                mCanvas.drawLine(startX, startY, endX, endY, mPaint);

                startX = location[0] + stepX * 2 - offset;
                endX = location[0] + stepX * 2 - offset;
                startY = location[1];
                endY = location[1] + stepY;
                mCanvas.drawLine(startX, startY, endX, endY, mPaint);

            } else if (size == 6) {
                float[] loc = cellLocations.get(1);
                startX = loc[0] - offset;
                endX = loc[0] - offset;
                startY = loc[1];
                endY = loc[1] + stepY * 2;
                mCanvas.drawLine(startX, startY, endX, endY, mPaint);

                loc = cellLocations.get(2);
                startX = loc[0] - offset;
                endX = loc[0] - offset;
                startY = loc[1];
                endY = loc[1] + stepY * 2;
                mCanvas.drawLine(startX, startY, endX, endY, mPaint);

                loc = cellLocations.get(3);
                startX = loc[0];
                endX = loc[0] + getWidth();
                startY = loc[1] - offset;
                endY = loc[1] - offset;
                mCanvas.drawLine(startX, startY, endX, endY, mPaint);

            } else if (size == 7) {
                float[] loc = cellLocations.get(2);
                startX = 0;
                endX = getWidth();
                startY = loc[1] - offset;
                endY = loc[1] - offset;
                mCanvas.drawLine(startX, startY, endX, endY, mPaint);

                startX = loc[0] - offset;
                endX = loc[0] - offset;
                startY = loc[1];
                endY = loc[1] + stepY * 2;
                mCanvas.drawLine(startX, startY, endX, endY, mPaint);

                loc = cellLocations.get(3);
                startX = loc[0] - offset;
                endX = loc[0] - offset;
                startY = loc[1];
                endY = loc[1] + stepY * 2;
                mCanvas.drawLine(startX, startY, endX, endY, mPaint);

                loc = cellLocations.get(4);
                startX = loc[0];
                endX = loc[0] + getWidth();
                startY = loc[1] - offset;
                endY = loc[1] - offset;
                mCanvas.drawLine(startX, startY, endX, endY, mPaint);

            } else if (size == 8) {
                float[] loc = cellLocations.get(1);
                startX = loc[0] - offset;
                endX = loc[0] - offset;
                startY = loc[1];
                endY = loc[1] + stepY;
                mCanvas.drawLine(startX, startY, endX, endY, mPaint);

                loc = cellLocations.get(3);
                startX = 0;
                endX = getWidth();
                startY = loc[1] - offset;
                endY = loc[1] - offset;
                mCanvas.drawLine(startX, startY, endX, endY, mPaint);

                startX = loc[0] - offset;
                endX = loc[0] - offset;
                startY = loc[1];
                endY = loc[1] + stepY * 2;
                mCanvas.drawLine(startX, startY, endX, endY, mPaint);

                loc = cellLocations.get(4);
                startX = loc[0] - offset;
                endX = loc[0] - offset;
                startY = loc[1];
                endY = loc[1] + stepY * 2;
                mCanvas.drawLine(startX, startY, endX, endY, mPaint);

                loc = cellLocations.get(5);
                startX = loc[0];
                endX = loc[0] + getWidth();
                startY = loc[1] - offset;
                endY = loc[1] - offset;
                mCanvas.drawLine(startX, startY, endX, endY, mPaint);

            } else if (size == 9) {
                float[] loc = cellLocations.get(1);
                startX = loc[0] - offset;
                endX = loc[0] - offset;
                startY = loc[1];
                endY = loc[1] + getHeight();
                mCanvas.drawLine(startX, startY, endX, endY, mPaint);

                loc = cellLocations.get(2);
                startX = loc[0] - offset;
                endX = loc[0] - offset;
                startY = loc[1];
                endY = loc[1] + getHeight();
                mCanvas.drawLine(startX, startY, endX, endY, mPaint);

                loc = cellLocations.get(3);
                startX = loc[0];
                endX = loc[0] + getWidth();
                startY = loc[1] - offset;
                endY = loc[1] - offset;
                mCanvas.drawLine(startX, startY, endX, endY, mPaint);

                loc = cellLocations.get(6);
                startX = loc[0];
                endX = loc[0] + getWidth();
                startY = loc[1] - offset;
                endY = loc[1] - offset;
                mCanvas.drawLine(startX, startY, endX, endY, mPaint);
            }
        }
    }

    /**
     * 绘制覆盖裁剪图层
     */
    private void drawOverlay() {
        mPaint.setXfermode(mPorterMode);
        mCanvas.drawBitmap(mOverlayBitmap, 0, 0, mPaint);
    }

    /**
     * 计算宫格的起始坐标和位置
     */
    private void computeCombinedCellLocation() {
        int size = mCombinedList.size();
        List<RectF> rectList = new ArrayList<>(size);
        for (int index = 0; index < mCombinedList.size(); index++) {
            RectF rectF = computeCellRectF(index, size);
            rectList.add(rectF);
        }
        mCombinedCells.clear();
        mCombinedCells.addAll(rectList);
    }

    /**
     * 计算每个宫格的坐标位置
     *
     * @param index int
     * @param size  int
     * @return RectF
     */
    private RectF computeCellRectF(int index, int size) {
        RectF rectF = new RectF();
        float left = 0;
        float top = 0;
        float width = getWidth();
        float height = getHeight();

        if (size == 2) {
            width = getWidth() * 1f / 2;
            if (index == 1) {
                left = getWidth() * 1f / 2;
                top = 0;
            }
        } else if (size == 3) {
            width = getWidth() * 1f / 2;
            if (index == 1) {
                height = getHeight() * 1f / 2;
                left = getWidth() * 1f / 2;
                top = 0;
            } else if (index == 2) {
                height = getHeight() * 1f / 2;
                left = getWidth() * 1f / 2;
                top = getHeight() * 1f / 2;
            }
        } else if (size == 4) {
            width = getWidth() * 1f / 2;
            height = getHeight() * 1f / 2;
            if (index == 0) {
                left = 0;
                top = 0;
            } else if (index == 1) {
                left = getWidth() * 1f / 2;
                top = 0;
            } else if (index == 2) {
                left = 0;
                top = getHeight() * 1f / 2;
            } else if (index == 3) {
                left = getWidth() * 1f / 2;
                top = getHeight() * 1f / 2;
            }
        } else if (size >= 5) {
            width = getWidth() * 1f / 3;
            height = getHeight() * 1f / 3;
            float[] location = computeCells(size).get(index);
            left = location[0];
            top = location[1];
        }
        rectF.left = left;
        rectF.top = top;
        rectF.right = left + width;
        rectF.bottom = top + height;
        return rectF;
    }

    /**
     * 计算宫格起始的x,y坐标
     *
     * @param size int
     * @return List<float [ ]>
     */
    private List<float[]> computeCells(int size) {
        List<float[]> locationList = new ArrayList<>();
        float left;
        float top;
        float width = getWidth() * 1f / 3;
        float height = getHeight() * 1f / 3;
        int residue = size % 3;
        int row = size / 3 + (residue > 0 ? 1 : 0);
        float y = computeY(row);
        Log.d(TAG, "computeCells row: " + row);
        for (int i = 0; i < row; i++) {
            top = y + height * i;
            int column = computeColumn(i, residue);
            float x = computeX(i, residue);
            for (int k = 0; k < column; k++) {
                left = x + k * width;
                locationList.add(new float[]{left, top});
            }
        }
        return locationList;
    }

    /**
     * 计算每行的列数
     *
     * @param index   int  行数
     * @param residue int  余数
     * @return int
     */
    private int computeColumn(int index, int residue) {
        if (index == 0 && residue > 0) {
            return residue;
        }
        return 3;
    }

    /**
     * 计算每行起始的y轴的位置
     *
     * @param row int 行数
     * @return float
     */
    private float computeY(int row) {
        float height = getHeight() * 1f / 3;
        return row == 3 ? 0 : height / 2;
    }

    /**
     * 计算每行起始的x轴的位置
     *
     * @param row     行数
     * @param residue 余数
     * @return float
     */
    private float computeX(int row, int residue) {
        if (row == 0) {
            float width = getWidth() * 1f / 3;
            if (residue == 1) {
                return getWidth() * 1f / 2 - width / 2;
            } else if (residue == 2) {
                return width / 2;
            }
        }
        return 0;
    }

    /**
     * 返回画布的宽度
     *
     * @return int
     */
    private int getWidth() {
        return width;
    }

    /**
     * 返回画布的高度
     *
     * @return int
     */
    private int getHeight() {
        return height;
    }

    /**
     * @param combined Combined
     * @return boolean
     */
    private boolean hasURL(Combined combined) {
        return combined != null && !TextUtils.isEmpty(combined.getUrl());
    }

    /**
     * 获取每个宫格的文本
     *
     * @param combined Combined
     * @return String
     */
    private String getText(Combined combined) {
        String text = combined.getText();
        if (TextUtils.isEmpty(text)) {
            text = "unknown";
        }
        Log.d(TAG, "getText: " + text);
        boolean single = mCombinedList.size() == 1;
        return single ? text.substring(0, Math.min(2, text.length())) : text.substring(0, 1);
    }

    /**
     * 计算每个宫格文本的文字大小
     *
     * @param index int
     * @return float
     */
    private float computeTextSize(int index) {
        float baseTextSize = mCombinedAttrs.textSize;
        int size = mCombinedList.size();
        float textSize = baseTextSize;
        if (size == 2) {
            textSize = baseTextSize * 0.8f;
        } else if (size == 3) {
            if (index == 0) {
                textSize = baseTextSize * 0.8f;
            } else {
                textSize = baseTextSize * 0.6f;
            }
        } else if (size == 4) {
            textSize = baseTextSize * 0.6f;
        } else if (size >= 5) {
            textSize = baseTextSize * 0.5f;
        }
        return textSize;
    }

    /**
     * 头像绘制完成，回调刷新UI
     */
    private void onResultCallback() {
        Bitmap bitmap = Bitmap.createBitmap(mCanvasBitmap);
        if (mCallback != null) {
            mCallback.onCombinedCanvas(mCombinedList, bitmap);
        }
    }

    public interface OnCombinedCanvasCallback {
        /**
         * 头像回调
         *
         * @param list   List<Combined>
         * @param bitmap Bitmap
         */
        void onCombinedCanvas(List<Combined> list, Bitmap bitmap);
    }
}
