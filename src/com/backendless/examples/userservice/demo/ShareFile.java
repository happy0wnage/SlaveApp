package com.backendless.examples.userservice.demo;

/**
 * Created by happy on 16.03.2016.
 */
public class ShareFile {

    private String url;

    private String email;

    private String fileName;

    public ShareFile() {
    }

    public ShareFile(String url, String email, String fileName) {
        this.url = url;
        this.email = email;
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "ShareFile{" +
                "url=" + url +
                ", email='" + email + '\'' +
                '}';
    }
}