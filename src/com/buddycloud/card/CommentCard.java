package com.buddycloud.card;

import java.text.ParseException;
import java.util.Date;

import org.json.JSONObject;

import android.text.Html;
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
import com.buddycloud.model.PostsModel;
import com.buddycloud.utils.AvatarUtils;
import com.buddycloud.utils.ImageHelper;
import com.buddycloud.utils.TextUtils;
import com.buddycloud.utils.TimeUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class CommentCard extends AbstractCard {

	private Spanned anchoredContent;
	private MainActivity activity;
	private JSONObject comment;
	private String channelJid;
	private String role;

	public CommentCard(String channelJid, JSONObject comment,
			MainActivity activity, String role) {
		this.channelJid = channelJid;
		this.comment = comment;
		this.activity = activity;
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
		DisplayImageOptions dio = new DisplayImageOptions.Builder()
				.cloneFrom(ImageHelper.defaultImageOptions())
				.showImageOnFail(R.drawable.avatar_icon)
				.showImageOnLoading(R.drawable.avatar_icon)
				.preProcessor(ImageHelper.createRoundProcessor(16, false, -1))
				.build();

		ImageView avatarView = holder.getView(R.id.bcProfilePic);
		ImageLoader.getInstance().displayImage(avatarURL, avatarView, dio);
		avatarView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (!channelJid.equals(replyAuthor)) {
					activity.showChannelFragment(replyAuthor);
				}
			}
		});

		TextView replyAuthorView = holder.getView(R.id.bcPostAuthor);
		if (!TextUtils.isEmpty(replyAuthor)) {
			replyAuthorView.setText(TextUtils.capitalize(replyAuthor));
		}

		View contextArrowDown = holder.getView(R.id.bcArrowDown);
		contextArrowDown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PostContextUtils.showPostContextActions(viewGroup.getContext(),
						channelJid, replyId, role, PostContextUtils.COMMENT_POST);
			}
		});

		TextView contentView = holder.getView(R.id.bcCommentContent);
		contentView.setMovementMethod(LinkMovementMethod.getInstance());
		contentView.setText(anchoredContent);

		boolean pending = PostsModel.isPending(comment);
		TextView publishedView = holder.getView(R.id.bcPostDate);
		TextView publishedPendingView = holder.getView(R.id.bcCommentPending);
		if (!pending) {
			try {
				long publishedTime = TimeUtils.fromISOToDate(published).getTime();
				publishedView.setText(DateUtils.getRelativeTimeSpanString(
						publishedTime, new Date().getTime(),
						DateUtils.MINUTE_IN_MILLIS));
				publishedPendingView.setVisibility(View.GONE);
				contextArrowDown.setVisibility(View.VISIBLE);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		} else {
			publishedView.setText(Html.fromHtml("&#x231A;"));
			publishedPendingView.setVisibility(View.VISIBLE);
			contextArrowDown.setVisibility(View.GONE);
		}

		return convertView;
	}

	private static CardViewHolder fillHolder(View view) {
		return CardViewHolder.create(view, R.id.bcProfilePic,
				R.id.postContainer, R.id.commentTitleWrapper,
				R.id.bcPostAuthor, R.id.bcPostDate, R.id.bcTopRightView,
				R.id.bcArrowDown, R.id.bcCommentPending, R.id.bcImageContent,
				R.id.bcCommentContent);
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
