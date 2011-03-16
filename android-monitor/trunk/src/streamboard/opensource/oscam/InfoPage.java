package streamboard.opensource.oscam;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
			
			RelativeLayout container = (RelativeLayout)findViewById(R.id.infopage_chartlayout);
			View chart = new ChartView(container.getContext(),client.request_ecmhistory);
			container.addView(chart);
			
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

	private class ChartView extends View{
		
		private String _values;
		private boolean _valid = false;
		private String _ecmvalues[];
		
		public ChartView(Context context, String values){
			super(context);
			_values = values;
					
			if (_values != null){
				if (_values.length() > 0){
					_ecmvalues = _values.split(",");
					if (_ecmvalues.length > 0){
						_valid = true;
					}
				}
			}
		}
	
		
		@Override protected void onDraw(Canvas canvas) {
			super.onDraw(canvas);

			if(_valid){
				Log.i("Draw"," " + getWidth() );
				
				int thickness = (getWidth()-20) / (_ecmvalues.length +1);
				int border=((getWidth()-5) - ((_ecmvalues.length) * (thickness)))/2;
				int startX;
				int startY;
				int stopX;
				int stopY;
				
				Paint paint = new Paint();
				paint.setStyle(Paint.Style.FILL);
				paint.setColor(Color.rgb(0x00, 0x66, 0xff));
				paint.setStrokeWidth(thickness);
				for(int i = 0; i < _ecmvalues.length; i++){
					
					startX = border + (i*(thickness+1));
					stopX = border + (i*(thickness+1));
					startY = getHeight();
					stopY=getHeight() - (Integer.parseInt(_ecmvalues[i]) / 100);
					canvas.drawLine(startX, startY, stopX, stopY, paint);
				}
			}
		}
	}


}
