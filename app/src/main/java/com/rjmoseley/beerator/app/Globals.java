package com.rjmoseley.beerator.app;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by richmose on 12/06/14.
 */
public class Globals {
    private static Globals instance;

    private ArrayList<Beer> globalBeerList;
    private List<Country> countries;

    private Globals() {
    }

    public void setBeerList(ArrayList<Beer> bl) {
        this.globalBeerList = bl;
    }

    public ArrayList<Beer> getBeerList() {
        return this.globalBeerList;
    }

    public void setCountries(List<Country> countries) {
        this.countries = countries;
    }

    public List<Country> getCountries() {
        return this.countries;
    }

    //Get database names
    public String getBeerDatabase() {
        return "Beer";
    }
    public String getBeerRatingsDatabase() {
        return "BeerRating";
    }

    public static synchronized Globals getInstance() {
        if (instance == null) {
            instance = new Globals();
        }
        return instance;
    }
}
