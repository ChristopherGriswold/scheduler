package com.iceybones.scheduler.application.controller;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Random;

public class Database {
    private static final String URL = "jdbc:mysql://wgudb.ucertify.com:3306/WJ07c5c";
    private static final String USERNAME = "U07c5c";
    private static final String PASSWORD = "53688987983";
    private static final String AUTH_STRING = "SELECT count(*) FROM users WHERE User_Name = ? AND Password = ?";
    private static Long authToken = null;

    public enum QueryType {
        SELECT, UPDATE, DELETE
    }
    public static class NotAuthorizedException extends Exception {

    }

    public static Long authenticate(String userName, String password) throws SQLException {
        try (var conn = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            var sql = conn.prepareStatement(AUTH_STRING);
            sql.setString(1, userName);
            sql.setString(2, password);
            var rs = sql.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                authToken = new Random().nextLong();
            } else {
                authToken = null;
            }
        }
        return authToken;
    }
//    public static  submitQuery(String query, Long authToken) throws NotAuthorizedException {
//
//        if (Database.authToken != null && authToken != null && authToken.compareTo(Database.authToken) != 0) {
//            throw new NotAuthorizedException();
//        }
//        try (var conn = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
//            var sql = conn.prepareStatement(query);
//            ResultSet rs = sql.executeQuery();
//        } catch (SQLException e) {
//            System.out.println("Error communicating with database.");
//            return null;
//        }
//    }
}