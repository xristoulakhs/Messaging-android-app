package com.example.distributedsystemsapp.domain;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.lang.ClassNotFoundException;

public class Message implements Serializable {

    private String message;
    private ProfileName name;
    private List<MultimediaFile> files;
    private Date date;

    public Message() {
    }

    public Message(List<MultimediaFile> files){
        this.files = files;
        this.date= new Date();
    }

    public Message(String message, ProfileName name){
        this.message=message;
        this.name=name;
        this.date= new Date();
    }

    public Message(ProfileName name, List<MultimediaFile> files) {
        this.name = name;
        this.files = files;
        this.date= new Date();
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ProfileName getName() {
        return name;
    }

    public void setName(ProfileName name) {
        this.name = name;
    }

    public List<MultimediaFile> getFiles() {
        return files;
    }

    public void setFiles(List<MultimediaFile> files) {
        this.files = files;
    }

    private void readObject(ObjectInputStream objectInputStream) throws ClassNotFoundException, IOException {
        objectInputStream.defaultReadObject();
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
    }


    @Override
    public String toString() {
        return "Message{" +
                "message='" + message + '\'' +
                ", name=" + name +
                ", files=" + files +
                ", date=" + date +
                '}';
    }
}