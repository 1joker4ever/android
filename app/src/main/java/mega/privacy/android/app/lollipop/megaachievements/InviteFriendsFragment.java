package mega.privacy.android.app.lollipop.megaachievements;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.SimpleDividerItemDecoration;
import mega.privacy.android.app.lollipop.controllers.ContactController;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaChatApiAndroid;

public class InviteFriendsFragment extends Fragment implements OnClickListener{
	
	public static int DEFAULT_AVATAR_WIDTH_HEIGHT = 150; //in pixels

	Context context;
	ActionBar aB;
	int height;

	RelativeLayout parentRelativeLayout;
	RecyclerView recyclerView;
//	StaggeredGridLayoutManager mLayoutManager;
	LinearLayoutManager mLayoutManager;
	MegaInviteFriendsAdapter adapter;
	EditText editTextMail;
	LinearLayout linearLayoutCard;
	Button inviteButton;

	ArrayList<String> mails;
	
	DisplayMetrics outMetrics;
	float density;

	MegaApiAndroid megaApi;
	MegaChatApiAndroid megaChatApi;

	@Override
	public void onCreate (Bundle savedInstanceState){
		log("onCreate");
		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}

		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		log("onCreateView");
		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}

		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		density = ((Activity) context).getResources().getDisplayMetrics().density;

		height = outMetrics.heightPixels;
		int width = outMetrics.widthPixels;

		boolean enabledAchievements = megaApi.isAchievementsEnabled();
		log("The achievements are: "+enabledAchievements);

		View v = inflater.inflate(R.layout.fragment_invite_friends, container, false);

//		parentRelativeLayout = (RelativeLayout) v.findViewById(R.id.referral_bonuses_relative_layout);
//		linearLayoutCard = (LinearLayout) v.findViewById(R.id.card_linear_layout);

		recyclerView = (RecyclerView) v.findViewById(R.id.invite_friends_recycler_view);
//		mLayoutManager = new StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL);
//		mLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
		mLayoutManager = new LinearLayoutManager(context);
		recyclerView.setLayoutManager(mLayoutManager);
		recyclerView.setItemAnimator(new DefaultItemAnimator());

		inviteButton = (Button)v.findViewById(R.id.invite_button);
		inviteButton.setBackgroundColor(ContextCompat.getColor(context, R.color.invite_button_deactivated));

		editTextMail = (EditText) v.findViewById(R.id.edit_text_invite_mail);

		editTextMail.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {}

			public void beforeTextChanged(CharSequence s, int start,
										  int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start,
									  int before, int count) {
				log("onTextChanged: " + s + ", " + start + ", " + before + ", " + count);

				if (s != null) {
					if (s.length() > 0) {
						String temp = s.toString();

//						CharSequence last = s.subSequence(s.length()-1, s.length());
						char last = s.charAt(s.length()-1);
						if(last == ' '){
							temp = temp.trim();
							boolean isValid = isValidEmail(temp);
							if(isValid){
								addMail(temp.trim());
								editTextMail.getText().clear();
							}
							else{

							}

						}
						else{
							log("Last character is: "+last);
						}
//						String lastCharacter = last.toString();
//						if(last.equals(" ")){
//
//						}
//						else{
//							log("Last character is: "+last);
//						}
//						if(temp.trim().length()>0){
//							sendIcon.setVisibility(View.VISIBLE);
//						}
//						else{
//							sendIcon.setVisibility(View.GONE);
//						}
					}
					else {
//						sendIcon.setVisibility(View.GONE);
					}
				}
				else{
//					sendIcon.setVisibility(View.GONE);
				}

//				if(megaChatApi.isSignalActivityRequired()){
//					megaChatApi.signalPresenceActivity();
//				}

			}
		});

		mails = new ArrayList<>();

		if (adapter == null){
			adapter = new MegaInviteFriendsAdapter(context, this, mails, recyclerView);
		}
		else{
//			adapter.setReferralBonuses(((AchievementsActivity)context).referralBonuses);
		}

		recyclerView.setAdapter(adapter);

		((MegaApplication) ((Activity)context).getApplication()).sendSignalPresenceActivity();

		return v;
	}

	public final static boolean isValidEmail(CharSequence target) {
		if (target == null) {
			return false;
		} else {
			return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
		}
	}

	public void deleteMail(String mailToDelete){
		int positionToRemove=-1;
		for(int i=0;i<mails.size();i++){
			if(mailToDelete.equals(mails.get(i))){
				positionToRemove = i;
				break;
			}
		}
		if(positionToRemove!=-1){
			mails.remove(positionToRemove);
			adapter.setNames(mails);
			adapter.notifyDataSetChanged();
		}

		if(mails.isEmpty()){
			inviteButton.setBackgroundColor(ContextCompat.getColor(context, R.color.invite_button_deactivated));
			inviteButton.setOnClickListener(null);
		}
		else{
			inviteButton.setBackgroundColor(ContextCompat.getColor(context, R.color.accentColor));
			inviteButton.setOnClickListener(this);
		}
	}

	public void addMail(String mail){
		log("addMail: "+mail);
		mails.add(mail);
		adapter.setNames(mails);

		if(mails.isEmpty()){
			inviteButton.setBackgroundColor(ContextCompat.getColor(context, R.color.invite_button_deactivated));
			inviteButton.setOnClickListener(null);
		}
		else{
			inviteButton.setBackgroundColor(ContextCompat.getColor(context, R.color.accentColor));
			inviteButton.setOnClickListener(this);
		}
//		if(adapter.getItemCount() > 5){
//			View item = adapter.getView(0, null, recyclerView);
//			adapter.getItemViewType()
//			item.measure(0, 0);
//			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, (int) (5.5 * item.getMeasuredHeight()));
//			recyclerView.setLayoutParams(params);
//		}

//		int totalHeight = 0;
//
//		for (int i = 0; i < adapter.getItemCount(); i++) {
//			View mView = recyclerView.findViewHolderForAdapterPosition(i).itemView;
//
//			mView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
//					View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//
//			totalHeight += mView.getMeasuredHeight();
//			log(totalHeight);
//		}

//
//		if (recyclerView.getLayoutParams().height > height) {
//			ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
//			params.height = 500;
//			recyclerView.setLayoutParams(params);
//		}

		recyclerView.setVisibility(View.VISIBLE);
//		log("recyclerView: "+recyclerView.getLayoutParams().height+" height "+height);
	}

//	public static AchievementsFragment newInstance() {
//		log("newInstance");
//		AchievementsFragment fragment = new AchievementsFragment();
//		return fragment;
//	}

	@Override
	public void onAttach(Activity activity) {
		log("onAttach");
		super.onAttach(activity);
		context = activity;
		aB = ((AppCompatActivity)activity).getSupportActionBar();
	}

	@Override
	public void onAttach(Context context) {
		log("onAttach context");
		super.onAttach(context);
		this.context = context;
		aB = ((AppCompatActivity)getActivity()).getSupportActionBar();
	}

	@Override
	public void onClick(View v) {
		log("onClick");
		((MegaApplication) ((Activity)context).getApplication()).sendSignalPresenceActivity();
		switch (v.getId()) {

			case R.id.invite_button:{
				log("Invite friends");
				((AchievementsActivity)context).inviteFriends(mails);
				editTextMail.getText().clear();
				mails.clear();
				adapter.setNames(mails);
				adapter.notifyDataSetChanged();
				break;
			}
		}
	}

	public int onBackPressed(){
		log("onBackPressed");
		((MegaApplication) ((Activity)context).getApplication()).sendSignalPresenceActivity();

//		if(exportMKLayout.getVisibility()==View.VISIBLE){
//			log("Master Key layout is VISIBLE");
//			hideMKLayout();
//			return 1;
//		}

		return 0;
	}

	public static void log(String log) {
		Util.log("InviteFriendsFragment", log);
	}
}
