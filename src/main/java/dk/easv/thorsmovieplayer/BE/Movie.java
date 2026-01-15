package dk.easv.thorsmovieplayer.BE;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Movie {
    private int id;
    private String title;
    private float imdbRating;
    private float personalRating;
    private String filePath;
    private LocalDate lastView;
    private List<Category> categories;

    // Constructor for loading movies from database
    public Movie(int id, String title, float imdbRating, float personalRating, String filePath, LocalDate lastView) {
        this.id = id;
        this.title = title;
        this.imdbRating = imdbRating;
        this.personalRating = personalRating;
        this.filePath = filePath;
        this.lastView = lastView;
        this.categories = new ArrayList<>();
    }

    // Constructor for creating new movies
    public Movie(String title, float imdbRating, String filePath) {
        this.title = title;
        this.imdbRating = imdbRating;
        this.filePath = filePath;
        this.personalRating = 0.0f;
        this.lastView = null;
        this.categories = new ArrayList<>();
    }

    // Getter methods
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public float getImdbRating() {
        return imdbRating;
    }

    public float getPersonalRating() {
        return personalRating;
    }

    public String getFilePath() {
        return filePath;
    }

    public LocalDate getLastView() {
        return lastView;
    }

    public List<Category> getCategories() {
        return categories;
    }

    // Setter methods
    public void setId(int id) {
        this.id = id;
    }

    public void setImdbRating(float imdbRating) {
        this.imdbRating = imdbRating;
    }

    public void setPersonalRating(float personalRating) {
        this.personalRating = personalRating;
    }

    public void setLastView(LocalDate lastView) {
        this.lastView = lastView;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    // Category management methods
    public void addCategory(Category category) {
        if (!categories.contains(category)) {
            categories.add(category);
        }
    }

    public void removeCategory(Category category) {
        categories.remove(category);
    }

    public boolean hasCategory(Category category) {
        return categories.contains(category);
    }

    // Returns the movie title and IMDB rating when displayed in lists
    @Override
    public String toString() {
        return title + " (" + imdbRating + ")";
    }
}