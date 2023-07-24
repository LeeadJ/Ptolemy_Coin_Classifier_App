package com.example.ptolemycoinclassifier;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class photo_option extends AppCompatActivity {

    // Upload Button
    Button uploadBtn, cameraBtn ;

    // One Preview Image
    ImageView IVPreviewImage;

    // constant to compare the activity result code
    private static final int REQUEST_CAMERA_PERMISSION_CODE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_IMAGE_UPLOAD = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove the title from the ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
        }
        actionBar.hide();

        setContentView(R.layout.activity_photo_option);

        // register the UI widgets with their appropriate IDs
        uploadBtn = findViewById(R.id.uploadImageBtn);
        cameraBtn = findViewById(R.id.openCameraBtn);
        IVPreviewImage = findViewById(R.id.image_view);

        // handle the Choose Image button to trigger the image chooser function
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureImage(view);
            }
        });
    }

    public void captureImage(View view){
        // check for permission for camera
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            // Request camera permission if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_CODE);
            return;
        }

        // Launch camera intent to capture image
        Intent intent = new Intent((MediaStore.ACTION_IMAGE_CAPTURE));
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    // this function is triggered when the Upload Image Button is clicked
    void imageChooser() {
        // create an instance of the intent of the type image
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it with the returned requestCode
        startActivityForResult(Intent.createChooser(intent, "Upload Image"), REQUEST_IMAGE_UPLOAD);
    }

    // this function is triggered when user selects the image from the imageChooser
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // compare the resultCode with the SELECT_PICTURE constant
            if (requestCode == REQUEST_IMAGE_UPLOAD) {
                // Check if the data returned from the intent is null (camera capture)
                if (data != null) {
                    // Get the url of the image from data (image chooser)
                    Uri selectedImageUri = data.getData();
                    if (null != selectedImageUri) {
                        // update the preview image in the layout
                        IVPreviewImage.setImageURI(selectedImageUri);
                    }
                }
            }
            else{
                // Image was captured using the camera
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                // Set captured image to ImageView
                IVPreviewImage.setImageBitmap(imageBitmap);
            }
        }
    }
}