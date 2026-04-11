package model;

public class Customer extends User {
    private int loyaltyPoints;

    public Customer() {
        super();
        this.loyaltyPoints = 0;
    }

    public Customer(String firstName, String lastName, String email, String phoneNumber,
                    String addressLine, String city, String state, String zipCode,
                    String country, String passwordHash) {
        super(firstName, lastName, email, phoneNumber, addressLine, city, state, zipCode, country, passwordHash, 2);
        this.loyaltyPoints = 0;
    }

    @Override
	public int getLoyaltyPoints() { return loyaltyPoints; }
    @Override
	public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }

    public void addLoyaltyPoints(int points) {
        this.loyaltyPoints += points;
    }
}
