package igirepay.igire_capstoneproject.lab3;

import igirepay.igire_capstoneproject.lab3.util.NavigationManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApplication extends Application {

    private static final int WIDTH = 440;
    private static final int HEIGHT = 720;

    @Override
    public void start(Stage stage) {
        stage.setTitle("IgirePay - Mobile Banking");
        stage.setMinWidth(WIDTH);
        stage.setMinHeight(HEIGHT);

        NavigationManager navigation = new NavigationManager(stage);
        stage.setScene(navigation.load(NavigationManager.SPLASH_FXML));
        stage.setWidth(WIDTH);
        stage.setHeight(HEIGHT);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
