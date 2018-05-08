package it.polito.mad.booksharing;

import java.util.Date;

/**
 * Created by sergiogentile on 06/05/18.
 */

public class ChatMessage {
    private String sender, message, receiver;
    private long time;
    boolean status_read;

    public ChatMessage(String sender, String receiver ,String message) {
        this.sender = sender;
        this.message = message;
        this.status_read = false;
        this.time = new Date().getTime();
        this.receiver = receiver;
    }

    public ChatMessage(){

    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
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
}
