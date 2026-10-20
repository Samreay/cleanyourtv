package com.gathin.tvhome;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;

/**
 * The backdrop behind the tiles (config.xml / theme key background_style): one small bitmap per app colour,
 * stretched (and filtered) over the screen and cached by MainActivity, so the screen keeps a single background
 * layer and changing apps costs one 240x135 paint. Textured backdrops (grain, dot grid, pinstripes) were tried and
 * dropped: as a separate layer they doubled the frame time on a TCL C755, and baked in they still cost ~40%.
 *
 * Styles: glow (the original radial glow), aurora (two soft blobs, the app colour and a hue-shifted partner),
 * horizon (the colour washing down from the top edge to black).
 */
final class Backdrop {

    static final String GLOW = "glow", AURORA = "aurora", HORIZON = "horizon";

    private Backdrop() {
    }

    /** A known style name, or glow. */
    static String normalise(String style) {
        if (style == null) return GLOW;
        switch (style.trim().toLowerCase(java.util.Locale.ROOT)) {
            case AURORA:
                return AURORA;
            case HORIZON:
                return HORIZON;
            default:
                return GLOW;
        }
    }

    /** The backdrop bitmap for this style and app colour, 240x135. */
    static Bitmap colourLayer(String style, int color) {
        int w = 240, h = 135;
        int c = color & 0x00FFFFFF;
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        if (AURORA.equals(style)) {
            int partner = rotateHue(color, 48f) & 0x00FFFFFF;
            paint.setShader(new RadialGradient(w * 0.22f, h * 0.2f, w * 0.58f,
                    new int[]{0x5C000000 | c, 0x1F000000 | c, c}, new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, paint);
            paint.setShader(new RadialGradient(w * 0.86f, h * 0.92f, w * 0.5f,
                    new int[]{0x4A000000 | partner, 0x18000000 | partner, partner}, new float[]{0f, 0.5f, 1f},
                    Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, paint);
        } else if (HORIZON.equals(style)) {
            paint.setShader(new LinearGradient(0, 0, 0, h * 0.75f,
                    new int[]{0x66000000 | c, 0x24000000 | c, c}, new float[]{0f, 0.4f, 1f}, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, paint);
            // A brighter band along the top edge, like light spilling over a horizon.
            paint.setShader(new RadialGradient(w * 0.5f, -h * 0.25f, w * 0.55f,
                    new int[]{0x40000000 | c, c}, new float[]{0f, 1f}, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, paint);
        } else {
            // glow: the original radial glow.
            paint.setShader(new RadialGradient(w * 0.32f, h * 0.42f, w * 0.62f,
                    new int[]{0x5C000000 | c, 0x26000000 | c, c}, new float[]{0f, 0.45f, 1f}, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, w, h, paint);
        }
        return bmp;
    }

    /** The colour turned round the colour wheel by degrees, same saturation and brightness. */
    static int rotateHue(int color, float degrees) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[0] = (hsv[0] + degrees) % 360f;
        return Color.HSVToColor(0xFF, hsv);
    }
}
