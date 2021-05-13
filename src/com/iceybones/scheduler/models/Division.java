package com.iceybones.scheduler.models;

/**
 * Used to store data pertaining to a division of a <code>Country</code>.
 */
public class Division {

  private final String division;
  private final int divisionId;
  private final Country country;

  /**
   * Constructs a new <code>Division</code> object with the given parameters.
   *
   * @param division   the name of the <code>Division</code>
   * @param divisionId the ID of the <code>Division</code>
   * @param country    the <code>Country</code> that the <code>Division</code> belongs to
   */
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
