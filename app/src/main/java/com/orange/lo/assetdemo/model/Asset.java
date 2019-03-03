package com.orange.lo.assetdemo.model;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.orange.lo.assetdemo.MyApplication;
import com.orange.lo.assetdemo.R;
import com.orange.lo.assetdemo.activity.ApplicationConstants;
import com.orange.lo.assetdemo.activity.Notify;
import com.orange.lo.assetdemo.AppPreferences;
import com.orange.lo.assetdemo.utils.RndUtils;

import java.beans.PropertyChangeEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by ZLGP6287 on 27/10/2016.
 */
public class Asset extends AbstractListenable {

    private static final String TAG = Asset.class.getName();

    public static final String ASSET_CONFIG_REFRESH = "updateRate";
    public static final String ASSET_CONFIG_LOG = "logLevel";
    public static final String ASSET_COMMAND_BUZZER = "buzzer";
    public static final String ASSET_COMMAND_OPEN_DOOR = "open door";
    public static final String ASSET_COMMAND_CLOSE_DOOR = "close door";
    public static final String ASSET_COMMAND_RESET = "reset";
    public static final String ASSET_RESOURCE_SPLASH_ID = "demo_splash_screen";

    /*

    Data simulation parameters : make coherent generated values

    */
    private static final int NUMBER_BEFORE_CHECK_REVMIN_THRESHOLD = 10;
    private static int countBeforeCheckRevminThreshold = NUMBER_BEFORE_CHECK_REVMIN_THRESHOLD;
    private static final int NUMBER_BEFORE_CHECK_PRESSURE_THRESHOLD = 10;
    private static int countBeforeCheckPressureThreshold = NUMBER_BEFORE_CHECK_PRESSURE_THRESHOLD;
    private static final int MAX_REVMIN = 10000;
    private static final int REVMIN_START = 5000;
    private static final int REVMIN_CHANGE_STEP = 500;
    private static int lastRevmin = REVMIN_START;
    private static final int CO2_START = 400;
    private static final int MAX_CO2 = 2000;
    private static final int CO2_CHANGE_STEP = 20;
    private static int lastCO2 = CO2_START;
    private static final int MAX_PRESSURE = 4000;
    private static final int MIN_PRESSURE = 500;
    private static final int PRESSURE_START = 1000;
    private static final int PRESSURE_CHANGE_STEP = 50;
    private static int lastPressure = PRESSURE_START;
    private static int lastTemperature = 0;
    private static int lastHygrometry = 0;
    private static boolean revminIncrease = true;
    private static boolean pressureIncrease = true;
    private static final int CHANGE_REVMIN_THRESHOLD_CHANCE = 2; // 1/x chance to switch from increase to decrease
    private static final int CHANGE_PRESSURE_THRESHOLD_CHANCE = 2; // 1/x chance to switch from increase to decrease

    private AppPreferences appPreferences =  MyApplication.getInstance().getAppPreferences();

    @Getter
    private DeviceDataTelemetry telemetry;

    private DeviceDataTelemetry telemetryOld;

    @Getter
    private Double[] location;

    private Double[] locationOld;

    @Getter
    private DeviceConfig config;

    @Getter
    @Setter
    private boolean telemetryModeAuto;

    @Getter
    private boolean locationModeAuto;

    @Getter
    private DeviceResources resources;


    private Context context;

    // GPS Track for simulating locations of the Asset
    private List<Double[]> gpsTrack = new ArrayList<>();

    private int gpsTrackCurrentIdx = 0;

    @Getter
    private DeviceStatus deviceStatus;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Asset.this.location = new Double[]{location.getLatitude(), location.getLongitude()};
            Asset.this.notifyDeviceLocationChange();
            Log.i(TAG, "New Location: " +  location.toString());
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d(TAG, "Location Status Changed: " + s);
        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };


    public Asset(Context context, String model, String version) {
        telemetry = new DeviceDataTelemetry();
        config = new DeviceConfig();
        location = new Double[]{0.0, 0.0};
        telemetryModeAuto = true;
        locationModeAuto = true;
        this.context = context;
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        this.loadGpsTrackSimulator();

        // Create the Current Device Status (Info)
        this.deviceStatus = new DeviceStatus(model, version);

        // Get the stored config of the device
        DeviceConfig.CfgParameter defaultCfgRate = new DeviceConfig.CfgParameter("i32", ApplicationConstants.DEFAULT_UPDATE_RATE);
        DeviceConfig.CfgParameter defaultCFGLog = new DeviceConfig.CfgParameter("str", ApplicationConstants.DEFAULT_LOG_LEVEL);

        this.loadConfigValue(ASSET_CONFIG_REFRESH, defaultCfgRate);
        this.loadConfigValue(ASSET_CONFIG_LOG, defaultCFGLog);

        //this.setConfigValue("logLevel", new DeviceConfig.CfgParameter("str", ApplicationConstants.DEFAULT_LOG_LEVEL) );
       //  this.loadConfigValue(ASSET_CONFIG_LOG, defaultCfgParameter);

        // TODO : Append Device Status in Device Configuration for now ==> Demo use it
    /*    try {
            this.setConfigValue("model", new DeviceConfig.CfgParameter("bin", Base64.encodeToString(model.getBytes("UTF-8"), Base64.NO_WRAP)));
            this.setConfigValue("version", new DeviceConfig.CfgParameter("bin", Base64.encodeToString(version.getBytes("UTF-8"), Base64.NO_WRAP)));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "CONVERSION ERROR: " + Log.getStackTraceString(e));
        }*/

        resources = new DeviceResources();

        try {

            // Init the first version of the Splash Screen
            boolean isSetup = loadResource(ASSET_RESOURCE_SPLASH_ID);
            if (!isSetup) {
                this.copyResource(ASSET_RESOURCE_SPLASH_ID, "v1.0", context.getAssets().open("lo_frame_eng.png"));
            }
            //FileInputStream fis =  getResource(ASSET_RESOURCE_SPLASH_ID);
        } catch (IOException e) {
            Log.e(TAG, "Unable to load resource file: " + Log.getStackTraceString(e));
            e.printStackTrace();
        }
    }

    public void resetAsset(){
        telemetry = new DeviceDataTelemetry();
        location = new Double[]{0.0, 0.0};
    }

    private void notifyDeviceDataChange() {
        notifyListeners((new PropertyChangeEvent(this, ApplicationConstants.TelemetryProperty, this.telemetryOld, this.telemetry)));
        this.telemetryOld = DeviceDataTelemetry.clone(this.telemetry);
        //notifyDeviceLocationChange();
    }

    private void notifyDeviceResourceChange(DeviceResourceVersion drv) {
        notifyListeners((new PropertyChangeEvent(this, ApplicationConstants.ResourceNewVersionProperty, null, drv)));
    }
    private void notifyDeviceLocationChange() {
        notifyListeners((new PropertyChangeEvent(this, ApplicationConstants.LocationProperty, this.locationOld, this.location)));
        this.locationOld = new Double[]{this.location[0], this.location[1]};
    }

    private void notifyDeviceConfigurationChange() {
        notifyListeners((new PropertyChangeEvent(this, ApplicationConstants.ConfigurationProperty, null, this.config)));
        //this.telemetryOld = DeviceDataTelemetry.clone(this.telemetry);
    }


    public DeviceData createDeviceData(DeviceDataTelemetry value, Double[] loc) {
        DeviceData data = new DeviceData();
        data.setStreamId(appPreferences.getStreamId());
        data.setValue(value);
        data.setLocation(loc);
        data.setModel(ApplicationConstants.PUBLISHED_MODEL);
        return data;

    }

    private void loadGpsTrackSimulator() {


//        try (InputStream is = context.getAssets().open("voiture1.csv")) {
          try (InputStream is = context.getAssets().open("Garden- Lille - 30 04.csv")) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            Double lat, lon;
            while ((line = reader.readLine()) != null) {
                String[] row = line.split(",");
                lat = Double.valueOf(row[1]);
                lon = Double.valueOf(row[2]);
                gpsTrack.add(new Double[]{lat, lon});
            }
        } catch (IOException ex) {
            // handle exception
        }

    }

    private Double[] getNextGpsFixSimulator() {
        this.location = gpsTrack.get(gpsTrackCurrentIdx);
        gpsTrackCurrentIdx++;
        if (gpsTrackCurrentIdx >= gpsTrack.size()) gpsTrackCurrentIdx = 0;
        return this.location;
    }


    // switch increase <=> decrease every NUMBER_BEFORE_CHECK_REVMIN_THRESHOLD
    private boolean setRevminIncrease(){
        if (countBeforeCheckRevminThreshold-- > 0)
            return revminIncrease;
        else{
            countBeforeCheckRevminThreshold = NUMBER_BEFORE_CHECK_REVMIN_THRESHOLD;
            return RndUtils.randInt(0,100)>(100/CHANGE_REVMIN_THRESHOLD_CHANCE);
        }
    }

    // switch increase <=> decrease every NUMBER_BEFORE_CHECK_PRESSURE_THRESHOLD
    private boolean setPressureIncrease(){
        if (countBeforeCheckPressureThreshold-- > 0)
        {
            return pressureIncrease;
        }
        else
        {
            countBeforeCheckPressureThreshold = NUMBER_BEFORE_CHECK_PRESSURE_THRESHOLD;
            return RndUtils.randInt(0,100)>(100/CHANGE_PRESSURE_THRESHOLD_CHANCE);
        }
    }

    /**
     *
     * very basic automaton which build coherent telemetry values in order to avoid anarchic random values
     *
     * revmin will progressively increase/decrease following the boolean revminIncrease value
     * hygrometry and temperature are related to revmin value
     *
     * pressure will progressively increase/decrease following the boolean pressureIncrease value
     * CO2 is related to pressure value
     *
     * doorOpen is managed by a command "door open" / "door close" or a GUI action
     *
     * @return
     */
    private DeviceDataTelemetry getNextTelemetrySimulator() {
/*
        this.telemetry.setHydrometry(RndUtils.randInt(0, 100));
        this.telemetry.setTemperature(RndUtils.randInt(ApplicationConstants.TEMPERATURE_MIN_PROGESS, ApplicationConstants.TEMPERATURE_MAX_PROGESS));
        this.telemetry.setRevmin(RndUtils.randInt(0, 10000));
*/


        revminIncrease = setRevminIncrease();
        // increase/decrease depending of revMinIncrease
        if (revminIncrease)
            lastRevmin += RndUtils.randInt(-REVMIN_CHANGE_STEP/2, REVMIN_CHANGE_STEP);
        else
            lastRevmin -= RndUtils.randInt(-REVMIN_CHANGE_STEP/2, REVMIN_CHANGE_STEP);
        if (lastRevmin > MAX_REVMIN) revminIncrease = false;
        if (lastRevmin < 0) revminIncrease = true;
        this.telemetry.setRevmin(Math.min(Math.max(lastRevmin, 0), MAX_REVMIN));

        lastHygrometry = Math.min(lastRevmin/100 + RndUtils.randInt(0, 10), 100);
        this.telemetry.setHydrometry(lastHygrometry);

        lastTemperature = (lastRevmin * ApplicationConstants.TEMPERATURE_MAX_PROGESS)/MAX_REVMIN - ApplicationConstants.TEMPERATURE_MIN_PROGESS;
        this.telemetry.setTemperature(lastTemperature);

        pressureIncrease = setPressureIncrease();
        if (pressureIncrease)
            lastPressure += RndUtils.randInt(-PRESSURE_CHANGE_STEP/2, PRESSURE_CHANGE_STEP);
        else
            lastPressure -= RndUtils.randInt(-PRESSURE_CHANGE_STEP/2, PRESSURE_CHANGE_STEP);
        if (lastPressure > MAX_PRESSURE) pressureIncrease = false;
        if (lastPressure < MIN_PRESSURE) pressureIncrease = true;
        this.telemetry.setPressure(Math.min(Math.max(MIN_PRESSURE,lastPressure), MAX_PRESSURE));

        lastCO2 = lastPressure * MAX_CO2/MAX_PRESSURE;
        this.telemetry.setCO2(Math.min(Math.max(0, lastCO2), MAX_CO2));

        return this.telemetry;
    }


    public void setLocationModeAuto(boolean locationModeAuto) {
        this.locationModeAuto = locationModeAuto;
        if (locationModeAuto) {
            stopRequestLocationUpdates();
        } else {
            startRequestLocationUpdates();
        }
    }

    private void startRequestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Notify.toast(context,  context.getString(R.string.no_location_permissions), Toast.LENGTH_LONG);
            return;
        }
        boolean atLeastOneProvider = false;
        if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            atLeastOneProvider = true;

            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    ApplicationConstants.MIN_TIME_BW_UPDATES,
                    ApplicationConstants.MIN_DISTANCE_CHANGE_FOR_UPDATES, mLocationListener);
        }

        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            atLeastOneProvider = true;

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    ApplicationConstants.MIN_TIME_BW_UPDATES,
                    ApplicationConstants.MIN_DISTANCE_CHANGE_FOR_UPDATES, mLocationListener);
        }
        if (!atLeastOneProvider) {
            Notify.toast(context,  context.getString(R.string.no_network_no_gps), Toast.LENGTH_LONG);
        }
    }


    private void stopRequestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Notify.toast(context,  context.getString(R.string.no_location_permissions), Toast.LENGTH_LONG);
            return;
        }
        mLocationManager.removeUpdates(mLocationListener);
    }

    public Double[] getNextLocation() {
        if (this.isLocationModeAuto()) {
            location = this.getNextGpsFixSimulator();
            notifyDeviceLocationChange();
        }
        return location;
    }

    /**
     * generate new telemetry values
     *
     * @return
     */
    public DeviceDataTelemetry getNextTelemetry() {
        if (this.isTelemetryModeAuto()) {
            this.telemetry = this.getNextTelemetrySimulator();
            notifyDeviceDataChange();
        }
        return telemetry;
    }

    /**
     * enable the open/close door from outside
     * @param bOpen
     */
    public void SetTelemetryOpenDoor(boolean bOpen){
        this.telemetry.setDoorOpen(bOpen);
    }

    /**
     * reset the telemetry values
     */
    public void ResetTelemetryValues(){
        this.telemetry.setDoorOpen(false);
        lastTemperature = 0;
        lastHygrometry = 0;
        lastRevmin = REVMIN_START;
        lastCO2 = CO2_START;
        lastPressure = PRESSURE_START;
        revminIncrease = true;
        pressureIncrease = true;
    }

    private DeviceConfig.CfgParameter loadConfigValue(String cfgKey, DeviceConfig.CfgParameter defaultValue) {
        DeviceConfig.CfgParameter value;
        Log.d(TAG, "LoadConfig , key : "+cfgKey+" value : "+defaultValue);
        if (!appPreferences.hasCfgParameter(cfgKey)) {
            // Set the default value
            appPreferences.setCfgParameter(cfgKey, defaultValue);
            value = defaultValue;
        }
        else {
            value = appPreferences.getCfgParameter(cfgKey, defaultValue);
        }
        config.current.put(cfgKey, value);

        return getConfigValue(cfgKey);
    }

    public DeviceConfig.CfgParameter getConfigValue(String cfgKey) {

        return config.current.get(cfgKey);

    }

    // Will be used when displaying this parameter in the GUI
    public void setConfigValue(String cfgKey, DeviceConfig.CfgParameter cfgParameter) {
        if("i32".equals(cfgParameter.getType().toLowerCase())) {
            Number number;
            number = (Number) cfgParameter.getValue();
            int test = number.intValue();
            DeviceConfig.CfgParameter cfgParameter1 = new DeviceConfig.CfgParameter("i32", test);
            config.current.put(cfgKey, cfgParameter1);
        }
        else{
                config.current.put(cfgKey, cfgParameter);
        }

        // Store the configuration in the app preferences
        appPreferences.setCfgParameter(cfgKey, cfgParameter);
        Log.d(TAG, " key : "+cfgKey+" param : "+cfgParameter);
        notifyDeviceConfigurationChange();
    }


    public void downloadResource(final String id) throws IOException {

        DeviceResourceVersion drv = resources.getRsc().get(id);
        URL url = new URL(drv.getUrl());
        final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        this.storeResourceLocalStorage(id, urlConnection.getInputStream());
        // Store the new version in the app prefs and Map in memory
        this.putResource(id, drv.getNewVersion());
        drv.setNewVersion(null);

    }

    private void copyResource(String id, String version, InputStream inputStream) throws IOException {
        this.putResource(id, version);
        this.storeResourceLocalStorage(id, inputStream);
    }

    public void putResourceNewVersion(String id, String newVersion, String url) throws IOException {
        if (id == null) return;
        DeviceResourceVersion drv = resources.getRsc().get(id);
        if (drv == null) return;
        drv.setNewVersion(newVersion);
        drv.setUrl(url);
        notifyDeviceResourceChange(drv);

    }
    private void putResource(String id, String version) throws IOException {

        if (id == null) return;
        if (version == null || TextUtils.isEmpty(version.trim())) return;

        DeviceResourceVersion drv = resources.getRsc().get(id);
        if (drv == null) {
            drv = new DeviceResourceVersion(version);
            resources.getRsc().put(id, drv);
        } else {
            drv.setVersion(version);
        }

        // Store the version in App Preferences
        appPreferences.setAssetResourceVersion(id, version);

        //storeResourceLocalStorage(inputStream);

    }

    private void storeResourceLocalStorage(String id, InputStream inputStream) throws IOException {
        String dataDir = context.getFilesDir().getAbsolutePath().concat(File.separator);

        // Check if sub directory exists
        File dir = new File(dataDir.concat(getAssetResourcesDirectory()));
        if (!dir.isDirectory()) dir.mkdir();

        String filenamePath = dataDir.concat(getAssetResourcesDirectory().concat(id));
        File file = new File(filenamePath);

        try (FileOutputStream outputStream = new FileOutputStream (file, false)) {

            int read;
            byte[] bytes = new byte[1024];

            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }
    }

    private boolean loadResource(String id) throws IOException {
        String version = appPreferences.getAssetResourceVersion(id);
        if(version == null) return false;
        resources.getRsc().put(id, new DeviceResourceVersion(version));

        return true;
    }

    public File getResource(String id) throws IOException {
        if (id == null) return null;

        String dataDir = context.getFilesDir().getAbsolutePath().concat(File.separator);
        String filenamePath = dataDir.concat(getAssetResourcesDirectory()).concat(id);

        return new File(filenamePath);

//        FileInputStream fis = new FileInputStream (file);
//
//        //BitmapFactory.decodeStream(fis);
//        return fis;

    }


//    private String buildResourceFileName(String id) {
//        return getAssetResourcesDirectory().concat(File.separator).concat(id);
//    }
    private String getAssetResourcesDirectory() {
        return "asset_resources".concat(File.separator);

    }
}
