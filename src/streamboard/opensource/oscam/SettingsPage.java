package streamboard.opensource.oscam;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingsPage extends Activity {
	
	private ServerSetting activeprofile;
	private ServerProfiles profiles = OscamMonitor.profiles;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settingspage);
        loadSettings();
        
	
        
        
    	//Set listener for button in settings
		final Button buttonsave = (Button) findViewById(R.id.saveButton1);
		buttonsave.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				saveSettings();
			}
		});
		

    }
    
	private void loadSettings(){
		EditText urlfield = (EditText)findViewById(R.id.editUri1);
		urlfield.setText(OscamMonitor.profiles.getActiveProfile().getServerAddress());
		EditText portfield = (EditText)findViewById(R.id.editPort1);
		portfield.setText(OscamMonitor.profiles.getActiveProfile().getServerPort().toString());
		EditText userfield = (EditText)findViewById(R.id.editUser1);
		userfield.setText(OscamMonitor.profiles.getActiveProfile().getServerUser());
		EditText passfield = (EditText)findViewById(R.id.editPass1);
		passfield.setText(OscamMonitor.profiles.getActiveProfile().getServerPass());
		CheckBox checkssl = (CheckBox)findViewById(R.id.checkSSL1);
		checkssl.setChecked(OscamMonitor.profiles.getActiveProfile().getServerSSL());
		Spinner selectrefresh = (Spinner)findViewById(R.id.selectRefresh1);
		selectrefresh.setSelection(OscamMonitor.profiles.getActiveProfile().getServerRefreshIndex());
	}
	
	private void saveSettings(){
		EditText urlfield = (EditText)findViewById(R.id.editUri1);
		OscamMonitor.profiles.getActiveProfile().setServerAddress(urlfield.getText().toString());
		EditText portfield = (EditText)findViewById(R.id.editPort1);
		OscamMonitor.profiles.getActiveProfile().setServerPort(portfield.getText().toString());
		EditText userfield = (EditText)findViewById(R.id.editUser1);
		OscamMonitor.profiles.getActiveProfile().setServerUser(userfield.getText().toString());
		EditText passfield = (EditText)findViewById(R.id.editPass1);
		OscamMonitor.profiles.getActiveProfile().setServerPass(passfield.getText().toString());
		CheckBox checkssl = (CheckBox)findViewById(R.id.checkSSL1);
		OscamMonitor.profiles.getActiveProfile().setServerSSL(checkssl.isChecked());
		Spinner selectrefresh = (Spinner)findViewById(R.id.selectRefresh1);
		OscamMonitor.profiles.getActiveProfile().setServerRefreshIndex(selectrefresh.getSelectedItemPosition());
		OscamMonitor.profiles.saveSettings();
	}
}