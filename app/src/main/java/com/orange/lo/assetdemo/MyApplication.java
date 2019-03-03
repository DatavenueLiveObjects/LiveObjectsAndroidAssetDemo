package com.orange.lo.assetdemo;

import android.app.Application;

import com.orange.lo.assetdemo.model.Asset;
import com.orange.lo.assetdemo.mqtt.Connection;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by ZLGP6287 on 13/10/2016.
 */

public class MyApplication extends Application {


    @Getter
    private static MyApplication instance;

    @Getter @Setter
    private Connection mqttConnection;

    @Getter
    private AppPreferences appPreferences;


    @Getter
    private Asset asset;

    public MyApplication() {
        super();

    }


    @Override
    public void onCreate() {
        super.onCreate();
        MyApplication.instance = this;
        this.appPreferences = AppPreferences.build(this);
        this.mqttConnection = Connection.createConnection(this);
        this.initAsset();

    }

    private void initAsset() {
        this.asset = new Asset(getApplicationContext(), appPreferences.getPhoneModel(), appPreferences.getAppVersion());
    }

    public void clearData() {
        appPreferences.clearPreferences();
        asset.resetAsset();
    }




}