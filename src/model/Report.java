package model;

public class Report {
    private int reportId;
    private String reportName;
    private String reportType;
    private String description;
    private String generatedDate;

    public Report() {
    }

    public Report(String reportName, String reportType, String description, String generatedDate) {
        this.reportName = reportName;
        this.reportType = reportType;
        this.description = description;
        this.generatedDate = generatedDate;
    }

    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public String getReportName() { return reportName; }
    public void setReportName(String reportName) { this.reportName = reportName; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(String generatedDate) { this.generatedDate = generatedDate; }
}
