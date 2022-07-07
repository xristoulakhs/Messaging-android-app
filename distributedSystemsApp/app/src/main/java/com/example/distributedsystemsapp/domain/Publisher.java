package com.example.distributedsystemsapp.domain;

import java.util.ArrayList;

public interface Publisher {

    public ArrayList<MultimediaFile> generateChunks(byte[] media, String typeOfMedia);

    void sendMessage(String topic, String message);

    void push(String topic, byte[] media, String typeOfMedia);
}
