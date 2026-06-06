/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lims.models;

public class ResultItem {

    private String testName;
    private String status;
    private String pdfReport;
    private String medicalImage;

    public ResultItem(String testName, String status, String pdfReport, String medicalImage) {
        this.testName = testName;
        this.status = status;
        this.pdfReport = pdfReport;
        this.medicalImage = medicalImage;
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
}