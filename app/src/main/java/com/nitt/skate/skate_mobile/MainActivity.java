package com.nitt.skate.skate_mobile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.speech.RecognizerIntent;

import com.nitt.skate.skate_mobile.Threads.ConnectThread;
import com.nitt.skate.skate_mobile.Threads.ReadWriteThread;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private ConnectThread connector;
    private ReadWriteThread readWriteThread;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        socket = null;
        readWriteThread = null;
        setContentView(R.layout.activity_main);
        btnSpeak = (ImageButton) findViewById(R.id.spkBtn);
        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(broadcastReceiver, filter);
    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                "Say Something !");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    "Sorry! Your device doesn\\'t support speech input",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void onStop(View v) {

        if (readWriteThread != null) {
            String msg = "Stop";
            readWriteThread.write(msg.getBytes());
            Toast.makeText(MainActivity.this, "Stopped !!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this, "Check your Bluetooth connection to the Skate Board and try again !", Toast.LENGTH_LONG).show();
        }
    }

    public void onBluetoothConnect(View v) {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                if (deviceName.equals("My Android Things device")) {
                    Log.e("Bluetooth-bondage", "In ! Gotcha !");
                    connector = new ConnectThread(device);
                    socket = connector.StartThread();
//                    socket = connector.getSocket();

                    if (socket != null) {
                        if (!socket.isConnected()) {
                            Log.e("Bluetooth-Socket", "Not Connected");
                        } else {
                            v.setBackgroundColor(0xFF00FF00);
                            readWriteThread = new ReadWriteThread(socket);
                        }

                    }
                }
            }
        } else {
            boolean doneDiscovery = bluetoothAdapter.startDiscovery();
            if (doneDiscovery) {
                Log.e("tag", "Discovered some devices ");
            } else {
//            Log.e("tag", "Discovered no devices. Check  ");
//                v.setBackgroundColor(0xAAA);
                Toast.makeText(MainActivity.this, "Check Bluetooth Connectivity !", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onStartClick(View v) {
        if (readWriteThread != null) {
            String msg = "Start";
            readWriteThread.write(msg.getBytes());
            Toast.makeText(MainActivity.this, "Started !!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MainActivity.this, "Check your Bluetooth connection to the Skate Board and try again !", Toast.LENGTH_LONG).show();
        }

    }


    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                try {
                    Log.e("Bluetooth-Device-Name", deviceName);
                    Log.e("Bluetooth-Device-Addr", deviceHardwareAddress);
                    Log.e("equals", String.valueOf(deviceName.equals("My Android Things device")));
                } catch (NullPointerException e) {
                    Log.e("Bluetooth-Device", "Error in finding number of devices", e);
                }

                if (device.equals("My Android Things device")) {
                    Thread connector = new ConnectThread(device);
                    connector.run();
                }

            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                String res = "";

                if(result.size() > 0)
                    res = result.get(0);

//                for (String s : result) {
//                    Log.e("Voice-Recog", s);
//                    res = res.concat(" " + s);
//                }
                res = res.toLowerCase();
                switch (res) {
                    case "start": {
                        Log.e("Voice-Recog", "Start !!!!! Yay !");
                        Button startBtn = findViewById(R.id.button2);
                        onStartClick(startBtn);
                        break;
                    }
                    case "start it": {
                        Log.e("Voice-Recog", "Start it !!!!! Yay !");
                        Button startBtn = findViewById(R.id.button2);
                        onStartClick(startBtn);
                        break;
                    }
                    case "stop": {
                        Log.e("Voice-Recog", "Stop ! ");
                        Button stopBtn = findViewById(R.id.button3);
                        onStop(stopBtn);
                        break;
                    }
                    default: {
                        Log.e("Voice-Recog", "I don't recognize this ");
                        Toast.makeText(MainActivity.this, "Unrecognizable  !!", Toast.LENGTH_LONG).show();
                        break;
                    }

                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(broadcastReceiver);
    }
}
