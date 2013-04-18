package com.buddycloud.card;

import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.buddycloud.R;
import com.buddycloud.image.SmartImageView;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.TimeUtils;
import com.fima.cardsui.objects.Card;

public class PostCard extends Card {
	
	public static final String MEDIA_PATTERN_SUFIX = "/\\S+@\\S+/media/\\w+";
	public static final String MEDIA_URL_SUFIX = "?maxheight=600";
	
	private String avatarURL;
	private String content;
	private Integer commentCount;
	private final String published;

	public PostCard(String title, String avatarURL, String content, 
			String published, Integer commentCount) {
		super(title);
		this.published = published;
		this.commentCount = commentCount;
		this.avatarURL = avatarURL;
		this.content = content;
	}

	
	@Override
	public View getCardContent(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.post_entry, null);

		((TextView) view.findViewById(R.id.title)).setText(title);
		
		((SmartImageView) view.findViewById(R.id.bcProfilePic)).setImageUrl(avatarURL, R.drawable.personal_50px);
		
		((TextView) view.findViewById(R.id.bcPostContent)).setText(content);
		
		((TextView) view.findViewById(R.id.bcCommentCount)).setText(commentCount.toString());
		
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		Pattern mediaPattern = Pattern.compile(apiAddress + MEDIA_PATTERN_SUFIX);
		Matcher matcher = mediaPattern.matcher(content);
		boolean found = matcher.find();
		
		if (found) {
			String mediaURL = content.substring(matcher.start(), matcher.end());
			SmartImageView mediaView = (SmartImageView) view.findViewById(R.id.bcImageContent);
			mediaView.setVisibility(View.VISIBLE);
			mediaView.setImageUrl(mediaURL + MEDIA_URL_SUFIX);
		}
		
		try {
			long publishedTime = TimeUtils.fromISOToDate(published).getTime();
			((TextView) view.findViewById(R.id.bcPostDate)).setText(
					DateUtils.getRelativeTimeSpanString(publishedTime, 
							new Date().getTime(), DateUtils.MINUTE_IN_MILLIS));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return view;
	}
	
	protected int getLastCardLayout() {
		return R.layout.item_card_empty_last_bc;
	}

}
