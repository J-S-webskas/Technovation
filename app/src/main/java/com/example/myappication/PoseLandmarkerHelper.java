package com.example.myappication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import androidx.annotation.VisibleForTesting;
import androidx.camera.core.ImageProxy;
import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.core.Delegate;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker;
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PoseLandmarkerHelper {
    // Constants (converted from Kotlin companion object)
    public static final String TAG = "PoseLandmarkerHelper";
    public static final int DELEGATE_CPU = 0;
    public static final int DELEGATE_GPU = 1;
    public static final float DEFAULT_POSE_DETECTION_CONFIDENCE = 0.5F;
    public static final float DEFAULT_POSE_TRACKING_CONFIDENCE = 0.5F;
    public static final float DEFAULT_POSE_PRESENCE_CONFIDENCE = 0.5F;
    public static final int DEFAULT_NUM_POSES = 1;
    public static final int OTHER_ERROR = 0;
    public static final int GPU_ERROR = 1;
    public static final int MODEL_POSE_LANDMARKER_FULL = 0;
    public static final int MODEL_POSE_LANDMARKER_LITE = 1;
    public static final int MODEL_POSE_LANDMARKER_HEAVY = 2;

    // Properties
    public float minPoseDetectionConfidence;
    public float minPoseTrackingConfidence;
    public float minPosePresenceConfidence;
    public int currentModel;
    public int currentDelegate;
    public RunningMode runningMode;
    public Context context;
    public LandmarkerListener poseLandmarkerHelperListener;

    private PoseLandmarker poseLandmarker;

    // Constructor matching Kotlin's primary constructor with default values
    public PoseLandmarkerHelper(Context context) {
        this(DEFAULT_POSE_DETECTION_CONFIDENCE,
                DEFAULT_POSE_TRACKING_CONFIDENCE,
                DEFAULT_POSE_PRESENCE_CONFIDENCE,
                MODEL_POSE_LANDMARKER_FULL,
                DELEGATE_CPU,
                RunningMode.IMAGE,
                context,
                null);
    }

    public PoseLandmarkerHelper(
            float minPoseDetectionConfidence,
            float minPoseTrackingConfidence,
            float minPosePresenceConfidence,
            int currentModel,
            int currentDelegate,
            RunningMode runningMode,
            Context context,
            LandmarkerListener poseLandmarkerHelperListener
    ) {
        this.minPoseDetectionConfidence = minPoseDetectionConfidence;
        this.minPoseTrackingConfidence = minPoseTrackingConfidence;
        this.minPosePresenceConfidence = minPosePresenceConfidence;
        this.currentModel = currentModel;
        this.currentDelegate = currentDelegate;
        this.runningMode = runningMode;
        this.context = context;
        this.poseLandmarkerHelperListener = poseLandmarkerHelperListener;

        this.setupPoseLandmarker();
    }

    public void clearPoseLandmarker() {
        if (poseLandmarker != null) {
            poseLandmarker.close();
        }
        poseLandmarker = null;
    }

    public boolean isClose() {
        return poseLandmarker == null;
    }

    public void setupPoseLandmarker() {
        BaseOptions.Builder baseOptionBuilder = BaseOptions.builder();

        switch (currentDelegate) {
            case DELEGATE_CPU:
                baseOptionBuilder.setDelegate(Delegate.CPU);
                break;
            case DELEGATE_GPU:
                baseOptionBuilder.setDelegate(Delegate.GPU);
                break;
        }

        String modelName;
        switch (currentModel) {
            case MODEL_POSE_LANDMARKER_FULL:
                modelName = "pose_landmarker_full.task";
                break;
            case MODEL_POSE_LANDMARKER_LITE:
                modelName = "pose_landmarker_lite.task";
                break;
            case MODEL_POSE_LANDMARKER_HEAVY:
                modelName = "pose_landmarker_heavy.task";
                break;
            default:
                modelName = "pose_landmarker_full.task";
                break;
        }

        baseOptionBuilder.setModelAssetPath(modelName);

        if (runningMode == RunningMode.LIVE_STREAM) {
            if (poseLandmarkerHelperListener == null) {
                throw new IllegalStateException(
                        "poseLandmarkerHelperListener must be set when runningMode is LIVE_STREAM."
                );
            }
        }

        try {
            BaseOptions baseOptions = baseOptionBuilder.build();
            PoseLandmarker.PoseLandmarkerOptions.Builder optionsBuilder =
                    PoseLandmarker.PoseLandmarkerOptions.builder()
                            .setBaseOptions(baseOptions)
                            .setMinPoseDetectionConfidence(minPoseDetectionConfidence)
                            .setMinTrackingConfidence(minPoseTrackingConfidence)
                            .setMinPosePresenceConfidence(minPosePresenceConfidence)
                            .setRunningMode(runningMode);

            if (runningMode == RunningMode.LIVE_STREAM) {
                optionsBuilder
                        .setResultListener(this::returnLivestreamResult)
                        .setErrorListener(this::returnLivestreamError);
            }

            PoseLandmarker.PoseLandmarkerOptions options = optionsBuilder.build();
            poseLandmarker = PoseLandmarker.createFromOptions(context, options);
        } catch (IllegalStateException e) {
            if (poseLandmarkerHelperListener != null) {
                poseLandmarkerHelperListener.onError(
                        "Pose Landmarker failed to initialize. See error logs for details"
                );
            }
            Log.e(TAG, "MediaPipe failed to load the task with error: " + e.getMessage());
        } catch (RuntimeException e) {
            if (poseLandmarkerHelperListener != null) {
                poseLandmarkerHelperListener.onError(
                        "Pose Landmarker failed to initialize. See error logs for details",
                        GPU_ERROR
                );
            }
            Log.e(TAG, "Image classifier failed to load model with error: " + e.getMessage());
        }
    }
    public void detectLiveStream(ImageProxy imageProxy, boolean isFrontCamera) {
        if (runningMode != RunningMode.LIVE_STREAM) {
            throw new IllegalArgumentException(
                    "Attempting to call detectLiveStream while not using RunningMode.LIVE_STREAM"
            );
        }
        long frameTime = SystemClock.uptimeMillis();

        Bitmap bitmapBuffer = Bitmap.createBitmap(
                imageProxy.getWidth(),
                imageProxy.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        imageProxy.getPlanes()[0].getBuffer().rewind();
        bitmapBuffer.copyPixelsFromBuffer(imageProxy.getPlanes()[0].getBuffer());
        imageProxy.close();

        Matrix matrix = new Matrix();
        matrix.postRotate(imageProxy.getImageInfo().getRotationDegrees());

        if (isFrontCamera) {
            matrix.postScale(
                    -1f,
                    1f,
                    imageProxy.getWidth(),
                    imageProxy.getHeight()
            );
        }

        Bitmap rotatedBitmap = Bitmap.createBitmap(
                bitmapBuffer, 0, 0, bitmapBuffer.getWidth(), bitmapBuffer.getHeight(),
                matrix, true
        );

        MPImage mpImage = new BitmapImageBuilder(rotatedBitmap).build();
        detectAsync(mpImage, frameTime);
    }

    @VisibleForTesting
    public void detectAsync(MPImage mpImage, long frameTime) {
        if (poseLandmarker != null) {
            poseLandmarker.detectAsync(mpImage, frameTime);
        }
    }

    public ResultBundle detectVideoFile(Uri videoUri, long inferenceIntervalMs) throws IOException {
        if (runningMode != RunningMode.VIDEO) {
            throw new IllegalArgumentException(
                    "Attempting to call detectVideoFile while not using RunningMode.VIDEO"
            );
        }

        long startTime = SystemClock.uptimeMillis();
        boolean didErrorOccurred = false;

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, videoUri);
        String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        Long videoLengthMs = durationStr != null ? Long.parseLong(durationStr) : null;

        Bitmap firstFrame = retriever.getFrameAtTime(0);
        Integer width = firstFrame != null ? firstFrame.getWidth() : null;
        Integer height = firstFrame != null ? firstFrame.getHeight() : null;

        if (videoLengthMs == null || width == null || height == null) {
            retriever.release();
            return null;
        }

        List<PoseLandmarkerResult> resultList = new ArrayList<>();
        long numberOfFrameToRead = videoLengthMs / inferenceIntervalMs;

        for (long i = 0; i <= numberOfFrameToRead; i++) {
            long timestampMs = i * inferenceIntervalMs;
            Bitmap frame = retriever.getFrameAtTime(
                    timestampMs * 1000,
                    MediaMetadataRetriever.OPTION_CLOSEST
            );

            if (frame != null) {
                Bitmap argb8888Frame;
                if (frame.getConfig() == Bitmap.Config.ARGB_8888) {
                    argb8888Frame = frame;
                } else {
                    argb8888Frame = frame.copy(Bitmap.Config.ARGB_8888, false);
                }

                MPImage mpImage = new BitmapImageBuilder(argb8888Frame).build();
                if (poseLandmarker != null) {
                    PoseLandmarkerResult detectionResult =
                            poseLandmarker.detectForVideo(mpImage, timestampMs);
                    if (detectionResult != null) {
                        resultList.add(detectionResult);
                    } else {
                        didErrorOccurred = true;
                        if (poseLandmarkerHelperListener != null) {
                            poseLandmarkerHelperListener.onError(
                                    "ResultBundle could not be returned in detectVideoFile"
                            );
                        }
                    }
                }
            } else {
                didErrorOccurred = true;
                if (poseLandmarkerHelperListener != null) {
                    poseLandmarkerHelperListener.onError(
                            "Frame at specified time could not be retrieved when detecting in video."
                    );
                }
            }
        }

        retriever.release();

        long inferenceTimePerFrameMs = (SystemClock.uptimeMillis() - startTime) / numberOfFrameToRead;

        if (didErrorOccurred) {
            return null;
        } else {
            return new ResultBundle(resultList, inferenceTimePerFrameMs, height, width);
        }
    }

    public ResultBundle detectImage(Bitmap image) {
        if (runningMode != RunningMode.IMAGE) {
            throw new IllegalArgumentException(
                    "Attempting to call detectImage while not using RunningMode.IMAGE"
            );
        }

        long startTime = SystemClock.uptimeMillis();
        MPImage mpImage = new BitmapImageBuilder(image).build();

        if (poseLandmarker != null) {
            PoseLandmarkerResult landmarkResult = poseLandmarker.detect(mpImage);
            if (landmarkResult != null) {
                long inferenceTimeMs = SystemClock.uptimeMillis() - startTime;
                List<PoseLandmarkerResult> resultList = new ArrayList<>();
                resultList.add(landmarkResult);
                return new ResultBundle(resultList, inferenceTimeMs, image.getHeight(), image.getWidth());
            }
        }

        if (poseLandmarkerHelperListener != null) {
            poseLandmarkerHelperListener.onError("Pose Landmarker failed to detect.");
        }
        return null;
    }

    private void returnLivestreamResult(PoseLandmarkerResult result, MPImage input) {
        long finishTimeMs = SystemClock.uptimeMillis();
        long inferenceTime = finishTimeMs - result.timestampMs();

        if (poseLandmarkerHelperListener != null) {
            List<PoseLandmarkerResult> resultList = new ArrayList<>();
            resultList.add(result);
            ResultBundle bundle = new ResultBundle(resultList, inferenceTime, input.getHeight(), input.getWidth());
            poseLandmarkerHelperListener.onResults(bundle);
        }
    }

    private void returnLivestreamError(RuntimeException error) {
        if (poseLandmarkerHelperListener != null) {
            poseLandmarkerHelperListener.onError(
                    error.getMessage() != null ? error.getMessage() : "An unknown error has occurred"
            );
        }
    }

    // Inner classes for ResultBundle and LandmarkerListener
    public static class ResultBundle {
        public List<PoseLandmarkerResult> results;
        public long inferenceTime;
        public int inputImageHeight;
        public int inputImageWidth;

        public ResultBundle(List<PoseLandmarkerResult> results, long inferenceTime,
                            int inputImageHeight, int inputImageWidth) {
            this.results = results;
            this.inferenceTime = inferenceTime;
            this.inputImageHeight = inputImageHeight;
            this.inputImageWidth = inputImageWidth;
        }
    }

    public interface LandmarkerListener {
        void onError(String error, int errorCode);
        void onError(String error); // Overloaded method
        void onResults(ResultBundle resultBundle);
    }
}