package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.VisionConfig;
import com.asus.robotframework.API.results.DetectFaceResult;
import com.asus.robotframework.API.results.RoomInfo;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

import static com.example.myapplication.bedroomActivity.sRoom3;

public class MainActivity extends RobotActivity {


    Button BtTimerStart;
    TextView TvTimerShow;
    public int counter;
    int milli = 3000;
    int countInter = 1000;

    public MainActivity(RobotCallback robotCallback, RobotCallback.Listen robotListenCallback) {
        super(robotCallback, robotListenCallback);
    }

    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static boolean isRobotApiInitialed = false;
    private static String sRoom1;
    private static String sRoom2;
    private static String sRoom3;
    private TextView mTvRoom1;
    private TextView mTvPermissionState;
    private Button mBtnPermission;
    private static Context context;
    private static String thismap = "map1";

    private int day, month, hr, min,PICK_IMAGE_REQUEST = 111,REQUEST_IMAGE_CAPTURE = 1;
    private String IslamicuUrl, PrayingUrl, FacialUrl, responseResult,selectedImagePath;
    Bitmap bitmap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BtTimerStart = (Button) findViewById(R.id.BtTimerStart);
        TvTimerShow = (TextView) findViewById(R.id.tvTimerDisplay);
        mTvPermissionState = (TextView) findViewById(R.id.TvPermission);
        mTvRoom1 = (TextView) findViewById(R.id.TvRoom1);
        counter = milli/countInter;

        mBtnPermission = (Button) findViewById(R.id.BtnPermission);


        IslamicuUrl = "https://zenbo.pythonanywhere.com/api/v1/resources/prayertime?day=" + day + "&month=" +
                month + "&hour=" + hr + "&minute=" + min + "&country=QA";
        PrayingUrl = "https://zenbo.pythonanywhere.com/praying_detection";
        FacialUrl = "https://zenbo.pythonanywhere.com/identify_user";

        //final Intent intent = new Intent(this, )

        mBtnPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });

        BtTimerStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new CountDownTimer(milli,countInter) {
                    @Override
                    public void onTick(long millisUntilFinished) {

                        TvTimerShow.setText(String.valueOf(counter));
                        counter--;

                    }

                    @Override
                    public void onFinish() {
                        TvTimerShow.setText("GO!");

                        ArrayList<RoomInfo> arrayListRooms = robotAPI.contacts.room.getAllRoomInfo();
                        sRoom1 = arrayListRooms.get(0).keyword;
                        sRoom2 = arrayListRooms.get(1).keyword;
                        //sRoom3 = arrayListRooms.get(2).keyword;

                        //TODO: add fetch time and country
                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date dt = new Date();
                        day = dt.getDate();
                        month = dt.getMonth() + 1;
                        hr = dt.getHours();
                        min = dt.getMinutes();

                        IslamicCalendar();

                        //mTvRoom1.setText(sRoom1 +" ;"+ sRoom2 );

                        //if()
                        //robotAPI.motion.goTo(sRoom1);


                    }
                }.start();

                new CountDownTimer(60000,1000) {

                    @Override
                    public void onTick(long millisUntilFinished) {


                    }

                    @Override
                    public void onFinish() {

                        robotAPI.robot.speak("Go To end");
                        Intent i = new Intent(MainActivity.this,motionControlActivity.class);
                        //Intent i = new Intent(Submap0_activity.this,Submap1_activity.class);
                        i.putExtra("sRoom2",sRoom2);
                        //i.putExtra("sRoom3",sRoom3);
                        i.putExtra("thismap",thismap);
                        startActivity(i);
                    }
                }.start();

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        // check permission READ_CONTACTS is granted or not
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted by user yet
            Log.d("ZenboGoToLocation", "READ_CONTACTS permission is not granted by user yet");
            mTvPermissionState.setText(getString(R.string.permission_not_granted));
            mBtnPermission.setEnabled(true);

        }
        else{
            // permission is granted by user
            Log.d("ZenboGoToLocation", "READ_CONTACTS permission is granted");
            mTvPermissionState.setText(getString(R.string.permission_granted));
            mBtnPermission.setEnabled(false);

        }

        // initial params
        mTvRoom1.setText(getString(R.string.first_room_info));

    }



    public static RobotCallback robotCallback = new RobotCallback() {
        @Override
        public void initComplete() {
            super.initComplete();

            Log.d("ZenboGoToLocation", "initComplete()");
            isRobotApiInitialed = true;
        }

        @Override
        public void onResult(int cmd, int serial, RobotErrorCode err_code, Bundle result) {
            super.onResult(cmd, serial, err_code, result);


        }

        @Override
        public void onDetectFaceResult(List<DetectFaceResult> resultList) {
            super.onDetectFaceResult(resultList);

            /*Log.d("RobotDevSample", "onDetectFaceResult: " + resultList.get(0));

            //use toast to show detected faces
            facedetect_result = "Face Detected";
            String toast_result = "Detect Face";
            Toast toast = Toast.makeText(context, toast_result, Toast.LENGTH_SHORT);
            toast.show();*/
        }

        @Override
        public void onStateChange(int cmd, int serial, RobotErrorCode err_code, RobotCmdState state) {
            super.onStateChange(cmd, serial, err_code, state);


        }
    };


    public static RobotCallback.Listen robotListenCallback = new RobotCallback.Listen() {
        @Override
        public void onFinishRegister() {

        }

        @Override
        public void onVoiceDetect(JSONObject jsonObject) {

        }

        @Override
        public void onSpeakComplete(String s, String s1) {

        }

        @Override
        public void onEventUserUtterance(JSONObject jsonObject) {

        }

        @Override
        public void onResult(JSONObject jsonObject) {

        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }
    };


    public MainActivity() {
        super(robotCallback, robotListenCallback);
    }


    private void requestPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                this.checkSelfPermission(Manifest.permission.READ_CONTACTS) ==
                        PackageManager.PERMISSION_GRANTED) {
            // Android version is lesser than 6.0 or the permission is already granted.
            Log.d("ZenboGoToLocation", "permission is already granted");
            return;
        }

        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            //showMessageOKCancel("You need to allow access to Contacts",
            //        new DialogInterface.OnClickListener() {
            //            @Override
            //            public void onClick(DialogInterface dialog, int which) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
            //            }
            //        });
        }
    }

    /*
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    */

    private static void startDetectFace() {
        // start detect face
        VisionConfig.FaceDetectConfig config = new VisionConfig.FaceDetectConfig();
        config.enableDebugPreview = true;  // set to true if you need preview screen
        config.intervalInMS = 2000;
        config.enableDetectHead = true;
        robotAPI.vision.requestDetectFace(config);
    }

    private void stopDetectFace() {
        // stop detect face
        robotAPI.vision.cancelDetectFace();
    }

    private void IslamicCalendar() {
        StringRequest req = new StringRequest(Request.Method.GET, IslamicuUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("Response", response);

                if (response.equals("True")) {
                    //mTvRoom1.setText(sRoom1 +" ;"+ sRoom2 );
                    robotAPI.motion.goTo(sRoom1);
                    //TextResponse.setText("Going to bedroom");


                } else {
                    robotAPI.motion.goTo(sRoom2);
                    //TextResponse.setText("Going to Living Room");
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", error.toString());

            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(req);
    }


}
