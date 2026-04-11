package model;

public class Administrator extends User {
    private String adminName;
    private String role;

    public Administrator() {
        super();
    }

    public Administrator(String adminName, String email, String passwordHash, String role) {
        super();
        this.adminName = adminName;
        this.setEmail(email);
        this.setPasswordHash(passwordHash);
        this.role = role;
    }

    public String getAdminName() { return adminName; }
    public void setAdminName(String adminName) { this.adminName = adminName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String getEmail() { return super.getEmail(); }

    @Override
    public void setEmail(String email) { super.setEmail(email); }

    @Override
    public String getPasswordHash() { return super.getPasswordHash(); }

    @Override
    public void setPasswordHash(String passwordHash) { super.setPasswordHash(passwordHash); }
}
