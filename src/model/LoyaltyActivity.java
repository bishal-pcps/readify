package model;

import java.sql.Timestamp;

public class LoyaltyActivity {
    private int activityId;
    private int userId;
    private int pointsChange;
    private String description;
    private Timestamp createdAt;

    public LoyaltyActivity() {
    }

    public LoyaltyActivity(int userId, int pointsChange, String description) {
        this.userId = userId;
        this.pointsChange = pointsChange;
        this.description = description;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getPointsChange() {
        return pointsChange;
    }

    public void setPointsChange(int pointsChange) {
        this.pointsChange = pointsChange;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
