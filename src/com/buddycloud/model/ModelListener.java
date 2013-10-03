package com.buddycloud.model;

import org.json.JSONObject;

public interface ModelListener {

	void dataChanged();
	
	void itemRemoved(String itemId, String parentId);

	void pendingItemAdded(JSONObject pendingItem);
	
}
