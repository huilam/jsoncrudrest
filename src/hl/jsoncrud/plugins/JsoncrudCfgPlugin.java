package hl.jsoncrud.plugins;

import hl.jsoncrud.common.http.HttpResp;
import hl.jsoncrud.restapi.CRUDServiceReq;
import hl.jsoncrud.restapi.ICRUDServicePlugin;

public class JsoncrudCfgPlugin implements ICRUDServicePlugin {

	public CRUDServiceReq preProcess(CRUDServiceReq aCrudReq) {
		
		System.out.println("[ preProcess ]");
		System.out.println("httpMethod="+aCrudReq.getHttpMethod());
		System.out.println("inputContentType="+aCrudReq.getInputContentType());
		System.out.println("inputContentData="+aCrudReq.getInputContentData());
		System.out.println("filters="+aCrudReq.getCrudFilters().toString());
		System.out.println("sorting="+String.join(", ", aCrudReq.getCrudSorting()));
		System.out.println("returns="+String.join(", ", aCrudReq.getCrudReturns()));
		
		System.out.println("pagination=start:+"+aCrudReq.getPaginationStartFrom());
		System.out.println("pagination=fetchsize:+"+aCrudReq.getPaginationFetchSize());
		
		return aCrudReq;
	}

	public HttpResp postProcess(CRUDServiceReq aCrudReq, HttpResp aHttpResp) {
		
		System.out.println("[ postProcess ]");
		System.out.println("httpStatus="+aHttpResp.getHttp_status());
		System.out.println("httpStatusMsg="+aHttpResp.getHttp_status_message());
		
		System.out.println("contentType="+aHttpResp.getContent_type());
		System.out.println("contentData="+aHttpResp.getContent_data());
		
		return aHttpResp;
	}
	
	
}