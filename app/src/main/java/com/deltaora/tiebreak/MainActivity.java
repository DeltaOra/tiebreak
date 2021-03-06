package com.deltaora.tiebreak;

import android.os.Bundle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import zephyr.android.HxMBT.*;

public class MainActivity extends AppCompatActivity {
    BluetoothAdapter adapter = null;
    BTClient _bt;
    ConnectionListener _NConnListener;
    private final int HEART_RATE = 0x100;
    private final int INSTANT_SPEED = 0x101;
    GeoTracker gps;

    double latitude;
    double longitude;
    EditText elat;
    EditText elng;
    EditText epid;
    EditText ehb;
    int iConnect = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar); // Attaching the layout to the toolbar object
        setSupportActionBar(toolbar);
        elat = (EditText) findViewById(R.id.editLatitude);
        elng = (EditText) findViewById(R.id.editLongitude);
        epid = (EditText) findViewById(R.id.editPID);
        ehb = (EditText) findViewById(R.id.editHeartRate);

        ehb.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String slat = elat.getText().toString();
                String slot = elng.getText().toString();
                String spid = epid.getText().toString();

                String url = "http://192.168.100.121:8084/HeartMonitoringSystem/MobileHandler?lat=" + slat +
                        "&long=" + slot +
                        "&pr=" + ehb.getText().toString().trim() +
                        "&pid=" + spid;

                System.out.println(url);
                //String val=  rh.getTargetRate("http://192.168.100.17:8080/de-wander/dewander.html?lat="+et1.getText().toString().trim()+"&lon="+et2.getText().toString().trim()+"&sex="+str+"&level="+str1+"&age="+et3.getText().toString().trim()+"&sim="+str2,Startpage.this);
                RequestHandler rh = new RequestHandler();

                double iLat = Double.parseDouble(slat);

                System.out.println("ilat=" + iLat);
                System.out.println("iConnect=" + iConnect);
                if (iLat > 0 && iConnect == 1) {
                    //Toast.makeText(MainActivity.this, "call to server",Toast.LENGTH_SHORT).show();
                    rh.getTargetRate(url, MainActivity.this);

                }


            }
        });

        /*Sending a message to android that we are going to initiate a pairing request*/
        IntentFilter filter = new IntentFilter("android.bluetooth.device.action.PAIRING_REQUEST");
        /*Registering a new BTBroadcast receiver from the Main Activity context with pairing request event*/
        this.getApplicationContext().registerReceiver(new BTBroadcastReceiver(), filter);
        // Registering the BTBondReceiver in the application that the status of the receiver has changed to Paired
        IntentFilter filter2 = new IntentFilter("android.bluetooth.device.action.BOND_STATE_CHANGED");
        this.getApplicationContext().registerReceiver(new BTBondReceiver(), filter2);

        //Obtaining the handle to act on the CONNECT button
        TextView tv = (TextView) findViewById(R.id.editStatus);
        String ErrorText = "Not Connected to HxM ! !";
        tv.setText(ErrorText);

        Button btnConnect = (Button) findViewById(R.id.ButtonConnect);

        gps = new GeoTracker(MainActivity.this);


        // check if GPS enabled
        if (gps.canGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            elat.setText(String.valueOf(latitude));
            elng.setText(String.valueOf(longitude));


        }


        if (btnConnect != null) {
            btnConnect.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    String BhMacID = "00:07:80:9D:8A:E8";
                    //String BhMacID = "00:07:80:88:F6:BF";
                    adapter = BluetoothAdapter.getDefaultAdapter();

                    Set<BluetoothDevice> pairedDevices = adapter.getBondedDevices();

                    if (pairedDevices.size() > 0) {
                        for (BluetoothDevice device : pairedDevices) {
                            if (device.getName().startsWith("HXM")) {
                                BhMacID = device.getAddress();
                                break;

                            }
                        }


                    }

                    //BhMacID = btDevice.getAddress();
                    BluetoothDevice Device = adapter.getRemoteDevice(BhMacID);
                    String DeviceName = Device.getName();
                    _bt = new BTClient(adapter, BhMacID);
                    _NConnListener = new ConnectionListener(Newhandler, Newhandler);
                    _bt.addConnectedEventListener(_NConnListener);

                    TextView tv1 = (EditText) findViewById(R.id.editHeartRate);
                    tv1.setText("000");

                    tv1 = (EditText) findViewById(R.id.editInstantSpeed);
                    tv1.setText("0.0");

                    //tv1 = 	(EditText)findViewById(R.id.labelSkinTemp);
                    //tv1.setText("0.0");

                    //tv1 = 	(EditText)findViewById(R.id.labelPosture);
                    //tv1.setText("000");

                    //tv1 = 	(EditText)findViewById(R.id.labelPeakAcc);
                    //tv1.setText("0.0");
                    if (_bt.IsConnected()) {
                        _bt.start();
                        TextView tv = (TextView) findViewById(R.id.editStatus);
                        String ErrorText = "Connected to HxM " + DeviceName;
                        tv.setText(ErrorText);
                        iConnect = 1;
                        //Reset all the values to 0s

                    } else {
                        TextView tv = (TextView) findViewById(R.id.editStatus);
                        String ErrorText = "Unable to Connect !";
                        tv.setText(ErrorText);

                    }
                }
            });
        }
        /*Obtaining the handle to act on the DISCONNECT button*/
        Button btnDisconnect = (Button) findViewById(R.id.ButtonDisconnect);
        if (btnDisconnect != null) {
            btnDisconnect.setOnClickListener(new OnClickListener() {
                @Override
                /*Functionality to act if the button DISCONNECT is touched*/
                public void onClick(View v) {
                    /*Reset the global variables*/
                    TextView tv = (TextView) findViewById(R.id.editStatus);
                    String ErrorText = "Disconnected from HxM!";
                    tv.setText(ErrorText);

					/*This disconnects listener from acting on received messages*/
                    _bt.removeConnectedEventListener(_NConnListener);
					/*Close the communication with the device & throw an exception if failure*/
                    _bt.Close();

                }
            });
        }
    }

    private class BTBondReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle b = intent.getExtras();
            BluetoothDevice device = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
            Log.d("Bond state", "BOND_STATED = " + device.getBondState());
        }
    }

    private class BTBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BTIntent", intent.getAction());
            Bundle b = intent.getExtras();
            Log.d("BTIntent", b.get("android.bluetooth.device.extra.DEVICE").toString());
            Log.d("BTIntent", b.get("android.bluetooth.device.extra.PAIRING_VARIANT").toString());
            try {
                BluetoothDevice device = adapter.getRemoteDevice(b.get("android.bluetooth.device.extra.DEVICE").toString());
                Method m = BluetoothDevice.class.getMethod("convertPinToBytes", new Class[]{String.class});
                byte[] pin = (byte[]) m.invoke(device, "1234");
                m = device.getClass().getMethod("setPin", new Class[]{pin.getClass()});
                Object result = m.invoke(device, pin);
                Log.d("BTTest", result.toString());
            } catch (SecurityException | NoSuchMethodException | IllegalArgumentException
                    | IllegalAccessException | InvocationTargetException e1) {
                e1.printStackTrace();
            }
        }
    }


    final Handler Newhandler = new Handler() {
        public void handleMessage(Message msg) {
            TextView tv;
            switch (msg.what) {
                case HEART_RATE:
                    final String HeartRatetext = msg.getData().getString("HeartRate");
                    tv = (EditText) findViewById(R.id.editHeartRate);
                    System.out.println("Heart Rate Info is " + HeartRatetext);


                    if (tv != null) {
                        tv.setText(HeartRatetext);


                    }


                    break;

                case INSTANT_SPEED:
                    String InstantSpeedtext = msg.getData().getString("InstantSpeed");
                    tv = (EditText) findViewById(R.id.editInstantSpeed);
                    if (tv != null)
                        tv.setText(InstantSpeedtext);

                    break;

            }
        }

    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}



