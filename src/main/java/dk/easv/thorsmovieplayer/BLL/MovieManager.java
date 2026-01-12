package dk.easv.thorsmovieplayer.BLL;

import dk.easv.thorsmovieplayer.BE.Movie;
import dk.easv.thorsmovieplayer.DAL.CategoryDAO;
import dk.easv.thorsmovieplayer.DAL.MovieDAO;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;

public class MovieManager
{
    private MovieDAO movieDAO;
    private CategoryDAO categoryDAO;

    public MovieManager() throws IOException
    {
        movieDAO = new MovieDAO();
        categoryDAO = new CategoryDAO();
    }
    // Gets all movies and loads their categories
    public List<Movie> getAllMovies() throws SQLException
    {
        List<Movie> movies = movieDAO.getAllMovies();

        // Load categories for each movie
        for (Movie movie : movies){
            List<dk.easv.thorsmovieplayer.BE.Category> categories = categoryDAO.getCategoriesForMovie(movie.getId());
            movie.setCategories(categories);
        }

        return movies;
    }



    public Movie createMovie(Movie movie) throws SQLException
    {
        return movieDAO.createMovie(movie);
    }

    public void updateMovie(Movie movie) throws SQLException {
        movieDAO.updateMovie(movie);
    }

    public void deleteMovie(Movie movie) throws SQLException {
        movieDAO.deleteMovie(movie);
    }

    public void updatePersonalRating(Movie movie, float newRating) throws SQLException {
        movie.setPersonalRating(newRating);
        movieDAO.updateMovie(movie);
    }


    public void importMoviesFromFolder(String data) throws IOException {
        try {
            MovieDAO movieDAO = new MovieDAO();
            File folder = new File(data);

            if (!folder.exists() || !folder.isDirectory()) {
                System.out.println("Invalid folder: " + data);
                return;
            }

            File[] files = folder.listFiles();
            if (files == null) return;

            for (File file : files) {
                if (!file.isFile()) continue;

                String name = file.getName().toLowerCase();
                if (name.endsWith(".mp4") || name.endsWith(".mpeg4")) {

                    // Use absolute path as an identifier
                    String path = file.getAbsolutePath();

                    // Check if movie already exists in DB to avoid duplicates
                    if (movieDAO.getAllMovies().stream()
                            .anyMatch(m -> m.getFilePath().equals(path))) {
                        continue; // skip duplicates
                    }

                    // Create Movie object
                    Movie movie = new Movie(
                            0,
                            file.getName(),
                            0.0f,
                            0.0f,
                            file.getAbsolutePath(),
                            null
                    );

                    // Save movie to the databse
                    movieDAO.createMovie(movie);
                    System.out.println("Imported movie: " + path);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
