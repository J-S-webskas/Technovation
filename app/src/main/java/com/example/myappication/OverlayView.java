package com.example.myappication;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import java.util.List;

//// Stores visual data only while the video is running. For visuals only Once the video closes it deletes them
public class OverlayView extends View {
    private List<NormalizedLandmark> currentLandmarks;
    private PointF comPoint; // Calculated Center of Mass
    private PointF bosStart, bosEnd; // Calculated Base of Support line
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
        // 1. Define connections (same as Python's POSE_CONNECTIONS)
        int[][] connections = {
                {0, 1}, {1, 2}, {2, 3}, {3, 7},    // Face
                {0, 4}, {4, 5}, {5, 6}, {6, 8},
                {9, 10},                            // Body center
                {11, 12}, {11, 23}, {12, 24}, {23, 24}, // Torso
                {11, 13}, {13, 15},                 // Left arm
                {12, 14}, {14, 16},                 // Right arm
                {23, 25}, {25, 27}, {27, 29}, {29, 31}, // Left leg
                {24, 26}, {26, 28}, {28, 30}, {30, 32}  // Right leg
        };

        // 2. Draw each connection
        for (int[] conn : connections) {
            NormalizedLandmark start = currentLandmarks.get(conn[0]);
            NormalizedLandmark end = currentLandmarks.get(conn[1]);

            float startX = start.x() * width;
            float startY = start.y() * height;
            float endX = end.x() * width;
            float endY = end.y() * height;

            canvas.drawLine(startX, startY, endX, endY, skeletonPaint);
        }

        // 3. Draw landmarks as circles
        for (NormalizedLandmark landmark : currentLandmarks) {
            float x = landmark.x() * width;
            float y = landmark.y() * height;
            canvas.drawCircle(x, y, 5, dotPaint); // 5px radius
        }
    }
}
