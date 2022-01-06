package com.saisumanth.textinterpreterconverter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;

public class TranslateActivity extends AppCompatActivity {

    private TextView text_display;
    private Spinner spinner;
    private Button translatebtn;
    private Translator translator;
    public int pos;
    public ProgressDialog progressDialog;

    public String[] languages = {"Telugu", "Tamil", "Hindi", "Spanish", "Chinese"};

    HashMap<String,String> languageId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);

        text_display = findViewById(R.id.recg_text);
        spinner = findViewById(R.id.language);
        translatebtn = findViewById(R.id.translateButton);


        languageId = new HashMap<>();
        languageId.put("Telugu","te");
        languageId.put("Tamil","ta");
        languageId.put("Hindi","hi");
        languageId.put("Spanish","es");
        languageId.put("Chinese","zh");

        final Intent intent = getIntent();

        pos = intent.getIntExtra("position",0);

        progressDialog = new ProgressDialog(this);

        progressDialog.setMessage("Translating...");

        progressDialog.setCancelable(false);

        new Thread(new Runnable(){

            public void run(){

                String text = "";

                try {

                    URL url = new URL(FilesActivity.items.get(pos).getLink());

                    HttpURLConnection conn=(HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(60000);
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    String str;
                    while ((str = in.readLine()) != null) {
                        text += str;
                    }
                    in.close();
                } catch (Exception e) {
                    Log.d("Connection",e.toString());
                }

                String finalText = text;
                TranslateActivity.this.runOnUiThread(new Runnable(){
                    public void run(){
                        text_display.setText(finalText);
                    }
                });

            }
        }).start();


        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,languages);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(aa);

        translatebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog.show();

                prepareModel();

            }
        });



    }

    private void prepareModel() {

        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(Objects.requireNonNull(languageId.get(spinner.getSelectedItem().toString())))
                        .build();
        translator = Translation.getClient(options);

        translator.downloadModelIfNeeded().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translateText();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Log.d("Test", "onFailure: " + e.getMessage());
            }
        });

    }

    private void translateText() {

        translator.translate(text_display.getText().toString()).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {

                Toast.makeText(getApplicationContext(),"Sucess",Toast.LENGTH_LONG).show();

                progressDialog.dismiss();

                AlertDialog.Builder builder = new AlertDialog.Builder(TranslateActivity.this);

                builder.setTitle("Translation of " + FilesActivity.items.get(pos).getFilename());

                builder.setMessage(s);

                Log.d("Test", "onSuccess: " + s);

                builder.setCancelable(true);

                builder.show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Test1", "onFailure: " + e.getMessage());
                progressDialog.dismiss();
            }
        });

    }
}