package com.example.distributedsystemsapp.domain;

import android.util.Log;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.lang.ClassNotFoundException;

public class PublisherImp extends UserNode implements Publisher, Serializable {

    private ProfileName profileName;
    private UserNode userNode;

    PublisherImp(){}
    public PublisherImp(UserNode userNode, ProfileName profileName){
        this.profileName=profileName;
        this.userNode = userNode;
    }

    public ProfileName getProfileName() {
        return profileName;
    }

    public void setProfileName(ProfileName profileName) {
        this.profileName = profileName;
    }

    @Override
    public ArrayList<MultimediaFile> generateChunks(byte[] media, String typeOfMedia){

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = timeStamp + typeOfMedia;
        List<byte[]> listOfChunks = Util.splitFileToChunks(media, 1024*16);
        int numOfChunks = listOfChunks.size();
        ArrayList<MultimediaFile> listOfMultimediaFiles = new ArrayList<>();

        for (int i = 0; i < numOfChunks; i++){
            byte[] tempArr = listOfChunks.get(i);
            MultimediaFile tempFile = new MultimediaFile(fileName, getProfileName().getProfileName(), tempArr.length, tempArr);
            listOfMultimediaFiles.add(tempFile);
        }
        return listOfMultimediaFiles;
    }


    @Override
    public void sendMessage(String topic, String message){
        try {
            out.writeUTF("publisher");
            out.flush();

            out.writeUTF("sendMessage");
            out.flush();

            Message messageToSend = new Message(message, this.profileName);

            out.writeUTF(topic);
            out.flush();

            out.writeObject(messageToSend);
            out.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void push(String topic, byte[] media, String typeOfMedia) {
        ArrayList<MultimediaFile> chunks = this.generateChunks(media, typeOfMedia);
        int numOfChunks = chunks.size();
        try {
            out.writeUTF("publisher");
            out.flush();

            Log.d("pushinnggg", "push: publisher");

            out.writeUTF("multimediaFile");
            out.flush();

            Log.d("pushinnggg", "push: action");
            out.writeUTF(topic);
            out.flush();

            Log.d("pushinnggg", "push: topic");
            out.writeObject(getProfileName());
            out.flush();

            Log.d("pushinnggg", "push: name");
            out.writeInt(numOfChunks);
            out.flush();

            Log.d("pushinnggg", "push: numofchunks");
            for (int i = 0; i < numOfChunks; i++) {
                out.writeObject(chunks.get(i));
                out.flush();
            }
            Log.d("pushinnggg", "push: all");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}