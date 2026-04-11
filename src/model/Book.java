package model;

import java.sql.Date;

public class Book {
    private int bookId;
    private String bookName;
    private int authorId;
    private int categoryId;
    private int publisherId;
    private Date publicationDate;
    private String isbn;
    private String description;
    private double price;
    private String coverImage;
    private int stockLevel;

    public Book() {
    }

    public Book(String bookName, int authorId, int categoryId, int publisherId,
                Date publicationDate, String isbn, String description, double price,
                String coverImage, int stockLevel) {
        this.bookName = bookName;
        this.authorId = authorId;
        this.categoryId = categoryId;
        this.publisherId = publisherId;
        this.publicationDate = publicationDate;
        this.isbn = isbn;
        this.description = description;
        this.price = price;
        this.coverImage = coverImage;
        this.stockLevel = stockLevel;
    }

    // Getters and Setters
    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }

    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public int getPublisherId() { return publisherId; }
    public void setPublisherId(int publisherId) { this.publisherId = publisherId; }

    public Date getPublicationDate() { return publicationDate; }
    public void setPublicationDate(Date publicationDate) { this.publicationDate = publicationDate; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public int getStockLevel() { return stockLevel; }
    public void setStockLevel(int stockLevel) { this.stockLevel = stockLevel; }
}
