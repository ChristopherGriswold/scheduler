package com.iceybones.scheduler.application.model;

import java.util.Objects;

public class Country {
    private final String country;
    private final int countryId;

    public Country(String country, int countryId) {
        this.country = country;
        this.countryId = countryId;
    }

//    Table Representation
//    Division_ID INT(10) (PK)
//    Division VARCHAR(50)
//    Create_Date DATETIME
//    Created_By VARCHAR(50)
//    Last_Update TIMESTAMP
//    Last_Updated_By VARCHAR(50)
//    Country_ID INT(10) (FK)

    public String getCountry() {
        return country;
    }

    public int getCountryId() {
        return countryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Country)) return false;
        Country country = (Country) o;
        return countryId == country.countryId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(countryId);
    }

    @Override
    public String toString() {
        return country;
    }
}
