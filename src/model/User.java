package model;

/**
 * Abstract base class representing a user in the system.
 * Note: Passwords are never stored in memory - only authenticated users keep their session.
 */
public abstract class User {
    protected int userId;
    protected String email;
    protected String firstName;
    protected String lastName;
    protected String phone;
    protected Role role;
    
    public User() {}
    
    public User(int userId, String email, String firstName, 
                String lastName, Role role) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }
    
    // Getters and Setters
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getFullName() { return firstName + " " + lastName; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    
    /**
     * For backward compatibility - convert Role enum to string
     */
    public String getRoleString() {
        return role != null ? role.name() : "CUSTOMER";
    }
}