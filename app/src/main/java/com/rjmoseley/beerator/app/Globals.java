package com.rjmoseley.beerator.app;

import java.util.ArrayList;

/**
 * Created by richmose on 12/06/14.
 */
public class Globals {
    private static Globals instance;

    private ArrayList<Beer> beerList;

    private Globals() {
    }

    public void setBeerlist(ArrayList<Beer> bl) {
        this.beerList = bl;
    }

    public ArrayList<Beer> getBeerList() {
        return this.beerList;
    }

    public static synchronized Globals getInstance() {
        if (instance == null) {
            instance = new Globals();
        }
        return instance;
    }
}
