package dk.easv.thorsmovieplayer.BLL;

import dk.easv.thorsmovieplayer.BE.Category;
import dk.easv.thorsmovieplayer.BE.Movie;
import dk.easv.thorsmovieplayer.DAL.CategoryDAO;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class CategoryManager {
    private CategoryDAO categoryDAO;

    public CategoryManager() throws IOException {
        categoryDAO = new CategoryDAO();
    }

    // Creates a new category with validation
    public Category createCategory(String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }

        Category category = new Category(name.trim());
        return categoryDAO.createCategory(category);
    }

    // Gets all categories from database
    public List<Category> getAllCategories() throws SQLException {
        return categoryDAO.getAllCategories();
    }

    // Deletes a category after validation
    public void deleteCategory(Category category) throws SQLException {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        categoryDAO.deleteCategory(category);
    }

    // Updates a category with validation
    public void updateCategory(Category category) throws SQLException {
        if (category == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid category data");
        }
        categoryDAO.updateCategory(category);
    }

    // Assigns a category to a movie
    public void addCategoryToMovie(Movie movie, Category category) throws SQLException {
        if (movie == null || category == null) {
            throw new IllegalArgumentException("Movie and category cannot be null");
        }
        categoryDAO.addCategoryToMovie(movie.getId(), category.getId());

        // Also update the in-memory movie object
        if (!movie.getCategories().contains(category)) {
            movie.addCategory(category);
        }
    }

    // Removes a category from a movie
    public void removeCategoryFromMovie(Movie movie, Category category) throws SQLException {
        if (movie == null || category == null) {
            throw new IllegalArgumentException("Movie and category cannot be null");
        }
        categoryDAO.removeCategoryFromMovie(movie.getId(), category.getId());

        // Also update the in-memory movie object
        movie.removeCategory(category);
    }

    // Gets all categories for a specific movie
    public List<Category> getCategoriesForMovie(Movie movie) throws SQLException {
        if (movie == null) {
            throw new IllegalArgumentException("Movie cannot be null");
        }
        return categoryDAO.getCategoriesForMovie(movie.getId());
    }

    // Gets all movies in a specific category
    public List<Movie> getMoviesByCategory(Category category) throws SQLException {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        return categoryDAO.getMoviesByCategory(category.getId());
    }

    // Gets movies that have ALL specified categories
    public List<Movie> getMoviesByMultipleCategories(List<Category> categories) throws SQLException {
        if (categories == null || categories.isEmpty()) {
            throw new IllegalArgumentException("Categories list cannot be empty");
        }

        // Start with movies from first category
        List<Movie> movies = getMoviesByCategory(categories.get(0));

        // Filter to keep only movies that have ALL categories
        for (int i = 1; i < categories.size(); i++) {
            final Category currentCategory = categories.get(i);
            movies.removeIf(movie -> {
                try {
                    List<Category> movieCategories = getCategoriesForMovie(movie);
                    return !movieCategories.contains(currentCategory);
                } catch (SQLException e) {
                    return true;
                }
            });
        }

        return movies;
    }

    // Loads categories for all movies in a list
    public void loadCategoriesForMovies(List<Movie> movies) throws SQLException {
        for (Movie movie : movies) {
            List<Category> categories = getCategoriesForMovie(movie);
            movie.setCategories(categories);
        }
    }
}