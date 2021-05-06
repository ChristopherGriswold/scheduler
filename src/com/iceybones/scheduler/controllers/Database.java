package com.iceybones.scheduler.controllers;

import com.iceybones.scheduler.models.Appointment;
import com.iceybones.scheduler.models.Contact;
import com.iceybones.scheduler.models.Country;
import com.iceybones.scheduler.models.Customer;
import com.iceybones.scheduler.models.Division;
import com.iceybones.scheduler.models.User;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Performs all interactions between the application and the MySql database. NOTE: All methods in
 * this class are single threaded.
 */
public class Database {

  private static final String URL = "jdbc:mysql://wgudb.ucertify.com:3306/WJ07c5c";
  private static final String USERNAME = "U07c5c";
  private static final String PASSWORD = "53688987983";
  private static final String LOGIN_SQL = "SELECT User_Name , User_ID FROM users "
      + "WHERE User_Name = ? AND Password = ?";
  private static final String GET_USERS_SQL = "SELECT User_Name, User_ID FROM users";
  private static final String GET_CONTACTS_SQL = "SELECT Contact_Name, Contact_ID FROM contacts";
  private static final String GET_CUST_SQL = "SELECT Customer_Name, Customer_ID, "
      + "Address, Postal_Code, Division_ID, Phone "
      + "FROM customers ORDER BY Customer_ID";
  private static final String GET_APPS_SQL = "SELECT Title, Appointment_ID, Description, Location, "
      + "Type, Start, End, Customer_ID, Contact_ID, User_ID "
      + "FROM appointments ORDER BY Appointment_ID";
  private static final String INSERT_CUST_SQL = "INSERT INTO customers (Customer_Name, Address, "
      + "Postal_Code, Phone, Create_Date, Created_By, Last_Update, Last_Updated_By, Division_ID) "
      + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?, ?)";
  private static final String UPDATE_CUST_SQL = "UPDATE customers SET Customer_Name = ?, "
      + "Address = ?, Postal_Code = ?, Phone = ?, Last_Update = CURRENT_TIMESTAMP, "
      + "Last_Updated_By = ?, Division_ID = ? WHERE Customer_ID = ?";
  private static final String UPDATE_APP_SQL = "UPDATE appointments SET Title = ?, "
      + "Description = ?, Location = ?, Type = ?, Start = ?, End = ?, "
      + "Last_Update = CURRENT_TIMESTAMP, Last_Updated_By = ?, Customer_ID = ?, "
      + "User_ID = ?, Contact_ID = ? WHERE Appointment_ID = ?";
  private static final String DELETE_CUST_APP_SQL = "DELETE FROM appointments WHERE Customer_ID = ?";
  private static final String DELETE_CUST_SQL = "DELETE FROM customers WHERE Customer_ID = ?";
  private static final String DELETE_APP_SQL = "DELETE FROM appointments WHERE Appointment_ID = ?";
  private static final String GET_DIVISIONS_SQL = "SELECT d.Division, d.Division_ID, c.Country, "
      + "c.Country_ID FROM countries c JOIN first_level_divisions d ON c.Country_ID = d.Country_ID "
      + "ORDER BY d.Division_ID";
  private static final String INSERT_APP_SQL = "INSERT INTO appointments (Title, Description, "
      + "Location, Type, Start, End, Create_Date, Created_By, Last_Update, Last_Updated_By, "
      + "Customer_ID, User_ID, Contact_ID) "
      + "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?)";
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

  /**
   * Gets the active connection to the database. If the current connection is no longer valid
   * establish a new one.
   *
   * @return the active connection to the database
   * @throws SQLException if the database cannot be reached
   */
  private static Connection getConnection() throws SQLException {
    if (!connection.isValid(1)) {
      connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
      connection.setAutoCommit(false);
    }
    return connection;
  }

  /**
   * Performs an SQL rollback operation on the database. This reverts all transactions since the
   * last explicit or implicit commit. Automatically syncs the cached customer and appointment lists
   * with the database after the rollback.
   *
   * @throws SQLException if the database cannot be reached
   */
  public static void rollback() throws SQLException {
    getConnection().rollback();
    cacheCustomers();
    cacheAppointments();
  }

  /**
   * Performs an SQL commit operation on the database. This finalizes any pending transactions.
   *
   * @throws SQLException if the database cannot be reached
   */
  static void commit() throws SQLException {
    getConnection().commit();
  }

  /**
   * Connects to the database and authenticates the provided login credentials. If login fails the
   * database connection is closed and an exception is thrown.
   *
   * @param userName the name entered by the user attempting to connect
   * @param password the password entered by the user attempting to connect
   * @throws Exception    if the <code>userName</code> or <code>password</code> provided do not
   *                      match a valid database record
   * @throws SQLException if the database cannot be reached or input is invalid
   */
  static void login(String userName, String password) throws Exception {
    try {
      var sql = getConnection().prepareStatement(LOGIN_SQL);
      sql.setString(1, userName);
      sql.setString(2, password);
      var rs = sql.executeQuery();
      if (rs.next()) {
        connectedUser = new User(rs.getString(1), rs.getInt(2));
      } else {
        connection.close();
        throw new Exception("Invalid Login");
      }
    } catch (SQLException e) {
      throw new SQLException("Connection Error");
    }
  }

  /**
   * Returns the <code>user</code> object representing the user that has successfully logged into
   * the database. This object contains only the user's name and ID.
   *
   * @return the current user
   */
  public static User getConnectedUser() {
    return connectedUser;
  }

  /**
   * Performs a database query to retrieve all <code>users</code> and updates the <code>users</code>
   * field with the result.
   *
   * @throws SQLException if the database cannot be reached
   */
  private static void cacheUsers() throws SQLException {
    var sql = getConnection().prepareStatement(GET_USERS_SQL);
    var rs = sql.executeQuery();
    while (rs.next()) {
      users.add(new User(rs.getString(1), rs.getInt(2)));
    }
  }

  /**
   * Returns the cached list of <code>users</code>. If the <code>users</code> list is empty an
   * attempt is made to re-cache and return the list.
   *
   * @return the list of users
   * @throws SQLException if the database cannot be reached while attempting to re-cache
   */
  static List<User> getUsers() throws SQLException {
    if (users.isEmpty()) {
      cacheUsers();
    }
    return users;
  }

  /**
   * Returns the <code>user</code> object matching the provided <code>id</code> if it exists in the
   * <code>users</code> list. If the <code>users</code> list is empty an attempt is made to
   * re-cache the list.
   *
   * @param id the user's ID number
   * @return the user matching the provided <code>id</code>
   * @throws SQLException if the database cannot be reached while attempting to re-cache
   */
  static User getUser(int id) throws SQLException {
    if (users.isEmpty()) {
      cacheUsers();
    }
    var user = users.parallelStream().filter(a -> a.getUserId() == id).findAny();
    return user.orElse(null);
  }

  /**
   * Performs a database query to retrieve all <code>contacts</code> and updates the
   * <code>contacts</code> field with the result.
   *
   * @throws SQLException if the database cannot be reached
   */
  private static void cacheContacts() throws SQLException {
    contacts.clear();
    var sql = getConnection().prepareStatement(GET_CONTACTS_SQL);
    var rs = sql.executeQuery();
    while (rs.next()) {
      contacts.add(new Contact(rs.getString(1), rs.getInt(2)));
    }
  }

  /**
   * Returns the cached list of <code>contacts</code>. If the <code>contacts</code> list is empty an
   * attempt is made to re-cache and return the list.
   *
   * @return the list of contacts
   * @throws SQLException if the database cannot be reached while attempting to re-cache
   */
  static List<Contact> getContacts() throws SQLException {
    if (contacts.isEmpty()) {
      cacheContacts();
    }
    return contacts;
  }

  /**
   * Returns the <code>contact</code> object matching the provided <code>id</code> if it exists in
   * the
   * <code>contacts</code> list. If the <code>contacts</code> list is empty an attempt is made to
   * re-cache the list.
   *
   * @param id the contact's ID number
   * @return the contact matching the provided <code>id</code>
   * @throws SQLException if the database cannot be reached while attempting to re-cache
   */
  static Contact getContact(int id) throws SQLException {
    if (contacts.isEmpty()) {
      cacheContacts();
    }
    var contact = contacts.parallelStream().filter(a -> a.getContactId() == id).findAny();
    return contact.orElse(null);
  }

  /**
   * Performs a database query to retrieve all <code>divisions</code> and updates the
   * <code>divisions</code> field with the result.
   *
   * @throws SQLException if the database cannot be reached
   */
  private static void cacheDivisions() throws SQLException {
    var sql = getConnection().prepareStatement(GET_DIVISIONS_SQL);
    var rs = sql.executeQuery();
    while (rs.next()) {
      divisions.add(
          new Division(rs.getString(1), rs.getInt(2), new Country(rs.getString(3), rs.getInt(4))));
    }
  }

  /**
   * Returns the <code>division</code> object matching the provided <code>id</code> if it exists in
   * the
   * <code>division</code> list. If the <code>divisions</code> list is empty an attempt is made to
   * re-cache the list.
   *
   * @param id the division's ID number
   * @return the division matching the provided <code>id</code>
   * @throws SQLException if the database cannot be reached while attempting to re-cache
   */
  static Division getDivision(int id) throws SQLException {
    if (divisions.isEmpty()) {
      cacheDivisions();
    }
    var division = divisions.parallelStream().filter(a -> a.getDivisionId() == id).findAny();
    return division.orElse(null);
  }

  /**
   * Returns a list of <code>division</code> objects matching the provided <code>country</code> that
   * exist in the
   * <code>division</code> list. If the <code>divisions</code> list is empty an attempt is made to
   * re-cache the list.
   *
   * @param country the country that will be matched against
   * @return a list of the divisions matching the provided <code>country</code>
   * @throws SQLException if the database cannot be reached while attempting to re-cache
   */
  static List<Division> getDivisionsByCountry(Country country) throws SQLException {
    if (divisions.isEmpty()) {
      cacheDivisions();
    }
    return divisions.parallelStream().filter(a -> a.getCountry().equals(country))
        .collect(Collectors.toList());
  }

  /**
   * Returns the cached list of <code>countries</code>. If the <code>countries</code> list is empty
   * an attempt is made to re-cache and return the list.
   *
   * @return the list of countries
   * @throws SQLException if the database cannot be reached while attempting to re-cache
   */
  static List<Country> getCountries() throws SQLException {
    if (divisions.isEmpty()) {
      cacheDivisions();
    }
    return divisions.parallelStream().map(Division::getCountry).distinct()
        .collect(Collectors.toList());
  }

  ///////////////////Customer Methods//////////////////////////

  /**
   * Performs a database query to retrieve all <code>customers</code> and updates the
   * <code>customers</code> field with the result.
   *
   * @throws SQLException if the database cannot be reached
   */
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

  /**
   * Returns the <code>customer</code> object matching the provided <code>id</code> if it exists in
   * the
   * <code>customers</code> list. If the <code>customers</code> list is empty an attempt is made to
   * re-cache the list.
   *
   * @param id the customer's ID number
   * @return the customer matching the provided <code>id</code>
   * @throws SQLException if the database cannot be reached while attempting to re-cache
   */
  static Customer getCustomer(int id) throws SQLException {
    if (customers.isEmpty()) {
      cacheCustomers();
    }
    var customer = customers.parallelStream().filter(a -> a.getCustomerId() == id).findAny();
    return customer.orElse(null);
  }

  /**
   * Returns the cached list of <code>customers</code>. If the <code>customers</code> list is empty
   * an attempt is made to re-cache and return the list.
   *
   * @return the list of customers
   * @throws SQLException if the database cannot be reached while attempting to re-cache
   */
  static List<Customer> getCustomers() throws SQLException {
    if (customers.isEmpty()) {
      cacheCustomers();
    }
    return customers;
  }

  /**
   * Inserts a new customer record into the database and returns the <code>customerId</code>
   * generated by the database. A commit is performed on the database before submitting the
   * <code>INSERT</code> request and the <code>customers</code> list is subsequently updated to
   * include the new customer.
   *
   * @param customer the <code>customer</code> to be inserted into the database
   * @return the <code>customerId</code> generated by the database
   * @throws SQLException if the database cannot be reached or data contained in the
   *                      <code>customer</code> object does not comply with the parameters set
   *                      within the database. For example, a <code>name</code> field being more
   *                      characters than the database is set to allow.
   */
  static int insertCustomer(Customer customer) throws SQLException {
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
    return customers.get(customers.size() - 1).getCustomerId();
  }

  /**
   * Updates a customer record in the database. A commit is performed on the database before
   * submitting the
   * <code>UPDATE</code> request and the corresponding <code>customer</code> in
   * <code>customers</code> list is updated.
   *
   * @param customer the <code>customer</code> to be inserted into the database
   * @throws SQLException if the database cannot be reached or data contained in the
   *                      <code>customer</code> object does not comply with the parameters set
   *                      within the database. For example, a <code>name</code> field being more
   *                      characters than the database is set to allow.
   */
  static void updateCustomer(Customer customer) throws SQLException {
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
    customers.set(customers.indexOf(customer), customer);
  }

  /**
   * Removes a customer record from the database. A commit is performed on the database before
   * submitting the <code>DELETE</code> request and the <code>customers</code> list is subsequently
   * updated to reflect the removal of the <code>customer</code>.
   *
   * @param customer the <code>customer</code> to be deleted from the database
   * @throws SQLException if the database cannot be reached.
   */
  static void deleteCustomer(Customer customer) throws SQLException {
    commit();
    var sql = getConnection().prepareStatement(DELETE_CUST_APP_SQL);
    sql.setInt(1, customer.getCustomerId());
    sql.executeUpdate();
    sql = getConnection().prepareStatement(DELETE_CUST_SQL);
    sql.setInt(1, customer.getCustomerId());
    sql.executeUpdate();
    customers.remove(customer);
    cacheAppointments();
  }

  ///////////////////Appointment Methods//////////////////////////

  /**
   * Performs a database query to retrieve all <code>appointments</code> and updates the
   * <code>appointments</code> field with the result.
   *
   * @throws SQLException if the database cannot be reached
   */
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
      app.setUser(getUser(rs.getInt(10)));
      appointments.add(app);
    }
  }

  /**
   * Returns the cached list of <code>appointments</code>. If the <code>appointments</code> list is
   * empty an attempt is made to re-cache and return the list.
   *
   * @return the list of appointments
   * @throws SQLException if the database cannot be reached while attempting to re-cache
   */
  static List<Appointment> getAppointments() throws SQLException {
    if (appointments.isEmpty()) {
      cacheAppointments();
    }
    return appointments;
  }

  /**
   * Inserts a new appointment record into the database and returns the <code>appointmentId</code>
   * generated by the database. A commit is performed on the database before submitting the insert
   * request and the <code>appointment</code> list is subsequently updated to include the new
   * appointment.
   *
   * @param appointment the <code>appointment</code> to be inserted into the database
   * @return the <code>appointmentId</code> generated by the database
   * @throws SQLException if the database cannot be reached or data contained in the
   *                      <code>appointment</code> object does not comply with the parameters set
   *                      within the database. For example, a <code>title</code> field being more
   *                      characters than the database is set to allow.
   */
  static int insertAppointment(Appointment appointment) throws SQLException {
    commit();
    var sql = getConnection().prepareStatement(INSERT_APP_SQL);
    sql.setString(1, appointment.getTitle());
    sql.setString(2, appointment.getDescription());
    sql.setString(3, appointment.getLocation());
    sql.setString(4, appointment.getType());
    sql.setTimestamp(5, Timestamp.from(appointment.getStart().toInstant()));
    sql.setTimestamp(6, Timestamp.from(appointment.getEnd().toInstant()));
    sql.setString(7, appointment.getCreatedBy().getUserName());
    sql.setString(8, appointment.getLastUpdatedBy().getUserName());
    sql.setInt(9, appointment.getCustomer().getCustomerId());
    sql.setInt(10, appointment.getUser().getUserId());
    sql.setInt(11, appointment.getContact().getContactId());
    sql.executeUpdate();
    cacheAppointments();
    return appointments.get(appointments.size() - 1).getAppointmentId();
  }

  /**
   * Updates an appointment in the database. A commit is performed on the database before submitting
   * the
   * <code>UPDATE</code> request and the corresponding <code>appointment</code> in
   * <code>appointments</code> list is updated.
   *
   * @param appointment the <code>appointment</code> to be inserted into the database
   * @throws SQLException if the database cannot be reached or data contained in the
   *                      <code>appointment</code> object does not comply with the parameters set
   *                      within the database. For example, a <code>title</code> field being more
   *                      characters than the database is set to allow.
   */
  static void updateAppointment(Appointment appointment) throws SQLException {
    commit();
    var sql = getConnection().prepareStatement(UPDATE_APP_SQL);
    sql.setString(1, appointment.getTitle());
    sql.setString(2, appointment.getDescription());
    sql.setString(3, appointment.getLocation());
    sql.setString(4, appointment.getType());
    sql.setTimestamp(5, Timestamp.from(appointment.getStart().toInstant()));
    sql.setTimestamp(6, Timestamp.from(appointment.getEnd().toInstant()));
    sql.setString(7, appointment.getLastUpdatedBy().getUserName());
    sql.setInt(8, appointment.getCustomer().getCustomerId());
    sql.setInt(9, appointment.getUser().getUserId());
    sql.setInt(10, appointment.getContact().getContactId());
    sql.setInt(11, appointment.getAppointmentId());
    sql.executeUpdate();
    appointments.set(appointments.indexOf(appointment), appointment);
  }

  /**
   * Removes an appointment from the database. A commit is performed on the database before
   * submitting the <code>DELETE</code> request and the <code>appointment</code> list is
   * subsequently updated to reflect the removal of the <code>appointment</code>.
   *
   * @param appointment the <code>appointment</code> to be deleted from the database
   * @throws SQLException if the database cannot be reached.
   */
  static void deleteAppointment(Appointment appointment) throws SQLException {
    commit();
    var sql = getConnection().prepareStatement(DELETE_APP_SQL);
    sql.setInt(1, appointment.getAppointmentId());
    sql.executeUpdate();
    appointments.remove(appointment);
  }
}
