package dk.easv.thorsmovieplayer.DAL;
//Project imports
import dk.easv.thorsmovieplayer.BE.Movie;

//Java Imports
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.sql.SQLException;

public class MovieDAO
{
    private DBConnector dbConnector;

    public MovieDAO() throws IOException
    {
        this.dbConnector = DBConnector.getInstance();
    }
    public Movie createMovie(Movie movie) throws SQLException{
        String sql = "INSERT INTO Movie (title, filePath, lastView, personalRating, imdbRating) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dbConnector.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
        stmt.setString(1, movie.getTitle());
        stmt.setString(2, movie.getFilePath());
        stmt.setTimestamp(3, Timestamp.valueOf(movie.getLastView()));
        stmt.setFloat(4, movie.getPersonalRating());
        stmt.setFloat(5, movie.getImdbRating());

        stmt.executeUpdate();

        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {movie.setId(rs.getInt(1));}
        }
        return movie;
    }
    public List<Movie> getAllMovies() throws SQLException
    {
        List<Movie> movies = new ArrayList<>();

        String sql = """
            SELECT id, title, filePath, lastView, personalRating, imdbRating FROM Movie
            """;
        try (Connection conn = dbConnector.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql))
        {
            while (rs.next()){ movies.add(mapRowToMovie(rs));}
        }
        return movies;
    }

    private Movie mapRowToMovie(ResultSet rs) throws SQLException {
        LocalDateTime lastView = null;
        Timestamp ts = rs.getTimestamp("lastView");
        if (ts != null) {
            lastView = ts.toLocalDateTime();
        }

        return new Movie(
                rs.getInt("id"),
                rs.getString("title"),
                rs.getString("filePath"),
                lastView,
                rs.getFloat("personalRating"),
                rs.getFloat("imdbRating")
        );
    }

}
