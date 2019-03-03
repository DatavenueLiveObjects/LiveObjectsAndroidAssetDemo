/**
 *
 */

package com.orange.lo.assetdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;

import com.orange.lo.assetdemo.activity.ApplicationConstants;
import com.orange.lo.assetdemo.model.DeviceConfig;
import com.orange.lo.assetdemo.mqtt.ServerSettings;
import com.orange.lo.assetdemo.mqtt.ServerSettingsList;

import lombok.Getter;

/**
 * Created by ZLGP6287 on 24/10/2016.
 */

public class AppPreferences {
    private static final String API_KEY = "apiKey";
    //public static final String ROUTER_MODE = "routerMode";
    private static final String AUTO_RECONNECT = "autoReconnect";
    private static final String MQTT_SERVER = "mqttServer";
    private static final String MQTT_PROTOCOL = "mqttProtocol";
    private static String ASSET_RESOURCE_PREFIX = "assetResource.";

    private static String ASSET_NAMESPACE = "android";

    //@Getter
    //private static AppPreferences instance;
    @SuppressLint("HardwareIds")
    public static AppPreferences build(Context context) {
        AppPreferences appPreferences = new AppPreferences(context);
        appPreferences.context = context;

        return appPreferences;
    }

    public void initAssetId() {
        String imei;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            imei = ApplicationConstants.DEFAULT_DEVICE_NAME;
        } else {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            imei = telephonyManager.getDeviceId();
            if ("000000000000000".equals(imei)) {
                imei = ApplicationConstants.DEFAULT_DEVICE_NAME;
            }
        }
        this.assetId = imei + BuildConfig.ASSET_ID_SUFFIX;
    }
    private Context context;

    @Getter
    private String assetId;


    private AppPreferences(Context context) {
        this.context = context;
    }

    public String getUsernameDeviceMode (){
        return "json+device";
    }

    public String getUsernameBridgeMode (){
        return "json+device";
    }

    public String getStreamId() {
        return ASSET_NAMESPACE +  this.assetId;
    }

    public String getShortClientId(){

        return ASSET_NAMESPACE +  ":" + this.assetId;
    }

    public String getClientId(){

        return "urn:lo:nsid:" + getShortClientId();
    }


    public String getPhoneModel() {
        return Build.MODEL;
    }

    public String getAppVersion() {
        try {
            return context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    private SharedPreferences getPreferences() {
        //return getApplicationContext().getSharedPreferences("", Context.MODE_PRIVATE );
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setAutoReconnectStatus (Boolean bool){
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putBoolean(AUTO_RECONNECT, bool);
        editor.apply();
    }

    public Boolean getAutoReconnectStatus(){
        return getPreferences().getBoolean(AUTO_RECONNECT, true);
    }

    public String getApiKey(){
        return getPreferences().getString(API_KEY, "").trim();
    }

    public void setApiKey(String apiKey) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putString(API_KEY, apiKey);
        editor.apply();
    }

    public void clearPreferences() {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.clear();
        editor.apply();
    }


    public ServerSettings getMqttServerSettings() {
        ServerSettingsList.MQTTConnectionType connectionType =
                ServerSettingsList.MQTTConnectionType.fromValue(getPreferences().getInt(MQTT_PROTOCOL, 0));

        return ServerSettingsList.instance.getServerSettings(connectionType);
    }

//    public Integer getMqttServerKey() {
//        return getPreferences().getInt(MQTT_SERVER, 0);
//    }

    public void setMqttServerKey(Integer mqttServerKey) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putInt(MQTT_SERVER, mqttServerKey);
        editor.apply();
    }

    public Integer getMqttProtocolKey() {
        return getPreferences().getInt(MQTT_PROTOCOL, 1);
    }

    public void setMqttProtocolKey(Integer mqttProtocolKey) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putInt(MQTT_PROTOCOL, mqttProtocolKey);
        editor.apply();
    }

    public void setCfgParameter(String cfgKey, DeviceConfig.CfgParameter cfgParameter) {
        SharedPreferences.Editor editor = getPreferences().edit();
        Number number;
        switch (cfgParameter.getType().toLowerCase()) {
            case "bin":
                editor.putString(cfgKey, (String)cfgParameter.getValue());
                break;
            case "str":
                editor.putString(cfgKey, (String)cfgParameter.getValue());
                break;
            case "i32":
                number = (Number)cfgParameter.getValue();
                editor.putInt(cfgKey, number.intValue());
                break;
            case "u32":
                number = (Number)cfgParameter.getValue();
                editor.putLong(cfgKey, number.longValue());
                break;
            case "f64":
                number = (Number)cfgParameter.getValue();
                editor.putFloat(cfgKey, number.floatValue());
                break;
        }
        editor.putString(cfgKey + "_type", cfgParameter.getType());
        editor.apply();
    }

    public boolean hasCfgParameter(String cfgKey) {
        String type = getPreferences().getString(cfgKey + "_type", null);
        return type != null;

    }
    public DeviceConfig.CfgParameter getCfgParameter(String cfgKey, DeviceConfig.CfgParameter defaultCfgParam) {

        String type = getPreferences().getString(cfgKey + "_type", null);

        Object value = null;

        if (type != null) {

            Object defaultValue = defaultCfgParam==null?null:defaultCfgParam.getValue();
            switch (type.toLowerCase()) {
                case "str":
                case "bin":
                    //case "raw":
                    value = defaultValue == null? null: getPreferences().getString(cfgKey, (String) defaultValue);
                    break;
                case "i32":
                    value = defaultValue == null? null: getPreferences().getInt(cfgKey, (Integer) defaultValue);
                    break;
                case "u32":
                    value = defaultValue == null? null: getPreferences().getLong(cfgKey, (Long) defaultValue);
                    break;
                case "f64":
                    value = defaultValue == null? null: getPreferences().getFloat(cfgKey, (Float) defaultValue);
                    break;
            }
        }
        if (value == null) {
            return defaultCfgParam;
        }

        return new DeviceConfig.CfgParameter(type, value);
    }

    public void setAssetResourceVersion(String id, String version) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putString(ASSET_RESOURCE_PREFIX + id, version);
        editor.apply();
    }

    public String getAssetResourceVersion(String id) {
        return getPreferences().getString(ASSET_RESOURCE_PREFIX + id, null);
    }

}
