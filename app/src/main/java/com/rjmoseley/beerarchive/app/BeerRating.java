package com.rjmoseley.beerarchive.app;

import java.util.Date;

/**
 * Object to hold beer ratings
 */
public class BeerRating {

    String element1 = null;
    String element2 = null;
    Date date = null;

    public BeerRating(String element1, String element2, Date date) {
        super();
        this.element1 = element1;
        this.element2 = element2;
        this.date = date;
    }

    public String getElement1() {
        return element1;
    }

    public String getElement2() {
        return element2;
    }

    public Date getDate() {
        return date;
    }

    public String toString(){
        return element1 + " " + element2;
    }
}
