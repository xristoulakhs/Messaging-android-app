package com.example.distributedsystemsapp.domain;

public interface Broker {

    void notifyBrokersOnRegister(String topic, String name);

    void init(String ip, int port);
}
