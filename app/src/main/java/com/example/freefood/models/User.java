package com.example.freefood.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseUser;

@ParseClassName("_User")
public class User extends ParseUser {
    public static final String KEY_NAME = "name";
    public static final String KEY_PROFILE_IMAGE = "profileImage";

    public User() { super(); }

    public String getName() { return getString(KEY_NAME); }
    public void setName(String name) { put(KEY_NAME, name); }

    public ParseFile getProfileImage() {
        return getParseFile(KEY_PROFILE_IMAGE); }
    public void setProfileImage(ParseFile parseFile) { put(KEY_PROFILE_IMAGE, parseFile); }
}
