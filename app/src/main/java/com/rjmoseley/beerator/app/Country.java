package com.rjmoseley.beerator.app;

import android.widget.Adapter;

/**
 * Class to hold country details
 */
class Country {

    private String code;

    private String name;

    Country(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getCode() {
        return this.code;
    }

    public String toString() {
        return name + " (" + code + ")";
    }
}
