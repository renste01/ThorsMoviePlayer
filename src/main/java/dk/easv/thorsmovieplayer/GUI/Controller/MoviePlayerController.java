package dk.easv.thorsmovieplayer.GUI.Controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MoviePlayerController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    private void clearFilters(ActionEvent actionEvent) {
    }

    @FXML
    private void playMovie(ActionEvent actionEvent) {

    }
}
