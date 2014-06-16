package com.rjmoseley.beerarchive.app;

/**
 * Created by richmose on 11/06/14.
 */
public class Beer {

    String name = null;
    String brewery = null;
    String objectId = null;
    String abv = null;

    public Beer(String name, String brewery, String objectId) {
        super();
        this.name = name;
        this.brewery = brewery;
        this.objectId = objectId;
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
