package it.polito.mad.booksharing;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

//This interface implements the Parcelable interface because the object must be shared between more than one activity.
//It's better share all the object and not only a string at a time.
/*
    putExtras("user", user)

    Instead of

    putExtraString("name", name)
    putExtraString("surname", surname)
    putExtraString("phone", phone)
    and so on....


 */

public class User implements Parcelable{
    private Pair<String,String> name, surname, phone, email, description, city, cap, street;
    private String imagePath;

    private static String myDefault="";
    public User(){

        this.name = new Pair<>("","public");
        this.surname=new Pair<>("","public");
        this.phone=new Pair<>("","public");
        this.email = new Pair<>("","public");
        this.description = new Pair<>("","public");
        this.city= new Pair<>("","public");
        this.cap= new Pair<>("","public");
        this.street= new Pair<>("","public");


    }

    public User(Pair<String, String> name, Pair<String, String> surname, Pair<String, String> phone, Pair<String, String> email, Pair<String, String> description, Pair<String, String> city, Pair<String, String> cap, Pair<String, String> street) {
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
        this.description = description;
        this.city = city;
        this.cap = cap;
        this.street = street;
    }

    public Pair<String, String> getName() {
        return name;
    }

    public void setName(Pair<String, String> name) {
        this.name = name;
    }

    public Pair<String, String> getSurname() {
        return surname;
    }

    public void setSurname(Pair<String, String> surname) {
        this.surname = surname;
    }

    public Pair<String, String> getPhone() {
        return phone;
    }

    public void setPhone(Pair<String, String> phone) {
        this.phone = phone;
    }

    public Pair<String, String> getEmail() {
        return email;
    }

    public void setEmail(Pair<String, String> email) {
        this.email = email;
    }

    public Pair<String, String> getDescription() {
        return description;
    }

    public void setDescription(Pair<String, String> description) {
        this.description = description;

    }

    public Pair<String, String> getCity() {
        return city;
    }

    public void setCity(Pair<String, String> city) {
        this.city = city;
    }

    public Pair<String, String> getCap() {
        return cap;
    }

    public void setCap(Pair<String, String> cap) {
        this.cap = cap;
    }

    public Pair<String, String> getStreet() {
        return street;
    }

    public void setStreet(Pair<String, String> street) {
        this.street = street;
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.surname.first+"/t"+this.surname.second);
        dest.writeString(this.name.first+"/t"+this.name.second);
        dest.writeString(this.phone.first+"/t"+this.phone.second);
        dest.writeString(this.email.first+"/t"+this.email.second);
        dest.writeString(this.description.first + "/t" + this.description.second);
        dest.writeString(this.city.first+"/t"+this.city.second);
        dest.writeString(this.cap.first+"/t"+this.cap.second);
        dest.writeString(this.street.first+"/t"+this.street.second);
        dest.writeString(this.getImagePath());
    }

    public final static Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public User(Parcel parcel) {
        String[] newname = parcel.readString().split("/t");
        this.name = new Pair<String, String>(newname[0],newname[1]);
        String[] newsurname = parcel.readString().split("/t");
        this.surname = new Pair<String, String>(newsurname[0],newsurname[1]);
        String[] newphone = parcel.readString().split("/t");
        this.phone = new Pair<String, String>(newphone[0],newphone[1]);
        String[] newmail = parcel.readString().split("/t");
        this.email = new Pair<String, String>(newmail[0],newmail[1]);
        String[] descr = parcel.readString().split("/t");
        this.description = new Pair<String, String>(descr[0],descr[1]);
        String[] newcity = parcel.readString().split("/t");
        this.city = new Pair<String, String>(newcity[0],newcity[1]);
        String[] newcap = parcel.readString().split("/t");
        this.cap = new Pair<String, String>(newcap[0],newcap[1]);
        String[] newstreet = parcel.readString().split("/t");
        this.street = new Pair<String, String>(newstreet[0],newstreet[1]);
        this.imagePath = parcel.readString();

    }


    public boolean checkInfo() {

        if(name.first.equals(myDefault) || surname.first.equals(myDefault) || email.first.equals(myDefault) || cap.first.equals(myDefault) || city.first.equals(myDefault)){
            return false;
        }
        return true;
    }

    public void setCheckMail(String checkMail) {
        String email = this.email.first;
        this.email = new Pair<>(email,checkMail);
    }

    public void setCheckStreet(String checkStreet) {
        String street = this.street.first;
        this.street = new Pair<>(street,checkStreet);
    }

    public void setCheckPhone(String checkPhone) {
        String phone = this.phone.first;
        this.phone = new Pair<>(phone,checkPhone);
    }

    public boolean checkMail(){
        if(this.email.second.equals("public")){
            return true;
        }
        return false;
    }

    public boolean checkStreet(){
        if(this.street.second.equals("public")){
            return true;
        }
        return false;
    }

    public boolean checkPhone(){
        if(this.phone.second.equals("public")){
            return true;
        }
        return false;
    }

    public String getImagePath(){
        return this.imagePath;
    }

    public void setImagePath(String path){

        this.imagePath = path;
    }
}
