package com.buddycloud.fragments.contacts;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.buddycloud.model.ContactMatchingModel;
import com.buddycloud.model.ModelCallback;

public class ContactMatcherUtils {

	public static String hash(String provider, String id) {
		return new String(Hex.encodeHex(DigestUtils.sha256(provider + ":" + id)));
	}

	public static void reportToFriendFinder(final Context context,
			final ModelCallback<JSONArray> callback, JSONArray friendsHashes,
			JSONArray myHashes) {
		JSONObject contactMatchingReq = new JSONObject();
		try {
			contactMatchingReq.put("mine", myHashes);
			contactMatchingReq.put("others", friendsHashes);
		} catch (JSONException e) {}
		
		ContactMatchingModel.getInstance().save(context, contactMatchingReq, 
				new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
				JSONArray matchedJids = response.optJSONArray("items");
				callback.success(matchedJids);
			}
			
			@Override
			public void error(Throwable throwable) {
				callback.error(throwable);
			}
		});
	}
	
}
