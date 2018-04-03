package com.asuscomm.mymeanderings.blecontroller;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class BluetoothLeService extends Service {
    private BluetoothManager bleManager;
    private BluetoothAdapter bleAdapter;

    private String bleDeviceAddress;
    private BluetoothGatt bleGatt;

    private static final String TAG = BluetoothLeService.class.getSimpleName();

    public BluetoothLeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS)
            {
                for (BluetoothGattService service : gatt.getServices())
                {
                    Log.d(TAG, "Service: " + service.getUuid().toString());
                }
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
        }
    };

    public boolean connect(final String address)
    {
        final BluetoothDevice device = bleAdapter.getRemoteDevice(address);
        if (device == null)
        {
            Log.e(TAG, "Error connecting to device.");
            return false;
        }

        bleGatt = device.connectGatt(this, false, gattCallback);
        return false;
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    public boolean initialize()
    {
        if (bleManager == null)
        {
            bleManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
            if (bleManager == null)
            {
                Log.e(TAG, "Unable to create bluetooth manager.");
                return false;
            }
        }

        bleAdapter = bleManager.getAdapter();
        if(bleAdapter == null)
        {
            Log.e(TAG, "Unable to create bluetooth adapter.");
            return false;
        }

        return true;
    }
}
