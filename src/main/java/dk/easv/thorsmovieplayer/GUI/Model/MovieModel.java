package dk.easv.thorsmovieplayer.GUI.Model;
// Project imports
import dk.easv.thorsmovieplayer.BE.Category;
import dk.easv.thorsmovieplayer.BE.Movie;
import dk.easv.thorsmovieplayer.BLL.CategoryManager;
import dk.easv.thorsmovieplayer.BLL.MovieManager;
// Java Imports
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class MovieModel {
    private ObservableList<Movie> movies;
    private MovieManager movieManager;
    // List to store all available categories
    private ObservableList<Category> categories;
    // Manager to handle category database operations
    private CategoryManager categoryManager;

    // Constructor - sets up the model with movies and categories
    public MovieModel() throws IOException, SQLException
    {
        movieManager = new MovieManager();
        movies = FXCollections.observableArrayList();
        movies.addAll(movieManager.getAllMovies());

        // Initialize category components
        categoryManager = new CategoryManager();
        categories = FXCollections.observableArrayList();
        refreshCategories(); // Load categories from database
    }
    public void createMovie(Movie movie) throws SQLException
    {
        movieManager.createMovie(movie);
        movies.add(movie);
    }
    public void deleteMovie(Movie movie) throws SQLException
    {
        movieManager.deleteMovie(movie);
        movies.remove(movie);
    }
    // Returns the list of all movies
    public ObservableList<Movie> getMovies() {
        return movies;
    }

    // Returns the list of all categories
    public ObservableList<Category> getCategories() {
        return categories;
    }

    // Reloads categories from the database
    public void refreshCategories() throws SQLException
    {
        categories.clear();
        categories.addAll(categoryManager.getAllCategories());

        //this adds a virtual "All movies" category at the top (id = 0 to mark it as virtual)
        Category allMovies = new Category(0, "All movies");
        categories.add(0, allMovies); //this puts  it as the first item
    }

    // Creates a new category and adds it to the list
    public void addCategory(String name) throws SQLException
    {
        Category category = categoryManager.createCategory(name);
        categories.add(category);
    }

    public void updatePersonalRating(Movie movie, float newRating) throws SQLException
    {
        movieManager.updatePersonalRating(movie, newRating);
        // No need to refresh the list since we're updating the existing object
    }

    // Deletes a category from database and removes it from the list
    public void deleteCategory(Category category) throws SQLException
    {
        categoryManager.deleteCategory(category);
        categories.remove(category);
    }

    // Links a category to a movie in the database
    public void addCategoryToMovie(Movie movie, Category category) throws SQLException
    {
        categoryManager.addCategoryToMovie(movie, category);
    }

    // Removes a category from a movie in the database
    public void removeCategoryFromMovie(Movie movie, Category category) throws SQLException
    {
        categoryManager.removeCategoryFromMovie(movie, category);
    }

    // Filters movies to show only those with ALL selected categories
    public ObservableList<Movie> filterMoviesByCategories(List<Category> selectedCategories) throws SQLException
    {
        // If no categories selected, return all movies
        if (selectedCategories == null || selectedCategories.isEmpty()) {
            return movies;
        }

        ObservableList<Movie> filteredMovies = FXCollections.observableArrayList();

        // Check each movie to see if it has all selected categories
        for (Movie movie : movies) {
            boolean hasAllCategories = true;

            // For each selected category, check if movie has it
            for (Category category : selectedCategories) {
                List<Category> movieCategories = categoryManager.getCategoriesForMovie(movie);
                if (!movieCategories.contains(category)) {
                    hasAllCategories = false;
                    break;
                }
            }
            // If movie has all categories, add it to filtered list
            if (hasAllCategories) {
                filteredMovies.add(movie);
            }
        }
        return filteredMovies;
    }

    //
    public void openMovieInSystemPlayer(Movie movie) throws IOException {
        if (movie == null || movie.getFilePath() == null) {
            throw new IllegalArgumentException("Movie or file path is not found!");
        }

        // Brug projektets data-mappe som base
        String basePath = "data/";

        File movieFile = new File(basePath + movie.getFilePath());

        if (!movieFile.exists()) {
            throw new IOException("Movie file does not exist: " + movieFile.getAbsolutePath());
        }

        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(movieFile);
        } else {
            throw new IOException("Desktop operations not supported on this system!");
        }
    }

}