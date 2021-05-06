package com.iceybones.scheduler.controllers;

import com.iceybones.scheduler.models.Country;
import com.iceybones.scheduler.models.Customer;
import com.iceybones.scheduler.models.Division;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;

public class CustTabController implements Initializable {
  private MainController mainController;
  private ResourceBundle resourceBundle;

  @Override
  public void initialize(URL url, ResourceBundle rb) {
    resourceBundle = rb;
    custTableView.getColumns().get(0).setText(rb.getString("ID"));
    custTableView.getColumns().get(1).setText(rb.getString("Name"));
    custTableView.getColumns().get(2).setText(rb.getString("Address"));
    custTableView.getColumns().get(3).setText(rb.getString("Postal Code"));
    custTableView.getColumns().get(4).setText(rb.getString("Division"));
    custTableView.getColumns().get(5).setText(rb.getString("Country"));
    custTableView.getColumns().get(6).setText(rb.getString("Phone Number"));
    custIdField.setPromptText(rb.getString("Auto-Generated"));
    custIdLbl.setText(rb.getString("ID") + ":");
    custNameLbl.setText(rb.getString("Name") + ":");
    custAddressLbl.setText(rb.getString("Address") + ":");
    custPostalCodeLbl.setText(rb.getString("Postal Code") + ":");
    custPhoneLbl.setText(rb.getString("Phone") + ":");
    addCustomerBtn.getTooltip().setText(rb.getString("Add New Customer"));
    editCustomerBtn.getTooltip().setText(rb.getString("Edit Selected Customer"));
    deleteCustomerBtn.getTooltip().setText(rb.getString("Remove Selected Customer"));
    addCustAppBtn.getTooltip().setText(rb.getString("Schedule Appointment with Selected Customer"));
    custRefreshBtn.getTooltip().setText(rb.getString("Refresh"));
    custConfirmBtn.getTooltip().setText(rb.getString("Confirm Submission"));

    setupTable();
    custTableView.getSelectionModel().selectedItemProperty()
        .addListener((obs, oldSelection, newSelection) -> {
          if (newSelection != null) {
            deleteCustomerBtn.setDisable(false);
            editCustomerBtn.setDisable(false);
            addCustAppBtn.setDisable(false);
            if (custToolDrawer.isExpanded()) {
              if (editCustomerBtn.isSelected()) {
                custConfirmBtn.setDisable(true);
              }
              openToolDrawer(custTableView.getSelectionModel().getSelectedItem());
            }
            if (addCustomerBtn.isSelected()) {
              addCustomerBtn.setSelected(false);
              setCollapseToolDrawer(true);
            }
          }
        });
    String countryTxt = resourceBundle.getString("Country");
    countryComboBox.setPromptText(countryTxt);
    countryComboBox.setButtonCell(new ListCell<>() {
      @Override
      protected void updateItem(Country item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(countryTxt);
        } else {
          setText(item.getCountry());
        }
      }
    });
    String stateTxt = resourceBundle.getString("State");
    stateComboBox.setPromptText(stateTxt);
    stateComboBox.setButtonCell(new ListCell<>() {
      @Override
      protected void updateItem(Division item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(stateTxt);
        } else {
          setText(item.getDivision());
        }
      }
    });
  }

  void setMainController(MainController mainController) {
    this.mainController = mainController;
  }

  void populate() {
    populateTable();
    populateCountryBox();
  }

  private void setupTable() {
    custIdCol.setCellValueFactory(new PropertyValueFactory<>("customerId"));
    custNameCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
    custAddressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
    custPostalCodeCol.setCellValueFactory(new PropertyValueFactory<>("postalCode"));
    custDivisionCol.setCellValueFactory(new PropertyValueFactory<>("division"));
    custCountryCol.setCellValueFactory(new PropertyValueFactory<>("country"));
    custPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
  }

  void populateTable() {
    mainController.getTableProgress().setVisible(true);
    MainController.getDbService().submit(() -> {
      try {
        List<Customer> customers = Database.getCustomers();
        custTableView.getItems().clear();
        custTableView.getItems().addAll(customers);
      } catch (SQLException e) {
        Platform.runLater(
            () -> mainController.notify(resourceBundle.getString("Failed to populate customer table. Check connection."),
                MainController.NotificationType.ERROR, false));
      } finally {
        Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
      }
    });
  }

  private void populateCountryBox() {
    MainController.getDbService().submit(() -> {
      try {
        List<Country> countries = Database.getCountries();
        Platform.runLater(() -> countryComboBox.getItems().addAll(countries));
      } catch (SQLException e) {
        Platform.runLater(
            () -> mainController.notify(resourceBundle.getString("Failed to populate country box. Check connection."),
                MainController.NotificationType.ERROR, false));
      }
    });
  }

  private void populateStateBox(Country country) {
    stateComboBox.getItems().clear();
    MainController.getDbService().submit(() -> {
      try {
        List<Division> division = Database.getDivisionsByCountry(country);
        Platform.runLater(() -> stateComboBox.getItems().addAll(division));
      } catch (SQLException e) {
        Platform
            .runLater(() -> mainController.notify(resourceBundle.getString("Failed to populate state box. Check connection."),
                MainController.NotificationType.ERROR, false));
      }
    });
  }

  void setCollapseToolDrawer(boolean b) {
    if (b) {
      custToolDrawer.setAnimated(true);
    }
    custToolDrawer.setCollapsible(true);
    custToolDrawer.setExpanded(!b);
    custToolDrawer.setCollapsible(false);
    custToolDrawer.setAnimated(false);
  }

  private void clearToolDrawer() {
    custNameField.setText(null);
    custIdField.setText(null);
    custPhoneField.setText(null);
    custAddressField.setText(null);
    custPostalCodeField.setText(null);
    setValHelper(countryComboBox, null);
    setValHelper(stateComboBox, null);
    stateComboBox.setDisable(true);
  }

  void resetToolButtons() {
    editCustomerBtn.setDisable(true);
    deleteCustomerBtn.setDisable(true);
    addCustAppBtn.setDisable(true);
    addCustomerBtn.setSelected(false);
    editCustomerBtn.setSelected(false);
    deleteCustomerBtn.setSelected(false);
  }

  private void setToolDrawerEditable(boolean isEdit) {
    custNameField.setDisable(!isEdit);
    custPhoneField.setDisable(!isEdit);
    custAddressField.setDisable(!isEdit);
    custPostalCodeField.setDisable(!isEdit);
    countryComboBox.setDisable(!isEdit);
    stateComboBox.setDisable(!isEdit);
  }

  private void openToolDrawer(Customer cust) {
    setCollapseToolDrawer(false);
    if (cust == null) {
      clearToolDrawer();
    } else {
      custIdField.setText(Integer.toString(cust.getCustomerId()));
      custNameField.setText(cust.getCustomerName());
      custPhoneField.setText(cust.getPhone());
      custAddressField.setText(cust.getAddress());
      custPostalCodeField.setText(cust.getPostalCode());
      setValHelper(countryComboBox, cust.getDivision().getCountry());
      populateStateBox(cust.getDivision().getCountry());
      setValHelper(stateComboBox, cust.getDivision());
    }
    if (!editCustomerBtn.isSelected()) {
      tryActivateConfirmBtn();
    }
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private void setValHelper(ComboBoxBase box, Object val) {
    var handler = box.getOnAction();
    box.setOnAction(null);
    box.setValue(val);
    box.setOnAction(handler);
  }

  private void confirmAdd(Customer customer) {
    mainController.getTableProgress().setVisible(true);
    MainController.getDbService().submit(() -> {
      try {
        int custId = Database.insertCustomer(customer);
        customer.setCustomerId(custId);
        Platform.runLater(() -> {
          setCollapseToolDrawer(true);
          resetToolButtons();
          populateTable();
          mainController.notify(resourceBundle.getString("Customer Added") + ": " + customer, MainController.NotificationType.ADD, true);
        });
      } catch (SQLException e) {
        Platform.runLater(
            () -> mainController.notify(resourceBundle.getString("Failed to add customer. Check connection and input."),
                MainController.NotificationType.ERROR, false));
      } finally {
        Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
      }
    });
  }

  private void confirmDelete(Customer customer) {
    mainController.getTableProgress().setVisible(true);
    MainController.getDbService().submit(() -> {
      try {
        Database.deleteCustomer(customer);
        Platform.runLater(() -> {
          clearToolDrawer();
          setCollapseToolDrawer(true);
          resetToolButtons();
          populateTable();
          mainController.getAppTabController().populateTable();
          mainController.notify(resourceBundle.getString("Customer Removed") + ": " + customer,
              MainController.NotificationType.DELETE, true);
        });
      } catch (SQLException e) {
        e.printStackTrace();
        Platform
            .runLater(() -> mainController.notify(resourceBundle.getString("Failed to delete customer. Check connection."),
                MainController.NotificationType.ERROR, false));
      } finally {
        Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
      }
    });
  }

  private void confirmUpdate(Customer newCust, Customer original) {
    mainController.getTableProgress().setVisible(true);
    MainController.getDbService().submit(() -> {
      try {
        Database.updateCustomer(newCust);
        Platform.runLater(() -> {
          clearToolDrawer();
          setCollapseToolDrawer(true);
          resetToolButtons();
          custTableView.getItems().set(custTableView.getItems().indexOf(original), newCust);
          mainController.notify(resourceBundle.getString("Customer Updated") + ": " + newCust,
              MainController.NotificationType.EDIT, true);
        });
      } catch (SQLException e) {
        Platform
            .runLater(() -> mainController.notify(resourceBundle.getString("Failed to update customer. Check connection."),
                MainController.NotificationType.ERROR, false));
      } finally {
        Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
      }
    });
  }

  private void tryActivateConfirmBtn() {
    custConfirmBtn
        .setDisable(custNameField.getText() == null || custPhoneField.getText() == null ||
            custAddressField.getText() == null || custPostalCodeField.getText() == null ||
            countryComboBox.getValue() == null || stateComboBox.getValue()== null);
  }

  @FXML
  private void onActionConfirm() {
    Customer customer = new Customer();
    customer.setCustomerName(custNameField.getText());
    customer.setCustomerId((custIdField.getText() == null ? 0
        : Integer.parseInt(custIdField.getText())));
    customer.setPhone(custPhoneField.getText());
    customer.setAddress(custAddressField.getText());
    customer.setPostalCode(custPostalCodeField.getText());
    customer.setCreatedBy(Database.getConnectedUser());
    customer.setLastUpdatedBy(Database.getConnectedUser());
    customer.setDivision(stateComboBox.getValue());
    if (addCustomerBtn.isSelected()) {
      confirmAdd(customer);
    } else if (deleteCustomerBtn.isSelected()) {
      confirmDelete(custTableView.getSelectionModel().getSelectedItem());
    } else if (editCustomerBtn.isSelected()) {
      confirmUpdate(customer, custTableView.getSelectionModel().getSelectedItem());
    }
  }

  @FXML
  private void onActionAddCust() {
    custTableView.getSelectionModel().clearSelection();
    editCustomerBtn.setDisable(true);
    deleteCustomerBtn.setDisable(true);
    addCustAppBtn.setDisable(true);
    if (addCustomerBtn.isSelected()) {
      setToolDrawerEditable(true);
      custConfirmBtnImg.setImage(MainController.getAddImg());
      openToolDrawer(null);
    } else {
      setCollapseToolDrawer(true);
    }
  }

  @FXML
  private void onActionDeleteCust() {
    if (deleteCustomerBtn.isSelected()) {
      setToolDrawerEditable(false);
      custConfirmBtnImg.setImage(MainController.getDeleteImg());
      openToolDrawer(custTableView.getSelectionModel().getSelectedItem());
    } else {
      setCollapseToolDrawer(true);
    }
  }

  @FXML
  private void onActionEditCust() {
    if (editCustomerBtn.isSelected()) {
      setToolDrawerEditable(true);
      custConfirmBtnImg.setImage(MainController.getEditImg());
      custConfirmBtn.setDisable(true);
      openToolDrawer(custTableView.getSelectionModel().getSelectedItem());
    } else {
      setCollapseToolDrawer(true);
    }
  }

  @FXML
  private void onActionAddCustApp() {
    setCollapseToolDrawer(true);
    resetToolButtons();
    mainController.getTabPane().getSelectionModel().select(0);
    mainController.getAppTabController().pushAppointment(custTableView.getSelectionModel()
        .getSelectedItem());
    custTableView.getSelectionModel().clearSelection();
  }

  @FXML
  private void onActionCountryComboBox() {
    setValHelper(stateComboBox, null);
    stateComboBox.setDisable(false);
    if (countryComboBox.getSelectionModel().getSelectedItem() != null) {
      populateStateBox(countryComboBox.getSelectionModel().getSelectedItem());
    }
    tryActivateConfirmBtn();
  }

  @FXML
  private void onActionStateComboBox() {
    tryActivateConfirmBtn();
  }

  @FXML
  private void onActionRefresh() {
    mainController.getTableProgress().setVisible(true);
    custTableView.getSelectionModel().clearSelection();
    setCollapseToolDrawer(true);
    resetToolButtons();
    MainController.getDbService().submit(() -> {
      try {
        Database.commit();
        Platform.runLater(() -> mainController.notify(resourceBundle.getString("Changes Committed"),
            MainController.NotificationType.SUCCESS, false)
        );
      } catch (SQLException e) {
        Platform
            .runLater(() -> mainController.notify(resourceBundle.getString("Failed to refresh database. Check connection."),
                MainController.NotificationType.ERROR, false));
      } finally {
        Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
      }
    });
    mainController.refresh();
  }

  @FXML
  private void onKeyTypedCustField() {
    tryActivateConfirmBtn();
  }

  @FXML
  private ImageView custConfirmBtnImg;


  @FXML
  private ToggleButton addCustomerBtn;

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
  private Label custIdLbl;

  @FXML
  private Label custNameLbl;

  @FXML
  private Label custPhoneLbl;

  @FXML
  private Label custPostalCodeLbl;

  @FXML
  private Label custAddressLbl;

}
