package dk.easv.thorsmovieplayer.GUI.Model;

import dk.easv.thorsmovieplayer.BE.Movie;
import dk.easv.thorsmovieplayer.BLL.MovieManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.sql.SQLException;

public class MovieModel {
    private ObservableList<Movie> movies;
    private MovieManager movieManager;

    public MovieModel() throws IOException, SQLException {
        movieManager = new MovieManager();
        movies = FXCollections.observableArrayList();
        movies.addAll(movieManager.getAllMovies());
    }

    public ObservableList<Movie> getMovies() {
        return movies;
    }
}
