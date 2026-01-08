package dk.easv.thorsmovieplayer.DAL;
import dk.easv.thorsmovieplayer.BE.Movie;



import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MovieDAOSQLite implements MovieDAO {


    private static final String DB_URL = "jdbc:sqlite:movies.db";





    @Override
    public List<Movie> getAllMovies() throws Exception {
        return List.of();
    }

    @Override
    public void addMovie(Movie movie) throws Exception {
        //SQL KOMANDO, VALUES HOLDER PLADSER TIL VÆRDIERNE
     String sql = "INSERT INTO Movie(title, imbdRating, personalRating, filePath, lastView) Values(?,?,?,?,?)";

        //forbinder til vorews database
        try (Connection conn = DriverManager.getConnection(DB_URL);
             //(sql) som i String sql 2 linjer over
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, movie.getTitle());
            ps.setFloat(2, movie.getImdbRating());
            ps.setNull(3, Types.INTEGER);
            ps.setString(4, movie.getFilePath());
            ps.setNull(5, Types.DATE);

            ps.executeUpdate();
        }
    }

    @Override
    public void deleteMovie(Movie movie) throws Exception {

    }

    @Override
    public void updateMovie(Movie movie) throws Exception {

    }

    @Override
    public Movie getMovie(int id) throws Exception {
        return null;
    }
}
