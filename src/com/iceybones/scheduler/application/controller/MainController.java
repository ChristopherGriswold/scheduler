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
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;

public class MainController implements Initializable {
    private Map<String, Map<String, Integer>> divisions;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Stage stage = ApplicationManager.getStage();
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.setFullScreen(true);
        stage.setTitle("iceybones Scheduler");
        populateCustomerTable();
        populateAppointmentTable();
        populateCountryBox();
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
                    closeCustToolDrawer();
                }
            }
        });
    }

    public void populateCustomerTable() {
        custIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        custNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        custAddressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        custPostalCodeCol.setCellValueFactory(new PropertyValueFactory<>("postalCode"));
        custDivisionCol.setCellValueFactory(new PropertyValueFactory<>("division"));
        custCountryCol.setCellValueFactory(new PropertyValueFactory<>("country"));
        custPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        Runnable getCustomers = () -> {
            try {
                Platform.runLater(() -> custTableProgress.setVisible(true));
                List<Customer> customers = Database.getCustomers();
                custTableView.getItems().clear();
                for (var customer : customers) {
                    custTableView.getItems().add(customer);
                }
                Platform.runLater(custTableView::refresh);
                Platform.runLater(() -> custTableProgress.setVisible(false));
            } catch (Database.NotAuthorizedException | SQLException e) {
                e.printStackTrace();
            }
        };
        var service = Executors.newSingleThreadExecutor();
        service.submit(getCustomers);
        service.shutdown();
    }

    public void populateAppointmentTable() {
        appIdCol.setCellValueFactory(new PropertyValueFactory<>("appointmentId"));
        appTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        appDescriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        appLocationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        appContactCol.setCellValueFactory(new PropertyValueFactory<>("contact"));
        appTypeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        appStartCol.setCellValueFactory(new PropertyValueFactory<>("start"));
        appEndCol.setCellValueFactory(new PropertyValueFactory<>("end"));
        appCustIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
        Runnable getAppointments = () -> {
            try {
                Platform.runLater(() -> appTableProgress.setVisible(true));
                List<Appointment> appointments = Database.getAppointments();
                for (var app : appointments) {
                    appTableView.getItems().add(app);
                }
                Platform.runLater(appTableView::refresh);
                Platform.runLater(() -> appTableProgress.setVisible(false));
            } catch (Database.NotAuthorizedException | SQLException e) {
                e.printStackTrace();
            }
        };
        var service = Executors.newSingleThreadExecutor();
        service.submit(getAppointments);
        service.shutdown();
    }

    void populateCountryBox() {
        Runnable getCountries = () -> {
            try {
                divisions = Database.getDivisions();
                for (var country : divisions.keySet()) {
                    countryComboBox.getItems().add(country);
                }
            } catch (Database.NotAuthorizedException | SQLException e) {
                e.printStackTrace();
            }
        };
        var service = Executors.newSingleThreadExecutor();
        service.submit(getCountries);
        service.shutdown();
    }

    void populateStateBox(String country) {
        divisions.get(country).keySet().stream().sorted().forEach(stateComboBox.getItems()::add);
    }

    void openCustToolDrawer(Customer cust) {
        custToolDrawer.setCollapsible(true);
        custToolDrawer.setExpanded(true);
        custToolDrawer.setCollapsible(false);
        if (cust == null) {
            custIdField.setText("Auto-Generated");
            custNameField.clear();
            custPhoneField.clear();
            custAddressField.clear();
            custPostalCodeField.clear();
            countryComboBox.getSelectionModel().clearSelection();
            stateComboBox.getSelectionModel().clearSelection();
            countryComboBox.setPromptText("Country");
            countryComboBox.setButtonCell(new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty) ;
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
                    super.updateItem(item, empty) ;
                    if (empty || item == null) {
                        setText("State");
                    } else {
                        setText(item);
                    }
                }
            });
        } else {
            custIdField.setText(Integer.toString(cust.getCustomerId()));
            custNameField.setText(cust.getCustomerName());
            custPhoneField.setText(cust.getPhone());
            custAddressField.setText(cust.getAddress());
            custPostalCodeField.setText(cust.getPostalCode());
            countryComboBox.getSelectionModel().select(cust.getCountry());
            stateComboBox.getSelectionModel().select(cust.getDivision());
        }
    }

    void closeCustToolDrawer() {
        custToolDrawer.setCollapsible(true);
        custToolDrawer.setExpanded(false);
        custToolDrawer.setCollapsible(false);
    }

    private void confirmAddCustomer(Customer customer) {
        Runnable putCustomer = () -> {
            try {
                Platform.runLater(() -> custTableProgress.setVisible(true));
                boolean isSuccessful = Database.insertCustomer(customer);
                if (isSuccessful) {
                    notify(customer.getCustomerName() + " has been added to the database.", addImg);
                    clearCustToolDrawer();
                    populateCustomerTable();
                }
                Platform.runLater(() -> custTableProgress.setVisible(false));
            } catch (Database.NotAuthorizedException | SQLException e) {
                e.printStackTrace();
            }
        };
        var service = Executors.newSingleThreadExecutor();
        service.submit(putCustomer);
        service.shutdown();
    }

    private void confirmDeleteCustomer(Customer customer) {
        Runnable deleteCustomer = () -> {
            try {
                Platform.runLater(() -> custTableProgress.setVisible(true));
                boolean isSuccessful = Database.deleteCustomer(customer);
                if (isSuccessful) {
                    clearCustToolDrawer();
                    notify(customer.getCustomerName() + " has been removed from the database.", deleteImg);
                    Platform.runLater(() -> custTableView.getItems().remove(customer));
                }
                Platform.runLater(() -> custTableProgress.setVisible(false));
            } catch (Database.NotAuthorizedException | SQLException e) {
                notify(e.getMessage(), errorImg);
            }
        };
        var service = Executors.newSingleThreadExecutor();
        service.submit(deleteCustomer);
        service.shutdown();
    }

    private void confirmUpdateCustomer(Customer newCust, Customer original) {
        var service = Executors.newSingleThreadExecutor();
        service.submit(() -> {
            try {
                Platform.runLater(() -> custTableProgress.setVisible(true));
                boolean isSuccessful = Database.updateCustomer(newCust);
                if (isSuccessful) {
                    clearCustToolDrawer();
                    notify("Customer record has been updated.", editImg);
                    custTableView.getItems().set(custTableView.getItems().indexOf(original), newCust);
                }
                Platform.runLater(() -> custTableProgress.setVisible(false));
            } catch (Database.NotAuthorizedException | SQLException e) {
                notify(e.getMessage(), errorImg);
            }
        });
        service.shutdown();
    }

    private void clearCustToolDrawer() {
        Platform.runLater(() -> {
            custNameField.setText("");
            custIdField.setText("Auto-Generated");
            custPhoneField.setText("");
            custAddressField.setText("");
            custPostalCodeField.setText("");
            addCustomerBtn.setSelected(false);
            custToolDrawer.setCollapsible(true);
            custToolDrawer.setExpanded(false);
            custToolDrawer.setCollapsible(false);
            editCustomerBtn.setSelected(false);
            editCustomerBtn.setDisable(true);
            deleteCustomerBtn.setSelected(false);
            deleteCustomerBtn.setDisable(true);
            addCustAppBtn.setSelected(false);
            addCustAppBtn.setDisable(true);
            custConfirmBtn.setDisable(true);
        });
    }

    private void notify(String message, Image image) {
        notificationBar.setExpanded(true);
        Platform.runLater(() -> {
            notificationLbl.setText(message);
            notificationImg.setImage(image);
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
            openCustToolDrawer(null);
            confirmBtnImg.setImage(addImg);
            custNameField.setEditable(true);
            custPhoneField.setEditable(true);
            custAddressField.setEditable(true);
            custPostalCodeField.setEditable(true);
            countryComboBox.setDisable(false);
            stateComboBox.setDisable(false);
        } else {
            closeCustToolDrawer();
        }
    }

    void tryActivateConfirmButton() {
        custConfirmBtn.setDisable(custNameField.getText().equals("") || custPhoneField.getText().equals("") ||
                custAddressField.getText().equals("") || custPostalCodeField.getText().equals("") ||
                countryComboBox.getSelectionModel().isEmpty() || stateComboBox.getSelectionModel().isEmpty());
    }

    @FXML
    void onActionDeleteCust(ActionEvent event) {
        if (deleteCustomerBtn.isSelected()) {
            confirmBtnImg.setImage(deleteImg);
            custNameField.setEditable(false);
            custPhoneField.setEditable(false);
            custAddressField.setEditable(false);
            custPostalCodeField.setEditable(false);
            countryComboBox.setDisable(true);
            stateComboBox.setDisable(true);
            custConfirmBtn.setDisable(false);
            openCustToolDrawer(custTableView.getSelectionModel().getSelectedItem());
        } else {
            closeCustToolDrawer();
        }
    }

    @FXML
    void onActionEditCust(ActionEvent event) {
        if (editCustomerBtn.isSelected()) {
            confirmBtnImg.setImage(editImg);
            custNameField.setEditable(true);
            custPhoneField.setEditable(true);
            custAddressField.setEditable(true);
            custPostalCodeField.setEditable(true);
            countryComboBox.setDisable(false);
            stateComboBox.setDisable(false);
            custConfirmBtn.setDisable(true);
            openCustToolDrawer(custTableView.getSelectionModel().getSelectedItem());
        } else {
            closeCustToolDrawer();
        }
    }

    @FXML
    void onActionCustRefresh(ActionEvent event) {
        populateCustomerTable();
    }

    @FXML
    void onActionAppRefresh(ActionEvent event) {

    }

    @FXML
    void onActionAddCustApp(ActionEvent event) {

    }

    @FXML
    void onActionCountryComboBox(ActionEvent event) {
        stateComboBox.getItems().clear();
        if (countryComboBox.getSelectionModel().getSelectedItem() != null) {
            populateStateBox(countryComboBox.getSelectionModel().getSelectedItem());
        }
        tryActivateConfirmButton();
    }

    @FXML
    void onActionStateComboBox(ActionEvent event) {
        tryActivateConfirmButton();
    }

    @FXML
    void onKeyTypedCustNameField(KeyEvent event)  {
        tryActivateConfirmButton();
    }

    @FXML
    void onKeyTypedCustAddressField(KeyEvent event) {
        tryActivateConfirmButton();
    }


    @FXML
    void onKeyTypedPhoneField(KeyEvent event) {
        tryActivateConfirmButton();
    }

    @FXML
    void onKeyTypedPostalCodeField(KeyEvent event) {
        tryActivateConfirmButton();
    }

    @FXML
    private ImageView confirmBtnImg;
    private final Image addImg = new Image(getClass().getResourceAsStream("../resources/add_icon.png"));
    private final Image deleteImg = new Image(getClass().getResourceAsStream("../resources/blue_remove_icon.png"));
    private final Image editImg = new Image(getClass().getResourceAsStream("../resources/edit_icon.png"));
    private final Image errorImg = new Image(getClass().getResourceAsStream("../resources/remove_icon.png"));

    @FXML
    private Button addAppBtn;

    @FXML
    private Button deleteAppBtn;

    @FXML
    private Button editAppBtn;

    @FXML
    private Button upcomingAppBtn;

    @FXML
    private Button appRefreshBtn;

    @FXML
    private TitledPane appToolDrawer;

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
    private ToggleButton deleteCustomerBtn;

    @FXML
    private ToggleButton editCustomerBtn;

    @FXML
    private ToggleButton addCustAppBtn;

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

}
