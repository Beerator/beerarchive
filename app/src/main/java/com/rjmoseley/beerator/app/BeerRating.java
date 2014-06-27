package com.rjmoseley.beerator.app;

import com.parse.ParseGeoPoint;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Object to hold beer ratings
 */
public class BeerRating {

    String rating = null;
    Date date = null;

    String objectId = null;
    String userObjectId = null;
    String userName = null;
    ParseGeoPoint location = null;

    public BeerRating(String rating, String system, Date date) {
        super();
        String normRating = null;
        if (system.equals("1-5+")) {
            if (rating.equals("5+")) normRating = "100";
            else if (rating.equals("5 ")) normRating = "93";
            else if (rating.equals("5-")) normRating = "86";
            else if (rating.equals("4+")) normRating = "79";
            else if (rating.equals("4 ")) normRating = "72";
            else if (rating.equals("4-")) normRating = "65";
            else if (rating.equals("3+")) normRating = "58";
            else if (rating.equals("3 ")) normRating = "51";
            else if (rating.equals("3-")) normRating = "44";
            else if (rating.equals("2+")) normRating = "37";
            else if (rating.equals("2 ")) normRating = "30";
            else if (rating.equals("2-")) normRating = "23";
            else if (rating.equals("1+")) normRating = "16";
            else if (rating.equals("1 ")) normRating = "9";
            else if (rating.equals("1-")) normRating = "2";
            else normRating = "0";
        } else normRating = "0";
        this.rating = normRating;
        this.date = date;
    }

    public BeerRating(String normRating, Date date) {
        this.rating = normRating;
        this.date = date;
    }

    public BeerRating(String normRating, Date date, String objectId, String userObjectId,
                      String userName, ParseGeoPoint geoPoint) {
        this.rating = normRating;
        this.date = date;
        this.objectId = objectId;
        this.userObjectId = userObjectId;
        this.userName = userName;
        this.location = geoPoint;
    }

    public String getRating(String system) {
        String out = null;
        if (system.equals("1-5+")) {
            int i = Integer.parseInt(rating);
            if (i > 96) out = "5+";
            else if (i > 89) out = "5 ";
            else if (i > 82) out = "5-";
            else if (i > 75) out = "4+";
            else if (i > 68) out = "4 ";
            else if (i > 61) out = "4-";
            else if (i > 54) out = "3+";
            else if (i > 47) out = "3 ";
            else if (i > 40) out = "3-";
            else if (i > 33) out = "2+";
            else if (i > 26) out = "2 ";
            else if (i > 19) out = "2-";
            else if (i > 12) out = "1+";
            else if (i > 5) out = "1 ";
            else out = "1-";
        } else if (system.equals("1-10")) {
            float f = Float.parseFloat(rating);
            f = f/10;
            f = Math.round(f*2)/2;
            out = String.format("%.1f", f);
        } else {
            out = "Unrecognised rating system";
        }
        return out;
    }

    public String getNormRating() {
        return rating;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setUserObjectId(String userObjectId) {
        this.userObjectId = userObjectId;
    }

    public String getUserObjectId() {
        return userObjectId;
    }

    public void setUserName(String name) {
        this.userName = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setLocation(ParseGeoPoint geoPoint) {
        this.location = geoPoint;
    }

    public ParseGeoPoint getLocation() {
        return location;
    }

    public Date getDate() {
        return date;
    }

    public String toString(){
        return rating;
    }
}
