package cn.com.maxpanda.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import java.lang.ref.WeakReference;
import cn.com.maxpanda.scratchview.R;
import cn.com.maxpanda.util.BitmapUtils;

/**
 * Created by MaxPanda on 2020/9/26.
 */
public class ScratchView extends View {
    /**
     * 默认蒙板的颜色
     */
    private final static int DEFAULT_MASKER_COLOR = 0XFF808080;
    /**
     * 默认橡皮擦大小
     */
    private final static int DEFAULT_ERASER_SIZE = 0;
    /**
     * 擦除比例最大值默认值
     */
    private final static float DEFAULT_PERCENT = 1f;
    /**
     * 擦除比例最大值默认值
     */
    private final static int DEFAULT_MODE = 0;

    /**
     * 涂层绘制模式
     */
    public static class Mode {
        // 默认模式
        public static final int NORMAL = 0;
        // 图片模式
        public static final int IMAGE = 1;
        // 水印模式
        public static final int WATERMARK = 2;
    }

    /* ################################################################### */
    private Context mContext;
    /**
     * 橡皮擦大小
     */
    private float mEraseSize;
    /**
     * 涂层颜色
     */
    private int mSurfaceColor;
    /**
     * 涂层绘制模式
     */
    private int mSurfaceDrawMode;
    /**
     * 图层图片资源ID
     */
    private int mSurfaceImageSrcId;
    /**
     * 擦除比例
     */
    private float mMaxPercent;
    /**
     * 最小触摸差值
     */
    private float mTouchSlop = 10;
    /**
     * 擦除轨迹
     */
    private Path mErasePath;
    /**
     * 擦除画笔
     */
    private Paint mErasePaint;
    /**
     * 涂层位图
     */
    private Bitmap mSurfaceBitmap;
    /**
     * 涂层画布
     */
    private Canvas mSurfaceCanvas;
    /**
     * 涂层画笔
     */
    private Paint mSurfacePaint;
    /**
     * 擦除起始坐标
     */
    private float mX, mY;
    /**
     * 擦除状态监听
     */
    private OnEraseStatusListener mOnEraseStatusListener;
    /**
     * 异步更新线程数
     */
    private int mThreadCount;
    /**
     * 当前擦除进度
     */
    private float mPercent;

    public ScratchView(Context context) {
        super(context);
        TypedArray typedArray = context.obtainStyledAttributes(R.styleable.ScratchView);
        mContext = context;
        init(context, typedArray);
    }

    public ScratchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScratchView);
        mContext = context;
        init(context, typedArray);
    }

    public ScratchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScratchView, defStyleAttr, 0);
        mContext = context;
        init(context, typedArray);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ScratchView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScratchView, defStyleAttr, defStyleRes);
        mContext = context;
        init(context, typedArray);
    }

    private void init(Context context, TypedArray typedArray) {
        mSurfaceColor = typedArray.getColor(R.styleable.ScratchView_surfaceColor, DEFAULT_MASKER_COLOR);
        mSurfaceImageSrcId = typedArray.getResourceId(R.styleable.ScratchView_surfaceImageSrc, -1);
        mEraseSize = typedArray.getFloat(R.styleable.ScratchView_eraseSize, DEFAULT_ERASER_SIZE);
        mMaxPercent = typedArray.getFloat(R.styleable.ScratchView_maxPercent, DEFAULT_PERCENT);
        mSurfaceDrawMode = typedArray.getInt(R.styleable.ScratchView_surfaceDrawMode, DEFAULT_MODE);
        typedArray.recycle();

        // 涂层画笔初始化
        mSurfacePaint = new Paint();
        mSurfacePaint.setAntiAlias(true);
        mSurfacePaint.setDither(true);
        mSurfacePaint.setColor(mSurfaceColor);

        // 擦除画笔初始化
        mErasePaint = new Paint();
        mErasePaint.setAntiAlias(true);
        mErasePaint.setDither(true);
        mErasePaint.setStyle(Paint.Style.STROKE);
        mErasePaint.setStrokeJoin(Paint.Join.BEVEL);
        mErasePaint.setStrokeCap(Paint.Cap.ROUND);
        mErasePaint.setStrokeWidth(mEraseSize);
        mErasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mErasePath = new Path();

        ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制涂层遮罩
        canvas.drawBitmap(mSurfaceBitmap, 0, 0, mSurfacePaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        createSurfaceBitmap(w, h);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        // 当没有设置橡皮擦大小时 默认为手指接触面积
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_down(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mEraseSize == 0) {
                    mErasePaint.setStrokeWidth(event.getPressure());
                }
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 手指按下事件
     *
     * @param x
     * @param y
     */
    private void touch_down(float x, float y) {
        mErasePath.reset();
        mErasePath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    /**
     * 手指移动事件
     *
     * @param x
     * @param y
     */
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= mTouchSlop || dy >= mTouchSlop) {
            mErasePath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
            mErasePath.lineTo(mX, mY);
            mSurfaceCanvas.drawPath(mErasePath, mErasePaint);
            mErasePath.reset();
            mErasePath.moveTo(mX, mY);
            if(mThreadCount > 1) {
                Log.d("Captcha", "Count greater than 1");
                return;
            }
            mThreadCount++;
            new UpdateErasePercentTask(this).execute(mSurfaceBitmap);
        }

    }

    /**
     * 手指抬起事件
     */
    private void touch_up() {
        mX = 0;
        mY = 0;
        mErasePath.reset();
    }

    /**
     * 绘制表面的涂层
     *
     * @param width
     * @param height
     */
    private void createSurfaceBitmap(int width, int height) {
        mPercent = 0f;
        mSurfaceBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mSurfaceCanvas = new Canvas(mSurfaceBitmap);
        switch (mSurfaceDrawMode) {
            case Mode.IMAGE:
                createImageSurfaceBitmap();
                break;
            case Mode.WATERMARK:
                createWatermarkSurfaceBitmap(width, height);
                break;
            case Mode.NORMAL:
            default:
                createNormalSurfaceBitmap(width, height);
        }
    }

    /**
     * 创建一个图片涂层
     *
     */
    private void createImageSurfaceBitmap() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            @SuppressLint("UseCompatLoadingForDrawables")
            Drawable drawable = mContext.getDrawable(mSurfaceImageSrcId);
            if (drawable == null) {
                drawable = new BitmapDrawable();
            }
            drawable.setBounds(0, 0, mSurfaceBitmap.getWidth(), mSurfaceBitmap.getHeight());
            drawable.draw(mSurfaceCanvas);
        } else {
            BitmapDrawable mDrawable = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(mContext.getResources(), mSurfaceImageSrcId));
            mDrawable.setBounds(0, 0, mSurfaceBitmap.getWidth(), mSurfaceBitmap.getHeight());
            mDrawable.draw(mSurfaceCanvas);

        }
    }

    /**
     * 创建一个默认的涂层
     *
     * @param width
     * @param height
     */
    private void createNormalSurfaceBitmap(int width, int height) {
        Rect rect = new Rect(0, 0, width, height);
        mSurfaceCanvas.drawRect(rect, mSurfacePaint);
    }

    /**
     * 创建一个带水印的涂层
     *
     * @param width
     * @param height
     */
    private void createWatermarkSurfaceBitmap(int width, int height) {
        Bitmap srcBitmap;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            @SuppressLint("UseCompatLoadingForDrawables")
            Drawable drawable = mContext.getDrawable(mSurfaceImageSrcId);
            srcBitmap = BitmapUtils.drawableToBitmap(drawable);
        } else {
            srcBitmap = BitmapFactory.decodeResource(mContext.getResources(), mSurfaceImageSrcId);
        }
        createNormalSurfaceBitmap(width, height);
        BitmapDrawable mDrawable = new BitmapDrawable(getResources(), srcBitmap);
        mDrawable.setBounds(0, 0, width, height);
        mDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        mDrawable.draw(mSurfaceCanvas);
    }

    /**
     * 重置为初始状态
     */
    public void reset() {
        int width = getWidth();
        int height = getHeight();
        createSurfaceBitmap(width, height);
        invalidate();
    }

    /**
     * 清除整个图层
     */
    public void clear() {
        int width = getWidth();
        int height = getHeight();
        mSurfaceBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mSurfaceCanvas = new Canvas(mSurfaceBitmap);
        Rect rect = new Rect(0, 0, width, height);
        mSurfaceCanvas.drawRect(rect, mErasePaint);
        invalidate();
    }

    public float getEraseSize() {
        return mEraseSize;
    }

    public void setEraseSize(float eraseSize) {
        mEraseSize = eraseSize;
        mErasePaint.setStrokeWidth(eraseSize);
    }

    public int getSurfaceColor() {
        return mSurfaceColor;

    }

    public void setSurfaceColor(int surfaceColor) {
        mSurfaceColor = surfaceColor;
        mSurfacePaint.setColor(surfaceColor);
    }

    public void setSurfaceDrawMode(int surfaceDrawMode) {
        this.mSurfaceDrawMode = surfaceDrawMode;
    }

    public void setMaxPercent(float maxPercent) {
        if (maxPercent > 1 || maxPercent <= 0) {
            return;
        }
        mMaxPercent = maxPercent;
    }

    public float getMaxPercent() {
        return mMaxPercent;
    }

    public float getPercent() {
        return mPercent;
    }

    public void setPercent(float percent) {
        mPercent = percent;
    }

    public void setOnEraseStatusListener(OnEraseStatusListener onEraseStatusListener) {
        mOnEraseStatusListener = onEraseStatusListener;
    }

    public OnEraseStatusListener getOnEraseStatusListener() {
        return mOnEraseStatusListener;
    }

    public void setSurfaceImageSrcId(int surfaceImageSrcId) {
        this.mSurfaceImageSrcId = surfaceImageSrcId;
    }

    /**
     * 擦除状态监听器
     */
    public interface OnEraseStatusListener {
        /**
         * 擦除进度
         *
         * @param percent 进度值，大于0，小于等于1；
         */
        void onProgress(float percent);

        /**
         * 擦除完成回调函数
         *
         * @param view
         */
        void onCompleted(View view);
    }

    /**
     * 更新擦除进度的异步任务
     */
    private static class UpdateErasePercentTask extends AsyncTask<Bitmap, Void, Float> {

        private WeakReference<ScratchView> scratchViewReference;

        UpdateErasePercentTask(ScratchView scratchView) {
            scratchViewReference = new WeakReference<>(scratchView);
        }

        @Override
        protected Float doInBackground(Bitmap... params) {
            return BitmapUtils.getTransparentPixelPercent(params[0]);
        }

        @Override
        protected void onPostExecute(Float percent) {
            ScratchView scratchView = scratchViewReference.get();
            if (scratchView != null && scratchView.getOnEraseStatusListener() != null) {
                scratchView.mThreadCount--;
                scratchView.mPercent = percent;
                if (percent < scratchView.getMaxPercent()) {
                    scratchView.getOnEraseStatusListener().onProgress(percent);
                } else {
                    scratchView.getOnEraseStatusListener().onCompleted(scratchView);
                }
            }
        }
    }
}
