package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.results.RoomInfo;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import java.util.ArrayList;

public class livingRoomActivity extends RobotActivity {

    public livingRoomActivity(RobotCallback robotCallback, RobotCallback.Listen robotListenCallback) {
        super(robotCallback, robotListenCallback);
    }

    private static String sRoom2;
    private static String sRoom3;
    private static String thismap = "map2";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_living_room);

       // ArrayList<RoomInfo> arrayListRooms = robotAPI.contacts.room.getAllRoomInfo();
        //sRoom2 = arrayListRooms.get(1).keyword;

        Intent i = getIntent();
        sRoom3 = i.getStringExtra("sRoom3");
        sRoom2 = i.getStringExtra("sRoom2");

        robotAPI.robot.speak("At map " + sRoom2);

        new CountDownTimer(10000,1000) {

            @Override
            public void onTick(long millisUntilFinished) {


            }

            @Override
            public void onFinish() {

                robotAPI.motion.goTo(sRoom2);
            }
        }.start();



        new CountDownTimer(60000,1000) {

            @Override
            public void onTick(long millisUntilFinished) {


            }

            @Override
            public void onFinish() {

                robotAPI.robot.speak("Living room done");
                Intent i = new Intent(livingRoomActivity.this,motionControlActivity.class);
                //Intent i = new Intent(Submap0_activity.this,Submap1_activity.class);
                i.putExtra("sRoom2",sRoom2);
                i.putExtra("sRoom3",sRoom3);
                i.putExtra("thismap",thismap);
                startActivity(i);
            }
        }.start();

    }

    @Override
    protected void onResume() {
        super.onResume();

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


    public livingRoomActivity() {
        super(robotCallback, robotListenCallback);
    }


}
