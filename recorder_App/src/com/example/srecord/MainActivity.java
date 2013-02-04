package com.example.srecord;

import java.util.Locale;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;


public class MainActivity extends Activity {
	private static final int RECORDER_SAMPLERATE = 8000;
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	private AudioRecord recorder = null;
	private Thread recordingThread = null;
	private boolean isRecording = false;
	//private TextToSpeech tts;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setButtonHandlers();
		enableButtons(false);

		int bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
				RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);


		//tts = new TextToSpeech(this, (OnInitListener) this);
		//txtText = (EditText) findViewById(R.id.txtText);
	}

	private void setButtonHandlers() {
		((Button) findViewById(R.id.StartButton)).setOnClickListener(btnClick);
		((Button) findViewById(R.id.StopButton)).setOnClickListener(btnClick);
	}

	private void enableButton(int id, boolean isEnable) {
		((Button) findViewById(id)).setEnabled(isEnable);
	}

	private void enableButtons(boolean isRecording) {
		enableButton(R.id.StartButton, !isRecording);
		enableButton(R.id.StopButton, isRecording);
	}

	int BufferElements2Rec = 4096; // want to play 2048 (2K) since 2 bytes we use only 1024
	int BytesPerElement = 2; // 2 bytes in 16bit format

	private void startRecording() {

		recorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				RECORDER_SAMPLERATE, RECORDER_CHANNELS,
				RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);

		recorder.startRecording();
		isRecording = true;
		recordingThread = new Thread(new Runnable() {
			public void run() {
				try{
					writeAudioDataToFile();
				}
				catch(Exception e)
				{
					Log.d("catch:",e.getMessage());
				}
				/*        	short sData[] = new short[BufferElements2Rec];
        	while(isRecording)
        	{	
        		recorder.read(sData, 0, BufferElements2Rec);
        	}	
        	 Log.d("sData:", sData.toString());*/


			}
		}, "AudioRecorder Thread");
		recordingThread.start();
	}

	//convert short to byte
	private byte[] short2byte(short[] sData) {
		int shortArrsize = sData.length;
		byte[] bytes = new byte[shortArrsize * 2];
		for (int i = 0; i < shortArrsize; i++) {
			bytes[i * 2] = (byte) (sData[i] & 0x00FF);
			bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);
			sData[i] = 0;
		}
		return bytes;

	}

	private void writeAudioDataToFile() throws InterruptedException {
		// Write the output audio in byte

		//String filePath = "/sdcard/voice8K16bitmono.pcm";
		//short sData[] = new short[BufferElements2Rec];

		/* FileOutputStream os = null;
    try {
        os = new FileOutputStream(filePath);
    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }*/

		while (isRecording) {
			// gets the voice output from microphone to byte format
			short sData[] = new short[BufferElements2Rec];
			recorder.read(sData, 0, BufferElements2Rec);
			//    System.out.println("Short wirting to file" + sData.toString());

			Complex[] x = new Complex[BufferElements2Rec];

			// original data
			for (int i = 0; i < BufferElements2Rec; i++) {
				x[i] = new Complex((int) sData[i], 0);
				//x[i] = new Complex(-2*Math.random() + 1, 0);
			}

			Complex[] y = FFT.fft(x);
			double magnitude= getMagnitude(y);
			int a=(int)magnitude;

			Thread.sleep(3000);
			
		//	speakOut("Magnitude is ");

			Log.d("Magnitude: ", " "+a);
		}

		/* try {
       // os.close();
    } catch (IOException e) {
        e.printStackTrace();
    }*/
	}

	/*private void speakOut(String text) {
		//Get the text typed
		// String text = txtText.getText().toString();
		//If no text is typed, tts will read out 'You haven't typed text'
		//else it reads out the text you typed
		if (text.length() == 0) {
			tts.speak("You haven't typed text", TextToSpeech.QUEUE_ADD, null);
		} else {
			tts.speak(text, TextToSpeech.QUEUE_ADD, null);
		}
	}*/


	public static double getMagnitude(Complex[] N)
	{
		double magnitude = 0;
		for (int i = 0; i < N.length; i++)
		{
			magnitude += (N[i].re() * N[i].re()) + (N[i].im() + N[i].im());
		}
		return Math.log10(magnitude) * 10;
	}

	private void stopRecording() {
		// stops the recording activity
		if (null != recorder) {
			isRecording = false;
			recorder.stop();
			recorder.release();
			recorder = null;
			recordingThread = null;
		}
	}

	private View.OnClickListener btnClick = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.StartButton: {
				enableButtons(true);
				startRecording();
				break;
			}
			case R.id.StopButton: {
				enableButtons(false);
				stopRecording();
				break;
			}
			}
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}
	
/*	public void onInit(int initStatus) {
	    if (initStatus == TextToSpeech.SUCCESS) {
	        tts.setLanguage(Locale.US);
	    }
	}*/
	
}