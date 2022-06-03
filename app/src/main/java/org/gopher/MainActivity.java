package org.gopher;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.Guess).setOnClickListener((View v) -> {

            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("Mode", "Guess");
            startActivity(intent);
        });

        findViewById(R.id.Continuous).setOnClickListener((View v) -> {

            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("Mode", "Continuous");
            startActivity(intent);
        });
    }
}