package com.orange.lo.assetdemo.model;

import lombok.Data;

@Data
public class Subscription {

    private String topic;
    private int qos;
    private String lastMessage;
    private String clientHandle;

    public Subscription(String topic, int qos, String clientHandle){
        this.topic = topic;
        this.qos = qos;
        this.clientHandle = clientHandle;
    }

}
