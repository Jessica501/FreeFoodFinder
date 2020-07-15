package com.example.freefood;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Utils {

    // converts the contains JSONObject to a String
    protected static String containsJsontoString(JSONObject jsonObject) throws JSONException {
        if (jsonObject == null) {
            return "NO MAJOR ALLERGENS";
        }
        List<String> contains = new ArrayList<>();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (jsonObject.getBoolean(key)) {
                contains.add(key);
            }
        }
        if (contains.size() == 0) {
            return "NO MAJOR ALLERGENS";
        } else {
            return "CONTAINS: " + String.join(", ", contains).toUpperCase();
        }
    }
}
