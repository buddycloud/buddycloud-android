package com.buddycloud.fragments.adapter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.ActionMode.Callback;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.buddycloud.R;
import com.buddycloud.fragments.GenericSelectableChannelsFragment;

public abstract class SelectableChannelAdapter extends GenericChannelAdapter {

	private static final int CHILD_PADDING = 4;
	
	private Map<String, JSONObject> selection = new HashMap<String, JSONObject>();
	private ActionMode mActionMode;
	private GenericSelectableChannelsFragment fragment;
	private View parentView;

	@Override
	public void configure(GenericSelectableChannelsFragment fragment, View view) {
		this.fragment = fragment;
		this.parentView = view;
	}
	
	protected void hideProgress() {
		parentView.findViewById(R.id.channelListProgress).setVisibility(View.GONE);
	}

	protected void showProgress() {
		parentView.findViewById(R.id.channelListProgress).setVisibility(View.VISIBLE);
	}
	
	private Callback createActionModeCallback(final ViewGroup viewGroup) {
		return new ActionMode.Callback() {

			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				SelectableChannelAdapter.this.onCreateActionMode(mode, menu);
				return true;
			}

			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
			}

			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				Context context = viewGroup.getContext();
				onActionMenuClicked(context, item);
				mode.finish();
				return true;
			}

			public void onDestroyActionMode(ActionMode mode) {
				for (int i = 0; i < viewGroup.getChildCount(); i++) {
					viewGroup.getChildAt(i).setSelected(false);
				}
				selection.clear();
				mActionMode = null;
			}
		};
	}
	

	@Override
	public View getChildView(final int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, final ViewGroup viewGroup) {
		final View childView = super.getChildView(groupPosition, childPosition, isLastChild,
						convertView, viewGroup);
		
		if (!canSelect()) {
			return childView;
		}
		
		childView.setBackgroundResource(R.drawable.channel_item_background_selector);
		childView.setPadding(CHILD_PADDING, CHILD_PADDING, 
				CHILD_PADDING, CHILD_PADDING);
		childView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View arg0) {
				
				JSONObject channelItem = getChild(groupPosition, childPosition);
				
				if (mActionMode == null) {
					Callback actionModeCallback = createActionModeCallback(viewGroup);
	            	mActionMode = fragment.getSherlockActivity().startActionMode(actionModeCallback);
	            }
	            
				String jid = channelItem.optString("jid");
	            boolean mustSelect = !selection.containsKey(jid);
	            if (mustSelect) {
	            	selection.put(jid, channelItem);
	            } else {
	            	selection.remove(jid);
	            	if (selection.isEmpty()) {
	            		mActionMode.finish();
	            	}
	            }
	            childView.setSelected(mustSelect);
	            return true;
			}
		});
		childView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				JSONObject channelItem = getChild(groupPosition, childPosition);
				fragment.selectChannel(channelItem.optString("jid"));
			}
		});
		
		return childView;
	}
	
	protected Collection<JSONObject> getSelection() {
		return selection.values();
	}
	
	protected void finishActionMode() {
		if (mActionMode != null) {
			mActionMode.finish();
		}
	}

	protected abstract boolean canSelect();

	protected abstract void onCreateActionMode(ActionMode mode, Menu menu);
	
	protected abstract void onActionMenuClicked(Context context, MenuItem item);
	
}
