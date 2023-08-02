package com.example.ptolemycoinclassifier;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.graphics.Bitmap;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class photo_option extends AppCompatActivity
{

    // Upload Button
    Button uploadBtn, cameraBtn ;

    // One Preview Image
    ImageView IVPreviewImage;

    // constant to compare the activity result code
    private static final int REQUEST_CAMERA_PERMISSION_CODE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_IMAGE_UPLOAD = 3;


    // Global Uri variable to store the currently displayed image URI
    private Bitmap selectedImageBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Remove the title from the ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle("");
        actionBar.hide();

        setContentView(R.layout.activity_photo_option);

        // register the UI widgets with their appropriate IDs
        uploadBtn = findViewById(R.id.uploadImageBtn);
        cameraBtn = findViewById(R.id.openCameraBtn);
        IVPreviewImage = findViewById(R.id.image_view);

        // Get the custom application instance
        MyApp myApp = (MyApp) getApplication();

        // Check if the custom application has the selected image bitmap
        selectedImageBitmap = myApp.getSelectedImageBitmap();

        // Display the image if it exists
        if (selectedImageBitmap != null)
            IVPreviewImage.setImageBitmap(selectedImageBitmap);


        // handle the Choose Image button to trigger the image chooser function
        uploadBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });

        cameraBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                captureImage(view);
            }
        });
    }

    /**
     * Function to handle the camera capture (when "Open Camera" button is clicked).
     * @param view The view from which the function is called.
     */
    public void captureImage(View view)
    {
        // check for permission for camera
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            // Request camera permission if not granted
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_CODE);
            return;
        }

        // Launch camera intent to capture image
        Intent intent = new Intent((MediaStore.ACTION_IMAGE_CAPTURE));
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    /**
     * Function to handle image chooser (when "Upload Image" button is clicked).
     */
    void imageChooser()
    {
        // create an instance of the intent of the type image
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it with the returned requestCode
        startActivityForResult(Intent.createChooser(intent, "Upload Image"), REQUEST_IMAGE_UPLOAD);
    }


    /**
     * Function to handle the result after capturing or selecting an image.
     * @param requestCode The request code provided when starting the activity.
     * @param resultCode The result code returned by the activity.
     * @param data The intent data containing the result.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
            if (requestCode == REQUEST_IMAGE_UPLOAD)
            {
                if (data != null)
                {
                    // Get the URI of the selected image
                    Uri selectedImageUri = data.getData();
                    try {
                        // Convert the selected image URI to a Bitmap
                        selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        // Navigate to ImageConfirmationActivity with the selected image bitmap
                        Intent imageConfirmationIntent = new Intent(photo_option.this, ImageConfirmationActivity.class);
                        imageConfirmationIntent.putExtra("selectedImage", selectedImageBitmap);
                        startActivity(imageConfirmationIntent);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else if (requestCode == REQUEST_IMAGE_CAPTURE)
            {
                Bundle extras = data.getExtras();
                // Retrieve the captured image bitmap
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                // Navigate to ImageConfirmationActivity with the captured image bitmap
                Intent imageConfirmationIntent = new Intent(photo_option.this, ImageConfirmationActivity.class);
                imageConfirmationIntent.putExtra("selectedImage", imageBitmap);
                startActivity(imageConfirmationIntent);
            }
        }
    }

    /**
     * Function to save the bitmap to internal storage.
     * @param bitmap The bitmap image to be saved.
     * @return The file path where the image is saved.
     */
    public File saveImageToInternalStorage(Bitmap bitmap)
    {
        // Create a file to save the image
        File imagesDir = getFilesDir();
        File imageFile = new File(imagesDir, "captured_image.jpg");
        try {
            // Compress the bitmap and save it to the file
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageFile;
    }
}