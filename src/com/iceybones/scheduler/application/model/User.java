package com.iceybones.scheduler.application.model;
import java.time.ZonedDateTime;

public class User {
    private int userId;
    private String userName;
    private String password;
    private ZonedDateTime createDate;
    private String createdBy;
    private ZonedDateTime lastUpdate;
    private String lastUpdatedBy;

//    Table representation
//    User_ID INT(10) (PK)
//    User_Name VARCHAR(50) (UNIQUE)
//    Password TEXT
//    Create_Date DATETIME
//    Created_By VARCHAR(50)
//    Last_Update TIMESTAMP
//    Last_Updated_By VARCHAR(50)
}
