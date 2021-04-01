package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static com.robot.asus.robotactivity.RobotActivity.robotAPI;

public class CameraActivity extends AppCompatActivity {

    private int PICK_IMAGE_REQUEST = 111,REQUEST_IMAGE_CAPTURE = 1;
    private String selectedImagePath, currentPhotoPath,responseResult;
    Bitmap bitmap;
    private Button btnCamera;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Intent intent = getIntent();
        btnCamera = findViewById(R.id.btnCamera);

        try {
            dispatchTakePictureIntent();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*try {
            galleryAddPic();
        } catch (IOException e) {
            Log.e("bitmap",e.toString());
        }*/

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FacialRecognition("https://zenbo.pythonanywhere.com/identify_user");

            }
        });



    }

    public void FacialRecognition(String postUrl) {

        Log.d("Server", "byte");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        // Read BitMap by file path
        Bitmap bitmap = BitmapFactory.decodeFile(selectedImagePath, options);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", selectedImagePath, RequestBody.create(MediaType.parse("image/*jpg"), byteArray))
                .build();

        Log.d("path",selectedImagePath);

        //TextView responseText = findViewById(R.id.txFacialResponse);
        //responseText.setText("Please wait ...");

        postRequest(postUrl, postBodyImage);
    }

    void postRequest(final String postUrl, RequestBody postBody) {

        final OkHttpClient client = new OkHttpClient();

        final okhttp3.Request request = new okhttp3.Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                // Cancel the post on failure.
                call.cancel();
                e.printStackTrace();


                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       // robotAPI.robot.speak("Zenbo Cannot Connect to the Server");
                        //TextView responseText = findViewById(R.id.txFacialResponse);
                        //responseText.setText("Failed to Connect to Server");
                    }
                });
            }

            @Override
            public void onResponse(Call call, final okhttp3.Response response) throws IOException {
                // In order to access the TextView inside the UI thread, the code is executed inside runOnUiThread()
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //TextView responseText = findViewById(R.id.txFacialResponse);
                        try {

                            if(postUrl.equals("https://zenbo.pythonanywhere.com/identify_user")){
                                //robotAPI.robot.speak(response.body().string());
                                //responseText.setText(response.body().string());
                                //responseResult = responseText.getText().toString();
                                responseResult = response.body().string();
                                Log.d("response", responseResult);
                            }
                            else{
                                JSONObject object = new JSONObject(response.body().string());
                                JSONArray resultArray = object.getJSONArray("result");
                                for (int i = 0; i < resultArray.length() ; i++) {
                                    JSONObject resultobject = resultArray.getJSONObject(i);
                                    //robotAPI.robot.speak("result is "+resultobject.getString("label"));
                                    String response = resultobject.getString("label");
                                    Log.d("responsePray",response);
                                    //responseText.setText(resultobject.getString("label"));
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();

            try {
                //bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //selectedImagePath = filePath.toString();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            // Bundle extras = data.getExtras();
            //Bitmap imageBitmap = (Bitmap) extras.get("data");
            Bitmap imageBitmap  = (Bitmap) data.getExtras().get("data");
            //imageView.setImageBitmap(imageBitmap);

        }
    }


    private void dispatchTakePictureIntent() throws IOException {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Log.d("photofile",ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider2",
                        photoFile);
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), photoURI);
                selectedImagePath = currentPhotoPath;
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        //String imageFileName = "JPEG_" + timeStamp + "_";
        String imageFileName = "FaceImage";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        Log.d("file",currentPhotoPath);

        //selectedImagePath = currentPhotoPath;
        return image;
    }

    private void galleryAddPic() throws IOException {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), contentUri);
        selectedImagePath = contentUri.toString();
        Log.d("Img Path",selectedImagePath);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

}
