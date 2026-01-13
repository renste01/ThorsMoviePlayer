package dk.easv.thorsmovieplayer.BE; //Patrick

public class Category {
    private int  id;
    private String name;

    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Category(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;

        Category other = (Category) o;

        if (id == 0 || other.id == 0) {
            return false; // ikke gemte kategorier er aldrig ens
        }
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return id == 0 ? super.hashCode() : Integer.hashCode(id);
    }
}
