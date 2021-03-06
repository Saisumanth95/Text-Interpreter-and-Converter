package com.saisumanth.textinterpreterconverter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private final int PDF_SELECT = 1;
    private final int PHOTO_SELECT = 2;
    private String TAG = "Testing";

    private MyTessOCR tessaract;
    private Button uploadDoc,uploadImg;
    private Button convertBtn;
    private Uri pdfUri;
    public Uri photoUri;
    public Bitmap photo;
    public static ProgressDialog progressDialog;
    public static String TexttoDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            startActivity(new Intent(this,LoginActivity.class));
            finish();
        }

        uploadDoc = findViewById(R.id.upload_doc);
        uploadImg = findViewById(R.id.upload_img);
        convertBtn = findViewById(R.id.convert);
        convertBtn.setEnabled(false);
        progressDialog = new ProgressDialog(MainActivity.this);
        pdfUri = null;
        progressDialog.setMessage("Converting...");
        progressDialog.setCanceledOnTouchOutside(false);


        if(isWriteStoragePermissionGranted()) {
            tessaract = new  MyTessOCR(MainActivity.this);
        }
        TexttoDisplay = "";

        uploadDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isReadStoragePermissionGranted()){

                    Intent intent = new Intent();
                    intent.setType("application/pdf");
                    intent.setAction(intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent,PDF_SELECT);

                }else{

                    Toast.makeText(getApplicationContext(),"Permission Required",Toast.LENGTH_SHORT).show();

                }

            }
        });

        uploadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isReadStoragePermissionGranted()){

                    Intent photoPick = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    startActivityForResult(photoPick,PHOTO_SELECT);

                }else{

                    Toast.makeText(getApplicationContext(),"Permission Required",Toast.LENGTH_SHORT).show();

                }

            }
        });

        convertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                progressDialog.show();

                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if(pdfUri != null){

                            convertToText();


                        }else if(photoUri != null){

                            TexttoDisplay = tessaract.getOCRResult(photo);

                        }

                        startActivity(new Intent(MainActivity.this,DisplayActivity.class));

                    }
                }, 100);


            }
        });


    }

    public void convertToText(){

        File file = null;

        try {
            file = FileUtil.from(MainActivity.this,pdfUri);
            Log.d("file", "File...:::: uti - "+file .getPath()+" file -" + file + " : " + file .exists());

        } catch (IOException e) {
            e.printStackTrace();
        }

        pdfToBitmap(file);

    }

    private void pdfToBitmap(File pdfFile){

        try {
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));

            Bitmap bitmap;
            final int pageCount = renderer.getPageCount();

            if(pageCount > 100){
                Toast.makeText(getApplicationContext(),"Select Document less than 100 pages",Toast.LENGTH_LONG).show();
                return;
            }

            for (int i = 0; i < pageCount; i++) {

                PdfRenderer.Page page = renderer.openPage(i);

                int width = getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
                int height = getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                String convertedText = tessaract.getOCRResult(bitmap);
                TexttoDisplay += convertedText;

                page.close();

            }
            renderer.close();
        } catch (Exception ex) {
            ex.printStackTrace();

            Toast.makeText(getApplicationContext(), ex.getMessage(),Toast.LENGTH_LONG).show();

        }

    }


    public  boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted1");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted1");
            return true;
        }
    }

    public  boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted2");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked2");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted2");
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();

            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Permission Required", Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode == 2){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();

            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Permission Required", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==PDF_SELECT && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            pdfUri = data.getData();
            uploadDoc.setText("Document Selected");
            uploadImg.setEnabled(false);
            convertBtn.setEnabled(true);

        }else if(requestCode==PHOTO_SELECT && resultCode==RESULT_OK && data!=null && data.getData()!=null){

            photoUri = data.getData();

            try {
                photo = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            uploadImg.setText("Image Selected");
            uploadDoc.setEnabled(false);
            convertBtn.setEnabled(true);

        }else{
            Toast.makeText(getApplicationContext(),"Error in selecting pdf or Image",Toast.LENGTH_LONG).show();
        }

    }



}