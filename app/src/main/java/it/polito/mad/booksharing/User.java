package it.polito.mad.booksharing;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.DatabaseReference;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


//The User class will contain all the information about the user info and their status (private || public)


//This interface implements the Parcelable interface because the object must be shared between more than one activity.

public class User implements Parcelable{
    private MyPair name, surname, phone, email, description, city, cap, street;
    private String imagePath="";
    private String key;
    public static final int IMAGE_QUALITY = 100;
    public static final Bitmap.CompressFormat COMPRESS_FORMAT_BIT= Bitmap.CompressFormat.JPEG;
    public static final String COMPRESS_FORMAT_STR = "jpeg";
    public static final String profileImgName = "profile." + COMPRESS_FORMAT_STR;
    public static final String profileImgNameCrop = "profile_cropper." + COMPRESS_FORMAT_STR;
    public static final String imageDir = "imageDir";


    private static String myDefault="";
    public User(){

        name=new MyPair();
        surname=new MyPair();
        phone=new MyPair();
        email=new MyPair();
        description=new MyPair();
        city=new MyPair();
        cap=new MyPair();
        street = new MyPair();

    }

    public User(MyPair name, MyPair surname, MyPair phone, MyPair email, MyPair description, MyPair city, MyPair cap, MyPair street) {
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
        this.description = description;
        this.city = city;
        this.cap = cap;
        this.street = street;
    }

    public User(MyPair name, MyPair surname, MyPair phone, MyPair email, MyPair description, MyPair city, MyPair cap, MyPair street,String imagePath,String key) {
        this.name = name;
        this.surname = surname;
        this.phone = phone;
        this.email = email;
        this.description = description;
        this.city = city;
        this.cap = cap;
        this.street = street;
        this.imagePath = imagePath;
        this.key = key;
    }

    public User(User value) {

        this.name = new MyPair(value.getName());
        this.surname = new MyPair(value.getSurname());
        this.phone = new MyPair(value.getPhone());
        this.email = new MyPair(value.getEmail());
        this.description = new MyPair(value.getDescription());
        this.city = new MyPair(value.getCity());
        this.cap = new MyPair(value.getCap());
        this.street =new MyPair( value.getStreet());
        this.imagePath = new String(value.getImagePath());
        this.key = new String(value.getKey());
    }

    //All the setter and getter of the fields
    public MyPair getName() {
        return name;
    }

    public void setName(MyPair name) {
        this.name = name;
    }

    public MyPair getSurname() {
        return surname;
    }

    public void setSurname(MyPair surname) {
        this.surname = surname;
    }

    public MyPair getPhone() {
        return phone;
    }

    public void setPhone(MyPair phone) {
        this.phone = phone;
    }

    public MyPair getEmail() {
        return email;
    }

    public void setEmail(MyPair email) {
        String clean_email = email.getValue().toLowerCase().replace(" ","");
        this.email = new MyPair(clean_email,email.getStatus());
    }

    public MyPair getDescription() {
        return description;
    }

    public void setDescription(MyPair description) {
        this.description = description;

    }

    public MyPair getCity() {
        return city;
    }

    public void setCity(MyPair city) {
        this.city = city;
    }

    public MyPair getCap() {
        return cap;
    }

    public void setCap(MyPair cap) {
        this.cap = cap;
    }

    public MyPair getStreet() {
        return street;
    }

    public void setStreet(MyPair street) {
        this.street = street;
    }




    @Override
    public int describeContents() {
        return 0;
    }

    //Used to serialize the object
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name.getValue()+"/t"+this.name.getStatus());
        dest.writeString(this.surname.getValue()+"/t"+this.surname.getStatus());
        dest.writeString(this.phone.getValue()+"/t"+this.phone.getStatus());
        dest.writeString(this.email.getValue()+"/t"+this.email.getStatus());
        dest.writeString(this.description.getValue() + "/t" + this.description.getStatus());
        dest.writeString(this.city.getValue()+"/t"+this.city.getStatus());
        dest.writeString(this.cap.getValue()+"/t"+this.cap.getStatus());
        dest.writeString(this.street.getValue()+"/t"+this.street.getStatus());
        dest.writeString(this.getImagePath());
        dest.writeString(this.getKey());
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
        this.name = new MyPair(newname[0],newname[1]);
        String[] newsurname = parcel.readString().split("/t");
        this.surname = new MyPair(newsurname[0],newsurname[1]);
        String[] newphone = parcel.readString().split("/t");
        this.phone = new MyPair(newphone[0],newphone[1]);
        String[] newmail = parcel.readString().split("/t");
        this.email = new MyPair(newmail[0],newmail[1]);
        String[] descr = parcel.readString().split("/t");
        this.description = new MyPair(descr[0],descr[1]);
        String[] newcity = parcel.readString().split("/t");
        this.city = new MyPair(newcity[0],newcity[1]);
        String[] newcap = parcel.readString().split("/t");
        this.cap = new MyPair(newcap[0],newcap[1]);
        String[] newstreet = parcel.readString().split("/t");
        this.street = new MyPair(newstreet[0],newstreet[1]);
        this.imagePath = parcel.readString();
        this.key = parcel.readString();
    }


    //Method used to check if the information placed by the user during the edit phase are correct
    public String checkInfo(Context context) {

        int counter = 0;
        String correctly = "";
        String message = "";

        //Name check
        Pattern namePatter = Pattern.compile("^[a-z ,.'-]+$", Pattern.CASE_INSENSITIVE);
        Matcher matcherName = namePatter.matcher(name.getValue());
        boolean findName = matcherName.find();
        if(name.getValue().equals(myDefault) || !findName){
            counter++;
            message += " -" + context.getString(R.string.name) + "\n";
            if(!findName && !name.getValue().equals(myDefault)){
                correctly = context.getString(R.string.correctly);
            }
        }

        //surname check
        Pattern surnamePatter = Pattern.compile("^[a-z ,.'-]+$", Pattern.CASE_INSENSITIVE);
        Matcher matcherSurname = surnamePatter.matcher(surname.getValue());
        boolean findSurname = matcherSurname.find();
        if(surname.getValue().equals(myDefault) || !findSurname){
            counter++;
            message += " -" + context.getString(R.string.surname) + "\n";
            if(!findSurname && !surname.getValue().equals(myDefault)){
                correctly = context.getString(R.string.correctly);
            }
        }

        //email check
        Pattern emailPatter = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcherMail = emailPatter.matcher(email.getValue());
        boolean findMail = matcherMail.find();
        if(email.getValue().equals(myDefault) || !findMail){
            counter++;
            message += " -" + context.getString(R.string.mail) + "\n";
            if(!findMail && !email.getValue().equals(myDefault)){
                correctly = context.getString(R.string.correctly);
            }
        }

        if(cap.getValue().equals(myDefault)){
            counter++;
            message += " -" + context.getString(R.string.cap) + "\n";
        }

        if(city.getValue().equals(myDefault)){
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
        String email = this.email.getValue();
        this.email = new MyPair(email,checkMail);
    }

    public void setCheckStreet(String checkStreet) {
        String street = this.street.getValue();
        this.street = new MyPair(street,checkStreet);
    }

    public void setCheckPhone(String checkPhone) {
        String phone = this.phone.getValue();
        this.phone = new MyPair(phone,checkPhone);
    }

    //Methods used to check if an attribute is public(return true) or private(return false)

    public boolean checkMail(){
        if(this.email.getStatus().equals("public")){
            return true;
        }
        return false;
    }

    public boolean checkStreet(){
        if(this.street.getStatus().equals("public")){
            return true;
        }
        return false;
    }

    public boolean checkPhone(){
        if(this.phone.getStatus().equals("public")){
            return true;
        }
        return false;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean checkMailFormat(String mail) {

        Pattern emailPatter = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
        Matcher matcherMail = emailPatter.matcher(mail);
        boolean findMail = matcherMail.find();
        if(mail.equals(myDefault) || !findMail){

            return false;
        }

        return true;
    }


    public static class MyPair{

        private String value;
        private String status;

        MyPair(){
            status = "public";
            value="";
        }

        MyPair(String value,String status){
            this.value = value;
            this.status = status;
        }

        public MyPair(MyPair name) {
            this.value = new String(name.getValue());
            this.status = new String(name.getStatus());
        }

        public String getStatus() {
            return status;
        }


        public void setStatus(String status) {
            this.status = status;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }


    }

}
