package com.iceybones.scheduler.application.controller;

import com.iceybones.scheduler.application.model.Appointment;
import com.iceybones.scheduler.application.model.Customer;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class Database {
    private static final String URL = "jdbc:mysql://wgudb.ucertify.com:3306/WJ07c5c";
    private static final String USERNAME = "U07c5c";
    private static final String PASSWORD = "53688987983";
    private static final String AUTH_STRING = "SELECT count(*) FROM users WHERE User_Name = ? AND Password = ?";
    private static final String GET_CUST_SQL =
            "SELECT c.Customer_ID, c.Customer_Name, c.Address, c.Postal_Code, f.Division_ID, " +
                    "f.Division , t.Country, c.Phone\n" +
                    "FROM customers c\n" +
                    "LEFT JOIN first_level_divisions f\n" +
                    "ON c.Division_ID = f.Division_ID\n" +
                    "LEFT JOIN countries t\n" +
                    "ON f.COUNTRY_ID = t.Country_ID";
    private static final String GET_APP_SQL =
            "SELECT a.Appointment_ID, a.Title, a.Description, a.Location, " +
                    "c.Contact_Name, a.Type, a.Start, a.End, a.Customer_ID\n" +
                    "FROM appointments a\n" +
                    "LEFT JOIN contacts c\n" +
                    "ON a.Contact_ID = c.Contact_ID\n" +
                    "ORDER BY a.Start";
    private static final String INSERT_CUST_SQL = "INSERT INTO customers\n" +
            "(Customer_ID, Customer_Name, Address, Postal_Code, Phone, Create_Date, " +
            "Created_By, Last_Update, Last_Updated_By, Division_ID)\n" +
            "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?, ?)";
    private static final String UPDATE_CUST_SQL = "UPDATE customers\n" +
            "SET Customer_Name = ?, Address = ?, Postal_Code = ?, Phone = ?, " +
            "Last_Update = CURRENT_TIMESTAMP, Last_Updated_By = ?, Division_ID = ?\n" +
            "WHERE Customer_ID = ?";
    private static final String DELETE_CUST_SQL = "DELETE FROM customers WHERE Customer_ID = ?";
    private static final String GET_COUNTRY_SQL = "SELECT c.Country, d.Division_ID, d.Division\n" +
            "FROM countries c\n" +
            "JOIN first_level_divisions d\n" +
            "ON c.Country_ID = d.Country_ID\n" +
            "ORDER BY c.Country";
    private static Long authToken = null;
    private static String user;

    public static String getUser() {
        return user;
    }


    public static class NotAuthorizedException extends Exception {
        public NotAuthorizedException() {
            super();
        }

        public NotAuthorizedException(String message) {
            super(message);
        }
    }

    public static boolean authenticate(String userName, String password) throws SQLException {
        try (var conn = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            var sql = conn.prepareStatement(AUTH_STRING);
            sql.setString(1, userName);
            sql.setString(2, password);
            var rs = sql.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                authToken = new Random().nextLong();
                user = userName;
                return true;
            } else {
                authToken = null;
            }
        }
        return false;
    }

    private static boolean checkAuthToken(Long t) {
        return t == null || authToken == null || (t.compareTo(authToken) != 0);
    }

    public static List<Customer> getCustomers() throws NotAuthorizedException, SQLException {
        if (checkAuthToken(authToken)) {
            throw new NotAuthorizedException("User does not have authorization to perform this operation.");
        }
        List<Customer> customers = new ArrayList<>();
        try (var conn = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            var sql = conn.prepareStatement(GET_CUST_SQL);
            var rs = sql.executeQuery();
            while (rs.next()) {
                var cust = new Customer();
                cust.setCustomerId(rs.getInt(1));
                cust.setCustomerName(rs.getString(2));
                cust.setAddress(rs.getString(3));
                cust.setPostalCode(rs.getString(4));
                cust.setDivisionId(rs.getInt(5));
                cust.setDivision(rs.getString(6));
                cust.setCountry(rs.getString(7));
                cust.setPhone(rs.getString(8));
                customers.add(cust);
            }
        }
        return customers;
    }

    public static boolean insertCustomer(Customer customer) throws NotAuthorizedException, SQLException {
        if (checkAuthToken(authToken)) {
            throw new NotAuthorizedException("User does not have authorization to perform this operation.");
        }
        try (var conn = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            var sql = conn.prepareStatement(INSERT_CUST_SQL);
            sql.setString(1, Integer.toString(customer.getCustomerId()));
            sql.setString(2, customer.getCustomerName());
            sql.setString(3, customer.getAddress());
            sql.setString(4, customer.getPostalCode());
            sql.setString(5, customer.getPhone());
            sql.setString(6, customer.getCreatedBy());
            sql.setString(7, customer.getLastUpdatedBy());
            sql.setInt(8, customer.getDivisionId());
            return sql.executeUpdate() == 1;
        }
    }

    public static boolean updateCustomer(Customer customer) throws NotAuthorizedException, SQLException {
        if (checkAuthToken(authToken)) {
            throw new NotAuthorizedException("User does not have authorization to perform this operation.");
        }
        try (var conn = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            var sql = conn.prepareStatement(UPDATE_CUST_SQL);
            sql.setString(1, customer.getCustomerName());
            sql.setString(2, customer.getAddress());
            sql.setString(3, customer.getPostalCode());
            sql.setString(4, customer.getPhone());
            sql.setString(5, customer.getLastUpdatedBy());
            sql.setInt(6, customer.getDivisionId());
            sql.setInt(7, customer.getCustomerId());
            System.out.println(sql.toString());
            return sql.executeUpdate() == 1;
        }
    }

    public static boolean deleteCustomer(Customer customer) throws NotAuthorizedException, SQLException {
        if (checkAuthToken(authToken)) {
            throw new NotAuthorizedException("User does not have authorization to perform this operation.");
        }
        try (var conn = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            var sql = conn.prepareStatement(DELETE_CUST_SQL);
            sql.setInt(1, customer.getCustomerId());
            return sql.executeUpdate() == 1;
        }
    }

    public static List<Appointment> getAppointments() throws NotAuthorizedException, SQLException {
        if (checkAuthToken(authToken)) {
            throw new NotAuthorizedException("User does not have authorization to perform this operation.");
        }
        List<Appointment> appointments = new ArrayList<>();
        try (var conn = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            var sql = conn.prepareStatement(GET_APP_SQL);
            var rs = sql.executeQuery();
            while (rs.next()) {
                var app = new Appointment();
                app.setAppointmentId(rs.getInt(1));
                app.setTitle(rs.getString(2));
                app.setDescription(rs.getString(3));
                app.setLocation(rs.getString(4));
                app.setContact(rs.getString(5));
                app.setType(rs.getString(6));
                app.setStart(rs.getTimestamp(7));
                app.setEnd(rs.getTimestamp(8));
                app.setContactId(rs.getInt(9));
                appointments.add(app);
            }
        }
        return appointments;
    }

    public static Map<String, Map<String, Integer>> getDivisions() throws NotAuthorizedException, SQLException {
        if (checkAuthToken(authToken)) {
            throw new NotAuthorizedException("User does not have authorization to perform this operation.");
        }
        Map<String, Map<String, Integer>> divisions = new HashMap<>();
        try (var conn = DriverManager.getConnection(URL, USERNAME, PASSWORD)) {
            var sql = conn.prepareStatement(GET_COUNTRY_SQL);
            var rs = sql.executeQuery();
            while (rs.next()) {
                if (!divisions.containsKey(rs.getString(1))) {
                    var list = new HashMap<String, Integer>();
                    list.put(rs.getString(3), rs.getInt(2));
                    divisions.put(rs.getString(1), list);
                } else {
                    divisions.get(rs.getString(1)).put(rs.getString(3), rs.getInt(2));
                }
            }
        }
        return divisions;
    }
}
