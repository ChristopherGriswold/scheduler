package com.iceybones.scheduler.models;

import java.util.Objects;

/**
 * Used to store data pertaining to an individual.
 */
public class Contact {

  private final String contactName;
  private final int contactId;

  /**
   * Constructs a new <code>Contact</code> object with the given parameters.
   *
   * @param contactName the name of the <code>Contact</code>
   * @param contactId   the ID of the <code>Contact</code>
   */
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Contact)) {
      return false;
    }
    Contact contact = (Contact) o;
    return contactId == contact.contactId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(contactId);
  }
}
