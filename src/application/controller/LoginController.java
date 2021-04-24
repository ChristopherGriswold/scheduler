package application.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ResourceBundle;
import java.util.concurrent.*;

public class LoginController implements Initializable {
    private static Long userAuth;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println(ZoneId.systemDefault());
    }

    @FXML
    void onActionLogin(ActionEvent event) {
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
                    Platform.runLater(() -> errorLbl.setText("Login SUCCESS"));
                } else {
                    Platform.runLater(() -> errorLbl.setText("Login FAILED"));
                }
                Platform.runLater(() -> loginBtn.setDisable(false));
                Platform.runLater(() -> loginBtn.setVisible(true));
                Platform.runLater(() -> errorLbl.setVisible(true));
                Platform.runLater(() -> progressBar.setVisible(false));
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        };
        service.submit(waitForAuth);
        service.shutdown();
    }

    @FXML
    void onActionPassword(ActionEvent event) {

    }

    @FXML
    void onActionUsername(ActionEvent event) {

    }
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
