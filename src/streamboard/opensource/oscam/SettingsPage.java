package streamboard.opensource.oscam;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class SettingsPage extends Activity {
	
	
	private ServerProfiles profiles = OscamMonitor.profiles;
	private ServerSetting activeprofile = profiles.getActiveProfile();
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settingspage);
        loadSettings();
        
	
        
        
    	//Set listener for button in settings
		final Button buttonsave = (Button) findViewById(R.id.saveButton1);
		buttonsave.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				saveSettings();
				finish();
			}
		});
		

    }
    
	private void loadSettings(){
		EditText profilefield = (EditText)findViewById(R.id.editProfileName);
		profilefield.setText(activeprofile.getProfile());
		EditText urlfield = (EditText)findViewById(R.id.editUri1);
		urlfield.setText(activeprofile.getServerAddress());
		EditText portfield = (EditText)findViewById(R.id.editPort1);
		portfield.setText(activeprofile.getServerPort().toString());
		EditText userfield = (EditText)findViewById(R.id.editUser1);
		userfield.setText(activeprofile.getServerUser());
		EditText passfield = (EditText)findViewById(R.id.editPass1);
		passfield.setText(activeprofile.getServerPass());
		CheckBox checkssl = (CheckBox)findViewById(R.id.checkSSL1);
		checkssl.setChecked(activeprofile.getServerSSL());
		Spinner selectrefresh = (Spinner)findViewById(R.id.selectRefresh1);
		selectrefresh.setSelection(activeprofile.getServerRefreshIndex());
	}
	
	private void saveSettings(){
		EditText profilefield = (EditText)findViewById(R.id.editProfileName);
		activeprofile.setProfile(profilefield.getText().toString());
		EditText urlfield = (EditText)findViewById(R.id.editUri1);
		activeprofile.setServerAddress(urlfield.getText().toString());
		EditText portfield = (EditText)findViewById(R.id.editPort1);
		activeprofile.setServerPort(portfield.getText().toString());
		EditText userfield = (EditText)findViewById(R.id.editUser1);
		activeprofile.setServerUser(userfield.getText().toString());
		EditText passfield = (EditText)findViewById(R.id.editPass1);
		activeprofile.setServerPass(passfield.getText().toString());
		CheckBox checkssl = (CheckBox)findViewById(R.id.checkSSL1);
		activeprofile.setServerSSL(checkssl.isChecked());
		Spinner selectrefresh = (Spinner)findViewById(R.id.selectRefresh1);
		activeprofile.setServerRefreshIndex(selectrefresh.getSelectedItemPosition());
		profiles.saveSettings();
	}
}