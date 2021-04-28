package com.iceybones.scheduler.application.controller;

import com.iceybones.scheduler.application.model.Appointment;
import com.iceybones.scheduler.application.model.Customer;
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

import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;

public class MainController implements Initializable {
    private enum TabType {
        CUSTOMER, APPOINTMENT
    }
    private Map<String, Map<String, Integer>> divisions;

    /////////////////////////// Common Methods /////////////////////////////////
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Stage stage = ApplicationManager.getStage();
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.setFullScreen(true);
        stage.setTitle("iceybones Scheduler");
        populateTable(TabType.CUSTOMER);
        populateTable(TabType.APPOINTMENT);
        populateCountryBox();
        populateContactComboBox();
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
        notificationBar.setExpanded(true);
        Platform.runLater(() -> {
            notificationLbl.setText(message);
            notificationImg.setImage(image);
            undoLink.setVisible(undoable);
        });
        var service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            try {
                Thread.sleep(5000);
                notificationBar.setExpanded(false);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        service.shutdown();
    }

    @FXML
    void onActionUndoLink(ActionEvent event) {
        var service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            try {
                Platform.runLater(() -> custTableProgress.setVisible(true));
                Database.rollback();
                resetToolButtons(TabType.CUSTOMER);
                setCollapseToolDrawer(true, TabType.CUSTOMER);
                notify("Undo Successful", checkmarkImg, false);
                populateTable(TabType.CUSTOMER);
            } catch (SQLException e) {
                notify(e.getMessage(), errorImg, false);
            } finally {
                Platform.runLater(() -> custTableProgress.setVisible(false));
            }
        });
        service.shutdown();
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
                    appEndComboBox.getSelectionModel().isEmpty() && !deleteAppBtn.isSelected());
        }
    }

    public void populateTable(TabType tabType) {
        if (tabType == TabType.CUSTOMER) {
            custIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
            custNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
            custAddressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
            custPostalCodeCol.setCellValueFactory(new PropertyValueFactory<>("postalCode"));
            custDivisionCol.setCellValueFactory(new PropertyValueFactory<>("division"));
            custCountryCol.setCellValueFactory(new PropertyValueFactory<>("country"));
            custPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
            var service = Executors.newSingleThreadExecutor();
            service.submit(() -> {
                try {
                    Platform.runLater(() -> custTableProgress.setVisible(true));
                    List<Customer> customers = Database.getCustomers();
                    custTableView.getItems().clear();
                    for (var customer : customers) {
                        custTableView.getItems().add(customer);
                    }
                    populateCustComboBox();
                } catch (SQLException e) {
                    notify(e.getMessage(), errorImg, false);
                } finally {
                    Platform.runLater(() -> custTableProgress.setVisible(false));
                }
            });
            service.shutdown();
        } else {
            appIdCol.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
            appTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
            appDescriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
            appLocationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
            appContactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
            appTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
            appStartCol.setCellValueFactory(new PropertyValueFactory<>("start"));
            appEndCol.setCellValueFactory(new PropertyValueFactory<>("end"));
            appCustIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
            var service = Executors.newSingleThreadExecutor();
            service.submit(() -> {
                try {
                    Platform.runLater(() -> appTableProgress.setVisible(true));
                    List<Appointment> appointments = Database.getAppointments();
                    appTableView.getItems().clear();
                    for (var app : appointments) {
                        appTableView.getItems().add(app);
                    }
                    Platform.runLater(appTableView::refresh);
                } catch (SQLException e) {
                    notify(e.getMessage(), errorImg, false);
                } finally {
                    Platform.runLater(() -> appTableProgress.setVisible(false));
                }
            });
            service.shutdown();
        }
    }

    void setCollapseToolDrawer(boolean b, TabType tabType) {
        Platform.runLater(() -> {
            if (tabType == TabType.CUSTOMER) {
                custToolDrawer.setCollapsible(true);
                custToolDrawer.setExpanded(!b);
                custToolDrawer.setCollapsible(false);
            } else {
                appToolDrawer.setCollapsible(true);
                appToolDrawer.setExpanded(!b);
                appToolDrawer.setCollapsible(false);
            }
        });
    }

    private void clearToolDrawer(TabType tabType) {
        Platform.runLater(() -> {
            if (tabType == TabType.CUSTOMER) {
                custNameField.setText("");
                custIdField.setText("Auto-Generated");
                custPhoneField.setText("");
                custAddressField.setText("");
                custPostalCodeField.setText("");
                countryComboBox.getSelectionModel().clearSelection();
                stateComboBox.getSelectionModel().clearSelection();
                countryComboBox.setPromptText("Country");
                countryComboBox.setButtonCell(new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText("Country");
                        } else {
                            setText(item);
                        }
                    }
                });
                stateComboBox.setPromptText("State");
                stateComboBox.setButtonCell(new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText("State");
                        } else {
                            setText(item);
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
                appEndComboBox.getSelectionModel().clearSelection();
                appContactsComboBox.getSelectionModel().clearSelection();
                appStartComboBox.setPromptText("Start");
                appStartComboBox.setButtonCell(new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText("End");
                        } else {
                            setText(item);
                        }
                    }
                });
                appEndComboBox.setPromptText("End");
                appEndComboBox.setButtonCell(new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText("End");
                        } else {
                            setText(item);
                        }
                    }
                });
                appCustComboBox.setPromptText("Customer");
                appCustComboBox.setButtonCell(new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText("Customer");
                        } else {
                            setText(item);
                        }
                    }
                });
                appContactsComboBox.setPromptText("Contact");
                appContactsComboBox.setButtonCell(new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText("Contact");
                        } else {
                            setText(item);
                        }
                    }
                });
            }
        });
    }

    @FXML
    void onActionRefresh(ActionEvent event) {
        var service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            try {
                if (event.getSource() == custRefreshBtn) {
                    Platform.runLater(() -> custTableProgress.setVisible(true));
                    custTableView.getSelectionModel().clearSelection();
                    setCollapseToolDrawer(true, TabType.CUSTOMER);
                    populateTable(TabType.CUSTOMER);
                    resetToolButtons(TabType.CUSTOMER);
                } else {
                    Platform.runLater(() -> appTableProgress.setVisible(true));
                    appTableView.getSelectionModel().clearSelection();
                    setCollapseToolDrawer(true, TabType.APPOINTMENT);
                    populateTable(TabType.APPOINTMENT);
                    resetToolButtons(TabType.APPOINTMENT);
                }
                undoLink.setVisible(false);
            } finally {
                Platform.runLater(() -> {
                    custTableProgress.setVisible(false);
                    appTableProgress.setVisible(false);
                });
            }
        });
        service.shutdown();
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
            stateComboBox.setDisable(!isEdit);
        } else {
            appTitleField.setEditable(isEdit);
            appLocationField.setEditable(isEdit);
            appDescriptionField.setEditable(isEdit);
            appTypeField.setEditable(isEdit);
        }
    }

    ////////////////////////////////// Customer Tab Methods //////////////////////////////////////////

    void populateCountryBox() {
        var service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            try {
                divisions = Database.getDivisions();
                for (var country : divisions.keySet()) {
                    countryComboBox.getItems().add(country);
                }
            } catch (SQLException e) {
                notify(e.getMessage(), errorImg, false);
            }
        });
        service.shutdown();
    }

    void populateStateBox(String country) {
        stateComboBox.getItems().clear();
        divisions.get(country).keySet().stream().sorted().forEach(stateComboBox.getItems()::add);
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
            countryComboBox.getSelectionModel().select(cust.getCountry());
            countryComboBox.setOnAction(handler);
            populateStateBox(cust.getCountry());
            var handler2 = stateComboBox.getOnAction();
            stateComboBox.setOnAction(null);
            stateComboBox.getSelectionModel().select(cust.getDivision());
            stateComboBox.setOnAction(handler2);

        }
    }

    private void confirmAddCustomer(Customer customer) {
        var service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            try {
                Platform.runLater(() -> custTableProgress.setVisible(true));
                Database.insertCustomer(customer);
                notify(customer.getCustomerName() + " has been added to the database.", addImg, true);
                setCollapseToolDrawer(true, TabType.CUSTOMER);
                resetToolButtons(TabType.CUSTOMER);
                populateTable(TabType.CUSTOMER);
            } catch (SQLException e) {
                notify(e.getMessage(), errorImg, false);
            } finally {
                Platform.runLater(() -> custTableProgress.setVisible(false));
            }
        });
        service.shutdown();
    }

    private void confirmDeleteCustomer(Customer customer) {
        var service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            try {
                Platform.runLater(() -> custTableProgress.setVisible(true));
                Database.deleteCustomer(customer);
                notify(customer.getCustomerName() + " has been removed from the database.", deleteImg, true);
                clearToolDrawer(TabType.CUSTOMER);
                setCollapseToolDrawer(true, TabType.CUSTOMER);
                resetToolButtons(TabType.CUSTOMER);
                Platform.runLater(() -> {
                    custTableView.getItems().remove(customer);
                    populateCustComboBox();
                });
            } catch (SQLException e) {
                notify(e.getMessage(), errorImg, false);
            } finally {
                Platform.runLater(() -> custTableProgress.setVisible(false));
            }
        });
        service.shutdown();
    }

    private void confirmUpdateCustomer(Customer newCust, Customer original) {
        var service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            try {
                Platform.runLater(() -> custTableProgress.setVisible(true));
                Database.updateCustomer(newCust);
                notify("Customer record has been updated.", editImg, true);
                clearToolDrawer(TabType.CUSTOMER);
                setCollapseToolDrawer(true, TabType.CUSTOMER);
                resetToolButtons(TabType.CUSTOMER);
                Platform.runLater(() -> {
                    custTableView.getItems().set(custTableView.getItems().indexOf(original), newCust);
                    populateCustComboBox();
                });
            } catch (SQLException e) {
                notify(e.getMessage(), errorImg, false);
            } finally {
                Platform.runLater(() -> custTableProgress.setVisible(false));
            }
        });
        service.shutdown();
    }

    @FXML
    void onActionCustConfirm(ActionEvent event) {
        Customer customer = new Customer();
        customer.setCustomerName(custNameField.getText());
        customer.setCustomerId((custIdField.getText().equals("Auto-Generated") ? 0 : Integer.parseInt(custIdField.getText())));
        customer.setPhone(custPhoneField.getText());
        customer.setAddress(custAddressField.getText());
        customer.setPostalCode(custPostalCodeField.getText());
        customer.setCreatedBy(Database.getUser());
        customer.setLastUpdatedBy(Database.getUser());
        int divId = divisions.get(countryComboBox.getSelectionModel().getSelectedItem()).
                get(stateComboBox.getSelectionModel().getSelectedItem());
        String state = stateComboBox.getSelectionModel().getSelectedItem();
        String country = countryComboBox.getSelectionModel().getSelectedItem();
        customer.setCountry(country);
        customer.setDivisionId(divId);
        customer.setDivision(state);
        if (addCustomerBtn.isSelected()) {
            confirmAddCustomer(customer);
        } else if (deleteCustomerBtn.isSelected()) {
            confirmDeleteCustomer(custTableView.getSelectionModel().getSelectedItem());
        } else if (editCustomerBtn.isSelected()) {
            confirmUpdateCustomer(customer, custTableView.getSelectionModel().getSelectedItem());
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
        Platform.runLater(() -> {
            tabPane.getSelectionModel().select(appTab);
            setCollapseToolDrawer(false, TabType.APPOINTMENT);
            appCustComboBox.getSelectionModel().select(custTableView.getSelectionModel().getSelectedItem().toString());
        });
    }

    @FXML
    void onActionCountryComboBox(ActionEvent event) {
        stateComboBox.getItems().clear();
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
        Platform.runLater(() -> {
            appCustComboBox.getItems().clear();
            custTableView.getItems().stream().sorted().forEach((a) -> appCustComboBox.getItems().add(a.toString()));
            appCustComboBox.setPromptText("Customer");
            appCustComboBox.setButtonCell(new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Customer");
                    } else {
                        setText(item);
                    }
                }
            });
        });
    }

    void populateContactComboBox() {
        Platform.runLater(() -> {
            appContactsComboBox.getItems().clear();
            var service = Executors.newSingleThreadExecutor();
            service.submit(() -> {
                try {
                    Database.getContacts().forEach((a) -> appContactsComboBox.getItems().add(a.getContactName()));
                } catch (SQLException e) {
                    notify(e.getMessage(), errorImg, false);
                }
            });
            service.shutdown();
            custTableView.getItems().stream().sorted().forEach((a) -> appCustComboBox.getItems().add(a.toString()));
            appCustComboBox.setPromptText("Customer");
            appCustComboBox.setButtonCell(new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Customer");
                    } else {
                        setText(item);
                    }
                }
            });
        });
    }

    void openAppToolDrawer(Appointment app) {
        setCollapseToolDrawer(false, TabType.APPOINTMENT);
        if (app == null) {
            clearToolDrawer(TabType.APPOINTMENT);
        } else {
            custIdField.setText(Integer.toString(app.getCustomerId()));
            var handler = appCustComboBox.getOnAction();
            appCustComboBox.setOnAction(null);
//            appCustComboBox.getSelectionModel().select(app.getContact().toString());
            //TODO THIS NEEDS ATTENTION!
            appCustComboBox.setOnAction(handler);
            var handler2 = appContactsComboBox.getOnAction();
            appContactsComboBox.setOnAction(null);
            appContactsComboBox.getSelectionModel().select(app.getContact().toString());
            appContactsComboBox.setOnAction(handler2);

        }
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
    void onActionAppConfirm(ActionEvent event) {

    }

    @FXML
    void onActionDatePicker(ActionEvent event) {

    }

    @FXML
    void onActionEndComboBox(ActionEvent event) {

    }

    @FXML
    void onActionStartComboBox(ActionEvent event) {

    }


    @FXML
    void onActionCustComboBox(ActionEvent event) {
        appDatePicker.setDisable(false);
        appStartComboBox.setDisable(false);
        appEndComboBox.setDisable(false);
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
    private ComboBox<String> appCustComboBox;

    @FXML
    private TextField appLocationField;

    @FXML
    private ComboBox<String> appContactsComboBox;

    @FXML
    private Button appConfirmBtn;

    @FXML
    private ImageView appConfirmBtnImg;

    @FXML
    private ComboBox<String> appStartComboBox;

    @FXML
    private ComboBox<String> appEndComboBox;

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
    private TableColumn<Appointment, Timestamp> appStartCol;

    @FXML
    private TableColumn<Appointment, Timestamp> appEndCol;

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
    private ComboBox<String> countryComboBox;

    @FXML
    private ComboBox<String> stateComboBox;

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
