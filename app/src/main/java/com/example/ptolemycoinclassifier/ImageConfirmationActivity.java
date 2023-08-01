package com.example.ptolemycoinclassifier;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class ImageConfirmationActivity extends AppCompatActivity {

    // UI elements
    private ImageView confirmedImageView;
    private Button chooseNewImageButton, continueButton;

    // Variables to store image data
    private Bitmap selectedImageBitmap;
    private Uri capturedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_confirmation);

        // Initialize UI elements
        confirmedImageView = findViewById(R.id.confirmedImageView);
        chooseNewImageButton = findViewById(R.id.chooseNewImageButton);
        continueButton = findViewById(R.id.continueButton);

        // Get the selected image bitmap from the Intent, if available
        selectedImageBitmap = getIntent().getParcelableExtra("selectedImage");

        // Get the captured image URI from the Intent, if available
        capturedImageUri = getIntent().getData();

        // Display the image based on the available data
        if (selectedImageBitmap != null) {
            // If a selected image bitmap is available, display it in the ImageView
            confirmedImageView.setImageBitmap(selectedImageBitmap);
        } else if (capturedImageUri != null) {
            // If a captured image URI is available, display it in the ImageView
            confirmedImageView.setImageURI(capturedImageUri);
        }

        // Handle the "Choose New Image" button click
        chooseNewImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the photo_option activity to choose a new image
                Intent photoOptionIntent = new Intent(ImageConfirmationActivity.this, photo_option.class);
                startActivity(photoOptionIntent);
                finish();
            }
        });

        // Handle the "Continue" button click
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add your code to proceed to the next step/activity
                // For example, you can pass the image URI to the next activity for further processing

                // Check if the selectedImageBitmap is not null (meaning the image is displayed in the ImageView)
                if (selectedImageBitmap != null) {
                    // Resize the selectedImageBitmap to 150x150 matrix
                    int targetWidth = 150;
                    int targetHeight = 150;
                    Bitmap resizedBitmap = getResizedBitmap(selectedImageBitmap, targetWidth, targetHeight);

                    // Convert the resized bitmap to RGB format
                    Bitmap rgbBitmap = getRGBBitmap(resizedBitmap);

                    // Now you have the resized and RGB converted bitmap ready for further processing
                    // You can pass the rgbBitmap to your model for inference, save it to a file, or do any other processing as needed.
                }
            }
        });
    }

    /**
     * Function to resize the bitmap image.
     * @param imageBitmap The original bitmap image to be resized.
     * @param targetWidth The desired width of the resized bitmap.
     * @param targetHeight The desired height of the resized bitmap.
     * @return The resized bitmap.
     */
    public Bitmap getResizedBitmap(Bitmap imageBitmap, int targetWidth, int targetHeight) {
        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();
        float scaleWidth = ((float) targetWidth) / width;
        float scaleHeight = ((float) targetHeight) / height;

        // Create a matrix for the manipulation
        Matrix matrix = new Matrix();

        // Resize the bitmap
        matrix.postScale(scaleWidth, scaleHeight);

        // Recreate the new bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, width, height, matrix, false);

        // Return the resized bitmap
        return resizedBitmap;
    }

    /**
     * Function to convert the bitmap to RGB format.
     * @param bitmap The original bitmap to be converted to RGB.
     * @return The bitmap in RGB format.
     */
    public Bitmap getRGBBitmap(Bitmap bitmap) {
        // Convert the bitmap to RGB format
        return bitmap.copy(Bitmap.Config.ARGB_8888, true);
    }
}
