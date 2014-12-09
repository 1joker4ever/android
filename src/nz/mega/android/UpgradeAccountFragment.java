package nz.mega.android;

import java.text.DecimalFormat;

import nz.mega.android.utils.Util;
import nz.mega.sdk.MegaApiAndroid;
import nz.mega.sdk.MegaApiJava;
import nz.mega.sdk.MegaError;
import nz.mega.sdk.MegaPricing;
import nz.mega.sdk.MegaRequest;
import nz.mega.sdk.MegaRequestListenerInterface;
import nz.mega.sdk.MegaUser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class UpgradeAccountFragment extends Fragment implements MegaRequestListenerInterface{
	
	public static int MY_ACCOUNT_FRAGMENT = 5000;
	public static int UPGRADE_ACCOUNT_FRAGMENT = 5001;
	public static int PAYMENT_FRAGMENT = 5002;
	
	private ActionBar aB;
	private MegaApiAndroid megaApi;
	private TextView storage1;
	private TextView bandwidth1;
	private TextView pricingPerMonth1;
	private TextView storage2;
	private TextView bandwidth2;
	private TextView pricingPerMonth2;
	private TextView storage3;
	private TextView bandwidth3;
	private TextView pricingPerMonth3;
	private ImageView pro1;
	private ImageView pro2;
	private ImageView pro3;
	private Fragment selectMembership;
	Context context;
	MegaUser myUser;
	
	@Override
	public void onCreate (Bundle savedInstanceState){
		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}

		super.onCreate(savedInstanceState);
		log("onCreate");
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (megaApi == null){
			megaApi = ((MegaApplication) ((Activity)context).getApplication()).getMegaApi();
		}

		if (aB == null){
			aB = ((ActionBarActivity)context).getSupportActionBar();
		}
		
		aB.setTitle(R.string.section_account);
		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
		DisplayMetrics outMetrics = new DisplayMetrics();
		display.getMetrics(outMetrics);
		float density = ((Activity) context).getResources().getDisplayMetrics().density;

		float scaleW = Util.getScaleW(outMetrics, density);
		float scaleH = Util.getScaleH(outMetrics, density);

		View v = null;
		v = inflater.inflate(R.layout.activity_upgrade, container, false);
		
		pro1 = (ImageView) v.findViewById(R.id.pro1_image);
		pro2 = (ImageView) v.findViewById(R.id.pro2_image);
		pro3 = (ImageView) v.findViewById(R.id.pro3_image);
		
	
		pro1.getLayoutParams().width = Util.px2dp((100*scaleW), outMetrics);
		pro1.getLayoutParams().height = Util.px2dp((100*scaleW), outMetrics);
		pro2.getLayoutParams().width = Util.px2dp((100*scaleW), outMetrics);
		pro2.getLayoutParams().height = Util.px2dp((100*scaleW), outMetrics);
		pro3.getLayoutParams().width = Util.px2dp((100*scaleW), outMetrics);
		pro3.getLayoutParams().height = Util.px2dp((100*scaleW), outMetrics);
		
		storage1 = (TextView) v.findViewById(R.id.pro1_storage);
		storage2 = (TextView) v.findViewById(R.id.pro2_storage);
		storage3 = (TextView) v.findViewById(R.id.pro3_storage);
		
		bandwidth1 = (TextView) v.findViewById(R.id.pro1_bandwidth);
		bandwidth2 = (TextView) v.findViewById(R.id.pro2_bandwidth);
		bandwidth3 = (TextView) v.findViewById(R.id.pro3_bandwidth);
		
		pricingPerMonth1 = (TextView) v.findViewById(R.id.pricing1_from);
		pricingPerMonth2 = (TextView) v.findViewById(R.id.pricing2_from);
		pricingPerMonth3 = (TextView) v.findViewById(R.id.pricing3_from);
		
		megaApi.getAccountDetails(this);
		megaApi.getPricing(this);
		
		return v;
	}	

	public void onUpgrade1Click(View view) {
		((ManagerActivity)context).showpF(1);
	}

	public void onUpgrade2Click(View view) {
		((ManagerActivity)context).showpF(2);
	}

	public void onUpgrade3Click(View view) {
		((ManagerActivity)context).showpF(3);
	}

	@Override
	public void onRequestStart(MegaApiJava api, MegaRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRequestUpdate(MegaApiJava api, MegaRequest request) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRequestFinish(MegaApiJava api, MegaRequest request,MegaError e) {
		 
		if (request.getType() == MegaRequest.TYPE_GET_PRICING){
	            MegaPricing p = request.getPricing();
	            
	            for (int i=0;i<p.getNumProducts();i++){
	                log("p["+ i +"] = " + p.getHandle(i) + "__" + p.getAmount(i) + "___" + p.getGBStorage(i) + "___" + p.getMonths(i) + "___" + p.getProLevel(i) + "___" + p.getGBTransfer(i)); 
	                
	            }    
	            /*RESULTS
	            p[0] = 1560943707714440503__999___500___1___1___1024 - PRO 1 montly
        		p[1] = 7472683699866478542__9999___500___12___1___12288 - PRO 1 annually
        		p[2] = 7974113413762509455__1999___2048___1___2___4096  - PRO 2 montly
        		p[3] = 370834413380951543__19999___2048___12___2___49152 - PRO 2 annually
        		p[4] = -2499193043825823892__2999___4096___1___3___8192 - PRO 3 montly
        		p[5] = 7225413476571973499__29999___4096___12___3___98304 - PRO 3 annually*/
	                       	            
	            storage1.setText(p.getGBStorage(1)+"GB");
	            storage2.setText(sizeTranslation(p.getGBStorage(3),0));
	            storage3.setText(sizeTranslation(p.getGBStorage(5),0));           
	            	            	            
	            bandwidth1.setText(sizeTranslation(p.getGBTransfer(0)*12,0));
	            bandwidth2.setText(sizeTranslation(p.getGBTransfer(2)*12,0));
	            bandwidth3.setText(sizeTranslation(p.getGBTransfer(4)*12,0));
	            
	            double saving1 = p.getAmount(1)/12.00/100.00;
	            DecimalFormat df = new DecimalFormat("#.##");
	            String saving1String =df.format(saving1);
	            
	            double saving2 = p.getAmount(3)/12.00/100.00;
	            String saving2String =df.format(saving2);
	            
	            double saving3 = p.getAmount(5)/12.00/100.00;
	            String saving3String =df.format(saving3);
	            
	            pricingPerMonth1.setText("from " + saving1String +" € per month");
	            pricingPerMonth2.setText("from " + saving2String +" € per month");
	            pricingPerMonth3.setText("from " + saving3String +" € per month");
	        }
	        else if (request.getType() == MegaRequest.TYPE_GET_PAYMENT_URL){
	            log("PAYMENT URL: " + request.getLink());
	            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(request.getLink()));
	            startActivity(browserIntent);
	            
	        }
	}
	
	public String sizeTranslation(long size, int type) {
		switch(type){
			case 0:{
				//From GB to TB
				if(size!=1024){
					size=size/1024;
				}
								
				String value = new DecimalFormat("#").format(size) + "TB";			
				return value;
			}
		}
		return null;
	      
	}

	@Override
	public void onRequestTemporaryError(MegaApiJava api, MegaRequest request,
			MegaError e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		context = activity;
		aB = ((ActionBarActivity)activity).getSupportActionBar();
	}
	
	public static void log(String log) {
		Util.log("UpgradeAccountFragment", log);
	}
}
