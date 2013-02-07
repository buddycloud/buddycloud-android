package com.buddycloud.model;

import java.io.Serializable;

public class Channel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1396459184012348432L;
	
	private String jid;
	private String description;
	private int unread;
	private String avatarURL;
	
	public Channel(String jid) {
		this.jid = jid;
	}
	
	public void setAvatarURL(String avatarURL) {
		this.avatarURL = avatarURL;
	}
	
	public String getAvatarURL() {
		return avatarURL;
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
