package com.example.distributedsystemsapp.domain;

import org.javatuples.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public final class Util {

    private static final String PATH = "com/example/distributedsystemsapp/data/broker/conf.txt";
    private static final String FOLDER_PATH = "com/example/distributedsystemsapp/data/broker/";
    private static final String USERNODE_CONF_PATH = "com/example/distributedsystemsapp/data/usernode/userConf.txt";
    private static final String DATA_USERNODE_PATH = "com/example/distributedsystemsapp/data/usernode/";


    /**
     * Reading from the file conf.txt information about brokers
     * @return A HashMap with IP Address and Port of brokers
     */
    public static HashMap<String, Integer> readAllBrokersFromConfToHashMap(){

        HashMap<String, Integer> brokers = new HashMap<>();
        File file = new File(PATH);
        Scanner line = null;
        try {
            line = new Scanner(file);
            if (!line.nextLine().equals("Brokers")) return null;//check for possible error in file
            while (line.hasNextLine()){
                String data = line.nextLine();
                if (data.equals("Topics")) break; //break the while and stop reading file
                String info[] = data.split(",");
                brokers.put(info[1], Integer.parseInt(info[2]));
            }
            line.close();//close the file stream
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return brokers;
    }

    /**
     * reading all topics from the configuration file
     * @return HashSet
     */
    public static HashSet<String> readAllTopicsFromConf(){

        HashSet<String> topics = new HashSet<>();
        try {
            File file = new File(PATH);
            Scanner line = new Scanner(file);
            boolean topicFound = false;
            while (line.hasNextLine()) {
                String data = line.nextLine();
                if (data.equals("Topics")){
                    topicFound = true;
                    continue;
                }
                if(topicFound) {
                    topics.add(data);
                }
                else {
                    continue;
                }
            }
            line.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return topics;
    }


    /**
     * based on id it finds the address and the port of a broker
     * @param brokerID
     * @return
     */
    public static Pair<String, Integer> findIPAddressAndPortOfBroker(int brokerID){

        Pair<String, Integer> brokerInfo = null;

        try {
            File file = new File(PATH);
            Scanner line = new Scanner(file);
            while (line.hasNextLine()){
                String data = line.nextLine();
                if(data.equals("Brokers")) continue;
                else if (data.equals("Topics")) break;
                String info[] = data.split(",");
                if (brokerID == Integer.parseInt(info[0])){
                    brokerInfo = new Pair<>(info[1], Integer.parseInt(info[2]));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return brokerInfo;
    }

    /**
     * creating hash value of a String with SHA-1
     * @param value
     * @return
     */
    public static String topicToSHA1Hash(String value){
        String sha1 = "";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            digest.update(value.getBytes("utf8"));
            sha1 = String.format("%040x", new BigInteger(1, digest.digest()));
        } catch (Exception e){
            e.printStackTrace();
        }
        return sha1;
    }

    /**
     * methodos gia na epistrepsei to HashMap me ta topic
     * kai poios broker einai upeuthunos gia auta
     * @return
     */
    public static HashMap<String,String> readAllBrokerTopicsFromConf(){
        HashMap<String,Integer> brokerIps= readAllBrokersFromConfToHashMap();
        HashMap<String,String> topics = new HashMap<>();
        try {
            File file = new File(PATH);
            Scanner line = new Scanner(file);
            boolean topicFound = false;
            while (line.hasNextLine()) {
                String data = line.nextLine();
                if (data.equals("Topics")){
                    topicFound = true;
                    continue;
                }
                if(topicFound) {
                    topics.put(data,saveToProperBroker(data,brokerIps));
                }
            }
            line.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return topics;
    }

    /**
     * methodos gia na apothikeusei to topic ston katallilo broker
     * @param topicName : to onoma tou topic
     * @param brokers   : HashMap me ip kai port gia kathe broker
     * @return          : gurnaei tin ip tou broker pou tha apothikeutei to topic
     */
    public static String saveToProperBroker(String topicName,HashMap<String, Integer> brokers){
        String hashedStr= topicToSHA1Hash(topicName);
        String strToReturn="";

        //sortarisma gia sugkrisi
        HashMap<String, String> newHashMap= new HashMap<>();
        String[] tempArray= new String[brokers.size()];
        int i=0;
        for(Map.Entry<String,Integer> entry : brokers.entrySet()){
            String key = entry.getKey();
            Integer value = entry.getValue();
            String hashed= topicToSHA1Hash(key+value);
            tempArray[i]=hashed;
            newHashMap.put(hashed,key);
            i++;
        }
        Arrays.sort(tempArray);

        //sugkrisi
        for(String hashedBroker:tempArray){
            if(hashedStr.compareTo(hashedBroker)<0){
                strToReturn=newHashMap.get(hashedBroker);
                break;
            }
            else{
                strToReturn=newHashMap.get(tempArray[0]); //deafult broker
            }
        }
        return strToReturn;
    }

    public static Queue<Message> readConversationOfTopic(String fileName, String path){

        Queue<Message> messages = new LinkedList<>();

        try {
            File file = new File(path + fileName + ".txt");
            Scanner line = new Scanner(file);
            while (line.hasNextLine()){
                String message = line.nextLine();
                String profileName = "", messageSend = "", dateSend = "";
                if(message.charAt(0) == '#'){
                    String[] dataStr = message.split("#");

                    profileName = dataStr[1];
                    messageSend = dataStr[2];
                    dateSend = dataStr[3];
                    ProfileName name = new ProfileName(profileName);
                    Message tempMessage = new Message();
                    Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(dateSend);
                    tempMessage.setDate(date);
//                    System.out.println(date);
                    tempMessage.setName(name);

                    if(messageSend.charAt(0)=='$'){
                        String multimediaFile= messageSend.substring(1);
                        List<byte[]> listOfChunks = splitFileToChunks(loadFile(path + multimediaFile), 1024*16);
                        int numOfChunks = listOfChunks.size();
                        ArrayList<MultimediaFile> listOfMultimediaFiles = new ArrayList<>();
                        for (int i = 0; i < numOfChunks; i++){
                            byte[] tempArr = listOfChunks.get(i);
                            MultimediaFile tempFile = new MultimediaFile(multimediaFile, profileName, tempArr.length, tempArr);
                            tempFile.setDateCreated(date);
//                            System.out.println("in multimediaFile: "+date);
                            listOfMultimediaFiles.add(tempFile);
                        }
                        tempMessage.setFiles(listOfMultimediaFiles);
                        messages.add(tempMessage);
                    }
                    else {
                        tempMessage.setMessage(messageSend);
                        messages.add(tempMessage);
                    }
                }
            }
            line.close();
        } catch (FileNotFoundException | ParseException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public static byte[] loadFile(String path){
        try{
            File file = new File(path);
            FileInputStream fis = new FileInputStream(file);
            byte[] fileData = new byte[(int)file.length()];
            fis.read(fileData);
            return fileData;
        }catch (FileNotFoundException e){
            System.err.println("File not found!");
        }catch (IOException e){
            System.err.println("Error at loadFile() ");
        }
        return null;
    }

    /**
     * Splits a file's data into smaller parts
     * @param chunkSize the chunk size in bytes
     */
    public static List<byte[]> splitFileToChunks(byte[] data, int chunkSize){
        List<byte[]> result = new ArrayList<>();
        int chunks = data.length / chunkSize;

        for (int i = 0; i < chunks ; i++) {
            byte[] current = new byte[chunkSize];
            for (int j = 0; j < chunkSize ; j++) {
                current[j] = data[j + i*chunkSize];
            }
            result.add(current);
        }

        //Add last chunk
        boolean isRemaining = data.length % chunkSize != 0;
        if (isRemaining){
            int remaining = data.length % chunkSize;
            int offset = chunks * chunkSize;

            byte[] current = new byte[remaining];
            for(int i = 0; i < remaining; ++i){
                current[i] = data[offset + i];
            }
            result.add(current);
        }
        return result;
    }

    public static String[] initUserNode(int user){

        try {
            File file = new File(USERNODE_CONF_PATH);
            Scanner line = new Scanner(file);
            while (line.hasNextLine()) {
                String data = line.nextLine();
                int charZero = Integer.parseInt(String.valueOf(data.charAt(0)));
                if(charZero == user){
                    String[] strArr = data.split(",");
                    return strArr;
                }
            }
            line.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static HashMap<String, ArrayList<String>> getUsersAtTopic(){
        HashMap<String, ArrayList<String>> userAtTopic = new HashMap<>();
        try{
            File file = new File(USERNODE_CONF_PATH);
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String data = scanner.nextLine();
                String[] arrStr = data.split(",");
                ArrayList<String> arrayList = new ArrayList<>();
                for(int i = 2; i < arrStr.length; i++){
                    arrayList.add(arrStr[i]);
                }
                userAtTopic.put(arrStr[1], arrayList);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return userAtTopic;
    }
}
