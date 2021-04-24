package com.iceybones.scheduler.application.model;

import java.time.ZonedDateTime;

public class Customer {
    private int customerId;
    private String customerName;
    private String address;
    private String postalCode;
    private String phone;
    private ZonedDateTime createDate;
    private String createdBy;
    private ZonedDateTime lastUpdate;
    private String lastUpdatedBy;
    private int divisionId;

//    Table Representation
//    Customer_ID INT(10) (PK)
//    Customer_Name VARCHAR(50)
//    Address VARCHAR(100)
//    Postal_Code VARCHAR(50)
//    Phone VARCHAR(50)
//    Create_Date DATETIME
//    Created_By VARCHAR(50)
//    Last_Update TIMESTAMP
//    Last_Updated_By VARCHAR(50)
//    Division_ID INT(10) (FK)
}
