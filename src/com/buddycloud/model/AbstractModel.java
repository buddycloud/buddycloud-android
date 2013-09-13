package com.buddycloud.model;


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
	
}
