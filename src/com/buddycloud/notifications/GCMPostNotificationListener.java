package com.buddycloud.notifications;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.buddycloud.GCMIntentService;
import com.buddycloud.MainActivity;
import com.buddycloud.R;
import com.buddycloud.fragments.GenericChannelsFragment;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.PostsModel;

public class GCMPostNotificationListener implements GCMNotificationListener {

	public static final String GCM_NOTIFICATION_POST_AUTHORS = "com.buddycloud.GCM_NOTIFICATION_POST_AUTHORS";
	private static final int NOTIFICATION_ID = 1001;
	
	@Override
	public void onMessage(GCMEvent event, Context context, Intent message) {

		String content = message.getStringExtra("CONTENT");
		String authorJid = message.getStringExtra("AUTHOR_JID");
		String channelJid = message.getStringExtra("CHANNEL_JID");
		
		if (content == null || authorJid == null || channelJid == null) {
			return;
		}
		
		GCMUtils.addGCMAuthor(context, authorJid);
		JSONArray gcmAuthors = GCMUtils.getGCMAuthors(context);
		
		NotificationCompat.Builder mBuilder = GCMUtils.createNotificationBuilder(context);
		
		boolean isAggregate = gcmAuthors.length() > 1;
		
		if (isAggregate) {
			
			Integer gcmAuthorCount = gcmAuthors.length();
			mBuilder.setContentTitle(context.getString(
					R.string.gcm_multi_notification_title, 
					gcmAuthorCount.toString()));
			
			Set<String> gcmAuthorsUnique = new HashSet<String>();
			for (int i = 0; i < gcmAuthors.length(); i++) {
				gcmAuthorsUnique.add(gcmAuthors.optString(i));
			}
			List<String> gcmAuthorsUniqueList = new ArrayList<String>(
					gcmAuthorsUnique);
			
			StringBuilder allAuthorsButLastBuilder = new StringBuilder();
			allAuthorsButLastBuilder.append(gcmAuthorsUniqueList.get(0));
			
			for (int i = 1; i < gcmAuthorsUniqueList.size() - 1; i++) {
				allAuthorsButLastBuilder.append(", ");
				String gcmAuthor = gcmAuthorsUniqueList.get(i);
				allAuthorsButLastBuilder.append(gcmAuthor);
			}
			
			String lastAuthor = gcmAuthorsUniqueList.get(
					gcmAuthorsUniqueList.size() - 1);
			
			mBuilder.setContentText(context.getString(R.string.gcm_multi_notification_content, 
					allAuthorsButLastBuilder.toString(), lastAuthor));
		} else {
			mBuilder.setContentTitle(context.getString(
					R.string.gcm_single_notification_title, authorJid));
			mBuilder.setContentText(content);
		}
		
		Intent resultIntent = new Intent(context, MainActivity.class);
		resultIntent.putExtra(GCMIntentService.GCM_NOTIFICATION_EVENT, event.toString());
		if (!isAggregate) {
			resultIntent.putExtra(GenericChannelsFragment.CHANNEL, channelJid);
		}
		
		Notification notification = GCMUtils.build(context, mBuilder, resultIntent);
		
		NotificationManager mNotificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(NOTIFICATION_ID, notification);
		
		PostsModel.getInstance().fill(context, new ModelCallback<Void>() {
			@Override
			public void success(Void response) {
				// Pretty much best effort here
			}

			@Override
			public void error(Throwable throwable) {
				// Pretty much best effort here
			}
		}, channelJid);
	}
	
}
