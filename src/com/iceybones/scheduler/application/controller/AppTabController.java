package com.iceybones.scheduler.application.controller;

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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

public class AppTabController implements Initializable {

  private MainController mainController;

  public void setMainController(MainController mainController) {
    this.mainController = mainController;
  }

  public void populate() {
    populateTable();
    populateContactComboBox();
    populateUserComboBox();
    populateCustComboBox();
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
      final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm a");

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
      final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm a");

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
  }

  void tryActivateConfirmBtn() {
    appConfirmBtn
        .setDisable(appTitleField.getText().equals("") || appLocationField.getText().equals("") ||
            appTypeField.getText().equals("") || appDescriptionField.getText().equals("") ||
            appDatePicker.getValue() == null || appCustComboBox.getSelectionModel().isEmpty() ||
            appContactsComboBox.getSelectionModel().isEmpty() || appStartComboBox
            .getSelectionModel().isEmpty() || appUserComboBox.getSelectionModel().isEmpty() ||
            appDurationComboBox.getSelectionModel().isEmpty() && !deleteAppBtn.isSelected());
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
        appTableView.getItems().clear();
        appTableView.getItems().addAll(appointments);
      } catch (SQLException e) {
        Platform.runLater(() -> mainController.notify("Failed to populate table. Check connection.",
            MainController.NotificationType.ERROR, false));
      } finally {
        Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
      }
    });
  }

  public void setCollapseToolDrawer(boolean b) {
    appToolDrawer.setCollapsible(true);
    appToolDrawer.setExpanded(!b);
    appToolDrawer.setCollapsible(false);
  }

  private void clearToolDrawer() {
    appTitleField.setText("");
    appIdField.setText("");
    appLocationField.setText("");
    appDescriptionField.setText("");
    appTypeField.setText("");
    appCustComboBox.getSelectionModel().clearSelection();
    appDatePicker.setValue(LocalDate.now());
    appDatePicker.setDisable(true);
    appDurationComboBox.setDisable(true);
    appStartComboBox.setDisable(true);
    appStartComboBox.getSelectionModel().clearSelection();
    appDurationComboBox.getSelectionModel().clearSelection();
    appContactsComboBox.getSelectionModel().clearSelection();
    appStartComboBox.setPromptText("Start");
    appStartComboBox.setButtonCell(new ListCell<>() {
      @Override
      protected void updateItem(ZonedDateTime item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText("Start");
        } else {
          setText(appStartComboBox.getConverter().toString());
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
          setText(item.toString());
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
          ;
          setText(appCustComboBox.getConverter().toString(item));
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
    appTypeField.setDisable(!isEdit);
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
          appCustComboBox.setPromptText("Customer");
          appCustComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Customer customer) {
              return customer.getCustomerId() + ": " + customer.getCustomerName();
            }

            @Override
            public Customer fromString(String s) {
              return null;
            }
          });
          appCustComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Customer item, boolean empty) {
              super.updateItem(item, empty);
              if (empty || item == null) {
                setText("Customer");
              } else {
                setText(appCustComboBox.getConverter().toString(item));
              }
            }
          });
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
    var utcStartDt = ZonedDateTime
        .of(LocalDateTime.of(appDatePicker.getValue(), LocalTime.of(12, 0)), ZoneId.of("UTC"));
    var utcEndDt = utcStartDt.plusHours(14);
    int duration = appDurationComboBox.getSelectionModel().getSelectedItem();
    List<Appointment> custApps = new ArrayList<>();
    for (var app : appTableView.getItems()) {
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
    appStartComboBox.getItems().clear();
    appStartComboBox.getItems().setAll(getAvailableTimes(cust));
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
    appStartComboBox.setButtonCell(new ListCell<ZonedDateTime>() {
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
    appDurationComboBox.setPromptText("Duration");
    appDurationComboBox.setConverter(new StringConverter<Integer>() {
      @Override
      public String toString(Integer time) {
        return time.toString() + " min";
      }

      @Override
      public Integer fromString(String s) {
        return null;
      }
    });
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
    appDurationComboBox.getItems().addAll(List.of(15, 30, 45, 60, 90, 120));
  }

  void openToolDrawer(Appointment app) {
    setCollapseToolDrawer(false);
    if (app == null) {
      clearToolDrawer();
    } else {
      appTitleField.setText(app.getTitle());
      appDescriptionField.setText(app.getDescription());
      appLocationField.setText(app.getLocation());
      appTypeField.setText(app.getTitle());
      var handler = appDatePicker.getOnAction();
      appDatePicker.setOnAction(null);
      appDatePicker.setValue(app.getStart().toLocalDate());
      appDatePicker.setOnAction(handler);
      handler = appDurationComboBox.getOnAction();
      appDurationComboBox.setOnAction(null);
      appDurationComboBox.setValue(
          (int) Duration.between(app.getStart().toLocalDateTime(), app.getEnd().toLocalDateTime())
              .toMinutes());
      appDurationComboBox.setOnAction(handler);
      appStartComboBox.setValue(app.getStart());
      handler = appCustComboBox.getOnAction();
      appCustComboBox.setOnAction(null);
      appCustComboBox.getSelectionModel().select(app.getCustomer());
      appCustComboBox.setOnAction(handler);
      handler = appContactsComboBox.getOnAction();
      appContactsComboBox.setOnAction(null);
      appContactsComboBox.getSelectionModel().select(app.getContact());
      appContactsComboBox.setOnAction(handler);
      appUserComboBox.getSelectionModel().select(app.getUser());
      populateStartComboBox();
      if (deleteAppBtn.isSelected()) {
        appConfirmBtn.setDisable(false);
      }
    }
  }

  private void confirmAddApp(Appointment app) {
    mainController.getTableProgress().setVisible(true);
    MainController.getDbService().submit(() -> {
      try {
        Database.insertAppointment(app);
        Platform.runLater(() -> {
          setCollapseToolDrawer(true);
          resetToolButtons();
          populateTable();
          mainController.notify("Appointment: " + app.getTitle() +
              " has been added to the database.", MainController.NotificationType.ADD, true);
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
          mainController.notify("Appointment " + original.getTitle() + " has been updated.",
              MainController.NotificationType.EDIT, true);
        });
      } catch (SQLException e) {
        Platform
            .runLater(() -> mainController.notify("Failed to update customer. Check connection.",
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
          mainController.notify(appointment.getTitle() +
              " has been removed from the database.", MainController.NotificationType.DELETE, true);
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
      setToolDrawerEditable(true);
      appConfirmBtnImg.setImage(MainController.getAddImg());
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
      openToolDrawer(appTableView.getSelectionModel().getSelectedItem());
    } else {
      setCollapseToolDrawer(true);
//            populateAppComboBox();
      // TODO SOMETHING HERE
    }
  }

  @FXML
  void onActionDatePicker(ActionEvent event) {
    appDurationComboBox.setDisable(false);
    var handler = appDurationComboBox.getOnAction();
    appDurationComboBox.setOnAction(null);
    appDurationComboBox.getSelectionModel().clearSelection();
    appStartComboBox.getSelectionModel().clearSelection();
    appDurationComboBox.setOnAction(handler);
    appStartComboBox.setDisable(true);
  }

  @FXML
  void onActionDurationComboBox(ActionEvent event) {
    appStartComboBox.setDisable(false);
    populateStartComboBox();
  }

  @FXML
  void onActionStartComboBox(ActionEvent event) {
    tryActivateConfirmBtn();
  }


  @FXML
  void onActionCustComboBox(ActionEvent event) {
//    appDatePicker.getEditor().clear();
    appDurationComboBox.setDisable(true);
    appStartComboBox.setDisable(true);
    var handler = appDurationComboBox.getOnAction();
    appDurationComboBox.setOnAction(null);
    appDurationComboBox.getSelectionModel().clearSelection();
    appStartComboBox.getSelectionModel().clearSelection();
    appDurationComboBox.setOnAction(handler);
    appDatePicker.setDisable(false);
    appDatePicker.setValue(LocalDate.now());
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
  void onKeyTypedAppField(KeyEvent event) {
    tryActivateConfirmBtn();
  }

  @FXML
  void onActionConfirm(ActionEvent event) {
    if(deleteAppBtn.isSelected()) {
      confirmDeleteApp(appTableView.getSelectionModel().getSelectedItem());
      return;
    }
    Appointment app = new Appointment();
    app.setTitle(appTitleField.getText());
    app.setType(appTypeField.getText());
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
    } else if (editAppBtn.isSelected()) {
      confirmUpdateApp(app, appTableView.getSelectionModel().getSelectedItem());
    }
  }

  @FXML
  void onActionRefresh(ActionEvent event) {
    mainController.getTableProgress().setVisible(true);
    appTableView.getSelectionModel().clearSelection();
    setCollapseToolDrawer(true);
    populateTable();
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
  private Parent parent;

  @FXML
  private Tab appTab;

  @FXML
  private ToggleButton addAppBtn;

  @FXML
  private ToggleButton deleteAppBtn;

  @FXML
  private ToggleButton editAppBtn;

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
  private TextField appTypeField;


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

}
