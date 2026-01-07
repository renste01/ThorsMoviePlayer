package dk.easv.thorsmovieplayer.GUI;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MoviePlayerController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
