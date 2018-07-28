package org.xwiki.android.sync.utils;

import android.graphics.Bitmap;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayOutputStream;
import java.util.Random;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ImageUtilsTest {

    private static final Random random = new Random();
    private static final Integer defaultWidth = 16384;
    private static final Integer defaultHeight = 16384;

    @Test
    public void bitmapCompressCorrect() {
        Integer qualityMaxSize = 1024;

        Bitmap resultBitmap = ImageUtils.compressByQuality(
            allocateDefaultBitmap(),
            qualityMaxSize
        );

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        resultBitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);

        assertTrue(
            os.size() / 1024 <= qualityMaxSize
        );
    }

    @Test(expected = NullPointerException.class)
    public void bitmapThrowNullPointerExceptionWhenCompressNull() {
        ImageUtils.compressByQuality(null, 1024);
    }

    private Bitmap allocateDefaultBitmap() {
        Bitmap bitmap = null;

        Integer width = defaultWidth;
        Integer height = defaultHeight;

        while (bitmap == null) {
            try {
                bitmap = Bitmap.createBitmap(
                    width,
                    height,
                    Bitmap.Config.ARGB_8888
                );
            } catch (OutOfMemoryError | IllegalArgumentException e) {
                width /= 2;
                height /= 2;
            }
        }
        bitmap.eraseColor(0x00FF0000);
        for (int x = 0; x < width; x += 2) {
            for (int y = 0; y < height; y += 5) {
                bitmap.setPixel(
                    x,
                    y,
                    random.nextInt()
                );
            }
        }
        return bitmap;
    }
}
