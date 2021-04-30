package com.iceybones.scheduler.application.controller;

import com.iceybones.scheduler.application.model.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController implements Initializable {
    private static ExecutorService notifyService = Executors.newSingleThreadExecutor();
    private static final ExecutorService dbService = Executors.newSingleThreadExecutor();

    private enum TabType {
        CUSTOMER, APPOINTMENT
    }

    public static void stopNotifyService() {
        notifyService.shutdown();
    }
    public static void stopDbService() {
        dbService.shutdown();
    }

    private void cancelNotify() {
        notifyService.shutdownNow();
        notifyService = Executors.newSingleThreadExecutor();
    }

    /////////////////////////// Common Methods /////////////////////////////////
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        populateDurationComboBox();
        Stage stage = ApplicationManager.getStage();
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.setFullScreen(true);
        stage.setTitle("iceybones Scheduler");
        setupTables();
        populateTable(TabType.CUSTOMER);
        populateTable(TabType.APPOINTMENT);
        populateCountryBox();
        populateContactComboBox();
        populateUserComboBox();
        appDatePicker.setDayCellFactory(picker -> new DateCell() {
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                setDisable(empty || date.compareTo(today) < 0 );
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
        custTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                deleteCustomerBtn.setDisable(false);
                editCustomerBtn.setDisable(false);
                addCustAppBtn.setDisable(false);
                if (oldSelection != null && custToolDrawer.isExpanded()) {
                    openCustToolDrawer(custTableView.getSelectionModel().getSelectedItem());
                }
                if (addCustomerBtn.isSelected()) {
                    addCustomerBtn.setSelected(false);
                    setCollapseToolDrawer(true, TabType.CUSTOMER);
                }
            }
        });
        appTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                deleteAppBtn.setDisable(false);
                editAppBtn.setDisable(false);
                addAppBtn.setDisable(false);
                if (oldSelection != null && appToolDrawer.isExpanded()) {
                    openAppToolDrawer(appTableView.getSelectionModel().getSelectedItem());
                }
                if (addAppBtn.isSelected()) {
                    addAppBtn.setSelected(false);
                    setCollapseToolDrawer(true, TabType.APPOINTMENT);
                }
            }
        });
    }

    private void notify(String message, Image image, Boolean undoable) {
        cancelNotify();
        notificationBar.setExpanded(true);
        notificationLbl.setText(message);
        notificationImg.setImage(image);
        undoLink.setVisible(undoable);
        notifyService.submit(() -> {
            try {
                Thread.sleep(5000);
                Platform.runLater(() -> notificationBar.setExpanded(false));
            } catch (InterruptedException e) {

            }
        });
        notifyService.shutdown();
    }

    @FXML
    void onActionUndoLink(ActionEvent event) {
        dbService.submit(() -> {
            try {
                Platform.runLater(() -> custTableProgress.setVisible(true));
                Database.rollback();
                Platform.runLater(() -> {
                    resetToolButtons(TabType.CUSTOMER);
                    setCollapseToolDrawer(true, TabType.CUSTOMER);
                    notify("Undo Successful", checkmarkImg, false);
                    populateTable(TabType.CUSTOMER);
                });
            } catch (SQLException e) {
                Platform.runLater(() -> notify(e.getMessage(), errorImg, false));
            } finally {
                Platform.runLater(() -> custTableProgress.setVisible(false));
            }
        });
    }

    void tryActivateConfirmBtn(TabType tabType) {
        if (tabType == TabType.CUSTOMER) {
            custConfirmBtn.setDisable(custNameField.getText().equals("") || custPhoneField.getText().equals("") ||
                    custAddressField.getText().equals("") || custPostalCodeField.getText().equals("") ||
                    countryComboBox.getSelectionModel().isEmpty() || stateComboBox.getSelectionModel().isEmpty() &&
                    !deleteCustomerBtn.isSelected());
        } else {
            appConfirmBtn.setDisable(appTitleField.getText().equals("") || appLocationField.getText().equals("") ||
                    appTypeField.getText().equals("") || appDescriptionField.getText().equals("") ||
                    appDatePicker.getValue() == null || appCustComboBox.getSelectionModel().isEmpty() ||
                    appContactsComboBox.getSelectionModel().isEmpty() || appStartComboBox.getSelectionModel().isEmpty() ||
                    appDurationComboBox.getSelectionModel().isEmpty() && !deleteAppBtn.isSelected());
        }
    }

    private void setupTables() {
        custIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        custNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        custAddressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        custPostalCodeCol.setCellValueFactory(new PropertyValueFactory<>("postalCode"));
        custDivisionCol.setCellValueFactory(new PropertyValueFactory<>("division"));
        custCountryCol.setCellValueFactory(new PropertyValueFactory<>("country"));
        custPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

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

    public void populateTable(TabType tabType) {
        if (tabType == TabType.CUSTOMER) {
            dbService.submit(() -> {
                Platform.runLater(() -> custTableProgress.setVisible(true));
                List<Customer> customers = Database.getCustomers();
                Platform.runLater(() -> {
                    custTableView.getItems().clear();
                    custTableView.getItems().addAll(customers);
                    populateCustComboBox();
                    custTableProgress.setVisible(false);
                });
            });
        } else {
            dbService.submit(() -> {
                Platform.runLater(() -> appTableProgress.setVisible(true));
                List<Appointment> appointments = Database.getAppointments();
                Platform.runLater(() -> {
                    appTableView.getItems().clear();
                    appTableView.getItems().addAll(appointments);
                    appTableProgress.setVisible(false);
                });
            });
        }
    }

    void setCollapseToolDrawer(boolean b, TabType tabType) {
        if (tabType == TabType.CUSTOMER) {
            custToolDrawer.setCollapsible(true);
            custToolDrawer.setExpanded(!b);
            custToolDrawer.setCollapsible(false);
        } else {
            appToolDrawer.setCollapsible(true);
            appToolDrawer.setExpanded(!b);
            appToolDrawer.setCollapsible(false);
        }
    }

    private void clearToolDrawer(TabType tabType) {
        if (tabType == TabType.CUSTOMER) {
            custNameField.setText("");
            custIdField.setText("Auto-Generated");
            custPhoneField.setText("");
            custAddressField.setText("");
            custPostalCodeField.setText("");
            countryComboBox.getSelectionModel().clearSelection();
            stateComboBox.getSelectionModel().clearSelection();
            stateComboBox.setDisable(true);
            countryComboBox.setPromptText("Country");
            countryComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Country item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Country");
                    } else {
                        setText(item.getCountry());
                    }
                }
            });
            stateComboBox.setPromptText("State");
            stateComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Division item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("State");
                    } else {
                        setText(item.getDivision());
                    }
                }
            });
        } else {
            appTitleField.setText("");
            appIdField.setText("Auto-Generated");
            appLocationField.setText("");
            appDescriptionField.setText("");
            appTypeField.setText("");
            appCustComboBox.getSelectionModel().clearSelection();
            appDatePicker.setValue(LocalDate.now());
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
    }

    @FXML
    void onActionRefresh(ActionEvent event) {
        dbService.submit(() -> {
            try {
                if (event.getSource() == custRefreshBtn) {
                    Platform.runLater(() -> {
                        custTableProgress.setVisible(true);
                        custTableView.getSelectionModel().clearSelection();
                        setCollapseToolDrawer(true, TabType.CUSTOMER);
                        populateTable(TabType.CUSTOMER);
                        resetToolButtons(TabType.CUSTOMER);
                    });
                    Database.commit();
                } else {
                    Platform.runLater(() -> {
                        appTableProgress.setVisible(true);
                        appTableView.getSelectionModel().clearSelection();
                        setCollapseToolDrawer(true, TabType.APPOINTMENT);
                        populateTable(TabType.APPOINTMENT);
                        resetToolButtons(TabType.APPOINTMENT);
                    });
                }
            } catch (SQLException e) {
                Platform.runLater(() ->notify(e.getMessage(), errorImg, false));
            } finally {
                Platform.runLater(() -> {
                    custTableProgress.setVisible(false);
                    appTableProgress.setVisible(false);
                    undoLink.setVisible(false);
                });
            }
        });
    }

    void resetToolButtons(TabType tabType) {
        if (tabType == TabType.CUSTOMER) {
            editCustomerBtn.setDisable(true);
            deleteCustomerBtn.setDisable(true);
            addCustAppBtn.setDisable(true);
            addCustomerBtn.setSelected(false);
            editCustomerBtn.setSelected(false);
            deleteCustomerBtn.setSelected(false);
        } else {
            editAppBtn.setDisable(true);
            deleteAppBtn.setDisable(true);
            addAppBtn.setSelected(false);
            editAppBtn.setSelected(false);
            deleteAppBtn.setSelected(false);
        }
    }

    void setToolDrawerEditable(boolean isEdit, TabType tabType) {
        if (tabType == TabType.CUSTOMER) {
            custNameField.setEditable(isEdit);
            custPhoneField.setEditable(isEdit);
            custAddressField.setEditable(isEdit);
            custPostalCodeField.setEditable(isEdit);
            countryComboBox.setDisable(!isEdit);
        } else {
            appTitleField.setEditable(isEdit);
            appLocationField.setEditable(isEdit);
            appDescriptionField.setEditable(isEdit);
            appTypeField.setEditable(isEdit);
        }
    }

    ////////////////////////////////// Customer Tab Methods //////////////////////////////////////////

    void populateCountryBox() {
        dbService.submit(() -> {
            var countries = Database.getCountries();
            Platform.runLater(() -> countryComboBox.getItems().addAll(countries));
        });
    }

    void populateStateBox(Country country) {
        stateComboBox.getItems().clear();
        dbService.submit(() -> {
            var divisions = Database.getDivisionsByCountry(country);
            Platform.runLater(() -> stateComboBox.getItems().addAll(divisions));
        });
    }

    void openCustToolDrawer(Customer cust) {
        setCollapseToolDrawer(false, TabType.CUSTOMER);
        if (cust == null) {
            clearToolDrawer(TabType.CUSTOMER);
        } else {
            custIdField.setText(Integer.toString(cust.getCustomerId()));
            custNameField.setText(cust.getCustomerName());
            custPhoneField.setText(cust.getPhone());
            custAddressField.setText(cust.getAddress());
            custPostalCodeField.setText(cust.getPostalCode());
            var handler = countryComboBox.getOnAction();
            countryComboBox.setOnAction(null);
            countryComboBox.getSelectionModel().select(cust.getDivision().getCountry());
            countryComboBox.setOnAction(handler);
            populateStateBox(cust.getDivision().getCountry());
            var handler2 = stateComboBox.getOnAction();
            stateComboBox.setOnAction(null);
            stateComboBox.getSelectionModel().select(cust.getDivision());
            stateComboBox.setOnAction(handler2);

        }
    }

    private void confirmAddCustomer(Customer customer) {
        dbService.submit(() -> {
            Platform.runLater(() -> custTableProgress.setVisible(true));
            if(!Database.insertCustomer(customer)) {
                Platform.runLater(() -> notify("Failed to add customer. Check input.", errorImg, false));
            }
            Platform.runLater(() -> {
                notify(customer.getCustomerName() + " has been added to the database.", addImg, true);
                setCollapseToolDrawer(true, TabType.CUSTOMER);
                resetToolButtons(TabType.CUSTOMER);
                populateTable(TabType.CUSTOMER);
            });
        });
    }

    private void confirmDeleteCustomer(Customer customer) {
        dbService.submit(() -> {
            Platform.runLater(() -> custTableProgress.setVisible(true));
            if (!Database.deleteCustomer(customer)) {
                Platform.runLater(() -> notify("Failed to delete customer. Check connection.", errorImg, false));
            }
            Platform.runLater(() -> {
                notify(customer.getCustomerName() + " has been removed from the database.", deleteImg, true);
                clearToolDrawer(TabType.CUSTOMER);
                setCollapseToolDrawer(true, TabType.CUSTOMER);
                resetToolButtons(TabType.CUSTOMER);
                custTableView.getItems().remove(customer);
                populateCustComboBox();
                custTableProgress.setVisible(false);
            });
        });
    }

    private void confirmUpdateCustomer(Customer newCust, Customer original) {
        dbService.submit(() -> {
            Platform.runLater(() -> custTableProgress.setVisible(true));
            if(!Database.updateCustomer(newCust)) {
                Platform.runLater(() -> notify("Failed to update customer. Check connection.", errorImg, false));
            }
            Platform.runLater(() -> {
                notify("Customer " + original.getCustomerId() +  " has been updated.", editImg, true);
                clearToolDrawer(TabType.CUSTOMER);
                setCollapseToolDrawer(true, TabType.CUSTOMER);
                resetToolButtons(TabType.CUSTOMER);
                custTableView.getItems().set(custTableView.getItems().indexOf(original), newCust);
                populateCustComboBox();
                custTableProgress.setVisible(false);
            });
        });
    }

    @FXML
    void onActionConfirm(ActionEvent event) {
        if (event.getSource().equals(custConfirmBtn)) {
            Customer customer = new Customer();
            customer.setCustomerName(custNameField.getText());
            customer.setCustomerId((custIdField.getText().equals("Auto-Generated") ? 0 : Integer.parseInt(custIdField.getText())));
            customer.setPhone(custPhoneField.getText());
            customer.setAddress(custAddressField.getText());
            customer.setPostalCode(custPostalCodeField.getText());
            customer.setCreatedBy(Database.getUser(appUserComboBox.getValue().getUserId()));
            customer.setLastUpdatedBy(Database.getConnectedUser());
            customer.setDivision(stateComboBox.getValue());
            if (addCustomerBtn.isSelected()) {
                confirmAddCustomer(customer);
            } else if (deleteCustomerBtn.isSelected()) {
                confirmDeleteCustomer(custTableView.getSelectionModel().getSelectedItem());
            } else if (editCustomerBtn.isSelected()) {
                confirmUpdateCustomer(customer, custTableView.getSelectionModel().getSelectedItem());
            }
        } else {
            Appointment app = new Appointment();
            app.setTitle(appTitleField.getText());
//            app.setCustomerId((custIdField.getText().equals("Auto-Generated") ? 0 : Integer.parseInt(custIdField.getText())));
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
            } else if (deleteAppBtn.isSelected()) {
                confirmDeleteApp(appTableView.getSelectionModel().getSelectedItem());
            } else if (editAppBtn.isSelected()) {
//                confirmUpdateCustomer(app, custTableView.getSelectionModel().getSelectedItem());
            }
        }
    }

    @FXML
    void onActionAddCust(ActionEvent event) {
        custTableView.getSelectionModel().clearSelection();
        editCustomerBtn.setDisable(true);
        deleteCustomerBtn.setDisable(true);
        addCustAppBtn.setDisable(true);
        if (addCustomerBtn.isSelected()) {
            setToolDrawerEditable(true, TabType.CUSTOMER);
            custConfirmBtnImg.setImage(addImg);
            openCustToolDrawer(null);
        } else {
            setCollapseToolDrawer(true, TabType.CUSTOMER);
        }
    }

    @FXML
    void onActionDeleteCust(ActionEvent event) {
        if (deleteCustomerBtn.isSelected()) {
            setToolDrawerEditable(false, TabType.CUSTOMER);
            custConfirmBtnImg.setImage(deleteImg);
            custConfirmBtn.setDisable(false);
            openCustToolDrawer(custTableView.getSelectionModel().getSelectedItem());
        } else {
            setCollapseToolDrawer(true, TabType.CUSTOMER);
        }
    }

    @FXML
    void onActionEditCust(ActionEvent event) {
        if (editCustomerBtn.isSelected()) {
            setToolDrawerEditable(true, TabType.CUSTOMER);
            custConfirmBtnImg.setImage(editImg);
            custConfirmBtn.setDisable(true);
            openCustToolDrawer(custTableView.getSelectionModel().getSelectedItem());
        } else {
            setCollapseToolDrawer(true, TabType.CUSTOMER);
            populateCustComboBox();
        }
    }

    @FXML
    void onActionAddCustApp(ActionEvent event) {
        tabPane.getSelectionModel().select(appTab);
        setCollapseToolDrawer(false, TabType.APPOINTMENT);
        appCustComboBox.getSelectionModel().select(custTableView.getSelectionModel().getSelectedItem());
    }

    @FXML
    void onActionCountryComboBox(ActionEvent event) {
        stateComboBox.getItems().clear();
        stateComboBox.setDisable(false);
        if (countryComboBox.getSelectionModel().getSelectedItem() != null) {
            populateStateBox(countryComboBox.getSelectionModel().getSelectedItem());
        }
        tryActivateConfirmBtn(TabType.CUSTOMER);
    }

    @FXML
    void onActionStateComboBox(ActionEvent event) {
        tryActivateConfirmBtn(TabType.CUSTOMER);
    }

    @FXML
    void onKeyTypedCustField(KeyEvent event) {
        tryActivateConfirmBtn(TabType.CUSTOMER);
    }

    ////////////////////////////////// Appointment Tab Methods ////////////////////////////////////////

    @FXML
    void onKeyTypedAppField(KeyEvent event) {
        tryActivateConfirmBtn(TabType.APPOINTMENT);
    }

    void populateCustComboBox() {
        appCustComboBox.getItems().clear();
        appCustComboBox.getItems().setAll(custTableView.getItems().sorted());
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
    }

    void populateContactComboBox() {
        // TODO SOMETHING HERE MAYBE
            appContactsComboBox.getItems().clear();
            dbService.submit(() -> {
                List<Contact> contacts = Database.getContacts();
                Platform.runLater(() -> appContactsComboBox.getItems().addAll(contacts));
            });
    }

    private void populateUserComboBox() {
        // TODO SOMETHING HERE MAYBE
        appUserComboBox.getItems().clear();
        dbService.submit(() -> {
            List<User> users = Database.getUsers();
            Platform.runLater(() -> appUserComboBox.getItems().addAll(users));
        });
    }

    private List<ZonedDateTime> getAvailableTimes(Customer customer) {
        var utcStartDt = ZonedDateTime.of(LocalDateTime.of(appDatePicker.getValue(), LocalTime.of(12,0)), ZoneId.of("UTC"));
        var utcEndDt = utcStartDt.plusHours(14);
        int duration = appDurationComboBox.getSelectionModel().getSelectedItem();
        List<Appointment> custApps = new ArrayList<>();
        for(var app : appTableView.getItems()) {
            if(app.getCustomer().equals(customer) &&
                    app.getStart().toLocalDateTime().toLocalDate().equals(appDatePicker.getValue()))
            {
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
        return  outTimes;
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
        appDurationComboBox.getItems().addAll(List.of(15,30,45,60,90,120));
    }

    void openAppToolDrawer(Appointment app) {
        setCollapseToolDrawer(false, TabType.APPOINTMENT);
        if (app == null) {
            clearToolDrawer(TabType.APPOINTMENT);
        } else {
            custIdField.setText(Integer.toString(app.getCustomer().getCustomerId()));
            var handler = appCustComboBox.getOnAction();
            appCustComboBox.setOnAction(null);
//            appCustComboBox.getSelectionModel().select(app.getContact().toString());
            //TODO THIS NEEDS ATTENTION!
            appCustComboBox.setOnAction(handler);
            var handler2 = appContactsComboBox.getOnAction();
            appContactsComboBox.setOnAction(null);
            appContactsComboBox.getSelectionModel().select(app.getContact());
            appContactsComboBox.setOnAction(handler2);

        }
    }

    private void confirmAddApp(Appointment app) {
        dbService.submit(() -> {
            Platform.runLater(() -> appTableProgress.setVisible(true));
            if(!Database.insertAppointment(app)) {
                Platform.runLater(() -> notify("Failed to add appointment. Check connection.", errorImg, false));
            }
            Platform.runLater(() -> {
                notify("Appointment: " + app.getTitle() + " has been added to the database.", addImg, true);
                setCollapseToolDrawer(true, TabType.APPOINTMENT);
                resetToolButtons(TabType.APPOINTMENT);
                populateTable(TabType.APPOINTMENT);
                appTableProgress.setVisible(false);
            });
        });
    }

    private void confirmDeleteApp(Appointment appointment) {
        dbService.submit(() -> {
            Platform.runLater(() -> appTableProgress.setVisible(true));
            if (!Database.deleteAppointment(appointment)) {
                Platform.runLater(() -> notify("Failed to delete appointment. Check connection.", errorImg, false));
            }
            Platform.runLater(() -> {
                notify(appointment.getTitle() + " has been removed from the database.", deleteImg, true);
                clearToolDrawer(TabType.APPOINTMENT);
                setCollapseToolDrawer(true, TabType.APPOINTMENT);
                resetToolButtons(TabType.APPOINTMENT);
                appTableView.getItems().remove(appointment);
//                populateCustComboBox();
                custTableProgress.setVisible(false);
            });
        });
    }

    @FXML
    void onActionAddApp(ActionEvent event) {
        appTableView.getSelectionModel().clearSelection();
        editAppBtn.setDisable(true);
        deleteAppBtn.setDisable(true);
        if (addAppBtn.isSelected()) {
            setToolDrawerEditable(true, TabType.APPOINTMENT);
            appConfirmBtnImg.setImage(addImg);
            openAppToolDrawer(null);
        } else {
            setCollapseToolDrawer(true, TabType.APPOINTMENT);
        }
    }

    @FXML
    void onActionDeleteApp(ActionEvent event) {
        if (deleteAppBtn.isSelected()) {
            setToolDrawerEditable(false, TabType.APPOINTMENT);
            appConfirmBtnImg.setImage(deleteImg);
            appConfirmBtn.setDisable(false);
            openAppToolDrawer(appTableView.getSelectionModel().getSelectedItem());
        } else {
            setCollapseToolDrawer(true, TabType.APPOINTMENT);
        }
    }

    @FXML
    void onActionEditApp(ActionEvent event) {
        if (editAppBtn.isSelected()) {
            setToolDrawerEditable(true, TabType.APPOINTMENT);
            appConfirmBtnImg.setImage(editImg);
            appConfirmBtn.setDisable(true);
            openAppToolDrawer(appTableView.getSelectionModel().getSelectedItem());
        } else {
            setCollapseToolDrawer(true, TabType.APPOINTMENT);
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
        tryActivateConfirmBtn(TabType.APPOINTMENT);
    }


    @FXML
    void onActionCustComboBox(ActionEvent event) {
        appDatePicker.getEditor().clear();
        appDurationComboBox.setDisable(true);
        appStartComboBox.setDisable(true);
        var handler = appDurationComboBox.getOnAction();
        appDurationComboBox.setOnAction(null);
        appDurationComboBox.getSelectionModel().clearSelection();
        appStartComboBox.getSelectionModel().clearSelection();
        appDurationComboBox.setOnAction(handler);
        appDatePicker.setDisable(false);
    }

    @FXML
    void onActionContactComboBox(ActionEvent event) {

    }

    @FXML
    void onActionUserComboBox(ActionEvent event) {

    }

    @FXML
    private ImageView custConfirmBtnImg;
    private final Image addImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream("../resources/add_icon.png")));
    private final Image deleteImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream("../resources/blue_remove_icon.png")));
    private final Image editImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream("../resources/edit_icon.png")));
    private final Image errorImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream("../resources/remove_icon.png")));
    private final Image checkmarkImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream("../resources/checkmark_icon.png")));

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab appTab;

    @FXML
    private Tab custTab;

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

    @FXML
    private ToggleButton addCustomerBtn;

    @FXML
    private ToggleGroup custToggleGroup;

    @FXML
    private ToggleGroup appToggleGroup;

    @FXML
    private ToggleButton deleteCustomerBtn;

    @FXML
    private ToggleButton editCustomerBtn;

    @FXML
    private Button addCustAppBtn;

    @FXML
    private Button custRefreshBtn;

    @FXML
    private TitledPane custToolDrawer;

    @FXML
    private TextField custNameField;

    @FXML
    private TextField custAddressField;

    @FXML
    private TextField custIdField;

    @FXML
    private TextField custPostalCodeField;

    @FXML
    private TextField custPhoneField;

    @FXML
    private ComboBox<Country> countryComboBox;

    @FXML
    private ComboBox<Division> stateComboBox;

    @FXML
    private Button custConfirmBtn;


    @FXML
    private TableView<Customer> custTableView;

    @FXML
    private TableColumn<Customer, Integer> custIdCol;

    @FXML
    private TableColumn<Customer, String> custNameCol;

    @FXML
    private TableColumn<Customer, String> custAddressCol;

    @FXML
    private TableColumn<Customer, String> custPostalCodeCol;

    @FXML
    private TableColumn<Customer, String> custDivisionCol;

    @FXML
    private TableColumn<Customer, String> custCountryCol;

    @FXML
    private TableColumn<Customer, String> custPhoneCol;

    @FXML
    private TitledPane notificationBar;

    @FXML
    private ImageView notificationImg;

    @FXML
    private Label notificationLbl;

    @FXML
    private ProgressIndicator custTableProgress;

    @FXML
    private ProgressIndicator appTableProgress;

    @FXML
    private Hyperlink undoLink;

}
