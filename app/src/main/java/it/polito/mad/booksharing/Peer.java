package it.polito.mad.booksharing;

import java.util.ArrayList;
import java.util.List;

class Peer{
    private List<PeerInformation> peer;
    private String keyChat, lastMessage;
    private String date;

    public Peer(User user1, User user2, String keyChat) {
        peer = new ArrayList<>();
        peer.add(new PeerInformation(user1.getName().getValue(), user1.getSurname().getValue(), user1.getUser_image_url(), user1.getKey()));
        peer.add(new PeerInformation(user2.getName().getValue(), user2.getSurname().getValue(), user2.getUser_image_url(), user2.getKey()));
        this.keyChat = keyChat;
        this.lastMessage = "";
    }

    public Peer() {
    }

    public Peer(List<PeerInformation> peer) {
        this.peer = peer;
    }

    public List<PeerInformation> getPeer() {
        return peer;
    }

    public void setPeer(List<PeerInformation> peer) {
        this.peer = peer;
    }

    public boolean contains(String key){
        for(PeerInformation peerInformation : peer){
            if(peerInformation.getKey().equals(key)){
                return true;
            }
        }
        return false;
    }

    public PeerInformation getPeerInformationSender(String key){
        for(PeerInformation peerInformation : peer){
            if(peerInformation.getKey().equals(key)){
                return peerInformation;
            }
        }
        return null;
    }
    public PeerInformation getPeerInformationReceiver(String key){
        for(PeerInformation peerInformation : peer){
            if(!peerInformation.getKey().equals(key)){
                return peerInformation;
            }
        }
        return null;
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


}

class PeerInformation{
    private String name, surname, pathImage, key;

    public PeerInformation(String name, String surname, String pathImage, String key) {
        this.name = name;
        this.surname = surname;
        this.pathImage = pathImage;
        this.key = key;
    }

    public PeerInformation() {
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
}
