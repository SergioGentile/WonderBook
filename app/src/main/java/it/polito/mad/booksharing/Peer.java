package it.polito.mad.booksharing;

import android.widget.TextView;

class Peer {
    private ReceiverInformation receiverInformation;
    private String keyChat, lastMessage;
    private String date;
    private Long lastTimestamp;

    public Peer(User receiver, String keyChat) {
        receiverInformation = new ReceiverInformation(receiver.getName().getValue(), receiver.getSurname().getValue(), receiver.getUser_image_url(), receiver.getKey());
        this.keyChat = keyChat;
        this.lastMessage = "";
    }

    public Peer() {
    }

    public Peer(ReceiverInformation receiverInformation) {
        this.receiverInformation = receiverInformation;
    }

    public ReceiverInformation getReceiverInformation() {
        return receiverInformation;
    }

    public void setReceiverInformation(ReceiverInformation receiverInformation) {
        this.receiverInformation = receiverInformation;
    }


    public String getKeyChat() {
        return keyChat;
    }

    public void setKeyChat(String keyChat) {
        this.keyChat = keyChat;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Long getLastTimestamp() {
        return lastTimestamp;
    }

    public void setLastTimestamp(Long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

class ReceiverInformation {
    private String name, surname, pathImage, key;
    private TextView notification;

    public ReceiverInformation(String name, String surname, String pathImage, String key) {
        this.name = name;
        this.surname = surname;
        this.pathImage = pathImage;
        this.key = key;
    }

    public ReceiverInformation() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPathImage() {
        return pathImage;
    }

    public void setPathImage(String pathImage) {
        this.pathImage = pathImage;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public TextView getNotification() {
        return notification;
    }

    public void setNotification(TextView notification) {
        this.notification = notification;
    }
}
