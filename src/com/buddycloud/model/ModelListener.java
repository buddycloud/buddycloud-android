package com.buddycloud.model;

import org.json.JSONObject;

public interface ModelListener {

	void dataChanged();
	
	void itemRemoved(String channelJid, String itemId, String parentId);

	void pendingItemAdded(String channelJid, JSONObject pendingItem);
	
}
