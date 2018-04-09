package com.asuscomm.mymeanderings.blecontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button startScanningButton;
    private Button controlButton;
    private ListView listView;
    private ArrayAdapter listContentAdaptor;
    private boolean isScanning = false;
    private int SCAN_DURATION = 5000;
    private static final String TAG = MainActivity.class.getSimpleName();

    private BluetoothManager bleManager;
    private BluetoothAdapter bleAdaptor;
    private List<String> bleDeviceAddress;
    private Map<String, BluetoothDevice> bleDeviceMap;

    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            System.out.println("No bluetooth enabled.");
        }

        handler = new Handler();
        bleDeviceAddress = new ArrayList<>();
        bleDeviceMap = new HashMap<>();
        bleManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdaptor = bleManager.getAdapter();

        listView = (ListView) findViewById(R.id.listView);

        listContentAdaptor = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, bleDeviceAddress);
        listView.setAdapter(listContentAdaptor);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // Connect to the device that has been clicked.
                final String deviceAddress = bleDeviceAddress.get(position);
                final BluetoothDevice device = bleDeviceMap.get(deviceAddress);

                if (device == null)
                    return;

                Intent intent = new Intent(getApplicationContext(), DeviceControlActivity.class);
                intent.putExtra(DeviceControlActivity.DEVICE_ADDRESS, device.getAddress());
                intent.putExtra(DeviceControlActivity.DEVICE_NAME, device.getName());

                // If scanning, stop.
                if (isScanning)
                {
                    bleAdaptor.stopLeScan(scannerCallback);
                    isScanning = false;
                }

                startActivity(intent);
            }
        });

        startScanningButton = (Button) findViewById(R.id.scan_button);
        startScanningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isScanning)
                {
                    Log.d(TAG, "Start scanning");
                    bleScanning(true);
                    startScanningButton.setText("Stop Scanning");
                }
                else
                {
                    bleScanning(false);
                    startScanningButton.setText("Start Scanning");
                }

            }
        });

        controlButton = (Button) findViewById(R.id.control_button);
        controlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), JoystickControlActivity.class);
                startActivity(intent);
            }
        });
    }

    private BluetoothAdapter.LeScanCallback scannerCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord)
        {
            runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    // Do something here with the device results.
                    System.out.println("Got a device: " + device.getAddress());
                    System.out.println("Name: " + device.getName());

                    bleDeviceMap.put(device.getAddress(), device);
                    if(!bleDeviceAddress.contains(device.getAddress()))
                    {
                        bleDeviceAddress.add(device.getAddress());
                        listContentAdaptor.notifyDataSetChanged();
                    }
                }
            });
        }
    };

    public void bleScanning(final boolean enable) {
        if (enable)
        {
            handler.postDelayed(new Runnable() {
                public void run()
                {
                    isScanning = false;
                    bleAdaptor.stopLeScan(scannerCallback);
                    startScanningButton.setText("Start Scanning");
                }
            }, SCAN_DURATION);

            isScanning = true;
            bleAdaptor.startLeScan(scannerCallback);
        }
        else
        {
            isScanning = false;
            bleAdaptor.stopLeScan(scannerCallback);
        }
    }
}
