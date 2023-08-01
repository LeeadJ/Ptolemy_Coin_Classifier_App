package com.example.ptolemycoinclassifier;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


public class ImageConfirmationActivity extends AppCompatActivity {

    // UI elements
    private ImageView confirmedImageView;
    private Button chooseNewImageButton, continueButton;

    // Variables to store image data
    private Bitmap selectedImageBitmap;
    private Uri capturedImageUri;

    // Interpreter variable for the TensorFlow Lite model
    Interpreter tfliteInterpreter;

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

        try {
            // Load the TensorFlow Lite model from the assets folder
            tfliteInterpreter = new Interpreter(loadModelFile());
        } catch (IOException e) {
            e.printStackTrace();
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
                // Check if the selectedImageBitmap is not null (meaning the image is displayed in the ImageView)
                if (selectedImageBitmap != null) {
                    try {
                        // Resize the selectedImageBitmap to 150x150 matrix
                        int targetWidth = 150;
                        int targetHeight = 150;
                        Bitmap resizedBitmap = getResizedBitmap(selectedImageBitmap, targetWidth, targetHeight);

                        // Convert the resized bitmap to RGB format
                        Bitmap rgbBitmap = getRGBBitmap(resizedBitmap);

                        // Prepare the input tensor for the model.
                        float[][][][] inputTensor = new float[1][150][150][3];
                        for (int i = 0; i < 150; i++) {
                            for (int j = 0; j < 150; j++) {
                                int pixel = rgbBitmap.getPixel(i, j);
                                inputTensor[0][i][j][0] = Color.red(pixel) / 255.0f;
                                inputTensor[0][i][j][1] = Color.green(pixel) / 255.0f;
                                inputTensor[0][i][j][2] = Color.blue(pixel) / 255.0f;
                            }
                        }

                        // Perform inference using the TensorFlow Lite model
                        float[][] outputTensor = new float[1][5]; // Replace '5' with the number of output classes in your model
                        tfliteInterpreter.run(inputTensor, outputTensor);

                        // Now you have the inference results in 'outputTensor'
                        // You can process the results as per your requirement
                        // For example, you can find the index of the highest probability to get the predicted class.
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Handle the exception here, for example, show an error message to the user
                    }
                }
            }
        });
    }

    // Function to load the TensorFlow Lite model file from the assets folder
    public MappedByteBuffer loadModelFile() throws IOException {
        // Load the model file from the assets folder
        AssetFileDescriptor fileDescriptor = getAssets().openFd("resnet50.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        // Map the model file into memory
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
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
