package com.buddycloud.card;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.buddycloud.R;
import com.buddycloud.image.SmartImageView;
import com.fima.cardsui.objects.Card;

public class PostCard extends Card {
	
	private static final DateFormat ISO_8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
	
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
		
		try {
			long publishedTime = ISO_8601.parse(published).getTime();
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
