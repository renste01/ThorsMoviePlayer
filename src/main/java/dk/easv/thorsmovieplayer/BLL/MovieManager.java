package dk.easv.thorsmovieplayer.BLL;

import dk.easv.thorsmovieplayer.BE.Movie;
import dk.easv.thorsmovieplayer.DAL.CategoryDAO;
import dk.easv.thorsmovieplayer.DAL.MovieDAO;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

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



}
