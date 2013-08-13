package com.buddycloud.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONObject;

public class TimeUtils {
	private TimeUtils() {}
	
	public static final DateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
	public static final String OLDEST_DATE = "1970-01-01T00:00:00.000Z";
	static {
		ISO_8601.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	public static Date fromISOToDate(String isoDate) throws ParseException {
		return ISO_8601.parse(isoDate);
	}
	
	public static Date updated(JSONObject post) throws ParseException {
		return fromISOToDate(post.optString("updated"));
	}
}
