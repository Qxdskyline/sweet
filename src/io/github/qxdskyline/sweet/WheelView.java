package io.github.qxdskyline.sweet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;
import android.text.TextPaint;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

final class WheelView extends View {
    private static final int[] COLORS = {
        0xffeac4ba, 0xfff5e1c7, 0xffd8e2d4, 0xffebd4df, 0xfff0d2bd, 0xffe3ddf0
    };
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint shape = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final TextPaint labels = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final List<String> options = new ArrayList<>();
    private Bitmap disc;

    WheelView(Context context) {
        super(context);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    void setOptions(List<String> values) {
        options.clear();
        options.addAll(values);
        renderDisc();
        invalidate();
    }

    static float targetAngle(int index, int size) {
        return (360f - (index + .5f) * (360f / size)) % 360f;
    }

    @Override protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        renderDisc();
    }

    private void renderDisc() {
        int side = Math.min(getWidth(), getHeight());
        if (side == 0 || options.isEmpty()) {
            disc = null;
            return;
        }
        Bitmap rendered = Bitmap.createBitmap(side, side, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(rendered);
        float center = side / 2f;
        float rim = side * .025f;
        float radius = center - rim;
        shape.setColor(0xfffbf4e9);
        canvas.drawCircle(center, center, center, shape);
        RectF bounds = new RectF(rim, rim, side - rim, side - rim);
        float sweep = 360f / options.size();
        labels.setColor(0xff423035);
        labels.setTextAlign(Paint.Align.CENTER);
        labels.setTypeface(android.graphics.Typeface.create("sans-serif-medium", 0));
        labels.setTextSize(side * (options.size() > 12 ? .035f : .045f));
        float baseline = -(labels.ascent() + labels.descent()) / 2f;
        for (int i = 0; i < options.size(); i++) {
            int colorIndex = i % COLORS.length;
            if (i == options.size() - 1 && colorIndex == 0) colorIndex = 2;
            shape.setColor(COLORS[colorIndex]);
            canvas.drawArc(bounds, -90f + i * sweep, sweep, true, shape);
        }
        shape.setColor(0xfffdf4e9);
        shape.setStrokeWidth(Math.max(1f, side * .004f));
        for (int i = 0; i < options.size(); i++) {
            float edge = -90f + i * sweep;
            double radians = Math.toRadians(edge);
            canvas.drawLine(center, center, center + radius * (float) Math.cos(radians),
                center + radius * (float) Math.sin(radians), shape);
            float angle = edge + sweep / 2f;
            String label = TextUtils.ellipsize(options.get(i), labels, radius * .61f,
                TextUtils.TruncateAt.END).toString();
            canvas.save();
            canvas.translate(center, center);
            canvas.rotate(angle);
            canvas.translate(radius * .62f, 0f);
            if (angle > 90f && angle < 270f) canvas.rotate(180f);
            canvas.drawText(label, 0f, baseline, labels);
            canvas.restore();
        }
        shape.setColor(0xffa25361);
        canvas.drawCircle(center, center, side * .075f, shape);
        shape.setColor(Color.WHITE);
        canvas.drawCircle(center, center, side * .023f, shape);
        disc = rendered;
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (disc != null) {
            canvas.drawBitmap(disc, (getWidth() - disc.getWidth()) / 2f,
                (getHeight() - disc.getHeight()) / 2f, paint);
        }
    }
}
