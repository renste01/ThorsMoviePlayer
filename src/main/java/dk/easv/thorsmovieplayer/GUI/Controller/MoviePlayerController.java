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
import javafx.util.Callback;
import javafx.collections.ListChangeListener;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MoviePlayerController implements Initializable {
    // Make sure ALL these @FXML declarations exist:
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
            allMovies = FXCollections.observableArrayList(movieModel.getMovies()); //This works as a bakcing list, so it basically holds the "master" movies that FilteredList will wrap

            // Wrap movies in a FilteredList
            filteredMovies = new FilteredList<>(allMovies, m -> true);

            movieTable.setItems(filteredMovies); //Now the tableView will be bound to the filteredList and not directly to AllMovies

            setupTableView();
            setupFilters();
            loadData(); //Refreshes AllMovies and categories and applies the filters
            checkMoviesToDelete();

            // The Rating label will now show the number in real-time as slider moves
            personalRatingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                // This will update the label as user moves the slider with 1 decimal place
                currentRatingLabel.setText(String.format("%.1f", newVal.doubleValue()));
            });


        } catch (IOException | SQLException e) {
            showError("Error initializing application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Movie> checkMoviesToDelete() {
        try {
            List<Movie> moviesToDelete = movieModel.getMovies().stream()
                    .filter(movie -> movie.getPersonalRating() != 0f && movie.getPersonalRating() < 6) // checks if it has a rating and it's under 6
                    .filter(movie -> movie.getLastView() != null && movie.getLastView().isBefore(java.time.LocalDate.now().minusYears(2))) // checks last viewdate
                    .collect(Collectors.toList()); // adds movies to the warning list if they have the criterias above
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



    private void setupTableView() {
        //Set up cell value factories
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        //Categories as a joined string shown directly
        //A joined string is when you take a collection of values (like a List<String>) and combine them into one string with a separator such as a comma and a space).
        categoryColumn.setCellValueFactory(cellData -> {
            String text = cellData.getValue().getCategories().stream()
                    .map(Category::getName)
                    .collect(Collectors.joining(", "));
            return new javafx.beans.property.ReadOnlyObjectWrapper<>(text);
        });

        //Format IMDB rating column (show 1 decimal place)
        imdbColumn.setCellValueFactory(new PropertyValueFactory<>("imdbRating"));
        imdbColumn.setCellFactory(column -> new TableCell<Movie, Float>() {

            @Override
            protected void updateItem(Float rating, boolean empty) {
                super.updateItem(rating, empty);
                if (empty || rating == null) {
                    setText("");
                } else {
                    // Format to show 1 decimal place (e.g., "8.5")
                    setText(String.format("%.1f", rating));
                }
            }
        });

        // Format personal rating column (shows “Not rated” for 0.0)
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("personalRating"));
        ratingColumn.setCellFactory(column -> new TableCell<Movie, Float>() {

            @Override
            protected void updateItem(Float rating, boolean empty) {
                super.updateItem(rating, empty);
                if (empty || rating == null) {
                    setText("");
                } else {
                    // Format to show 1 decimal place or "Not rated" for 0
                    if (rating == 0.0f) {
                        setText("Not rated");
                    } else {
                        setText(String.format("%.1f", rating));
                    }
                }
            }
        });

    }


    @FXML
    private void handleRatingChange() {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();

        if (selectedMovie != null) {
            // Get the exact value from slider (0.1 increments)
            float newRating = (float) personalRatingSlider.getValue();

            // Only update if rating has actually changed (using small epsilon)
            if (Math.abs(selectedMovie.getPersonalRating() - newRating) > 0.001f) {
                try {
                    // Update the model
                    movieModel.updatePersonalRating(selectedMovie, newRating);

                    // Update the local movie object
                    selectedMovie.setPersonalRating(newRating);

                    // Update the label with formatted value (1 decimal place)
                    currentRatingLabel.setText(String.format("%.1f", newRating));

                    // Refresh the table to show updated rating
                    movieTable.refresh();

                    showInfo("Rating updated to: " + String.format("%.1f", newRating));
                } catch (Exception e) {
                    showError("Error updating rating: " + e.getMessage());
                    // Revert slider to original value
                    personalRatingSlider.setValue(selectedMovie.getPersonalRating());
                }
            }
        } else {
            showWarning("Please select a movie first.");
        }
    }
    private void setupFilters() {
        // Title text
        titleFilterField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Rating slider
        ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Category selection (MULTIPLE) — use ListChangeListener
        genreListView.getSelectionModel().getSelectedItems()
                .addListener((ListChangeListener<Category>) change -> applyFilters());

        // Multiple selection mode
        genreListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }



    private void loadData() throws SQLException {
        // This refreshes the backing list that filteredMovies wraps (look in initialize)
        allMovies.setAll(movieModel.getMovies());

        // Load categories for filter list
        genreListView.setItems(movieModel.getCategories());

        // Re-apply filters after data load
        applyFilters();
    }

    private void applyFilters() {
        String titleQuery = (titleFilterField.getText() == null)
                ? "" : titleFilterField.getText().toLowerCase().trim();
        double minRating = ratingSlider.getValue();
        List<Category> selectedCategories = genreListView.getSelectionModel().getSelectedItems();

        //Single predicate that combines Title + Min IMDB + Category criteria
        //predicate is function that takes items and returns true or false. So here it shows the FilteredList which movies to keep in the list using true or false.
        filteredMovies.setPredicate(movie -> {
            if (movie == null) return false;

            //Title filter (case-insensitive contains)
            if (!titleQuery.isEmpty()) {
                String t = movie.getTitle() == null ? "" : movie.getTitle().toLowerCase();
                if (!t.contains(titleQuery)) return false;
            }

            //Min IMDB (internet movie database) rating
            if (movie.getImdbRating() < minRating) return false;

            //Category filter (match ANY selected category)
            if (selectedCategories != null && !selectedCategories.isEmpty()) {
                boolean matchesAny = selectedCategories.stream()
                        .anyMatch(movie.getCategories()::contains);
                if (!matchesAny) return false;
            }

            return true;
        });
    }



    private void showMovieDetails(Movie movie) {
        detailTitle.setText("Title: " + movie.getTitle());
        detailImdb.setText("IMDB Rating: " + String.format("%.1f", movie.getImdbRating()));

        // Display categories as comma-separated list
        String categories = movie.getCategories().stream()
                .map(Category::getName)
                .collect(Collectors.joining(", "));
        detailCategories.setText("Categories: " + (categories.isEmpty() ? "None" : categories));

        personalRatingSlider.setValue(movie.getPersonalRating());

        // Update the rating label
        currentRatingLabel.setText(String.format("%.1f", movie.getPersonalRating()));
    }

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

    @FXML
    private void handleAddCategoryToMovie() {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();  // <-- This uses movieTable
        if (selectedMovie == null) {
            showWarning("Please select a movie first.");
            return;
        }

        // Get available categories (not already assigned to this movie)
        ObservableList<Category> availableCategories = movieModel.getCategories().filtered(category ->
                !selectedMovie.getCategories().contains(category)
        );

        if (availableCategories.isEmpty()) {
            showInfo("All available categories are already assigned to this movie.");
            return;
        }

        // Create dialog with multiple selection
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

    @FXML
    private void handleRemoveCategoryFromMovie() {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();  // <-- This uses movieTable
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

    @FXML
    private void clearFilters(ActionEvent actionEvent) {
        titleFilterField.clear();
        genreListView.getSelectionModel().clearSelection();
        ratingSlider.setValue(0);
        applyFilters();
    }


    @FXML
    private void playMovie(ActionEvent actionEvent) {
        Movie selectedMovie = movieTable.getSelectionModel().getSelectedItem();  // <-- This uses movieTable
        if (selectedMovie != null) {
            try {
                selectedMovie.setLastView(java.time.LocalDate.now()); // Updates lastview
                showInfo("Playing movie: " + selectedMovie.getTitle());
                movieModel.openMovieInSystemPlayer(selectedMovie); //Plays the movie
            } catch (Exception e) {
                showError("Error playing movie: " + e.getMessage());
            }
        } else {
            showWarning("Please select a movie to play.");
        }
    }

    private void refreshMovieTable() throws SQLException {
        allMovies.clear();
        allMovies.addAll(movieModel.getMovies());
        applyFilters();
    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}