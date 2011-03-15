package streamboard.opensource.oscam;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class InfoPage extends Activity {

	private LogoFactory logos;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// prepare Channellogos
		setContentView(R.layout.infopage);
		logos = new LogoFactory(this.getBaseContext());

		// got selected item index from calling activity
		int idx = this.getIntent().getIntExtra("clientid", 0);
		
		// get clientarray from global MainApp
		ArrayList<StatusClient> clients = ((MainApp) getApplication()).getClients();
		Log.i("Details", "Array size " + clients.size() + " ID " + idx);
		
		if(!(clients.size() == 0)){
			StatusClient client = clients.get(idx);
			Boolean isServer = false;
			
			// identify the server types
			if (client.type.equals("s") || 
					client.type.equals("h") || 
					client.type.equals("m") || 
					client.type.equals("a")){
				isServer = true;
			}

			ImageView icon = (ImageView)findViewById(R.id.infopage_icon);
			if ( client.type.equals("r") ) {
				icon.setImageResource(R.drawable.readericon);
			} else if (client.type.equals("p")) {
				icon.setImageResource(R.drawable.proxyicon);
			} else if (client.type.equals("s")) {
				icon.setImageResource(R.drawable.servericon);
			} else if (client.type.equals("h")) {
				icon.setImageResource(R.drawable.servericon);
			} else if (client.type.equals("m")) {
				icon.setImageResource(R.drawable.servericon);
			} else if (client.type.equals("a")) {
				icon.setImageResource(R.drawable.servericon);
			} else if (client.type.equals("c")) {
				icon.setImageResource(R.drawable.clienticon);
			}
			
			TextView headline = (TextView)findViewById(R.id.infopage_headline);
			headline.setText("Details for " + client.name);

			addTableRow("Name:",client.name);
			addTableRow("Protocol:", client.protocol);
			if(!isServer){
				addTableRow("Request:", client.request_caid + ":" + client.request_srvid);
				addTableRow("Channel:", client.request);
			}
			addTableRow("Login:", OscamMonitor.sdf.format(client.times_login));
			addTableRow("Online:", OscamMonitor.sec2time(client.times_online));
			addTableRow("Idle:", OscamMonitor.sec2time(client.times_idle));
			addTableRow("Connect:", client.connection_ip); 
			addTableRow("Status:", client.connection);

			if(!isServer){
				String caidsrvid[] = new String[2];
				caidsrvid[0] = client.request_caid;
				caidsrvid[1] = client.request_srvid;
	
				ImageView chanlogo = (ImageView)findViewById(R.id.infopage_channellogo);
				chanlogo.setImageBitmap(logos.getLogo(caidsrvid, 0));
			}
			
		}

	}
	
	private void addTableRow(String parameter, String value){

		TableLayout table = (TableLayout)findViewById(R.id.infopage_table);

		TableRow row = new TableRow(table.getContext());
		
		TableRow.LayoutParams trParams = new TableRow.LayoutParams();
		trParams.setMargins(2, 2, 2, 1);
		row.setLayoutParams(trParams);
		row.setBackgroundColor(Color.rgb(0x00, 0x00, 0x00));
		
		
		TextView text1 = new TextView(row.getContext());
		text1.setTextSize(18);
		text1.setText(parameter);
		text1.setBackgroundColor(Color.rgb(0x18, 0x18, 0x18));
		text1.setLayoutParams(trParams);
		text1.setPadding(5, 0, 0, 0);
		row.addView(text1);
		
		TextView text2 = new TextView(row.getContext());
		text2.setTextSize(18);
		text2.setText(value);
		text2.setBackgroundColor(Color.rgb(0x18, 0x18, 0x18));
		text2.setLayoutParams(trParams);
		text2.setPadding(5, 0, 0, 0);
		row.addView(text2);

		table.addView(row);

	}


}
