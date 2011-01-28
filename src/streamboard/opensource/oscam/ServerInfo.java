package streamboard.opensource.oscam;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

public class ServerInfo {
	private String version = "Server Version: unknown";
	private Date startdate;
	private Integer uptime;
	private Boolean haserror = false;
	private String errormessage = "undefined error";
	
	public String getVersion(){
		return this.version;
	}
	
	public Date getStartdate(){
		return this.startdate;
	}
	
	public Integer getUptime(){
		return this.uptime;
	}
	
	public Boolean hasError(){
		return haserror;
	}
	
	public String getErrorMessage(){
		if (haserror){
			return errormessage;
		} else {
			return "no error";
		}
	}
	
	public ServerInfo(){
		
	}
	
	public ServerInfo(Document doc){
		try {
		SimpleDateFormat dateparser = new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ssZ"); 
		
		Node rootnode = doc.getElementsByTagName("oscam").item(0);
		Element rootelement = (Element) rootnode;
		version = rootelement.getAttribute("version");
		
		try {
			startdate = dateparser.parse(rootelement.getAttribute("starttime"));
		} catch (ParseException e) {
			startdate = new Date();
		}
		uptime = Integer.parseInt(rootelement.getAttribute("uptime")); 
		
		NodeList nl = doc.getElementsByTagName("error");
		if (nl != null) {
			if (nl.getLength() > 0){
				haserror = true;
				Node errornode = nl.item(0);
				if (errornode.getFirstChild() != null)
					errormessage = errornode.getFirstChild().getNodeValue();
			}
		}
		} catch (Exception e) {
			Log.i("XML Pasing Excpetion = " , version + e.getMessage());
		}
		
	}
	
}