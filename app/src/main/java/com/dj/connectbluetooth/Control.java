package com.dj.connectbluetooth;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class Control extends AppCompatActivity implements Button.OnClickListener {

    Button btn1,btn2,btn3,btn4,btn5,btnDisconnect,btnSend;
    String address = null;
    TextView lumn,rssiMsg;
    EditText data;
    Byte sendData;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        initXML();

        new Control.ConnectBT().execute();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Control.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        receiveSignal();
                    }
                });
            }
        },0,1000);
    }

    @SuppressLint("SetTextI18n")
    private void initXML() {
        btn1 = findViewById(R.id.button2);
        btn1.setOnClickListener(this);
        btn2 = findViewById(R.id.button3);
        btn2.setOnClickListener(this);
        btn3 = findViewById(R.id.button5);
        btn3.setOnClickListener(this);
        btn4 = findViewById(R.id.button6);
        btn4.setOnClickListener(this);
        btn5 = findViewById(R.id.button7);
        btn5.setOnClickListener(this);
        btnDisconnect = findViewById(R.id.button4);
        btnDisconnect.setOnClickListener(this);
        btnSend = findViewById(R.id.send);
        btnSend.setOnClickListener(this);
        lumn = findViewById(R.id.textView2);
        data = findViewById(R.id.editTextNumber);
        data.setText(null);
        rssiMsg = findViewById(R.id.textView3);

        Intent intent = getIntent();
        address = intent.getStringExtra(MainActivity.EXTRA_ADDRESS);
        //long rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.button2:
                sendSignal((byte) 1);
                break;
            case R.id.button3:
                sendSignal((byte) 2);
                break;
            case R.id.button5:
                sendSignal((byte) 3);
                break;
            case R.id.button6:
                sendSignal((byte) 4);
                break;
            case R.id.button7:
                sendSignal((byte) 5);
                break;
            case R.id.button4:
                Disconnect();
                break;
            case R.id.send:
                if(data.getText() == null) {
                    msg("Type Something!");
                } else {
                    sendData = Byte.parseByte(data.getText().toString());
                }
                if(sendData > 126){
                    msg("Enter numbers between 0 to 126");
                } else {
                    sendSignal(sendData);
                    data.setText(null);
                }
                break;
        }
    }

    private void sendSignal (byte number) {
        if( btSocket != null) {
            try {
                btSocket.getOutputStream().write(number);
            }catch (IOException e) {
                msg("Error");
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void receiveSignal() {
        StringBuilder receivedData = new StringBuilder();
        try{
            InputStream in = btSocket.getInputStream();
            int temp = in.available();
            for(int i=0;i<temp;i++) {
                int i11 = in.read();
                if(i11 != -1)
                    receivedData.append((char) i11);
            }
        } catch (IOException ignored) {

        }
        if(receivedData.toString().trim().length() != 0) {
            rssiMsg.setText(receivedData.toString());
        }
    }

    private void Disconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();
            } catch(IOException e) {
                msg("Error");
            }
        }
        finish();
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    @SuppressLint("StaticFieldLeak")
    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(Control.this, "Connecting...", "Please Wait!!!");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if ((btSocket == null) || (!isBtConnected)) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();
                }
            } catch (IOException e) {
                ConnectSuccess = false;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try Again.");
                finish();
            } else {
                msg("Connected");
                isBtConnected = true;
            }

            progress.dismiss();
        }
    }
}