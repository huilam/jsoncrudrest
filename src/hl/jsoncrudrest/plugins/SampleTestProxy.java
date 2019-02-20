package hl.jsoncrudrest.plugins;

import java.util.Properties;

import hl.common.http.HttpResp;
import hl.jsoncrud.JsonCrudException;
import hl.jsoncrudrest.restapi.CRUDServiceReq;
import hl.jsoncrudrest.restapi.ICRUDServicePlugin;

public class SampleTestProxy implements ICRUDServicePlugin {
	
	public CRUDServiceReq preProcess(CRUDServiceReq aCrudReq) {
		
		String sPathParam1 = aCrudReq.getUrlPathParam("param1");
		String sPathParam2 = aCrudReq.getUrlPathParam("param2");
		String sCombinedPathParam = sPathParam1+"."+sPathParam2;
		//
		aCrudReq.addUrlPathParam("combined-param", sCombinedPathParam);
		//
		System.out.println("param1:"+sPathParam1);
		System.out.println("param2:"+sPathParam2);
		System.out.println("combined-param:"+sCombinedPathParam);
		///////////////////
		
		aCrudReq.addCrudFilter("newfilter1", "testfilter1");

		return aCrudReq;
	}

	public HttpResp postProcess(CRUDServiceReq aCrudReq, HttpResp aHttpResp) {
		
		return aHttpResp;
	}
	
	public HttpResp handleException(CRUDServiceReq aCrudReq, HttpResp aHttpResp, JsonCrudException aException) {
		return aHttpResp;
	}

	public Properties getPluginProps() {
		return null;
	}
	
	
}