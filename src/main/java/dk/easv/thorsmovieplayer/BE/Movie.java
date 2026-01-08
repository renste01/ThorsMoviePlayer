package dk.easv.thorsmovieplayer.BE;

import java.sql.Date;
import java.time.LocalDateTime;

public class Movie
{
    private int id;
    private String title;
    private String filePath;
    private LocalDateTime lastView;
    private float personalRating;
    private float imdbRating;

    public Movie(int id, String title, String filePath, LocalDateTime lastView, float personalRating, float imdbRating)
    {
        this.id = id;
        this.title = title;
        this.filePath = filePath;
        this.lastView = lastView;
        this.personalRating = personalRating;
        this.imdbRating = imdbRating;
    }

    // Getters
    public int getId() {return id;}

    public String getTitle() {return title;}

    public String getFilePath() {return filePath;}

    public LocalDateTime getLastView() {return lastView;}

    public float getPersonalRating() {return personalRating;}

    public float getImdbRating() {return imdbRating;}

    // Setters
    public void setId(int id) {this.id = id;}

    public void setTitle(String title) {this.title = title;}

    public void setFilepath(String filePath) {this.filePath = filePath;}

    public void setLastView(LocalDateTime lastView) {this.lastView = lastView;}

    public void setPersonalRating(float personalRating) {this.personalRating = personalRating;}

    public void setImdbRating(float imdbRating) {this.imdbRating = imdbRating;}
}
