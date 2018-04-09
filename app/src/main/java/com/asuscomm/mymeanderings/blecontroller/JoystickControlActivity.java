package com.asuscomm.mymeanderings.blecontroller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import Joystick.JoystickView;

public class JoystickControlActivity extends AppCompatActivity {
    private JoystickView leftJoystickView;
    private JoystickView rightJoystickView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joystick_control);

        leftJoystickView = (JoystickView) findViewById(R.id.left_joystick);
        rightJoystickView = (JoystickView) findViewById(R.id.right_joystick);
    }
}
