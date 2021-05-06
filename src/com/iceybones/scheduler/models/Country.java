package com.iceybones.scheduler.models;

import java.util.Objects;

public class Country {
    private final String country;
    private final int countryId;

    public Country(String country, int countryId) {
        this.country = country;
        this.countryId = countryId;
    }

    public String getCountry() {
        return country;
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
