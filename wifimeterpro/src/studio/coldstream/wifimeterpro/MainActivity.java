package studio.coldstream.wifimeterpro;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
	
	private static final String TAG = "WIFI_METER";
	
	protected static final int TICK = 0x9001;
	
	public static final String WPA2 = "WPA2";
	public static final String WPA = "WPA";
	public static final String WEP = "WEP";
	public static final String OPEN = "Open";
	
	private boolean wakelock = false;
	private boolean opennet = false;
	PowerManager pm;
	PowerManager.WakeLock wl;
	
	WifiManager wifi;
	
	Thread thread1;
	
	TextView statusView1;
	TextView statusView2;
	
	TextView textview1;
	TextView textview2;
	TextView textview3;
	TextView textview4;
	TextView textview5;
	
	Button lb;
	
	DisplayMetrics metrics;
	
	private LinearLayout my_ll;
	
	List<TextView> my_meter;
	
	String currentvalue;
	int cv;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        
        statusView1 = (TextView)findViewById(R.id.statusText1);
		statusView2 = (TextView)findViewById(R.id.statusText2);
        
        textview1 = (TextView) findViewById(R.id.textview1);
        textview2 = (TextView) findViewById(R.id.textview2);
        textview3 = (TextView) findViewById(R.id.mainText);
        textview4 = (TextView) findViewById(R.id.fixText);       
        textview5 = (TextView) findViewById(R.id.textview5);       
        
        my_meter = new LinkedList<TextView>();
        my_ll = (LinearLayout)findViewById(R.id.mainll);
       
        metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);       
        
        for(int i = 0; i <= 40; i++){
        	my_meter.add(new TextView(this));
        	my_meter.get(i).setTextSize(1.0f);
        	my_meter.get(i).setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.FILL_PARENT,1f));
        	if(i%2 == 0)
        		//my_meter.get(i).setBackgroundColor(Color.rgb(136 - i , 136 - i , 238 - i));   
        	    my_meter.get(i).setBackgroundColor(Color.rgb(20 , 20 , 80));
        	
        	my_ll.addView(my_meter.get(i));
        }
                
        lb = (Button)findViewById(R.id.logButton);
		lb.setOnClickListener(new OnClickListener() 
        {
			public void onClick(View v) 
            {                
    			Log.d(TAG, "Click!");    			
    			
    			startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
            }
        });
        
        initializeWiFiListener();
        
        myTimer();
    }
    
    public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Intent mainIntent = new Intent(MainActivity.this,MainActivity.class);
		MainActivity.this.startActivity(mainIntent);
		MainActivity.this.finish();
	}
    
    public void myTimer(){
		thread1 = new Thread()
	    {
	        public void run() {
	            try {
	                while(true) {	        			
	        			sleep(2000);
	        			Message m = new Message();
                    	m.what = TICK;                            
                    	messageHandler.sendMessage(m);
	                }
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }
	    };
	    thread1.start();
	}
    
    private Handler messageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg){
			switch(msg.what){
			//handle messages
			
			case TICK:
				Log.d(TAG, "Tick!");				
				wifi.startScan();
				if(wakelock)					
					statusView1.setTextColor(Color.rgb(136 , 136 , 238));
				else
					statusView1.setTextColor(Color.rgb(20 , 20 , 80));
					
				if(opennet)	
					statusView2.setTextColor(Color.rgb(136 , 136 , 238));
				else
					statusView2.setTextColor(Color.rgb(20 , 20 , 80));
				break;
			default:
				//break;
			}
		}
	};
    
    private void initializeWiFiListener(){
        Log.i(TAG, "executing initializeWiFiListener");

        String connectivity_context = Context.WIFI_SERVICE;
        wifi = (WifiManager)getSystemService(connectivity_context);        

        if(!wifi.isWifiEnabled()){
                if(wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLING){
                        wifi.setWifiEnabled(true);              
                }
        }
                	
        Log.d(TAG,"Starting wifi scan...");
        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(wifiReceiver, filter);
	}
	
    private final BroadcastReceiver wifiReceiver = new BroadcastReceiver()
    {
      @Override
      public void onReceive(Context ctx, Intent intent)
      {    	  
    	  int hi_level = 0;
    	  int last_level = 1000;
    	  
    	  try{
	          List<ScanResult> results = wifi.getScanResults();
	          
	          for(int i = 0; i < results.size(); i++){
	        	  if(Math.abs(results.get(i).level) < Math.abs(last_level)){
	        		  hi_level = i;
	        		  last_level = results.get(i).level;
	        	  }
	          }
	          
	          textview1.setText("Network Name (SSID): " + results.get(hi_level).SSID);
	          
	          textview2.setText("Security Protocols: " + results.get(hi_level).capabilities);
	          
	          DecimalFormat maxDigitsFormatter = new DecimalFormat("####");	  			  		  
	          Typeface font = Typeface.createFromAsset(getAssets(), "digit.ttf");
	          textview3.setTypeface(font);	          
	          currentvalue = String.valueOf(maxDigitsFormatter.format(results.get(hi_level).level));	  	      
	  	      textview3.setText(currentvalue + " ");
	  	      
	  	      cv = results.get(hi_level).level + 100;
	  	      if(cv < 0){
	  	    	  cv = 100;
	  	    	  textview3.setText("0 ");
	  	      }
	  	      else if(cv > 100)
	  	    	  cv = 100;
	          
	          for(int i = 0; i <= 40; i++){
	 			 if(i%2 == 0){
	 					 if(10 * cv >= i * 25)
	 						 my_meter.get(i).setBackgroundColor(Color.rgb(136 - i , 136 - i , 238 - i));					 
	 					 else
	 						 my_meter.get(i).setBackgroundColor(Color.rgb(20 , 20 , 80));
	 					 
	 					 if(cv >= 100)
	 						 my_meter.get(i).setBackgroundColor(Color.rgb(238 - i , 136 - i , 136 - i));
	 			 }
	 			 else
	 				 	my_meter.get(i).setBackgroundColor(Color.rgb(34 , 34 , 102));
	          }
	          
	          if(results.size() > 0)
	        	  textview4.setText("WiFi Networks Found: " + String.valueOf(results.size()));
	          else
	        	  textview4.setText("WiFi Networks Found: 0");
	          
	          if(results.size() > 0 && results.get(hi_level).capabilities.contains(OPEN))
	        	  opennet = true;
	          else
	        	  opennet = false;
	          
	          StringBuilder sb = new StringBuilder();
	          sb.append("");
	          for(int i = 0; i < results.size(); i++){
	        	  sb.append(results.get(i).level);
	        	  sb.append("  ");
	          }
	          
	          //textview5.setText(sb.toString() + " Hi:" + hi_level);
	          Log.d(TAG, sb.toString() + " Hi:" + hi_level);
	        	  
	       } catch (Exception e){
	    	   e.printStackTrace();
	    	   textview1.setText("Network Name (SSID): ");		          
		       textview2.setText("Security Protocols: ");
	    	   textview3.setText("   --- ");
	    	   textview4.setText("WiFi Networks Found: 0");	    	  
	       }
      }
    };
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.options_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		/*if(my_logdata.size() > 0)
	    	menu.findItem(R.id.exportlog).setEnabled(true);
	    else
	    	menu.findItem(R.id.exportlog).setEnabled(false);*/
		return true;
	}
	//Menu item "exportlog" should only be visible when logdata exists! android:visible=["visible" | "invisible" | "gone"]
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {	    
	    case R.id.settings:
	        showSettings();
	    	//Toast.makeText(this, "Settings", Toast.LENGTH_LONG).show();
	        return true;
	    case R.id.about:
	        showAbout();
	    	//Toast.makeText(this, "About", Toast.LENGTH_LONG).show();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	public void showAbout(){
		/*Intent myIntent = new Intent(this, AboutActivity.class);
        startActivityForResult(myIntent, 0);*/
        
        Intent mainIntent = new Intent(MainActivity.this,AboutActivity.class);
		/*MainActivity.this.startActivity(mainIntent);
		MainActivity.this.finish();*/
        MainActivity.this.startActivityForResult(mainIntent, -1);
        
		return;
	}	
	
	
	public void showSettings(){
		Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
		myIntent.putExtra("SETTINGS_WAKELOCK", wakelock);
		//myIntent.putExtra("SETTINGS_SENSITIVITY", sensitivity);
		//myIntent.putExtra("SETTINGS_AUTOLOGGING", autologging);
		MainActivity.this.startActivityForResult(myIntent, 0);
        
        /*Intent mainIntent = new Intent(MainActivity.this,SettingsActivity.class);
		MainActivity.this.startActivity(mainIntent);
		MainActivity.this.finish();*/

		return;
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(resultCode == RESULT_OK){
			
			wakelock = data.getBooleanExtra("SETTINGS_WAKELOCK", false);
			Log.d(TAG, Boolean.toString(wakelock));
			/*sensitivity = data.getIntExtra("SETTINGS_SENSITIVITY", SENSITIVITY_DEFAULT);
			Log.d(TAG, Integer.toString(sensitivity));
			autologging = data.getIntExtra("SETTINGS_AUTOLOGGING", AUTOLOGGING_DEFAULT);
			Log.d(TAG, Integer.toString(autologging));
			
			for(int i = 0; i < (SENSITIVITY_MAX * 10); i++)
	        	sensidata[i] = abssum;*/
			
			if(wakelock){
		         wl.acquire();
		    }else{
	            if(wl.isHeld()){
	            	wl.release();
	            }
	            //wl = null;
	        }

		}
	}
    
    @Override
	protected void onStop() {
		/* may as well just finish since saving the state is not important for this toy app */
		Log.d(TAG, "onStop");
		//sensorManager.unregisterListener(this);
		//finish();
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		//sensorManager.unregisterListener(this);
		if(wl.isHeld()){
        	wl.release();
        }
        //wl = null;
		//finish();
		super.onStop();
	}
    
    
}
