package it.polito.mad.booksharing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sergiogentile on 06/05/18.
 */

public class ChatMessage {
    private String sender, message, receiver, key;
    private long time;
    boolean status_read;
    private List<String> deleteFor;


    public ChatMessage(String sender, String receiver ,String message, String key) {
        this.sender = sender;
        this.message = message;
        this.status_read = false;
        this.time = new Date().getTime();
        this.receiver = receiver;
        this.key = key;
        deleteFor = new ArrayList<>();
    }

    public ChatMessage(){
        deleteFor = new ArrayList<>();
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isStatus_read() {
        return status_read;
    }

    public void setStatus_read(boolean status_read) {
        this.status_read = status_read;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void addUserDelete(String keyUser){
        deleteFor.add(keyUser);
    }

    public List<String> getDeleteFor() {
        return deleteFor;
    }

    public void setDeleteFor(List<String> deleteFor) {
        this.deleteFor = deleteFor;
    }
}
