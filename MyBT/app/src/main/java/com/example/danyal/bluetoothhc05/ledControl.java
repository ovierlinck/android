package com.example.danyal.bluetoothhc05;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

public class ledControl extends AppCompatActivity {

    public static final String TAG = "MyBT";
    Button btn1, btn2, btn3, btn4, btn5, btnDis;
    String address = null;
    ThermoModel model;
    TextView  temperatureTV, setpointTV, modeTV, timeTV;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        model = ViewModelProviders.of(this).get(ThermoModel.class);

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS);

        setContentView(R.layout.activity_led_control);

        btn1 = (Button) findViewById(R.id.button2);
        btn2 = (Button) findViewById(R.id.button3);
        btn3 = (Button) findViewById(R.id.button5);
        btn4 = (Button) findViewById(R.id.button6);
        btn5 = (Button) findViewById(R.id.button7);
        btnDis = (Button) findViewById(R.id.button4);

        temperatureTV = (TextView) findViewById(R.id.temperatureTV);
        model.temperature.observe(this, v->temperatureTV.setText(""+v+"°C"));

        setpointTV = (TextView) findViewById(R.id.setPointTV);
        model.setpoint.observe(this, v->setpointTV.setText(""+v+"°C"));

        modeTV = (TextView) findViewById(R.id.modeTV);
        model.mode.observe(this, v->modeTV.setText(v));

        timeTV = (TextView) findViewById(R.id.timeTV);
        model.timeText.observe(this, v->timeTV.setText(model.dateText.getValue() + " "+ model.timeText.getValue()));

        new ConnectBT().execute();
        new ReadBT().execute();

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSignal("AUTO\n");
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSignal("MANU 15\n");
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSignal("MANU 30\n");
            }
        });

        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSignal("getDate\n");
            }
        });

        btn5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSignal("getTime\n");
            }
        });

        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect();
            }
        });
    }

    private void sendSignal(String msg) {
        if (btSocket != null) {
            try {
                btSocket.getOutputStream().write(msg.getBytes());
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private void Disconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                msg("Error");
            }
        }

        finish();
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    private class ReadBT extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... devices) {

            while (btSocket == null || !isBtConnected) {
                Log.d(TAG, "###Waiting connection");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Log.d(TAG, "### Connected");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(btSocket.getInputStream()))) {
                while (btSocket != null && isBtConnected) {
                    Log.d(TAG, "### Waiting input");
                    String str = in.readLine();
                    Log.d(TAG, "###READ: " + str);

                    try {
                        JSONObject json = new JSONObject(str);
                        String name = json.names().get(0).toString();
                        Log.d(TAG, "---" + name + "---");
                        Log.d(TAG, name);
                        switch (name) {
                            case "TEMPERATURE":
                                model.temperature.postValue(json.getInt(name));
                                break;
                            case "SETPOINT":
                                model.setpoint.postValue(json.getInt(name));
                                break;
                            case "MODE":
                                model.mode.postValue(json.getString(name));
                                break;
                            case "DAY":
                                model.setDay(json.getInt(name));
                                break;
                            case "MONTH":
                                model.setMonth(json.getInt(name));
                                break;
                            case "YEAR":
                                model.setYear(json.getInt(name));
                                break;
                            case "HOUR":
                                model.setHour(json.getInt(name));
                                break;
                            case "MINUTE":
                                model.setMinute(json.getInt(name));
                                break;
                             case "SECOND":
                                 model.setSecond(json.getInt(name));
                                break;
                        }
                    } catch (JSONException e) {
                        Log.w(TAG, "Not a valid JSON string" + str);
                    }


                }
            } catch (IOException e) {
                Log.e(TAG, "Problem while reading input " + e.getMessage());
            }

            Log.e(TAG, "########### FINISHED READING");

            return null;
        }

    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean ConnectSuccess = true;

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(ledControl.this, "Connecting...", "Please Wait!!!");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (btSocket == null || !isBtConnected) {
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
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            } else {
                msg("Connected");
                isBtConnected = true;
            }

            progress.dismiss();
        }
    }
}
