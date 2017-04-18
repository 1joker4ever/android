package mega.privacy.android.app.lollipop.managerSections;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.ListIterator;

import mega.privacy.android.app.AndroidCompletedTransfer;
import mega.privacy.android.app.DatabaseHandler;
import mega.privacy.android.app.MegaApplication;
import mega.privacy.android.app.R;
import mega.privacy.android.app.components.SimpleDividerItemDecoration;
import mega.privacy.android.app.lollipop.ManagerActivityLollipop;
import mega.privacy.android.app.lollipop.adapters.MegaCompletedTransfersAdapter;
import mega.privacy.android.app.lollipop.adapters.MegaTransfersLollipopAdapter;
import mega.privacy.android.app.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaTransfer;


public class CompletedTransfersFragmentLollipop extends Fragment {

	Context context;
	ActionBar aB;
	RecyclerView listView;
	MegaCompletedTransfersAdapter adapter;
	
	MegaApiAndroid megaApi;
	TextView contentText;
	ImageView emptyImage;
	TextView emptyText;

	float density;
	DisplayMetrics outMetrics;
	Display display;
	
	LinearLayoutManager mLayoutManager;

	DatabaseHandler dbH;
	
	CompletedTransfersFragmentLollipop transfersFragment = this;
	
//	SparseArray<TransfersHolder> transfersListArray = null;

	ArrayList<AndroidCompletedTransfer> tL = null;

	private Handler handler;
	
	@Override
	public void onCreate (Bundle savedInstanceState){
		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}

		tL = new ArrayList<AndroidCompletedTransfer>();
		dbH = DatabaseHandler.getDbHandler(context);

		super.onCreate(savedInstanceState);
		log("onCreate");		
	}

	public static CompletedTransfersFragmentLollipop newInstance() {
		log("newInstance");
		CompletedTransfersFragmentLollipop fragment = new CompletedTransfersFragmentLollipop();
		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {  
		
		super.onCreateView(inflater, container, savedInstanceState);
		
		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}
		
		if (aB == null){
			aB = ((AppCompatActivity)context).getSupportActionBar();
		}
		
		aB.setTitle(getResources().getString(R.string.section_transfers));
		aB.setHomeAsUpIndicator(R.drawable.ic_menu_white);
		((ManagerActivityLollipop)context).setFirstNavigationLevel(true);
		((ManagerActivityLollipop)context).supportInvalidateOptionsMenu();
//		aB.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
		
		display = ((Activity)context).getWindowManager().getDefaultDisplay();
		outMetrics = new DisplayMetrics ();
	    display.getMetrics(outMetrics);
	    density  = getResources().getDisplayMetrics().density;
	    
		View v = inflater.inflate(R.layout.fragment_transfers, container, false);

		listView = (RecyclerView) v.findViewById(R.id.transfers_list_view);
		listView.addItemDecoration(new SimpleDividerItemDecoration(context, outMetrics));
		mLayoutManager = new LinearLayoutManager(context);
		listView.setLayoutManager(mLayoutManager);
		listView.setHasFixedSize(true);
		listView.setItemAnimator(new DefaultItemAnimator());
		mLayoutManager.setReverseLayout(true);

		emptyImage = (ImageView) v.findViewById(R.id.transfers_empty_image);
		emptyText = (TextView) v.findViewById(R.id.transfers_empty_text);

		emptyImage.setImageResource(R.drawable.ic_no_active_transfers);
		emptyText.setText(getString(R.string.transfers_empty));

		setCompletedTransfers();

		adapter = new MegaCompletedTransfersAdapter(context, this, tL, listView);

		listView.setAdapter(adapter);

		return v;
	}

	public void setCompletedTransfers(){
		log("setCompletedTransfers");

		tL = dbH.getCompletedTransfers();

		if(tL!=null){
			if (tL.size() == 0){
				emptyImage.setVisibility(View.VISIBLE);
				emptyText.setVisibility(View.VISIBLE);
				listView.setVisibility(View.GONE);
			}
			else{
				emptyImage.setVisibility(View.GONE);
				emptyText.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
			}
		}
		else{
			emptyImage.setVisibility(View.VISIBLE);
			emptyText.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		}
	}

	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
        aB = ((AppCompatActivity)activity).getSupportActionBar();
    }

	public int onBackPressed(){
		
		if (adapter == null){
			return 0;
		}
		
		if (adapter.getPositionClicked() != -1){
			adapter.setPositionClicked(-1);
			adapter.notifyDataSetChanged();
			return 1;
		}
		else{
			return 0;
		}
	}

    public void transferFinish(AndroidCompletedTransfer transfer){
		log("transferFinish");
		tL.add(transfer);

		if (tL.size() == 0){
			emptyImage.setVisibility(View.VISIBLE);
			emptyText.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		}
		else{
			emptyImage.setVisibility(View.GONE);
			emptyText.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}
		adapter.notifyDataSetChanged();
	}

	private static void log(String log) {
		Util.log("CompletedTransfersFragmentLollipop", log);
	}

}
