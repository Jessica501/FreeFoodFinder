package com.example.freefood.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Comment")
public class Comment extends ParseObject {
    public static final String KEY_POST = "post";
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_DESCRIPTION = "description";

    public void setPost(Post post) { put(KEY_POST, post); }
    public Post getPost() { return (Post) getParseObject(KEY_POST); }

    public void setAuthor(ParseUser parseUser) { put(KEY_AUTHOR, parseUser); }
    public ParseUser getAuthor() { return getParseUser(KEY_AUTHOR); }

    public void setImage(ParseFile parseFile) { put(KEY_IMAGE, parseFile); }
    public ParseFile getImage() { return getParseFile(KEY_IMAGE); }

    public void setDescription(String description) { put(KEY_DESCRIPTION, description); }
    public String getDescrption() { return getString(KEY_DESCRIPTION); }
}
