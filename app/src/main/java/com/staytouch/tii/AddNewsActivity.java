package com.staytouch.tii;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import at.markushi.ui.CircleButton;

public class AddNewsActivity extends AppCompatActivity {

    private CircleButton newsImageUploadBtn;
    private ImageView uploadedImage;
    private static final int CAMERA_REQUEST=1001;
    private static final int SELECT_FILE=1002;
    private static String imageFilePath;
    private static final String AUTHORITY = BuildConfig.APPLICATION_ID+".provider";
    private EditText newsHeadLinesEditText;
    private EditText newsDescriptionEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_news);
        newsHeadLinesEditText =(EditText)findViewById(R.id.NewsHeadLines);
        newsDescriptionEditText =(EditText)findViewById(R.id.NewsDescription);
        Utils.alignHintEditText(newsHeadLinesEditText, "Headlines here...");
        Utils.alignHintEditText(newsDescriptionEditText, "Description here...");
        newsImageUploadBtn =(CircleButton)findViewById(R.id.NewsImageUploadBtn);
        uploadedImage=(ImageView)findViewById(R.id.uploadedImage);
        newsImageUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        if(imageFilePath!=null){
            Glide.with(this).load(imageFilePath).into(uploadedImage);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)  {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==Activity.RESULT_OK){
            if(requestCode==CAMERA_REQUEST){
               Glide.with(this).load(imageFilePath).into(uploadedImage);
            }
            else if(requestCode==SELECT_FILE){
                Uri selectedImageUri = data.getData();
                Glide.with(this).load(selectedImageUri).into(uploadedImage);
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();
    }

    @Override
    public boolean onSupportNavigateUp(){
        onBackPressed();
        return true;
    }

    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir =
                getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        imageFilePath = image.getAbsolutePath();
        return image;
    }

    private void selectImage(){
        final CharSequence[] items ={"Camera", "Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(AddNewsActivity.this);
        builder.setTitle("Add Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(items[which].equals("Camera")){
                    takePhoto();
                }else if(items[which].equals("Gallery")){
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent.createChooser(intent, "Select File"), SELECT_FILE);
                } else if(items[which].equals("Cancel")){
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void takePhoto(){

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null){
            //Create a file to store the image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,AUTHORITY,photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        photoURI);
                startActivityForResult(intent,CAMERA_REQUEST);
            }
        }
    }
}
