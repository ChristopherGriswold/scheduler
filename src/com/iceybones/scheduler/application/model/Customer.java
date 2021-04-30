package com.iceybones.scheduler.application.model;

import java.time.ZonedDateTime;
import java.util.Objects;

public class Customer implements Comparable<Customer>{
    private int customerId;
    private String customerName;
    private String address;
    private String postalCode;
    private String phone;
    private Division division;
    private ZonedDateTime createDate;
    private ZonedDateTime lastUpdate;
    private User createdBy;
    private User lastUpdatedBy;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer)) return false;
        Customer customer = (Customer) o;
        return customerId == customer.customerId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId);
    }

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

    public Division getDivision() {
        return division;
    }

    public Country getCountry() { return division.getCountry(); }

    public void setDivision(Division division) {
        this.division = division;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) { this.phone = phone; }

    public ZonedDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(ZonedDateTime createDate) {
        this.createDate = createDate;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public ZonedDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(ZonedDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public User getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(User lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    @Override
    public int compareTo(Customer other) {
        return this.getCustomerId() - other.getCustomerId();
    }
}
