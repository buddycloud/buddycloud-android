package com.buddycloud.card;

import java.text.ParseException;
import java.util.Date;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.buddycloud.R;
import com.buddycloud.utils.ImageHelper;
import com.buddycloud.utils.TimeUtils;
import com.squareup.picasso.Picasso;

public class CommentCard extends AbstractCard {
	
	private String avatarURL;
	private String content;
	private final String published;
	
	public CommentCard(String avatarURL, String content, String published) {
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
		
		ImageView avatarView = holder.getView(R.id.bcProfilePic);
		Picasso.with(viewGroup.getContext()).load(avatarURL)
				.placeholder(R.drawable.personal_50px)
				.error(R.drawable.personal_50px)
				.transform(ImageHelper.createRoundTransformation(
						viewGroup.getContext(), 16, false, -1))
				.into(avatarView);
		
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
		return CardViewHolder.create(view, R.id.bcProfilePic, 
				R.id.bcPostContent, R.id.bcPostDate);
	}
}
