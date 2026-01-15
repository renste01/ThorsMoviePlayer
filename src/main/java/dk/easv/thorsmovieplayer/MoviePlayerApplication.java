package dk.easv.thorsmovieplayer;
// Java Imports
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MoviePlayerApplication extends Application
{
    @Override
    public void start(Stage stage) throws IOException
    {
        FXMLLoader fxmlLoader = new FXMLLoader(MoviePlayerApplication.class.getResource("MoviePlayerView.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Movie Player");
        stage.setScene(scene);
        stage.show();
    }

}
