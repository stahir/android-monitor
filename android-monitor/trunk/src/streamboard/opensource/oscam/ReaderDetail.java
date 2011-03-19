package streamboard.opensource.oscam;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ReaderDetail {

	private ArrayList<ECMstat> _ecmlist;
	
	
	public ArrayList<ECMstat> getEcmList() {
		return _ecmlist;
	}
	
	public ReaderDetail(Document doc) {
		NodeList ecmnodes = doc.getElementsByTagName("ecm");
		
		if(ecmnodes.getLength() > 0){
			_ecmlist = new ArrayList<ECMstat>();
			for (int i = 0; i < ecmnodes.getLength(); i++) {
				_ecmlist.add(new ECMstat(ecmnodes.item(i)));
			}
		}
	}
	
	
	
	
	
	
	

	
	class ECMstat {

		private String _caid;
		private String _provid;
		private String _srvid;
		private String _channelname;
		private Integer _avgtime;
		private Integer _lasttime;
		private Integer _rc;
		private String _rcs;
		private String _lastrequest;
		private Integer _count;
		
		public String getCaid(){
			return _caid;
		}
		public String getProvid(){
			return _provid;
		}
		public String getSrvid(){
			return _srvid;
		}
		public String getChannelName(){
			return _channelname;
		}
		public Integer getAvgTime(){
			return _avgtime;
		}
		public Integer getLastTime(){
			return _lasttime;
		}
		public Integer getRc(){
			return _rc;
		}
		public String getRcs(){
			return _rcs;
		}
		public String getLastRequest(){
			return _lastrequest;
		}
		public Integer getCount(){
			return _count;
		}
		
		public ECMstat(Node node){

			Element element = (Element) node;
			_caid = chkNull(element.getAttribute("caid"));
			_provid = chkNull(element.getAttribute("provid"));
			_srvid = chkNull(element.getAttribute("srvid"));
			_channelname = chkNull(element.getAttribute("channelname"));
			_avgtime = chkIntNull(element.getAttribute("avgtime"));
			_lasttime = chkIntNull(element.getAttribute("lasttime"));
			_rc = chkIntNull(element.getAttribute("rc"));
			_rcs = chkNull(element.getAttribute("rcs"));
			_lastrequest = chkNull(element.getAttribute("lastrequest"));
			
			if (node.getFirstChild() != null)
				_count = chkIntNull(node.getFirstChild().getNodeValue());
			else
				_count = 0;
			
		}
	
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
	}
}
