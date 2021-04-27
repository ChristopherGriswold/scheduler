package com.iceybones.scheduler.application.model;

import java.sql.Timestamp;

public class Customer implements Comparable<Customer>{
    private int customerId;
    private String customerName;
    private String address;
    private String postalCode;
    private String division;
    private int divisionId;
    private String country;
    private String phone;
    private Timestamp createDate;
    private String createdBy;
    private Timestamp lastUpdate;
    private String lastUpdatedBy;

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


    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public int getDivisionId() { return divisionId; }

    public void setDivisionId(int divisionId) { this.divisionId = divisionId; }

    public String getCountry() { return country; }

    public void setCountry(String country) { this.country = country; }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) { this.phone = phone; }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Timestamp getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Timestamp lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    @Override
    public int compareTo(Customer other) {
        return this.getCustomerId() - other.getCustomerId();
    }

    @Override
    public String toString() {
        return customerId + " : " + customerName;
    }
}
