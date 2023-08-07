package com.example.ptolemycoinclassifier;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultsActivity extends AppCompatActivity {

    public TextView resultsTextView;
    public ImageView resultImageView;
    public Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        resultsTextView = findViewById(R.id.resultsTextView);
        resultImageView = findViewById(R.id.resultImageView);
        backButton = findViewById(R.id.backButton);

        // Retrieve the predictedClassLabel from the Intent
        String predictedClassLabel = getIntent().getStringExtra("predictedClassLabel");

        // Set the predictedClassLabel to the TextView
        resultsTextView.setText(predictedClassLabel);

        // Set the result image based on the predicted class label
        int resultImageResourceId = R.drawable.ptolemy1; // Default image resource ID
        switch (predictedClassLabel) {
            case "Ptolemy 1":
                resultImageResourceId = R.drawable.ptolemy1;
                break;
            case "Ptolemy 6":
                resultImageResourceId = R.drawable.ptolemy6;
                break;
            case "Ptolemy 12":
                resultImageResourceId = R.drawable.ptolemy12;
                break;
            case "Ptolemy 9":
                resultImageResourceId = R.drawable.ptolemy9;
                break;
            case "Alexander":
                resultImageResourceId = R.drawable.alexander;
                break;
        }
        resultImageView.setImageResource(resultImageResourceId);

        // Set up click listener for the "Back" button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear image data in the custom application before navigating back to photo_option
                MyApp myApp = (MyApp) getApplication();
                myApp.setSelectedImageBitmap(null);
                myApp.setCapturedImageUri(null);
                resultImageView.setImageDrawable(null);

                // Navigate back to the ImageConfirmationActivity
                Intent imageConfirmationIntent = new Intent(ResultsActivity.this, photo_option.class);
                startActivity(imageConfirmationIntent);
                finish();            }
        });
    }
}
