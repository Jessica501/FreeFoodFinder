package com.example.freefood.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONObject;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String KEY_AUTHOR = "author";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_TITLE = "title";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_CONTAINS = "contains";
    public static final String KEY_CLAIMED = "claimed";
    
    public Post() { super(); }

    public void setAuthor(ParseUser user) { put(KEY_AUTHOR, user); }
    public ParseUser getAuthor() { return getParseUser("author"); }

    public void setImage(ParseFile file) { put(KEY_IMAGE, file); }
    public ParseFile getImage() { return getParseFile(KEY_IMAGE); }

    public void setTitle(String title) { put(KEY_TITLE, title); }
    public String getTitle() { return getString(KEY_TITLE); }

    public void setLocation(ParseGeoPoint geoPoint) { put(KEY_LOCATION, geoPoint); }
    public ParseGeoPoint getLocation() { return getParseGeoPoint(KEY_LOCATION); }

    public void setDescription(String description) { put(KEY_DESCRIPTION, description); }
    public String getDescription() { return getString(KEY_DESCRIPTION); }

    public void setContains(JSONObject jsonObject) { put(KEY_CONTAINS, jsonObject); }
    public JSONObject getContains() { return getJSONObject(KEY_CONTAINS); }

    public void setClaimed(boolean claimed) { put(KEY_CLAIMED, claimed); }
    public boolean getClaimed() { return getBoolean(KEY_CLAIMED); }
}
