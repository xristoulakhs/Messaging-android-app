package com.example.distributedsystemsapp.domain;


import org.javatuples.Pair;
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.ClassNotFoundException;

public class ActionsForClients extends BrokerImp implements Runnable {
    ObjectInputStream in;
    ObjectOutputStream out;
    BrokerImp broker;
    Socket connection;


    public ActionsForClients(BrokerImp broker,Socket connection) {
        this.broker = broker;
        this.connection = connection;
        try {
            System.out.println("Got a connection...Opening streams....");
            out = new ObjectOutputStream(connection.getOutputStream());
            in = new ObjectInputStream(connection.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void consumer(){
        try {
            synchronized (broker) {
                String consumerAction = in.readUTF();
                if (consumerAction.equals("showConversation")) {
                    String topic = in.readUTF();
                    String stateOfConversation = in.readUTF();
                    Queue<Message> conversation;
                    if (stateOfConversation.equals("all")) {
                        conversation = new LinkedList<>(broker.getConversations().get(topic));
                        System.out.println("all");
                    } else {
                        Date lastMessageDate = (Date) in.readObject();
                        LinkedList<Message> tempList = new LinkedList<>(broker.getConversations().get(topic));
                        conversation = new LinkedList<>();
                        for (int i = 0; i < tempList.size(); i++) {
                            Message tempMessage = tempList.get(i);
                            if (tempMessage.getDate().compareTo(lastMessageDate) > 0) {
                                conversation.add(tempMessage);
                            }
                        }
                    }
                    int sizeOfQueue = conversation.size();

                    out.writeInt(sizeOfQueue);
                    out.flush();
                    for (int i = 0; i < sizeOfQueue; i++) {
                        Message message = conversation.poll();
                        if (message.getMessage() == null) {
                            out.writeUTF("f");
                            out.flush();
                            List<MultimediaFile> files = message.getFiles();
                            int sizeOfFiles = files.size();
                            out.writeInt(sizeOfFiles);
                            out.flush();
                            out.writeObject(message.getName());
                            out.flush();
                            for (int j = 0; j < sizeOfFiles; j++) {
                                out.writeObject(files.get(j));
                                out.flush();
                            }
                        } else {
                            out.writeUTF("s");
                            out.flush();

                            out.writeObject(message);
                            out.flush();

                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void publisher(){
        try {
            synchronized (broker) {
                String publisherAction = in.readUTF();
                if (publisherAction.equals("sendMessage")) {
                    try {
                        String topic = in.readUTF();
                        Message m = (Message) in.readObject();
                        broker.addMessageOnConversation(topic, m);
                        System.out.println("User send a message in topic: " + topic + "!");
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if (publisherAction.equals("multimediaFile")) {
                    try {
                        String topic = in.readUTF();
                        ProfileName profileName = (ProfileName) in.readObject();
                        int chunks = in.readInt();
                        List<MultimediaFile> fileInChunks = new ArrayList<>();
                        for (int i = 0; i < chunks; i++) {
                            MultimediaFile multimediaFile = (MultimediaFile) in.readObject();
                            fileInChunks.add(multimediaFile);
                        }
                        broker.addMessageOnConversation(topic, new Message(profileName, fileInChunks));
                        System.out.println("User send a video/image in topic: " + topic + "!");
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void userNode(){
        try {
            synchronized (broker) {
                String action = in.readUTF();
                if (action.equals("firstCommunication")) {
                    String name = in.readUTF();
                    System.out.println("User " + name + " communicate with broker!");

                    //this will return
                    HashMap<String, Pair<String, Integer>> topicForUserNode = new HashMap<>();

                    HashMap<String, String> topicOfBrokers = broker.getTopicsOfBrokers();
                    HashMap<String, Integer> brokerIps = broker.getBrokerIps();
                    ArrayList<String> topics = broker.getUsersAtTopic().get(name);

                    for (int i = 0; i < topics.size(); i++) {
                        String ip = topicOfBrokers.get(topics.get(i));
                        int port = brokerIps.get(ip);
                        Pair<String, Integer> broker = new Pair<>(ip, port);
                        topicForUserNode.put(topics.get(i), broker);
                    }
                    out.writeObject(topicForUserNode);
                    out.flush();
                } else if (action.equals("register")) {
                    String topic = in.readUTF();
                    String name = in.readUTF();

                    HashMap<String, ArrayList<String>> users = broker.getUsersAtTopic();

                    if (broker.getTopicsOfBrokers().containsKey(topic)) {
                        users.get(name).add(topic);
                        broker.setUsersAtTopic(users);

                        broker.notifyBrokersOnRegister(topic, name);
                        out.writeUTF("success");
                        out.flush();
                        System.out.println("User " + name + " register successfully in topic " + topic + "!");
                    } else {
                        out.writeUTF("fail");
                        out.flush();
                        System.out.println("User " + name + " failed to register in topic " + topic + "!");
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void run() {
        while(!this.connection.isClosed()) {
            try {
                String userType = in.readUTF();
                if (userType.equals("consumer")) {
                    consumer();
                } else if (userType.equals("publisher")) {
                    publisher();
                }
                else if (userType.equals("userNode")) {
                    userNode();
                }
                else if(userType.equals("broker")){
                    String topic = in.readUTF();
                    String name = in.readUTF();

                    HashMap<String, ArrayList<String>> users = broker.getUsersAtTopic();
                    users.get(name).add(topic);
                    broker.setUsersAtTopic(users);

                }
                else if(userType.equals("close")){
                    System.out.println("Closing connection");
                    break;
                }
            } catch (IOException e) {
                System.err.println("User disconnected for unknown reason, closing connection...");
                try {
                    this.connection.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}
