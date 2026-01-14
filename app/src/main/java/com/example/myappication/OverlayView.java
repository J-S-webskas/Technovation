package com.example.myappication;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import java.util.List;

public class OverlayView extends View {
    private List<NormalizedLandmark> currentLandmarks;
    private PointF comPoint; // Your calculated Center of Mass
    private PointF bosStart, bosEnd; // Your calculated Base of Support line

    private Paint skeletonPaint = new Paint();
    private Paint dotPaint = new Paint();
    private Paint textPaint = new Paint();

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupPaints();
    }

    private void setupPaints() {
        skeletonPaint.setColor(Color.GREEN);
        skeletonPaint.setStrokeWidth(8.0f);
        skeletonPaint.setStyle(Paint.Style.STROKE);

        dotPaint.setColor(Color.RED);
        dotPaint.setStrokeWidth(20.0f);
        dotPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(48.0f);
    }

    // Call this from the Fragment to update the drawing data
    public void updateLandmarksAndGraphics(List<NormalizedLandmark> landmarks, PointF com, PointF bosS, PointF bosE) {
        this.currentLandmarks = landmarks;
        this.comPoint = com;
        this.bosStart = bosS;
        this.bosEnd = bosE;
        invalidate(); // Triggers onDraw()
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (currentLandmarks == null) return;

        int width = getWidth();
        int height = getHeight();

        // 1. Draw Pose Skeleton (Connect landmarks with lines)
        drawSkeleton(canvas, width, height);

        // 2. Draw Center of Mass (CoM) as a big red dot
        if (comPoint != null) {
            canvas.drawPoint(comPoint.x * width, comPoint.y * height, dotPaint);
        }

        // 3. Draw Base of Support (BoS) as a green line
        if (bosStart != null && bosEnd != null) {
            canvas.drawLine(bosStart.x * width, bosStart.y * height,
                    bosEnd.x * width, bosEnd.y * height, skeletonPaint);
        }

        // 4. Draw Text (e.g., "Balanced" or angle values)
        canvas.drawText("MS Pose Analysis", 50, 100, textPaint);
    }

    private void drawSkeleton(Canvas canvas, int width, int height) {
        // Map normalized landmarks [0,1] to screen pixels and draw lines between them.
        // Example: Draw line between left shoulder (landmark 11) and left hip (23)
        // Implement connections for full skeleton
    }
}
