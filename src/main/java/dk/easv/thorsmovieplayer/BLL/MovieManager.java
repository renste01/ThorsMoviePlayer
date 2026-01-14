package dk.easv.thorsmovieplayer.BLL;
// Project imports
import dk.easv.thorsmovieplayer.BE.Movie;
import dk.easv.thorsmovieplayer.DAL.CategoryDAO;
import dk.easv.thorsmovieplayer.DAL.MovieDAO;
// Java imports
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;

public class MovieManager
{
    private MovieDAO movieDAO;
    private CategoryDAO categoryDAO;
    private static final String DATA_FOLDER = "data";

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
        if (movie == null)
        {
            throw new IllegalArgumentException("Movie cannot be null");
        }

        File file = new File(movie.getFilePath());
        if (!file.exists())
        {
            throw new IllegalArgumentException("Movie file does not exist");
        }
        // Checks if file is in data folder
        if (!isFileInDataFolder(file))
        {
            throw new IllegalArgumentException("Movie files must be located in the 'data' folder. Please move your file to: " + new File(DATA_FOLDER).getAbsolutePath());
        }
        Movie m = new Movie(0, file.getName(), 0f, 0f, file.getAbsolutePath(), null);

        return movieDAO.createMovie(movie);

    }

    public void deleteMovie(Movie movie) throws SQLException {
        movieDAO.deleteMovie(movie);
    }

    public void updatePersonalRating(Movie movie, float newRating) throws SQLException {
        movie.setPersonalRating(newRating);
        movieDAO.updateMovie(movie);
    }

/* This method checks to see if the file is in the data folder, and if not it creates a copy in that folder so the
  movie can be played*/
    private boolean isFileInDataFolder(File file)
    {
        try
        {
            File dataDir = new File(DATA_FOLDER);

            // Makes sure the data folder exists
            if (!dataDir.exists())
            {
                dataDir.mkdirs();
            }

            String filePath = file.getCanonicalPath();
            String dataPath = dataDir.getCanonicalPath();

            // Checks if file is already in the data folder
            if (filePath.startsWith(dataPath))
            {
                return true;
            }

            // If not, copies the file into data folder
            Files.copy(
                    file.toPath(),
                    new File(dataDir, file.getName()).toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
