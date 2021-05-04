package com.iceybones.scheduler.application.controller;

import com.iceybones.scheduler.application.controller.MainController.NotificationType;
import com.iceybones.scheduler.application.model.Appointment;
import com.iceybones.scheduler.application.model.Contact;
import com.iceybones.scheduler.application.model.Customer;
import com.iceybones.scheduler.application.model.User;
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
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class AppTabController implements Initializable {

  private double timeBarWidth = 0;
  ZonedDateTime utcStartDt = ZonedDateTime
      .of(LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0)), ZoneId.of("UTC"));

  private enum Mode {
    ALL(MainController.getGreenClock(), "Show Month"),
    MONTH(MainController.getYellowClockImg(), "Show Week"),
    WEEK(MainController.getRedClock(), "Show All");
    Image img;
    String tipText;

    Mode(Image img, String tipText) {
      this.img = img;
      this.tipText = tipText;
    }
  }

  private Mode curMode = Mode.ALL;
  private MainController mainController;

  public void setMainController(MainController mainController) {
    this.mainController = mainController;
  }

  public void populate() {
    populateTable();
    populateContactComboBox();
    populateUserComboBox();
    populateCustComboBox();
    populateTypeComboBox();
    checkForUpcomingApps(15);
    setReportTimes();
  }

  private void setReportTimes() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
    reportTimeLbl1
        .setText(formatter.format(utcStartDt.withZoneSameInstant(ZoneId.systemDefault())));
    reportTimeLbl2.setText(
        formatter.format(utcStartDt.withZoneSameInstant(ZoneId.systemDefault()).plusHours(2)));
    reportTimeLbl3.setText(
        formatter.format(utcStartDt.withZoneSameInstant(ZoneId.systemDefault()).plusHours(4)));
    reportTimeLbl4.setText(
        formatter.format(utcStartDt.withZoneSameInstant(ZoneId.systemDefault()).plusHours(6)));
    reportTimeLbl5.setText(
        formatter.format(utcStartDt.withZoneSameInstant(ZoneId.systemDefault()).plusHours(8)));
    reportTimeLbl6.setText(
        formatter.format(utcStartDt.withZoneSameInstant(ZoneId.systemDefault()).plusHours(10)));
    reportTimeLbl7.setText(
        formatter.format(utcStartDt.withZoneSameInstant(ZoneId.systemDefault()).plusHours(12)));
    reportTimeLbl8.setText(
        formatter.format(utcStartDt.withZoneSameInstant(ZoneId.systemDefault()).plusHours(14)));
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
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
            if (oldSelection != null && appToolDrawer.isExpanded()) {
              openToolDrawer(appTableView.getSelectionModel().getSelectedItem());
            }
            if (addAppBtn.isSelected()) {
              addAppBtn.setSelected(false);
              setCollapseToolDrawer(true);
            }
          }
        });
    appStartComboBox.setPromptText("Start");
    appStartComboBox.setButtonCell(new ListCell<>() {
      @Override
      protected void updateItem(ZonedDateTime item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText("Start");
        } else {
          setText(item.toString());
        }
      }
    });
    appDurationComboBox.setPromptText("Duration");
    appDurationComboBox.setButtonCell(new ListCell<>() {
      @Override
      protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText("Duration");
        } else {
          setText(appDurationComboBox.getConverter().toString(item));
        }
      }
    });
    appCustComboBox.setPromptText("Customer");
    appCustComboBox.setButtonCell(new ListCell<>() {
      @Override
      protected void updateItem(Customer item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText("Customer");
        } else {
          setText(item.toString());
        }
      }
    });
    appContactsComboBox.setPromptText("Contact");
    appContactsComboBox.setButtonCell(new ListCell<>() {
      @Override
      protected void updateItem(Contact item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText("Contact");
        } else {
          setText(item.getContactName());
        }
      }
    });
    appUserComboBox.setPromptText("Users");
    appUserComboBox.setButtonCell(new ListCell<>() {
      @Override
      protected void updateItem(User item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText("Users");
        } else {
          setText(item.getUserName());
        }
      }
    });
    timeBarWidth = appTimeBar.getWidth();
    appTimeBar.widthProperty().addListener((obs, oldVal, newVal) -> {
      calculateTimeBar(appTimeBar.getChildren());
    });
  }

  void checkForUpcomingApps(int mins) {
    MainController.getDbService().submit(() -> {
      var nearestApp = appTableView.getItems().parallelStream()
          .filter((a) -> a.getUser().equals(Database.getConnectedUser()))
          .filter((a) -> !a.getStart().isBefore(ZonedDateTime.now()))
          .min(Comparator.comparing(a -> a.getStart().toInstant()));

      if (nearestApp.isPresent() &&
          nearestApp.get().getStart().withZoneSameInstant(ZoneId.systemDefault())
              .isBefore(ZonedDateTime.now().plusMinutes(mins))) {
        Platform.runLater(
            () -> mainController
                .notify("Upcoming appointment: " + nearestApp.get(), NotificationType.UPCOMING_APP,
                    false));
      } else {
        Platform.runLater(() -> {
          mainController.notify("No upcoming appointments",
              NotificationType.NONE_UPCOMING, false);
        });
      }
    });
  }

  void tryActivateConfirmBtn() {
    appConfirmBtn
        .setDisable(appTitleField.getText() == null || appLocationField.getText() == null ||
            appDescriptionField.getText() == null || appTypeComboBox.getValue() == null ||
            appDatePicker.getValue() == null ||
            (appCustComboBox.getValue() == null || appTypeComboBox.getValue().equals("")) ||
            appContactsComboBox.getValue() == null || appStartComboBox.getValue() == null ||
            appUserComboBox.getValue() == null || appDurationComboBox.getValue() == null);
  }

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

  public void populateTable() {
    mainController.getTableProgress().setVisible(true);
    MainController.getDbService().submit(() -> {
      try {
        List<Appointment> appointments = Database.getAppointments();
        Platform.runLater(() -> {
          appTableView.getItems().clear();
          appTableView.getItems().addAll(filterApps(appointments, curMode));
        });
      } catch (SQLException e) {
        Platform.runLater(() -> mainController.notify("Failed to populate table. Check connection.",
            MainController.NotificationType.ERROR, false));
      } finally {
        Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
      }
    });
  }

  private void populateTypeComboBox() {
    MainController.getDbService().submit(() -> {
      List<Appointment> apps = new ArrayList<>();
      try {
        apps = Database.getAppointments();
      } catch (SQLException e) {
        e.printStackTrace();
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

  public void setCollapseToolDrawer(boolean b) {
    appToolDrawer.setCollapsible(true);
    appToolDrawer.setExpanded(!b);
    appToolDrawer.setCollapsible(false);
  }

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

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Deprecated
  private void setValHelper(ComboBoxBase box, Object val) {
    var handler = box.getOnAction();
    box.setOnAction(null);
    box.setValue(val);
    box.setOnAction(handler);
  }

  void resetToolButtons() {
    editAppBtn.setDisable(true);
    deleteAppBtn.setDisable(true);
    addAppBtn.setSelected(false);
    editAppBtn.setSelected(false);
    deleteAppBtn.setSelected(false);
  }

  void setToolDrawerEditable(boolean isEdit) {
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

  void populateCustComboBox() {
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
            () -> mainController.notify("Failed to populate customer box. Check connection.",
                MainController.NotificationType.ERROR, false));
      }
    });
  }

  void populateContactComboBox() {
    appContactsComboBox.getItems().clear();
    MainController.getDbService().submit(() -> {
      try {
        List<Contact> contacts = Database.getContacts();
        appContactsComboBox.getItems().addAll(contacts);
        reportContactBox.getItems().addAll(contacts);
      } catch (SQLException e) {
        Platform.runLater(
            () -> mainController.notify("Failed to populate contact box. Check connection.",
                MainController.NotificationType.ERROR, false));
      }
    });
  }

  private void populateUserComboBox() {
    appUserComboBox.getItems().clear();
    MainController.getDbService().submit(() -> {
      try {
        List<User> users = Database.getUsers();
        appUserComboBox.getItems().addAll(users);
      } catch (SQLException e) {
        Platform
            .runLater(() -> mainController.notify("Failed to populate user box. Check connection.",
                MainController.NotificationType.ERROR, false));
      }
    });
  }

  private List<ZonedDateTime> getAvailableTimes(Customer customer) {
    Appointment excludeApp = null;
    if (editAppBtn.isSelected()) {
      excludeApp = appTableView.getSelectionModel().getSelectedItem();
    }
    var utcStartDt = ZonedDateTime
        .of(LocalDateTime.of(appDatePicker.getValue(), LocalTime.of(12, 0)), ZoneId.of("UTC"));
    var utcEndDt = utcStartDt.plusHours(14);
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
    var tempDt = utcStartDt;
    times.add(tempDt);
    while (tempDt.isBefore(utcEndDt.minusMinutes(15))) {
      tempDt = tempDt.plusMinutes(15);
      times.add(tempDt);
    }
    List<ZonedDateTime> outTimes = new ArrayList<>();
    OUTER:
    for (var time : times) {
      if (time.plusMinutes(duration).isAfter(utcEndDt) || time.isBefore(utcStartDt)) {
        continue;
      }
      INNER:
      for (var app : custApps) {
        if (app.getStart().isBefore(time.plusMinutes(duration)) && app.getEnd().isAfter(time)) {
          continue OUTER;
        }
      }
      outTimes.add(time);
    }
    return outTimes;
  }

  private void populateStartComboBox() {
    Customer cust = appCustComboBox.getSelectionModel().getSelectedItem();
    var handler = appStartComboBox.getOnAction();
    appStartComboBox.setOnAction(null);
    appStartComboBox.setValue(null);
    appStartComboBox.getItems().clear();
    appStartComboBox.getItems().setAll(getAvailableTimes(cust));
    appStartComboBox.setOnAction(handler);
    appStartComboBox.setPromptText("Start");
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
          setText("Start");
        } else {
          setText(appStartComboBox.getConverter().toString(item));
        }
      }
    });
  }

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

  void openToolDrawer(Appointment app) {
    setCollapseToolDrawer(false);
    if (app == null) {
      clearToolDrawer();
    } else {
      appTitleField.setText(app.getTitle());
      appIdField.setText(Integer.toString(app.getAppointmentId()));
      appDescriptionField.setText(app.getDescription());
      appLocationField.setText(app.getLocation());
      setValHelper(appTypeComboBox, app.getType());
      setValHelper(appDatePicker, app.getStart().toLocalDate());
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
    tryActivateConfirmBtn();
  }


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
          mainController
              .notify("Appointment Added: " + app, MainController.NotificationType.ADD, true);
        });
      } catch (SQLException e) {
        Platform
            .runLater(() -> mainController.notify("Failed to add appointment. Check connection.",
                MainController.NotificationType.ERROR, false));
      } finally {
        Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
      }
    });
  }

  private void confirmUpdateApp(Appointment newApp, Appointment original) {
    mainController.getTableProgress().setVisible(true);
    MainController.getDbService().submit(() -> {
      try {
        Database.updateAppointment(newApp);
        Platform.runLater(() -> {
          clearToolDrawer();
          setCollapseToolDrawer(true);
          resetToolButtons();
          appTableView.getItems().set(appTableView.getItems().indexOf(original), newApp);
          mainController.notify("Appointment Updated: " + newApp,
              MainController.NotificationType.EDIT, true);
        });
      } catch (SQLException e) {
        Platform
            .runLater(() -> mainController.notify("Failed to update appointment. Check connection.",
                MainController.NotificationType.ERROR, false));
      } finally {
        Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
      }
    });
  }

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
              .notify("Appointment Removed: " + appointment, MainController.NotificationType.DELETE,
                  true);
        });
      } catch (SQLException e) {
        Platform
            .runLater(() -> mainController.notify("Failed to delete appointment. Check connection.",
                MainController.NotificationType.ERROR, false));
      } finally {
        Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
      }
    });
  }

  public void addAppointment(Customer cust) {
    appTableView.getSelectionModel().clearSelection();
    resetToolButtons();
    addAppBtn.setSelected(true);
    setToolDrawerEditable(true);
    appConfirmBtnImg.setImage(MainController.getAddImg());
    openToolDrawer(null);
    appCustComboBox.getSelectionModel().select(cust);
  }

  @FXML
  void onActionAddApp(ActionEvent event) {
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

  @FXML
  void onActionDeleteApp(ActionEvent event) {
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

  @FXML
  void onActionEditApp(ActionEvent event) {
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

  @FXML
  void onActionReport() {
    resetToolButtons();
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

  @FXML
  void onActionDatePicker(ActionEvent event) {
    if (appDatePicker.getValue() == null) {
      return;
    }
    appDurationComboBox.setDisable(false);
    var handler = appDurationComboBox.getOnAction();
    appDurationComboBox.setOnAction(null);
    appDurationComboBox.setValue(null);
    appDurationComboBox.setOnAction(handler);
    appStartComboBox.setValue(null);
    appStartComboBox.setValue(null);
    appStartComboBox.setDisable(true);
  }

  @FXML
  void onActionDurationComboBox(ActionEvent event) {
    appStartComboBox.setDisable(false);
    populateStartComboBox();
    tryActivateConfirmBtn();
  }

  @FXML
  void onActionStartComboBox(ActionEvent event) {
    tryActivateConfirmBtn();
  }


  @FXML
  void onActionCustComboBox(ActionEvent event) {
    setValHelper(appDurationComboBox, null);
    appDatePicker.setDisable(false);
    setValHelper(appDatePicker, LocalDate.now());
    setValHelper(appStartComboBox, null);
    appStartComboBox.setDisable(true);
    appDurationComboBox.setDisable(false);
  }

  @FXML
  void onActionContactComboBox(ActionEvent event) {
    tryActivateConfirmBtn();
  }

  @FXML
  void onActionUserComboBox(ActionEvent event) {
    tryActivateConfirmBtn();
  }

  @FXML
  private void onActionTypeComboBox(ActionEvent actionEvent) {
    tryActivateConfirmBtn();
  }

  @FXML
  void onKeyTypedAppField(KeyEvent event) {
    tryActivateConfirmBtn();
  }

  @FXML
  void onActionConfirm(ActionEvent event) {
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

  @FXML
  void onActionRefresh(ActionEvent event) {
    mainController.getTableProgress().setVisible(true);
    appTableView.getSelectionModel().clearSelection();
    setCollapseToolDrawer(true);
    resetToolButtons();
    MainController.getDbService().submit(() -> {
      try {
        Database.commit();
        Platform.runLater(() -> mainController.notify("Changes have been committed.",
            MainController.NotificationType.SUCCESS, false)
        );
      } catch (SQLException e) {
        Platform
            .runLater(() -> mainController.notify("Failed to refresh database. Check connection.",
                MainController.NotificationType.ERROR, false)
            );
      } finally {
        Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
      }
    });
    mainController.refresh();
  }

  @FXML
  public void onActionAppMode(ActionEvent actionEvent) {
    switch (curMode) {
      case ALL:
        curMode = Mode.MONTH;
        break;
      case MONTH:
        curMode = Mode.WEEK;
        break;
      case WEEK:
        curMode = Mode.ALL;
        break;
    }
    modeImgView.setImage(curMode.img);
    modeTooltip.setText(curMode.tipText);
    populateTable();
  }

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

  @FXML
  private AnchorPane storagePane;

  @FXML
  private StackPane toolStackPane;

  @FXML
  private Tab appTab;

  @FXML
  private Button modeBtn;

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
  private Button upcomingAppBtn;

  @FXML
  private Button appRefreshBtn;

  @FXML
  private TitledPane appToolDrawer;

  @FXML
  private TextField appTitleField;

  @FXML
  private TextArea appDescriptionField;

  @FXML
  private ComboBox<Customer> appCustComboBox;

  public ComboBox<Customer> getAppCustComboBox() {
    return appCustComboBox;
  }

  @FXML
  private TextField appLocationField;

  @FXML
  private ComboBox<Contact> appContactsComboBox;

  @FXML
  private ComboBox<User> appUserComboBox;

  @FXML
  private Button appConfirmBtn;

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

  //////////////////


  @FXML
  private ToggleGroup appToggleGroup;

  @FXML
  private ToolBar toolbar;

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
  private CategoryAxis monthTypeX;

  @FXML
  private NumberAxis monthTypeY;

  @FXML
  private HBox appTimeBar;

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
  private void onActionReportContactBox(ActionEvent actionEvent) {
    setValHelper(reportDatePicker, null);
    appTimeBar.getChildren().clear();
    setReportDatePicker(reportContactBox.getValue());
  }

  @FXML
  private void onActionReportDatePicker(ActionEvent actionEvent) {
    setTimeBar(reportContactBox.getValue());
  }

  private void calculateTimeBar(ObservableList<Node> buttons) {
    double deltaRat = appTimeBar.getWidth() / timeBarWidth;
    for (var button : buttons) {
      ((Button) button).setPrefWidth(((Button) button).getPrefWidth() * deltaRat);
    }
    timeBarWidth = appTimeBar.getWidth();
  }

  private void setTimeBar(Contact contact) {
    timeBarWidth = appTimeBar.getWidth();
    appTimeBar.getChildren().clear();
    ZonedDateTime utcStart = ZonedDateTime
        .of(LocalDateTime.of(reportDatePicker.getValue(), LocalTime.of(12, 0)), ZoneId.of("UTC"));
    List<Appointment> apps = appTableView.getItems();
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

    ZonedDateTime timePointer = utcStart;
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
      button.setStyle("-fx-background-color: #00a6ff; -fx-focus-traversable: true");
      button.hoverProperty().addListener(((observable, oldValue, show) -> {
        if (show) {
          button.setStyle("-fx-background-color: #8cd0f5; ");
        } else {
          button.setStyle("-fx-background-color: #00a6ff; ");
        }
      }));
      button.setMaxWidth(Double.MAX_VALUE);
      button.setPrefWidth(
          timeSlice * Duration.between(start, end).toMinutes() / 15f);
      appTimeBar.getChildren().add(button);
      i += skip;
      timePointer = nextApp.getEnd();
    }
    if (!timePointer.isEqual(utcStart.plusHours(14))) {
      Button last = new Button();
      last.setStyle("-fx-background-radius: 0");
      last.setMaxWidth(Double.MAX_VALUE);
      last.setPrefWidth(timeSlice * (Duration
          .between(conApps.get(conApps.size() - 1).getEnd(), utcStart.plusHours(14)).toMinutes())
          / 15f);
      last.setDisable(true);
      appTimeBar.getChildren().add(last);
    }
  }

  private void setReportDatePicker(Contact contact) {
    List<LocalDate> dates = getMarkedDays(contact);
    reportDatePicker.setDayCellFactory(picker -> new DateCell() {
      public void updateItem(LocalDate date, boolean empty) {
        super.updateItem(date, empty);
        if (dates.contains(date)) {
          setTooltip(new Tooltip("View Appointments"));
          setStyle("-fx-background-color: #00a6ff;");
          hoverProperty().addListener(((observable, oldValue, show) -> {
            if (show) {
              setStyle("-fx-background-color: #8cd0f5");
            } else {
              setStyle("-fx-background-color: #00a6ff");
            }
          }));
        } else {
          setDisable(true);
        }
      }
    });
  }

  private List<LocalDate> getMarkedDays(Contact contact) {
    List<Appointment> apps = appTableView.getItems();
    List<LocalDate> dates = new ArrayList<>();
    for (var app : apps) {
      if (app.getContact().equals(contact)) {
        dates.add(app.getStart().withZoneSameInstant(ZoneId.systemDefault()).toLocalDate());
      }
    }
    return dates;
  }

  private void setupMonthTypeChart(List<Appointment> apps, Set<String> types) {
    monthTypeChart.getData().clear();
    for (var type : types) {
      int jan = 0, feb = 0, mar = 0, apr = 0, may = 0, jun = 0, jul = 0, aug = 0, sep = 0, oct = 0, nov = 0, dec = 0;
      for (var app : apps) {
        if (app.getType().equals(type)) {
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
}
