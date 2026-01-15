package dk.easv.thorsmovieplayer.GUI.Controller;

import dk.easv.thorsmovieplayer.BE.Category;
import dk.easv.thorsmovieplayer.BE.Movie;
import dk.easv.thorsmovieplayer.GUI.Model.MovieModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.ListChangeListener;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MoviePlayerController implements Initializable {
    @FXML
    private Label welcomeText;
    @FXML
    private TextField titleFilterField;
    @FXML
    private ListView<Category> genreListView;
    @FXML
    private Slider ratingSlider;
    @FXML
    private TableView<Movie> movieTable;
    @FXML
    private TableColumn<Movie, String> titleColumn;
    @FXML
    private TableColumn<Movie, String> categoryColumn;
    @FXML
    private TableColumn<Movie, Float> imdbColumn;
    @FXML
    private TableColumn<Movie, Float> ratingColumn;
    @FXML
    private Label detailTitle;
    @FXML
    private Label detailCategories;
    @FXML
    private Label detailImdb;
    @FXML
    private Slider personalRatingSlider;
    @FXML
    private Label currentRatingLabel;
    private MovieModel movieModel;
    private ObservableList<Movie> allMovies;
    private FilteredList<Movie> filteredMovies;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            movieModel = new MovieModel();
            allMovies = FXCollections.observableArrayList(movieModel.getMovies());
            filteredMovies = new FilteredList<>(allMovies, m -> true);
            movieTable.setItems(filteredMovies);

            setupTableView();
            setupFilters();
            loadData();
            checkMoviesToDelete();

            // Update rating label as slider moves
            personalRatingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                currentRatingLabel.setText(String.format("%.1f", newVal.doubleValue()));
            });

        } catch (IOException | SQLException e) {
            showError("Error initializing application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Checks movies that should be considered for deletion
    public List<Movie> checkMoviesToDelete() {
        try {
            List<Movie> moviesToDelete = movieModel.getMovies().stream()
                    .filter(movie -> movie.getPersonalRating() != 0f && movie.getPersonalRating() < 6)
                    .filter(movie -> movie.getLastView() != null && movie.getLastView().isBefore(java.time.LocalDate.now().minusYears(2)))
                    .collect(Collectors.toList());

            if (!moviesToDelete.isEmpty()) {
                String titles = moviesToDelete.stream()
                        .map(Movie::getTitle)
                        .collect(Collectors.joining("\n"));

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Reminder");
                alert.setHeaderText("You should consider deleting these movies");
                alert.setContentText(titles);
                alert.showAndWait();
            }
            return moviesToDelete;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Sets up the table view columns and cell factories
    private void setupTableView() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        // Combine categories into a comma-separated string
        categoryColumn.setCellValueFactory(cellData -> {
            String text = cellData.getValue().getCategories().stream()
                    .map(Category::getName)
                    .collect(Collectors.joining(", "));
            return new javafx.beans.property.ReadOnlyObjectWrapper<>(text);
        });

        // Format IMDB rating to show 1 decimal place
        imdbColumn.setCellValueFactory(new PropertyValueFactory<>("imdbRating"));
        imdbColumn.setCellFactory(column -> new TableCell<Movie, Float>() {
            @Override
            protected void updateItem(Float rating, boolean empty) {
                super.updateItem(rating, empty);
                if (empty || rating == null) {
                    setText("");
                } else {
                    setText(String.format("%.1f", rating));
                }
            }
        });

        // Format personal rating, showing "Not rated" for 0.0
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("personalRating"));
        ratingColumn.setCellFactory(column -> new TableCell<Movie, Float>() {
            @Override
            protected void updateItem(Float rating, boolean empty) {
                super.updateItem(rating, empty);
                if (empty || rating == null) {
                    setText("");
                } else {
                    if (rating == 0.0f) {
                        setText("Not rated");
                    } else {
                        setText(String.format("%.1f", rating));
                    }
                }
            }
        });

        // Show movie details when a movie is selected
        movieTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldMovie, newMovie) -> {
                    if (newMovie != null) {
                        showMovieDetails(newMovie);
                    }
                }
        );
    }

    // Displays details of the selected movie in the right panel
    private void showMovieDetails(Movie movie) {
        detailTitle.setText("Title: " + movie.getTitle());
        detailImdb.setText("IMDB Rating: " + String.format("%.1f", movie.getImdbRating()));

        // Display categories as comma-separated list
        String categories = movie.getCategories().stream()
                .map(Category::getName)
                .collect(Collectors.joining(", "));
        detailCategories.setText("Categories: " + (categories.isEmpty() ? "None" : categories));

        personalRatingSlider.setValue(movie.getPersonalRating());
        currentRatingLabel.setText(String.format("%.1f", movie.getPersonalRating()));
    }

    // Handles changes to the personal rating slider
    @FXML
    private void handleRatingChange() {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();

        if (selectedMovie != null) {
            float newRating = (float) personalRatingSlider.getValue();

            // Only update if rating has actually changed
            if (Math.abs(selectedMovie.getPersonalRating() - newRating) > 0.001f) {
                try {
                    movieModel.updatePersonalRating(selectedMovie, newRating);
                    selectedMovie.setPersonalRating(newRating);
                    currentRatingLabel.setText(String.format("%.1f", newRating));
                    movieTable.refresh();
                    showInfo("Rating updated to: " + String.format("%.1f", newRating));
                } catch (Exception e) {
                    showError("Error updating rating: " + e.getMessage());
                    personalRatingSlider.setValue(selectedMovie.getPersonalRating());
                }
            }
        } else {
            showWarning("Please select a movie first.");
        }
    }

    // Sets up the filter listeners for title, rating, and categories
    private void setupFilters() {
        titleFilterField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        genreListView.getSelectionModel().getSelectedItems()
                .addListener((ListChangeListener<Category>) change -> applyFilters());
        genreListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }

    // Loads data from the model and applies filters
    private void loadData() throws SQLException {
        allMovies.setAll(movieModel.getMovies());
        genreListView.setItems(movieModel.getCategories());
        applyFilters();
    }

    // Applies all active filters to the movie list
    private void applyFilters() {
        String titleQuery = (titleFilterField.getText() == null)
                ? "" : titleFilterField.getText().toLowerCase().trim();
        double minRating = ratingSlider.getValue();
        List<Category> selectedCategories = genreListView.getSelectionModel().getSelectedItems();

        filteredMovies.setPredicate(movie -> {
            if (movie == null) return false;

            // Title filter (case-insensitive contains)
            if (!titleQuery.isEmpty()) {
                String t = movie.getTitle() == null ? "" : movie.getTitle().toLowerCase();
                if (!t.contains(titleQuery)) return false;
            }

            // Minimum IMDB rating filter
            if (movie.getImdbRating() < minRating) return false;

            // Category filter (match ANY selected category)
            if (selectedCategories != null && !selectedCategories.isEmpty()) {
                // If "All movies" is selected, don't filter by categories
                boolean allMoviesSelected = selectedCategories.stream()
                        .anyMatch(c -> c.getName() != null && c.getName().equalsIgnoreCase("All movies"));
                if (!allMoviesSelected) {
                    boolean matchesAny = selectedCategories.stream()
                            .anyMatch(sc -> movie.getCategories().contains(sc));
                    if (!matchesAny) return false;
                }
            }
            return true;
        });
    }

    // Handles adding a new category
    @FXML
    private void handleAddCategory() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Category");
        dialog.setHeaderText("Enter new category name:");
        dialog.setContentText("Category:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(categoryName -> {
            if (categoryName == null || categoryName.trim().isEmpty()) {
                showWarning("Category name cannot be empty!");
                return;
            }

            try {
                movieModel.addCategory(categoryName.trim());
                showInfo("Category added successfully!");
                applyFilters();
            } catch (Exception e) {
                showError("Error adding category: " + e.getMessage());
            }
        });
    }

    // Handles removing an existing category
    @FXML
    private void handleRemoveCategory() {
        Category selected = genreListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Remove Category");
            alert.setHeaderText("Are you sure you want to remove this category?");
            alert.setContentText("Category: " + selected.getName() + "\n\nThis will also remove it from all movies.");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                try {
                    movieModel.deleteCategory(selected);
                    showInfo("Category removed successfully!");
                    applyFilters();
                } catch (Exception e) {
                    showError("Error removing category: " + e.getMessage());
                }
            }
        } else {
            showWarning("Please select a category to remove.");
        }
    }

    // Handles adding categories to a selected movie
    @FXML
    private void handleAddCategoryToMovie() {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
        if (selectedMovie == null) {
            showWarning("Please select a movie first.");
            return;
        }

        // Get available categories not already assigned to this movie
        ObservableList<Category> availableCategories = movieModel.getCategories().filtered(category ->
                !selectedMovie.getCategories().contains(category)
        );

        if (availableCategories.isEmpty()) {
            showInfo("All available categories are already assigned to this movie.");
            return;
        }

        // Create dialog for selecting multiple categories
        Dialog<List<Category>> dialog = new Dialog<>();
        dialog.setTitle("Add Categories to Movie");
        dialog.setHeaderText("Select categories to add to: " + selectedMovie.getTitle());

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        ListView<Category> categorySelectList = new ListView<>();
        categorySelectList.setItems(availableCategories);
        categorySelectList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        dialog.getDialogPane().setContent(categorySelectList);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return categorySelectList.getSelectionModel().getSelectedItems();
            }
            return null;
        });

        Optional<List<Category>> result = dialog.showAndWait();
        result.ifPresent(categories -> {
            if (categories.isEmpty()) {
                showWarning("No categories selected.");
                return;
            }

            try {
                for (Category category : categories) {
                    movieModel.addCategoryToMovie(selectedMovie, category);
                }
                showInfo("Categories added to movie!");
                refreshMovieTable();
                showMovieDetails(selectedMovie);
            } catch (Exception e) {
                showError("Error adding categories: " + e.getMessage());
            }
        });
    }

    // Handles removing a category from a selected movie
    @FXML
    private void handleRemoveCategoryFromMovie() {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
        if (selectedMovie == null) {
            showWarning("Please select a movie first.");
            return;
        }

        if (selectedMovie.getCategories().isEmpty()) {
            showInfo("This movie has no categories to remove.");
            return;
        }

        ChoiceDialog<Category> dialog = new ChoiceDialog<>(
                selectedMovie.getCategories().get(0),
                selectedMovie.getCategories()
        );
        dialog.setTitle("Remove Category from Movie");
        dialog.setHeaderText("Select category to remove from: " + selectedMovie.getTitle());
        dialog.setContentText("Category:");

        Optional<Category> result = dialog.showAndWait();
        result.ifPresent(category -> {
            try {
                movieModel.removeCategoryFromMovie(selectedMovie, category);
                showInfo("Category removed from movie!");
                refreshMovieTable();
                showMovieDetails(selectedMovie);
            } catch (Exception e) {
                showError("Error removing category: " + e.getMessage());
            }
        });
    }

    // Clears all active filters
    @FXML
    private void clearFilters(ActionEvent actionEvent) {
        titleFilterField.clear();
        genreListView.getSelectionModel().clearSelection();
        ratingSlider.setValue(0);
        applyFilters();
    }

    // Plays the selected movie and updates last view date
    @FXML
    private void playMovie(ActionEvent actionEvent) {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();
        if (selectedMovie != null) {
            try {
                selectedMovie.setLastView(java.time.LocalDate.now());
                showInfo("Playing movie: " + selectedMovie.getTitle());
                movieModel.openMovieInSystemPlayer(selectedMovie);
            } catch (Exception e) {
                showError("Error playing movie: " + e.getMessage());
            }
        } else {
            showWarning("Please select a movie to play.");
        }
    }

    // Refreshes the movie table with updated data
    public void refreshMovieTable() throws SQLException {
        allMovies.clear();
        allMovies.addAll(movieModel.getMovies());
        applyFilters();
    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    // Shows an error dialog with the given message
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Shows a warning dialog with the given message
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Shows an information dialog with the given message
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleAddMovie(ActionEvent actionEvent)
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Movie file");

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.mpeg4")
        );

        File file = fileChooser.showOpenDialog(movieTable.getScene().getWindow());
        if (file == null) return;

        try
        {
            Movie movie = new Movie(0, file.getName(), 0f, 0f, file.getAbsolutePath(), null);
            movieModel.createMovie(movie);
            allMovies.add(movie);
            showInfo("Movie added: " + file.getName());

        } catch (SQLException e) {
            showError("Could not add movie: " + e.getMessage());
        }
    }

    @FXML
    private void handleRemoveMovie(ActionEvent actionEvent)
    {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();

        if (selectedMovie == null)
        {
            showWarning("Please select a movie to delete!");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete movie");
        alert.setHeaderText("Are you sure you want to delete this movie?");
        alert.setContentText("Movie: " + selectedMovie.getTitle());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK)
        {
            try
            {
                movieModel.deleteMovie(selectedMovie);

                allMovies.remove(selectedMovie);
                showInfo("Movie deleted successfully!");
            }
            catch (Exception e)
            {
                showError("Error deleting movie: " + e.getMessage());
            }
        }

    }

}