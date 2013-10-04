package com.buddycloud.model;

import org.json.JSONObject;


public abstract class AbstractModel<RefreshType, SaveType, Params> implements Model<RefreshType, SaveType, Params> {

	private ModelListener listener;
	
	@Override
	public void setListener(ModelListener listener) {
		this.listener = listener;
	}
	
	protected void notifyChanged() {
		if (listener != null) {
			listener.dataChanged();
		}
	}
	
	protected void notifyDeleted(String channelJid, String itemId, String parentId) {
		if (listener != null) {
			listener.itemRemoved(channelJid, itemId, parentId);
		}
	}
	
	protected void notifyAdded(String channelJid, JSONObject pendingItem) {
		if (listener != null) {
			listener.pendingItemAdded(channelJid, pendingItem);
		}
	}
}
