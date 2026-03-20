package model;

/**
 * Enum representing user roles in the system.
 * Provides type-safe role management and permission checking.
 */
public enum Role {
    ADMIN("Admin", true, true, true, true),
    CUSTOMER("Customer", false, true, false, false);
    
    private final String displayName;
    private final boolean canManageUsers;
    private final boolean canBrowseBooks;
    private final boolean canManageBooks;
    private final boolean canManageOrders;
    
    /**
     * Constructor for Role enum.
     */
    Role(String displayName, boolean canManageUsers, boolean canBrowseBooks, 
         boolean canManageBooks, boolean canManageOrders) {
        this.displayName = displayName;
        this.canManageUsers = canManageUsers;
        this.canBrowseBooks = canBrowseBooks;
        this.canManageBooks = canManageBooks;
        this.canManageOrders = canManageOrders;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public boolean canManageUsers() {
        return canManageUsers;
    }
    
    public boolean canBrowseBooks() {
        return canBrowseBooks;
    }
    
    public boolean canManageBooks() {
        return canManageBooks;
    }
    
    public boolean canManageOrders() {
        return canManageOrders;
    }
    
    /**
     * Convert string role to enum value.
     */
    public static Role fromString(String roleString) {
        if (roleString == null) {
            return CUSTOMER;
        }
        try {
            return Role.valueOf(roleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CUSTOMER;
        }
    }
}
