package igirepay.igire_capstoneproject.lab3.util;

import igirepay.igire_capstoneproject.lab3.controller.AppController;
import igirepay.igire_capstoneproject.lab3.controller.DashboardController;
import igirepay.igire_capstoneproject.lab3.controller.LoginController;
import igirepay.igire_capstoneproject.lab3.controller.ProfileController;
import igirepay.igire_capstoneproject.lab3.controller.RegisterController;
import igirepay.igire_capstoneproject.lab3.controller.SplashController;
import igirepay.igire_capstoneproject.lab3.controller.TransactionController;
import igirepay.igire_capstoneproject.lab3.session.UserSession;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Set;

public class NavigationManager {

    public static final String SPLASH_FXML = "/igirepay/igire_capstoneproject/lab3/view/splash.fxml";
    public static final String LOGIN_FXML = "/igirepay/igire_capstoneproject/lab3/view/login.fxml";
    public static final String REGISTER_FXML = "/igirepay/igire_capstoneproject/lab3/view/register.fxml";
    public static final String DASHBOARD_FXML = "/igirepay/igire_capstoneproject/lab3/view/dashboard.fxml";
    public static final String TRANSACTION_FXML = "/igirepay/igire_capstoneproject/lab3/view/transaction.fxml";
    public static final String PROFILE_FXML = "/igirepay/igire_capstoneproject/lab3/view/profile.fxml";

    public static final String MODE_DEPOSIT = "DEPOSIT";
    public static final String MODE_WITHDRAW = "WITHDRAW";
    public static final String MODE_TRANSFER = "TRANSFER";
    public static final String MODE_HISTORY = "HISTORY";

    private static final String STYLESHEET = "/igirepay/igire_capstoneproject/lab3/css/style.css";
    private static final Set<String> PROTECTED = Set.of(DASHBOARD_FXML, TRANSACTION_FXML, PROFILE_FXML);

    private final Stage stage;
    private final UserSession session;
    private final AppController appController;
    private String transactionMode = MODE_DEPOSIT;

    public NavigationManager(Stage stage) {
        this.stage = stage;
        this.session = new UserSession();
        this.appController = new AppController();
    }

    public Scene load(String fxmlPath) {
        try {
            URL resource = Objects.requireNonNull(getClass().getResource(fxmlPath), "FXML not found: " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            wireController(loader.getController());
            Scene scene = new Scene(root);
            URL css = getClass().getResource(STYLESHEET);
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            return scene;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML: " + fxmlPath, e);
        }
    }

    private void wireController(Object controller) {
        if (controller instanceof SplashController c) {
            c.init(this);
        } else if (controller instanceof LoginController c) {
            c.init(this, session, appController);
        } else if (controller instanceof RegisterController c) {
            c.init(this, session, appController);
        } else if (controller instanceof DashboardController c) {
            c.init(this, session, appController);
        } else if (controller instanceof TransactionController c) {
            c.init(this, session, appController, transactionMode);
        } else if (controller instanceof ProfileController c) {
            c.init(this, session, appController);
        }
    }

    public void goTo(String fxmlPath) {
        if (PROTECTED.contains(fxmlPath) && !session.isLoggedIn()) {
            fxmlPath = LOGIN_FXML;
        }
        stage.setScene(load(fxmlPath));
        stage.show();
    }

    public void goToLogin() {
        goTo(LOGIN_FXML);
    }

    public void goToDashboard() {
        goTo(DASHBOARD_FXML);
    }

    public void goToTransaction(String mode) {
        transactionMode = mode == null ? MODE_DEPOSIT : mode;
        goTo(TRANSACTION_FXML);
    }

    public void goToProfile() {
        goTo(PROFILE_FXML);
    }
}
