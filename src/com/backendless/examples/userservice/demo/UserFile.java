package com.backendless.examples.userservice.demo;

/**
 * Created by happy on 16.03.2016.
 */
public class UserFile {

    private String url;

    private String email;

    private String name;

    public UserFile() {
    }

    public UserFile(String url, String email, String name) {
        this.url = url;
        this.email = email;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        return "UserFile{" +
                "url=" + url +
                ", email='" + email + '\'' +
                '}';
    }
}