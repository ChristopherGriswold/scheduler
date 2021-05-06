package com.iceybones.scheduler.models;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Appointment {

  private int appointmentId;
  private String title;
  private String description;
  private String location;
  private String type;
  private ZonedDateTime start;
  private ZonedDateTime end;
  private User createdBy;
  private User lastUpdatedBy;
  private User user;
  private Customer customer;
  private Contact contact;

  public int getAppointmentId() {
    return appointmentId;
  }

  public void setAppointmentId(int appointmentId) {
    this.appointmentId = appointmentId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public ZonedDateTime getStart() {
    return start;
  }

  public void setStart(ZonedDateTime start) {
    this.start = start;
  }

  public ZonedDateTime getEnd() {
    return end;
  }

  public void setEnd(ZonedDateTime end) {
    this.end = end;
  }

  public User getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(User createdBy) {
    this.createdBy = createdBy;
  }

  public User getLastUpdatedBy() {
    return lastUpdatedBy;
  }

  public void setLastUpdatedBy(User lastUpdatedBy) {
    this.lastUpdatedBy = lastUpdatedBy;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public Customer getCustomer() {
    return customer;
  }

  @SuppressWarnings("unused")
  public int getCustomerId() { return customer.getCustomerId(); }

  public void setCustomer(Customer customer) {
    this.customer = customer;
  }

  public Contact getContact() {
    return contact;
  }

  public void setContact(Contact contact) {
    this.contact = contact;
  }

  @Override
  public String toString() {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a");
    return appointmentId + " - " + title + " - " + start.withZoneSameInstant(ZoneId.systemDefault())
        .format(dtf)
        + " to " + end.withZoneSameInstant(ZoneId.systemDefault()).format(dtf);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Appointment)) {
      return false;
    }
    Appointment that = (Appointment) o;
    return appointmentId == that.appointmentId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(appointmentId);
  }
}
