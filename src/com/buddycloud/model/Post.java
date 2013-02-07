package com.buddycloud.model;

import java.io.Serializable;
import java.util.Date;

public class Post implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8259871828759927592L;
	
	private final String channelJid;
	private final String id;
	private String authorJid;
	private Date published;
	private String inReplyTo;
	private String content;

	private String avatarURL;

	public Post(String channelJid, String id) {
		this.channelJid = channelJid;
		this.id = id;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}

	public String getChannelJid() {
		return channelJid;
	}
	
	public String getId() {
		return id;
	}
	
	public String getAuthorJid() {
		return authorJid;
	}
	
	public void setAuthorJid(String authorJid) {
		this.authorJid = authorJid;
	}
	
	public Date getPublished() {
		return published;
	}
	
	public void setPublished(Date published) {
		this.published = published;
	}
	
	public String getInReplyTo() {
		return inReplyTo;
	}
	
	public void setInReplyTo(String inReplyTo) {
		this.inReplyTo = inReplyTo;
	}

	public void setAuthorAvatarURL(String avatarURL) {
		this.avatarURL = avatarURL;
	}

	public String getAuthorAvatarURL() {
		return avatarURL;
	}
}
