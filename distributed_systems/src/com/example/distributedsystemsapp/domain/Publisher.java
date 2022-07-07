package com.example.distributedsystemsapp.domain;

import java.util.ArrayList;

public interface Publisher {

    public ArrayList<MultimediaFile> generateChunks(String file);

    void sendMessage(String topic, String message);

    public void push(String str, String nameOfFile);

}
