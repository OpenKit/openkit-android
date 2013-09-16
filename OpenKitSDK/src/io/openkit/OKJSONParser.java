package io.openkit;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

public class OKJSONParser {
	public static int safeParseInt(String key, JSONObject json)
	{
		try {
			return json.getInt(key);
		}
		catch(JSONException e) {
			return 0;
		}
	}

	public static long safeParseLong(String key, JSONObject json)
	{
		try {
			return json.getLong(key);
		}
		catch(JSONException e) {
			return 0;
		}
	}

	// Android JSON parsing returns the string "null" for null strings instead of
	// return null, so we check this using json.isNull and TextUtils.isEmpty for empty strings
	// and return null if it's empty or null
	public static String safeParseString(String key, JSONObject json)
	{
		try {
			if(json.isNull(key) || TextUtils.isEmpty(json.getString(key))) {
				return null;
			} else {
				return json.getString(key);
			}
		}
		catch(JSONException e) {
			return null;
		}
	}

	public static boolean safeParseBoolean(String key, JSONObject json)
	{
		try {
			return json.getBoolean(key);
		}
		catch(JSONException e) {
			return false;
		}
	}

	public static boolean isZeroStringLiteral(String s)
	{
		if (s == null) {
			return false;
		} else if (s.equalsIgnoreCase("0")) {
			return true;
		} else {
			try {
				long retValLong = Long.parseLong(s);
				return (retValLong == 0);
			} catch (Exception ex) {
				return false;
			}
		}
	}

}
