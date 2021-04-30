package com.iceybones.scheduler.application.model;

public class Contact {
    private final String contactName;
    private final int contactId;

    public Contact(String contactName, int contactId) {
        this.contactName = contactName;
        this.contactId = contactId;
    }
    public String getContactName() {
        return contactName;
    }

    public int getContactId() {
        return contactId;
    }

    @Override
    public String toString() {
        return contactName;
    }
}
