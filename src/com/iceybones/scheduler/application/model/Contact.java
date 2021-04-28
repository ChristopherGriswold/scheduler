package com.iceybones.scheduler.application.model;

public class Contact {
    private int contactId;
    private String contactName;
    private String email;

//    Table Representation
//    Contact_ID INT(10) (PK)
//    Contact_Name VARCHAR(50)
//    Email VARCHAR(50)


    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
