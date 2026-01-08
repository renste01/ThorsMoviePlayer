package dk.easv.thorsmovieplayer.DAL;
import dk.easv.thorsmovieplayer.BE.Movie;


import java.util.List;
//movie dao er hvor vores metoder er til vores database(SQL)



public interface MovieDAO {

    //til at hente film
List <Movie> getAllMovies() throws Exception;

    // tilføj en nu film

    public abstract void addMovie(Movie movie) throws Exception;

    // slet film

    public abstract void deleteMovie(Movie movie) throws Exception;

   // opdatere fks rating på film
    public abstract void updateMovie(Movie movie) throws Exception;

    //henter film ud fra ID
    Movie getmMovie(int id) throws Exception;

}
