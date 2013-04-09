package com.buddycloud.model.db.bean;

import java.security.InvalidParameterException;

import org.json.JSONException;
import org.json.JSONObject;

public class UnreadCounter {
	
	private String channel;
	private int mentionsCount;
	private int totalCount;
	
	
	public UnreadCounter(String channel, JSONObject unreadCounter) {
		this.channel = channel;
		try {
			this.mentionsCount = unreadCounter.getInt("mentionsCount");
			this.totalCount = unreadCounter.getInt("totalCount");
		} catch (JSONException e) {
			throw new InvalidParameterException("JSONObject must contains mentionsCount and totalCount fields.");
		}
	}
	
	public UnreadCounter(String channel, int mentionsCount, int totalCount) {
		this.channel = channel;
		this.mentionsCount = mentionsCount;
		this.totalCount = totalCount;
	}
	
	
	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public int getMentionsCount() {
		return mentionsCount;
	}

	public void setMentionsCount(int mentionsCount) {
		this.mentionsCount = mentionsCount;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
}
