package com.rjmoseley.beerator.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by richmose on 11/06/14.
 */
public class Beer {

    String name = null;
    String brewery = null;
    String objectId = null;
    String abv = null;
    Country country = null;
    ArrayList<BeerRating> ratingsList = null;
    ArrayList<BeerRating> myRatingsList = null;

    public Beer(String name, String brewery, String objectId) {
        super();
        this.name = name;
        this.brewery = brewery;
        this.objectId = objectId;
        this.ratingsList = new ArrayList<BeerRating>();
        this.myRatingsList = new ArrayList<BeerRating>();
    }

    public String getName() {
        return name;
    }

    public String getBrewery(){
        return brewery;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setABV(String abv) {
        this.abv = abv;
    }

    public String getABV() {
        return this.abv;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Country getCountry() {
        return this.country;
    }

    public void addRating(BeerRating beerRating) {
        ratingsList.add(beerRating);
    }

    public void addMyRating(BeerRating beerRating) {
        myRatingsList.add(beerRating);
    }

    public ArrayList<BeerRating> getRatingsList() {
        return ratingsList;
    }

    public ArrayList<BeerRating> getMyRatingsList() {
        return myRatingsList;
    }

    public void clearRatings() {
        ratingsList.clear();
        myRatingsList.clear();
    }

    public void sortRatings() {
        Collections.sort(ratingsList, new Comparator<BeerRating>() {
            @Override
            public int compare(BeerRating beerRating, BeerRating beerRating2) {
                return beerRating2.getDate().compareTo(beerRating.getDate());
            }
        });
        Collections.sort(myRatingsList, new Comparator<BeerRating>() {
            @Override
            public int compare(BeerRating beerRating, BeerRating beerRating2) {
                return beerRating2.getDate().compareTo(beerRating.getDate());
            }
        });
    }

    public String get(String key) {
        if (key == "name") {
            return name;
        }
        else if (key == "brewery") {
            return brewery;
        }
        else if (key == "objectId") {
            return objectId;
        }
        else {
            return "Failed to find key";
        }
    }

    @Override
    public String toString() {
        return brewery + " " + name + " " + objectId;
    }
}
