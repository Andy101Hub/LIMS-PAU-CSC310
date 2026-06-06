package lims.models;

public class TestRequest {

    private int requestId;
    private String customerName;
    private String customerEmail;
    private String testName;
    private String paymentStatus;
    private String requestStatus;
    private String requestedAt;
    private int turnaroundTimeHours;
    private String countdown;

    // NEW constructor for countdown feature
    public TestRequest(int requestId, String customerName, String customerEmail,
                       String testName, String paymentStatus, String requestStatus,
                       String requestedAt, int turnaroundTimeHours, String countdown) {

        this.requestId = requestId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.testName = testName;
        this.paymentStatus = paymentStatus;
        this.requestStatus = requestStatus;
        this.requestedAt = requestedAt;
        this.turnaroundTimeHours = turnaroundTimeHours;
        this.countdown = countdown;
    }

    // OLD constructor kept for compatibility with other controllers
    public TestRequest(int requestId, String customerName, String customerEmail,
                       String testName, String paymentStatus, String requestStatus,
                       String requestedAt) {

        this.requestId = requestId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.testName = testName;
        this.paymentStatus = paymentStatus;
        this.requestStatus = requestStatus;
        this.requestedAt = requestedAt;

        this.turnaroundTimeHours = 0;
        this.countdown = "";
    }

    public int getRequestId() {
        return requestId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getTestName() {
        return testName;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public String getRequestedAt() {
        return requestedAt;
    }

    public int getTurnaroundTimeHours() {
        return turnaroundTimeHours;
    }

    public String getCountdown() {
        return countdown;
    }
}