package com.buddycloud.card;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.buddycloud.R;
import com.buddycloud.image.SmartImageView;
import com.fima.cardsui.objects.Card;

public class PostCard extends Card {
	
	private String avatarURL;
	private String content;
	

	public PostCard(String title, String avatarURL, String content) {
		super(title);
		this.avatarURL = avatarURL;
		this.content = content;
	}

	
	@Override
	public View getCardContent(Context context) {
		View view = LayoutInflater.from(context).inflate(R.layout.post_entry, null);

		((TextView) view.findViewById(R.id.title)).setText(title);
		
		((SmartImageView) view.findViewById(R.id.bcProfilePic)).setImageUrl(avatarURL, R.drawable.personal_50px);
		
		((TextView) view.findViewById(R.id.bcPostContent)).setText(content);
		
		//TODO: post time
		
		return view;
	}
	
	protected int getLastCardLayout() {
		return R.layout.item_card_empty_last_bc;
	}

}
