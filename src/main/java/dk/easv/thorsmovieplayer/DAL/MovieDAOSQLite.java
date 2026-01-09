package dk.easv.thorsmovieplayer.DAL;

import dk.easv.thorsmovieplayer.BE.Movie;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MovieDAOSQLite {

    private static final String DB_URL = "jdbc:sqlite:movies.db";

    // Gets all movies from the SQLite database
    public List<Movie> getAllMovies() throws Exception {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM Movie";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Movie movie = new Movie(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getFloat("imdbRating"),
                        rs.getFloat("personalRating"),
                        rs.getString("filePath"),
                        rs.getTimestamp("lastView").toLocalDateTime()
                );
                movies.add(movie);
            }
        }
        return movies;
    }

    // Adds a new movie to the SQLite database
    public void addMovie(Movie movie) throws Exception {
        String sql = "INSERT INTO Movie(title, imdbRating, personalRating, filePath, lastView) VALUES(?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, movie.getTitle());
            ps.setFloat(2, movie.getImdbRating());
            ps.setFloat(3, movie.getPersonalRating());
            ps.setString(4, movie.getFilePath());

            if (movie.getLastView() != null) {
                ps.setDate(5, Date.valueOf(movie.getLastView()));
            } else {
                ps.setNull(5, Types.DATE);
            }

            ps.executeUpdate();
        }
    }

    // Deletes a movie from the SQLite database
    public void deleteMovie(Movie movie) throws Exception {
        String sql = "DELETE FROM Movie WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, movie.getId());
            ps.executeUpdate();
        }
    }



    // Gets a single movie by ID from the SQLite database
    public Movie getMovie(int id) throws Exception {
        String sql = "SELECT * FROM Movie WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Movie(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getFloat("imdbRating"),
                        rs.getFloat("personalRating"),
                        rs.getString("filePath"),
                        rs.getTimestamp("lastView").toLocalDateTime()
                );
            }
        }
        return null;
    }
}