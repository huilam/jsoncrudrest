package hl.jsoncrudrest.plugins;

import java.util.Properties;

import org.json.JSONObject;

import hl.common.http.HttpResp;
import hl.jsoncrud.JsonCrudException;
import hl.jsoncrudrest.restapi.CRUDServiceReq;
import hl.jsoncrudrest.restapi.ICRUDServicePlugin;

public class SQLSelectPlugin implements ICRUDServicePlugin {

	public CRUDServiceReq preProcess(CRUDServiceReq aCrudReq) {
		
		aCrudReq.setSkipJsonCrudDbProcess(true);
		return aCrudReq;
	}

	public HttpResp postProcess(CRUDServiceReq aCrudReq, HttpResp aHttpResp) {

		JSONObject json = null;
		try {
			
			String sParamSQLName = aCrudReq.getHttpServletReq().getParameter("sqlname");
			String sSqlCfgKey = "sqlname."+sParamSQLName+".sql";	
			String sConfigVal = aCrudReq.getConfigMap().get(sSqlCfgKey);
			
			if(sConfigVal!=null)
			{
				json = aCrudReq.retrieveBySQL(sConfigVal, new Object[] {});	
			}
			
			if(json!=null)
			{
				aHttpResp.setHttp_status(200);
				aHttpResp.setContent_type("application/json");
				aHttpResp.setContent_data(json.toString());
			}
		} catch (JsonCrudException e) {
			aHttpResp.setHttp_status(400);
			aHttpResp.setContent_type("plain/text");
			aHttpResp.setContent_data(e.getErrorMsg());
			e.printStackTrace();
		}
		return aHttpResp;
	}
	
	public HttpResp handleException(CRUDServiceReq aCrudReq, HttpResp aHttpResp, JsonCrudException aException) 
			throws JsonCrudException 
	{
		return aHttpResp;
	}

	public Properties getPluginProps() {
		return null;
	}
	
	
}