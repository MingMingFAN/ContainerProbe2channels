package mingming.research.containerprobe2channels;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity implements OnClickListener, OnItemSelectedListener {

	Spinner spinner_materials;
	Spinner spinner_volume;
	Spinner spinner_level;
	Spinner spinner_containg;
	Spinner spinner_seal;
	Spinner spinner_soundtype;
	
	Button bt_probing;
	CheckBox cb_auto;
	
	EditText et_note_materials;
	EditText et_note_level;
	EditText et_note_containing;
	EditText et_note_container;
	
	TextView tv_counter;
	
	String materials = "";
	float volumeRatio = 1.0f;
	String level = "0";
	String containing = "";
	String seal = "";  // 0: not sealed; 1: sealed;
	String containerNote = "";
	String probetype = "";
	
	File myDir;
	String mRecordFileName;  // file name of recorded sound clip
	File  mRecordFile = null;
	Handler handler = new Handler();
	
	String  timestamp = "timestamp";
	String note_materials = "";
	String note_level = "0";
	String note_containing = "";
	
	ExtAudioRecorder extAudioRecorder = null;

	
	Timer mTimerPeriodic = null;
	Timer mTimerOneTime = null;
	
	
	boolean autoprobing = false;
	boolean running = false;
	
	int counter = 0;
	
	AudioManager mAudioManager;
	MediaPlayer mp = null;
	int playtime_cnt = 0;
	private int soundID = 0;
	/*
	SoundPool  mSoundPool;
	boolean loaded = false;
	
	boolean plays = false;
	*/
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		spinner_materials = (Spinner)findViewById(R.id.spinner_materials);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.materials, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_materials.setAdapter(adapter);
		spinner_materials.setOnItemSelectedListener(this);
		
		
		spinner_volume = (Spinner)findViewById(R.id.spinner_volume);		
		ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.volume, android.R.layout.simple_spinner_item);
		adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_volume.setAdapter(adapter2);
		spinner_volume.setOnItemSelectedListener(this);

		spinner_level = (Spinner)findViewById(R.id.spinner_level);
		ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this, R.array.level, android.R.layout.simple_spinner_item);
		adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_level.setAdapter(adapter3);
		spinner_level.setOnItemSelectedListener(this);
		
		spinner_containg = (Spinner)findViewById(R.id.spinner_containing);		
		ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(this, R.array.containing, android.R.layout.simple_spinner_item);
		adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_containg.setAdapter(adapter4);
		spinner_containg.setOnItemSelectedListener(this);
				
		spinner_seal = (Spinner)findViewById(R.id.spinner_seal);
		ArrayAdapter<CharSequence> adapter5 = ArrayAdapter.createFromResource(this, R.array.seal, android.R.layout.simple_spinner_item);
		adapter5.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_seal.setAdapter(adapter5);
		spinner_seal.setOnItemSelectedListener(this);
		
		
		spinner_soundtype = (Spinner)findViewById(R.id.spinner_soundtype);
		ArrayAdapter<CharSequence> adapter6 = ArrayAdapter.createFromResource(this, R.array.soundtype, android.R.layout.simple_spinner_item);
		adapter6.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner_soundtype.setAdapter(adapter6);
		spinner_soundtype.setOnItemSelectedListener(this);
		
		
		bt_probing = (Button)findViewById(R.id.button_probing);
		bt_probing.setOnClickListener(this);
		bt_probing.setBackgroundColor(Color.GREEN);
		
		cb_auto = (CheckBox)findViewById(R.id.checkBox_auto);
		
		et_note_materials = (EditText)findViewById(R.id.editText_materials_note);
		et_note_level = (EditText)findViewById(R.id.editText_note_level);
		et_note_containing = (EditText)findViewById(R.id.editText_note_containing);
		et_note_container = (EditText)findViewById(R.id.editText_container_note);
		
		
		tv_counter = (TextView)findViewById(R.id.textView_counter);
		
   	    String root = Environment.getExternalStorageDirectory().toString();
	    myDir = new File(root + "/containerprobe");    
	    myDir.mkdirs();
	    
		
	    mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
	    
		/*
		mSoundPool = new SoundPool(10, AudioManager.STREAM_MUSIC,0);
		mSoundPool.setOnLoadCompleteListener(new OnLoadCompleteListener(){
		
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
					int status) {
				// TODO Auto-generated method stub
				loaded = true;
			}
			
		});
	    
		soundID = mSoundPool.load(this, R.raw.sin20hz20000hzlin_0db16bit44100hz,1);
		*/
		
		
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		String temp = parent.getItemAtPosition(position).toString();
		
		switch(parent.getId())
		{
			case R.id.spinner_materials:
				if(!temp.equals(materials))
				{
					counter = 0;
					materials = temp;
				}
				break;
			case R.id.spinner_volume:
				if(!temp.equals(""+volumeRatio))
				{
					counter = 0;
					volumeRatio = Float.parseFloat(temp);
				}
				break;
			case R.id.spinner_containing:
				if(!temp.equals(materials))
				{
					counter = 0;
					containing = temp;
				}
				break;
			case R.id.spinner_seal:
				if(!temp.equals(seal))
				{
					counter = 0;
					seal = temp;
				}
				break;
			case R.id.spinner_level:
				if(!temp.equals(level))
				{
					counter = 0;		
					level = temp;
				}
				break;
			case R.id.spinner_soundtype:
				if(temp.equals("sametime"))
				{
					probetype = "sametime";
					soundID = R.raw.sin20hz20000hzlin_0db16bit44100hz;
				}
				else if(temp.equals("sequentially"))
				{
					probetype = "sequentially";
					soundID = R.raw.sin20hz20000hzlin_2channels;
				}
				else if(temp.equals("halfoverlap"))
				{
					probetype = "halfoverlap";
					soundID = R.raw.offset_0_05;
				}
				else if(temp.equals("adjacent"))
				{
					probetype = "adjacent";
					soundID = R.raw.offset_0_10;
				}
				break;
			default:
				break;
		}
		
		if(counter == 0)
			tv_counter.setText("0");
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}


	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
			case R.id.button_probing:
				
				int currentapiVersion = android.os.Build.VERSION.SDK_INT;
				if (currentapiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH){
				    // Do something for froyo and above versions
					final Date currentTime = new Date();
					final SimpleDateFormat sdf =  new SimpleDateFormat("MM-dd HH:mm:ss",Locale.US);
					timestamp = sdf.format(currentTime);
				} else{
				    // do something for phones running an SDK before ICE 
					//Log.i("Tag","Version: " + currentapiVersion);
				}
				
				if(materials.equals("others"))
				{
					materials = et_note_materials.getText().toString();
				}
				
				if(level.equals("others"))
				{
					level = et_note_level.getText().toString();
				}
				
				if(containing.equals("others"))
				{
					containing = et_note_containing.getText().toString();
				}
				
				containerNote = et_note_container.getText().toString();
				
				mRecordFileName =  level +  "_" + probetype + "_" + materials + "_" + containing + "_" + containerNote + "_" + seal  + "_" + volumeRatio + "_" + timestamp + ".wav";
				
				mRecordFile = new File(myDir,mRecordFileName);
				
				autoprobing  = cb_auto.isChecked();
				
				if(autoprobing)
				{
					running = !running;
					if(running)
					{
						bt_probing.setText("Stop");
						bt_probing.setBackgroundColor(Color.RED);
						startPeriodProbing();
					}
					else
					{
						bt_probing.setText("Probing");
						bt_probing.setBackgroundColor(Color.GREEN);
						stopPeriodProbing();
						counter = 0;
					}
				}
				else
				{
					bt_probing.setClickable(false);
					bt_probing.setBackgroundColor(Color.GRAY);
					startOneProbing();
					//startOneProbingWith2Channels();
					counter ++;
				} 
				
				
				
				tv_counter.setText(counter + "");
				
				break;
			default:
				break;
		}
	}
		
	
	private void startRecordSound()
	{
		// Start recording
		//extAudioRecorder = ExtAudioRecorder.getInstanse(true);	  // Compressed recording (AMR)
		extAudioRecorder = ExtAudioRecorder.getInstanse(false); // Uncompressed recording (WAV)

		extAudioRecorder.setOutputFile(mRecordFile.getAbsolutePath());
		//Log.i("TAG", "after init extAudioRecorder");
		
		extAudioRecorder.prepare();
		//Log.i("TAG", "after preparing");
		
		extAudioRecorder.start();		
	}
	
	private void playSweepSound() {
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
				(int) (volumeRatio * mAudioManager
						.getStreamMaxVolume(AudioManager.STREAM_MUSIC)), 0);
		mp = MediaPlayer.create(getApplicationContext(), soundID);
		if (mp != null) {
			mp.start();
			mp.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(final MediaPlayer mp) {
					// TODO Auto-generated method stub
					
					playtime_cnt++;
					
					if(playtime_cnt >= Utils.repeat_times)
					{
						ReleaseMediaPlayer();
						startTimerTask(Utils.probing_duration);
						playtime_cnt = 0;
					}
					else
					{
						 new CountDownTimer(Utils.probing_duration_for_2_channels, 100000) {

						     public void onTick(long millisUntilFinished) {
						    	 //bt_sweep.setText("seconds remaining: " + millisUntilFinished / 1000);
						     }

						     public void onFinish() {		    	
						    	 mp.start();
						     }
						  }.start();
						
					}
				}
			});

		}

	}
	
	
	/*
	private void playSweepSoundSeparateFrom2Channels() {
       if(loaded && !plays)
       {
    	   mSoundPool.play(soundID, volumeRatio, 0, 1,0,1);
    	   startTimerTaskAfterLeftChannelProb(Utils.probing_duration_for_2_channels);
    	   plays = true;
       }
	}
	
	
	private void startTimerTaskAfterLeftChannelProb(int timeinterval)
	{
		
		 new CountDownTimer(timeinterval, timeinterval) {

		     public void onTick(long millisUntilFinished) {
		    	 //bt_sweep.setText("seconds remaining: " + millisUntilFinished / 1000);
		     }

		     public void onFinish() {	
		    	 mSoundPool.play(soundID, 0, volumeRatio, 1,0,1);
		    	 startTimerTaskAfterRightChannelProb(Utils.probing_duration_for_2_channels);
		    	 
		    	 plays = false;
		     }
		  }.start();
	}
	
	private void startTimerTaskAfterRightChannelProb(int timeinterval)
	{
		
		 new CountDownTimer(timeinterval, timeinterval) {

		     public void onTick(long millisUntilFinished) {
		    	 //bt_sweep.setText("seconds remaining: " + millisUntilFinished / 1000);
		     }

		     public void onFinish() {		    	
		    	 StopRecordSound();
		    	 if(!autoprobing)
		    	 {
			    	 bt_probing.setText("Probing");
			    	 bt_probing.setClickable(true);
			    	 bt_probing.setBackgroundColor(Color.GREEN);
		    	 }
		     }
		  }.start();
	}
	*/
	
	private void startTimerTask(int timeinterval)
	{
		
		 new CountDownTimer(timeinterval, timeinterval) {

		     public void onTick(long millisUntilFinished) {
		    	 //bt_sweep.setText("seconds remaining: " + millisUntilFinished / 1000);
		     }

		     public void onFinish() {		    	
		    	 StopRecordSound();
		    	 if(!autoprobing)
		    	 {
			    	 bt_probing.setText("Probing");
			    	 bt_probing.setClickable(true);
			    	 bt_probing.setBackgroundColor(Color.GREEN);
		    	 }
		     }
		  }.start();
	}
	
	
	private void ReleaseMediaPlayer()
	{
		if( mp != null)
		{
	    	 mp.reset();
	    	 mp.release();
    	     mp = null;
		}
	}
	
	private void StopRecordSound()
	{
		// Stop recording
		extAudioRecorder.stop();
		extAudioRecorder.reset();
		extAudioRecorder.release();
	}
	
	
	private void startPeriodProbing()
	{
		if(mTimerPeriodic != null)
		{
			mTimerPeriodic.cancel();
		}
		
		mTimerPeriodic = new Timer();
		mTimerPeriodic.scheduleAtFixedRate(new myTimerTask(), 0, Utils.repeat_period); 
	}
	
	class myTimerTask extends TimerTask{

		@Override
		public void run() {
			

			
			int currentapiVersion = android.os.Build.VERSION.SDK_INT;
			if (currentapiVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH){
			    // Do something for froyo and above versions
				final Date currentTime = new Date();
				final SimpleDateFormat sdf =  new SimpleDateFormat("MM-dd HH:mm:ss",Locale.US);
				timestamp = sdf.format(currentTime);
			} else{
			    // do something for phones running an SDK before ICE 
				//Log.i("Tag","Version: " + currentapiVersion);
			}
			
			level = et_note_level.getText().toString();
			
			mRecordFileName =  level +  "_" + probetype + "_" +  materials + "_" + containing + "_" + containerNote + "_" + seal  + "_" + volumeRatio + "_" + timestamp + ".wav";
			
			mRecordFile = new File(myDir,mRecordFileName);
			
			startRecordSound();
			
			playSweepSound();
			
			counter ++;
						

			
			if(counter >= 100)
			{
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						bt_probing.setText("Probing");
						bt_probing.setBackgroundColor(Color.GREEN);
						stopPeriodProbing();
						counter = 0;
						tv_counter.setText(counter + "");
					}});

			}
			else
			{
				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						tv_counter.setText(counter + "");
					}});
			}
		}
	}
	
	private void stopPeriodProbing()
	{
		counter = 0;
		if(mTimerPeriodic != null)
		{
			mTimerPeriodic.cancel();
		}
	}
	
	
	private void startOneProbing()
	{
		startRecordSound();
		
		playSweepSound();
	}
	
	/*
	private void startOneProbingWith2Channels()
	{
		startRecordSound();
		playSweepSoundSeparateFrom2Channels();
	}
	*/	
}
