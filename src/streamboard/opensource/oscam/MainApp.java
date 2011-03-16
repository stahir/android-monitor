package streamboard.opensource.oscam;

import java.util.ArrayList;

import android.app.Application;

public class MainApp extends Application{

	private ArrayList<StatusClient> _clients;

	public MainApp(){
		_clients = new ArrayList<StatusClient>();
	}
	
	public ArrayList<StatusClient> getClients(){
		return _clients;
	}

	public void setClients(ArrayList<StatusClient> clients){
		if(clients != null)
			_clients = clients;
	}


}
