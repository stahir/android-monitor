package streamboard.opensource.oscam;

import java.util.Date;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class StatusClient {
	
	
	public String name;
	public String type;
	public String protocol;
	public String protocolext;
	public String au;

	public String request_caid;
	public String request_srvid;
	public Integer request_ecmtime;
	public String request_answered;
	public String request;

	public Date times_login;
	public Integer times_online;
	public Integer times_idle;

	public String connection_ip;
	public Integer connection_port;
	public String connection;
	
	private String chkNull(String value) {
		if (value == null) return "na";
		if (value.length() == 0)return "";
		return value;
	}
	private Integer chkIntNull(String value) {
		if (value == null) return 0;
		if (value.length() == 0)return 0;
		return Integer.parseInt(value);
	}
	
	public String getSummary(){
		
		if(name != null){
		StringBuilder text = new StringBuilder();
		text.append("Name:\t\t" + name + "\n");
		text.append("Protocol:\t" + protocol + "\n");
		text.append("Request:\t" + request_caid + ":" + request_srvid + "\n");
		text.append("Channel:\t" + request + "\n");
		text.append("Login:\t\t" + OscamMonitor.sdf.format(times_login) + "\n");
		text.append("Online:\t" + OscamMonitor.sec2time(times_online) + "\n");
		text.append("Idle:\t\t\t" + OscamMonitor.sec2time(times_idle) + "\n");
		text.append("Connect:\t" + connection_ip + "\n"); 
		text.append("Status:\t\t" + connection + "\n");
		
		return text.toString();
		} else {
			return "please wait for next refresh";
		}
	}
	
	public StatusClient(){
		
	}

	public StatusClient(Node node){
		try {
			
			Element baseelement = (Element) node;
			Element element = (Element) node;

			type = chkNull(element.getAttribute("type"));
			name = chkNull(element.getAttribute("name"));
			protocol = chkNull(element.getAttribute("protocol"));
			protocolext = chkNull(element.getAttribute("protocolext"));
			au = chkNull(element.getAttribute("au"));

			NodeList nl = baseelement.getElementsByTagName("request");
			Node innernode = nl.item(0);
			element = (Element) innernode;

			if (innernode.getFirstChild() != null)
				request = chkNull(innernode.getFirstChild().getNodeValue());
			else
				request = "unknown";
			
			request_caid = chkNull(element.getAttribute("caid"));
			request_srvid = chkNull(element.getAttribute("srvid"));
			request_ecmtime = chkIntNull(element.getAttribute("ecmtime"));
			request_answered = chkNull(element.getAttribute("answered"));

			nl = baseelement.getElementsByTagName("times");
			innernode = nl.item(0);
			element = (Element) innernode;

			times_login = OscamMonitor.dateparser.parse(chkNull(element.getAttribute("login")));
			times_online = chkIntNull(element.getAttribute("online"));
			times_idle = chkIntNull(element.getAttribute("idle"));

			nl = baseelement.getElementsByTagName("connection");
			innernode = nl.item(0);
			element = (Element) innernode;

			connection = chkNull(innernode.getFirstChild().getNodeValue());
			connection_ip = chkNull(element.getAttribute("ip"));
			connection_port = chkIntNull(element.getAttribute("port"));

		} catch (Exception e) {
			//Log.i("XML Pasing Excpetion = " , e.getMessage());
		}
	}
	

}