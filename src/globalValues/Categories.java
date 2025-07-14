package globalValues;

public class Categories {
    private String id;
    private String name;
    private String description;
    private boolean discontinued;

    public Categories() {
    }

    public Categories(String id, String name, String description, boolean discontinued) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.discontinued = discontinued;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getDiscontinued() {
        return discontinued;
    }

    public void setDiscontinued(boolean discontinued) {
        this.discontinued = discontinued;
    }
}
