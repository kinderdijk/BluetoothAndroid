package com.asuscomm.mymeanderings.blecontroller;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DeviceControlActivity extends AppCompatActivity {

    private static final String TAG = DeviceControlActivity.class.getSimpleName();

    public static String DEVICE_ADDRESS = "device_address";
    public static String DEVICE_NAME = "device_name";

    private TextView deviceTitle;
    private TextView deviceAddress;
    private ListView characteristicListView;
    private ArrayAdapter charArrayAdapter;
    private int updatedRowPosition;
    private Button controllerButton;

    private BluetoothLeService bleService;

    private List<String> gattCharacteristics;

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

            Log.d(TAG, "Attempting to connect the device.");
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
        controllerButton = (Button) findViewById(R.id.control_button);
        characteristicListView = (ListView) findViewById(R.id.char_list_view);

        gattCharacteristics = new ArrayList<>();

        characteristicListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        charArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, gattCharacteristics)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                final View renderer = super.getView(position, convertView, parent);

                if (position == updatedRowPosition)
                {
                    renderer.setBackgroundResource(android.R.color.darker_gray);
                }
                return renderer;
            }
        };

        controllerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), JoystickControlActivity.class);

                startActivity(intent);
            }
        });

        characteristicListView.setAdapter(charArrayAdapter);

        if ("null".equals(name) || name == null)
            deviceTitle.setText("Unknown device");
        else
            deviceTitle.setText(name);
        deviceAddress.setText(address);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action))
            {
                Log.d(TAG, "Services discovered");
                displayGattServices(bleService.getSupportedGattServices());
            }
            else if(BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action))
            {
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                System.out.println(data);
            }
        }
    };

    private void displayGattServices(List<BluetoothGattService> gattServices)
    {
        List<String> currentGattServiceList = new ArrayList<>();
        for (BluetoothGattService service : gattServices)
        {
            String uuid = service.getUuid().toString();
            currentGattServiceList.add(uuid);

            List<BluetoothGattCharacteristic> gattServiceCharacteristics = service.getCharacteristics();

            for(BluetoothGattCharacteristic characteristic : gattServiceCharacteristics)
            {
                if (!gattCharacteristics.contains(characteristic.getUuid().toString()))
                {
                    gattCharacteristics.add(characteristic.getUuid().toString());
                    charArrayAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bleService != null) {
            final boolean result = bleService.connect(address);
            Log.d(TAG, "Connect request result=" + result);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
