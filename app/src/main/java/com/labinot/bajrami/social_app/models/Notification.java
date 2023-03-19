package com.labinot.bajrami.social_app.models;

public class Notification {

    public static final String USERID = "userid";
    public static final String TEXT = "text";
    public static final String POSTID = "postid";
    public static final String IS_POST = "is_post";

    private String userid;
    private String text;
    private String postid;
    private boolean is_post;

    public Notification() {
    }

    public Notification(String userid, String text, String postid, boolean is_post) {
        this.userid = userid;
        this.text = text;
        this.postid = postid;
        this.is_post = is_post;
    }

    public String getUserid() {
        return userid;
    }

    public String getText() {
        return text;
    }

    public String getPostid() {
        return postid;
    }

    public boolean isIs_post() {
        return is_post;
    }
}
