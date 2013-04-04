package com.buddycloud.card;

import java.text.ParseException;
import java.util.Date;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.buddycloud.R;
import com.buddycloud.image.SmartImageView;
import com.fima.cardsui.objects.Card;

public class CommentCard extends Card {
	
	private String avatarURL;
	private String content;
	private final String published;
	
	public CommentCard(String title, String avatarURL, String content, String published) {
		super(title);
		this.avatarURL = avatarURL;
		this.content = content;
		this.published = published;
	}

	@Override
	public View getCardContent(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.comment_entry, null);

		((TextView) view.findViewById(R.id.title)).setText(title);
		
		((SmartImageView) view.findViewById(R.id.bcProfilePic)).setImageUrl(avatarURL, R.drawable.personal_50px);
		
		((TextView) view.findViewById(R.id.bcPostContent)).setText(content);
		
		try {
			long publishedTime = PostCard.ISO_8601.parse(published).getTime();
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
