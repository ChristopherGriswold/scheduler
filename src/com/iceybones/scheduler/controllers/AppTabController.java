package com.iceybones.scheduler.controllers;

import com.iceybones.scheduler.controllers.MainController.NotificationType;
import com.iceybones.scheduler.models.Appointment;
import com.iceybones.scheduler.models.Contact;
import com.iceybones.scheduler.models.Customer;
import com.iceybones.scheduler.models.User;
import java.net.URL;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

/**
 * Logic controller pertaining to the application tab of the main scene.
 */
public class AppTabController implements Initializable {

  private Mode curMode = Mode.ALL;
  private MainController mainController;
  private double timeBarWidth = 0;
  private ZonedDateTime estStartDt = ZonedDateTime
      .of(LocalDateTime.of(LocalDate.now(), LocalTime.of(8, 0)), ZoneId.of("America/New_York"));
  private ResourceBundle resourceBundle;

  /**
   * Used by the mode button to identify which icon should be displayed.
   */
  private enum Mode {
    ALL(MainController.getGreenClock()),
    MONTH(MainController.getYellowClockImg()),
    WEEK(MainController.getRedClock()),
    CONTACT(MainController.getBlueClock());
    Image img;

    Mode(Image img) {
      this.img = img;
    }
  }

  /**
   * Sets up the scene GUI components utilizing the resource bundle for text values. Two lambda
   * expressions is used to implement the <code>ChangeListener</code> interface and provide extra
   * functionality for when users select items in the <code>appTableView</code> and to resize the
   * daily contact schedule time bar so that it matches the width of its parent whenever the window
   * is resized. Lambdas are also used to implement the <code>Callback</code> interface to assist
   * with the formatting of table cells.
   *
   * @param url the url of the scene's fxml layout
   * @param rb  the currently loaded resource bundle
   */
  @Override
  public void initialize(URL url, ResourceBundle rb) {
    resourceBundle = rb;
    appTableView.getColumns().get(0).setText(rb.getString("ID"));
    appTableView.getColumns().get(1).setText(rb.getString("Title"));
    appTableView.getColumns().get(2).setText(rb.getString("Description"));
    appTableView.getColumns().get(3).setText(rb.getString("Location"));
    appTableView.getColumns().get(4).setText(rb.getString("Contact"));
    appTableView.getColumns().get(5).setText(rb.getString("Type"));
    appTableView.getColumns().get(6).setText(rb.getString("Start"));
    appTableView.getColumns().get(7).setText(rb.getString("End"));
    appTableView.getColumns().get(8).setText(rb.getString("Customer ID"));
    appTypeComboBox.setPromptText(rb.getString("Select or Create"));
    appIdField.setPromptText(rb.getString("Auto-Generated"));
    appIdLbl.setText(rb.getString("ID") + ":");
    appTitleLbl.setText(rb.getString("Title") + ":");
    appDescriptionLbl.setText(rb.getString("Description") + ":");
    appTypeLbl.setText(rb.getString("Type") + ":");
    appLocationLbl.setText(rb.getString("Location") + ":");
    appDateLbl.setText(rb.getString("Date") + ":");
    addAppBtn.getTooltip().setText(rb.getString("Add New Appointment"));
    editAppBtn.getTooltip().setText(rb.getString("Edit Selected Appointment"));
    deleteAppBtn.getTooltip().setText(rb.getString("Remove Selected Appointment"));
    appRefreshBtn.getTooltip().setText(rb.getString("Refresh"));
    modeTooltip.setText(rb.getString(curMode.name()));
    reportBtn.getTooltip().setText(rb.getString("View Reports"));
    pieChart.setTitle(rb.getString("Time Spent in Meetings"));
    monthTypeChart.setTitle(rb.getString("Appointments by Type/Month"));
    contactScheduleLbl.setText(rb.getString("Daily Contact Schedule"));
    appConfirmBtn.getTooltip().setText(rb.getString("Confirm Submission"));
    populateDurationComboBox();
    setupTable();
    appDatePicker.setDayCellFactory(picker -> new DateCell() {
      public void updateItem(LocalDate date, boolean empty) {
        super.updateItem(date, empty);
        setDisable(empty || date.compareTo(LocalDate.now()) < 0);
      }
    });
    appStartCol.setCellFactory(column -> new TableCell<>() {
      final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");

      @Override
      protected void updateItem(ZonedDateTime item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
          setText(null);
        } else {
          setText(formatter.format(item.withZoneSameInstant(ZoneId.systemDefault())));
        }
      }
    });
    appEndCol.setCellFactory(column -> new TableCell<>() {
      final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");

      @Override
      protected void updateItem(ZonedDateTime item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
          setText(null);
        } else {
          setText(formatter.format(item.withZoneSameInstant(ZoneId.systemDefault())));
        }
      }
    });
    appTableView.getSelectionModel().selectedItemProperty()
        .addListener((obs, oldSelection, newSelection) -> {
          if (newSelection != null) {
            deleteAppBtn.setDisable(false);
            editAppBtn.setDisable(false);
            addAppBtn.setDisable(false);
            if (appToolDrawer.isExpanded()) {
              if (editAppBtn.isSelected()) {
                appConfirmBtn.setDisable(true);
              }
              openToolDrawer(appTableView.getSelectionModel().getSelectedItem());
            }
            if (addAppBtn.isSelected()) {
              addAppBtn.setSelected(false);
              setCollapseToolDrawer(true);
            }
          }
        });
    String start = rb.getString("Start");
    appStartComboBox.setPromptText(start);
    appStartComboBox.setButtonCell(new ListCell<>() {
      @Override
      protected void updateItem(ZonedDateTime item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(start);
        } else {
          setText(item.toString());
        }
      }
    });
    String duration = rb.getString("Duration");
    appDurationComboBox.setPromptText(duration);
    appDurationComboBox.setButtonCell(new ListCell<>() {
      @Override
      protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(duration);
        } else {
          setText(appDurationComboBox.getConverter().toString(item));
        }
      }
    });
    String customer = rb.getString("Customer");
    appCustComboBox.setPromptText(customer);
    appCustComboBox.setButtonCell(new ListCell<>() {
      @Override
      protected void updateItem(Customer item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(customer);
        } else {
          setText(item.toString());
        }
      }
    });
    String contact = rb.getString("Contact");
    appContactsComboBox.setPromptText(contact);
    appContactsComboBox.setButtonCell(new ListCell<>() {
      @Override
      protected void updateItem(Contact item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(contact);
        } else {
          setText(item.getContactName());
        }
      }
    });
    String user = rb.getString("User");
    appUserComboBox.setPromptText(user);
    appUserComboBox.setButtonCell(new ListCell<>() {
      @Override
      protected void updateItem(User item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(user);
        } else {
          setText(item.getUserName());
        }
      }
    });
    String contactTxt = rb.getString("Contact");
    reportContactBox.setPromptText(contactTxt);
    reportContactBox.setButtonCell(new ListCell<>() {
      @Override
      protected void updateItem(Contact item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(contactTxt);
        } else {
          setText(item.toString());
        }
      }
    });
    timeBarWidth = appTimeBar.getWidth();
    appTimeBar.widthProperty()
        .addListener((obs, oldVal, newVal) -> calculateTimeBar(appTimeBar.getChildren()));
  }

  /**
   * Links this controller to it's parent controller.
   *
   * @param mainController the parent controller
   */
  void setMainController(MainController mainController) {
    this.mainController = mainController;
  }

  /**
   * Calls populate on both the <code>appointment</code> table, and the combo boxes. Also calls
   * <code>checkForUpcomingApp</code> as that operation must be performed immediately after all
   * initialization methods have run.
   */
  void populate() {
    populateTable();
    populateContactComboBox();
    populateUserComboBox();
    populateCustComboBox();
    populateTypeComboBox();
    checkForUpcomingApp();
  }

  /**
   * Links columns of the <code>appointment</code> table with corresponding <code>appointment</code>
   * object values.
   */
  private void setupTable() {
    appIdCol.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
    appTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
    appDescriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
    appLocationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
    appContactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
    appTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
    appStartCol.setCellValueFactory(new PropertyValueFactory<>("start"));
    appEndCol.setCellValueFactory(new PropertyValueFactory<>("end"));
    appCustIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
  }

  /**
   * Gets all <code>appointment</code> data from the Database class and uses it to populate the
   * table view. A lambda expression is used to implement the <code>Runnable</code> interface and
   * submit the task to the database to fetch the appointment records. Three more
   * <code>Runnable</code> lambdas are nested inside to update GUI components on the JavaFX thread.
   * A notification is displayed if the database request fails for any reason.
   */
  void populateTable() {
    mainController.getTableProgress().setVisible(true);
    MainController.getDbService().submit(() -> {
      try {
        List<Appointment> appointments = Database.getAppointments();
        Platform.runLater(() -> {
          appTableView.getItems().clear();
          appTableView.getItems().addAll(filterApps(appointments, curMode));
        });
      } catch (SQLException e) {
        Platform.runLater(() -> mainController
            .notify(resourceBundle.getString("Failed to populate table. Check connection."),
                NotificationType.ERROR, false));
      } finally {
        Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
      }
    });
  }

  /**
   * Gets all type data from the Database class and uses it to populate the
   * <code>countryComboBox</code>. A lambda expression is used to implement the
   * <code>Runnable</code> interface and submit the task to the database. Two more
   * <code>Runnable</code> lambdas are nested inside to update GUI components on
   * the JavaFX thread. A notification is displayed if the database request fails for any reason.
   */
  private void populateTypeComboBox() {
    MainController.getDbService().submit(() -> {
      List<Appointment> apps = new ArrayList<>();
      try {
        apps = Database.getAppointments();
      } catch (SQLException e) {
        Platform.runLater(
            () -> mainController.notify(
                resourceBundle.getString("Failed to populate type box. Check connection."),
                MainController.NotificationType.ERROR, false));
      }
      Set<String> types = new HashSet<>();
      for (var app : apps) {
        types.add(app.getType());
      }
      List<Appointment> finalApps = apps;
      Platform.runLater(() -> {
        appTypeComboBox.getItems().setAll(types);
        setupMonthTypeChart(finalApps, types);
      });
    });
  }

  /**
   * Gets all <code>customer</code> data from the Database class and uses it to populate the
   * <code>custComboBox</code>. A lambda expression is used to implement the
   * <code>Runnable</code> interface and submit the task to the database. Two more
   * <code>Runnable</code> lambdas are nested inside to update GUI components on
   * the JavaFX thread. A notification is displayed if the database request fails for any reason.
   */
  private void populateCustComboBox() {
    appCustComboBox.getItems().clear();
    MainController.getDbService().submit(() -> {
      try {
        List<Customer> customers = Database.getCustomers();
        Platform.runLater(() -> {
          appCustComboBox.getItems().setAll(customers);
          var handle = appCustComboBox.getOnAction();
          appCustComboBox.setOnAction(null);
          appCustComboBox.setValue(null);
          appCustComboBox.setOnAction(handle);
        });
      } catch (SQLException e) {
        Platform.runLater(
            () -> mainController.notify(
                resourceBundle.getString("Failed to populate customer box. Check connection."),
                NotificationType.ERROR, false));
      }
    });
  }

  /**
   * Gets all <code>contact</code> data from the Database class and uses it to populate the
   * <code>contactComboBox</code>. A lambda expression is used to implement the
   * <code>Runnable</code> interface and submit the task to the database. A second
   * <code>Runnable</code> lambda is nested inside to update GUI components on
   * the JavaFX thread. A notification is displayed if the database request fails for any reason.
   */
  private void populateContactComboBox() {
    appContactsComboBox.getItems().clear();
    MainController.getDbService().submit(() -> {
      try {
        List<Contact> contacts = Database.getContacts();
        appContactsComboBox.getItems().addAll(contacts);
        reportContactBox.getItems().addAll(contacts);
      } catch (SQLException e) {
        Platform.runLater(
            () -> mainController.notify(
                resourceBundle.getString("Failed to populate contact box. Check connection."),
                NotificationType.ERROR, false));
      }
    });
  }

  /**
   * Gets all <code>user</code> data from the Database class and uses it to populate the
   * <code>userComboBox</code>. A lambda expression is used to implement the
   * <code>Runnable</code> interface and submit the task to the database. A second
   * <code>Runnable</code> lambda is nested inside to update GUI components on
   * the JavaFX thread. A notification is displayed if the database request fails for any reason.
   */
  private void populateUserComboBox() {
    appUserComboBox.getItems().clear();
    MainController.getDbService().submit(() -> {
      try {
        List<User> users = Database.getUsers();
        appUserComboBox.getItems().addAll(users);
      } catch (SQLException e) {
        Platform
            .runLater(() -> mainController
                .notify(resourceBundle.getString("Failed to populate user box. Check connection."),
                    NotificationType.ERROR, false));
      }
    });
  }

  /**
   * Gets all the available start times, formats them and populates the <code>startComboBox</code>
   * with them.
   */
  private void populateStartComboBox() {
    Customer cust = appCustComboBox.getSelectionModel().getSelectedItem();
    var handler = appStartComboBox.getOnAction();
    appStartComboBox.setOnAction(null);
    appStartComboBox.setValue(null);
    appStartComboBox.getItems().clear();
    appStartComboBox.getItems().setAll(getAvailableTimes(cust));
    appStartComboBox.setOnAction(handler);
    appStartComboBox.setPromptText(resourceBundle.getString("Start"));
    appStartComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(ZonedDateTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
        return time.withZoneSameInstant(ZoneId.systemDefault()).format(formatter);
      }

      @Override
      public ZonedDateTime fromString(String s) {
        return null;
      }
    });
    appStartComboBox.setValue(null);
    appStartComboBox.setButtonCell(new ListCell<>() {
      @Override
      protected void updateItem(ZonedDateTime item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(resourceBundle.getString("Start"));
        } else {
          setText(appStartComboBox.getConverter().toString(item));
        }
      }
    });
  }

  /**
   * Populates the <code>durationComboBox</code> with formatted durations in various increments.
   */
  private void populateDurationComboBox() {
    appDurationComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(Integer time) {
        return time.toString() + " min";
      }

      @Override
      public Integer fromString(String s) {
        return null;
      }
    });
    appDurationComboBox.getItems().addAll(List.of(15, 30, 45, 60, 90, 120));
  }

  /**
   * Sets all the time labels in the daily contact schedule within the reporting dashboard according
   * to the current Locale.
   */
  private void setReportTimes() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
    reportTimeLbl1
        .setText(formatter.format(estStartDt.withZoneSameInstant(ZoneId.systemDefault())));
    reportTimeLbl2.setText(
        formatter.format(estStartDt.withZoneSameInstant(ZoneId.systemDefault()).plusHours(2)));
    reportTimeLbl3.setText(
        formatter.format(estStartDt.withZoneSameInstant(ZoneId.systemDefault()).plusHours(4)));
    reportTimeLbl4.setText(
        formatter.format(estStartDt.withZoneSameInstant(ZoneId.systemDefault()).plusHours(6)));
    reportTimeLbl5.setText(
        formatter.format(estStartDt.withZoneSameInstant(ZoneId.systemDefault()).plusHours(8)));
    reportTimeLbl6.setText(
        formatter.format(estStartDt.withZoneSameInstant(ZoneId.systemDefault()).plusHours(10)));
    reportTimeLbl7.setText(
        formatter.format(estStartDt.withZoneSameInstant(ZoneId.systemDefault()).plusHours(12)));
    reportTimeLbl8.setText(
        formatter.format(estStartDt.withZoneSameInstant(ZoneId.systemDefault()).plusHours(14)));
  }

  /**
   * Checks to see if the currently logged in user has an appointment scheduled within the next
   * fifteen minutes. A lambda expression is used to implement the <code>Runnable</code> interface
   * and submit the task to the database to fetch the appointment records. Three more
   * <code>Runnable</code> lambdas are nested inside to update GUI components on
   * the JavaFX thread. A notification is displayed indicating whether or not there is an upcoming
   * appointment or if the database request fails for any reason. Lastly, <code>Predicate</code>
   * and
   * <code>Comparable</code> interfaces are implemented with lambda expressions in a stream used to
   * filter the appointment list.
   */
  private void checkForUpcomingApp() {
    MainController.getDbService().submit(() -> {
      try {
        Optional<Appointment> nearestApp = Database.getAppointments().parallelStream()
            .filter((a) -> a.getUser().equals(Database.getConnectedUser()))
            .filter((a) -> !a.getStart().isBefore(ZonedDateTime.now()))
            .min(Comparator.comparing(a -> a.getStart().toInstant()));
        if (nearestApp.isPresent() &&
            nearestApp.get().getStart().withZoneSameInstant(ZoneId.systemDefault())
                .isBefore(ZonedDateTime.now().plusMinutes(15))) {
          Platform.runLater(() -> mainController
              .notify(resourceBundle.getString("Upcoming Appointment") + ": " + nearestApp.get(),
                  NotificationType.UPCOMING_APP, false));
        } else {
          Platform.runLater(
              () -> mainController.notify(resourceBundle.getString("No Upcoming Appointments"),
                  NotificationType.NONE_UPCOMING, false));
        }
      } catch (SQLException e) {
        Platform.runLater(() -> mainController
            .notify(resourceBundle.getString("Failed to fetch upcoming appointment"),
                NotificationType.ERROR, false));
      }
    });
  }

  /**
   * Helper method used to open and closes the tool drawer.
   *
   * @param b if the tool drawer should be closed
   */
  void setCollapseToolDrawer(boolean b) {
    if (b) {
      appToolDrawer.setAnimated(true);
    }
    appToolDrawer.setCollapsible(true);
    appToolDrawer.setExpanded(!b);
    appToolDrawer.setCollapsible(false);
    appToolDrawer.setAnimated(false);
  }

  /**
   * Clears all the GUI components in the tool drawer.
   */
  private void clearToolDrawer() {
    appTitleField.setText(null);
    appIdField.setText(null);
    appLocationField.setText(null);
    appDescriptionField.setText(null);
    setValHelper(appTypeComboBox, null);
    setValHelper(appCustComboBox, null);
    setValHelper(appDurationComboBox, null);
    setValHelper(appDatePicker, null);
    setValHelper(appContactsComboBox, null);
    setValHelper(appUserComboBox, null);
    setValHelper(appStartComboBox, null);
    appDatePicker.setDisable(true);
    appDurationComboBox.setDisable(true);
    appStartComboBox.setDisable(true);
  }

  /**
   * Resets the tool buttons to their default state.
   */
  void resetToolButtons() {
    editAppBtn.setDisable(true);
    deleteAppBtn.setDisable(true);
    addAppBtn.setSelected(false);
    editAppBtn.setSelected(false);
    deleteAppBtn.setSelected(false);
    reportBtn.setSelected(false);
  }

  /**
   * Makes GUI input elements in the tool drawer editable or not.
   *
   * @param isEdit if the input elements should be set as editable
   */
  private void setToolDrawerEditable(boolean isEdit) {
    appTitleField.setDisable(!isEdit);
    appLocationField.setDisable(!isEdit);
    appDescriptionField.setDisable(!isEdit);
    appTypeComboBox.setDisable(!isEdit);
    appDatePicker.setDisable(!isEdit);
    appCustComboBox.setDisable(!isEdit);
    appUserComboBox.setDisable(!isEdit);
    appContactsComboBox.setDisable(!isEdit);
    appDurationComboBox.setDisable(!isEdit);
    appStartComboBox.setDisable(!isEdit);
  }

  /**
   * Opens the tool drawer and populates the GUI elements with data from the provided
   * <code>appointment</code>.
   *
   * @param app the <code>appointment</code> that is to be opened
   */
  private void openToolDrawer(Appointment app) {
    setCollapseToolDrawer(false);
    if (app == null) {
      clearToolDrawer();
    } else {
      appTitleField.setText(app.getTitle());
      appIdField.setText(Integer.toString(app.getAppointmentId()));
      appDescriptionField.setText(app.getDescription());
      appLocationField.setText(app.getLocation());
      setValHelper(appTypeComboBox, app.getType());
      setValHelper(appDatePicker,
          app.getStart().withZoneSameInstant(ZoneId.systemDefault()).toLocalDate());
      setValHelper(appDurationComboBox,
          (int) Duration.between(app.getStart().toLocalDateTime(), app.getEnd().toLocalDateTime())
              .toMinutes());
      setValHelper(appCustComboBox, app.getCustomer());
      setValHelper(appContactsComboBox, app.getContact());
      setValHelper(appUserComboBox, app.getUser());
      populateStartComboBox();
      setValHelper(appStartComboBox, app.getStart());
      if (deleteAppBtn.isSelected()) {
        appConfirmBtn.setDisable(false);
      }
    }
    if (!editAppBtn.isSelected()) {
      tryActivateConfirmBtn();
    }
  }

  /**
   * Helper method used to implement a workaround that allows the caller to change the GUI subject's
   * value without having its <code>onAction</code> event fired.
   *
   * @param box the GUI element that will be operated on
   * @param val the new value that is to be applied to the subject
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private void setValHelper(ComboBoxBase box, Object val) {
    var handler = box.getOnAction();
    box.setOnAction(null);
    box.setValue(val);
    box.setOnAction(handler);
  }

  /**
   * Returns a list of available time slots for the provided <code>customer</code> and
   * <code>duration</code> selected in the <code>durationComboBox</code>.
   *
   * @param customer the meeting attendant
   * @return the list of available times
   */
  private List<ZonedDateTime> getAvailableTimes(Customer customer) {
    Appointment excludeApp = null;
    if (editAppBtn.isSelected()) {
      excludeApp = appTableView.getSelectionModel().getSelectedItem();
    }
    var estEndDt = estStartDt.plusHours(14);
    int duration = appDurationComboBox.getValue();
    List<Appointment> custApps = new ArrayList<>();
    List<Appointment> tableApps = new ArrayList<>(appTableView.getItems());
    tableApps.remove(excludeApp);
    for (var app : tableApps) {
      if (app.getCustomer().equals(customer) &&
          app.getStart().toLocalDateTime().toLocalDate().equals(appDatePicker.getValue())) {
        custApps.add(app);
      }
    }
    List<ZonedDateTime> times = new ArrayList<>();
    var tempDt = estStartDt;
    times.add(tempDt);
    while (tempDt.isBefore(estEndDt.minusMinutes(15))) {
      tempDt = tempDt.plusMinutes(15);
      times.add(tempDt);
    }
    List<ZonedDateTime> outTimes = new ArrayList<>();
    OUTER:
    for (var time : times) {
      if (time.plusMinutes(duration).isAfter(estEndDt) || time.isBefore(estStartDt)) {
        continue;
      }
      for (var app : custApps) {
        if (app.getStart().isBefore(time.plusMinutes(duration)) && app.getEnd().isAfter(time)) {
          continue OUTER;
        }
      }
      outTimes.add(time);
    }
    return outTimes;
  }

  /**
   * Filter the list of appointments displayed in the <code>appTableView</code> according to the
   * specified <code>Mode</code>. Lambda expressions are used to implement the
   * <code>Predicate</code> interface for the <code>filter</code> stream methods.
   *
   * @param list the list to be filtered
   * @param mode how the list should be filtered
   * @return the list of <code>appointments</code> post filter
   */
  private List<Appointment> filterApps(List<Appointment> list, Mode mode) {
    List<Appointment> out = null;
    switch (mode) {
      case ALL:
        out = new ArrayList<>(list);
        break;
      case MONTH:
        out = (list.stream()
            .filter((a) -> a.getStart().withZoneSameInstant(ZoneId.systemDefault()).getMonthValue()
                == ZonedDateTime.now().getMonthValue()).collect(Collectors.toList()));
        break;
      case WEEK:
        int weekNumber = ZonedDateTime.now()
            .get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        out = (list.stream()
            .filter((a) -> a.getStart().withZoneSameInstant(ZoneId.systemDefault())
                .get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear())
                == weekNumber).collect(Collectors.toList()));
        break;
    }
    return out;
  }

  /**
   * Dynamically resizes the time bar buttons to fill their parent while maintaining their
   * individual ratios.
   *
   * @param buttons the buttons from the time bar
   */
  private void calculateTimeBar(ObservableList<Node> buttons) {
    double deltaRat = appTimeBar.getWidth() / timeBarWidth;
    for (var button : buttons) {
      ((Button) button).setPrefWidth(((Button) button).getPrefWidth() * deltaRat);
    }
    timeBarWidth = appTimeBar.getWidth();
  }

  /**
   * Creates buttons in a specific size and order to appear as a single timeline element spanning a
   * single work day and illustrating a contact's daily appointment schedule.
   *
   * @param contact the individual who's schedule will be displayed
   */
  private void setTimeBar(Contact contact) {
    timeBarWidth = appTimeBar.getWidth();
    appTimeBar.getChildren().clear();
    ZonedDateTime estStart = ZonedDateTime
        .of(LocalDateTime.of(reportDatePicker.getValue(), LocalTime.of(8, 0)),
            ZoneId.of("America/New_York"));
    List<Appointment> apps = new ArrayList<>();
    try {
      apps = Database.getAppointments();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    List<Appointment> conApps = new ArrayList<>();
    for (var app : apps) {
      if (app.getContact().equals(contact) && app.getStart()
          .withZoneSameInstant(ZoneId.systemDefault()).toLocalDate()
          .isEqual(reportDatePicker.getValue())) {
        conApps.add(app);
      }
    }
    conApps.sort(Comparator.comparing(Appointment::getStart));
    double timeSlice = timeBarWidth / 56;
    ZonedDateTime timePointer = estStart;
    for (int i = 0; i < conApps.size(); i++) {
      var start = conApps.get(i).getStart();
      var end = conApps.get(i).getEnd();
      if (start.isAfter(timePointer)) {
        Button freeTime = new Button();
        freeTime.setMaxWidth(Double.MAX_VALUE);
        freeTime.setPrefWidth(
            timeSlice * Duration.between(timePointer, conApps.get(i).getStart()).toMinutes() / 15f);
        freeTime.setDisable(true);
        appTimeBar.getChildren().add(freeTime);
      }
      Appointment nextApp = conApps.get(i);
      int skip = 0;
      for (int j = i + 1; j < conApps.size(); j++) {
        if (conApps.get(j).getStart().isBefore(end)) {
          skip++;
          if (end.isBefore(conApps.get(j).getEnd())) {
            end = conApps.get(j).getEnd();
            nextApp = conApps.get(j);
          }
        }
      }
      Button button = new Button();
      button.setCursor(Cursor.HAND);
      int finalI = i;
      int finalSkip = skip;
      button.setOnAction((event) -> {
        appTableView.getItems().clear();
        for (int k = finalI; k <= finalI + finalSkip; k++) {
          appTableView.getItems().add(conApps.get(k));
        }
        curMode = Mode.CONTACT;
        modeImgView.setImage(curMode.img);
        modeTooltip.setText(resourceBundle.getString(curMode.name()));
      });
      var toolText = new StringBuilder();
      for (int k = i; k <= i + finalSkip; k++) {
        toolText.append(conApps.get(k).toString()).append("\n");
      }
      toolText.setLength(toolText.length() - 1);
      button.setStyle("-fx-background-color: #239cc7; -fx-focus-traversable: true");
      button.setTooltip(new Tooltip(toolText.toString()));
      button.hoverProperty().addListener(((observable, oldValue, show) -> {
        if (show) {
          button.setStyle("-fx-background-color: #40bae6");
        } else {
          button.setStyle("-fx-background-color: #239cc7");
        }
      }));
      button.setMaxWidth(Double.MAX_VALUE);
      button.setPrefWidth(
          timeSlice * Duration.between(start, end).toMinutes() / 15f);
      appTimeBar.getChildren().add(button);
      i += skip;
      timePointer = nextApp.getEnd();
    }
    if (!timePointer.isEqual(estStart.plusHours(14))) {
      Button last = new Button();
      last.setStyle("-fx-background-radius: 0");
      last.setMaxWidth(Double.MAX_VALUE);
      last.setPrefWidth(timeSlice * (Duration
          .between(conApps.get(conApps.size() - 1).getEnd(), estStart.plusHours(14)).toMinutes())
          / 15f);
      last.setDisable(true);
      appTimeBar.getChildren().add(last);
    }
  }

  /**
   * Marks days on the <code>reportDatePicker</code> that the specified <code>Contact</code> has
   * appointments on. A lambda is used as an implementation of the <code>Callback</code> interface
   * to set the cells of the date picker. A lambda also implements the <code>ChangeListener</code>
   * interface to change the look of the cells when the mouse hovers over them.
   *
   * @param contact the individual who's schedule will be displayed
   */
  private void setReportDatePicker(Contact contact) {
    List<LocalDate> dates = getMarkedDays(contact);
    reportDatePicker.setDayCellFactory(picker -> new DateCell() {
      public void updateItem(LocalDate date, boolean empty) {
        super.updateItem(date, empty);
        if (dates.contains(date)) {
          setTooltip(new Tooltip("View Appointments"));
          setStyle("-fx-background-color: #239cc7; -fx-text-fill: #FFFFFF");
          hoverProperty().addListener(((observable, oldValue, show) -> {
            if (show) {
              setStyle("-fx-background-color: #40bae6; -fx-text-fill: #FFFFFF");
            } else {
              setStyle("-fx-background-color: #239cc7; -fx-text-fill: #FFFFFF");
            }
          }));
        } else {
          setDisable(true);
        }
      }
    });
  }

  /**
   * Helper method used by <code>setReportDatePicker</code> to get the <code>Appointment</code> list
   * from the <code>Database</code> class that pertain to the provided <code>Contact</code>.
   *
   * @param contact the individual who's schedule will be fetched
   * @return the list of days
   */
  private List<LocalDate> getMarkedDays(Contact contact) {
    List<Appointment> apps = new ArrayList<>();
    try {
      apps = Database.getAppointments();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    List<LocalDate> dates = new ArrayList<>();
    for (var app : apps) {
      if (app.getContact().equals(contact)) {
        dates.add(app.getStart().withZoneSameInstant(ZoneId.systemDefault()).toLocalDate());
      }
    }
    return dates;
  }

  /**
   * Sets up and populates the month/type line chart in the reporting dashboard.
   *
   * @param apps  the list of appointments
   * @param types the set of appointment type
   */
  private void setupMonthTypeChart(List<Appointment> apps, Set<String> types) {
    monthTypeChart.getData().clear();
    for (var type : types) {
      int jan = 0, feb = 0, mar = 0, apr = 0, may = 0, jun = 0, jul = 0, aug = 0, sep = 0, oct = 0, nov = 0, dec = 0;
      for (var app : apps) {
        if (app.getType().equals(type)
            && app.getStart().withZoneSameInstant(ZoneId.systemDefault()).getYear()
            == ZonedDateTime.now().getYear()) {
          switch (app.getStart().withZoneSameInstant(ZoneId.systemDefault()).getMonth()) {
            case JANUARY:
              jan++;
              break;
            case FEBRUARY:
              feb++;
              break;
            case MARCH:
              mar++;
              break;
            case APRIL:
              apr++;
              break;
            case MAY:
              may++;
              break;
            case JUNE:
              jun++;
              break;
            case JULY:
              jul++;
              break;
            case AUGUST:
              aug++;
              break;
            case SEPTEMBER:
              sep++;
              break;
            case OCTOBER:
              oct++;
              break;
            case NOVEMBER:
              nov++;
              break;
            case DECEMBER:
              dec++;
              break;
          }
        }
      }
      XYChart.Series<String, Integer> series = new Series<>();
      series.getData().add(new XYChart.Data<>("Jan", jan));
      series.getData().add(new XYChart.Data<>("Feb", feb));
      series.getData().add(new XYChart.Data<>("Mar", mar));
      series.getData().add(new XYChart.Data<>("Apr", apr));
      series.getData().add(new XYChart.Data<>("May", may));
      series.getData().add(new XYChart.Data<>("Jun", jun));
      series.getData().add(new XYChart.Data<>("Jul", jul));
      series.getData().add(new XYChart.Data<>("Aug", aug));
      series.getData().add(new XYChart.Data<>("Sep", sep));
      series.getData().add(new XYChart.Data<>("Oct", oct));
      series.getData().add(new XYChart.Data<>("Nov", nov));
      series.getData().add(new XYChart.Data<>("Dec", dec));
      series.setName(type);
      monthTypeChart.getData().add(series);
    }
  }

  /**
   * Sets up and populates the month/type line chart in the reporting dashboard. A lambda expression
   * is used to implement the <code>Runnable</code> interface and submit the task to the database to
   * fetch the <code>Appointment</code> and <code>Contact</code> records. Two <code>Function</code>
   * lambda expressions are used by the <code>Collectors.toMap</code> method of the stream that
   * collects the values of minutes spent in meetings for each <code>Contact</code>A
   * <code>Runnable</code> lambda is also used to update the pie chart GUI on the JavaFX thread.
   */
  private void setupPieChart() {
    MainController.getDbService().submit(() -> {
      try {
        List<Appointment> apps = Database.getAppointments();
        List<Contact> contacts = Database.getContacts();
        Map<Contact, Long> contactMins = contacts.stream()
            .collect(Collectors.toMap(contact -> contact, a -> 0L));
        for (var app : apps) {
          contactMins.put(app.getContact(), contactMins.get(app.getContact())
              + Duration.between(app.getStart(), app.getEnd()).toMinutes());
        }
        List<PieChart.Data> list = new ArrayList<>();
        for (var contact : contacts) {
          list.add(new Data(contact.getContactName(), contactMins.get(contact)));
        }
        ObservableList<PieChart.Data> oList = FXCollections.observableArrayList(list);
        Platform.runLater(() -> pieChart.setData(oList));
      } catch (SQLException e) {
        e.printStackTrace();
      }
    });
  }

  /**
   * Gives the <code>Customers</code> tab the ability to add an appointment with the specified
   * <code>Customer</code>.
   *
   * @param cust the <code>Customer</code> that will pre-populate the <code>custComboBox</code>
   */
  void pushAppointment(Customer cust) {
    appTableView.getSelectionModel().clearSelection();
    resetToolButtons();
    addAppBtn.fire();
    appCustComboBox.getSelectionModel().select(cust);
  }

  /**
   * Sends a request to the Database class to <code>INSERT</code> a new <code>Appointment</code>
   * record. A lambda expression is used to implement the <code>Runnable</code> interface and submit
   * a new task to the database to perform the operation. Three more <code>Runnable</code> lambdas
   * are nested inside to update GUI components on the JavaFX thread. A notification is displayed to
   * report the success or failure of the operation.
   *
   * @param app the new <code>Appointment</code> that is to be added
   */
  private void confirmAddApp(Appointment app) {
    mainController.getTableProgress().setVisible(true);
    MainController.getDbService().submit(() -> {
      try {
        int appId = Database.insertAppointment(app);
        app.setAppointmentId(appId);
        Platform.runLater(() -> {
          setCollapseToolDrawer(true);
          resetToolButtons();
          populateTable();
          mainController.notify(resourceBundle.getString("Appointment Added") + ": " + app,
              NotificationType.ADD, true);
        });
      } catch (SQLException e) {
        Platform.runLater(() -> mainController
            .notify(resourceBundle.getString("Failed to add appointment. Check connection."),
                NotificationType.ERROR, false));
      } finally {
        Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
      }
    });
  }

  /**
   * Sends a request to the Database class to <code>UPDATE</code> an <code>Appointment</code>
   * record. A lambda expression is used to implement the <code>Runnable</code> interface and submit
   * a new task to the database to perform the operation. Three more <code>Runnable</code> lambdas
   * are nested inside to update GUI components on the JavaFX thread. A notification is displayed to
   * report the success or failure of the operation.
   *
   * @param newApp   an <code>Appointment</code> object holding the data that is to be copied unto
   *                 the original
   * @param original the <code>Appointment</code> that is to be updated
   */
  private void confirmUpdateApp(Appointment newApp, Appointment original) {
    mainController.getTableProgress().setVisible(true);
    MainController.getDbService().submit(() -> {
      try {
        Database.updateAppointment(newApp);
        Platform.runLater(() -> {
          clearToolDrawer();
          setCollapseToolDrawer(true);
          resetToolButtons();
          try {
            appTableView.getItems().set(appTableView.getItems().indexOf(original), newApp);
          } catch (RuntimeException e) {
            e.printStackTrace();
          }
          mainController.notify(resourceBundle.getString("Appointment Updated") + ": " + newApp,
              NotificationType.EDIT, true);
        });
      } catch (SQLException e) {
        Platform
            .runLater(() -> mainController
                .notify(resourceBundle.getString("Failed to update appointment. Check connection."),
                    NotificationType.ERROR, false));
      } finally {
        Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
      }
    });
  }

  /**
   * Sends a request to the Database class to <code>DELETE</code> an <code>Appointment</code>
   * record. A lambda expression is used to implement the <code>Runnable</code> interface and submit
   * a new task to the database to perform the operation. Three more <code>Runnable</code> lambdas
   * are nested inside to update GUI components on the JavaFX thread. A notification is displayed to
   * report the success or failure of the operation.
   *
   * @param appointment the <code>Appointment</code> that is to be deleted
   */
  private void confirmDeleteApp(Appointment appointment) {
    mainController.getTableProgress().setVisible(true);
    MainController.getDbService().submit(() -> {
      try {
        Database.deleteAppointment(appointment);
        Platform.runLater(() -> {
          clearToolDrawer();
          setCollapseToolDrawer(true);
          resetToolButtons();
          populateTable();
          mainController
              .notify(resourceBundle.getString("Appointment Removed") + ": " + appointment,
                  NotificationType.DELETE,
                  true);
        });
      } catch (SQLException e) {
        Platform
            .runLater(() -> mainController
                .notify(resourceBundle.getString("Failed to delete appointment. Check connection."),
                    NotificationType.ERROR, false));
      } finally {
        Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
      }
    });
  }

  /**
   * Activates the <code>custConfirmBtn</code> if all the required values are present in the
   * toolbar's inputs.
   */
  private void tryActivateConfirmBtn() {
    appConfirmBtn
        .setDisable(appTitleField.getText() == null || appLocationField.getText() == null ||
            appDescriptionField.getText() == null || appTypeComboBox.getValue() == null ||
            appDatePicker.getValue() == null ||
            (appCustComboBox.getValue() == null || appTypeComboBox.getValue().equals("")) ||
            appContactsComboBox.getValue() == null || appStartComboBox.getValue() == null ||
            appUserComboBox.getValue() == null || appDurationComboBox.getValue() == null);
  }

  /////////////////////////Event Handlers//////////////////////////

  /**
   * Sets the <code>reportDatePicker</code> when an element of the <code>reportingContactBox</code>
   * is selected.
   */
  @FXML
  private void onActionReportContactBox() {
    setValHelper(reportDatePicker, null);
    reportDatePicker.setDisable(false);
    appTimeBar.getChildren().clear();
    setReportDatePicker(reportContactBox.getValue());
  }

  /**
   * Sets the time bar when a selection is made on the <code>reportDatePicker</code>.
   */
  @FXML
  private void onActionReportDatePicker() {
    setTimeBar(reportContactBox.getValue());
  }

  /**
   * Opens the tool drawer when the <code>addAppBtn</code> is selected.
   */
  @FXML
  private void onActionAddApp() {
    appTableView.getSelectionModel().clearSelection();
    editAppBtn.setDisable(true);
    deleteAppBtn.setDisable(true);
    if (addAppBtn.isSelected()) {
      appConfirmBtnImg.setImage(MainController.getAddImg());
      setToolDrawerEditable(true);
      if (!toolStackPane.getChildren().contains(appGridPane)) {
        reportVbox.setVisible(false);
        toolStackPane.getChildren().remove(reportVbox);
        storagePane.getChildren().add(reportVbox);
        toolStackPane.getChildren().add(appGridPane);
        appGridPane.setVisible(true);
      }
      openToolDrawer(null);
    } else {
      setCollapseToolDrawer(true);
    }
  }

  /**
   * Opens the tool drawer when the <code>deleteAppBtn</code> is selected.
   */
  @FXML
  private void onActionDeleteApp() {
    if (deleteAppBtn.isSelected()) {
      setToolDrawerEditable(false);
      appConfirmBtnImg.setImage(MainController.getDeleteImg());
      if (!toolStackPane.getChildren().contains(appGridPane)) {
        reportVbox.setVisible(false);
        toolStackPane.getChildren().remove(reportVbox);
        storagePane.getChildren().add(reportVbox);
        toolStackPane.getChildren().add(appGridPane);
        appGridPane.setVisible(true);
      }
      openToolDrawer(appTableView.getSelectionModel().getSelectedItem());
    } else {
      setCollapseToolDrawer(true);
    }
  }

  /**
   * Opens the tool drawer when the <code>editAppBtn</code> is selected.
   */
  @FXML
  private void onActionEditApp() {
    if (editAppBtn.isSelected()) {
      setToolDrawerEditable(true);
      appConfirmBtnImg.setImage(MainController.getEditImg());
      appConfirmBtn.setDisable(true);

      if (!toolStackPane.getChildren().contains(appGridPane)) {
        reportVbox.setVisible(false);
        toolStackPane.getChildren().remove(reportVbox);
        storagePane.getChildren().add(reportVbox);
        toolStackPane.getChildren().add(appGridPane);
        appGridPane.setVisible(true);
      }
      openToolDrawer(appTableView.getSelectionModel().getSelectedItem());
    } else {
      setCollapseToolDrawer(true);
    }
  }

  /**
   * Opens the tool drawer and changes it's contents to the reporting dashboard when the
   * <code>reportBtn</code> is selected.
   */
  @FXML
  private void onActionReport() {
    setReportTimes();
    setupPieChart();
    editAppBtn.setDisable(true);
    deleteAppBtn.setDisable(true);
    setValHelper(reportDatePicker, null);
    reportDatePicker.setDisable(true);
    setValHelper(reportContactBox, null);
    appTimeBar.getChildren().clear();
    if (reportBtn.isSelected()) {
      if (!toolStackPane.getChildren().contains(reportVbox)) {
        appGridPane.setVisible(false);
        toolStackPane.getChildren().remove(appGridPane);
        storagePane.getChildren().add(appGridPane);
        toolStackPane.getChildren().add(reportVbox);
        reportVbox.setVisible(true);
      }
      appTableView.getSelectionModel().clearSelection();
      populateTypeComboBox();
      openToolDrawer(null);
    } else {
      setCollapseToolDrawer(true);
    }
  }

  /**
   * Enables the <code>durationComboBox</code> when a date is selected in the
   * <code>appDatePicker</code>.
   */
  @FXML
  private void onActionDatePicker() {
    if (appDatePicker.getValue() == null) {
      return;
    }
    estStartDt = ZonedDateTime.of(appDatePicker.getValue(), estStartDt.toLocalTime(), estStartDt.getZone());
    appDurationComboBox.setDisable(false);
    setValHelper(appDurationComboBox, null);
    setValHelper(appStartComboBox, null);
    setValHelper(appStartComboBox, null);
    appStartComboBox.setDisable(true);
    tryActivateConfirmBtn();
  }

  /**
   * Sets up the <code>startComboBox</code> when a selection is made on the
   * <code>durationComboBox</code> and tries to activate the <code>appConfirmBtn</code>.
   */
  @FXML
  private void onActionDurationComboBox() {
    appStartComboBox.setDisable(false);
    populateStartComboBox();
    tryActivateConfirmBtn();
  }

  /**
   * Tries to activate the <code>appConfirmBtn</code> when a selection is made in the
   * <code>startComboBox</code>.
   */
  @FXML
  private void onActionStartComboBox() {
    tryActivateConfirmBtn();
  }


  /**
   * Enables the <code>appDatePicker</code> when a selection is made in the
   * <code>custComboBox</code>.
   */
  @FXML
  private void onActionCustComboBox() {
    setValHelper(appDurationComboBox, null);
    appDatePicker.setDisable(false);
    setValHelper(appDatePicker, LocalDate.now());
    setValHelper(appStartComboBox, null);
    appStartComboBox.setDisable(true);
    appDurationComboBox.setDisable(false);
  }

  /**
   * Tries to activate the <code>appConfirmBtn</code> when a selection is made in the
   * <code>contactComboBox</code>.
   */
  @FXML
  private void onActionContactComboBox() {
    tryActivateConfirmBtn();
  }

  /**
   * Tries to activate the <code>appConfirmBtn</code> when a selection is made in the
   * <code>userComboBox</code>.
   */
  @FXML
  private void onActionUserComboBox() {
    tryActivateConfirmBtn();
  }

  /**
   * Tries to activate the <code>appConfirmBtn</code> when a selection is made in the
   * <code>typeComboBox</code>.
   */
  @FXML
  private void onActionTypeComboBox() {
    tryActivateConfirmBtn();
  }

  /**
   * Tries to activate the <code>appConfirmBtn</code> whenever a key is typed in any of the toolbar
   * text fields.
   */
  @FXML
  private void onKeyTypedAppField() {
    tryActivateConfirmBtn();
  }

  /**
   * Gathers all the input data from the tool drawer elements and calls the relevant confirm method
   * when the <code>appConfirmBtn</code> is pressed.
   */
  @FXML
  private void onActionConfirm() {
    Appointment app = new Appointment();
    app.setTitle(appTitleField.getText());
    app.setAppointmentId(appIdField.getText() == null ? 0 : Integer.parseInt(appIdField.getText()));
    app.setType(appTypeComboBox.getValue());
    app.setLocation(appLocationField.getText());
    app.setDescription(appDescriptionField.getText());
    app.setStart(appStartComboBox.getValue());
    app.setEnd(appStartComboBox.getValue().plusMinutes(appDurationComboBox.getValue()));
    app.setCustomer(appCustComboBox.getValue());
    app.setUser(appUserComboBox.getValue());
    app.setContact(appContactsComboBox.getValue());
    app.setCreatedBy(Database.getConnectedUser());
    app.setLastUpdatedBy(Database.getConnectedUser());
    if (addAppBtn.isSelected()) {
      confirmAddApp(app);
    } else if (deleteAppBtn.isSelected()) {
      confirmDeleteApp(appTableView.getSelectionModel().getSelectedItem());
    } else if (editAppBtn.isSelected()) {
      confirmUpdateApp(app, appTableView.getSelectionModel().getSelectedItem());
    }
  }

  /**
   * Makes a request to the Database class to perform a <code>COMMIT</code> on the database. This
   * deselects any selected items and closes the tool drawer. A lambda expression is used to
   * implement the <code>Runnable</code> interface and submit a new task to the database to perform
   * the <code>COMMIT</code>. Three more <code>Runnable</code> lambdas are nested inside to update
   * GUI components on the JavaFX thread. A notification is displayed to report the success or
   * failure of this request.
   */
  @FXML
  private void onActionRefresh() {
    mainController.getTableProgress().setVisible(true);
    appTableView.getSelectionModel().clearSelection();
    setCollapseToolDrawer(true);
    resetToolButtons();
    MainController.getDbService().submit(() -> {
      try {
        Database.commit();
        Platform.runLater(() -> mainController.notify(resourceBundle.getString("Changes Committed"),
            NotificationType.SUCCESS, false)
        );
      } catch (SQLException e) {
        Platform
            .runLater(() -> mainController
                .notify(resourceBundle.getString("Failed to refresh database. Check connection."),
                    NotificationType.ERROR, false)
            );
      } finally {
        Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
      }
    });
    mainController.disableUndo();
  }

  /**
   * Change how the appointments in the <code>appTableView</code> are filtered and displayed when
   * the <code>appModeBtn</code> is pressed.
   */
  @FXML
  private void onActionAppMode() {
    resetToolButtons();
    setCollapseToolDrawer(true);
    switch (curMode) {
      case ALL:
        curMode = Mode.MONTH;
        break;
      case MONTH:
        curMode = Mode.WEEK;
        break;
      case WEEK:
      case CONTACT:
        curMode = Mode.ALL;
        break;
    }
    modeImgView.setImage(curMode.img);
    modeTooltip.setText(resourceBundle.getString(curMode.name()));
    populateTable();
  }

  //////////////////GUI Components///////////////////////

  @FXML
  private AnchorPane storagePane;

  @FXML
  private StackPane toolStackPane;

  @FXML
  private ImageView modeImgView;

  @FXML
  private Tooltip modeTooltip;

  @FXML
  private ToggleButton addAppBtn;

  @FXML
  private ToggleButton deleteAppBtn;

  @FXML
  private ToggleButton editAppBtn;

  @FXML
  private ToggleButton reportBtn;

  @FXML
  private TitledPane appToolDrawer;

  @FXML
  private TextField appTitleField;

  @FXML
  private TextArea appDescriptionField;

  @FXML
  private ComboBox<Customer> appCustComboBox;

  @FXML
  private TextField appLocationField;

  @FXML
  private ComboBox<Contact> appContactsComboBox;

  @FXML
  private ComboBox<User> appUserComboBox;

  @FXML
  private Button appConfirmBtn;

  @FXML
  private Button appRefreshBtn;

  @FXML
  private ImageView appConfirmBtnImg;

  @FXML
  private ComboBox<ZonedDateTime> appStartComboBox;

  @FXML
  private ComboBox<Integer> appDurationComboBox;

  @FXML
  private DatePicker appDatePicker;

  @FXML
  private TextField appIdField;

  @FXML
  private ComboBox<String> appTypeComboBox;

  @FXML
  private TableView<Appointment> appTableView;

  @FXML
  private TableColumn<Appointment, Integer> appIdCol;

  @FXML
  private TableColumn<Appointment, String> appTitleCol;

  @FXML
  private TableColumn<Appointment, String> appDescriptionCol;

  @FXML
  private TableColumn<Appointment, String> appLocationCol;

  @FXML
  private TableColumn<Appointment, String> appContactCol;

  @FXML
  private TableColumn<Appointment, String> appTypeCol;

  @FXML
  private TableColumn<Appointment, ZonedDateTime> appStartCol;

  @FXML
  private TableColumn<Appointment, ZonedDateTime> appEndCol;

  @FXML
  private TableColumn<Appointment, Integer> appCustIdCol;

  @FXML
  private GridPane appGridPane;

  @FXML
  private VBox reportVbox;

  @FXML
  private ComboBox<Contact> reportContactBox;

  @FXML
  private DatePicker reportDatePicker;

  @FXML
  private LineChart<String, Integer> monthTypeChart;

  @FXML
  private PieChart pieChart;

  @FXML
  private HBox appTimeBar;

  @FXML
  private Label contactScheduleLbl;

  @FXML
  private Label reportTimeLbl1;
  @FXML
  private Label reportTimeLbl2;
  @FXML
  private Label reportTimeLbl3;
  @FXML
  private Label reportTimeLbl4;
  @FXML
  private Label reportTimeLbl5;
  @FXML
  private Label reportTimeLbl6;
  @FXML
  private Label reportTimeLbl7;
  @FXML
  private Label reportTimeLbl8;

  @FXML
  private Label appIdLbl;
  @FXML
  private Label appTitleLbl;
  @FXML
  private Label appTypeLbl;
  @FXML
  private Label appDescriptionLbl;
  @FXML
  private Label appLocationLbl;
  @FXML
  private Label appDateLbl;
}
