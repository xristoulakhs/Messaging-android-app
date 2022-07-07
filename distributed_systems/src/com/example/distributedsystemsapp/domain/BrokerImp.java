package com.example.distributedsystemsapp.domain;

import org.javatuples.Pair;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class BrokerImp implements Broker {

    private HashMap<String, Integer> brokerIps= new HashMap<>(); //list with all brokers
    /* Define the socket that receives requests */
    private ServerSocket providerSocket;
    /* Define the socket that is used to handle the connection */
    private Socket connection = null;
    private String ip;
    private int port;
    protected volatile HashMap<String, Queue<Message>> conversations;

    private List<String> registeredPublishers;
    private HashMap<String,String> topicsOfBrokers; //topic and brokers
    private HashMap<String, ArrayList<String>> usersAtTopic; //user and his topics

    public BrokerImp() {
        topicsOfBrokers= new HashMap<>();
        conversations = new HashMap<>();
        usersAtTopic = new HashMap<>();
    }

    public BrokerImp(String ip, int port, HashMap<String, String> topicsOfBrokers, HashMap<String, Integer> brokerIps, HashMap<String, ArrayList<String>> usersAtTopic) {
        this.ip=ip;
        this.port=port;
        this.topicsOfBrokers= topicsOfBrokers;
        this.conversations = new HashMap<>();
        this.brokerIps = brokerIps;
        this.usersAtTopic = usersAtTopic;
    }

    @Override
    public void notifyBrokersOnRegister(String topic, String name) {
        Socket socket;
        ObjectOutputStream out;
        ObjectInputStream in;
        try {
            for (Map.Entry<String, Integer> broker : brokerIps.entrySet()) {
                String ip = broker.getKey();
                int port = broker.getValue();

                if(!(ip.equals(this.getIp()) && port==this.getPort())) {
                    socket = new Socket(ip, port);
                    out = new ObjectOutputStream(socket.getOutputStream());
                    in = new ObjectInputStream(socket.getInputStream());

                    out.writeUTF("broker");
                    out.flush();

                    out.writeUTF(topic);
                    out.flush();

                    out.writeUTF(name);
                    out.flush();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void init(String ip, int port) {

        System.out.println(ip + "==="+ port);


        try {

            InetAddress addr = InetAddress.getByName(ip);
            /* Create Server Socket */
            providerSocket = new ServerSocket(port);

            while (true) {
                /* Accept the connection */

                connection = providerSocket.accept();

                /* Handle the request */
                Thread t = new Thread(new ActionsForClients(this, connection));
                t.start();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
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

    public HashMap<String,String> getTopicsOfBrokers() {
        return topicsOfBrokers;
    }

    public void setTopicsOfBrokers(HashMap<String,String> topicsOfBrokers) {
        this.topicsOfBrokers = topicsOfBrokers;
    }

    public HashMap<String, Integer> getBrokerIps() {
        return brokerIps;
    }

    public void setBrokerIps(HashMap<String, Integer> brokerIps) {
        this.brokerIps = brokerIps;
    }

    public List<String> getRegisteredPublishers() {
        return registeredPublishers;
    }

    public void setRegisteredPublishers(List<String> registeredPublishers) {
        this.registeredPublishers = registeredPublishers;
    }

    public HashMap<String, Queue<Message>> getConversations() {
        return this.conversations;
    }

    public void setConversations(HashMap<String, Queue<Message>> conversations) {
        this.conversations = conversations;
    }

    //gia na prosthesoume minuma
    public void addMessageOnConversation(String topic, Message message){
        this.conversations.get(topic).add(message);
    }

    public HashMap<String, ArrayList<String>> getUsersAtTopic() {
        return usersAtTopic;
    }

    public void setUsersAtTopic(HashMap<String, ArrayList<String>> usersAtTopic) {
        this.usersAtTopic = usersAtTopic;
    }

    public static void main(String[] args) {

        int brokerID = Integer.parseInt(args[0]);
        Pair<String, Integer> brokerInfo = Util.findIPAddressAndPortOfBroker(brokerID);
        BrokerImp broker = new BrokerImp(brokerInfo.getValue0(), brokerInfo.getValue1(),Util.readAllBrokerTopicsFromConf(), Util.readAllBrokersFromConfToHashMap(), Util.getUsersAtTopic());

        System.out.println("The server running is: " + args[0]);

        //temp vars for init conversations
        HashMap<String, Queue<Message>> conversation = new HashMap<>();
        String pathOfConversations = "data/broker/conversations/";

        for(Map.Entry<String, String> topic: broker.getTopicsOfBrokers().entrySet()){
            if(topic.getValue().equals(brokerInfo.getValue0())){
                conversation.put(topic.getKey(), Util.readConversationOfTopic(topic.getKey(), pathOfConversations));
            }
        }
        //init conversations and broker
        broker.setConversations(conversation);
        broker.init(brokerInfo.getValue0(), brokerInfo.getValue1());
    }
}