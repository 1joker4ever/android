package com.mega.android;

import java.util.ArrayList;
import java.util.List;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader.TileMode;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ManagerActivity extends ActionBarActivity implements OnItemClickListener, OnClickListener {
	
	public enum DrawerItem {
		CLOUD_DRIVE, SAVED_FOR_OFFLINE, SHARED_WITH_ME, RUBBISH_BIN, CONTACTS, IMAGE_VIEWER, TRANSFERS, ACCOUNT;

		public String getTitle(Context context) {
			switch(this)
			{
				case CLOUD_DRIVE: return context.getString(R.string.section_cloud_drive);
				case SAVED_FOR_OFFLINE: return context.getString(R.string.section_saved_for_offline);
				case SHARED_WITH_ME: return context.getString(R.string.section_shared_with_me);
				case RUBBISH_BIN: return context.getString(R.string.section_rubbish_bin);
				case CONTACTS: return context.getString(R.string.section_contacts);
				case IMAGE_VIEWER: return context.getString(R.string.section_image_viewer);
				case TRANSFERS: return context.getString(R.string.section_transfers);
				case ACCOUNT: return context.getString(R.string.section_account);
			}
			return null;
		}
	}
	
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    
	private MenuItem searchMenuItem;
	
	private static DrawerItem drawerItem;
	private static DrawerItem lastDrawerItem;
	
	ImageButton customListGrid;

	private boolean firstTime = true;
	
	private boolean isListCloudDrive = true;
	private boolean isListContacts = true;
    private FileBrowserListFragment fbL;
    private FileBrowserGridFragment fbG;
    private ContactsListFragment cL;
    private ContactsGridFragment cG;
    


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manager);	
				
		ImageView imageProfile = (ImageView) findViewById(R.id.profile_photo);
		Bitmap imBitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.jesus);
		Bitmap circleBitmap = Bitmap.createBitmap(imBitmap.getWidth(), imBitmap.getHeight(), Bitmap.Config.ARGB_8888);

		BitmapShader shader = new BitmapShader (imBitmap,  TileMode.CLAMP, TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setShader(shader);

        Canvas c = new Canvas(circleBitmap);
        int radius; 
        if (imBitmap.getWidth() < imBitmap.getHeight())
        	radius = imBitmap.getWidth()/2;
        else
        	radius = imBitmap.getHeight()/2;
        
	    c.drawCircle(imBitmap.getWidth()/2, imBitmap.getHeight()/2, radius, paint);
        imageProfile.setImageBitmap(circleBitmap);
		
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer_list);
        
        TextView used_space = (TextView) findViewById(R.id.used_space);
        String used = "11";
        String total = "50";
        
        String used_space_string = getString(R.string.used_space, used, total);
        used_space.setText(used_space_string);
        
        Spannable wordtoSpan = new SpannableString(used_space_string);        

        wordtoSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.used_space_OK)), 0, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        wordtoSpan.setSpan(new RelativeSizeSpan(1.5f), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        wordtoSpan.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.navigation_drawer_mail)), 6, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        wordtoSpan.setSpan(new RelativeSizeSpan(1.5f), 9, 11, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        used_space.setText(wordtoSpan);
        
        List<String> items = new ArrayList<String>();
		for (DrawerItem item : DrawerItem.values()) {
			items.add(item.getTitle(this));
		}
        
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				R.layout.drawer_list_item, items)
				{
					public View getView(int position, View rowView, ViewGroup parentView) {
						TextView view = (TextView)super.getView(position, rowView, parentView);
						switch(position)
						{
						case 0:
							view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_contacts,0,0,0);
							break;
						case 1:
							view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_contacts,0,0,0);
							break;
						case 2:
							view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_contacts,0,0,0);
							break;
						case 3:
							view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_contacts,0,0,0);
							break;
						case 4:
							view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_contacts,0,0,0);
							break;
						case 5:
							view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_contacts,0,0,0);
							break;
						case 6:
							view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_contacts,0,0,0);
							break;
						case 7:
							view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_contacts,0,0,0);
							break;
						}
						return view;
					}
				}
				
				);
        
        mDrawerList.setOnItemClickListener(this);
        
        getSupportActionBar().setIcon(R.drawable.ic_launcher);
        getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.app_name,  /* "open drawer" description for accessibility */
                R.string.app_name  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                supportInvalidateOptionsMenu();	// creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        if (savedInstanceState == null){
        	mDrawerLayout.openDrawer(Gravity.LEFT);
        }
        else{
			mDrawerLayout.closeDrawer(Gravity.LEFT);
        }
        
        //Create the actionBar Menu
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.custom_action_bar_top);
        ImageButton customSearch = (ImageButton) getSupportActionBar().getCustomView().findViewById(R.id.menu_action_bar_search);
		customSearch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (searchMenuItem != null) {
					MenuItemCompat.expandActionView(searchMenuItem);
				}
			}
		}); 
		
		customListGrid = (ImageButton) getSupportActionBar().getCustomView().findViewById(R.id.menu_action_bar_grid);
		customListGrid.setOnClickListener(this);
		
		if (drawerItem == null) {
			drawerItem = DrawerItem.CLOUD_DRIVE;
		}

		//INITIAL FRAGMENT
		selectDrawerItem(drawerItem);
	}
    
    public void selectDrawerItem(DrawerItem item){
    	switch (item){
    		case CLOUD_DRIVE:{
    			   			
    			if (fbG == null){
    				fbG = new FileBrowserGridFragment();
    			}
    			if (fbL == null){
    				fbL = new FileBrowserListFragment();
    			}
    			if (isListCloudDrive){
    				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fbL).commit();
    				customListGrid.setImageResource(R.drawable.ic_menu_action_grid);
    			}
    			else{
    				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fbG).commit();
    				customListGrid.setImageResource(R.drawable.ic_menu_action_list);
    			}
    			
    			if (!firstTime){
    				mDrawerLayout.closeDrawer(Gravity.LEFT);
    			}
    			else{
    				firstTime = false;
    			}
    			
    			break;
    		}
    		case CONTACTS:{
    			
    			if (cG == null){
    				cG = new ContactsGridFragment();
    			}
    			if (cL == null){
    				cL = new ContactsListFragment();
    			}
    			if (isListContacts){
    				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, cL).commit();
    				customListGrid.setImageResource(R.drawable.ic_menu_action_grid);
    			}
    			else{
    				getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, cG).commit();
    				customListGrid.setImageResource(R.drawable.ic_menu_action_list);
    			}
				
    			mDrawerLayout.closeDrawer(Gravity.LEFT);

    			break;
    		}
    	}
    }
	
	@Override
	public void onBackPressed() {
		
		if (fbL.isVisible()){
			if (fbL.onBackPressed() == 0){
				super.onBackPressed();
				return;
			}
		}
		else if (fbG.isVisible()){
			if (fbG.onBackPressed() == 0){
				super.onBackPressed();
				return;
			}
		}
		else if (cL.isVisible()){
			if (cL.onBackPressed() == 0){
				super.onBackPressed();
				return;
			}
		}
		else if (cG.isVisible()){
			if (cG.onBackPressed() == 0){
				super.onBackPressed();
				return;
			}
		}
	}


	@Override
	public void onPostCreate(Bundle savedInstanceState){
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.activity_manager, menu);
	    
	    searchMenuItem = menu.findItem(R.id.action_search);
	    searchMenuItem.setVisible(false);
	    final SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
	    
	    if (searchView != null) {
			searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
			searchView.setIconifiedByDefault(true);
		}
	    	    
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		// Handle presses on the action bar items
	    switch (item.getItemId()) {
		    case android.R.id.home:
		    case R.id.home:
		    case R.id.homeAsUp:
	    	//case 16908332: //Algo pasa con la CyanogenMod
		    	if (mDrawerToggle.isDrawerIndicatorEnabled()) {
					mDrawerToggle.onOptionsItemSelected(item);
				}
		    	return true;
	        case R.id.action_settings:
	        	Toast.makeText(this,  "Icono de preferencias clickado", Toast.LENGTH_SHORT).show();
	            return true;
            default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		selectDrawerItem(DrawerItem.values()[position]);
	}

	@Override
	public void onClick(View v) {

		switch(v.getId()){
			case R.id.menu_action_bar_grid:{
				if (fbL.isVisible() || fbG.isVisible()){
					if (isListCloudDrive){
						getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fbG).commit();
						ImageButton customListGrid = (ImageButton)getSupportActionBar().getCustomView().findViewById(R.id.menu_action_bar_grid);
						customListGrid.setImageResource(R.drawable.ic_menu_action_list);
						isListCloudDrive = false;
					}
					else{
						getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fbL).commit();
						ImageButton customListGrid = (ImageButton)getSupportActionBar().getCustomView().findViewById(R.id.menu_action_bar_grid);
						customListGrid.setImageResource(R.drawable.ic_menu_action_grid);
				        isListCloudDrive = true;					
					}
				}
				else if (cL.isVisible() || cG.isVisible()){
					if (isListContacts){
						getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, cG).commit();
						ImageButton customListGrid = (ImageButton)getSupportActionBar().getCustomView().findViewById(R.id.menu_action_bar_grid);
						customListGrid.setImageResource(R.drawable.ic_menu_action_list);
						isListContacts = false;
					}
					else{
						getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, cL).commit();
						ImageButton customListGrid = (ImageButton)getSupportActionBar().getCustomView().findViewById(R.id.menu_action_bar_grid);
						customListGrid.setImageResource(R.drawable.ic_menu_action_grid);
						isListContacts = true;					
					}
				}
				break;
			}
		}
	}
}
