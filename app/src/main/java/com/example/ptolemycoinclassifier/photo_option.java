package com.example.ptolemycoinclassifier;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.graphics.Bitmap;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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


    // Global Bitmap variable to store the currently selected image
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

        // Register UI widgets with their IDs
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


        // Handle "Choose Image" button click
        uploadBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });

        // Handle "Open Camera" button click
        cameraBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                captureImage(view);
            }
        });
    }

    // Function to handle camera capture
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

    // Function to handle image chooser
    void imageChooser()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Upload Image"), REQUEST_IMAGE_UPLOAD);
    }


    // Function to handle the result after capturing or selecting an image
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_UPLOAD) {
                if (data != null) {
                    try
                    {
                        // Convert the selected image URI to a Bitmap
                        selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());

                        // Save the selected Bitmap to a file
                        File imageFile = saveImageToInternalStorage(selectedImageBitmap);

                        // Pass the file path to the ImageConfirmationActivity
                        Intent imageConfirmationIntent = new Intent(photo_option.this, ImageConfirmationActivity.class);
                        imageConfirmationIntent.putExtra("selectedImageFilePath", imageFile.getAbsolutePath());
                        startActivity(imageConfirmationIntent);

                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            else if (requestCode == REQUEST_IMAGE_CAPTURE)
            {
                Bundle extras = data.getExtras();
                selectedImageBitmap = (Bitmap) extras.get("data");
                Intent imageConfirmationIntent = new Intent(photo_option.this, ImageConfirmationActivity.class);
                imageConfirmationIntent.putExtra("selectedImageBitmap", selectedImageBitmap);
                startActivity(imageConfirmationIntent);
            }
        }
    }

    // Function to save the bitmap to internal storage
    public File saveImageToInternalStorage(Bitmap bitmap)
    {
        // Create a file to save the image
        File imagesDir = getFilesDir();
        File imageFile = new File(imagesDir, "captured_image.jpg");
        try
        {
            // Compress the bitmap and save it to the file
            FileOutputStream fos = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return imageFile;
    }
}