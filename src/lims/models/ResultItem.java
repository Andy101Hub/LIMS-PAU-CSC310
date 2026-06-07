package lims.models;

import java.io.File;

public class ResultItem {

    private String testName;
    private String status;
    private String pdfReport;
    private String medicalImage;

    private String pdfReportPath;
    private String medicalImagePath;

    public ResultItem(String testName,
                      String status,
                      String pdfReportPath,
                      String medicalImagePath) {
        this.testName = testName;
        this.status = status;
        this.pdfReportPath = pdfReportPath;
        this.medicalImagePath = medicalImagePath;

        this.pdfReport = buildDisplayName(pdfReportPath, "No PDF");
        this.medicalImage = buildDisplayName(medicalImagePath, "No Image");
    }

    private String buildDisplayName(String path, String defaultText) {
        if (path == null || path.trim().isEmpty()) {
            return defaultText;
        }

        File file = new File(path);
        return file.getName();
    }

    public String getTestName() {
        return testName;
    }

    public String getStatus() {
        return status;
    }

    public String getPdfReport() {
        return pdfReport;
    }

    public String getMedicalImage() {
        return medicalImage;
    }

    public String getPdfReportPath() {
        return pdfReportPath;
    }

    public String getMedicalImagePath() {
        return medicalImagePath;
    }
}