package com.iceybones.scheduler.application.model;

import java.time.ZonedDateTime;

public class FirstLevelDivision {
    private int divisionId;
    private String division;
    private ZonedDateTime createDate;
    private String createdBy;
    private ZonedDateTime lastUpdate;
    private String lastUpdatedBy;
    private int countryId;

//    Table Representation
//    Division_ID INT(10) (PK)
//    Division VARCHAR(50)
//    Create_Date DATETIME
//    Created_By VARCHAR(50)
//    Last_Update TIMESTAMP
//    Last_Updated_By VARCHAR(50)
//    Country_ID INT(10) (FK)
}
