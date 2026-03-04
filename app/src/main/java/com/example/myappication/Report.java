package com.example.myappication;

public class Report {

    private String title;
    private String preview;       // Short preview text
    private String fullContent;   // Complete report content
    private String date;          // Report date
    private String currentValueBaseline;
    public Report(String title, String preview, String fullContent, String date) {
        this.title = title;
        this.preview = preview;
        this.fullContent = fullContent;
        this.date = date;
    }

    public String getTitle() { return title; }
    public String getPreview() { return preview; }
    public String getFullContent() { return fullContent; }
    public String getDate() { return date; }
}
