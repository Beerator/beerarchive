package com.rjmoseley.beerator.app;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by richmose on 12/06/14.
 */
public class Globals {
    private static Globals instance;

    private ArrayList<Beer> beerList;
    private List<Country> countries;

    private Globals() {
    }

    public void setBeerlist(ArrayList<Beer> bl) {
        this.beerList = bl;
    }

    public ArrayList<Beer> getBeerList() {
        return this.beerList;
    }

    public void setCountries(List<Country> countries) {
        this.countries = countries;
    }

    public List<Country> getCountries() {
        return this.countries;
    }

    public static synchronized Globals getInstance() {
        if (instance == null) {
            instance = new Globals();
        }
        return instance;
    }
}
