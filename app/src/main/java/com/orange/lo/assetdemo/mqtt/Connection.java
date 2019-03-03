package com.orange.lo.assetdemo.mqtt;

import android.content.Context;
import android.net.SSLCertificateSocketFactory;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.orange.lo.assetdemo.BuildConfig;
import com.orange.lo.assetdemo.R;
import com.orange.lo.assetdemo.activity.ApplicationConstants;
import com.orange.lo.assetdemo.activity.Notify;
import com.orange.lo.assetdemo.model.AbstractListenable;
import com.orange.lo.assetdemo.utils.SSLUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import lombok.Getter;

/**
 * Created by ZLGP6287 on 25/10/2016.
 */

public class Connection extends AbstractListenable {
    private static final String TAG = Connection.class.getName();


    //@Getter
    private MqttAndroidClient client = null;

    @Getter
    private MqttConnectOptions connectOptions;

    //@Setter
    @Getter
    private ConnectionStatus status;

    /** The {@link Context} of the application this object is part of**/
    private final Context context;

    private Connection(Context context) {
        this.context = context;
        this.status = ConnectionStatus.DISCONNECTED;

    }

    public void connect(IMqttActionListener listener) throws MqttException {
        Log.i(TAG, "Connecting to " + client.getServerURI());
        this.client.connect(this.connectOptions, this.context, listener);
    }

    public void disconnect(long quiesceTimeout,IMqttActionListener listener ) throws MqttException {
        this.client.disconnect(quiesceTimeout, this.context, listener );
    }

    public void publish(String topic, Object obj) {
        Gson gson = new Gson();
        String json = gson.toJson(obj);
        publishJson(topic, json);
    }

    public void publishJson(String topic, String json) {
        MqttMessage msg;
        try {
            msg = new MqttMessage(json.getBytes("UTF-8"));
            Log.d(TAG, "Topic : "+topic+" - Message : "+msg);
            client.publish(topic, msg);
        } catch (UnsupportedEncodingException | MqttException e) {
            Log.e(TAG, "PUBLISH " + Log.getStackTraceString(e));
            Notify.toast(context, context.getString(R.string.publish_error) + e.getMessage(), Toast.LENGTH_SHORT);
        }
    }

    public void setMqttCallback(MqttCallbackExtended callback) {
        client.setCallback(callback);
    }

    public void subscribe(String topic) {
        try {
            client.subscribe(topic, 0);
            Log.d(TAG, "CONNECTION : subscribe to "+topic );
       } catch (MqttException e) {
            Log.e(TAG, "SUBSCRIBE " + Log.getStackTraceString(e));
            Notify.toast(context, context.getString(R.string.subscribe_error) + topic + " -> " + e.getMessage(), Toast.LENGTH_SHORT);

        }
    }

    public void unsubscribe(String topic) {
        try {
            client.unsubscribe(topic);
            Log.d(TAG, "CONNECTION : unsubscribe to "+topic );
        } catch (MqttException e) {
            Log.e(TAG, "SUBSCRIBE "+  Log.getStackTraceString(e));
            Notify.toast(context, context.getString(R.string.unsubscribe_error) + topic + " -> " + e.getMessage(), Toast.LENGTH_SHORT);
        }
    }

    /**
     * Connections status for  a connection
     */
    public enum ConnectionStatus {

        /** Client is Connecting **/
        CONNECTING,
        /** Client is Connected **/
        CONNECTED,
        /** Client is Disconnecting **/
        DISCONNECTING,
        /** Client is Disconnected **/
        DISCONNECTED,
        /** Client has encountered an Error **/
        ERROR,
        /** Status is unknown **/
        NONE
    }

    /**
     * Determines if the client is connected
     * @return is the client connected
     */
    public boolean isConnected() {
        return status == ConnectionStatus.CONNECTED;
    }

    /**
     * Changes the connection status of the client
     * @param connectionStatus The connection status of this connection
     */
    public void changeConnectionStatus(ConnectionStatus connectionStatus) {
        ConnectionStatus connectionStatusOld = status;
        status = connectionStatus;
        notifyListeners((new PropertyChangeEvent(this, ApplicationConstants.ConnectionStatusProperty, connectionStatusOld, status)));
    }

    //@Getter
    //private static Connection instance;

    public static Connection createConnection(Context context){
        return new Connection(context);
    }

    public Connection setupConnection(String clientId, ServerSettings serverSettings, MqttConnectOptions connectOptions){

        if (this.client != null) {
            this.client.unregisterResources();
            //this.client.close(); // close the previous client
        }

        this.client = new MqttAndroidClient(this.context, serverSettings.getURI(), clientId);
        this.connectOptions = connectOptions;

        return this;
    }


    public SSLSocketFactory createSSLSocketFactory(String caCertFile) throws IOException, MqttSecurityException {
        InputStream caCertStream = context.getAssets().open(caCertFile);

        SSLSocketFactory factory = null;
        if (BuildConfig.FLAVOR.equals("localProxy")) {
            factory = getSSLSocketFactoryClientAuth(caCertStream);
        }
        else {
            factory = getSSLSocketFactory(caCertStream);
        }
        return factory;
    }

    private SSLCertificateSocketFactory getSSLSocketFactory (InputStream caCertStream) throws MqttSecurityException {
        try{

//            SSLContext ctx;
//            SSLSocketFactory sslSockFactory;
//
//            TrustManager[] tm = SSLUtils.getTrustManagers(caCertStream);
//
//
//            ctx = SSLContext.getInstance("TLS");
//            ctx.init(null, tm, null);
//
//            sslSockFactory = ctx.getSocketFactory();

            SSLCertificateSocketFactory sslSockFactory = (SSLCertificateSocketFactory) SSLCertificateSocketFactory.getDefault(0);
            TrustManager[] tms = SSLUtils.getTrustManagers(caCertStream);
            sslSockFactory.setTrustManagers(tms);

            return sslSockFactory;

        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new MqttSecurityException(e);
        }
    }

    private SSLSocketFactory getSSLSocketFactoryClientAuth (InputStream caCertStream) throws MqttSecurityException {
        try{

            SSLCertificateSocketFactory sslSockFactory = getSSLSocketFactory(caCertStream);

            KeyManager[] kms = SSLUtils.getKeyManagers(context.getAssets().open("client_self_signed.bks"), "demodemo", "clicli");
            sslSockFactory.setKeyManagers(kms);
            return sslSockFactory;

        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new MqttSecurityException(e);
        }
    }


}
