package com.iceybones.scheduler.application.model;

public class Division {
    private final String division;
    private final int divisionId;
    private final Country country;

    public Division(String division, int divisionId, Country country) {
        this.division = division;
        this.divisionId = divisionId;
        this.country = country;
    }

    public String getDivision() {
        return division;
    }

    public int getDivisionId() {
        return divisionId;
    }

    public Country getCountry() {
        return country;
    }

    @Override
    public String toString() {
        return division;
    }
}
