package application.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.*;

public class LoginController implements Initializable {
    private static Long userAuth;
    private static ResourceBundle rb;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
//        rb = ResourceBundle.getBundle("application.resources.strings", new Locale("fr"));
        rb = ResourceBundle.getBundle("application.resources.strings", Locale.getDefault());
        loginBtn.setText(rb.getString("Login"));
        usernameTxt.setPromptText(rb.getString("Username"));
        passwordTxt.setPromptText(rb.getString("Password"));
        zoneIdTxt.setText(ZoneId.systemDefault().toString());
        Platform.runLater(() -> borderPane.requestFocus());
    }

    @FXML
    void onActionLogin(ActionEvent event) {
        borderPane.requestFocus();
        loginBtn.setDisable(true);
        loginBtn.setVisible(false);
        errorLbl.setVisible(false);
        progressBar.setVisible(true);
        var service = Executors.newFixedThreadPool(2);
        Callable<Long> getAuthToken = () -> Database.authenticate(usernameTxt.getText(), passwordTxt.getText());
        Future<Long> authToken = service.submit(getAuthToken);
        Runnable waitForAuth = () -> {
            while (!authToken.isDone()) {}
            try {
                if ((userAuth = authToken.get()) != null) {
                    Platform.runLater(() -> errorLbl.setText(rb.getString("Login Success")));
                } else {
                    Platform.runLater(() -> errorLbl.setText(rb.getString("Login Failed")));
                }
                Platform.runLater(() -> {
                    loginBtn.setDisable(false);
                    loginBtn.setVisible(true);
                    errorLbl.setVisible(true);
                    progressBar.setVisible(false);
                });
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        };
        service.submit(waitForAuth);
        service.shutdown();
    }

    @FXML
    void onActionPassword(ActionEvent event) {
        borderPane.requestFocus();
        loginBtn.fire();
    }

    @FXML
    void onActionUsername(ActionEvent event) {
        borderPane.requestFocus();
        loginBtn.fire();
    }

    @FXML
    private BorderPane borderPane;

    @FXML
    private TextField usernameTxt;

    @FXML
    private PasswordField passwordTxt;

    @FXML
    private Button loginBtn;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label errorLbl;

    @FXML
    private Label zoneIdTxt;

}
