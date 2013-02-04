package com.app.mywifscan;

import java.util.List;

import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.net.wifi.*;
public class MainActivity extends Activity implements OnInitListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button startBtn=(Button)findViewById(R.id.btnStart);
		
		startBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try
				{
					//while(true)
					{
						ScanWiFiNetworks();
						//Thread.sleep(2000);
					}
				}
				catch(Exception e)
				{
					
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	private void ScanWiFiNetworks()
	{
		String strLevel="";
		WifiManager myWifiManager=(WifiManager) getSystemService(Context.WIFI_SERVICE);
		int max=-1;
		if(myWifiManager.isWifiEnabled()){                
		     if(myWifiManager.startScan()){
		         // List available APs
		         List<ScanResult> scans = myWifiManager.getScanResults();
		         if(scans != null && !scans.isEmpty()){
		         for (ScanResult scan : scans) 
		         {
		        	 int level = WifiManager.calculateSignalLevel(scan.level, 100);
		        	 strLevel = strLevel + scan.SSID + " : " + level + "\n";
		        	 if(scan.SSID.equalsIgnoreCase("USC Wireless"))		        		 
		        	 {	 
		        		 if(level>max)
		        		 {
		        			 max=level;
		        		 }		        		 	
		        	 }
		         }
		         }
		     }
		}
		EditText edt=(EditText)findViewById(R.id.editText1);
	 	edt.setText("" + max);
	 	
		
	}

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		
	}

}
