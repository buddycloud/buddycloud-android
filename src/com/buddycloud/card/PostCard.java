package com.buddycloud.card;

import java.text.ParseException;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.buddycloud.FullScreenImageActivity;
import com.buddycloud.MainActivity;
import com.buddycloud.R;
import com.buddycloud.fragments.ChannelStreamFragment;
import com.buddycloud.model.MediaModel;
import com.buddycloud.model.ModelCallback;
import com.buddycloud.model.PostsModel;
import com.buddycloud.model.SubscribedChannelsModel;
import com.buddycloud.preferences.Preferences;
import com.buddycloud.utils.AvatarUtils;
import com.buddycloud.utils.ImageHelper;
import com.buddycloud.utils.InputUtils;
import com.buddycloud.utils.MeasuredMediaView;
import com.buddycloud.utils.MeasuredMediaView.MeasureListener;
import com.buddycloud.utils.TextUtils;
import com.buddycloud.utils.TimeUtils;
import com.buddycloud.utils.Typefaces;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

public class PostCard extends AbstractCard {
	
	private static final String MEDIA_URL_SUFIX = "?maxwidth=600";
	private static final String MEDIA_URL_SUFIX_FULL = "?maxwidth=1024";
	
	private JSONObject post;
	private String channelJid;
	private CardListAdapter repliesAdapter = new CardListAdapter();
	private Spanned anchoredContent;
	private MainActivity activity;
	private String role;
	private ChannelStreamFragment fragment;

	public PostCard(String channelJid, JSONObject post, 
			ChannelStreamFragment fragment, String role) {
		this.channelJid = channelJid;
		this.fragment = fragment;
		this.activity = (MainActivity) fragment.getActivity();
		this.role = role;
		setPost(post);
	}

	public JSONObject getPost() {
		return post;
	}
	
	public void setPost(JSONObject post) {
		this.post = post;
		this.anchoredContent = TextUtils.anchor(post.optString("content"));
		fillReplyAdapter(this.activity);
	}

	private void fillReplyAdapter(Context context) {
		repliesAdapter.clear();
		JSONArray comments = post.optJSONArray("replies");
		for (int i = 0; comments != null && i < comments.length(); i++) {
			JSONObject comment = comments.optJSONObject(i);
			repliesAdapter.addCard(toReplyCard(comment, context));
		}
		repliesAdapter.notifyDataSetChanged();
	}

	@Override
	public View getContentView(int position, View convertView, ViewGroup viewGroup) {
		
		final String postAuthor = post.optString("author");
		String published = post.optString("published");
		String mediaStr = post.optString("media");
		final String postId = post.optString("id");
		
		JSONArray mediaArray = null;
		if (mediaStr != null && mediaStr.length() > 0) {
			try {
				mediaArray = new JSONArray(mediaStr);
			} catch (JSONException e) {}
		}
		
		
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
		
		String avatarURL = AvatarUtils.avatarURL(viewGroup.getContext(), postAuthor);
		final Context context = viewGroup.getContext();
		ImageView avatarView = holder.getView(R.id.bcProfilePic);
		ImageHelper.picasso(viewGroup.getContext()).load(avatarURL)
				.placeholder(R.drawable.personal_50px)
				.error(R.drawable.personal_50px)
				.transform(ImageHelper.createRoundTransformation(context, 16, false, -1))
				.into(avatarView);
		avatarView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				activity.getBackStack().pushChannel(channelJid);
				activity.showChannelFragment(postAuthor);
			}
		});
		
		TextView contentTextView = holder.getView(R.id.bcPostContent);
		TextView contentTextViewAlt = holder.getView(R.id.bcPostContentAlt);
		
		final MeasuredMediaView mediaView = holder.getView(R.id.bcImageContent);
		mediaView.setImageBitmap(null);
		
		drawNoMediaLayout(contentTextView, contentTextViewAlt, mediaView);
		
		if (mediaArray != null) {
			drawMediaLayout(mediaArray, context, contentTextView,
					contentTextViewAlt, mediaView);
		}
		
		RelativeLayout topicWrapper = holder.getView(R.id.topicWrapper);
		topicWrapper.forceLayout();
		
		View postWrapper = holder.getView(R.id.postWrapper);
		TextView publishedView = holder.getView(R.id.bcPostDate);
		boolean pending = PostsModel.isPending(post);
		if (!pending) {
			try {
				long publishedTime = TimeUtils.fromISOToDate(published).getTime();
				publishedView.setText(
						DateUtils.getRelativeTimeSpanString(publishedTime, 
								new Date().getTime(), DateUtils.MINUTE_IN_MILLIS));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			publishedView.setTextColor(context.getResources().getColor(
					R.color.bc_text_light_grey));
			setBackgroundEnabled(context, postWrapper);
		} else {
			publishedView.setTextColor(context.getResources().getColor(
					R.color.bc_text_bold_grey));
			publishedView.setText(Html.fromHtml("&#x231A;"));
			setBackgroundDisabled(context, postWrapper);
		}
		
		View contextArrowDown = holder.getView(R.id.bcArrowDown);
		contextArrowDown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PostContextUtils.showPostContextActions(context, channelJid, 
						postId, role);
			}
		});
		
		// Replies section
		LinearLayout commentList = holder.getView(R.id.replyListView);
		ReplySectionUtils.configure(commentList, repliesAdapter);
		
		// Create reply section
		View replyFrame = holder.getView(R.id.replyFrameView);
		if (!SubscribedChannelsModel.canPost(role) || pending) {
			replyFrame.setVisibility(View.GONE);
		} else {
			replyFrame.setVisibility(View.VISIBLE);
			ImageView replyAuthorView = holder.getView(R.id.replyAuthorView);
			String replyAuthorURL = AvatarUtils.avatarURL(viewGroup.getContext(), 
					Preferences.getPreference(context, Preferences.MY_CHANNEL_JID));
			ImageHelper.picasso(viewGroup.getContext()).load(replyAuthorURL)
					.placeholder(R.drawable.personal_50px)
					.error(R.drawable.personal_50px)
					.transform(ImageHelper.createRoundTransformation(context, 16, false, -1))
					.into(replyAuthorView);
			final Button replyBtn = holder.getView(R.id.replyBtn);
			replyBtn.setTypeface(Typefaces.get(context,  "fonts/Roboto-Light.ttf"));
			final EditText replyTxt = holder.getView(R.id.replyContentTxt);
			replyBtn.setEnabled(false);
			
			final View thisView = convertView;
			
			replyBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					reply(thisView, context, replyBtn, replyTxt);
				}
			});
			configureReplySection(replyTxt, replyBtn);
		}
		
		return convertView;
	}

	private void setBackgroundDisabled(Context context, View view) {
		setBackground(context, view, R.drawable.bc_shadow_dis);
	}
	
	private void setBackgroundEnabled(Context context, View view) {
		setBackground(context, view, R.drawable.bc_shadow);
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void setBackground(Context context, View view, int resId) {
		int pLeft = view.getPaddingLeft();
		int pTop = view.getPaddingTop();
		int pRight = view.getPaddingRight();
		int pBottom = view.getPaddingBottom();
		int sdk = android.os.Build.VERSION.SDK_INT;
		Drawable drawable = context.getResources().getDrawable(resId);
		if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
			view.setBackgroundDrawable(drawable);
		} else {
		    view.setBackground(drawable);
		}
		view.setPadding(pLeft, pTop, pRight, pBottom);
	}
	
	public CardListAdapter getRepliesAdapter() {
		return repliesAdapter;
	}
	
	private void drawNoMediaLayout(TextView contentTextView,
			TextView contentTextViewAlt, final MeasuredMediaView mediaView) {
		mediaView.setVisibility(View.GONE);
		contentTextViewAlt.setVisibility(View.INVISIBLE);
		contentTextView.setVisibility(View.VISIBLE);
		contentTextView.setText(anchoredContent);
		contentTextView.setMovementMethod(LinkMovementMethod.getInstance());
	}

	private void drawMediaLayout(final JSONArray mediaArray, final Context context,
			final TextView contentTextView, final TextView contentTextViewAlt,
			final MeasuredMediaView mediaView) {
		contentTextView.setText(TextUtils.anchor(getMediaURL(mediaArray, context)));
		mediaView.setMeasureListener(new MeasureListener() {
			@Override
			public void measure(int widthMeasureSpec, int heightMeasureSpec) {
				drawMediaLayout(mediaArray, context, contentTextView,
						contentTextViewAlt, mediaView, widthMeasureSpec);
			}
		});
		mediaView.setVisibility(View.VISIBLE);
	}

	private void drawMediaLayout(final JSONArray mediaArray,
			final Context context, final TextView contentTextView,
			final TextView contentTextViewAlt,
			final MeasuredMediaView mediaView, int widthMeasureSpec) {
		final String imageURLLo = getMediaLoURL(mediaArray, context);
		
		ImageHelper.picasso(context)
			.load(imageURLLo)
			.transform(ImageHelper.createRoundTransformation(context, 8, 
					true, widthMeasureSpec))
			.into(new Target() {
			
				@Override
				public void onBitmapLoaded(Bitmap arg0, LoadedFrom arg1) {
					contentTextViewAlt.setVisibility(View.VISIBLE);
					contentTextView.setVisibility(View.INVISIBLE);
					contentTextViewAlt.setText(anchoredContent);
					contentTextViewAlt.setMovementMethod(LinkMovementMethod.getInstance());
					
					mediaView.setImageBitmap(arg0);
					mediaView.forceLayout();
					
					final String imageURLHi = getMediaHiURL(mediaArray, context);
					ImageHelper.picasso(context).load(imageURLHi).fetch();
					
					mediaView.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent();
							intent.setClass(context, FullScreenImageActivity.class);
							intent.putExtra(FullScreenImageActivity.IMAGE_URL, imageURLLo);
							intent.putExtra(FullScreenImageActivity.IMAGE_URL_HIGH_RES, imageURLHi);
							activity.startActivityForResult(intent, FullScreenImageActivity.REQUEST_CODE);
						}
					});
				}
				
				@Override
				public void onBitmapFailed() {
					// TODO Auto-generated method stub
				}
		});
	}
	
	private String getMediaLoURL(JSONArray mediaArray, final Context context) {
		String userMediaURL = getMediaURL(mediaArray, context);
		return userMediaURL + MEDIA_URL_SUFIX;
	}

	private String getMediaHiURL(JSONArray mediaArray, final Context context) {
		String userMediaURL = getMediaURL(mediaArray, context);
		return userMediaURL + MEDIA_URL_SUFIX_FULL;
	}
	
	private String getMediaURL(JSONArray mediaArray, final Context context) {
		String apiAddress = Preferences.getPreference(context, Preferences.API_ADDRESS);
		JSONObject mediaJson = mediaArray.optJSONObject(0);
		String userMediaURL = apiAddress + "/" + mediaJson.optString("channel") + 
				MediaModel.ENDPOINT + "/" + mediaJson.optString("id");
		return userMediaURL;
	}
	
	private void showProgress(View container) {
		container.findViewById(R.id.replyStreamProgress).setVisibility(View.VISIBLE);
	}
	
	private void hideProgress(View container) {
		container.findViewById(R.id.replyStreamProgress).setVisibility(View.GONE);
	}
	
	private void reply(final View convertView, final Context context,
			final Button replyBtn, final EditText replyTxt) {
		
		if (!replyBtn.isEnabled()) {
			return;
		}
		
		showProgress(convertView);
		
		JSONObject replyPost = createReply(replyTxt);
		replyTxt.setText("");
		InputUtils.hideKeyboard(context, replyTxt);
		PostsModel.getInstance().save(context, replyPost, new ModelCallback<JSONObject>() {
			@Override
			public void success(JSONObject response) {
				Toast.makeText(context, context.getString(R.string.message_post_created), 
						Toast.LENGTH_LONG).show();
				hideProgress(convertView);
				fragment.fillRemotely(null, null);
//				loadReplies(post, channelJid, context, new ModelCallback<Void>() {
//					@Override
//					public void success(Void response) {
//						getParentAdapter().sort();
//						hideProgress(convertView);
//					}
//
//					@Override
//					public void error(Throwable throwable) {
//						Toast.makeText(context,
//								context.getString(R.string.message_reply_created), 
//								Toast.LENGTH_LONG).show();
//						hideProgress(convertView);
//					}
//				});
				
			}
			
			@Override
			public void error(Throwable throwable) {
				Toast.makeText(context, throwable.getMessage(), Toast.LENGTH_LONG).show();
				hideProgress(convertView);
			}
		}, channelJid);
	}
	
	public static void configureReplySection(final EditText replyContent, final Button replyBtn) {
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
				replyBtn.setEnabled(enabled);
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
	
	private CommentCard toReplyCard(JSONObject comment, Context context) {
		CommentCard commentCard = new CommentCard(channelJid, comment, 
				activity, role);
		return commentCard;
	}
	
	private static CardViewHolder fillHolder(View view) {
		return CardViewHolder.create(view, 
				R.id.bcPostContentAlt, R.id.bcProfilePic, 
				R.id.bcPostContent, R.id.bcCommentCount, 
				R.id.bcImageContent, R.id.bcPostDate, 
				R.id.replyListView, R.id.replyAuthorView,
				R.id.replyContentTxt, R.id.replyBtn, 
				R.id.topicWrapper, R.id.replyFrameView, 
				R.id.bcArrowDown, R.id.postWrapper);
	}

	@Override
	public int compareTo(Card anotherCard) {
		try {
			Date otherUpdated = TimeUtils.threadUpdated(anotherCard.getPost());
			Date thisUpdated = TimeUtils.threadUpdated(this.getPost());
			return otherUpdated.compareTo(thisUpdated);
		} catch (ParseException e) {
			return 0;
		}
	}

	public void addPendingCard(JSONObject pendingItem) {
		repliesAdapter.addCard(toReplyCard(pendingItem, activity));
		repliesAdapter.sort();
	}
}
