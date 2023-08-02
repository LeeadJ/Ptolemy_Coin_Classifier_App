package com.example.ptolemycoinclassifier;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultsActivity extends AppCompatActivity {

    private TextView resultsTextView;
    private Button backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        resultsTextView = findViewById(R.id.resultsTextView);
        backButton = findViewById(R.id.backButton);

        // Retrieve the predictedClassLabel from the Intent
        String predictedClassLabel = getIntent().getStringExtra("predictedClassLabel");

        // Set the predictedClassLabel to the TextView
        resultsTextView.setText(predictedClassLabel);

        // Set up click listener for the "Back" button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear image data in the custom application before navigating back to photo_option
                MyApp myApp = (MyApp) getApplication();
                myApp.setSelectedImageBitmap(null);
                myApp.setCapturedImageUri(null);

                // Navigate back to the ImageConfirmationActivity
                Intent imageConfirmationIntent = new Intent(ResultsActivity.this, photo_option.class);
                startActivity(imageConfirmationIntent);
                finish();
            }
        });
    }
}
