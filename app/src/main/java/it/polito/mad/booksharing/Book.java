package it.polito.mad.booksharing;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by sergiogentile on 07/04/18.
 */

public class Book implements Parcelable {

    private String title, author, year, urlImage, urlMyImage, owner, isbn10, isbn13, publisher, description, rating, date, subtitle, city, street, cap;
    private boolean available;
    private String ownerName;
    private Double distance;

    public Book() {

    }

    public Book(String title, String subtitle, String author, String year, String publisher, String description , String urlImage, String urlMyImage, String owner, String isbn10, String isbn13, String rating, boolean available, String city, String street, String cap, String ownerName) {
        this.title = title.toLowerCase().trim();
        this.subtitle = subtitle;
        this.author = author.toLowerCase().trim();
        this.year = year;
        this.urlImage = urlImage;
        this.urlMyImage = urlMyImage;
        this.owner = owner;
        this.isbn10 = isbn10;
        this.isbn13 = isbn13;
        this.publisher = publisher.toLowerCase().trim();
        this.description = description;
        this.rating = rating;
        this.available = available;
        this.city = city.toLowerCase().trim();
        this.street = street;
        this.cap = cap;
        this.ownerName = ownerName.toLowerCase().trim();
        Date dateCalendar = Calendar.getInstance().getTime();
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        date = formatter.format(dateCalendar);
    }

    public Boolean isAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getPublisher() {
        return publisher.toLowerCase().trim();
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher.toLowerCase().trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrlMyImage() {
        return urlMyImage;
    }

    public void setUrlMyImage(String urlMyImage) {
        this.urlMyImage = urlMyImage;
    }

    public String getIsbn10() {
        return isbn10;
    }

    public void setIsbn10(String isbn10) {
        this.isbn10 = isbn10;
    }

    public String getIsbn13() {
        return isbn13;
    }

    public void setIsbn13(String isbn13) {
        this.isbn13 = isbn13;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title.toLowerCase().trim();
    }

    public void setTitle(String title) {
        this.title = title.toLowerCase().trim();
    }

    public String getAuthor() {
        return author.toLowerCase().trim();
    }

    public void setAuthor(String author) {
        this.author = author.toLowerCase().trim();
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }

    // Parcelling part
    public Book(Parcel in) {
        String[] data = new String[17];


        in.readStringArray(data);
        this.available = in.readByte() != 0;
        // the order needs to be the same as in writeToParcel() method
        this.title = data[0];
        this.subtitle = data[1];
        this.author = data[2];
        this.year = data[3];
        this.publisher = data[4];
        this.description = data[5];
        this.urlImage = data[6];
        this.urlMyImage = data[7];
        this.owner = data[8];
        this.isbn10 = data[9];
        this.isbn13 = data[10];
        this.rating = data[11];
        this.date = data[12];
        this.city = data[13];
        this.street = data[14];
        this.cap = data[15];
        this.ownerName = data[16];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{this.title,
                this.subtitle,
                this.author,
                this.year,
                this.publisher,
                this.description,
                this.urlImage,
                this.urlMyImage,
                this.owner,
                this.isbn10,
                this.isbn13,
                this.rating,
                this.date, this.city, this.street, this.cap, this.ownerName});
        dest.writeByte((byte) (available ? 1 : 0));
    }

    public static final Creator CREATOR = new Creator() {
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public Double getDistance(){
        return this.distance;
    }

    public void setDistance(double distance){
        this.distance = new Double(distance);
    }

    public String getOwnerName() {
        return ownerName.toLowerCase().trim();
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName.toLowerCase().trim();
    }

    public String getCity() {
        if(city == null){
            return "";
        }
        return city.toLowerCase().trim();
    }

    public void setCity(String city) {
        this.city = city.toLowerCase().trim();
    }

    public String getStreet() {
        if(street == null){
            return "";
        }
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCap() {
        if(cap == null){
            return "";
        }
        return cap;
    }

    public void setCap(String cap) {
        this.cap = cap;
    }
}
