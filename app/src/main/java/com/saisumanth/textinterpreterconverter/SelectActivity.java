package com.saisumanth.textinterpreterconverter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SelectActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);


        Button speech = findViewById(R.id.speech);

        Button summarize = findViewById(R.id.summerize);

        Button translate = findViewById(R.id.translate);

        final Intent intent = getIntent();

        int pos = intent.getIntExtra("position",0);


        speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SelectActivity.this,SpeechActivity.class);

                intent.putExtra("position",pos);

                startActivity(intent);
            }
        });

        summarize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SelectActivity.this,SummarizeActivity.class);

                intent.putExtra("position",pos);

                startActivity(intent);

            }
        });

        translate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(SelectActivity.this,TranslateActivity.class);

                intent.putExtra("position",pos);

                startActivity(intent);

            }
        });

    }
}