package com.buddycloud.model;

import android.graphics.Bitmap;

public class Channel {

	private String jid;
	private String description;
	private Bitmap avatar;
	private int unread;
	
	public Channel(String jid) {
		this.jid = jid;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getJid() {
		return jid;
	}

	public void setAvatar(Bitmap avatar) {
		this.avatar = avatar;
	}
	
	public Bitmap getAvatar() {
		return avatar;
	}
	
	@Override
	public boolean equals(Object o) {
		return this.getJid().equals(((Channel)o).getJid());
	}
	
	@Override
	public int hashCode() {
		return getJid().hashCode();
	}

	public void setUnread(Integer unread) {
		this.unread = unread;
	}
	
	public Integer getUnread() {
		return unread;
	}
}
