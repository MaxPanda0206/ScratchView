package cn.com.maxpanda.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.nio.ByteBuffer;

/**
 * Created by MaxPanda on 2020/9/26.
 */
public class BitmapUtils {

    /**
     * 将 Drawable 转化成 Bitmap
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 获取 Bitmap 透明像素所占比例
     * @param bitmap
     * @return 透明系数 0f - 1f
     */
    public static float getTransparentPixelPercent(Bitmap bitmap) {
        if (bitmap != null) {
            ByteBuffer buffer = ByteBuffer.allocate(bitmap.getHeight() * bitmap.getRowBytes());
            bitmap.copyPixelsToBuffer(buffer);
            byte[] array = buffer.array();
            // 透明像素
            float transparentPixelCount = 0;
            //总像素个数
            float totalPixelCount = array.length;
            for (byte b : array) {
                if (b == 0) {//透明的像素值为 0
                    transparentPixelCount++;
                }
            }

            return transparentPixelCount / totalPixelCount;
        }
        return 0f;
    }
}

