package model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Represents a book in the system.
 * Uses BigDecimal for price to avoid floating-point precision issues.
 */
public class Book {
    private int bookId;
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private Date publicationDate;
    private BigDecimal price;
    private int stockLevel;
    private String category;
    private String description;
    private double rating;
    private int reviewCount;
    private String imageUrl;
    
    public Book() {}
    
    public Book(int bookId, String title, String author, BigDecimal price, 
                String category, double rating, int reviewCount) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.price = price;
        this.category = category;
        this.rating = rating;
        this.reviewCount = reviewCount;
    }
    
    /**
     * Update the book price with validation.
     */
    public void updatePrice(BigDecimal newPrice) {
        if (newPrice != null && newPrice.compareTo(BigDecimal.ZERO) > 0) {
            this.price = newPrice;
        }
    }
    
    /**
     * Update stock quantity with validation.
     */
    public void updateStockQuantity(int quantity) {
        if (quantity >= 0) {
            this.stockLevel = quantity;
        }
    }
    
    /**
     * Check if book is in stock.
     */
    public boolean isInStock() {
        return stockLevel > 0;
    }
    
    // Getters and Setters
    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }
    
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    
    public String getPublisher() { return publisher; }
    public void setPublisher(String publisher) { this.publisher = publisher; }
    
    public Date getPublicationDate() { return publicationDate; }
    public void setPublicationDate(Date publicationDate) { this.publicationDate = publicationDate; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public int getStockLevel() { return stockLevel; }
    public void setStockLevel(int stockLevel) { this.stockLevel = stockLevel; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    
    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + bookId +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", price=" + price +
                ", inStock=" + isInStock() +
                '}';
    }
}