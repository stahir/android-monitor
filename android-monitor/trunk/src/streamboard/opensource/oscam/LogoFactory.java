package streamboard.opensource.oscam;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


public class LogoFactory {
	
	private Context _context;
	private String _basepath = ""; // Add basepath here
	private Bitmap _nologo;
	
	// constructor
	public LogoFactory(Context context){
		_context = context;
		_nologo = BitmapFactory.decodeResource( _context.getResources(), R.drawable.lg_no_logo);
	}
	
	public Bitmap getLogo(String name[], int type){
		
		if (!this.sdIsAvail() || !this.foldersExist()) {
			return _nologo;
		}
		
		switch(type){
			
			case 0:
				// channel logo
				return this.generateChannelLogo(name[0], name[1]);
					
			case 1:
				// user logo
				return this.generateUserLogo(name[0]);
		
			default:
				// case else
				return _nologo;
		}

	}
	
	private Bitmap generateUserLogo(String name){
		
		StringBuilder filename = new StringBuilder();
		filename.append(_basepath);
		filename.append(name);
		filename.append(".png");
		
		return this.getBitmapFromPath(filename.toString());
	}
	
	private Bitmap generateChannelLogo(String caid, String srvid){
		
		StringBuilder filename = new StringBuilder();
		filename.append(_basepath);
		filename.append(caid);
		filename.append("_");
		filename.append(srvid);
		filename.append(".png");
		
		return this.getBitmapFromPath(filename.toString());
	}

	private Bitmap getBitmapFromPath(String path){
		try {
			// filesystem operations (read Bitmap) here
			return _nologo;
			
			
		} catch (Exception e) {
			return _nologo;
		}
	}

	public Boolean sdIsAvail(){
		
		// should return the availibility of SD card
		return false;
	}
	
	private Boolean foldersExist(){
		
		// should return true if folders exist
		return false;
	}

	public void generateFolders(){
		
		// should folder structure
	}
	
}