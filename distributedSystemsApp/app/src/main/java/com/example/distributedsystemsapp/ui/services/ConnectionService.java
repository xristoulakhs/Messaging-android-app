package com.example.distributedsystemsapp.ui.services;

import android.app.Application;
import android.content.res.AssetFileDescriptor;
import android.os.StrictMode;
import android.util.Log;

import com.example.distributedsystemsapp.domain.Consumer;
import com.example.distributedsystemsapp.domain.ConsumerImp;
import com.example.distributedsystemsapp.domain.Message;
import com.example.distributedsystemsapp.domain.MultimediaFile;
import com.example.distributedsystemsapp.domain.ProfileName;
import com.example.distributedsystemsapp.domain.Publisher;
import com.example.distributedsystemsapp.domain.PublisherImp;
import com.example.distributedsystemsapp.domain.UserNode;
import com.example.distributedsystemsapp.domain.Util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ConnectionService extends Application {

    private final String SERVICES = "connectionService";
    private final String READCONV = "readconversationsfromfiles";

    UserNode userNode;
    String name;
    Consumer consumer;
    Publisher publisher;



    public void connect(){
        StrictMode.ThreadPolicy threadPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(threadPolicy);
        userNode = new UserNode("192.168.56.1",5001);
        userNode.setConversation(initConversations(this.name));
        userNode.init();

        userNode.communicateWithBroker(this.name);
        consumer = new ConsumerImp(userNode, new ProfileName(name));
        publisher = new PublisherImp(userNode, new ProfileName(name));
    }

    public void init(){
    }

    public ArrayList<String> getTopicOfUser(){
        ArrayList<String> arrayList = new ArrayList<>();

        HashMap<String, Queue<Message>> conversations = userNode.getConversation();

        for(String topic: conversations.keySet()){
            arrayList.add(topic);
        }

        return arrayList;
    }

    public Queue<Message> getConversation(String topic){
        return userNode.getConversation().get(topic);
    }


    public int showConversation(String topic){
        int previousSIze = userNode.getConversation().get(topic).size();
        consumer.showConversationData(topic);
        int currentSize = userNode.getConversation().get(topic).size();
        return currentSize - previousSIze;
    }

    public ArrayList<String> getLastMessages(String topic, int lastMessages){
        ArrayList<String> messages = new ArrayList<>();

        LinkedList<Message> currentConversation = (LinkedList<Message>) userNode.getConversation().get(topic);

        SimpleDateFormat myFormatObj = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        for(int i = 0; i < lastMessages; i++){
            Message tempMessage = currentConversation.get(currentConversation.size() - (lastMessages-i));
            String strMessage = "Name: " + tempMessage.getName().getProfileName() + "\n" +
                    "Message: " + tempMessage.getMessage() + "\n" +
                    "Date: " + tempMessage.getDate();
            if(tempMessage.getMessage() == null){
                strMessage = "Name: " + tempMessage.getName().getProfileName() + "\n" +
                        "Message: " + tempMessage.getFiles().get(0).getMultimediaFileName() + "\n" +
                        "Date: " + tempMessage.getDate();
            }

        messages.add(strMessage);

        }
        return messages;

    }


    public boolean isConnected(){
        return userNode.isSocketAlive();
    }

    public ArrayList<String> getTopicMessages(String topic){


        ArrayList<String> messages = new ArrayList<>();
        Queue<Message> conversation = new LinkedList<>(userNode.getConversation().get(topic));


        while (!conversation.isEmpty()){

            Message tmpMessage = conversation.poll();

            String strMessage = "";
            if(tmpMessage.getFiles() == null){

                strMessage = "Name: " + tmpMessage.getName().getProfileName() + "\n" +
                        "Message: " + tmpMessage.getMessage() + "\n" +
                        "Date: " + tmpMessage.getDate();
            }
            else {
                strMessage = "Name: " + tmpMessage.getName().getProfileName() + "\n" +
                        "Message: " + tmpMessage.getFiles().get(0).getMultimediaFileName() + "\n" +
                        "Date: " + tmpMessage.getDate();
            }

            messages.add(strMessage);
        }

        return messages;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public UserNode getUserNode() {
        return userNode;
    }

    public void setUserNode(UserNode userNode) {
        this.userNode = userNode;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public HashMap<String, Queue<Message>> initConversations(String name){
        HashMap<String, Queue<Message>> conversations = new HashMap<>();
        BufferedReader br = null;
        try {
            br= new BufferedReader(
                    new InputStreamReader(getAssets().open("data/usernode/userConf.txt"), "UTF-8"));
            String line;

            //gia na broume ton xristi
            nameLoop:
            while( (line = br.readLine()) != null){
                String[] dataFromLine = line.split(",");
                if(dataFromLine[1].equals(name)){

                    //gia kathe topic
                    for(int j = 2; j < dataFromLine.length; j++){
                        Queue<Message> tempQueue = new LinkedList<>();
                        String topic = dataFromLine[j];
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(getAssets().open("data/usernode/" +name+"/"+topic+".txt")));
                        String tempLine;


                        //diabazoume sunomilia
                        while( (tempLine = reader.readLine())!= null){
                            String[] messages = tempLine.split("#");
                            String profileName=messages[1],message=messages[2], date=messages[3];
                            ProfileName userName = new ProfileName(profileName);
                            Message tempMessage = new Message();
                            Date dateSend = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(date);
                            tempMessage.setDate(dateSend);
                            tempMessage.setName(userName);

                            if(message.charAt(0)=='$'){
                                String multimediaFile = message.substring(1);
                                AssetFileDescriptor assetFileDescriptor = getAssets().openFd("data/usernode/" +name+"/"+multimediaFile);
                                List<byte[]> listOfChunks = Util.splitFileToChunks(loadFile(assetFileDescriptor), 1024*16);
                                int numOfChunks = listOfChunks.size();
                                ArrayList<MultimediaFile> listOfMultimediaFiles = new ArrayList<>();
                                for (int i = 0; i < numOfChunks; i++){
                                    byte[] tempArr = listOfChunks.get(i);
                                    MultimediaFile tempFile = new MultimediaFile(multimediaFile, profileName, tempArr.length, tempArr);
                                    tempFile.setDateCreated(dateSend);
                                    listOfMultimediaFiles.add(tempFile);
                                }
                                tempMessage.setFiles(listOfMultimediaFiles);
                                tempQueue.add(tempMessage);
                            }
                            else{
                                tempMessage.setMessage(message);
                                tempQueue.add(tempMessage);
                            }
                        }
                        conversations.put(topic,tempQueue);
                    }
                    break nameLoop;
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return conversations;
    }

    public byte[] loadFile(AssetFileDescriptor assetFileDescriptor){
        byte[] fileData = new byte[(int) assetFileDescriptor.getLength()];
        try {
            FileInputStream fis = assetFileDescriptor.createInputStream();
            fis.read(fileData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileData;
    }
}
