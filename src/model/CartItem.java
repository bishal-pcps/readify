package model;

public class CartItem {
    private int cartItemId;
    private int cartId;
    private int bookId;
    private String bookName;
    private int quantity;
    private double price;

    public CartItem() {
    }

    public CartItem(int cartId, int bookId, int quantity, double price) {
        this.cartId = cartId;
        this.bookId = bookId;
        this.quantity = quantity;
        this.price = price;
    }

    public int getCartItemId() { return cartItemId; }
    public void setCartItemId(int cartItemId) { this.cartItemId = cartItemId; }

    public int getCartId() { return cartId; }
    public void setCartId(int cartId) { this.cartId = cartId; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getBookName() { return bookName; }
    public void setBookName(String bookName) { this.bookName = bookName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
