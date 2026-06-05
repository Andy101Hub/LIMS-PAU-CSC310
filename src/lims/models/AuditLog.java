package lims.models;

public class AuditLog {

    private int auditId;
    private String userName;
    private String userEmail;
    private String action;
    private String details;
    private String createdAt;

    public AuditLog(int auditId, String userName, String userEmail,
                    String action, String details, String createdAt) {
        this.auditId = auditId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.action = action;
        this.details = details;
        this.createdAt = createdAt;
    }

    public int getAuditId() {
        return auditId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}