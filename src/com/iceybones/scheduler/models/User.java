package com.iceybones.scheduler.models;

import java.util.Objects;

/**
 * Used to store data pertaining to an individual.
 */
public class User {

  private final String userName;
  private final int userId;

  /**
   * Constructs a new <code>User</code> object with the given parameters.
   *
   * @param userName the name of the <code>User</code>
   * @param userId   the ID of the <code>User</code>
   */
  public User(String userName, int userId) {
    this.userName = userName;
    this.userId = userId;
  }

  public String getUserName() {
    return userName;
  }

  public int getUserId() {
    return userId;
  }

  @Override
  public String toString() {
    return userName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof User)) {
      return false;
    }
    User user = (User) o;
    return userId == user.userId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId);
  }
}
