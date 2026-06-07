package lims.models;

public class PasswordResetRequest {

    private int resetRequestId;
    private int userId;
    private String email;
    private String role;
    private String status;
    private String requestedAt;

    public PasswordResetRequest(int resetRequestId,
                                int userId,
                                String email,
                                String role,
                                String status,
                                String requestedAt) {
        this.resetRequestId = resetRequestId;
        this.userId = userId;
        this.email = email;
        this.role = role;
        this.status = status;
        this.requestedAt = requestedAt;
    }

    public int getResetRequestId() {
        return resetRequestId;
    }

    public int getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public String getRequestedAt() {
        return requestedAt;
    }
}