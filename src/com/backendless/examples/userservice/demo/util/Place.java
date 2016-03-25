package com.backendless.examples.userservice.demo.util;

import java.util.Date;
import java.util.UUID;

/**
 * Created by happy on 19.03.2016.
 */
public class Place {

    private String geoId;

    private String userEmail;

    private String description;

    private double longitude;

    private double latitude;

    private Date addDate;

    private String urlPath;

    public Place() {
    }

    public String getGeoId() {
        return geoId;
    }

    public void setGeoId(String geoId) {
        this.geoId = geoId;
    }

    public Place(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getAddDate() {
        return addDate;
    }

    public void setAddDate(Date addDate) {
        this.addDate = addDate;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String toStringGeo() {
        return "Latitude: " + latitude + "\nLongitude: " + longitude;
    }

    @Override
    public String toString() {
        return "Place{" +
                " userEmail='" + userEmail + '\'' +
                ", description='" + description + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", addDate=" + addDate +
                ", urlPath='" + urlPath + '\'' +
                '}';
    }
}
