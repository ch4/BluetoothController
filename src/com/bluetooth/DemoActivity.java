package com.bluetooth;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.bluetooth.manager.BluetoothManager;
import com.bluetooth.manager.tools.BluetoothDevicesReceiver;
import com.bluetooth.manager.tools.BluetoothHandler;
import com.bluetooth.manager.tools.Logger;

public class DemoActivity extends Activity
{
    private static final int        REQUEST_ENABLE_BLUETOOTH = 0;
    private static final int        DISCOVERABLE_DURATION    = 600;

    private final BluetoothAdapter  bluetoothAdapter         = BluetoothAdapter.getDefaultAdapter();
    private final BroadcastReceiver broadcastReceiver        = new BluetoothDevicesReceiver();
    private final BluetoothManager  bluetoothManager         = new BluetoothManager(new BluetoothHandler());

    private Logger                  logger                   = new Logger(this);

    public BluetoothManager getBluetoothManager()
    {
        return this.bluetoothManager;
    }

    void startDiscovery()
    {
        this.bluetoothAdapter.startDiscovery();
    }

    void getBondedDevices()
    {
        Set<BluetoothDevice> bluetoothDevices = this.bluetoothAdapter.getBondedDevices();

        if (bluetoothDevices.isEmpty())
        {
            return;
        }
        for (BluetoothDevice bluetoothDevice : bluetoothDevices)
        {
            this.logger.d("Bounded device founded: " + bluetoothDevice.getName() + ", address: [" + bluetoothDevice.getAddress() + "]");
        }
    }

    void makeDeviceDiscoverable()
    {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERABLE_DURATION);
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        findViewById(R.id.button_start_discovery).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startDiscovery();
            }
        });

        findViewById(R.id.button_bonded_devices).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                getBondedDevices();
            }
        });

        findViewById(R.id.button_listen).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                makeDeviceDiscoverable();
                getBluetoothManager().listen();
            }
        });

        findViewById(R.id.button_stop_listening).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                getBluetoothManager().stopListening();
            }
        });

        findViewById(R.id.send_holstered).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                getBluetoothManager().sendString("test".getBytes());
            }
        });

        findViewById(R.id.send_unholstered).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                getBluetoothManager().sendString("test".getBytes());
            }
        });

        findViewById(R.id.toggle_holster_state).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //getBluetoothManager().sendString("test".getBytes());
                toggleState();
            }
        });

        makeDeviceDiscoverable();
        getBluetoothManager().listen();

        if (this.bluetoothAdapter == null)
        {
            throw new NullPointerException();
        }

        if (!this.bluetoothAdapter.isEnabled())
        {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BLUETOOTH);
        }

        IntentFilter intentFilter;
        intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(this.broadcastReceiver, intentFilter);
        intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(this.broadcastReceiver, intentFilter);

        runnable.run();
    }

    public void toggleState(){
        isHolstered = !isHolstered;
        if(isHolstered){
            findViewById(R.id.holster_state_indicator).setBackgroundColor(0xFF00FF00);
        } else {
            findViewById(R.id.holster_state_indicator).setBackgroundColor(0xFFFF0000);
        }
    }

    public boolean isHolstered = true;
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable()
    {

        public void run()
        {

            if(getBluetoothManager().getState() == BluetoothManager.STATE_CONNECTED) {
                //connection is active
                if (isHolstered) {
                    Log.d("SensorEmulator","Sending Weapon Secure");
                    //is holstered, send safe message
                    getBluetoothManager().sendString("Weapon secure".getBytes());
                } else {
                    //is unholstered, send alert message
                    Log.d("SensorEmulator","Sending Weapon In Use");
                    getBluetoothManager().sendString("ALERT: Weapon in use".getBytes());
                }
            }
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(this.broadcastReceiver);
        this.bluetoothAdapter.cancelDiscovery();
        this.bluetoothAdapter.disable();
        this.bluetoothManager.stopListening();
        this.bluetoothManager.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_ENABLE_BLUETOOTH && resultCode == Activity.RESULT_OK)
        {
            // TODO Auto-generated method stub
        }

        // private static final int REQUEST_CONNECT_DEVICE = 1;
        // private static final String EXTRA_DEVICE_ADDRESS =

        // "EXTRA_DEVICE_ADDRESS";
        // in this activity------------------------------------------
        // Intent intent = new Intent(this, /*Activity*/);
        // startActivityForResult(intent, REQUEST_CONNECT_DEVICE);

        // in activity start from this-------------------------------
        // String address = "";
        // Intent intent = new Intent();
        // intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
        // setResult(Activity.RESULT_OK, intent);
        // finish();

        // in this activity -----------------------------------------
        // if (requestCode == REQUEST_CONNECT_DEVICE && resultCode ==
        // Activity.RESULT_OK)
        // {
        // String address = data.getExtras().getString(EXTRA_DEVICE_ADDRESS);
        // BluetoothDevice bluetoothDevice =
        // bluetoothAdapter.getRemoteDevice(address);
        // }
        // ----------------------------------------------------------
    }
}
