package streamboard.opensource.oscam;

import java.io.DataInputStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import javax.net.ssl.SSLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TabHost.OnTabChangeListener;

public class OscamMonitor extends TabActivity {
	public static final String PREFS_NAME = "OscamMonitorPreferences";
	private ProgressDialog oProgressDialog = null;
	private TabHost tabHost;
	private ListView lv1;
	private ArrayList<StatusClient> clients;
	private String filter[];
	private Runnable status;
	private Thread thread; 
	private Handler handler = new Handler();
	private ServerInfo serverinfo = new ServerInfo();
	private Boolean statusbar_set = false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		
		// prepare thread
		status = new Runnable(){
			@Override
			public void run() {	
				getStatus();
				//make variable here
				handler.postDelayed(this, 15000);
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

		//intent = new Intent().setClass(this, SettingsTabpage.class);
		spec = tabHost.newTabSpec("settings").setIndicator("Settings",
				res.getDrawable(R.drawable.ic_tab_settings))
				.setContent(R.id.SettingsForm);
		tabHost.addTab(spec);

		// Set listener for button in settings
		final Button button = (Button) findViewById(R.id.saveButton);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				saveSettings();
			}
		});

		// Set listener for tabchange
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String arg0) {
				switchViews(tabHost.getCurrentTab());
			}     
		}); 
		
		// Clienttab on start
		tabHost.setCurrentTab(0);
		
		// first run type='c'
		switchViews(0);
	}

	/*
	 * Save all settings fro settingstab to device
	 */
	private void saveSettings() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();

		EditText urlfield = (EditText)findViewById(R.id.editUri);
		editor.putString("serveraddress", urlfield.getText().toString());
		EditText portfield = (EditText)findViewById(R.id.editPort);
		editor.putString("serverport", portfield.getText().toString());
		EditText userfield = (EditText)findViewById(R.id.editUser);
		editor.putString("serveruser", userfield.getText().toString());
		EditText passfield = (EditText)findViewById(R.id.editPass);
		editor.putString("serverpass", passfield.getText().toString());
		CheckBox checkssl = (CheckBox)findViewById(R.id.checkSSL);
		editor.putBoolean("serverssl", checkssl.isChecked());

		editor.commit();
		Toast.makeText(tabHost.getContext(), "Settings saved", Toast.LENGTH_SHORT).show();
	}
	
	/*
	 * fill the settings page textboxes from device
	 */
	private void loadSettings() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		EditText urlfield = (EditText)findViewById(R.id.editUri);
		urlfield.setText(settings.getString("serveraddress", ""));
		EditText portfield = (EditText)findViewById(R.id.editPort);
		portfield.setText(settings.getString("serverport", "80"));
		EditText userfield = (EditText)findViewById(R.id.editUser);
		userfield.setText(settings.getString("serveruser", ""));
		EditText passfield = (EditText)findViewById(R.id.editPass);
		passfield.setText(settings.getString("serverpass", ""));
		CheckBox checkssl = (CheckBox)findViewById(R.id.checkSSL);
		checkssl.setChecked(settings.getBoolean("serverssl", true));
	}
	
	/*
	 * switch views depending on given tab index
	 */
	private void switchViews(int tabidx) {
		
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
			loadSettings();
		}

		// Settingspage doesn't need connect to server
		if (tabidx < 3) {
			
			//if (thread.isAlive()) {
			//	thread.destroy();
			//}
			// stop eventually waiting call
			statusbar_set = false;
			handler.removeCallbacks(status);
			oProgressDialog = ProgressDialog.show(tabHost.getContext(), "Please wait...", "Retrieving data ...", true);
			thread = new Thread(null, status, "MagentoBackground");
			thread.start();
		}
		
	}
	
	private Runnable returnRes = new Runnable() {

		@Override
		public void run() {
			if (clients != null){
				
				if (!statusbar_set){
					TextView st = (TextView) findViewById(R.id.serverstatus);
					SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy HH:mm", Locale.GERMAN);
					if (serverinfo.hasError()){
						st.setText(serverinfo.getErrorMessage());
					} else {
						st.setText(
								"Server Version: " + serverinfo.getVersion() + "\t\t\t\t" 
								+ "Server Start: " + sdf.format(serverinfo.getStartdate()) + "\t\t\t\t" 
								+ "Uptime: " + sec2time(serverinfo.getUptime()));
						st.setSelected(true);
					}
					statusbar_set = true;
				}
				
				lv1.setAdapter(new ClientAdapter(tabHost.getContext(), R.layout.listview_row , clients));
				
				oProgressDialog.dismiss();
				lv1.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {

						ClientAdapter clients = (ClientAdapter) parent.getAdapter();
						StatusClient client = clients.getItem(position);
						//Toast.makeText(tabHost.getContext(), client.getSummary(), Toast.LENGTH_LONG).show();

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
		}
	};

	public NodeList getNodes() {
		try {
			// fixme: SSL issues must be handled (e.g. expired cert)
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			String server = settings.getString("serveraddress", "");

			if(server.length() > 0){
				int port = 80;
				try{
					port = Integer.parseInt(settings.getString("serverport", "80"));
				} catch (Exception e) {}
				String user = settings.getString("serveruser", "");
				String password = settings.getString("serverpass", "");
				StringBuilder uri = new StringBuilder();
				if (settings.getBoolean("serverssl", true) == true) {
					uri.append("https://").append(server);
				} else {
					uri.append("http://").append(server);
				}
				if(port != 80) uri.append(":" + port);
				uri.append("/oscamapi.html?part=status");

				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
				HttpConnectionParams.setSoTimeout(httpParameters, 5000);
				DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
				HttpProtocolParams.setUseExpectContinue(httpclient.getParams(), false);	 

				// Set password
				if(user.length() > 0) httpclient.getCredentialsProvider().setCredentials(
						new AuthScope(server, port, null, "Digest"), 
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
					// Create XML-DOM
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					Document doc = db.parse(new InputSource(new StringReader(httpresponse.toString())));
					doc.getDocumentElement().normalize();			

					// check serverinfo and exit on error
					serverinfo = new ServerInfo(doc);
					if (serverinfo.hasError()){
						Toast.makeText(tabHost.getContext(), serverinfo.getErrorMessage(), Toast.LENGTH_LONG).show();
						return null;
					}
					
					// return a list of clientnodes
					return doc.getElementsByTagName("client");
				}
			}
			return null;
		} 

		catch (SSLException sex) {
			Toast.makeText(tabHost.getContext(), sex.getMessage(), Toast.LENGTH_LONG).show();
			Log.i(getClass().getName() , "XML Download SSL Exception", sex);
			return null;
		}

		catch (Exception e) {
			Toast.makeText(tabHost.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.listview_row, null);
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

				if (tt != null) {
					tt.setText(o.name + " (" + o.protocol + ")");
				}
				if(bt != null){
					if (o.request_ecmtime > 0) {
						bt.setVisibility(0);
						bt.setText(o.request + " (" + o.request_ecmtime.toString() + "ms)");
						icon.setAlpha(255);
					} else {
						if(!isServer)
							icon.setAlpha(70);
						bt.setVisibility(8);
					}
				}
				// Iconset: http://www.iconfinder.com/search/?q=iconset:nuvola2
			}
			return v;
		}
	}
}

