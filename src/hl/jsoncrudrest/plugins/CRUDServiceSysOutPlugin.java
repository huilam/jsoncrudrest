package hl.jsoncrudrest.plugins;

import hl.common.http.HttpResp;
import hl.jsoncrud.JsonCrudException;
import hl.jsoncrudrest.restapi.CRUDServiceReq;
import hl.jsoncrudrest.restapi.ICRUDServicePlugin;

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
		System.out.println("echoJsonAttrs="+aCrudReq.getEchoJsonAttrs());
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
		
		if(aException!=null)
		{
			System.out.println("Exception="+aException.getMessage());
			
			Throwable t = aException.getCause();
			if(t!=null)
			{
				System.out.println("Cause="+t.getMessage());
			}
			
			StackTraceElement[] stackTraces = aException.getStackTrace();
			if(stackTraces!=null)
			{
				StringBuffer sb = new StringBuffer();
				for(int i=0; i<10; i++)
				{
					StackTraceElement e = stackTraces[i];
					sb.append(" ").append(e.getClassName()).append(":").append(e.getLineNumber()).append("\n");
				}
				System.out.println("StackTrace="+sb.toString());
			}
		}
		else
		{
			System.out.println("Exception="+aException);
		}
		
		System.out.println();
		return aHttpResp;
	}
	
	
}