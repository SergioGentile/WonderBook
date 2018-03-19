package it.polito.mad.booksharing;

import android.app.Application;
import android.os.Parcel;
import android.os.Parcelable;

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
    private String name, surname, phone, email, description, city, cap, street;
    private int isCheckStreet, isCheckPhone, isCheckMail;

    public User(){

    }

    public User(String name, String surname, String phone, String email, String description, String city, String cap, String street, int isCheckMail, int isCheckPhone, int isCheckStreet) {
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
        this.description = description;
        this.city = city;
        this.cap = cap;
        this.street = street;
        this.isCheckMail = isCheckMail;
        this.isCheckPhone = isCheckPhone;
        this.isCheckStreet = isCheckStreet;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCap() {
        return cap;
    }

    public void setCap(String cap) {
        this.cap = cap;
    }

    public int isCheckStreet() {
        return isCheckStreet;
    }

    public void setCheckStreet(int checkStreet) {
        isCheckStreet = checkStreet;
    }

    public int isCheckPhone() {
        return isCheckPhone;
    }

    public void setCheckPhone(int checkPhone) {
        isCheckPhone = checkPhone;
    }

    public int isCheckMail() {
        return isCheckMail;
    }

    public void setCheckMail(int checkMail) {
        isCheckMail = checkMail;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.surname);
        dest.writeString(this.phone);
        dest.writeString(this.email);
        dest.writeString(this.description);
        dest.writeString(this.city);
        dest.writeString(this.cap);
        dest.writeString(this.street);
        dest.writeInt(this.isCheckMail);
        dest.writeInt(this.isCheckPhone);
        dest.writeInt(this.isCheckStreet);
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
        this.name = parcel.readString();
        this.surname = parcel.readString();
        this.phone = parcel.readString();
        this.email = parcel.readString();
        this.description = parcel.readString();
        this.city = parcel.readString();
        this.cap = parcel.readString();
        this.street = parcel.readString();
        this.isCheckMail = parcel.readInt();
        this.isCheckPhone = parcel.readInt();
        this.isCheckStreet = parcel.readInt();
    }


}
