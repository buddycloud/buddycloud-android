package com.buddycloud.card;

import java.text.ParseException;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.buddycloud.R;
import com.buddycloud.model.MediaModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.PostsModel;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.AvatarUtils;
import com.buddycloud.utils.ImageHelper;
import com.buddycloud.utils.TimeUtils;
import com.squareup.picasso.Picasso;

public class PostCard extends AbstractCard {
	
	private static final String MEDIA_URL_SUFIX = "?maxwidth=600";
	
	private JSONObject post;
	private String channelJid;
	private CardListAdapter commentAdapter = new CardListAdapter();
	
	public PostCard(String channelJid, JSONObject post, Context context) {
		this.channelJid = channelJid;
		this.post = post;
		loadComments(post, context);
	}

	@Override
	public View getContentView(int position, View convertView, ViewGroup viewGroup) {
		
		String postAuthor = post.optString("author");
		String content = post.optString("content");
		String published = post.optString("published");
		String mediaStr = post.optString("media");
		
		JSONArray mediaArray = null;
		if (mediaStr != null && mediaStr.length() > 0) {
			try {
				mediaArray = new JSONArray(mediaStr);
			} catch (JSONException e) {}
		}
		
		String avatarURL = AvatarUtils.avatarURL(viewGroup.getContext(), postAuthor);
		
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
		
		final Context context = viewGroup.getContext();
		ImageView avatarView = holder.getView(R.id.bcProfilePic);
		Picasso.with(viewGroup.getContext()).load(avatarURL)
				.placeholder(R.drawable.personal_50px)
				.error(R.drawable.personal_50px)
				.transform(ImageHelper.createRoundTransformation(context, 16, false, -1))
				.into(avatarView);
		
		TextView contentView = holder.getView(R.id.bcPostContent);
		TextView contentViewAlt = holder.getView(R.id.bcPostContentAlt);
		
		ImageView mediaView = holder.getView(R.id.bcImageContent);
		if (mediaArray != null) {
			String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
			
			JSONObject mediaJson = mediaArray.optJSONObject(0);
			
			String userMediaURL = apiAddress + "/" + mediaJson.optString("channel") + 
					MediaModel.ENDPOINT + "/" + mediaJson.optString("id");
			
			mediaView.setVisibility(View.VISIBLE);
			Picasso.with(viewGroup.getContext())
				.load(userMediaURL + MEDIA_URL_SUFIX)
				.transform(ImageHelper.createRoundTransformation(context, 8, 
						true, viewGroup.getWidth()))
				.into(mediaView);
			
			contentViewAlt.setVisibility(View.VISIBLE);
			contentView.setVisibility(View.INVISIBLE);
			contentViewAlt.setText(content);
		} else {
			mediaView.setVisibility(View.INVISIBLE);
			mediaView.setImageBitmap(null);
			
			contentViewAlt.setVisibility(View.INVISIBLE);
			contentView.setVisibility(View.VISIBLE);
			contentView.setText(content);
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
		
		// Replies section
		LinearLayout commentList = holder.getView(R.id.replyListView);
		ReplySectionView.configure(commentList, commentAdapter);
		
		// Create reply section
		ImageView replyAuthorView = holder.getView(R.id.replyAuthorView);
		String replyAuthorURL = AvatarUtils.avatarURL(viewGroup.getContext(), 
				Preferences.getPreference(context, Preferences.MY_CHANNEL_JID));
		Picasso.with(viewGroup.getContext()).load(replyAuthorURL)
				.placeholder(R.drawable.personal_50px)
				.error(R.drawable.personal_50px)
				.transform(ImageHelper.createRoundTransformation(context, 16, false, -1))
				.into(replyAuthorView);
		final FrameLayout replyBtn = holder.getView(R.id.replyBtn);
		final EditText replyTxt = holder.getView(R.id.replyContentTxt);
		replyBtn.setEnabled(false);
		replyBtn.getChildAt(0).setEnabled(false);
		
		replyBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				reply(context, replyBtn, replyTxt);
			}
		});
		configureReplySection(replyTxt, replyBtn);
		
		return convertView;
	}
	
	private void reply(final Context context,
			final FrameLayout replyBtn, final EditText replyTxt) {
		JSONObject replyPost = createReply(replyTxt);
		PostsModel.getInstance().save(context, replyPost, new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
				if (replyBtn.isEnabled()) {
					Toast.makeText(context, "Post created", Toast.LENGTH_LONG).show();
					replyTxt.setText("");
					loadComments(post, context);
				}
			}
			
			@Override
			public void error(Throwable throwable) {
				Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_LONG).show();
			}
		}, channelJid);
	}
	
	public static void configureReplySection(final EditText replyContent, final FrameLayout replyButton) {
		replyContent.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) {
				
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				boolean enabled = arg0 != null && arg0.length() > 0;
				replyButton.setEnabled(enabled);
				replyButton.getChildAt(0).setEnabled(enabled);
			}
			
		});
	}
	
	private JSONObject createReply(EditText postContent) {
		JSONObject reply = new JSONObject();
		try {
			reply.putOpt("content", postContent.getText().toString());
			reply.putOpt("replyTo", this.post.optString("id"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return reply;
	}
	
	private void loadComments(JSONObject post, final Context context) {
		PostsModel.getInstance().getPostAsync(context, new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
				commentAdapter.clear();
				JSONArray comments = response.optJSONArray("replies");
				for (int i = comments.length() - 1; i >= 0; i--) {
					JSONObject comment = comments.optJSONObject(i);
					commentAdapter.addCard(toCard(comment, context));
				}
				commentAdapter.notifyDataSetChanged();
			}
			
			@Override
			public void error(Throwable throwable) {
				// TODO Auto-generated method stub
			}
		}, channelJid, post.optString("id"));
	}
	
	private CommentCard toCard(JSONObject comment, Context context) {
		String postAuthor = comment.optString("author");
		String postContent = comment.optString("content");
		String published = comment.optString("published");
		String avatarURL = AvatarUtils.avatarURL(context, postAuthor);
		CommentCard commentCard = new CommentCard(avatarURL, postContent, published);
		return commentCard;
	}
	
	private static CardViewHolder fillHolder(View view) {
		return CardViewHolder.create(view, 
				R.id.bcPostContentAlt, R.id.bcProfilePic, 
				R.id.bcPostContent, R.id.bcCommentCount, 
				R.id.bcImageContent, R.id.bcPostDate, 
				R.id.replyListView, R.id.replyAuthorView,
				R.id.replyContentTxt, R.id.replyBtn);
	}
}
