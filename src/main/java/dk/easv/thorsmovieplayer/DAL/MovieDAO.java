    package dk.easv.thorsmovieplayer.DAL;

    // Project imports
    import dk.easv.thorsmovieplayer.BE.Movie;

    // Java imports
    import java.io.IOException;
    import java.sql.*;
    import java.time.LocalDate;
    import java.time.LocalDateTime;
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

        // UPDATE
        public void updateMovie(Movie movie) throws SQLException
        {
            String sql = """
                UPDATE Movie
                SET title = ?, imdbRating = ?, personalRating = ?, filePath = ?, lastView = ?
                WHERE id = ?
                """;

            try (Connection conn = dbConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql))
            {
                ps.setString(1, movie.getTitle());
                ps.setFloat(2, movie.getImdbRating());
                ps.setFloat(3, movie.getPersonalRating());
                ps.setString(4, movie.getFilePath());

                if (movie.getLastView() != null)
                    ps.setTimestamp(5, Timestamp.valueOf(movie.getLastView().atStartOfDay()));
                else
                    ps.setNull(5, Types.TIMESTAMP);

                ps.setInt(6, movie.getId());
                ps.executeUpdate();
            }
        }

        // DELETE
        public void deleteMovie(Movie movie) throws SQLException
        {
            String sql = "DELETE FROM Movie WHERE id = ?";

            try (Connection conn = DBConnector.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql))
            {
                ps.setInt(1, movie.getId());
                ps.executeUpdate();
            }
        }

        // MAPPER (MEGET VIGTIG)
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
