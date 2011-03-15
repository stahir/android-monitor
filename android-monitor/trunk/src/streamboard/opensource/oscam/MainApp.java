package streamboard.opensource.oscam;

import java.util.ArrayList;

import android.app.Application;
import android.util.Log;

public class MainApp extends Application{

	private ArrayList<StatusClient> _clients;

	public MainApp(){
		_clients = new ArrayList<StatusClient>();
	}
	
	public ArrayList<StatusClient> getClients(){
		return _clients;
	}

	public void setClients(ArrayList<StatusClient> clients){
		_clients = clients;
		/*
		_clients.clear();
		for(int i = 0; i < clients.size(); i++){
			_clients.add(clients.get(i));
		}
	*/
		Log.i("Clients", "Changed to " + _clients.size());
	}


}
