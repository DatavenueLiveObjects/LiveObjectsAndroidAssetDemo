package com.orange.lo.assetdemo.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by existenz25 on 10/11/2016.
 */

public class SSLUtils {

    public static TrustManager[] getTrustManagers(InputStream caCertStream)
            throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        java.security.cert.Certificate ca = cf.generateCertificate(new BufferedInputStream(caCertStream));

        String keyStoreType = KeyStore.getDefaultType();
        KeyStore ksTrust = KeyStore.getInstance(keyStoreType);
        ksTrust.load(null, null);
        ksTrust.setCertificateEntry("ca", ca);

        //TrustManagerFactory tmf = TrustManagerFactory.getInstance("X509");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(ksTrust);

        return tmf.getTrustManagers();
    }

    public static KeyManager[] getKeyManagers(InputStream clientBksStream, String clientJksPassword, String clientKeyPairPassword)
            throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableKeyException {

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        // Process the client key & CA
        KeyStore ksClient = KeyStore.getInstance("BKS"); // Android only Support BKS (not JKS)
        ksClient.load(new BufferedInputStream(clientBksStream), clientJksPassword.toCharArray());
        kmf.init(ksClient, clientKeyPairPassword.toCharArray());

        return kmf.getKeyManagers();

    }
}