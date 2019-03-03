package com.orange.lo.assetdemo.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;
import com.orange.lo.assetdemo.MyApplication;
import com.orange.lo.assetdemo.R;
import com.orange.lo.assetdemo.AppPreferences;
import com.orange.lo.assetdemo.model.Asset;
import com.orange.lo.assetdemo.model.DeviceCommand;
import com.orange.lo.assetdemo.model.DeviceCommandResponse;
import com.orange.lo.assetdemo.model.DeviceConfig;
import com.orange.lo.assetdemo.model.DeviceData;
import com.orange.lo.assetdemo.model.DeviceDataTelemetry;
import com.orange.lo.assetdemo.model.DeviceResourceUpdate;
import com.orange.lo.assetdemo.model.DeviceResourceUpdateResponse;
import com.orange.lo.assetdemo.mqtt.Connection;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;


public class MainActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener,
        SimulateFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener,
        ResourcesFragment.OnFragmentInteractionListener {

    private static final String TAG = MainActivity.class.getName();
    public static final int REQUEST_MAIN_PERMISSIONS = 1;
    public static final int PERMISSION_REQUEST_CAMERA = 2;

    private Connection mConnection = null;
    private AppPreferences mAppPreferences = MyApplication.getInstance().getAppPreferences();
    private Asset mAsset = MyApplication.getInstance().getAsset();

    protected MqttMessageListener mqttMessageListener;

    private ScheduledExecutorService mScheduler;

    private Gson gson = new Gson();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Force to create the Settings Fragment when started
        BottomNavigationView navigationView = (BottomNavigationView) findViewById(R.id.nav_view);
        navigationView.setOnNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.getMenu().performIdentifierAction(R.id.action_settings, 0);

        mqttMessageListener = new MqttMessageListener();

        List<String> permissions = new ArrayList<>();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission_group.LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission_group.STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissions.size() > 0) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[permissions.size()]), REQUEST_MAIN_PERMISSIONS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        forceDisconnect();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_MAIN_PERMISSIONS: {
                boolean allGranted = true;
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults)  {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            allGranted = false; break;
                        }
                    }
                }
                else {
                    allGranted = false;
                }

                if (!allGranted) {

                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle(getString(R.string.alert));
                    alertDialog.setMessage(getString(R.string.no_permission_error));
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    MainActivity.this.finishAffinity();
                                }
                            });
                    alertDialog.show();
                    return;
                }

                mAppPreferences.initAssetId();

                return;
            }
            case PERMISSION_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length == 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    settingsFragment.runQrScan();

                } else {

                    // permission denied, boo! Disable the functionality
                }
                return;
            }

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button_bg, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    protected SettingsFragment settingsFragment = null;
    protected SimulateFragment simulateFragment = null;
    protected ResourcesFragment resourcesFragment= null;

    @Override
    protected void onResume() {
        super.onResume();

        BottomNavigationView navigationView = (BottomNavigationView) findViewById(R.id.nav_view);
        navigationView.setOnNavigationItemSelectedListener(this);

        this.mConnection = MyApplication.getInstance().getMqttConnection();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment;
        switch (item.getItemId()) {
            case R.id.action_settings:
                if (settingsFragment == null)  settingsFragment = SettingsFragment.newInstance();
                fragment = settingsFragment;
                break;
            case R.id.action_simulate:
                if (simulateFragment == null)  simulateFragment = SimulateFragment.newInstance();
                fragment = simulateFragment;
                break;
/* RESOURCE*/
            case R.id.action_resources:
                if (resourcesFragment == null)  resourcesFragment = ResourcesFragment.newInstance();
                fragment = resourcesFragment;
                break;
            default:
                return false;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();

        return true;
    }


    @Override
    public void mqttConnectOrDisconnect() {
        try {
            if (mConnection.isConnected()) {
                handleDisconnection();
            }
            else {
                if (!setupMqttClient()) return;
                mConnection.changeConnectionStatus(Connection.ConnectionStatus.CONNECTING);

                mConnection.connect( new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        MainActivity.this.handleOnConnectSuccess();
                        Notify.toast(getApplicationContext(),getString(R.string.client_connected),Toast.LENGTH_SHORT);
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        mConnection.changeConnectionStatus(Connection.ConnectionStatus.ERROR);
                        String error = getApplicationContext().getString(R.string.connection_error);
                        Log.e(TAG, "Connection Fail: " + Log.getStackTraceString(exception));
                        Notify.toast(getApplicationContext(), error + ": " + exception.getMessage(), Toast.LENGTH_LONG);
                    }
                });
            }
        } catch (MqttException e) {
            Log.d(TAG, "Error on login / logout : " + Log.getStackTraceString(e));
            e.printStackTrace();
        }
    }

    private boolean setupMqttClient() {

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(mAppPreferences.getUsernameDeviceMode());
        options.setPassword(mAppPreferences.getApiKey().toCharArray());

        options.setKeepAliveInterval(ApplicationConstants.MQTT_KEEP_ALIVE_INTERVAL);
        options.setConnectionTimeout(ApplicationConstants.MQTT_CONNECTION_TIMEOUT);
        options.setAutomaticReconnect(true);

        if (mAppPreferences.getMqttServerSettings().getTlsConnection()) {
            try {
                String caCertFile = mAppPreferences.getMqttServerSettings().getCaCertFile();
                SSLSocketFactory sslSocketFactory = mConnection.createSSLSocketFactory(caCertFile);
                options.setSocketFactory(sslSocketFactory);
            } catch (IOException | MqttSecurityException exception) {
                mConnection.changeConnectionStatus(Connection.ConnectionStatus.ERROR);
                String error = getApplicationContext().getString(R.string.connection_error);
                Log.e(TAG,"SSL Socket creation error " + Log.getStackTraceString(exception));
                Notify.toast(getApplicationContext(), error + ": " + exception.getMessage(), Toast.LENGTH_LONG);
                return false;
            }

        }
        mConnection.setupConnection(mAppPreferences.getClientId(),
                mAppPreferences.getMqttServerSettings(), options);

        return true;

    }

    private void handleOnConnectSuccess() {
        Log.i(TAG, "Connection Success");
        mConnection.changeConnectionStatus(Connection.ConnectionStatus.CONNECTED);

        // Publish the current device Status
        mConnection.publish(ApplicationConstants.MQTT_TOPIC_PUBLISH_STATUS, mAsset.getDeviceStatus());

        // Publish the current device Settings
        mConnection.publish(ApplicationConstants.MQTT_TOPIC_PUBLISH_CONFIG, mAsset.getConfig());

        // Publish the current resources Settings
        mConnection.publish(ApplicationConstants.MQTT_TOPIC_PUBLISH_RESOURCE, mAsset.getResources());

        mConnection.setMqttCallback(mqttMessageListener);

        // Subscribe to TOPICS for Config, Command and Resource
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mConnection.subscribe(ApplicationConstants.MQTT_TOPIC_SUBSCRIBE_CONFIG);
            }
        }, 1000);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mConnection.subscribe(ApplicationConstants.MQTT_TOPIC_SUBSCRIBE_COMMAND);
            }
        }, 2000);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mConnection.subscribe(ApplicationConstants.MQTT_TOPIC_SUBSCRIBE_RESOURCE);
            }
        }, 3000);

        this.createScheduler();

    }

    private void handleDisconnection() throws MqttException {
        // Subscribe to TOPICS for Config, Command and Resource
        mConnection.unsubscribe(ApplicationConstants.MQTT_TOPIC_SUBSCRIBE_CONFIG);
        mConnection.unsubscribe(ApplicationConstants.MQTT_TOPIC_SUBSCRIBE_COMMAND);
        mConnection.unsubscribe(ApplicationConstants.MQTT_TOPIC_SUBSCRIBE_RESOURCE);

        mConnection.changeConnectionStatus(Connection.ConnectionStatus.DISCONNECTING);
        if (mScheduler != null) mScheduler.shutdown();
        mScheduler = null;
        mConnection.disconnect(2000, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.i(TAG, "Disconnection Success");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.i(TAG, "Disconnection Fail: " + Log.getStackTraceString(exception));
            }
        });
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mConnection.changeConnectionStatus(Connection.ConnectionStatus.DISCONNECTED);
            }
        }, 2000);

    }
    private void createScheduler() {
        int interval =((Number) mAsset.getConfigValue(Asset.ASSET_CONFIG_REFRESH).getValue()).intValue();

        mScheduler = Executors.newSingleThreadScheduledExecutor();
        mScheduler.scheduleAtFixedRate
                (new Runnable() {
                     public void run() {
                         DeviceDataTelemetry telemetry = mAsset.getNextTelemetry();
                         Double[] loc = mAsset.getNextLocation();

                         DeviceData data = mAsset.createDeviceData(telemetry, loc);

                         mConnection.publish(ApplicationConstants.MQTT_TOPIC_PUBLISH_DATA, data);
                     }
                 },
                        0,
                        interval,
                        TimeUnit.SECONDS);

    }


    @Override
    public void forceDisconnect() {
        if (mConnection.isConnected()) {
            try {
                handleDisconnection();
            } catch (MqttException e) {
                Log.e(TAG, "Discconnection error: " + Log.getStackTraceString(e));
            }
        }
    }

    @Override
    public void onPublishResourcesNewVersion() {
        // Publish the current resources Settings
        mConnection.publish(ApplicationConstants.MQTT_TOPIC_PUBLISH_RESOURCE, mAsset.getResources());

    }

    public class MqttMessageListener implements MqttCallbackExtended {
        @Override
        public void connectionLost(Throwable cause) {
            Log.w(TAG, "Connection lost");
            mConnection.changeConnectionStatus(Connection.ConnectionStatus.DISCONNECTED);
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {

            Log.d(TAG, "Subscption New Message - Topic = " + topic + " , Message = " + message.toString());

            switch (topic) {
                case ApplicationConstants.MQTT_TOPIC_SUBSCRIBE_CONFIG:
                    DeviceConfig dc = gson.fromJson(message.toString(), DeviceConfig.class);

                    for (Map.Entry<String, DeviceConfig.CfgParameter> entry : dc.current.entrySet())
                    {
                        mAsset.setConfigValue(entry.getKey(), entry.getValue());
                    }

                    // Just copy out the input message to ACK the new configuration
                    mConnection.publishJson(ApplicationConstants.MQTT_TOPIC_RESPONSE_CONFIG, message.toString());
                    try {
                        if (mScheduler != null) mScheduler.shutdown();
                        createScheduler();
                    }
                    catch (Exception e) {
                        Log.e(TAG, "Problem on Scheduler Reset" + Log.getStackTraceString(e));
                    }
                    break;
                case ApplicationConstants.MQTT_TOPIC_SUBSCRIBE_COMMAND:
                    DeviceCommand deviceCommand = gson.fromJson(message.toString(), DeviceCommand.class);
                    if (deviceCommand.getReq().toLowerCase().equals(Asset.ASSET_COMMAND_BUZZER)) {
        /* RINGTONE
                        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);
                        ringtone.play();
        */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Notify.toast(getApplicationContext(), getString(R.string.asset_command_buzzer_received), Toast.LENGTH_LONG);
                            }
                        });
                    }
                    if (deviceCommand.getReq().toLowerCase().equals(Asset.ASSET_COMMAND_OPEN_DOOR)) {
                        mAsset.SetTelemetryOpenDoor(true);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Notify.toast(getApplicationContext(), getString(R.string.asset_command_open_door_received), Toast.LENGTH_LONG);
                            }
                        });
                    }
                    if (deviceCommand.getReq().toLowerCase().equals(Asset.ASSET_COMMAND_CLOSE_DOOR)) {
                        mAsset.SetTelemetryOpenDoor(false);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Notify.toast(getApplicationContext(), getString(R.string.asset_command_close_door_received), Toast.LENGTH_LONG);
                            }
                        });
                    }
                    if (deviceCommand.getReq().toLowerCase().equals(Asset.ASSET_COMMAND_RESET)) {
                        mAsset.ResetTelemetryValues();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Notify.toast(getApplicationContext(), getString(R.string.asset_command_reset_received), Toast.LENGTH_LONG);
                            }
                        });
                    }
                    // Just copy out the input message to ACK the new configuration
                    DeviceCommandResponse<Object> dcr = new DeviceCommandResponse<>(deviceCommand.getArg(), deviceCommand.getCid());
                    mConnection.publish(ApplicationConstants.MQTT_TOPIC_RESPONSE_COMMAND, dcr);
                    break;
                case ApplicationConstants.MQTT_TOPIC_SUBSCRIBE_RESOURCE:
                    DeviceResourceUpdate update = gson.fromJson(message.toString(), DeviceResourceUpdate.class);
                    DeviceResourceUpdateResponse response = new DeviceResourceUpdateResponse(DeviceResourceUpdateResponse.ResponseStatus.OK, update.getCid());

                    if (update.getId().equals(Asset.ASSET_RESOURCE_SPLASH_ID)) {
                        // Store the Resource
                        mAsset.putResourceNewVersion(update.getId(), update.getNewVersion(), update.getModel().getUri());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Notify.toast(getApplicationContext(), getString(R.string.asset_new_resource_available), Toast.LENGTH_LONG);
                            }
                        });
                    }
                    mConnection.publish(ApplicationConstants.MQTT_TOPIC_RESPONSE_RESOURCE, response);
                    break;

            }

        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.i(TAG, "Delivery complete");
        }

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            mConnection.changeConnectionStatus(Connection.ConnectionStatus.CONNECTED);
            Log.i(TAG, "Connect complete");
        }
    }



}
