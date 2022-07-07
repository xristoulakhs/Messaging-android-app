package com.example.distributedsystemsapp.domain;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

public class MultimediaFile implements Serializable {

    private String multimediaFileName, profileName, framerate, frameWidth, frameHeight;
    private long length;
    private Date dateCreated;
    private byte[] multimediaFileChunk;

    public MultimediaFile(String multimediaFileName, String profileName, Date dateCreated, long length, String framerate, String frameWidth, String frameHeight, byte[] multimediaFileChunk) {
        this.multimediaFileName = multimediaFileName;
        this.profileName = profileName;
        this.dateCreated = dateCreated;
        this.length = length;
        this.framerate = framerate;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.multimediaFileChunk = multimediaFileChunk;
    }

    public MultimediaFile(String multimediaFileName, String profileName, long length){
        this.multimediaFileName = multimediaFileName;
        this.profileName = profileName;
        this.dateCreated = new Date();
        this.length = length;
        this.multimediaFileChunk = Util.loadFile(multimediaFileName);
    }

    public MultimediaFile(String multimediaFileName, String profileName, Date date, long lenght, byte[]multimediaFileChunk){
        this.multimediaFileName = multimediaFileName;
        this.profileName = profileName;
        this.dateCreated = date;
        this.length = lenght;
        this.multimediaFileChunk = multimediaFileChunk;
    }

    public MultimediaFile(String multimediaFileName, String profileName, long lenght, byte[]multimediaFileChunk){
        this.multimediaFileName = multimediaFileName;
        this.profileName = profileName;
        this.dateCreated = new Date();
        this.length = lenght;
        this.multimediaFileChunk = multimediaFileChunk;
    }

    public MultimediaFile() {
    }

    public String getMultimediaFileName() {
        return multimediaFileName;
    }

    public void setMultimediaFileName(String multimediaFileName) {
        this.multimediaFileName = multimediaFileName;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getFramerate() {
        return framerate;
    }

    public void setFramerate(String framerate) {
        this.framerate = framerate;
    }

    public String getFrameWidth() {
        return frameWidth;
    }

    public void setFrameWidth(String frameWidth) {
        this.frameWidth = frameWidth;
    }

    public String getFrameHeight() {
        return frameHeight;
    }

    public void setFrameHeight(String frameHeight) {
        this.frameHeight = frameHeight;
    }

    public byte[] getMultimediaFileChunk() {
        return multimediaFileChunk;
    }

    public void setMultimediaFileChunk(byte[] multimediaFileChunk) {
        this.multimediaFileChunk = multimediaFileChunk;
    }
}
