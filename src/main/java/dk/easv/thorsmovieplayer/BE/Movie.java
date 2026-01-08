package dk.easv.thorsmovieplayer.BE;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Movie {

    private int id;
    private String title;
    private float imdbRating;
    private float personalRating;
    private String filePath;
    private LocalDate lastView;

    // konstruktør til at hente vores film fra sql databasen
    public Movie(int id, String title, float imdbRating, float personalRating, String filePath, LocalDateTime lastView) {
        this.id = id;
        this.title = title;
        this.imdbRating = imdbRating;
        this.personalRating = personalRating;
        this.filePath = filePath;
        this.lastView = lastView;
    }
    // konstruktør til at lave nye film
    public Movie(String title, float imdbRating, String filePath) {
        this.title = title;
        this.imdbRating = imdbRating;
        this.filePath = filePath;
        //Opmærksom den er ikke i konstruktør parameteren
        this.personalRating = Float.parseFloat(null);
        this.lastView = null;
    }

     // getter metoder
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public float getImdbRating() {
        return imdbRating;
    }
    public float getPersonalRating(){
        return personalRating;
    }

    public String getFilePath() {
        return filePath;
    }
    public LocalDate getLastView() {
        return lastView;
    }

    // setter metoderne kan tilføjes flere senere
    public void setId(int Id){this.id = Id;}

    public void setPersonalRating(Integer personalRating){
        this.personalRating = personalRating;
    }
    public void setLastView(LocalDate lastView){
        this.lastView = lastView;
    }
    // til movie Objekt når det printes i listview
    @Override
    public String toString() {
        return title + " (" + imdbRating + ")";
    }

}
