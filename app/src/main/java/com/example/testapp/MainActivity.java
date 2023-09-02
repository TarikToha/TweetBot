package com.example.testapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String API_KEY = "REMOVED";
    //    private static final String URL = "http://10.0.2.2:5000/";
    private static final String URL = "https://flask-app-sb1x.onrender.com/";

    private Button cam_button;
    private TextView loading_view;
    private ImageView image_view;
    private static final String TAG = MainActivity.class.getSimpleName();
    private String image_path;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cam_button = findViewById(R.id.cam_button);
        loading_view = findViewById(R.id.loading_view);
        image_view = findViewById(R.id.image_view);
    }

    public void start_camera(View view) {
        try {
            File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File image_file = new File(dir, "IMG_" + timeStamp + ".png");
            image_path = image_file.getAbsolutePath();

            Uri image_uri = FileProvider.getUriForFile(this, "com.example.testapp.fileprovider", image_file);
            Intent cam_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cam_intent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);

            startActivityForResult(cam_intent, 123);
        } catch (Exception e) {
            show_message(e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123) {
            try {
                loading_view.setText(R.string.loading_text);

                Uri uri = Uri.fromFile(new File(image_path));
                Bitmap img = CloudVision.scaleBitmapDown(MediaStore.Images.Media.getBitmap(getContentResolver(), uri));
//                Bitmap img = BitmapFactory.decodeFile(image_path);
                image_view.setImageBitmap(img);

                cam_button.setEnabled(false);
                new CloudVision.ObjectDetectionTasks(this, API_KEY, img, image_path, URL).execute();
                cam_button.setEnabled(true);

            } catch (Exception e) {
                show_message(e.getMessage());
            }
        }
    }

    private void show_message(String err) {
        Log.e(TAG, err);
        Toast.makeText(this, err, Toast.LENGTH_LONG).show();
    }
}