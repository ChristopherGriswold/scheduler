package com.iceybones.scheduler.application.model;
import java.time.ZonedDateTime;

public class Appointment {
    private int appointmentId;
    private String title;
    private String description;
    private String location;
    private String type;
    private ZonedDateTime start;
    private ZonedDateTime end;
    private ZonedDateTime createDate;
    private String createdBy;
    private ZonedDateTime lastUpdate;
    private String lastUpdatedBy;
    private int customerId;
    private int userId;
    private int contactId;

//    Table Representation
//    Appointment_ID INT(10) (PK)
//    Title VARCHAR(50)
//    Description VARCHAR(50)
//    Location VARCHAR(50)
//    Type VARCHAR(50)
//    Start DATETIME
//    End DATETIME
//    Create_Date DATETIME
//    Created_By VARCHAR(50)
//    Last_Update TIMESTAMP
//    Last_Updated_By VARCHAR(50)
//    Customer_ID INT(10) (FK)
//    User_ID INT(10) (FK)
//    Contact_ID INT(10) (FK)
}
