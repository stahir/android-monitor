package streamboard.opensource.oscam;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Bitmap;
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
				addTableRow("Channel",logos.getLogo(caidsrvid, 0));
			}
			
		}

		//Set listener for button in settings
		final Button buttonsave = (Button) findViewById(R.id.infopage_btn_back);
		buttonsave.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		
		
	}
	
	private void addTableRow(String parameter, String value){

		TableLayout table = (TableLayout)findViewById(R.id.infopage_table);

		TableRow row = new TableRow(table.getContext());
		
		TextView text1 = new TextView(row.getContext());
		text1.setTextSize(18);
		text1.setText(parameter);
		row.addView(text1);
		
		TextView text2 = new TextView(row.getContext());
		text2.setTextSize(18);
		text2.setText(value);
		row.addView(text2);

		table.addView(row);

	}
	
	private void addTableRow(String parameter, Bitmap value){

		TableLayout table = (TableLayout)findViewById(R.id.infopage_table);

		TableRow row = new TableRow(table.getContext());
		
		TextView text1 = new TextView(row.getContext());
		text1.setTextSize(18);
		text1.setText(parameter);
		row.addView(text1);
		
		ImageView image = new ImageView(row.getContext());
		image.setImageBitmap(value);
		row.addView(image);

		table.addView(row);

	}

}
