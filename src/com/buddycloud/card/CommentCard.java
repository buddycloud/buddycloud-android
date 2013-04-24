package com.buddycloud.card;

import java.text.ParseException;
import java.util.Date;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.buddycloud.R;
import com.buddycloud.image.SmartImageView;
import com.buddycloud.utils.TimeUtils;

public class CommentCard extends AbstractCard {
	
	private String avatarURL;
	private String content;
	private final String published;
	private final String title;
	
	public CommentCard(String title, String avatarURL, String content, String published) {
		this.title = title;
		this.avatarURL = avatarURL;
		this.content = content;
		this.published = published;
	}

	@Override
	public View getContentView(int position, View convertView,
			ViewGroup viewGroup) {
		
		boolean reuse = convertView != null && convertView.getTag() != null; 
		CardViewHolder holder = null;
		
		if (!reuse) {
			LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
			convertView = inflater.inflate(R.layout.comment_entry, viewGroup, false);
			holder = fillHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (CardViewHolder) convertView.getTag();
		}
		
		TextView titleView = holder.getView(R.id.title);
		titleView.setText(title);
		
		SmartImageView avatarView = holder.getView(R.id.bcProfilePic);
		avatarView.setImageUrl(avatarURL, R.drawable.personal_50px);
		
		TextView contentView = holder.getView(R.id.bcPostContent);
		contentView.setText(content);
		
		try {
			long publishedTime = TimeUtils.fromISOToDate(published).getTime();
			TextView publishedView = holder.getView(R.id.bcPostDate);
			publishedView.setText(
					DateUtils.getRelativeTimeSpanString(publishedTime, 
							new Date().getTime(), DateUtils.MINUTE_IN_MILLIS));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return convertView;
	}

	private static CardViewHolder fillHolder(View view) {
		return CardViewHolder.create(view, R.id.title, 
				R.id.bcProfilePic, R.id.bcPostContent, R.id.bcPostDate);
	}
}
