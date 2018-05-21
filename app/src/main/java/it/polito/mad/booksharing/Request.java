package it.polito.mad.booksharing;

import java.util.Date;

import it.polito.mad.booksharing.Book;
import it.polito.mad.booksharing.User;

/**
 * Created by sergiogentile on 21/05/18.
 */

public class Request {
    String nameLender, nameBorrower, bookImageUrl, bookTitle, status, keyLender, keyBorrower, keyRequest, position;
    long time;
    final static String SENDED = "sended", ACCEPTED = "accepted", REJECTED = "rejected";

    public Request(){

    }

    public Request(User owner, User logged, Book book, String keyRequest){
        this.keyRequest = keyRequest;
        this.nameLender = owner.getName().getValue() + " " + owner.getSurname().getValue();
        this.keyLender = owner.getKey();
        this.nameBorrower = logged.getName().getValue() + " " + logged.getSurname().getValue();
        this.keyBorrower = logged.getKey();
        this.bookTitle = book.getTitle();
        if(!book.getUrlImage().isEmpty()){
            this.bookImageUrl = book.getUrlImage();
        }
        else{
            this.bookImageUrl = book.getUrlMyImage();
        }
        this.position = book.getStreet() + ", " + book.getCity();
        this.time = -1*new Date().getTime();
        status = SENDED;
    }

    public String getNameLender() {
        return nameLender;
    }

    public void setNameLender(String nameLender) {
        this.nameLender = nameLender;
    }

    public String getNameBorrower() {
        return nameBorrower;
    }

    public void setNameBorrower(String nameBorrower) {
        this.nameBorrower = nameBorrower;
    }

    public String getBookImageUrl() {
        return bookImageUrl;
    }

    public void setBookImageUrl(String bookImageUrl) {
        this.bookImageUrl = bookImageUrl;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public String getKeyLender() {
        return keyLender;
    }

    public void setKeyLender(String keyLender) {
        this.keyLender = keyLender;
    }

    public String getKeyBorrower() {
        return keyBorrower;
    }

    public void setKeyBorrower(String keyBorrower) {
        this.keyBorrower = keyBorrower;
    }

    public String getKeyRequest() {
        return keyRequest;
    }

    public void setKeyRequest(String keyRequest) {
        this.keyRequest = keyRequest;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
