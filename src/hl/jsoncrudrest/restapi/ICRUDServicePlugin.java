package hl.jsoncrudrest.restapi;

import hl.jsoncrud.JsonCrudException;
import hl.common.http.HttpResp;
import hl.jsoncrudrest.restapi.CRUDServiceReq;

public interface ICRUDServicePlugin {
	
	public CRUDServiceReq preProcess(CRUDServiceReq aCrudReq);
	
	public HttpResp postProcess(CRUDServiceReq aCrudReq, HttpResp aHttpResp);
	
	public HttpResp handleException(CRUDServiceReq aCrudReq, HttpResp aHttpResp, JsonCrudException aJsonCrudException);
	
}