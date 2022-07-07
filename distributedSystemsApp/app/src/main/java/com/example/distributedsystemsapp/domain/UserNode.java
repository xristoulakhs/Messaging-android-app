package com.example.distributedsystemsapp.domain;

import android.util.Log;

import org.javatuples.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class UserNode extends Thread implements Serializable {

    /* Create socket for contacting the server on port 4321*/
    protected static Socket requestSocket = null;

    /* Create the streams to send and receive data from server */
    protected static ObjectOutputStream out = null;
    protected static ObjectInputStream in = null;
    protected String ip;
    protected int port;

    private HashMap<String, Pair<String, Integer>> topicWithBrokers;
    private HashMap<String, Queue<Message>> conversation;


    public UserNode(String ip, int port) {
        this.ip = ip;
        this.port = port;
        this.topicWithBrokers = new HashMap<>();
        this.conversation = new HashMap<>();
    }

    public UserNode(){
        this.topicWithBrokers = new HashMap<>();
        this.conversation = new HashMap<>();
    }

    public HashMap<String, Queue<Message>> getConversation() {
        return conversation;
    }

    public void setConversation(HashMap<String, Queue<Message>> conversation) {
        this.conversation = conversation;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }


    public void setPort(int port) {
        this.port = port;
    }

    public HashMap<String, Pair<String, Integer>> getTopicWithBrokers() {
        return topicWithBrokers;
    }

    public void setTopicWithBrokers(HashMap<String, Pair<String, Integer>> topicWithBrokers) {
        this.topicWithBrokers = topicWithBrokers;
    }

    public Date getLastDate(String topic){
        LinkedList<Message> ll = new LinkedList<>(this.conversation.get(topic));
        return ll.getLast().getDate();
    }

    public boolean isSocketAlive(){
        return requestSocket.isConnected();
    }

    public void communicateWithBroker(String name){
        try{
            out.writeUTF("userNode");
            out.flush();

            out.writeUTF("firstCommunication");
            out.flush();

            out.writeUTF(name);
            out.flush();

            this.topicWithBrokers = (HashMap<String, Pair<String, Integer>>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Socket init() {

        try {
            requestSocket = new Socket();
//            requestSocket.connect(new InetSocketAddress(this.getIp(),this.getPort()));
            requestSocket = new Socket(InetAddress.getByName(this.getIp()),this.getPort());
            out= new ObjectOutputStream(requestSocket.getOutputStream());
            in= new ObjectInputStream(requestSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return requestSocket;

    }

    public void redirect(String ip, int port){
        //close previous socket
        closeWithServer();
        this.setIp(ip);
        this.setPort(port);
        init();
    }

    /**
     * method to check if topic is at the broker we are currently connected
     * @param topic
     */
    public void checkBroker(String topic){
        String ip=topicWithBrokers.get(topic).getValue0();
        int port= topicWithBrokers.get(topic).getValue1();
        if(!(ip.equals(this.getIp()) && port==this.getPort())){
            redirect(ip,port);
        }
    }

    public void closeWithServer(){
        try {
            out.writeUTF("close");
            out.flush();
            requestSocket.close();
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean registerUserAtTopic(String topic, String profileName){
        boolean status = false;
        try {
            out.writeUTF("userNode");
            out.flush();

            out.writeUTF("register");
            out.flush();

            out.writeUTF(topic);
            out.flush();

            out.writeUTF(profileName);
            out.flush();

            String response = in.readUTF();
            if(response.equals("success")){
                System.out.println("Successfully registered at " + topic + "!");
                conversation.put(topic, new LinkedList<>());
                status = true;
            }
            else {
                System.out.println("Failed registration at topic: " + topic + ", the topic does not exist!");
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return status;
    }

}
