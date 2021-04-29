package com.iceybones.scheduler.application.controller;

import com.iceybones.scheduler.application.model.Appointment;
import com.iceybones.scheduler.application.model.Contact;
import com.iceybones.scheduler.application.model.Customer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
    private static final String URL = "jdbc:mysql://wgudb.ucertify.com:3306/WJ07c5c";
    private static final String USERNAME = "U07c5c";
    private static final String PASSWORD = "53688987983";
    private static final String LOGIN_SQL = "SELECT count(*) FROM users WHERE User_Name = ? AND Password = ?";
    private static final String GET_CUST_SQL =
            "SELECT c.Customer_ID, c.Customer_Name, c.Address, c.Postal_Code, f.Division_ID, " +
                    "f.Division , t.Country, c.Phone, c.Create_Date, c.Last_Update, c.Created_By\n" +
                    "FROM customers c\n" +
                    "LEFT JOIN first_level_divisions f\n" +
                    "ON c.Division_ID = f.Division_ID\n" +
                    "LEFT JOIN countries t\n" +
                    "ON f.COUNTRY_ID = t.Country_ID\n" +
                    "ORDER BY c.Customer_ID";
    private static final String GET_APPS_SQL =
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
    private static final String GET_DIVISIONS_SQL = "SELECT c.Country, d.Division_ID, d.Division\n" +
            "FROM countries c\n" +
            "JOIN first_level_divisions d\n" +
            "ON c.Country_ID = d.Country_ID\n" +
            "ORDER BY c.Country";
    private static final String GET_CONTACTS_SQL = "SELECT * FROM contacts";
    private static Connection connection;
    private static String user;

    static {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (!connection.isValid(3)) {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            connection.setAutoCommit(false);
        }
        return connection;
    }

    public static String getUser() {
        return user;
    }

    public static boolean authenticate(String userName, String password) throws SQLException {
        var sql = connection.prepareStatement(LOGIN_SQL);
        sql.setString(1, userName);
        sql.setString(2, password);
        var rs = sql.executeQuery();
        rs.next();
        if (rs.getInt(1) > 0) {
            user = userName;
            return true;
        } else {
            return false;
        }
    }

    public static List<Contact> getContacts() throws SQLException {
        List<Contact> contacts = new ArrayList<>();
        var sql = getConnection().prepareStatement(GET_CONTACTS_SQL);
        var rs = sql.executeQuery();
        while (rs.next()) {
            var contact = new Contact();
            contact.setContactId(rs.getInt(1));
            contact.setContactName(rs.getString(2));
            contact.setEmail(rs.getString(3));
            contacts.add(contact);
        }
        return contacts;
    }

    public static List<Customer> getCustomers() throws SQLException {
        List<Customer> customers = new ArrayList<>();
        var sql = getConnection().prepareStatement(GET_CUST_SQL);
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
            cust.setCreateDate(rs.getTimestamp(9).toLocalDateTime().atZone(ZoneId.systemDefault()).
                    withZoneSameInstant(ZoneId.of("UTC")));
            cust.setLastUpdate(rs.getTimestamp(10).toLocalDateTime().atZone(ZoneId.systemDefault()).
                    withZoneSameInstant(ZoneId.of("UTC")));
            cust.setCreatedBy(rs.getString(11));
            customers.add(cust);
        }
        return customers;
    }

    public static void insertCustomer(Customer customer) throws SQLException {
        commit();
        var sql = getConnection().prepareStatement(INSERT_CUST_SQL);
        sql.setString(1, Integer.toString(customer.getCustomerId()));
        sql.setString(2, customer.getCustomerName());
        sql.setString(3, customer.getAddress());
        sql.setString(4, customer.getPostalCode());
        sql.setString(5, customer.getPhone());
        sql.setString(6, customer.getCreatedBy());
        sql.setString(7, customer.getLastUpdatedBy());
        sql.setInt(8, customer.getDivisionId());
        sql.executeUpdate();
    }

    public static void updateCustomer(Customer customer) throws SQLException {
        commit();
        var sql = getConnection().prepareStatement(UPDATE_CUST_SQL);
        sql.setString(1, customer.getCustomerName());
        sql.setString(2, customer.getAddress());
        sql.setString(3, customer.getPostalCode());
        sql.setString(4, customer.getPhone());
        sql.setString(5, customer.getLastUpdatedBy());
        sql.setInt(6, customer.getDivisionId());
        sql.setInt(7, customer.getCustomerId());
        sql.executeUpdate();
    }

    public static void deleteCustomer(Customer customer) throws SQLException {
        commit();
        var sql = getConnection().prepareStatement(DELETE_CUST_SQL);
        sql.setInt(1, customer.getCustomerId());
        sql.executeUpdate();
    }

    public static List<Appointment> getAppointments() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        var sql = getConnection().prepareStatement(GET_APPS_SQL);
        var rs = sql.executeQuery();
        while (rs.next()) {
            var app = new Appointment();
            app.setAppointmentId(rs.getInt(1));
            app.setTitle(rs.getString(2));
            app.setDescription(rs.getString(3));
            app.setLocation(rs.getString(4));
            app.setContact(rs.getString(5));
            app.setType(rs.getString(6));
            app.setStart(rs.getTimestamp(7).toLocalDateTime().atZone(ZoneId.systemDefault()).
                    withZoneSameInstant(ZoneId.of("UTC")));
            app.setEnd(rs.getTimestamp(8).toLocalDateTime().atZone(ZoneId.systemDefault()).
                    withZoneSameInstant(ZoneId.of("UTC")));
            app.setCustomerId(rs.getInt(9));
            appointments.add(app);
        }
        return appointments;
    }

    public static Map<String, Map<String, Integer>> getDivisions() throws SQLException {
        Map<String, Map<String, Integer>> divisions = new HashMap<>();
        var sql = getConnection().prepareStatement(GET_DIVISIONS_SQL);
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
        return divisions;
    }

    public static void rollback() throws SQLException {
        getConnection().rollback();
    }

    public static void commit() throws SQLException {
        getConnection().commit();
    }
}
