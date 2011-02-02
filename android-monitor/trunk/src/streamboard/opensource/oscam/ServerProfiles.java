package streamboard.opensource.oscam;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;


public class ServerProfiles {

	public ArrayList<ServerSetting> profiles;
	private ServerSetting actualprofile;
	private SharedPreferences settings;
	private Integer actualprofile_idx;
	private Boolean noprofile = true;
	
	public Boolean noProfileAvail(){
		return noprofile;
	}
	
	public ServerSetting getActiveProfile(){
		return actualprofile;
	}
	
	public Integer getLastIdx(){
		return profiles.size() - 1;
	}
	
	public void setActiveProfile(Integer index){
		if(index > profiles.size()-1){
			actualprofile_idx = profiles.size()-1;
			actualprofile = profiles.get(actualprofile_idx);
		} else {
			actualprofile_idx = index;
			actualprofile = profiles.get(actualprofile_idx);
		}
	}
	
	public void createProfile(){
		ServerSetting set = new ServerSetting();
		set.setProfile("profile" + profiles.size());
		profiles.add(set);
		actualprofile_idx = profiles.size() -1;
		actualprofile = profiles.get(actualprofile_idx);
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
		if(index < profiles.size() && index >= 0){
			profiles.remove(profiles.get(index));
			actualprofile_idx = index -1;
			if(actualprofile_idx >= 0){
				actualprofile = profiles.get(actualprofile_idx);
			}else{
				actualprofile_idx = 0;
				actualprofile = profiles.get(actualprofile_idx);
			}
		}
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
		actualprofile_idx = Integer.parseInt(settings.getString("lastprofile", "0"));
		loadSettings();
		

	}
	
	public void loadSettings() {

		

		String[] profile;
		String[] serveraddress;
		String[] serverport;
		String[] serveruser;
		String[] serverpass;
		String[] serverssl;
		String[] serverrefresh;

		if (settings.getString("serverprofilename", "").length() > 0){
			if(settings.getString("serverprofilename", "").contains(";")){
				profile = TextUtils.split(settings.getString("serverprofilename", ""), ";");
				serveraddress = TextUtils.split(settings.getString("serveraddress", ""), ";");
				serverport = TextUtils.split(settings.getString("serverport", ""), ";");
				serveruser = TextUtils.split(settings.getString("serveruser", ""), ";");
				serverpass = TextUtils.split(settings.getString("serverpass", ""), ";");
				serverssl = TextUtils.split(settings.getString("serverssl", ""), ";");
				serverrefresh = TextUtils.split(settings.getString("serverrefresh", ""), ";");
			} else {
				profile = new String[]{settings.getString("serverprofilename", "")};
				serveraddress = new String[]{settings.getString("serveraddress", "")};
				serverport = new String[]{settings.getString("serverport", "")};
				serveruser = new String[]{settings.getString("serveruser", "")};
				serverpass = new String[]{settings.getString("serverpass", "")};
				serverssl = new String[]{settings.getString("serverssl", "0")};
				serverrefresh = new String[]{settings.getString("serverrefresh", "0")};
				
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
			noprofile = false;
			actualprofile = profiles.get(actualprofile_idx);

		} else {
			ServerSetting set = new ServerSetting();
			profiles.add(set);
			actualprofile_idx = 0;
			actualprofile=profiles.get(actualprofile_idx);
			noprofile = true;
		}

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
			serverrefresh = serverrefresh + seperator + set.getServerRefreshIndex().toString();
			
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
