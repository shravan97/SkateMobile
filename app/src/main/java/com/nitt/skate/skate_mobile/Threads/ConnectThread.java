package com.nitt.skate.skate_mobile.Threads;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by shravan on 4/2/18.
 */

public class ConnectThread extends Thread {
    private BluetoothSocket socket;
    private BluetoothAdapter adapter;
    private BluetoothDevice device;

    public ConnectThread(BluetoothDevice curDevice) {
        adapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothSocket tmpSocket = null;
        device = curDevice;

        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            String uuid = "SkateProj";
            tmpSocket = device.createRfcommSocketToServiceRecord(UUID.nameUUIDFromBytes(uuid.getBytes()));
        } catch (IOException e) {
            Log.e("Connect-Thread-Init", "Socket's create() method failed", e);
        }
        socket = tmpSocket;


    }

    public void run() {
        adapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            socket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                socket.close();
            } catch (IOException closeException) {
                Log.e("Connect-Thread-Run", "Could not close the client socket", closeException);
            }

            return;
        }

        Log.e("Connect-Thread-Run", "Connected !");

    }

    public BluetoothSocket StartThread() {
        run();
        return socket;
    }

    public BluetoothSocket getSocket() {
        return socket;
    }

    public void cancel() {
        try {
            socket.close();
        } catch (IOException e) {
            Log.e("Connect-Thread-Close", "Could not close the client socket", e);
        }
    }
}
