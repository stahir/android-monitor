package streamboard.opensource.oscam;

import java.io.DataInputStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import streamboard.opensource.oscam.http.CustomSSLSocketFactory;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.OnTabChangeListener;

public class OscamMonitor extends TabActivity {
	
	static SimpleDateFormat sdf;
	static SimpleDateFormat dateparser; 
	
	public static ServerProfiles profiles;
	
	public static final String PREFS_NAME = "OscamMonitorPreferences";
	private TabHost tabHost;
	private ListView lv1;
	private ArrayList<StatusClient> clients;
	private String filter[];
	private Runnable status;
	private Thread thread; 
	private Handler handler = new Handler();
	
	private ServerInfo serverinfo = new ServerInfo();
	private LogInfo loginfo = new LogInfo();
	
	private Integer statusbar_set = 0;
	private String lasterror = "";
	private SubMenu mnu_profiles;


	@Override
	public void onPause(){
		super.onPause();
		stopRunning();
		profiles.saveSettings();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		profiles.saveSettings();
	}
	
	@Override
	public void onResume(){
		super.onResume();
		setAppTitle();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.mainmenu, menu);
	    
		mnu_profiles = menu.addSubMenu(0, 3, 0, "Profiles");
		mnu_profiles.setIcon(getResources().getDrawable(R.drawable.ic_menu_profiles));
		mnu_profiles.setHeaderIcon(getResources().getDrawable(R.drawable.ic_menu_profiles));
		
	    return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		mnu_profiles.clear();
		
		ArrayList<String> pnames = profiles.getProfileNamesArray();
		for(int i = 0; i < pnames.size(); i++){
			mnu_profiles.add(0, i + 4, 0, pnames.get(i));
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.mnu_exit:     
	        	finish();
	            break;
	            
	        case R.id.mnu_settings: 
	        	stopRunning();
	        	Intent intent = new Intent().setClass(this, SettingsPage.class);
	        	startActivity(intent);
	            break;
	            
	        case R.id.mnu_run:     
	        	startRunning();
	            break;
	            
	        case 3:     
	        	// do nothing
	            break;
	            
	        default:
	        	if((item.getItemId() - 4) != profiles.getActualIdx()){
	        		profiles.setActiveProfile(item.getItemId() - 4);
	        		tabHost.setCurrentTab(0);
	        		setAppTitle();
	        		switchViews(0);
	        	}
	
	    }
	    
	    return true;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		profiles = new ServerProfiles(settings);
		
		sdf = new SimpleDateFormat("dd.MM.yy HH:mm", Locale.GERMAN);
		dateparser = new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ssZ"); 
		
		setContentView(R.layout.main);
		setAppTitle();
		
		// prepare thread
		status = new Runnable(){
			@Override
			public void run() {	
				getStatus();
				handler.postDelayed(this, profiles.getActiveProfile().getServerRefreshValue());
			}
		};

		Resources res = getResources(); // Resource object to get Drawables
		tabHost = getTabHost( );  // The activity TabHost
		TabHost.TabSpec spec;  // Resusable TabSpec for each tab
		//Intent intent;  // Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		//intent = new Intent().setClass(this, StatusClientTabpage.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("clients").setIndicator("Clients",
				res.getDrawable(R.drawable.ic_tab_clients))
				.setContent(R.id.ListViewClients);
		tabHost.addTab(spec);

		// Do the same for the other tabs
		//intent = new Intent().setClass(this, StatusReaderTabpage.class);
		spec = tabHost.newTabSpec("reader").setIndicator("Reader",
				res.getDrawable(R.drawable.ic_tab_reader))
				.setContent(R.id.ListViewReader);
		tabHost.addTab(spec);

		//intent = new Intent().setClass(this, StatusServerTabpage.class);
		spec = tabHost.newTabSpec("server").setIndicator("Server",
				res.getDrawable(R.drawable.ic_tab_server))
				.setContent(R.id.ListViewServer);
		tabHost.addTab(spec);

		spec = tabHost.newTabSpec("log").setIndicator("Log",
				res.getDrawable(R.drawable.ic_tab_log))
				.setContent(R.id.LogForm);
		tabHost.addTab(spec);
		
		//intent = new Intent().setClass(this, SettingsTabpage.class);
		spec = tabHost.newTabSpec("controls").setIndicator("Control",
				res.getDrawable(R.drawable.ic_tab_control))
				.setContent(R.id.ControlForm);
		tabHost.addTab(spec);
		
		
		
		// Set listener for Shutdown button in controls
		final Button buttonshutdown = (Button) findViewById(R.id.ctrlServerShutdown);
		buttonshutdown.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				sendcontrol(0);
			}
		});
		
		// Set listener for Restart button in controls
		final Button buttonrestart = (Button) findViewById(R.id.ctrlServerRestart);
		buttonrestart.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				sendcontrol(1);
			}
		});

		// Set listener for tabchange
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String arg0) {
				switchViews(tabHost.getCurrentTab());
			}     
		}); 
		
		if (profiles.noProfileAvail() == false){
			// if settings filled - clienttab on start
			tabHost.setCurrentTab(0);
			switchViews(0);
		} else {
			// if settings not filled - settingstab on start
			Intent intent = new Intent().setClass(this, SettingsPage.class);
        	startActivity(intent);
		}
		
	}

	public void setAppTitle(){
		this.setTitle("Oscam Monitor: " + profiles.getActiveProfile().getProfile());
	}
	
	private void sendcontrol(Integer value){

		String parameter ="";
		switch(value){

		case 0:
			//Shutdown
			parameter="/shutdown.html?action=Shutdown";
			break;
		case 1:
			//Restart
			parameter="/shutdown.html?action=Restart";
			break;
		}

		String result = getServerResponse(parameter);

	}
	
	private void startRunning(){
		thread = new Thread(null, status, "MagentoBackground");
		thread.start();
	}
	
	@SuppressWarnings("unchecked")
	private void stopRunning(){
		handler.removeCallbacks(status);
		if(thread != null){
			if (thread.isAlive()) {
				// todo: stop is deprecated and causes exception
				thread.interrupt();
			}
		}
		if (lv1 != null){
			if (lv1.getAdapter() != null){
				ArrayAdapter<StatusClient> aa = (ArrayAdapter<StatusClient>) lv1.getAdapter();
				aa.clear();
			}
		}
	}
	/*
	 * switch views depending on given tab index
	 */
	private void switchViews(int tabidx) {
		
		stopRunning();
		
		switch (tabidx){

		case 0:
			lv1 = (ListView)findViewById(R.id.ListViewClients);
			filter = new String[]{"c"};
			break;
		case 1:
			lv1 = (ListView)findViewById(R.id.ListViewReader);
			filter = new String[]{"p","r"};
			break;
		case 2:
			lv1 = (ListView)findViewById(R.id.ListViewServer);
			filter = new String[]{"s","m","a","h"};
			break;
		case 3:
			// Logpage
			TextView log = (TextView)findViewById(R.id.logtext);
			log.setText(loginfo.getLogContent());
		case 4:
			// controlpage
			break;
		}

		// Settingspage doesn't need connect to server
		if (tabidx < 4) {
			
			// stop eventually waiting call
			TextView st = (TextView) findViewById(R.id.serverstatus);
			st.setVisibility(0);
			statusbar_set = 0;
			startRunning();

		} else {
			TextView st = (TextView) findViewById(R.id.serverstatus);
			Animation a = st.getAnimation();
			if (a != null)
				a.cancel();
			st.setVisibility(8);
		}
		
		lv1.setAdapter(null);
		
		lv1.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				ClientAdapter clients = (ClientAdapter) parent.getAdapter();
				StatusClient client = clients.getItem(position);

				AlertDialog detailAlert = new AlertDialog.Builder(tabHost.getContext()).create();
				detailAlert.setTitle("Details");
				detailAlert.setMessage(client.getSummary());
				detailAlert.setButton("ok", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
					}
				});
				detailAlert.show();
			}
		}); 
		
	}
	
	private void setStatusbar(){
		
		TextView st = (TextView) findViewById(R.id.serverstatus);
		Animation a_in = AnimationUtils.loadAnimation(this, R.anim.alpha_in);
		
		a_in.setAnimationListener(new AnimationListener() {

			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				TextView st = (TextView) findViewById(R.id.serverstatus);
				st.setText("");
			}

			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
		});
	
		switch(statusbar_set){
		
		case 0:
			st.setText("Server Version: " + serverinfo.getVersion());
			statusbar_set++;
			break;
		case 1:
			
			st.setText("Server Start: " + sdf.format(serverinfo.getStartdate()));
			statusbar_set++;
			break;
		case 2:
			st.setText("Server Uptime: " + sec2time(serverinfo.getUptime()));
			statusbar_set=0;
			break;
		
		}
		  
	    a_in.reset();
	    st.clearAnimation();
	    st.startAnimation(a_in);
	  
	}
	
	private Runnable returnRes = new Runnable() {

		@Override
		public void run() {
			if (clients != null){
				
				setStatusbar();
				
				if (lv1.getAdapter() == null){
					lv1.setAdapter(new ClientAdapter(tabHost.getContext(), R.layout.listview_row , clients));
				} else {
					ClientAdapter ad = (ClientAdapter) lv1.getAdapter();
					ad.refreshItems(clients);
					ad.notifyDataSetChanged();
				}
				
				// if log tab is active fill fresh log
				if (tabHost.getCurrentTab() == 3) {
					TextView  log = (TextView )findViewById(R.id.logtext);
					log.setText(loginfo.getLogContent());
				}
				
			
			}
		}
	};

	private String getServerResponse(String parameter){
		try {
			
			String server = profiles.getActiveProfile().getServerAddress();
		

			if(server.length() > 0){
				int port = 80;
				try{
					port = profiles.getActiveProfile().getServerPort();
				} catch (Exception e) {}
				
				String host = "";
				String[] uriparts = null;
				if (server.contains("/")){
					uriparts = server.split("/");
					host = uriparts[0];
				} else {
					host = server;
				}
				
				if(port != 80){
					server = host + ":" + port;
				}
				
				if(uriparts != null){
					for(int i = 1; i< uriparts.length; i++){
						server = server + "/" + uriparts[i];
					}
				}
				
				String user = profiles.getActiveProfile().getServerUser();
				String password = profiles.getActiveProfile().getServerPass();
				
				StringBuilder uri = new StringBuilder();
				if (profiles.getActiveProfile().getServerSSL() == true) {
					uri.append("https://").append(server);
				} else {
					uri.append("http://").append(server);
				}
	
				uri.append(parameter);
				Log.i( "Loader ", uri.toString() + " user: " + user + " pass: " + password + " SSL: " + profiles.getActiveProfile().getServerSSL().toString());

				HttpParams httpParameters = new BasicHttpParams();
				//HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
				//HttpConnectionParams.setSoTimeout(httpParameters, 5000);
				
				DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
				if (profiles.getActiveProfile().getServerSSL()  == true )
					httpclient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", new CustomSSLSocketFactory(), port));
				HttpProtocolParams.setUseExpectContinue(httpclient.getParams(), false);	 

				// Set password
				if(user.length() > 0) httpclient.getCredentialsProvider().setCredentials(
						new AuthScope(host, port, null, "Digest"), 
						new UsernamePasswordCredentials(user, password));

				// Execute HTTP request
				HttpGet httpget = new HttpGet(uri.toString());
				HttpResponse response = httpclient.execute(httpget);		    

				// Retrieve content
				HttpEntity r_entity = response.getEntity();
				byte[] result = new byte[2048];
				StringBuilder httpresponse = new StringBuilder();
				int len;
				if( r_entity != null ) {
					DataInputStream is = new DataInputStream(r_entity.getContent()); 
					while ((len = is.read(result)) != -1) httpresponse.append(new String(result).substring(0, len));
				}
				httpclient.getConnectionManager().shutdown();

				if(httpresponse.length() > 0){
					//Log.i( "Loader ", httpresponse.toString());
					return httpresponse.toString();
				}
			}
			return "";
		} 

		catch (SSLException sex) {
			lasterror = sex.getMessage();
			runOnUiThread(showError);
			Log.i(getClass().getName() , "XML Download SSL Exception", sex);
			return "";
		}

		catch (Exception e) {
			lasterror = e.getMessage();
			runOnUiThread(showError);
			Log.i(getClass().getName() , "XML Download Exception", e);
			return "";
		}
	}
	
	public NodeList getNodes() {
		try {
			String httpresponse = getServerResponse("/oscamapi.html?part=status&appendlog=1");
			if(httpresponse.length() > 0){
				// Create XML-DOM
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(new InputSource(new StringReader(httpresponse.toString())));
				doc.getDocumentElement().normalize();			

				// check serverinfo and exit on error
				serverinfo = new ServerInfo(doc);
				if (serverinfo.hasError()){
					lasterror = serverinfo.getErrorMessage();
					runOnUiThread(showError);
					return null;
				}
				
				// parsing the Log node
				loginfo.parseLogContent(doc);

				// return a list of clientnodes
				return doc.getElementsByTagName("client");
			} else
				return null;

		} catch (Exception e) {
			lasterror = e.getMessage();
			runOnUiThread(showError);
			Log.i(getClass().getName() , "XML Download Exception", e);
			return null;
		}

	}

	/*
	 * Thread
	 */
	private void getStatus(){
		clients = getStatusClients(filter);
		runOnUiThread(returnRes);
	}
	
	/*
	 * Because if we want to show errors from thread we must come back to
	 * UI context before. Using this by setting lasterror first and call
	 * this runnable with runOnUiThread(showError); then
	 */
	private Runnable showError = new Runnable() {
		@Override
		public void run() {
			// stop update loop
			handler.removeCallbacks(status);
			//show message
			Toast.makeText(tabHost.getContext(), lasterror, Toast.LENGTH_LONG).show();
			lasterror = "";
			
			// dismiss progressbar to make UI avail again
			//oProgressDialog.dismiss();
		}
	};

	/*
	 * returns an arraylist of clients depending of types given in array
	 */
	public ArrayList<StatusClient> getStatusClients(String type[]){
		ArrayList<StatusClient> rc = new ArrayList<StatusClient>();
		StatusClient sc;
		NodeList nl = getNodes();

		try {
			if (nl != null) {
				for (int i = 0; i < nl.getLength(); i++) {
					Node item = nl.item(i);
					sc = new StatusClient(item);
					if (sc != null){
						// check all given types in array
						for (int j = 0; j < type.length; j++){
							if (sc.type.equals(type[j])){
								rc.add(sc);
							}
						}
					} else {
						Log.i(" Loop = " , " null sc  -> " + i);
					}
				} 
			} else {
				return null;
			}
			return rc;

		} catch (Exception e) {
			Log.i("XML Arraylist Excpetion = " , e.getMessage());
			return null;
		}
	}

	/*
	 * convert seconds to 00:00:00 format
	 */
	static String sec2time(long elapsedTime) {       
		String format = String.format("%%0%dd", 2); 
		String seconds = String.format(format, elapsedTime % 60);  
		String minutes = String.format(format, (elapsedTime % 3600) / 60);  
		String hours = String.format(format, elapsedTime / 3600);  
		String time =  hours + ":" + minutes + ":" + seconds;  
		return time;  
	}

	
	public class ClientAdapter extends ArrayAdapter<StatusClient> {

		private ArrayList<StatusClient> items;

		public ClientAdapter(Context context, int textViewResourceId, ArrayList<StatusClient> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}
		
		public void refreshItems(ArrayList<StatusClient> items){
			this.items = items;
			while (this.items.size() < this.getCount()){
				this.remove( this.getItem( this.getCount() - 1));
			}
			while (this.items.size() > this.getCount()){
				this.add(new StatusClient());
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.listview_row1, null);
			}
			
			StatusClient o = items.get(position);
			
			if (o != null) {

				ImageView icon =(ImageView) v.findViewById(R.id.icon);
				boolean isServer = false;

				if ( o.type.equals("r") ) {
					icon.setImageResource(R.drawable.readericon);
				} else if (o.type.equals("p")) {
					icon.setImageResource(R.drawable.proxyicon);
				} else if (o.type.equals("s")) {
					isServer = true;
					icon.setImageResource(R.drawable.servericon);
				} else if (o.type.equals("h")) {
					isServer = true;
					icon.setImageResource(R.drawable.servericon);
				} else if (o.type.equals("m")) {
					isServer = true;
					icon.setImageResource(R.drawable.servericon);
				} else if (o.type.equals("a")) {
					isServer = true;
					icon.setImageResource(R.drawable.servericon);
				} else if (o.type.equals("c")) {
					isServer = true;
					icon.setImageResource(R.drawable.clienticon);
				}

				TextView tt = (TextView) v.findViewById(R.id.toptext);
				TextView bt = (TextView) v.findViewById(R.id.bottomtext);
				TextView tmt = (TextView) v.findViewById(R.id.timetext);

				if (tt != null) {
					if (o.request_answered.length()>0){
						tt.setText(o.name  + " --> " + o.request_answered);
					}
					else
						tt.setText(o.name);
				}
				if(bt != null){
					if ((o.request_ecmtime > 0) || !(o.request_caid.equals("0000")) ) {
						bt.setVisibility(0);
						tmt.setVisibility(0);
						tmt.setText("(" + o.request_ecmtime.toString() + "ms)");
						if (o.request.equals("unknown")) {
							bt.setText(o.request + " [" + o.request_caid + ":" + o.request_srvid +"]");
						} else {
							bt.setText(o.request);
						}
						icon.setAlpha(255);
					} else {
						if(!isServer)
							icon.setAlpha(70);
						bt.setVisibility(8);
						tmt.setText("");
						//tmt.setVisibility(8);
					}
				}
				
				ImageView bar =(ImageView) v.findViewById(R.id.bar);
				Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_bar);
			    int width = 60;
				
				if(o.request_ecmtime < 3000){
					width = (o.request_ecmtime / 50) + 1; // +1 to avoid 0 and error
				}
				
				Bitmap resizedbitmap = Bitmap.createBitmap(bmp, 0, 0, width, 1);
				bar.setImageBitmap(resizedbitmap);
				
				ImageView icon1 =(ImageView) v.findViewById(R.id.icon1);
				
				if (o.protocol.equals("camd35")){
					icon1.setImageResource(R.drawable.ic_status_c3);
					icon1.setAlpha(255);
				} else if (o.protocol.equals("newcamd")){
					icon1.setImageResource(R.drawable.ic_status_nc);
					icon1.setAlpha(255);
				} else if (o.protocol.equals("cccam")){
					icon1.setImageResource(R.drawable.ic_status_cc);
					icon1.setAlpha(255);
				} else {
					icon1.setImageResource(R.drawable.ic_status_empty);
					icon1.setAlpha(70);
				}
				
				ImageView icon2 =(ImageView) v.findViewById(R.id.icon2);
				
				if (o.times_idle > 10){
					icon2.setImageResource(R.drawable.ic_status_idle);
					icon2.setAlpha(255);
				} else {
					icon2.setImageResource(R.drawable.ic_status_empty);
					icon2.setAlpha(70);
				}
				
				ImageView icon3 =(ImageView) v.findViewById(R.id.icon3);
				
				if (o.au.equals("1")){
					icon3.setImageResource(R.drawable.ic_status_au);
					icon3.setAlpha(255);
				} else if (o.au.equals("-1")){
					icon3.setImageResource(R.drawable.ic_status_au_fail);
					icon3.setAlpha(255);
				} else {
					icon3.setImageResource(R.drawable.ic_status_empty);
					icon3.setAlpha(70);
				}
				
				
				
				
				// Iconset: http://www.iconfinder.com/search/?q=iconset:nuvola2
				this.notifyDataSetChanged();
			}
			return v;
		}
	}
}

