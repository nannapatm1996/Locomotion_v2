package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.VisionConfig;
import com.asus.robotframework.API.results.DetectFaceResult;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import java.util.List;

public class motionControlActivity extends RobotActivity {

    public motionControlActivity(RobotCallback robotCallback, RobotCallback.Listen robotListenCallback) {
        super(robotCallback, robotListenCallback);
    }

    private static int current_map;
    private static Context context;
    private static  String facedetect_result = "no face detect"; //check if face is detected
    CountDownTimer TimerDetect;
    private String prev_map;
    private static String sRoom2;
    private static String sRoom3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motion_control);

        context = getApplicationContext();
        Intent i = getIntent();
        sRoom2 = i.getStringExtra("sRoom2"); //livingroom
        //sRoom3 = i.getStringExtra("sRoom3"); //bedroom
        prev_map = i.getStringExtra("thismap");

        robotAPI.robot.speak("Motion Control");

        new CountDownTimer(5000,1000) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                startDetectFace();

            }
        }.start();



        TimerDetect = new CountDownTimer(80000,20000) {

            @Override
            public void onTick(long millisUntilFinished) {

                robotAPI.robot.speak("Rotating...");
                robotAPI.motion.moveBody(0,0,90);

                if(!facedetect_result.equals("no face detect") && TimerDetect != null){

                    TimerDetect.cancel();
                    TimerDetect = null;
                    robotAPI.robot.speak("Timer Stopped");
                    stopDetectFace();
                    robotAPI.robot.speak("Face Found");
                    //Intent i = new Intent(context, faceRecognitionActivity.class);
                    //startActivity(i);
                }
            }

            @Override
            public void onFinish() {

                robotAPI.robot.speak("Timeout");

                stopDetectFace();

                if(prev_map.equals("map1")){
                    Intent i = new Intent(context,livingRoomActivity.class);
                    i.putExtra("sRoom2",sRoom2);
                    //i.putExtra("sRoom3",sRoom3);
                    startActivity(i);
                }
                else if (prev_map.equals("map2")){
                    Intent i = new Intent(context,bedroomActivity.class);
                    //i.putExtra("sRoom3",sRoom3);
                    startActivity(i);
                }
                else if (prev_map.equals("map3")){
                    robotAPI.robot.speak("Patient Not Found");
                }
            }
        }.start();
    }

    public static RobotCallback robotCallback = new RobotCallback() {
        @Override
        public void initComplete() {
            super.initComplete();
        }

        @Override
        public void onResult(int cmd, int serial, RobotErrorCode err_code, Bundle result) {
            super.onResult(cmd, serial, err_code, result);


        }

        @Override
        public void onDetectFaceResult(List<DetectFaceResult> resultList) {
            super.onDetectFaceResult(resultList);

            Log.d("RobotDevSample", "onDetectFaceResult: " + resultList.get(0));

            //use toast to show detected faces
            facedetect_result = "Face Detected";
            String toast_result = "Detect Face";
            Toast toast = Toast.makeText(context, toast_result, Toast.LENGTH_SHORT);
            toast.show();
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


    public motionControlActivity() {
        super(robotCallback, robotListenCallback);
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
}
