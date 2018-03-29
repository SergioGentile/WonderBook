package it.polito.mad.booksharing;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


//The User class will contain all the information about the user info and their status (private || public)


//This interface implements the Parcelable interface because the object must be shared between more than one activity.

public class User implements Parcelable{
    private Pair<String,String> name, surname, phone, email, description, city, cap, street;
    private String imagePath;

    public static final String profileImgName = "profile.jpeg";
    public static final String profileImgNameCrop = "profile_cropper.jpeg";
    public static final String imageDir = "imageDir";

    private static String myDefault="";
    public User(){
        //When a new object is created all the field will be empty and public
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

    //All the setter and getter of the fields
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

    public String getImagePath(){
        return this.imagePath;
    }

    public void setImagePath(String path){

        this.imagePath = path;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    //Used to serialize the object
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

    //Method used to deserialize the method
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


    //Method used to check if the information placed by the user during the edit phase are correct
    public String checkInfo(Context context) {

        int counter = 0;
        String correctly = "";
        String message = "";

        //Name check
        Pattern namePatter = Pattern.compile("^[a-z]+$", Pattern.CASE_INSENSITIVE);
        Matcher matcherName = namePatter.matcher(name.first);
        boolean findName = matcherName.find();
        if(name.first.equals(myDefault) || !findName){
            counter++;
            message += " -" + context.getString(R.string.name) + "\n";
            if(!findName && !name.first.equals(myDefault)){
                correctly = context.getString(R.string.correctly);
            }
        }

        //surname check
        Pattern surnamePatter = Pattern.compile("^[a-z]+$", Pattern.CASE_INSENSITIVE);
        Matcher matcherSurname = surnamePatter.matcher(surname.first);
        boolean findSurname = matcherSurname.find();
        if(surname.first.equals(myDefault) || !findSurname){
            counter++;
            message += " -" + context.getString(R.string.surname) + "\n";
            if(!findSurname && !surname.first.equals(myDefault)){
                correctly = context.getString(R.string.correctly);
            }
        }

        //email check
        Pattern emailPatter = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcherMail = emailPatter.matcher(email.first);
        boolean findMail = matcherMail.find();
        if(email.first.equals(myDefault) || !findMail){
            counter++;
            message += " -" + context.getString(R.string.mail) + "\n";
            if(!findMail && !email.first.equals(myDefault)){
                correctly = context.getString(R.string.correctly);
            }
        }

        if(cap.first.equals(myDefault)){
            counter++;
            message += " -" + context.getString(R.string.cap) + "\n";
        }

        if(city.first.equals(myDefault)){
            counter++;
            message += " -" + context.getString(R.string.city) + "\n";
        }


        if(counter == 0){
            return null;
        }
        else{
            if(counter==1){
                return context.getString(R.string.field_sin) + "\n" + message + context.getString(R.string.allert_end_sin) + " " + correctly;
            }
            else{
                return context.getString(R.string.field_pl) + "\n" + message + context.getString(R.string.allert_end_pl) + " " + correctly;
            }

        }
    }

    //Methods used to change the status of an attribute (private|| public)
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

    //Methods used to check if an attribute is public(return true) or private(return false)

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


}
