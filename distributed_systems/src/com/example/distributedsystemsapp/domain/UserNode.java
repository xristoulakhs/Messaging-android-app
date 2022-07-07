package com.example.distributedsystemsapp.domain;

import org.javatuples.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

public class UserNode extends Thread{

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

    UserNode(){
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

}
