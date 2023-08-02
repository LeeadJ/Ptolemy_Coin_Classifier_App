package com.example.ptolemycoinclassifier;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import org.tensorflow.lite.Interpreter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


public class ImageConfirmationActivity extends AppCompatActivity
{

    // UI elements
    private ImageView confirmedImageView;
    private Button chooseNewImageButton, continueButton;

    // Variables to store image data
    private Bitmap selectedImageBitmap;
    private Uri capturedImageUri;

    // Interpreter variable for the TensorFlow Lite model
    Interpreter tfliteInterpreter;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_confirmation);

        // Initialize UI elements
        confirmedImageView = findViewById(R.id.confirmedImageView);
        chooseNewImageButton = findViewById(R.id.chooseNewImageButton);
        continueButton = findViewById(R.id.continueButton);

        // Verify that the continueButton is found
        if (continueButton == null)
            Log.e("ImageConfirmation", "Continue button not found.");

        // Get the selected image bitmap from the Intent, if available
        selectedImageBitmap = getIntent().getParcelableExtra("selectedImage");

        // Get the captured image URI from the Intent, if available
        capturedImageUri = getIntent().getData();

        // Display the image based on the available data
        if (selectedImageBitmap != null)
        {
            // If a selected image bitmap is available, display it in the ImageView
            confirmedImageView.setImageBitmap(selectedImageBitmap);
        }
        else if (capturedImageUri != null)
        {
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
        chooseNewImageButton.setOnClickListener(new View.OnClickListener()
        {
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
            public void onClick(View v)
            {
                Log.d("ContinueButton", "Entered");
                // Check if the selectedImageBitmap is not null (meaning the image is displayed in the ImageView)
                if (selectedImageBitmap != null)
                {
                    try {
                        // Resize the selectedImageBitmap to 150x150 matrix
                        int targetWidth = 150;
                        int targetHeight = 150;
                        Bitmap resizedBitmap = getResizedBitmap(selectedImageBitmap, targetWidth, targetHeight);
                        Log.d("ContinueButton", "Passed Image Resize");
                        // Convert the resized bitmap to RGB format
                        Bitmap rgbBitmap = getRGBBitmap(resizedBitmap);
                        Log.d("ContinueButton", "Passed RGB Conversion");

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
                        Log.d("ContinueButton", "Passed Tensor input preparation");

                        // Perform inference using the TensorFlow Lite model
                        float[][] outputTensor = new float[1][5]; // Replace '5' with the number of output classes in your model
                        tfliteInterpreter.run(inputTensor, outputTensor);

                        // Now you have the inference results in 'outputTensor'
                        // You can process the results as per your requirement
                        // For example, you can find the index of the highest probability to get the predicted class.

                        // Find the predicted class index (index of the highest probability)
                        int predictedClassIndex = findPredictedClassIndex(outputTensor[0]);

                        // Get the label for the predicted class index (you need to define a function to do this)
                        String predictedClassLabel = getPredictedClassLabel(predictedClassIndex);

                        // Start the ResultsActivity and pass the predictedClassLabel as an extra
                        Intent resultsIntent = new Intent(ImageConfirmationActivity.this, ResultsActivity.class);
                        resultsIntent.putExtra("predictedClassLabel", predictedClassLabel);
                        startActivity(resultsIntent);


                        // Now you can use the predictedClassLabel to show the prediction result or take further actions


                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("ClickButton", "Exception during image classification: " + e.getMessage());
                        // Handle the exception here, for example, show an error message to the user
                    }
                }
                else{
                    Log.d("ClickButton", "Problem with bitmap");
                }
            }
        });
    }

    // Function to load the TensorFlow Lite model file from the assets folder
    public MappedByteBuffer loadModelFile() throws IOException {
        try {
            // Load the model file from the assets folder
            AssetFileDescriptor fileDescriptor = getAssets().openFd("resnet50.tflite");
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();

            // Map the model file into memory
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ImageConfirmationActiv", "Error loading TensorFlow Lite model: " + e.getMessage());
            return null;
        }
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


    // Function to find the index of the predicted class based on the output probabilities
    public int findPredictedClassIndex(float[] probabilities) {
        int predictedClassIndex = 0;
        float maxProbability = probabilities[0];
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > maxProbability) {
                maxProbability = probabilities[i];
                predictedClassIndex = i;
            }
        }
        return predictedClassIndex;
    }


    // Function to get the label for the predicted class index (you need to define this based on your model classes)
    public String getPredictedClassLabel(int predictedClassIndex) {
        // Replace this with your own logic to map the predicted index to class labels
        // For example, if you have a list of class labels ["class1", "class2", ...], you can return the corresponding label.
        // If you have the classes predefined in an array, you can use the index to access the label from the array.

        // For simplicity, let's return a default label here.
        return "Predicted Class: " + predictedClassIndex;
    }
}
