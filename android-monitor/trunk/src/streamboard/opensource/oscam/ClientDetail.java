package streamboard.opensource.oscam;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

public class ClientDetail {

	private Integer _cwok = 0;
	private Integer _cwnok = 0;
	private Integer _cwignore = 0;
	private Integer _cwtimeout = 0;
	private Integer _cwcache = 0;
	private Integer _cwtun = 0;
	private Integer _cwlastresptime = 0;
	private Integer _emmok = 0;
	private Integer _emmnok = 0;
	private float _cwrate = 0;
	
	public Integer getCWOK(){
		return _cwok;
	}
	
	public Integer getCWNOK(){
		return _cwnok;
	}
	
	public Integer getCWIGNORE(){
		return _cwignore;
	}
	
	public Integer getCWTIMEOUT(){
		return _cwtimeout;
	}
	
	public Integer getCWCACHE(){
		return _cwcache;
	}
	
	public Integer getCWTUNNEL(){
		return _cwtun;
	}
	
	public Integer getCWLASTRESPONSETIME(){
		return _cwlastresptime;
	}
	
	public Integer getEMMOK(){
		return _emmok;
	}
	
	public Integer getEMMNOK(){
		return _emmnok;
	}
	
	public Float getCWRATE(){
		return _cwrate;
	}
	
	public ClientDetail(){
		
	}
	
	public ClientDetail(Document doc){
		
		try{
		NodeList usernodes = doc.getElementsByTagName("users");
		if (usernodes != null){
			Node usernode = usernodes.item(0);
			
			NodeList statsnodes = ((Element) usernode).getElementsByTagName("stats");
			
			Node statsnode = statsnodes.item(0);
			NodeList content = statsnode.getChildNodes();
			
			for(int i = 0; i < content.getLength(); i++){
				if (content.item(i).getFirstChild() != null){
					//Log.i("ClientDetail", "ID " + i + " Content " + content.item(i).getFirstChild().getNodeValue() );
					switch(i){
					case 1:_cwok = chkIntNull(content.item(i).getFirstChild().getNodeValue());
					break;
					case 3:_cwnok = chkIntNull(content.item(i).getFirstChild().getNodeValue());
					break;
					case 5:_cwignore = chkIntNull(content.item(i).getFirstChild().getNodeValue());
					break;
					case 7:_cwtimeout = chkIntNull(content.item(i).getFirstChild().getNodeValue());
					break;
					case 9:_cwcache = chkIntNull(content.item(i).getFirstChild().getNodeValue());
					break;
					case 11:_cwtun = chkIntNull(content.item(i).getFirstChild().getNodeValue());
					break;
					case 13:_cwlastresptime = chkIntNull(content.item(i).getFirstChild().getNodeValue());
					break;
					case 15:_emmok = chkIntNull(content.item(i).getFirstChild().getNodeValue());
					break;
					case 17:_emmnok = chkIntNull(content.item(i).getFirstChild().getNodeValue());
					break;
					case 19:_cwrate = chkFloatNull(content.item(i).getFirstChild().getNodeValue());
					}
				}
			}
		}
		} catch (Exception e){
			Log.i("Clientdetail", "Error " + e.getStackTrace().toString());
		}
	}

	private Integer chkIntNull(String value) {
		if (value == null) return 0;
		if (value.length() == 0)return 0;
		return Integer.parseInt(value);
	}
	private Float chkFloatNull(String value) {
		if (value == null) value = "0";
		if (value.length() == 0) value = "0";
		return Float.parseFloat(value);
	}
}
