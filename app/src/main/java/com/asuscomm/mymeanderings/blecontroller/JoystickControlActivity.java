package com.asuscomm.mymeanderings.blecontroller;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import Joystick.JoystickView;

public class JoystickControlActivity extends AppCompatActivity {

    private static final String TAG = JoystickControlActivity.class.getSimpleName();

    private JoystickView leftJoystickView;
    private JoystickView rightJoystickView;

    private BluetoothLeService bleService;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bleService = ((BluetoothLeService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bleService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joystick_control);

        leftJoystickView = (JoystickView) findViewById(R.id.left_joystick);
        rightJoystickView = (JoystickView) findViewById(R.id.right_joystick);

        leftJoystickView.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                // This returns the angle and the strength of the movement. The strength is in terms of percent, and the angle begins at that left and rotates counter closkwise.
                //Basically for the rc controller we want the left one to control the speed and the right one to control the direction.
                double motorStrength = strength*Math.sin((angle*Math.PI/180.0));

                int motor = (int) Math.round(motorStrength);
                Log.d(TAG, "Attempting to write " + motor + " to SERVO");
                bleService.writeCharacteristicValue(BluetoothLeService.RC_MOTOR_UUID, motor);
            }
        });

        rightJoystickView.setOnMoveListener(new JoystickView.OnMoveListener() {
            @Override
            public void onMove(int angle, int strength) {
                double servoStrength = strength*Math.cos((angle*Math.PI/180.0));

                int servo = (int) Math.round(servoStrength);
                Log.d(TAG, "Attempting to write " + servo + " to MOTOR");
                bleService.writeCharacteristicValue(BluetoothLeService.RC_SERVO_UUID, servo);
            }
        });

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
    }
}
