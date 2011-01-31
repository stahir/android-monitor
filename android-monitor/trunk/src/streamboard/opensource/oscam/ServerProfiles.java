package streamboard.opensource.oscam;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Log;


public class ServerProfiles {

	public ArrayList<ServerSetting> profiles;
	private ServerSetting actualprofile;
	private SharedPreferences settings;
	private Integer actualprofile_idx;
	
	public Boolean noProfileAvail(){
		return profiles.get(0).getProfile().length() == 0;
	}
	
	public ServerSetting getActiveProfile(){
		return actualprofile;
	}
	
	public void setActiveProfile(Integer index){
		if(index > profiles.size()-1){
			actualprofile = profiles.get(profiles.size()-1);
			actualprofile_idx = profiles.size()-1;
		} else {
			actualprofile = profiles.get(index);
			actualprofile_idx = index;
		}
	}
	
	public Integer getActualIdx(){
		return actualprofile_idx;
	}
	
	public ServerSetting getProfile(Integer index){
		return profiles.get(index);
	}

	public void addProfile(ServerSetting profile){
		profiles.add(profile);
		saveSettings();
	}
	
	public void removeProfileAt(Integer index){
		if(index <= profiles.size()-1)
			profiles.remove(index);
	}
	
	// Returns string array to fill e.g. spinner or menu
	public String[] getProfileNamesArray(){
		ArrayList<String> array = new ArrayList<String>();
		for (int i = 0; i < profiles.size(); i++){
			array.add(profiles.get(i).getProfile());
		}
		return (String[])array.toArray();
	}

	//Constructor
	public ServerProfiles(SharedPreferences settings) {
		
		this.settings = settings;
		profiles = new ArrayList<ServerSetting>();
		loadSettings();

	}
	
	public void loadSettings() {
		
		actualprofile_idx = Integer.parseInt(settings.getString("lastprofile", "0"));
		
		String[] profile;
		String[] serveraddress;
		String[] serverport;
		String[] serveruser;
		String[] serverpass;
		String[] serverssl;
		String[] serverrefresh;
		Log.i( "ServerProfiles ", settings.getString("serverprofilename", "profile2"));
		if(settings.getString("serverprofilename", "profile1").contains(";")){
			profile = TextUtils.split(settings.getString("serverprofilename", ""), ";");
			serveraddress = TextUtils.split(settings.getString("serveraddress", ""), ";");
			serverport = TextUtils.split(settings.getString("serverport", ""), ";");
			serveruser = TextUtils.split(settings.getString("serveruser", ""), ";");
			serverpass = TextUtils.split(settings.getString("serverpass", ""), ";");
			serverssl = TextUtils.split(settings.getString("serverssl", ""), ";");
			serverrefresh = TextUtils.split(settings.getString("serverrefresh", ""), ";");
		} else {
			profile = new String[]{settings.getString("serverprofilename", "profile1")};
			serveraddress = new String[]{settings.getString("serveraddress", "")};
			serverport = new String[]{settings.getString("serverport", "")};
			serveruser = new String[]{settings.getString("serveruser", "")};
			serverpass = new String[]{settings.getString("serverpass", "")};
			try {
				serverssl = new String[]{settings.getString("serverssl", "0")};
			}catch (Exception e){
				serverssl = new String[]{"0"};
			}
			Log.i( "ServerSSL ", serverssl[0]);
			try {
				serverrefresh = new String[]{settings.getString("serverrefresh", "")};
			}catch (Exception e){
				serverrefresh = new String[]{"0"};
			}
		}
		
		for(int i = 0; i < profile.length; i++){
			ServerSetting set = new ServerSetting();
			set.setProfile(profile[i]);
			set.setServerAddress(serveraddress[i]);
			set.setServerPort(serverport[i]);
			set.setServerUser(serveruser[i]);
			set.setServerPass(serverpass[i]);
			set.setServerSSL(serverssl[i]);
			set.setServerRefreshIndex(serverrefresh[i]);
			profiles.add(set);
		}
		actualprofile = profiles.get(actualprofile_idx);
	}
	
	public void saveSettings() {
		
		String seperator = "";
		String profile = "";
		String serveraddress = "";
		String serverport = "";
		String serveruser = "";
		String serverpass = "";
		String serverssl = "";
		String serverrefresh = "";
		
		for(int i = 0; i < profiles.size(); i++){
						
			ServerSetting set = profiles.get(i);
			
			profile = profile + seperator + set.getProfile();
			serveraddress = serveraddress + seperator + set.getServerAddress();
			serverport = serverport + seperator + set.getServerPort().toString();
			serveruser = serveruser + seperator + set.getServerUser();
			serverpass = serverpass + seperator + set.getServerPass();
			if (set.getServerSSL())
				serverssl = serverssl + seperator + "1";
			else
				serverssl = serverssl + seperator + "0";
			serverrefresh = serverrefresh + seperator + set.getServerRefreshValue().toString();
			
			seperator = ";";
			
		}
		
		Editor editor = settings.edit();

		editor.putString("serverprofilename", profile);
		editor.putString("serveraddress", serveraddress);
		editor.putString("serverport", serverport);
		editor.putString("serveruser", serveruser);
		editor.putString("serverpass", serverpass);
		editor.putString("serverssl", serverssl);
		editor.putString("serverrefresh", serverrefresh);
		editor.putString("lastprofile", actualprofile_idx.toString());

		editor.commit();

	}

}
