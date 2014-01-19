package com.buddycloud.fragments.contacts;

import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;

import com.buddycloud.model.ModelCallback;

public class DeviceContactMatcher implements ContactMatcher {

	private static final String NAME = "device";
	
	private static final String PHONE = "phone";
	private static final String EMAIL = "email";
	private static final int PHONE_LENGTH = 7;
	
	@Override
	public void match(Activity activity, ModelCallback<JSONArray> callback) {
		ContentResolver contentResolver = activity.getContentResolver();
		JSONArray myHashes = getMyHashes(contentResolver);
		JSONArray friendHashes = getOtherHashes(contentResolver);
		ContactMatcherUtils.reportToFriendFinder(activity, callback, friendHashes, myHashes);
	}

	protected JSONArray getOtherHashes(ContentResolver contentResolver) {
		JSONArray hashes = new JSONArray();
		List<String> phones = getOtherPhones(contentResolver);
		for (String phone : phones) {
			hashes.put(ContactMatcherUtils.hash(PHONE, normalizePhone(phone)));
		}
		List<String> emailAddresses = getOtherEmailAddresses(contentResolver);
		for (String emailAddress : emailAddresses) {
			hashes.put(ContactMatcherUtils.hash(EMAIL, emailAddress));
		}
		return hashes;
	}
	
	@SuppressLint("NewApi")
	private JSONArray getMyHashes(ContentResolver contentResolver) {
		JSONArray hashes = new JSONArray();
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return hashes;
	    }
		
		Cursor cursor = contentResolver.query(
				 Uri.withAppendedPath(
	                        ContactsContract.Profile.CONTENT_URI,
	                        Contacts.Data.CONTENT_DIRECTORY), 
	                        null, null, null, null);
		while (cursor.moveToNext()) {
			String mimetype = getString(cursor, Contacts.Data.MIMETYPE);
			if (mimetype != null) {
				if (mimetype.equals(Email.CONTENT_ITEM_TYPE)) {
					String data = getString(cursor, Email.DATA);
					hashes.put(ContactMatcherUtils.hash(EMAIL, data));
				} else if (mimetype.equals(Phone.CONTENT_ITEM_TYPE)) {
					String data = getString(cursor, Phone.NUMBER);
					hashes.put(ContactMatcherUtils.hash(PHONE, normalizePhone(data)));
				}
			}
		}
		cursor.close();
		return hashes;
	}
	
	private String normalizePhone(String phone) {
		String numericPhone = phone.replaceAll("[^\\d]", "");
		return numericPhone.substring(Math.max(0, numericPhone.length() - PHONE_LENGTH));
	}

	private List<String> getContactData(ContentResolver contentResolver, Uri contentUri, String field) {
		List<String> contactData = new LinkedList<String>();
		Cursor cursor = contentResolver.query(contentUri, null,
				null, null, null);
		while (cursor.moveToNext()) {
			String dataEntry = getString(cursor, field);
			if (dataEntry != null) {
				contactData.add(dataEntry);
			}
		}
		cursor.close();
		return contactData;
	}
	
	private List<String> getOtherEmailAddresses(ContentResolver contentResolver) {
		return getContactData(contentResolver, Email.CONTENT_URI, Email.DATA);
	}

	private List<String> getOtherPhones(ContentResolver contentResolver) {
		return getContactData(contentResolver, Phone.CONTENT_URI, Phone.NUMBER);
	}

	protected String getString(Cursor cursor, String field) {
		return cursor.getString(cursor.getColumnIndex(field));
	}
	
	protected int getInt(Cursor cursor, String field) {
		return cursor.getInt(cursor.getColumnIndex(field));
	}

	@Override
	public String getName() {
		return NAME;
	}

}
