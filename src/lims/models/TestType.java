/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package lims.models;

public class TestType {

    private int testTypeId;
    private String testName;
    private String category;
    private double price;
    private int turnaroundTimeHours;
    private String resultFormat;

    public TestType() {
    }

    public TestType(int testTypeId, String testName, String category, double price,
                    int turnaroundTimeHours, String resultFormat) {
        this.testTypeId = testTypeId;
        this.testName = testName;
        this.category = category;
        this.price = price;
        this.turnaroundTimeHours = turnaroundTimeHours;
        this.resultFormat = resultFormat;
    }

    public int getTestTypeId() {
        return testTypeId;
    }

    public String getTestName() {
        return testName;
    }

    public String getCategory() {
        return category;
    }

    public double getPrice() {
        return price;
    }

    public int getTurnaroundTimeHours() {
        return turnaroundTimeHours;
    }

    public String getResultFormat() {
        return resultFormat;
    }
}