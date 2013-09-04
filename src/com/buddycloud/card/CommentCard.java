package com.buddycloud.card;

import java.text.ParseException;
import java.util.Date;

import org.json.JSONObject;

import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.buddycloud.MainActivity;
import com.buddycloud.R;
import com.buddycloud.utils.AvatarUtils;
import com.buddycloud.utils.ImageHelper;
import com.buddycloud.utils.TextUtils;
import com.buddycloud.utils.TimeUtils;

public class CommentCard extends AbstractCard {
	
	private Spanned anchoredContent;
	private MainActivity activity;
	private JSONObject comment;
	private String channelJid;
	private String role;
	private CardListAdapter cardAdapter;
	
	public CommentCard(String channelJid, JSONObject comment, MainActivity activity, 
			CardListAdapter cardAdapter, String role) {
		this.channelJid = channelJid;
		this.comment = comment;
		this.activity = activity;
		this.cardAdapter = cardAdapter;
		this.role = role;
		this.anchoredContent = TextUtils.anchor(comment.optString("content"));
	}
	
	@Override
	public JSONObject getPost() {
		return comment;
	}

	@Override
	public View getContentView(int position, View convertView,
			final ViewGroup viewGroup) {
		
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
		
		final String replyAuthor = comment.optString("author");
		String published = comment.optString("published");
		final String replyId = comment.optString("id");
		
		String avatarURL = AvatarUtils.avatarURL(viewGroup.getContext(), replyAuthor);
		ImageView avatarView = holder.getView(R.id.bcProfilePic);
		ImageHelper.picasso(viewGroup.getContext()).load(avatarURL)
				.placeholder(R.drawable.personal_50px)
				.error(R.drawable.personal_50px)
				.transform(ImageHelper.createRoundTransformation(
						viewGroup.getContext(), 16, false, -1))
				.into(avatarView);
		avatarView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				activity.showChannelFragment(replyAuthor);
			}
		});
		
		TextView contentView = holder.getView(R.id.bcPostContent);
		contentView.setMovementMethod(LinkMovementMethod.getInstance());
		contentView.setText(anchoredContent);
		
		try {
			long publishedTime = TimeUtils.fromISOToDate(published).getTime();
			TextView publishedView = holder.getView(R.id.bcPostDate);
			publishedView.setText(
					DateUtils.getRelativeTimeSpanString(publishedTime, 
							new Date().getTime(), DateUtils.MINUTE_IN_MILLIS));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		View contextArrowDown = holder.getView(R.id.bcArrowDown);
		contextArrowDown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PostContextUtils.showPostContextActions(viewGroup.getContext(), 
						channelJid, replyId, cardAdapter, role);
			}
		});
		
		return convertView;
	}

	private static CardViewHolder fillHolder(View view) {
		return CardViewHolder.create(view, R.id.bcProfilePic, 
				R.id.bcPostContent, R.id.bcPostDate, 
				R.id.bcArrowDown);
	}

	@Override
	public void setPost(JSONObject post) {
		this.comment = post;
	}
	
	@Override
	public int compareTo(Card anotherCard) {
		try {
			Date otherUpdated = TimeUtils.updated(anotherCard.getPost());
			Date thisUpdated = TimeUtils.updated(this.getPost());
			return thisUpdated.compareTo(otherUpdated);
		} catch (ParseException e) {
			return 0;
		}
	}
}
