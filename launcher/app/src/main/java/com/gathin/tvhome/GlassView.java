package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

/**
 * The focus highlight: a crisp rim that sits exactly on the focused tile's edge, a soft outer glow and a faint
 * sheen across the top. No fill over the artwork. One instance floats over the grid and is moved with spring
 * animations between tiles; its radius is the focused tile's (scaled) corner radius, so the two line up.
 */
public class GlassView extends View {

    /** Room outside the tile for the glow; MainActivity sizes the view with the same inset. */
    static final float GLOW_DP = 6f;
    private static final int GLOW_STEPS = 12;
    private static final int GLOW_ALPHA = 0x46;

    private final Paint sheen = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rim = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint glow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final RectF ring = new RectF();
    private final float density;
    private float radius;

    public GlassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = getResources().getDisplayMetrics().density;
        radius = 16f * density;
        rim.setStyle(Paint.Style.STROKE);
        rim.setStrokeWidth(1.5f * density);
        rim.setColor(0xF2FFFFFF);
        glow.setStyle(Paint.Style.STROKE);
        // Cache the highlight as a texture. The spring only changes translation, so the GPU moves it without
        // replaying the glow's strokes on every frame (they are software-heavy on the MT5895).
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    /** Corner radius of the focused tile as drawn (its radius times its focus scale). */
    void setRadius(float px) {
        radius = px;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        float inset = GLOW_DP * density;
        sheen.setShader(new LinearGradient(0, inset, 0, inset + (h - 2 * inset) * 0.4f,
                0x24FFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float inset = GLOW_DP * density;
        rect.set(inset, inset, getWidth() - inset, getHeight() - inset);

        // Outer glow: thin concentric rings fading out with distance (quadratic falloff), so it reads as one
        // soft light instead of separate bands. Each ring keeps the tile's shape: radius grows with the offset.
        float step = inset / GLOW_STEPS;
        glow.setStrokeWidth(step + 0.5f);
        for (int i = 0; i < GLOW_STEPS; i++) {
            float o = (i + 0.5f) * step;
            float t = 1f - o / inset;
            glow.setColor(Math.round(GLOW_ALPHA * t * t) << 24 | 0xFFFFFF);
            ring.set(rect.left - o, rect.top - o, rect.right + o, rect.bottom + o);
            canvas.drawRoundRect(ring, radius + o, radius + o, glow);
        }

        // Sheen over the top 40% only, inside the tile's shape.
        canvas.drawRoundRect(rect, radius, radius, sheen);

        // Rim on the tile's edge: inset by half the stroke so its outer edge is the tile's outline.
        float half = rim.getStrokeWidth() / 2f;
        ring.set(rect.left + half, rect.top + half, rect.right - half, rect.bottom - half);
        canvas.drawRoundRect(ring, radius - half, radius - half, rim);
    }
}
