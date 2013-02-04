package com.app.wifireciever;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnInitListener {
private int MY_DATA_CHECK_CODE = 0;
String TransmitterResult;
String RecevierResult;
Hashtable<String, String>myHashtable=new Hashtable<String, String>();
Hashtable<String, String>wifiNetworks=new Hashtable<String, String>();
TextView myBox;
Hashtable<String, Integer> oldDelta = new Hashtable<String, Integer>();
Hashtable<String, Integer> newDelta = new Hashtable<String, Integer>();
TextToSpeech tts;
private Button startButton;
boolean reached=false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startButton=(Button)findViewById(R.id.StartBtn);
		myBox = (TextView)findViewById(R.id.textView1);
		startButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Character currStep=null;
				Character[] lastThreeSteps=new Character[3];
				lastThreeSteps[0]='A';
				lastThreeSteps[1]='B';
				lastThreeSteps[2]='C';				
						
				Recieve();		
				while(!reached)
				{
					ScanWiFiNetworks();
					currStep=CompareResults();
					GiveInstruction(currStep);
					lastThreeSteps[0]=lastThreeSteps[1];
					lastThreeSteps[1]=lastThreeSteps[2];
					lastThreeSteps[2]=currStep;
					try {
						Thread.sleep(7000);
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					}
					if(lastThreeSteps[0].equals('F')
							&& lastThreeSteps[1].equals('F')
							&& lastThreeSteps[2].equals('B'))
					{
						currStep='L';
						GiveInstruction(currStep);
						lastThreeSteps[0]=lastThreeSteps[1];
						lastThreeSteps[1]=lastThreeSteps[2];
						lastThreeSteps[2]=currStep;
					}
				}
				
			}
			
		});
				
		 Intent checkIntent = new Intent();
	     checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
	     startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	private void GenerateSenderHashTable()
	{
		String[] allNetworks = TransmitterResult.split("\n");				
		for(String current : allNetworks)
		{
			wifiNetworks.put(current.split(":")[0],current.split(":")[1]);			
		}		
	}
	
	private void Recieve()
	{
		TransmitterResult="USC Wireless:98";
		GenerateSenderHashTable();
	}
	
	private void GiveInstruction(Character step)
	{
		switch(step)
		{
		case 'B':
			tts.speak("Go one step backward", TextToSpeech.QUEUE_ADD, null);
			break;
		case 'L':
			tts.speak("Go one step to the left", TextToSpeech.QUEUE_ADD, null);
			break;
		case 'F':
			tts.speak("Go one step Forward", TextToSpeech.QUEUE_ADD, null);
			break;
		case 'R':
			tts.speak("Go one step to the Right", TextToSpeech.QUEUE_ADD, null);
			break;
		case 'S':
			tts.speak("You have reached your Destination", TextToSpeech.QUEUE_ADD, null);
			break;
		default:
			tts.speak("Go one step Forward", TextToSpeech.QUEUE_ADD, null);
		}
	}
	
	private void ScanWiFiNetworks()
	{
		String strLevel="";
		WifiManager myWifiManager=(WifiManager) getSystemService(Context.WIFI_SERVICE);
		if(myWifiManager.isWifiEnabled())
		{                
		     if(myWifiManager.startScan())
		     {
		         List<ScanResult> scans = myWifiManager.getScanResults();
		         if(scans != null && !scans.isEmpty())
		         {
		        	 for (ScanResult scan : scans) 
		        	 {
		        		 int level = WifiManager.calculateSignalLevel(scan.level, 100);
		        		 strLevel = strLevel + scan.SSID + ":" + level + "\n"; 
		        	 }
		         }
		     }
		}				
		String[] recvNetworks=strLevel.split("\n");
		int maximum=-1;
		for(String current : recvNetworks)
		{
			if(current.split(":")[0].equalsIgnoreCase("USC Wireless")
					&& maximum<Integer.parseInt(current.split(":")[1]))
				maximum=Integer.parseInt(current.split(":")[1]);						
		}
		myHashtable.put("USC Wireless", ""+maximum);
		Log.d("level : ", strLevel);		 
	}
	
	public Character CompareResults()
	{			
		Enumeration<String> keys = wifiNetworks.keys();		
		if(oldDelta.isEmpty())
		{
			while( keys.hasMoreElements() ) 
			{
				String key =(String) keys.nextElement();
				oldDelta.put(key, 0);
			}
		}
		keys=null;
		keys=wifiNetworks.keys();
		while( keys.hasMoreElements() ) 
		{
		  String key =(String) keys.nextElement();
		  if(myHashtable.containsKey(key))
		  {
			  int recvValue = Integer.parseInt(myHashtable.get(key));
			  int sendValue = Integer.parseInt(wifiNetworks.get(key));
			  newDelta.put(key,sendValue-recvValue);
		  }
		}
		String key= "USC Wireless";
		Character c='A';
		if(oldDelta.containsKey(key) && newDelta.containsKey(key))
		{
			if(oldDelta.get(key)<newDelta.get(key))
			{
				c='B';
			}
			else
			{
				c='F';
			}
			if (Math.abs((oldDelta.get(key)-newDelta.get(key)))<2)
			{
				c='S';
				reached = true;
			}
		}
		oldDelta.put(key, newDelta.get(key));
		
		//print to box now
		
		myBox.setText("" + newDelta);
		return c;
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == MY_DATA_CHECK_CODE) {
	        if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
	            // success, create the TTS instance
	            tts = new TextToSpeech(this, this);
	        } 
	        else {
	            // missing data, install it
	            Intent installIntent = new Intent();
	            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
	            startActivity(installIntent);
	        }
	     }
	       }
	 

	@Override
	public void onInit(int status) {
		// TODO Auto-generated method stub
		 if (status == TextToSpeech.SUCCESS) {
		        Toast.makeText(this, "Text-To-Speech engine is initialized", Toast.LENGTH_LONG).show();
		      }
		      else if (status == TextToSpeech.ERROR) {
		        Toast.makeText(this, "Error occurred while initializing Text-To-Speech engine", Toast.LENGTH_LONG).show();
		      }
		        }
	}

