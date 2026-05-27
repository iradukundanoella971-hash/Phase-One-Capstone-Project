package igirepay.igire_capstoneproject.lab3.controller;

import igirepay.igire_capstoneproject.lab3.util.NavigationManager;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressIndicator;
import javafx.util.Duration;

public class SplashController {

    @FXML
    private ProgressIndicator loadingIndicator;

    private NavigationManager navigationManager;

    public void init(NavigationManager navigationManager) {
        this.navigationManager = navigationManager;
        if (loadingIndicator != null) {
            loadingIndicator.setProgress(-1);
        }

        PauseTransition pause = new PauseTransition(Duration.seconds(2.2));
        pause.setOnFinished(e -> navigationManager.goToLogin());
        pause.play();
    }
}
