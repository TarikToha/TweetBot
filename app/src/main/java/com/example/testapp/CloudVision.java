package com.example.testapp;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CloudVision {

    static final int MAX_DIMENSION = 1200;
    private static final int MAX_LABEL_RESULTS = 5;

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
        request.setFeatures(Collections.singletonList(labelDetection));

        BatchAnnotateImagesRequest batchRequests = new BatchAnnotateImagesRequest();
        batchRequests.setRequests(Collections.singletonList(request));

        return vision.images().annotate(batchRequests);
    }

    private static String makeString(List<EntityAnnotation> labels) {
        StringBuilder message = new StringBuilder("Object Detection Results:\n\n");

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

    private static JSONObject makeJSON(List<EntityAnnotation> labels, String image_path) throws JSONException {
        ArrayList<String> objects = new ArrayList<>();
        for (EntityAnnotation label : labels) {
            objects.add(label.getDescription());
        }

        HashMap<String, Integer> hm = new HashMap<>();
        for (String object : objects) {
            Integer count = hm.get(object);
            hm.put(object, (count == null) ? 1 : count + 1);
        }

        JSONArray packets = new JSONArray();
        for (Map.Entry<String, Integer> item : hm.entrySet()) {
            JSONObject packet = new JSONObject();
            packet.put("object", item.getKey());
            packet.put("count", item.getValue());
            packets.put(packet);
        }

        JSONObject packet = new JSONObject();
        packet.put("image_name", new File(image_path).getName());
        packet.put("labels", packets);

        return packet;
    }

    private static JSONObject makeJSONForGPT(List<EntityAnnotation> labels) throws JSONException {
        JSONArray keywords = new JSONArray();
        for (EntityAnnotation label : labels) {
            keywords.put(label.getDescription());
        }

        JSONObject packet = new JSONObject();
        packet.put("context", "UNC Embedded Intelligence Lab");
        packet.put("mode", "twitter");
        packet.put("model", "gemini-2-0-flash");
        packet.put("max_tokens", 128);
        packet.put("keywords", keywords);

        return packet;
    }

    private static void uploadJSON(JSONObject packet, String url, MainActivity activity) {
        RequestQueue requestQueue = Volley.newRequestQueue(activity);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, packet, response -> Log.d("success", response.toString()), error -> Log.e("error", error.toString()));

        AsyncTask.execute(() -> requestQueue.add(request));
    }

    private static void uploadJSONToGPT(JSONObject packet, String url, MainActivity activity, String apiKey) {
        RequestQueue requestQueue = Volley.newRequestQueue(activity);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, packet, response -> {
            TextView outputView = activity.findViewById(R.id.loading_view);
            StringBuilder text = new StringBuilder("@TweetBot\n\n");
            Log.i("success", response.toString());
            try {
                JSONArray outputs = response.getJSONObject("data").getJSONArray("outputs");
                String data = outputs.getJSONObject(0).getString("text");
                text.append(data.trim());
                Log.d("success", text.toString());
            } catch (Exception e) {
                Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_LONG).show();
                Log.e("success", e.toString());
            }
            outputView.setText(text);
        }, error -> {
            Toast.makeText(activity.getBaseContext(), error.toString(), Toast.LENGTH_LONG).show();
            Log.e("error", error.toString());
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + apiKey);
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        AsyncTask.execute(() -> requestQueue.add(request));
    }

    static class ObjectDetectionTasks extends AsyncTask<Object, Void, String> {
        private final String GPT_URL;
        private final String CV_KEY, GPT_KEY;
        private final Bitmap bitmap;
        private final WeakReference<MainActivity> activityRef;

        ObjectDetectionTasks(MainActivity activity, String CV_KEY, String GPT_KEY, Bitmap bitmap, String GPT_URL) {
            this.activityRef = new WeakReference<>(activity);
            this.CV_KEY = CV_KEY;
            this.GPT_KEY = GPT_KEY;
            this.bitmap = bitmap;
            this.GPT_URL = GPT_URL;
        }

        @Override
        protected String doInBackground(Object... objects) {
            try {
                Vision.Images.Annotate annotateRequest = preprocessing(CV_KEY, bitmap);
                BatchAnnotateImagesResponse batchResponse = annotateRequest.execute();
                List<EntityAnnotation> labels = batchResponse.getResponses().get(0).getLabelAnnotations();

//                JSONObject packet = makeJSON(labels, image_path);
                JSONObject packet = makeJSONForGPT(labels);
                Log.d("packet", packet.toString());
//                uploadJSON(packet, URL, activityRef.get());
                uploadJSONToGPT(packet, GPT_URL, activityRef.get(), GPT_KEY);

                return makeString(labels);
            } catch (Exception e) {
                Log.e("error", "AsyncTask error: " + e);
            }
            return "AsyncTask failed";
        }

        @Override
        protected void onPostExecute(String results) {
            super.onPostExecute(results);
            MainActivity activity = activityRef.get();
            TextView outputView = activity.findViewById(R.id.loading_view);
            outputView.setText(results);
        }
    }

}
