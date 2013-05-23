package com.buddycloud.card;

import java.text.ParseException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.buddycloud.R;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.TimeUtils;
import com.squareup.picasso.Picasso;

public class PostCard extends AbstractCard {
	
	public static final String MEDIA_PATTERN_SUFIX = "/\\S+@\\S+/media/\\w+";
	public static final String MEDIA_URL_SUFIX = "?maxheight=600";
	
	private String avatarURL;
	private String content;
	private Integer commentCount;
	private Boolean hasUserMedia;
	private final String published;
	private final String title;
	private String userMediaURL;

	public PostCard(String title, String avatarURL, String content, 
			String published, Integer commentCount) {
		this.title = title;
		this.published = published;
		this.commentCount = commentCount;
		this.avatarURL = avatarURL;
		this.content = content;
	}
	
	@Override
	public View getContentView(int position, View convertView, ViewGroup viewGroup) {
		
		boolean reuse = convertView != null && convertView.getTag() != null; 
		CardViewHolder holder = null;
		
		if (!reuse) {
			LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
			convertView = inflater.inflate(R.layout.post_entry, viewGroup, false);
			holder = fillHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (CardViewHolder) convertView.getTag();
		}
		
		Context context = viewGroup.getContext();
		TextView titleView = holder.getView(R.id.title);
		titleView.setText(title);
		
		ImageView avatarView = holder.getView(R.id.bcProfilePic);
		Picasso.with(viewGroup.getContext()).load(avatarURL)
				.placeholder(R.drawable.personal_50px)
				.error(R.drawable.personal_50px)
				.into(avatarView);
		
		TextView contentView = holder.getView(R.id.bcPostContent);
		contentView.setText(content);
		
		TextView commentCountView = holder.getView(R.id.bcCommentCount);
		commentCountView.setText(commentCount.toString());
		
		if (hasUserMedia == null) {
			String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
			Pattern mediaPattern = Pattern.compile(apiAddress + MEDIA_PATTERN_SUFIX);
			Matcher matcher = mediaPattern.matcher(content);
			hasUserMedia = matcher.find();
			if (hasUserMedia) {
				this.userMediaURL = content.substring(matcher.start(), matcher.end());
			}
		}
		
		ImageView mediaView = holder.getView(R.id.bcImageContent);
		if (hasUserMedia) {
			mediaView.setVisibility(View.VISIBLE);
			Picasso.with(viewGroup.getContext())
				.load(userMediaURL + MEDIA_URL_SUFIX)
				.placeholder(R.drawable.personal_50px)
				.error(R.drawable.personal_50px)
				.into(mediaView);
			
		} else {
			mediaView.setVisibility(View.GONE);
			mediaView.setImageBitmap(null);
		}
		
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
		return CardViewHolder.create(view, R.id.title, R.id.bcProfilePic, 
				R.id.bcPostContent, R.id.bcCommentCount, 
				R.id.bcImageContent, R.id.bcPostDate);
	}
}
