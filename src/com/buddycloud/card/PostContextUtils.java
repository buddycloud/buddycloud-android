package com.buddycloud.card;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.buddycloud.R;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.PostsModel;
import com.buddycloud.model.SubscribedChannelsModel;


public class PostContextUtils {

	private static final String CONTEXT_DELETE = "Delete";
	private static final String CONTEXT_SHARE = "Share";
	
	public static void showPostContextActions(final Context context, final String channelJid, 
			final String postId, final CardListAdapter cardAdapter, String role) {
		
		final List<String> contextItems = new ArrayList<String>();
		if (SubscribedChannelsModel.canDelete(role)) {
			contextItems.add(CONTEXT_DELETE);
		}
		contextItems.add(CONTEXT_SHARE);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Post actions");
		builder.setItems(contextItems.toArray(new String[]{}), 
				new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		        if (contextItems.get(item).equals(CONTEXT_DELETE)) {
		        	confirmDelete(context, channelJid, postId, cardAdapter);
		        }
		    }
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private static void confirmDelete(final Context context, final String channelJid, 
			final String postId, final CardListAdapter cardAdapter) {
		new AlertDialog.Builder(context)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setTitle("Confirm deletion")
				.setMessage("Are you sure you want to delete this post?")
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								delete(context, channelJid, postId, cardAdapter);
							}
						}).setNegativeButton(R.string.no, null).show();
	}
	
	private static void delete(final Context context, String channelJid, 
			final String postId, final CardListAdapter cardAdapter) {
		PostsModel.getInstance().delete(context, new ModelCallback<Void>() {
			@Override
			public void success(Void response) {
				cardAdapter.remove(postId);
				Toast.makeText(context, "Post deleted",
						Toast.LENGTH_LONG).show();
			}

			@Override
			public void error(Throwable throwable) {
				Toast.makeText(context, "Could not delete post.",
						Toast.LENGTH_LONG).show();
			}
		}, channelJid, postId);
	}

}
