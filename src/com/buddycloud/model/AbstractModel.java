package com.buddycloud.model;

import java.util.LinkedList;
import java.util.List;

public abstract class AbstractModel<RefreshType, SaveType, Params> implements Model<RefreshType, SaveType, Params> {

	private List<ModelListener> listeners = new LinkedList<ModelListener>();
	
	@Override
	public void addListener(ModelListener listener) {
		listeners.add(listener);
	}
	
	protected void notifyChanged() {
		for (ModelListener listener : listeners) {
			listener.dataChanged();
		}
	}
	
}
