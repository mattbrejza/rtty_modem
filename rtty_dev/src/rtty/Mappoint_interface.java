package rtty;

import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.*;

public class Mappoint_interface {

	private ActiveXComponent objMP;
	
	private Dispatch plotPoint;
	private Dispatch mapObject;
	private Dispatch activeMap;
	private Dispatch point;
	
	public Mappoint_interface() {
		// TODO Auto-generated constructor stub
	}
	
	public void test()
	{
		objMP = new ActiveXComponent("MapPoint.Application");
		mapObject = objMP.getObject();
		Dispatch.put(mapObject,"Visible",new Variant(true));
		Dispatch activeMap = objMP.getPropertyAsComponent("ActiveMap");
		 activeMap = objMP.getPropertyAsComponent("GetLocation");
		//Dispatch.put(activeMap,"GetLocation",new Variant(true));
		
		//Dispatch.put(mapObject,"AddPushpin",new Variant("test"));
	}

}
