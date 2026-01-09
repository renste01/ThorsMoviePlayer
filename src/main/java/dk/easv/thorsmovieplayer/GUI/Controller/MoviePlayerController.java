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
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MoviePlayerController implements Initializable {
    // Make sure ALL these @FXML declarations exist:
    @FXML private Label welcomeText;
    @FXML private TextField titleFilterField;
    @FXML private ListView<Category> genreListView;
    @FXML private Slider ratingSlider;
    @FXML private TableView<Movie> movieTable;  // <-- THIS ONE IS CRITICAL
    @FXML private TableColumn<Movie, String> titleColumn;
    @FXML private TableColumn<Movie, List<Category>> categoryColumn;
    @FXML private TableColumn<Movie, Float> imdbColumn;
    @FXML private TableColumn<Movie, Float> ratingColumn;
    @FXML private Label detailTitle;
    @FXML private Label detailCategories;
    @FXML private Label detailImdb;
    @FXML private Slider personalRatingSlider;

    private MovieModel movieModel;
    private ObservableList<Movie> allMovies;
    private FilteredList<Movie> filteredMovies;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            movieModel = new MovieModel();
            allMovies = FXCollections.observableArrayList(movieModel.getMovies());

            // Wrap movies in a FilteredList
            filteredMovies = new FilteredList<>(allMovies, m -> true);
            movieTable.setItems(filteredMovies);

            setupTableView();
            setupFilters();
            loadData();
        } catch (IOException | SQLException e) {
            showError("Error initializing application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupTableView() {
        // Set up cell value factories
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        imdbColumn.setCellValueFactory(new PropertyValueFactory<>("imdbRating"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("personalRating"));

        // Custom cell factory for categories column
        categoryColumn.setCellFactory(column -> new TableCell<Movie, List<Category>>() {
            @Override
            protected void updateItem(List<Category> categories, boolean empty) {
                super.updateItem(categories, empty);

                if (empty || categories == null || categories.isEmpty()) {
                    setText("");
                } else {
                    // Join category names with commas
                    String categoriesText = categories.stream()
                            .map(Category::getName)
                            .collect(Collectors.joining(", "));
                    setText(categoriesText);
                }
            }
        });

        // Listen for movie selection changes
        movieTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                showMovieDetails(newValue);
            }
        });
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
        // Load all movies
        allMovies.setAll(movieModel.getMovies());
        movieTable.setItems(allMovies);  // <-- This uses movieTable

        // Load categories for filter list
        genreListView.setItems(movieModel.getCategories());
    }


    private void applyFilters() {
        String titleQuery = titleFilterField.getText().toLowerCase().trim();
        double minRating = ratingSlider.getValue();
        List<Category> selectedCategories = genreListView.getSelectionModel().getSelectedItems();

        filteredMovies.setPredicate(movie -> {
            if (movie == null) return false;

            // Title filter
            if (!titleQuery.isEmpty() && !movie.getTitle().toLowerCase().contains(titleQuery)) {
                return false;
            }

            // IMDB rating filter
            if (movie.getImdbRating() < minRating) {
                return false;
            }

            // Category filter (match ANY selected category)
            if (!selectedCategories.isEmpty()) {
                boolean matches = selectedCategories.stream().anyMatch(movie.getCategories()::contains);
                if (!matches) return false;
            }

            return true;
        });
    }


    private void showMovieDetails(Movie movie) {
        detailTitle.setText("Title: " + movie.getTitle());
        detailImdb.setText("IMDB Rating: " + movie.getImdbRating());

        // Display categories as comma-separated list
        String categories = movie.getCategories().stream()
                .map(Category::getName)
                .collect(Collectors.joining(", "));
        detailCategories.setText("Categories: " + (categories.isEmpty() ? "None" : categories));

        personalRatingSlider.setValue(movie.getPersonalRating());
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
                selectedMovie.setLastView(LocalDate.from(LocalDateTime.now()));
                showInfo("Playing movie: " + selectedMovie.getTitle());
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