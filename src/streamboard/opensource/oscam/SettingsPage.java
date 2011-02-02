package streamboard.opensource.oscam;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class SettingsPage extends Activity {
	
	private Integer lastindex;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.settingsmenu, menu);
	    
	    
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.mnu_nextprofile:     
	        	if (OscamMonitor.profiles.getActualIdx() < OscamMonitor.profiles.getLastIdx()){
	        		OscamMonitor.profiles.setActiveProfile(OscamMonitor.profiles.getActualIdx() +1 );
	        		loadSettings();
	        	}
	            break;
	        case R.id.mnu_prevprofile: 
	        	if (OscamMonitor.profiles.getActualIdx() > 0){
	        		OscamMonitor.profiles.setActiveProfile(OscamMonitor.profiles.getActualIdx()-1);
	        		loadSettings();
	        	}
	            break;
	        case R.id.mnu_removeprofile: 
	        	OscamMonitor.profiles.removeProfileAt(OscamMonitor.profiles.getActualIdx());
	        	OscamMonitor.profiles.saveSettings();
	        	loadSettings();
	            break;
	        case R.id.mnu_addprofile: 
	        	OscamMonitor.profiles.createProfile();
	        	loadSettings();
	            break;
	        case R.id.mnu_exitsettings: 
	        	OscamMonitor.profiles.setActiveProfile(lastindex);
	        	finish();
	            break;
	
	    }
	    return true;
	}
	

	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
        lastindex = OscamMonitor.profiles.getActualIdx();
    	
    	
        setContentView(R.layout.settingspage);
        loadSettings();
        
	
        
        
    	//Set listener for button in settings
		final Button buttonsave = (Button) findViewById(R.id.saveButton1);
		buttonsave.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				saveSettings();
				//Toast.makeText(this, "Profile saved", Toast.LENGTH_LONG).show();
			}
		});
		

    }
    
	private void loadSettings(){
		EditText profilefield = (EditText)findViewById(R.id.editProfileName);
		profilefield.setText(OscamMonitor.profiles.getActiveProfile().getProfile());
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
		EditText profilefield = (EditText)findViewById(R.id.editProfileName);
		OscamMonitor.profiles.getActiveProfile().setProfile(profilefield.getText().toString());
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