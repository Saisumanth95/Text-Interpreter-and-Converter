package com.saisumanth.textinterpreterconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SummarizeActivity extends AppCompatActivity {

    private TextView summarized_text;
    private EditText editlines;
    Button summarize;
    int pos;
    private final String url = "https://textinterpreter.herokuapp.com/summarize";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summarize);

        summarized_text = findViewById(R.id.summarized_text);
        editlines = findViewById(R.id.noOfLines);
        summarize = findViewById(R.id.summerizeButton);

        final Intent intent = getIntent();

        pos = intent.getIntExtra("position",0);

        summarize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String s = String.valueOf(editlines.getText());

                if(s == null || s.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Enter number",Toast.LENGTH_LONG).show();
                    return;
                }

                Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();

                makeRequest(String.valueOf(summarized_text.getText()),s);
            }
        });

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
                SummarizeActivity.this.runOnUiThread(new Runnable(){
                    public void run(){
                        summarized_text.setText(finalText);
                    }
                });

            }
        }).start();

    }


    public void makeRequest(String text,String lines){

        ProgressDialog pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            Toast.makeText(getApplicationContext(),"Sucess",Toast.LENGTH_LONG).show();

                            AlertDialog.Builder builder = new AlertDialog.Builder(SummarizeActivity.this);

                            builder.setTitle("Summary of " + FilesActivity.items.get(pos).getFilename());

                            builder.setIcon(R.drawable.common_full_open_on_phone);

                            builder.setMessage(jsonObject.getString("text_summary"));

                            builder.setCancelable(true);

                            builder.show();

                            pDialog.hide();

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),"Sucess",Toast.LENGTH_LONG).show();

                            pDialog.hide();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Test Error", error.toString());
                Toast.makeText(getApplicationContext(),"failed",Toast.LENGTH_LONG).show();
                pDialog.hide();
            }
        }) {
            @Override
            protected Map getParams() {

                Map params = new HashMap();
                params.put("originalText", text);
                params.put("numOfLines", lines);


                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue queue = Volley.newRequestQueue(SummarizeActivity.this);
        queue.add(stringRequest);

    }

}