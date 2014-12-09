package nz.mega.android;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import nz.mega.android.utils.Util;
import nz.mega.components.RoundedImageView;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaNode;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaUser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class MegaContactsGridAdapter extends BaseAdapter implements OnClickListener{
	
	Context context;
	int positionClicked;
	ArrayList<MegaUser> contacts;
	MegaApiAndroid megaApi;
	ListView listFragment;
	
	boolean multipleSelect = false;
	
	private class UserAvatarListenerGrid implements MegaRequestListenerInterface{
		
		Context context;
		ViewHolderContactsGrid holder;
		MegaContactsGridAdapter adapter;
		int numView;
		
		public UserAvatarListenerGrid(Context context, ViewHolderContactsGrid holder, MegaContactsGridAdapter adapter, int numView){
			this.context = context;
			this.holder = holder;
			this.adapter = adapter;
			this.numView = numView;
		}

		@Override
		public void onRequestStart(MegaApiJava api, MegaRequest request) {
			log("onRequestStart()");
		}

		@Override
		public void onRequestFinish(MegaApiJava api, MegaRequest request,
				MegaError e) {
			log("onRequestFinish()");
			boolean avatarExists = false;
			if (e.getErrorCode() == MegaError.API_OK){
				if (numView == 1){
					if (holder.contactMail1.compareTo(request.getEmail()) == 0){
						File avatar = null;
						if (context.getExternalCacheDir() != null){
							avatar = new File(context.getExternalCacheDir().getAbsolutePath(), holder.contactMail1 + ".jpg");
						}
						else{
							avatar = new File(context.getCacheDir().getAbsolutePath(), holder.contactMail1 + ".jpg");
						}
						Bitmap bitmap = null;
						if (avatar.exists()){
							if (avatar.length() > 0){
								BitmapFactory.Options bOpts = new BitmapFactory.Options();
								bOpts.inPurgeable = true;
								bOpts.inInputShareable = true;
								bitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
								if (bitmap == null) {
									avatar.delete();
								}
								else{
									avatarExists = true;
									holder.imageView1.setImageBitmap(bitmap);
								}
							}
						}
					}
				}
				else if (numView == 2){
					if (holder.contactMail2.compareTo(request.getEmail()) == 0){
						File avatar = null;
						if (context.getExternalCacheDir() != null){
							avatar = new File(context.getExternalCacheDir().getAbsolutePath(), holder.contactMail2 + ".jpg");
						}
						else{
							avatar = new File(context.getCacheDir().getAbsolutePath(), holder.contactMail2 + ".jpg");
						}
						Bitmap bitmap = null;
						if (avatar.exists()){
							if (avatar.length() > 0){
								BitmapFactory.Options bOpts = new BitmapFactory.Options();
								bOpts.inPurgeable = true;
								bOpts.inInputShareable = true;
								bitmap = BitmapFactory.decodeFile(avatar.getAbsolutePath(), bOpts);
								if (bitmap == null) {
									avatar.delete();
								}
								else{
									avatarExists = true;
									holder.imageView2.setImageBitmap(bitmap);
								}
							}
						}
					}
				}
			}
			
			if (!avatarExists){
				createDefaultAvatar();
			}
		}
		
		public void createDefaultAvatar(){
			log("createDefaultAvatar()");
			
			Bitmap defaultAvatar = Bitmap.createBitmap(ManagerActivity.DEFAULT_AVATAR_WIDTH_HEIGHT,ManagerActivity.DEFAULT_AVATAR_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);
			Canvas c = new Canvas(defaultAvatar);
			Paint p = new Paint();
			p.setAntiAlias(true);
			p.setColor(context.getResources().getColor(R.color.color_default_avatar_mega));
			
			int radius; 
	        if (defaultAvatar.getWidth() < defaultAvatar.getHeight())
	        	radius = defaultAvatar.getWidth()/2;
	        else
	        	radius = defaultAvatar.getHeight()/2;
	        
			c.drawCircle(defaultAvatar.getWidth()/2, defaultAvatar.getHeight()/2, radius, p);
			if (numView == 1){
				holder.imageView1.setImageBitmap(defaultAvatar);
			}
			else if (numView == 2){
				holder.imageView2.setImageBitmap(defaultAvatar);
			}
			
			Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
			DisplayMetrics outMetrics = new DisplayMetrics ();
		    display.getMetrics(outMetrics);
		    float density  = context.getResources().getDisplayMetrics().density;
		    
		    int avatarTextSize = getAvatarTextSize(density);
		    log("DENSITY: " + density + ":::: " + avatarTextSize);
		    if (numView == 1){
			    if (holder.contactMail1 != null){
				    if (holder.contactMail1.length() > 0){
				    	log("TEXT: " + holder.contactMail1);
				    	log("TEXT AT 0: " + holder.contactMail1.charAt(0));
				    	String firstLetter = holder.contactMail1.charAt(0) + "";
				    	firstLetter = firstLetter.toUpperCase(Locale.getDefault());
				    	holder.contactInitialLetter1.setText(firstLetter);
				    	holder.contactInitialLetter1.setTextSize(100);
				    	holder.contactInitialLetter1.setTextColor(Color.WHITE);
				    }
			    }
		    }
		    else if (numView == 2){
		    	if (holder.contactMail2 != null){
				    if (holder.contactMail2.length() > 0){
				    	log("TEXT: " + holder.contactMail2);
				    	log("TEXT AT 0: " + holder.contactMail2.charAt(0));
				    	String firstLetter = holder.contactMail2.charAt(0) + "";
				    	firstLetter = firstLetter.toUpperCase(Locale.getDefault());
				    	holder.contactInitialLetter2.setText(firstLetter);
				    	holder.contactInitialLetter2.setTextSize(100);
				    	holder.contactInitialLetter2.setTextColor(Color.WHITE);
				    }
			    }
		    }
		}
		
		private int getAvatarTextSize (float density){
			float textSize = 0.0f;
			
			if (density > 3.0){
				textSize = density * (DisplayMetrics.DENSITY_XXXHIGH / 72.0f);
			}
			else if (density > 2.0){
				textSize = density * (DisplayMetrics.DENSITY_XXHIGH / 72.0f);
			}
			else if (density > 1.5){
				textSize = density * (DisplayMetrics.DENSITY_XHIGH / 72.0f);
			}
			else if (density > 1.0){
				textSize = density * (72.0f / DisplayMetrics.DENSITY_HIGH / 72.0f);
			}
			else if (density > 0.75){
				textSize = density * (72.0f / DisplayMetrics.DENSITY_MEDIUM / 72.0f);
			}
			else{
				textSize = density * (72.0f / DisplayMetrics.DENSITY_LOW / 72.0f); 
			}
			
			return (int)textSize;
		}

		@Override
		public void onRequestTemporaryError(MegaApiJava api,
				MegaRequest request, MegaError e) {
			log("onRequestTemporaryError()");
		}

		@Override
		public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public MegaContactsGridAdapter(Context _context, ArrayList<MegaUser> _contacts, ListView _listView) {
		this.context = _context;
		this.contacts = _contacts;
		this.positionClicked = -1;
		
		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}

		listFragment = _listView;
	}
	
	/*private view holder class*/
    private class ViewHolderContactsGrid {
        RoundedImageView imageView1;
        TextView contactInitialLetter1;
        TextView textViewContactName1;
        RelativeLayout itemLayout1;
        RoundedImageView imageView2;
        TextView contactInitialLetter2;
        TextView textViewContactName2;
        RelativeLayout itemLayout2;
        TextView textViewContent1;
        TextView textViewContent2;
        ImageButton imageButtonThreeDots1;
        ImageButton imageButtonThreeDots2;
//        ImageView arrowSelection1;
        RelativeLayout optionsLayout1;
        ImageButton optionProperties1;
        ImageButton optionShare1;
        ImageButton optionRemove1;
//        ImageView arrowSelection2;
        RelativeLayout optionsLayout2;
        ImageButton optionProperties2;
        ImageButton optionShare2;
        ImageButton optionRemove2;
        int currentPosition;
        String contactMail1;
        String contactMail2;
    }
    
    ViewHolderContactsGrid holder = null;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v;
		
//		listFragment.setVisibility(View.GONE);
	
		final int _position = position;
		
		Display display = ((Activity)context).getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    float density  = ((Activity)context).getResources().getDisplayMetrics().density;
		
	    float scaleW = Util.getScaleW(outMetrics, density);
	    float scaleH = Util.getScaleH(outMetrics, density);
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if ((position % 2) == 0){
			v = inflater.inflate(R.layout.item_contact_grid, parent, false);
			holder = new ViewHolderContactsGrid();
			holder.itemLayout1 = (RelativeLayout) v.findViewById(R.id.contact_grid_item_layout1);
			holder.itemLayout2 = (RelativeLayout) v.findViewById(R.id.contact_grid_item_layout2);
			
			//Set width and height itemLayout1
			RelativeLayout.LayoutParams paramsIL1 = new RelativeLayout.LayoutParams(Util.px2dp(172*scaleW, outMetrics),LayoutParams.WRAP_CONTENT);
			paramsIL1.setMargins(Util.px2dp(5*scaleW, outMetrics), Util.px2dp(5*scaleH, outMetrics), Util.px2dp(5*scaleW, outMetrics), 0);
			paramsIL1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			paramsIL1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			holder.itemLayout1.setLayoutParams(paramsIL1);
			
			//Set width and height itemLayout2
			RelativeLayout.LayoutParams paramsIL2 = new RelativeLayout.LayoutParams(Util.px2dp(172*scaleW, outMetrics),LayoutParams.WRAP_CONTENT);
			paramsIL2.setMargins(0, Util.px2dp(5*scaleH, outMetrics), Util.px2dp(5*scaleW, outMetrics), 0);
			paramsIL2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			paramsIL2.addRule(RelativeLayout.RIGHT_OF, R.id.contact_grid_item_layout1);
			paramsIL2.addRule(RelativeLayout.ALIGN_PARENT_TOP);

			holder.itemLayout2.setLayoutParams(paramsIL2);
			
			holder.imageView1 = (RoundedImageView) v.findViewById(R.id.contact_grid_thumbnail1);
			holder.imageView1.setCornerRadius(Util.px2dp(78*scaleW, outMetrics));
			holder.contactInitialLetter1 = (TextView) v.findViewById(R.id.contact_grid_initial_letter1);
            holder.imageView2 = (RoundedImageView) v.findViewById(R.id.contact_grid_thumbnail2);
			holder.imageView2.setCornerRadius(Util.px2dp(78*scaleW, outMetrics));
			holder.contactInitialLetter2 = (TextView) v.findViewById(R.id.contact_grid_initial_letter2);
//			holder.imageView1.setPadding(0, Util.px2dp(5*scaleH, outMetrics), 0, Util.px2dp(5*scaleH, outMetrics));
//			holder.imageView2.setPadding(0, Util.px2dp(5*scaleH, outMetrics), 0, Util.px2dp(5*scaleH, outMetrics));

			holder.imageView1.setTag(holder);
            holder.imageView1.setOnClickListener(this);
            
            holder.imageView2.setTag(holder);
            holder.imageView2.setOnClickListener(this);
                        
//			RelativeLayout.LayoutParams paramsIV1 = new RelativeLayout.LayoutParams(Util.px2dp(157*scaleW, outMetrics),Util.px2dp(157*scaleH, outMetrics));
//			paramsIV1.addRule(RelativeLayout.CENTER_HORIZONTAL);
//			holder.imageView1.setScaleType(ImageView.ScaleType.FIT_CENTER);
//			paramsIV1.setMargins(Util.px2dp(5*scaleW, outMetrics), Util.px2dp(5*scaleH, outMetrics), Util.px2dp(5*scaleW, outMetrics), 0);
//			holder.imageView1.setLayoutParams(paramsIV1);
			
//			RelativeLayout.LayoutParams paramsIV2 = new RelativeLayout.LayoutParams(Util.px2dp(157*scaleW, outMetrics),Util.px2dp(157*scaleH, outMetrics));
//			paramsIV2.addRule(RelativeLayout.CENTER_HORIZONTAL);
//			holder.imageView2.setScaleType(ImageView.ScaleType.FIT_CENTER);
//			paramsIV2.setMargins(0, Util.px2dp(5*scaleH, outMetrics), 0, 0);
//			holder.imageView2.setLayoutParams(paramsIV2);

			holder.textViewContactName1 = (TextView) v.findViewById(R.id.contact_grid_filename1);
			holder.textViewContactName1.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
			holder.textViewContactName1.getLayoutParams().width = Util.px2dp((125*scaleW), outMetrics);
			holder.textViewContactName1.setEllipsize(TextUtils.TruncateAt.END);
			holder.textViewContactName1.setSingleLine(true);
			holder.textViewContactName2 = (TextView) v.findViewById(R.id.contact_grid_filename2);
			holder.textViewContactName2.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;
			holder.textViewContactName2.getLayoutParams().width = Util.px2dp((125*scaleW), outMetrics);
			holder.textViewContactName2.setEllipsize(TextUtils.TruncateAt.END);
			holder.textViewContactName2.setSingleLine(true);
			
			holder.textViewContent1 = (TextView) v.findViewById(R.id.contact_grid_filesize1);
			holder.textViewContent2 = (TextView) v.findViewById(R.id.contact_grid_filesize2);
			
			holder.imageButtonThreeDots1 = (ImageButton) v.findViewById(R.id.contact_grid_three_dots1);
			holder.imageButtonThreeDots2 = (ImageButton) v.findViewById(R.id.contact_grid_three_dots2);
			
			holder.optionsLayout1 = (RelativeLayout) v.findViewById(R.id.contact_grid_options1);
			holder.optionProperties1 = (ImageButton) v.findViewById(R.id.contact_grid_option_properties1);
			holder.optionProperties1.setPadding(Util.px2dp((70*scaleW), outMetrics), Util.px2dp((15*scaleH), outMetrics), 0, 0);
			holder.optionShare1 = (ImageButton) v.findViewById(R.id.contact_grid_option_share1);
			holder.optionShare1.setPadding(Util.px2dp((70*scaleW), outMetrics), Util.px2dp((15*scaleH), outMetrics), 0, 0);
			holder.optionRemove1 = (ImageButton) v.findViewById(R.id.contact_grid_option_remove1);
			holder.optionRemove1.setPadding(Util.px2dp((70*scaleW), outMetrics), Util.px2dp((15*scaleH), outMetrics), Util.px2dp((30*scaleW), outMetrics), 0);
//			holder.arrowSelection1 = (ImageView) v.findViewById(R.id.contact_grid_arrow_selection1);
//			holder.arrowSelection1.setVisibility(View.GONE);

			holder.optionsLayout2 = (RelativeLayout) v.findViewById(R.id.contact_grid_options2);
			holder.optionProperties2 = (ImageButton) v.findViewById(R.id.contact_grid_option_properties2);
			holder.optionProperties2.setPadding(Util.px2dp((70*scaleW), outMetrics), Util.px2dp((15*scaleH), outMetrics), 0, 0);
			holder.optionShare2 = (ImageButton) v.findViewById(R.id.contact_grid_option_share2);
			holder.optionShare2.setPadding(Util.px2dp((70*scaleW), outMetrics), Util.px2dp((15*scaleH), outMetrics), 0, 0);
			holder.optionRemove2 = (ImageButton) v.findViewById(R.id.contact_grid_option_remove2);
			holder.optionRemove2.setPadding(Util.px2dp((70*scaleW), outMetrics), Util.px2dp((15*scaleH), outMetrics), Util.px2dp((50*scaleW), outMetrics), 0);
//			holder.arrowSelection2 = (ImageView) v.findViewById(R.id.contact_grid_arrow_selection2);
//			holder.arrowSelection2.setVisibility(View.GONE);
			
			v.setTag(holder);
			
			holder.currentPosition = position;

			MegaUser contact1 = (MegaUser) getItem(position);
			holder.contactMail1 = contact1.getEmail();
			holder.textViewContactName1.setText(contact1.getEmail());
			
			UserAvatarListenerGrid listener1 = new UserAvatarListenerGrid(context, holder, this, 1);
			
			File avatar1 = null;
			if (context.getExternalCacheDir() != null){
				avatar1 = new File(context.getExternalCacheDir().getAbsolutePath(), holder.contactMail1 + ".jpg");
			}
			else{
				avatar1 = new File(context.getCacheDir().getAbsolutePath(), holder.contactMail1 + ".jpg");
			}
			Bitmap bitmap1 = null;
			if (avatar1.exists()){
				if (avatar1.length() > 0){
					BitmapFactory.Options bOpts = new BitmapFactory.Options();
					bOpts.inPurgeable = true;
					bOpts.inInputShareable = true;
					bitmap1 = BitmapFactory.decodeFile(avatar1.getAbsolutePath(), bOpts);
					if (bitmap1 == null) {
						avatar1.delete();
						if (context.getExternalCacheDir() != null){
							megaApi.getUserAvatar(contact1, context.getExternalCacheDir().getAbsolutePath() + "/" + contact1.getEmail() + ".jpg", listener1);
						}
						else{
							megaApi.getUserAvatar(contact1, context.getCacheDir().getAbsolutePath() + "/" + contact1.getEmail() + ".jpg", listener1);
						}
					}
					else{
						holder.imageView1.setImageBitmap(bitmap1);
					}
				}
				else{
					megaApi.getUserAvatar(contact1, context.getCacheDir().getAbsolutePath() + "/" + contact1.getEmail() + ".jpg", listener1);
				}
			}
			else{
				if (context.getExternalCacheDir() != null){
					megaApi.getUserAvatar(contact1, context.getExternalCacheDir().getAbsolutePath() + "/" + contact1.getEmail() + ".jpg", listener1);
				}
				else{
					megaApi.getUserAvatar(contact1, context.getCacheDir().getAbsolutePath() + "/" + contact1.getEmail() + ".jpg", listener1);
				}
			}
			
			ArrayList<MegaNode> nodes1 = megaApi.getInShares(contact1);
			holder.textViewContent1.setText(getDescription(nodes1));
			
			MegaUser contact2;
			if (position < (getCount()-1)){
				contact2 = (MegaUser) getItem(position+1);
				holder.contactMail2 = contact2.getEmail();
				holder.textViewContactName2.setText(contact2.getEmail());
				
				UserAvatarListenerGrid listener2 = new UserAvatarListenerGrid(context, holder, this, 2);
				
				File avatar2 = null;
				if (context.getExternalCacheDir() != null){
					avatar2 = new File(context.getExternalCacheDir().getAbsolutePath(), holder.contactMail2 + ".jpg");
				}
				else{
					avatar2 = new File(context.getCacheDir().getAbsolutePath(), holder.contactMail2 + ".jpg");
				}
				Bitmap bitmap2 = null;
				if (avatar2.exists()){
					if (avatar2.length() > 0){
						BitmapFactory.Options bOpts = new BitmapFactory.Options();
						bOpts.inPurgeable = true;
						bOpts.inInputShareable = true;
						bitmap2 = BitmapFactory.decodeFile(avatar2.getAbsolutePath(), bOpts);
						if (bitmap2 == null) {
							avatar2.delete();
							if (context.getExternalCacheDir() != null){
								megaApi.getUserAvatar(contact2, context.getExternalCacheDir().getAbsolutePath() + "/" + contact2.getEmail() + ".jpg", listener2);
							}
							else{
								megaApi.getUserAvatar(contact2, context.getCacheDir().getAbsolutePath() + "/" + contact2.getEmail() + ".jpg", listener2);
							}
						}
						else{
							holder.imageView2.setImageBitmap(bitmap2);
						}
					}
					else{
						if (context.getExternalCacheDir() != null){
							megaApi.getUserAvatar(contact2, context.getExternalCacheDir().getAbsolutePath() + "/" + contact2.getEmail() + ".jpg", listener2);
						}
						else{
							megaApi.getUserAvatar(contact2, context.getCacheDir().getAbsolutePath() + "/" + contact2.getEmail() + ".jpg", listener2);
						}
					}
				}
				else{
					if (context.getExternalCacheDir() != null){
						megaApi.getUserAvatar(contact2, context.getExternalCacheDir().getAbsolutePath() + "/" + contact2.getEmail() + ".jpg", listener2);
					}
					else{
						megaApi.getUserAvatar(contact2, context.getCacheDir().getAbsolutePath() + "/" + contact2.getEmail() + ".jpg", listener2);
					}
				}
				
				ArrayList<MegaNode> nodes2 = megaApi.getInShares(contact2);
				holder.textViewContent2.setText(getDescription(nodes2));
				
				holder.itemLayout2.setVisibility(View.VISIBLE);
			}
			else{
				holder.itemLayout2.setVisibility(View.GONE);
			}
			
			holder.imageButtonThreeDots1.setTag(holder);
			holder.imageButtonThreeDots1.setOnClickListener(this);
			
			holder.imageButtonThreeDots2.setTag(holder);
			holder.imageButtonThreeDots2.setOnClickListener(this);
			
			if (positionClicked != -1){
				if (positionClicked == position){
//					holder.arrowSelection1.setVisibility(View.VISIBLE);
					LayoutParams params = holder.optionsLayout1.getLayoutParams();
					params.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, context.getResources().getDisplayMetrics());
					ListView list = (ListView) parent;
					list.smoothScrollToPosition(_position);
//					holder.arrowSelection2.setVisibility(View.GONE);
					LayoutParams params2 = holder.optionsLayout2.getLayoutParams();
					params2.height = 0;
				}
				else if (positionClicked == (position+1)){
//					holder.arrowSelection2.setVisibility(View.VISIBLE);
					LayoutParams params = holder.optionsLayout2.getLayoutParams();
					params.height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, context.getResources().getDisplayMetrics());
					ListView list = (ListView) parent;
					list.smoothScrollToPosition(_position);
//					holder.arrowSelection1.setVisibility(View.GONE);
					LayoutParams params1 = holder.optionsLayout1.getLayoutParams();
					params1.height = 0;
				}
				else{
//					holder.arrowSelection1.setVisibility(View.GONE);
					LayoutParams params1 = holder.optionsLayout1.getLayoutParams();
					params1.height = 0;
					
//					holder.arrowSelection2.setVisibility(View.GONE);
					LayoutParams params2 = holder.optionsLayout2.getLayoutParams();
					params2.height = 0;
				}
			}
			else{
//				holder.arrowSelection1.setVisibility(View.GONE);
				LayoutParams params1 = holder.optionsLayout1.getLayoutParams();
				params1.height = 0;
				
//				holder.arrowSelection2.setVisibility(View.GONE);
				LayoutParams params2 = holder.optionsLayout2.getLayoutParams();
				params2.height = 0;
			}
			
			holder.optionProperties1.setTag(holder);
			holder.optionProperties1.setOnClickListener(this);			
			holder.optionProperties2.setTag(holder);
			holder.optionProperties2.setOnClickListener(this);
			
			holder.optionShare1.setTag(holder);
			holder.optionShare1.setOnClickListener(this);	
			holder.optionShare2.setTag(holder);
			holder.optionShare2.setOnClickListener(this);	
			
			holder.optionRemove1.setTag(holder);
			holder.optionRemove1.setOnClickListener(this);	
			holder.optionRemove2.setTag(holder);
			holder.optionRemove2.setOnClickListener(this);
		}
		else{
			v = inflater.inflate(R.layout.item_file_empty_grid, parent, false);
		}
		
		return v;
	}

	@Override
    public int getCount() {		
        return contacts.size();
    }
 
    @Override
    public Object getItem(int position) {
        return contacts.get(position);
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }    
    
    public int getPositionClicked (){
    	return positionClicked;
    }
    
    public void setPositionClicked(int p){
    	positionClicked = p;
    }

	@Override
	public void onClick(View v) {
		ViewHolderContactsGrid holder = (ViewHolderContactsGrid) v.getTag();
		int currentPosition = holder.currentPosition;

		switch (v.getId()){
			case R.id.contact_grid_option_properties1:{
				MegaUser c = (MegaUser) getItem(currentPosition);
				Intent i = new Intent(context, ContactPropertiesMainActivity.class);
				i.putExtra("name", c.getEmail());
				context.startActivity(i);							
				positionClicked = -1;
				notifyDataSetChanged();
				break;
			}
			case R.id.contact_grid_option_properties2:{
				MegaUser c = (MegaUser) getItem(currentPosition+1);
				Intent i = new Intent(context, ContactPropertiesMainActivity.class);
				i.putExtra("name", c.getEmail());
				context.startActivity(i);							
				positionClicked = -1;
				notifyDataSetChanged();
				break;
			}
			case R.id.contact_grid_option_share1:{
				MegaUser c = (MegaUser) getItem(currentPosition);
				List<MegaUser> user = new ArrayList<MegaUser>();
				user.add(c);
				((ManagerActivity) context).pickFolderToShare(user);
				notifyDataSetChanged();				
				break;
			}
			case R.id.contact_grid_option_share2:{
				MegaUser c = (MegaUser) getItem(currentPosition+1);
				List<MegaUser> user = new ArrayList<MegaUser>();
				user.add(c);
				((ManagerActivity) context).pickFolderToShare(user);
				notifyDataSetChanged();				
				break;
			}
			case R.id.contact_grid_option_remove1:{
				MegaUser c = (MegaUser) getItem(currentPosition);
				((ManagerActivity) context).removeContact(c);
				notifyDataSetChanged();	
				break;
			}
			case R.id.contact_grid_option_remove2:{
				MegaUser c = (MegaUser) getItem(currentPosition+1);
				((ManagerActivity) context).removeContact(c);
				notifyDataSetChanged();			
				break;
			}
			case R.id.contact_grid_three_dots1:{
				if (positionClicked == -1){
					positionClicked = currentPosition;
					notifyDataSetChanged();
				}
				else{
					if (positionClicked == currentPosition){
						positionClicked = -1;
						notifyDataSetChanged();
					}
					else{
						positionClicked = currentPosition;
						notifyDataSetChanged();
					}
				}
				break;
			}
			case R.id.contact_grid_three_dots2:{
				if (positionClicked == -1){
					positionClicked = currentPosition+1;
					notifyDataSetChanged();
				}
				else{
					if (positionClicked == (currentPosition+1)){
						positionClicked = -1;
						notifyDataSetChanged();
					}
					else{
						positionClicked = currentPosition+1;
						notifyDataSetChanged();
					}
				}
				break;
			}
			case R.id.contact_grid_thumbnail1:{
				Intent i = new Intent(context, ContactPropertiesMainActivity.class);
				MegaUser contact = (MegaUser) getItem(currentPosition);
				i.putExtra("name", contact.getEmail());
				context.startActivity(i);
				break;
			}
			case R.id.contact_grid_thumbnail2:{
				Intent i = new Intent(context, ContactPropertiesMainActivity.class);
				MegaUser contact = (MegaUser) getItem(currentPosition+1);
				i.putExtra("name", contact.getEmail());
				context.startActivity(i);
				break;
			}
		}
	}
	
	public void setContacts (ArrayList<MegaUser> contacts){
		this.contacts = contacts;
		positionClicked = -1;
		notifyDataSetChanged();
	}
	
	public String getDescription(ArrayList<MegaNode> nodes){
		int numFolders = 0;
		int numFiles = 0;
		
		for (int i=0;i<nodes.size();i++){
			MegaNode c = nodes.get(i);
			if (c.isFolder()){
				numFolders++;
			}
			else{
				numFiles++;
			}
		}
		
		String info = "";
		if (numFolders > 0){
			info = numFolders +  " " + context.getResources().getQuantityString(R.plurals.general_num_folders, numFolders);
			if (numFiles > 0){
				info = info + ", " + numFiles + " " + context.getResources().getQuantityString(R.plurals.general_num_files, numFiles);
			}
		}
		else {
			if (numFiles == 0){
				info = numFiles +  " " + context.getResources().getQuantityString(R.plurals.general_num_folders, numFolders);
			}
			else{
				info = numFiles +  " " + context.getResources().getQuantityString(R.plurals.general_num_files, numFiles);
			}
		}
		
		return info;
	}
	
	public boolean isMultipleSelect() {
		return multipleSelect;
	}
	
	private static void log(String log) {
		Util.log("MegaContactsGridAdapter", log);
	}
}
