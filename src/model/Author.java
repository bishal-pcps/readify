package model;

public class Author {
    private int authorId;
    private String authorName;
    private String biography;
    private String email;
    private String phoneNumber;
    private String country;

    public Author() {
    }

    public Author(String authorName, String biography, String email, String phoneNumber, String country) {
        this.authorName = authorName;
        this.biography = biography;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.country = country;
    }

    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = biography; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}
