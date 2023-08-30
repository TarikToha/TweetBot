package com.example.testapp;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CloudVision {

    static final int MAX_DIMENSION = 1200;
    private static final int MAX_LABEL_RESULTS = 10;

    static Bitmap scaleBitmapDown(Bitmap bitmap) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = MAX_DIMENSION;
        int resizedHeight = MAX_DIMENSION;

        if (originalHeight > originalWidth) {
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        }

        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    static class ObjectDetectionTasks extends AsyncTask<Object, Void, String> {

        private final String API_KEY;
        private final Bitmap bitmap;
        private final WeakReference<MainActivity> activityRef;

        public ObjectDetectionTasks(MainActivity activity, String API_KEY, Bitmap bitmap) {
            this.activityRef = new WeakReference<>(activity);
            this.API_KEY = API_KEY;
            this.bitmap = bitmap;
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                Vision.Images.Annotate annotateRequest = preprocessing(API_KEY, bitmap);
                BatchAnnotateImagesResponse batchResponse = annotateRequest.execute();
                List<EntityAnnotation> labels = batchResponse.getResponses().get(0).getLabelAnnotations();
                return makeString(labels);
            } catch (Exception e) {
                Log.d("error", "AsyncTask error: " + e);
            }
            return "AsyncTask failed";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            MainActivity activity = activityRef.get();
            TextView outputView = activity.findViewById(R.id.loading_view);
            outputView.setText(s);
        }
    }

    private static Vision.Images.Annotate preprocessing(String API_KEY, Bitmap bitmap) throws Exception {
        Vision.Builder visionBuilder = new Vision.Builder(new NetHttpTransport(), new AndroidJsonFactory(), null);
        visionBuilder.setVisionRequestInitializer(new VisionRequestInitializer(API_KEY));
        Vision vision = visionBuilder.build();

        // Convert the bitmap to a JPEG
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        // Base64 encode the JPEG
        Image inputImage = new Image();
        inputImage.encodeContent(imageBytes);

        // add the features we want
        Feature labelDetection = new Feature();
        labelDetection.setType("LABEL_DETECTION");
        labelDetection.setMaxResults(MAX_LABEL_RESULTS);

        AnnotateImageRequest request = new AnnotateImageRequest();
        request.setImage(inputImage);
        request.setFeatures(Arrays.asList(labelDetection));

        BatchAnnotateImagesRequest batchRequests = new BatchAnnotateImagesRequest();
        batchRequests.setRequests(Arrays.asList(request));

        return vision.images().annotate(batchRequests);
    }

    private static String makeString(List<EntityAnnotation> labels) {
        StringBuilder message = new StringBuilder("I found these things:\n\n");

        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message.append(String.format(Locale.US, "%.3f: %s", label.getScore(), label.getDescription()));
                message.append("\n");
            }
        } else {
            message.append("nothing");
        }

        return message.toString();
    }


}
