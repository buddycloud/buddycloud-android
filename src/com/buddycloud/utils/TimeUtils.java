package com.buddycloud.utils;

import java.text.ParseException;
import java.util.Date;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONObject;

public class TimeUtils {
	
	private static final DateTimeFormatter ISO_8601_PARSER = ISODateTimeFormat.dateTimeParser();
	public static final String OLDEST_DATE = "1970-01-01T00:00:00.000Z";
	
	public static Date fromISOToDate(String isoDate) throws ParseException {
		return ISO_8601_PARSER.parseDateTime(isoDate).toDate();
	}
	
	public static Date updated(JSONObject post) throws ParseException {
		return fromISOToDate(post.optString("updated"));
	}
}
