package com.buddycloud.fragments.contacts;

import org.json.JSONArray;

import android.app.Activity;
import android.content.Context;

import com.buddycloud.model.ModelCallback;
import com.facebook.Request;
import com.facebook.Request.Callback;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

public class FacebookContactMatcher implements ContactMatcher {

	private static final String NAME = "facebook";
	
	@Override
	public void match(Activity activity, ModelCallback<JSONArray> callback) {
		authFB(activity, callback);
	}

	protected void authFB(final Activity activity, 
			final ModelCallback<JSONArray> callback) {
		Session.openActiveSession(activity, true,
				new Session.StatusCallback() {
			@Override
			public void call(Session session, SessionState state,
					Exception exception) {
				if (exception != null) {
					callback.error(exception);
					return;
				}
				if (session.isOpened()) {
					getFriends(activity, session, callback);
				}
			}
		});
	}

	private void getFriends(final Context context, final Session session, 
			final ModelCallback<JSONArray> callback) {
		Request.newMeRequest(session, new GraphUserCallback() {
			@Override
			public void onCompleted(final GraphUser user, Response response) {
				if (response.getError() != null) {
					callback.error(response.getError().getException());
					return;
				}
				getFriends(context, session, user, callback);
			}
		}).executeAsync();
	}
	
	protected void getFriends(final Context context, final Session session, 
			final GraphUser user, final ModelCallback<JSONArray> callback) {
		Request.newGraphPathRequest(session, "/me/friends", new Callback() {
			@Override
			public void onCompleted(Response response) {
				if (response.getError() != null) {
					callback.error(response.getError().getException());
					return;
				}
				matchContacts(context, user, response, callback);
			}
		}).executeAsync();
	}
	
	private void matchContacts(final Context context, final GraphUser user, 
			Response response, final ModelCallback<JSONArray> callback) {
		JSONArray friends = response.getGraphObject()
				.getInnerJSONObject().optJSONArray("data");
		JSONArray friendsHashes = new JSONArray();
		for (int i = 0; i < friends.length(); i++) {
			String friendId = friends.optString(i, "id");
			String friendHash = ContactMatcherUtils.hash(NAME, friendId);
			friendsHashes.put(friendHash);
		}
		
		JSONArray myHashes = new JSONArray();
		myHashes.put(user.getId());
		
		ContactMatcherUtils.reportToFriendFinder(context, callback, 
				friendsHashes, myHashes);
	}

	@Override
	public String getName() {
		return NAME;
	}
}
