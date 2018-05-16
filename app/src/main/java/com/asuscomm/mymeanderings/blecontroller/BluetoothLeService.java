package com.asuscomm.mymeanderings.blecontroller;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

public class BluetoothLeService extends Service {
    private BluetoothManager bleManager;
    private BluetoothAdapter bleAdapter;

    private BluetoothGatt bleGatt;

    public final static String ACTION_GATT_CONNECTED = "com.asuscomm.mymeanderings.blecontroller.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.asuscomm.mymeanderings.blecontroller.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = "com.asuscomm.mymeanderings.blecontroller.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA = "com.asuscomm.mymeanderings.blecontroller.EXTRA_DATA";

    public final static UUID RC_SERVICE_UUID = new UUID(new BigInteger("7efc7301b7974ae8", 16).longValue(), new BigInteger("ba40dd321aef33c2", 16).longValue());
    public final static UUID RC_MOTOR_UUID = new UUID(new BigInteger("3105a410260d4241", 16).longValue(), new BigInteger("8aea39db07493e03", 16).longValue());
    public final static UUID RC_SERVO_UUID = new UUID(new BigInteger("6a5a107022ba4d0f", 16).longValue(), new BigInteger("af6a394c4de4df19", 16).longValue());

    private static final String TAG = BluetoothLeService.class.getSimpleName();

    public BluetoothLeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                Log.d(TAG, "Connection to the device.");
                intentAction = ACTION_GATT_CONNECTED;
                bleGatt.discoverServices();

                broadcastUpdate(intentAction);
            }

            if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                Log.d(TAG, "Disconnecting device.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            if(status == BluetoothGatt.GATT_SUCCESS)
            {
                for (BluetoothGattService service : gatt.getServices())
                {
                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                    Log.d(TAG, "Service: " + service.getUuid().toString());
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            Log.d(TAG, "Characteristic was read.");
            Log.d(TAG, "Value: " + characteristic.getValue());
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
        {
            Log.d(TAG, "Characteristic was written.");
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
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

        Log.d(TAG, "Connecting to the device.");
        bleGatt = device.connectGatt(this, false, gattCallback);
        return false;
    }

    public class LocalBinder extends Binder
    {
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

    public List<BluetoothGattService> getSupportedGattServices()
    {
        if (bleGatt == null) return null;
        Log.d(TAG, "Attempting to discover services.");
        return bleGatt.getServices();
    }

    // Perhaps right now it would be best to just hard code this value, and if it is necessary later to find a better way to do it.
    public BluetoothGattService getGattService()
    {
        return bleGatt.getService(RC_SERVICE_UUID);
    }

    public boolean writeCharacteristicValue(UUID uuid, int value)
    {
        if (bleGatt == null)
        {
            Log.d(TAG, "Connection was lost.");
            return false;
        }

        BluetoothGattCharacteristic characteristic = bleGatt.getService(RC_SERVICE_UUID).getCharacteristic(uuid);
        characteristic.setValue(value, BluetoothGattCharacteristic.FORMAT_SINT8, 0);
        return bleGatt.writeCharacteristic(characteristic);
    }

    private void broadcastUpdate(final String action)
    {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic)
    {
        final Intent intent = new Intent(action);

        // For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0)
        {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
        }
        sendBroadcast(intent);
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    public void close()
    {
        if (bleGatt == null)
        {
            return;
        }
        bleGatt.close();
        bleGatt = null;
    }
}
