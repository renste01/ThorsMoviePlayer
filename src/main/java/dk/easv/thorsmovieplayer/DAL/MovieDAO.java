package dk.easv.thorsmovieplayer.DAL;

// Project imports
import dk.easv.thorsmovieplayer.BE.Movie;

// Java imports
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MovieDAO
{
    private DBConnector dbConnector;

    public MovieDAO() throws IOException
    {
        this.dbConnector = DBConnector.getInstance();
    }

    // CREATE
    public Movie createMovie(Movie movie) throws SQLException
    {
        String sql = """
            INSERT INTO Movie (title, filePath, lastView, personalRating, imdbRating)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getFilePath());

            if (movie.getLastView() != null)
                stmt.setTimestamp(3, Timestamp.valueOf(movie.getLastView().atStartOfDay()));
            else
                stmt.setNull(3, Types.TIMESTAMP);

            stmt.setFloat(4, movie.getPersonalRating());
            stmt.setFloat(5, movie.getImdbRating());

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next())
                movie.setId(rs.getInt(1));
        }
        return movie;
    }

    // READ ALL
    public List<Movie> getAllMovies() throws SQLException
    {
        List<Movie> movies = new ArrayList<>();

        String sql = """
            SELECT id, title, filePath, lastView, personalRating, imdbRating
            FROM Movie
            """;

        try (Connection conn = dbConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql))
        {
            while (rs.next())
                movies.add(mapRowToMovie(rs));
        }
        return movies;
    }

    // UPDATE - CORRECTED VERSION (3 SET values + WHERE)
    public void updateMovie(Movie movie) throws SQLException
    {
        String sql = """
            UPDATE Movie
            SET title = ?, imdbRating = ?, personalRating = ?
            WHERE id = ?
            """;

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            // Only set the 3 parameters that exist in SQL
            ps.setString(1, movie.getTitle());
            ps.setFloat(2, movie.getImdbRating());
            ps.setFloat(3, movie.getPersonalRating());

            // Set the WHERE condition (4th parameter)
            ps.setInt(4, movie.getId());

            ps.executeUpdate();
        }
    }

    // DELETE
    public void deleteMovie(Movie movie) throws SQLException
    {
        String sqlMovie = "DELETE FROM Movie WHERE id = ?";

        try (Connection conn = DBConnector.getConnection();
             PreparedStatement psMov = conn.prepareStatement(sqlMovie))
        {
            psMov.setInt(1, movie.getId());
            psMov.executeUpdate();
        }
    }

    // MAPPER
    private Movie mapRowToMovie(ResultSet rs) throws SQLException
    {
        LocalDate lastView = null;
        Timestamp ts = rs.getTimestamp("lastView");
        if (ts != null)
            lastView = ts.toLocalDateTime().toLocalDate();

        return new Movie(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getFloat("imdbRating"),
                rs.getFloat("personalRating"),
                rs.getString("filePath"),
                lastView
        );
    }
}