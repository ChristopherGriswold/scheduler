package com.iceybones.scheduler.application.controller;

import com.iceybones.scheduler.application.model.*;
import java.sql.*;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Database {
    private static final String URL = "jdbc:mysql://wgudb.ucertify.com:3306/WJ07c5c";
    private static final String USERNAME = "U07c5c";
    private static final String PASSWORD = "53688987983";
    private static final String LOGIN_SQL = "SELECT User_Name , User_ID FROM users WHERE User_Name = ? AND Password = ?";
    private static final String GET_USERS_SQL = "SELECT User_Name, User_ID FROM users";
    private static final String GET_CONTACTS_SQL = "SELECT Contact_Name, Contact_ID FROM contacts";
    private static final String GET_CUST_SQL = "SELECT Customer_Name, Customer_ID, Address, " +
            "Postal_Code, Division_ID, Phone\n" +
            "FROM customers\n" +
            "ORDER BY Customer_ID";
    private static final String GET_APPS_SQL = "SELECT Title, Appointment_ID, Description, Location, " +
            "Type, Start, End, Customer_ID, Contact_ID\n" +
            "FROM appointments";
    private static final String INSERT_CUST_SQL = "INSERT INTO customers\n" +
            "(Customer_Name, Address, Postal_Code, Phone, Create_Date, " +
            "Created_By, Last_Update, Last_Updated_By, Division_ID)\n" +
            "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?, ?)";
    private static final String UPDATE_CUST_SQL = "UPDATE customers\n" +
            "SET Customer_Name = ?, Address = ?, Postal_Code = ?, Phone = ?, " +
            "Last_Update = CURRENT_TIMESTAMP, Last_Updated_By = ?, Division_ID = ?\n" +
            "WHERE Customer_ID = ?";
    private static final String UPDATE_APP_SQL = "UPDATE appointments\n" +
            "SET (Title, Description, Location, Type, Start, End, Last_Update, Last_Updated_By, " +
            "Customer_ID, User_ID, Contact_ID)\n" +
            "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?)";
    private static final String DELETE_CUST_SQL = "DELETE FROM customers WHERE Customer_ID = ?";
    private static final String DELETE_APP_SQL = "DELETE FROM appointments WHERE Appointment_ID = ?";
    private static final String GET_DIVISIONS_SQL = "SELECT d.Division, d.Division_ID, c.Country, c.Country_ID\n" +
            "FROM countries c\n" +
            "JOIN first_level_divisions d\n" +
            "ON c.Country_ID = d.Country_ID\n" +
            "ORDER BY d.Division_ID";
    private static final String INSERT_APP_SQL = "INSERT INTO appointments " +
            "(Title, Description, Location, Type, Start, End, Create_Date, Created_By, Last_Update, " +
            "Last_Updated_By, Customer_ID, User_ID, Contact_ID)\n" +
            "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?)";
    private static Connection connection;
    private static User connectedUser;
    private static final List<Division> divisions = new ArrayList<>();
    private static final List<Customer> customers = new ArrayList<>();
    private static final List<Appointment> appointments = new ArrayList<>();
    private static final List<User> users = new ArrayList<>();
    private static final List<Contact> contacts = new ArrayList<>();

    static {
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    ///////////////////Common Methods//////////////////////////

    private static Connection getConnection() throws SQLException {
        if (!connection.isValid(3)) {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            connection.setAutoCommit(false);
        }
        return connection;
    }

    public static void rollback() throws SQLException {
        getConnection().rollback();
        cacheAppointments();
        cacheCustomers();
    }

    public static void commit() throws SQLException {
        getConnection().commit();
    }

    public static void login(String userName, String password) throws Exception {
        try {
            var sql = connection.prepareStatement(LOGIN_SQL);
            sql.setString(1, userName);
            sql.setString(2, password);
            var rs = sql.executeQuery();
            if (rs.next()) {
                connectedUser = new User(rs.getString(1), rs.getInt(2));
            } else {
                throw new Exception("Invalid Login");
            }
        } catch (SQLException e) {
            throw new SQLException("Connection Error");
        }
    }

    public static User getConnectedUser() {
        return connectedUser;
    }

    private static void cacheUsers() throws SQLException {
        var sql = getConnection().prepareStatement(GET_USERS_SQL);
        var rs = sql.executeQuery();
        while (rs.next()) {
            users.add(new User(rs.getString(1), rs.getInt(2)));
        }
    }

    public static List<User> getUsers() throws SQLException {
        if (users.isEmpty()) {
            cacheUsers();
        } return users;
    }

    public static User getUser(int id) throws SQLException {
        if (users.isEmpty()) {
            cacheUsers();
        }
        var user = users.parallelStream().filter(a -> a.getUserId() == id).findAny();
        return user.orElse(null);
    }

    private static void cacheContacts() throws SQLException {
        contacts.clear();
        var sql = getConnection().prepareStatement(GET_CONTACTS_SQL);
        var rs = sql.executeQuery();
        while (rs.next()) {
            contacts.add(new Contact(rs.getString(1), rs.getInt(2)));
        }
    }

    public static List<Contact> getContacts() throws SQLException {
        if (contacts.isEmpty()) {
            cacheContacts();
        }
        return contacts;
    }

    public static Contact getContact(int id) throws SQLException {
        if (contacts.isEmpty()) {
            cacheContacts();
        }
        var contact = contacts.parallelStream().filter(a -> a.getContactId() == id).findAny();
        return contact.orElse(null);
    }

    private static void cacheDivisions() throws SQLException {
        var sql = getConnection().prepareStatement(GET_DIVISIONS_SQL);
        var rs = sql.executeQuery();
        while (rs.next()) {
            divisions.add(new Division(rs.getString(1), rs.getInt(2), new Country(rs.getString(3), rs.getInt(4))));
        }
    }

    public static Division getDivision(int id) throws SQLException {
        if (divisions.isEmpty()) {
            cacheDivisions();
        }
        var division = divisions.parallelStream().filter(a -> a.getDivisionId() == id).findAny();
        return division.orElse(null);
    }
    public static List<Division> getDivisionsByCountry(Country country) throws SQLException {
        if (divisions.isEmpty()) {
            cacheDivisions();
        }
        return divisions.parallelStream().filter(a -> a.getCountry().equals(country)).collect(Collectors.toList());
    }

    public static List<Country> getCountries() throws SQLException {
        if (divisions.isEmpty()) {
            cacheDivisions();
        }
        return divisions.parallelStream().map(Division::getCountry).distinct().collect(Collectors.toList());
    }

    ///////////////////Customer Methods//////////////////////////

    private static void cacheCustomers() throws SQLException {
        customers.clear();
        var sql = getConnection().prepareStatement(GET_CUST_SQL);
        var rs = sql.executeQuery();
        while (rs.next()) {
            var cust = new Customer();
            cust.setCustomerName(rs.getString(1));
            cust.setCustomerId(rs.getInt(2));
            cust.setAddress(rs.getString(3));
            cust.setPostalCode(rs.getString(4));
            cust.setDivision(getDivision(rs.getInt(5)));
            cust.setPhone(rs.getString(6));
            customers.add(cust);
        }
    }

    public static Customer getCustomer(int id) throws SQLException {
        if (customers.isEmpty()) {
            cacheCustomers();
        }
        var customer = customers.parallelStream().filter(a -> a.getCustomerId() == id).findAny();
        return customer.orElse(null);
    }

    public static List<Customer> getCustomers() throws SQLException {
        if (customers.isEmpty()) {
            cacheCustomers();
        }
        return customers;
    }

    public static void insertCustomer(Customer customer) throws SQLException {
        commit();
        var sql = getConnection().prepareStatement(INSERT_CUST_SQL);
        sql.setString(1, customer.getCustomerName());
        sql.setString(2, customer.getAddress());
        sql.setString(3, customer.getPostalCode());
        sql.setString(4, customer.getPhone());
        sql.setString(5, customer.getCreatedBy().getUserName());
        sql.setString(6, customer.getLastUpdatedBy().getUserName());
        sql.setInt(7, customer.getDivision().getDivisionId());
        sql.executeUpdate();
        cacheCustomers();
    }

    public static void updateCustomer(Customer customer) throws SQLException {
        commit();
        var sql = getConnection().prepareStatement(UPDATE_CUST_SQL);
        sql.setString(1, customer.getCustomerName());
        sql.setString(2, customer.getAddress());
        sql.setString(3, customer.getPostalCode());
        sql.setString(4, customer.getPhone());
        sql.setString(5, customer.getLastUpdatedBy().getUserName());
        sql.setInt(6, customer.getDivision().getDivisionId());
        sql.setInt(7, customer.getCustomerId());
        sql.executeUpdate();
    }

    public static void deleteCustomer(Customer customer) throws SQLException {
        commit();
        var sql = getConnection().prepareStatement(DELETE_CUST_SQL);
        sql.setInt(1, customer.getCustomerId());
        sql.executeUpdate();
        customers.remove(customer);
    }

    ///////////////////Appointment Methods//////////////////////////

    private static void cacheAppointments() throws SQLException {
        appointments.clear();
        var sql = getConnection().prepareStatement(GET_APPS_SQL);
        var rs = sql.executeQuery();
        while (rs.next()) {
            var app = new Appointment();
            app.setTitle(rs.getString(1));
            app.setAppointmentId(rs.getInt(2));
            app.setDescription(rs.getString(3));
            app.setLocation(rs.getString(4));
            app.setType(rs.getString(5));
            app.setStart(rs.getTimestamp(6).toLocalDateTime().atZone(ZoneId.systemDefault()).
                    withZoneSameInstant(ZoneId.of("UTC")));
            app.setEnd(rs.getTimestamp(7).toLocalDateTime().atZone(ZoneId.systemDefault()).
                    withZoneSameInstant(ZoneId.of("UTC")));
            app.setCustomer(getCustomer(rs.getInt(8)));
            app.setContact(getContact(rs.getInt(9)));
            appointments.add(app);
        }
    }

    public static List<Appointment> getAppointments() throws SQLException {
        if (appointments.isEmpty()) {
            cacheAppointments();
        } return appointments;
    }

    public static Appointment getAppointment(int id) throws SQLException {
        if (appointments.isEmpty()) {
            cacheAppointments();
        }
        var appointment = appointments.parallelStream().filter(a -> a.getAppointmentId() == id).findAny();
        return appointment.orElse(null);
    }

    public static void insertAppointment(Appointment app) throws SQLException {
        commit();
        var sql = getConnection().prepareStatement(INSERT_APP_SQL);
//        (Title, Description, Type, Start, End, Create_Date, Created_By, Last_Update, Last_Updated_By, Customer_ID, User_ID, Contact_ID)
        sql.setString(1, app.getTitle());
        sql.setString(2, app.getDescription());
        sql.setString(3, app.getLocation());
        sql.setString(4, app.getType());
        sql.setTimestamp(5, Timestamp.from(app.getStart().toInstant()));
        sql.setTimestamp(6, Timestamp.from(app.getEnd().toInstant()));
        sql.setString(7, app.getCreatedBy().getUserName());
        sql.setString(8, app.getLastUpdatedBy().getUserName());
        sql.setInt(9, app.getCustomer().getCustomerId());
        sql.setInt(10, app.getUser().getUserId());
        sql.setInt(11, app.getContact().getContactId());
        sql.executeUpdate();
        cacheAppointments();
    }

    public static void updateAppointment(Appointment appointment) throws SQLException {
        commit();
        var sql = getConnection().prepareStatement(UPDATE_APP_SQL);
//            (Title, Description, Location, Type, Start, End, Last_Update, Last_Updated_By, Customer_ID, User_ID, Contact_ID)
        sql.setString(1, appointment.getTitle());
        sql.setString(2, appointment.getDescription());
        sql.setString(3, appointment.getLocation());
        sql.setString(4, appointment.getType());
        sql.setTimestamp(5, Timestamp.valueOf(appointment.getStart().toLocalDateTime()));
        sql.setTimestamp(6, Timestamp.valueOf(appointment.getEnd().toLocalDateTime()));
        sql.setString(7, appointment.getLastUpdatedBy().getUserName());
        sql.setInt(8, appointment.getCustomer().getCustomerId());
        sql.setInt(9, appointment.getUser().getUserId());
        sql.setInt(10, appointment.getContact().getContactId());
        sql.executeUpdate();
    }

    public static void deleteAppointment(Appointment appointment) throws SQLException {
        commit();
        var sql = getConnection().prepareStatement(DELETE_APP_SQL);
        sql.setInt(1, appointment.getAppointmentId());
        sql.executeUpdate();
        appointments.remove(appointment);
    }
}
