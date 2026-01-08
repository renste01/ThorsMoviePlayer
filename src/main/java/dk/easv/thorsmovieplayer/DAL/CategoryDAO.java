package dk.easv.thorsmovieplayer.DAL;

import dk.easv.thorsmovieplayer.BE.Category;
import dk.easv.thorsmovieplayer.BE.Movie;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;  // ADD THIS IMPORT
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private DBConnector dbConnector;

    public CategoryDAO() throws IOException {
        this.dbConnector = DBConnector.getInstance();
    }

    // Saves a new category to database and returns it with generated ID
    public Category createCategory(Category category) throws SQLException {
        String sql = "INSERT INTO Category (name) VALUES (?)";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, category.getName());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                category.setId(rs.getInt(1));
            }
        }
        return category;
    }

    // Gets all categories from database sorted by name
    public List<Category> getAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT id, name FROM Category ORDER BY name";

        try (Connection conn = dbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                categories.add(mapRowToCategory(rs));
            }
        }
        return categories;
    }

    // Deletes a category and removes it from all movies
    public void deleteCategory(Category category) throws SQLException {
        // First remove category from all movies
        String deleteRelationsSql = "DELETE FROM MovieCategory WHERE categoryId = ?";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteRelationsSql)) {
            stmt.setInt(1, category.getId());
            stmt.executeUpdate();
        }

        // Then delete the category itself
        String deleteCategorySql = "DELETE FROM Category WHERE id = ?";
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteCategorySql)) {
            stmt.setInt(1, category.getId());
            stmt.executeUpdate();
        }
    }

    // Updates a category's name in database
    public void updateCategory(Category category) throws SQLException {
        String sql = "UPDATE Category SET name = ? WHERE id = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.getName());
            stmt.setInt(2, category.getId());
            stmt.executeUpdate();
        }
    }

    // Links a category to a movie in the junction table
    public void addCategoryToMovie(int movieId, int categoryId) throws SQLException {
        String sql = "INSERT INTO MovieCategory (movieId, categoryId) VALUES (?, ?)";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movieId);
            stmt.setInt(2, categoryId);
            stmt.executeUpdate();
        }
    }

    // Removes a category from a movie in the junction table
    public void removeCategoryFromMovie(int movieId, int categoryId) throws SQLException {
        String sql = "DELETE FROM MovieCategory WHERE movieId = ? AND categoryId = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movieId);
            stmt.setInt(2, categoryId);
            stmt.executeUpdate();
        }
    }

    // Gets all categories assigned to a specific movie
    public List<Category> getCategoriesForMovie(int movieId) throws SQLException {
        List<Category> categories = new ArrayList<>();
        String sql = """
            SELECT c.id, c.name 
            FROM Category c
            INNER JOIN MovieCategory mc ON c.id = mc.categoryId
            WHERE mc.movieId = ?
            ORDER BY c.name
            """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movieId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                categories.add(mapRowToCategory(rs));
            }
        }
        return categories;
    }

    // Gets all movies that belong to a specific category
    public List<Movie> getMoviesByCategory(int categoryId) throws SQLException {
        List<Movie> movies = new ArrayList<>();
        String sql = """
            SELECT m.id, m.title, m.imdbRating, m.personalRating, m.filePath, m.lastView
            FROM Movie m
            INNER JOIN MovieCategory mc ON m.id = mc.movieId
            WHERE mc.categoryId = ?
            ORDER BY m.title
            """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                movies.add(mapRowToMovie(rs));
            }
        }
        return movies;
    }

    // Converts a database row to a Category object
    private Category mapRowToCategory(ResultSet rs) throws SQLException {
        return new Category(
                rs.getInt("id"),
                rs.getString("name")
        );
    }

    // Converts a database row to a Movie object
    private Movie mapRowToMovie(ResultSet rs) throws SQLException {
        LocalDate lastView = null;  // Changed from LocalDateTime to LocalDate
        Timestamp ts = rs.getTimestamp("lastView");
        if (ts != null) {
            lastView = ts.toLocalDateTime().toLocalDate();  // Convert DateTime to Date
        }

        return new Movie(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getFloat("imdbRating"),
                rs.getFloat("personalRating"),
                rs.getString("filePath"),
                ts != null ? ts.toLocalDateTime() : null  // Keep as LocalDateTime for constructor
        );
    }
}