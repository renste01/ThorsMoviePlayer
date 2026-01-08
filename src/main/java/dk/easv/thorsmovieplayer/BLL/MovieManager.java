package dk.easv.thorsmovieplayer.BLL;

import dk.easv.thorsmovieplayer.BE.Movie;
import dk.easv.thorsmovieplayer.DAL.MovieDAO;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class MovieManager
{
    private MovieDAO movieDAO;

    public MovieManager() throws IOException
    {
        movieDAO = new MovieDAO();
    }
    public List<Movie> getAllMovies() throws SQLException
    {
        return movieDAO.getAllMovies();
    }

}
