package com.example.freefood.models;

import com.example.freefood.utils.Utils;
import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import org.json.JSONObject;

@ParseClassName("Post")
public class Post extends ParseObject implements Comparable<Post> {

    public static final String KEY_AUTHOR = "author";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_TITLE = "title";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_CONTAINS = "contains";
    public static final String KEY_CLAIMED = "claimed";
    public static final String KEY_COMMENTS = "comments";
    public static final String KEY_TAGS = "tags";
    
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

    public void setTags(JSONObject jsonObject) { put(KEY_TAGS, jsonObject); }
    public JSONObject getTags() { return getJSONObject(KEY_TAGS); }

    public void setClaimed(boolean claimed) { put(KEY_CLAIMED, claimed); }
    public boolean getClaimed() { return getBoolean(KEY_CLAIMED); }

    public String getClaimedTitle() {
        if (getClaimed()) {
            return "CLAIMED - " + getTitle();
        } else {
            return getTitle();
        }
    }

    public ParseRelation<Comment> getComments() { return getRelation(KEY_COMMENTS); }

    @Override
    public int compareTo(Post post) {
        double myRelativeDistance = Utils.getRelativeDistance(this);
        double otherRelativeDistance = Utils.getRelativeDistance(post);
        double difference = myRelativeDistance - otherRelativeDistance;
        if (difference < 0 ) {
            return -1;
        } else if (difference > 0 ) {
            return 1;
        } else {
            return 0;
        }
    }
}
