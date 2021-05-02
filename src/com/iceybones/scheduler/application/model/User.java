package com.iceybones.scheduler.application.model;

import java.util.Objects;

public class User {
    private final String userName;
    private final int userId;

    public User(String userName, int userId) {
        this.userName = userName;
        this.userId = userId;
    }

//    Table representation
//    User_ID INT(10) (PK)
//    User_Name VARCHAR(50) (UNIQUE)
//    Password TEXT
//    Create_Date DATETIME
//    Created_By VARCHAR(50)
//    Last_Update TIMESTAMP
//    Last_Updated_By VARCHAR(50)

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
