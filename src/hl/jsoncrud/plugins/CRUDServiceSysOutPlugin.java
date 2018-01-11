package hl.jsoncrud.plugins;

import hl.jsoncrud.JsonCrudException;
import hl.jsoncrud.common.http.HttpResp;
import hl.jsoncrud.restapi.CRUDServiceReq;
import hl.jsoncrud.restapi.ICRUDServicePlugin;

public class CRUDServiceSysOutPlugin implements ICRUDServicePlugin {

	public CRUDServiceReq preProcess(CRUDServiceReq aCrudReq) {
		System.out.println();
		System.out.println("[ preProcess ]");
		System.out.println("httpMethod="+aCrudReq.getHttpMethod());
		System.out.println("inputContentType="+aCrudReq.getInputContentType());
		System.out.println("inputContentData="+aCrudReq.getInputContentData());
		System.out.println("filters="+aCrudReq.getCrudFilters().toString());
		System.out.println("sorting="+String.join(", ", aCrudReq.getCrudSorting()));
		System.out.println("returns="+String.join(", ", aCrudReq.getCrudReturns()));
		System.out.println("pagination=start:"+aCrudReq.getPaginationStartFrom());
		System.out.println("pagination=fetchsize:"+aCrudReq.getPaginationFetchSize());
		System.out.println("fetchlimit="+aCrudReq.getFetchLimit());
		System.out.println();
		return aCrudReq;
	}

	public HttpResp postProcess(CRUDServiceReq aCrudReq, HttpResp aHttpResp) {
		System.out.println();
		System.out.println("[ postProcess ]");
		System.out.println("httpStatus="+aHttpResp.getHttp_status());
		System.out.println("httpStatusMsg="+aHttpResp.getHttp_status_message());
		System.out.println("contentType="+aHttpResp.getContent_type());
		System.out.println("contentData="+aHttpResp.getContent_data());
		System.out.println();
		return aHttpResp;
	}
	
	public HttpResp handleException(CRUDServiceReq aCrudReq, HttpResp aHttpResp, JsonCrudException aException) {
		System.out.println();
		System.out.println("[ handleException ]");
		System.out.println("httpStatus="+aHttpResp.getHttp_status());
		System.out.println("httpStatusMsg="+aHttpResp.getHttp_status_message());
		System.out.println("contentType="+aHttpResp.getContent_type());
		System.out.println("contentData="+aHttpResp.getContent_data());
		System.out.println("Exception="+aException.getMessage());
		System.out.println();
		return aHttpResp;
	}
	
	
}