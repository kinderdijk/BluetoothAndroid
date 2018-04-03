package com.asuscomm.mymeanderings.blecontroller;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class DeviceControlActivity extends AppCompatActivity {

    private static final String TAG = DeviceControlActivity.class.getSimpleName();

    public static String DEVICE_ADDRESS = "device_address";
    public static String DEVICE_NAME = "device_name";

    private TextView deviceTitle;
    private TextView deviceAddress;
    private BluetoothLeService bleService;

    private String name;
    private String address;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bleService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!bleService.initialize())
            {
                Log.d(TAG, "Problem initializing service.");
                finish();
            }

            bleService.connect(address);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bleService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);

        final Intent intent = getIntent();
        name = intent.getStringExtra(DEVICE_NAME);
        address = intent.getStringExtra(DEVICE_ADDRESS);

        Log.d(TAG, "Device name: " + name);
        Log.d(TAG, "Device address: " + address);

        deviceTitle = (TextView) findViewById(R.id.device_name);
        deviceAddress = (TextView) findViewById(R.id.device_address);

        if ("null".equals(name) || name == null)
            deviceTitle.setText("Unknown device");
        else
            deviceTitle.setText(name);
        deviceAddress.setText(address);
    }
}
