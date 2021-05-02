package com.iceybones.scheduler.application.controller;

import com.iceybones.scheduler.application.model.Country;
import com.iceybones.scheduler.application.model.Customer;
import com.iceybones.scheduler.application.model.Division;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;

public class CustTabController implements Initializable {

  private MainController mainController;

  public void setMainController(MainController mainController) {
    this.mainController = mainController;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    setupTable();
    custTableView.getSelectionModel().selectedItemProperty()
        .addListener((obs, oldSelection, newSelection) -> {
          if (newSelection != null) {
            deleteCustomerBtn.setDisable(false);
            editCustomerBtn.setDisable(false);
            addCustAppBtn.setDisable(false);
            if (oldSelection != null && custToolDrawer.isExpanded()) {
              openToolDrawer(custTableView.getSelectionModel().getSelectedItem());
            }
            if (addCustomerBtn.isSelected()) {
              addCustomerBtn.setSelected(false);
              setCollapseToolDrawer(true);
            }
          }
        });
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
  }

  public void populate() {
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

  public void populateTable() {
    mainController.getTableProgress().setVisible(true);
    MainController.getDbService().submit(() -> {
      try {
        List<Customer> customers = Database.getCustomers();
        custTableView.getItems().clear();
        custTableView.getItems().addAll(customers);
      } catch (SQLException e) {
        Platform.runLater(
            () -> mainController.notify("Failed to populate customer table. Check connection.",
                MainController.NotificationType.ERROR, false));
      } finally {
        Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
      }
    });
  }

  void setCollapseToolDrawer(boolean b) {
    custToolDrawer.setCollapsible(true);
    custToolDrawer.setExpanded(!b);
    custToolDrawer.setCollapsible(false);
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

  void setToolDrawerEditable(boolean isEdit) {
    custNameField.setDisable(!isEdit);
    custPhoneField.setDisable(!isEdit);
    custAddressField.setDisable(!isEdit);
    custPostalCodeField.setDisable(!isEdit);
    countryComboBox.setDisable(!isEdit);
    stateComboBox.setDisable(!isEdit);
  }

  void populateCountryBox() {
    MainController.getDbService().submit(() -> {
      try {
        List<Country> countries = Database.getCountries();
        Platform.runLater(() -> countryComboBox.getItems().addAll(countries));
      } catch (SQLException e) {
        Platform.runLater(
            () -> mainController.notify("Failed to populate country box. Check connection.",
                MainController.NotificationType.ERROR, false));
      }
    });
  }

  void populateStateBox(Country country) {
    stateComboBox.getItems().clear();
    MainController.getDbService().submit(() -> {
      try {
        List<Division> division = Database.getDivisionsByCountry(country);
        Platform.runLater(() -> stateComboBox.getItems().addAll(division));
      } catch (SQLException e) {
        Platform
            .runLater(() -> mainController.notify("Failed to populate state box. Check connection.",
                MainController.NotificationType.ERROR, false));
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
  }

  void openToolDrawer(Customer cust) {
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
    tryActivateConfirmBtn();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Deprecated
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
        Platform.runLater(() -> {
          setCollapseToolDrawer(true);
          resetToolButtons();
          populateTable();
          mainController.notify(customer.getCustomerName() +
              " has been added to the database.", MainController.NotificationType.ADD, true);
        });
      } catch (SQLException e) {
        Platform.runLater(
            () -> mainController.notify("Failed to add customer. Check connection and input.",
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
          mainController.notify(customer.getCustomerName() + " has been removed from the database.",
              MainController.NotificationType.DELETE, true);
        });
      } catch (SQLException e) {
        e.printStackTrace();
        Platform
            .runLater(() -> mainController.notify("Failed to delete customer. Check connection.",
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
          mainController.notify("Customer " + original.getCustomerId() + " has been updated.",
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

  void tryActivateConfirmBtn() {
    custConfirmBtn
        .setDisable(custNameField.getText() == null || custPhoneField.getText() == null ||
            custAddressField.getText() == null || custPostalCodeField.getText() == null ||
            countryComboBox.getValue() == null || stateComboBox.getValue()== null);
  }

  @FXML
  void onActionConfirm(ActionEvent event) {
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
  void onActionAddCust(ActionEvent event) {
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
  void onActionDeleteCust(ActionEvent event) {
    if (deleteCustomerBtn.isSelected()) {
      setToolDrawerEditable(false);
      custConfirmBtnImg.setImage(MainController.getDeleteImg());
      openToolDrawer(custTableView.getSelectionModel().getSelectedItem());
    } else {
      setCollapseToolDrawer(true);
    }
  }

  @FXML
  void onActionEditCust(ActionEvent event) {
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
  void onActionAddCustApp(ActionEvent event) {
    setCollapseToolDrawer(true);
    resetToolButtons();
    mainController.getTabPane().getSelectionModel().select(0);
    mainController.getAppTabController().addAppointment(custTableView.getSelectionModel()
        .getSelectedItem());
    custTableView.getSelectionModel().clearSelection();
  }

  @FXML
  void onActionCountryComboBox(ActionEvent event) {
    stateComboBox.getItems().clear();
    stateComboBox.setDisable(false);
    if (countryComboBox.getSelectionModel().getSelectedItem() != null) {
      populateStateBox(countryComboBox.getSelectionModel().getSelectedItem());
    }
    tryActivateConfirmBtn();

  }

  @FXML
  void onActionStateComboBox(ActionEvent event) {
    tryActivateConfirmBtn();
  }

  @FXML
  void onKeyTypedCustField(KeyEvent event) {
    tryActivateConfirmBtn();
  }

  @FXML
  void onActionRefresh(ActionEvent event) {
    mainController.getTableProgress().setVisible(true);
    custTableView.getSelectionModel().clearSelection();
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
                MainController.NotificationType.ERROR, false));
      } finally {
        Platform.runLater(() -> mainController.getTableProgress().setVisible(false));
      }
    });
    mainController.refresh();
  }

  @FXML
  private ImageView custConfirmBtnImg;

  @FXML
  private Tab custTab;

  @FXML
  private ToggleButton addCustomerBtn;

  @FXML
  private ToggleGroup custToggleGroup;

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

}
