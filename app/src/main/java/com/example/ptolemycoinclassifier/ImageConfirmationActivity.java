package com.example.ptolemycoinclassifier;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import org.tensorflow.lite.Interpreter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


public class ImageConfirmationActivity extends AppCompatActivity
{

    // UI elements
    private ImageView confirmedImageView;
    private Button chooseNewImageButton, continueButton;

    // Variables to store image data
    private Bitmap selectedImageBitmap;

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

        // Get the selected image URI from the Intent
        selectedImageBitmap = getIntent().getParcelableExtra("selectedImageBitmap");

        // Get the selected image file path from the Intent
        String selectedImageFilePath = getIntent().getStringExtra("selectedImageFilePath");

        // Load the selected image Bitmap from the file path
        if (selectedImageFilePath != null) {
            selectedImageBitmap = BitmapFactory.decodeFile(selectedImageFilePath);
            confirmedImageView.setImageBitmap(selectedImageBitmap);
        }

        // Display the image based on the available data
        if (selectedImageBitmap != null)
        {
            confirmedImageView.setImageBitmap(selectedImageBitmap);
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
        continueButton.setOnClickListener(new View.OnClickListener()
        {
            @SuppressLint("DefaultLocale")
            @Override
            public void onClick(View v)
            {
                Log.d("ContinueButton", "Entered");
                if (selectedImageBitmap != null)
                {
                    try
                    {
                        // Resize the selectedImageBitmap to 150x150 matrix
                        int inputWidth = 150;
                        int inputHeight = 150;
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(selectedImageBitmap, inputWidth, inputHeight, true);
                        Log.d("ContinueButton", "Passed Image Resize");
                        float[] inputData = preprocessImage(resizedBitmap, inputWidth, inputHeight);

                        // Prepare the input buffer
                        int batchSize = 1;
                        int inputChannels = 3;
                        int inputSize = batchSize * inputWidth * inputHeight * inputChannels;
                        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(inputSize * Float.SIZE / 8);
                        inputBuffer.order(ByteOrder.nativeOrder());

                        // Copy the preprocessed input data into the input buffer and reshape to 4D
                        inputBuffer.rewind();
                        inputBuffer.asFloatBuffer().put(inputData);
                        inputBuffer.rewind();

                        // Run inference
                        float[][] outputArray = new float[1][5];
                        tfliteInterpreter.run(inputBuffer, outputArray);

                        // Get the predicted class label
                        int predictedClass = argmax(outputArray[0]);
                        String[] classLabels = {"Alexander", "Ptolemy 1", "Ptolemy 6", "Ptolemy 12", "Ptolemy 9"};
                        String predictedLabel = classLabels[predictedClass];

                        // Start ResultsActivity with prediction info
                        Intent resultsIntent = new Intent(ImageConfirmationActivity.this, ResultsActivity.class);
                        resultsIntent.putExtra("predictedClassLabel", predictedLabel+" probability: "+String.format("%.2f", outputArray[0][predictedClass]*100)+"%");
                        startActivity(resultsIntent);

                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.e("ClickButton", "Exception during image classification: " + e.getMessage());
                    }
                }
                else
                {
                    Log.d("ClickButton", "Problem with bitmap");
                }

                // Delete the image file when no longer needed
                if (selectedImageFilePath != null)
                {
                    File imageFile = new File(selectedImageFilePath);
                    if (imageFile.exists())
                        imageFile.delete();
                }
            }
        });
    }

    // Function to preprocess the image
    private float[] preprocessImage(Bitmap bitmap, int inputWidth, int inputHeight)
    {
        float[] inputData = new float[inputWidth * inputHeight * 3];
        int index = 0;
        for (int y = 0; y < inputHeight; y++)
        {
            for (int x = 0; x < inputWidth; x++)
            {
                int pixel = bitmap.getPixel(x, y);
                inputData[index++] = Color.red(pixel);
                inputData[index++] = Color.green(pixel);
                inputData[index++] = Color.blue(pixel);
            }
        }
        return inputData;
    }

    // Function to find highest probability
    private int argmax(float[] array)
    {
        int maxIndex = 0;
        float maxValue = array[0];
        for (int i = 1; i < array.length; i++)
        {
            if (array[i] > maxValue)
            {
                maxIndex = i;
                maxValue = array[i];
            }
        }
        return maxIndex;
    }

    // Function to load the TensorFlow Lite model file from the assets folder
    public MappedByteBuffer loadModelFile() throws IOException
    {
        try
        {
            // Load the model file from the assets folder
            AssetFileDescriptor fileDescriptor = getAssets().openFd("resnet50.tflite");
            FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
            FileChannel fileChannel = inputStream.getChannel();
            long startOffset = fileDescriptor.getStartOffset();
            long declaredLength = fileDescriptor.getDeclaredLength();

            // Map the model file into memory
            return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        }
        catch (IOException e)
        {
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
    public Bitmap getResizedBitmap(Bitmap imageBitmap, int targetWidth, int targetHeight)
    {
        int width = imageBitmap.getWidth();
        int height = imageBitmap.getHeight();
        float scaleWidth = ((float) targetWidth) / width;
        float scaleHeight = ((float) targetHeight) / height;

        // Create a matrix for the manipulation
        Matrix matrix = new Matrix();

        // Resize the bitmap
        matrix.postScale(scaleWidth, scaleHeight);

        // Return the resized bitmap
        return Bitmap.createBitmap(imageBitmap, 0, 0, width, height, matrix, false);
    }

    /**
     * Function to convert the bitmap to RGB format.
     * @param bitmap The original bitmap to be converted to RGB.
     * @return The bitmap in RGB format.
     */
    public Bitmap getRGBBitmap(Bitmap bitmap)
    {
        // Convert the bitmap to RGB format
        return bitmap.copy(Bitmap.Config.ARGB_8888, true);
    }


    // Function to find the index of the predicted class based on the output probabilities
    public int findPredictedClassIndex(float[] probabilities)
    {
        int predictedClassIndex = 0;
        float maxProbability = probabilities[0];
        Log log = null;
        for (int i = 0; i < probabilities.length; i++)
        {
            log.d("prob",Float.toString(probabilities[i]));
            if (probabilities[i] > maxProbability)
            {
                maxProbability = probabilities[i];
                predictedClassIndex = i;
            }
        }
        return predictedClassIndex;
    }
}
