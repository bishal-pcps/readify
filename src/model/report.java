package model;

import java.util.Date;
import java.util.Map;

/**
 * Represents a system report.
 * Can be generated with various formats and data types.
 */
public class Report {
    private int reportId;
    private String type;
    private Date generatedDate;
    private Map<String, Object> data;
    private String format;
    
    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public Date getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(Date generatedDate) { this.generatedDate = generatedDate; }
    
    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
    
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
}