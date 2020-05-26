package hl.jsoncrudrest.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import hl.common.http.HttpResp;
import hl.jsoncrud.JsonCrudException;
import hl.jsoncrudrest.restapi.CRUDServiceReq;
import hl.jsoncrudrest.restapi.ICRUDServicePlugin;

public class SQLSelectPlugin implements ICRUDServicePlugin {

	private static Pattern pattSQLParam = Pattern.compile("[\\s\\(\\)](\\?\\w+?)[\\s\\(\\)]");
	
	public CRUDServiceReq preProcess(CRUDServiceReq aCrudReq) throws JsonCrudException {
		//
		aCrudReq.setSkipJsonCrudDbProcess(true);
		//
		HttpServletRequest req  = aCrudReq.getHttpServletReq();
		
		if("true".equals(req.getParameter("listquery")))
		{
			return aCrudReq;
		}
		
		String sParamSQLName = req.getParameter("sqlname");
		String sSqlCfgKey = "sqlname."+sParamSQLName+".sql";
		String sConfigSQL = aCrudReq.getConfigMap().get(sSqlCfgKey);
		
		if(sConfigSQL!=null)
		{
			
			List<Object> listParamObjs = new ArrayList<Object>();
			if(sConfigSQL.indexOf("?")>-1)
			{
				Matcher m = pattSQLParam.matcher(sConfigSQL);
				String sTempSQL = sConfigSQL;
				List<String> listParams = new ArrayList<String>();
				while(m.find())
				{
					String sParamName = m.group(1);
					listParams.add(sParamName.substring(1));
					sTempSQL = sTempSQL.replace(sParamName, "?");
				}
				for(String sSQLParamName : listParams)
				{
					String sParamValue = req.getParameter(sSQLParamName);
					
					if(sParamValue!=null)
					{
						Object oVal = aCrudReq.cast2DBType(sSQLParamName, sParamValue);
						listParamObjs.add(oVal);
					}
				}
				sConfigSQL = sTempSQL;
			}
			
			
			if(sConfigSQL!=null && sConfigSQL.trim().length()>0)
			{
				int iParamFillCount = (sConfigSQL.split("\\?").length)-1;
				if(iParamFillCount != listParamObjs.size())
				{
					throw new JsonCrudException("SQLERR","Insufficient/Invalid Parameters ! " + iParamFillCount+"/"+listParamObjs.size());
				}
				
				req.setAttribute(aCrudReq.getReqUniqueID()+".sql", sConfigSQL);
				req.setAttribute(aCrudReq.getReqUniqueID()+".param.obj[]", listParamObjs);
			}
		}		
		
		return aCrudReq;
	}

	public HttpResp postProcess(CRUDServiceReq aCrudReq, HttpResp aHttpResp) {

		try {
			HttpServletRequest req  = aCrudReq.getHttpServletReq();
			long lStartTime 		= System.currentTimeMillis();
			JSONObject json = null;
			
			if("true".equals(req.getParameter("listquery")))
			{
				Map<String, String> mapConfig = aCrudReq.getConfigMap();
				json = new JSONObject();
				JSONArray jarrResult = new JSONArray();
				Pattern patt = Pattern.compile("^sqlname\\.(.+?)\\.sql");
				JSONObject jsonSqlName = null; 
				for(String sKey : mapConfig.keySet())
				{
					Matcher m = patt.matcher(sKey);
					if(m.matches())
					{
						String sSqlName 	= m.group(1);
						String sSqlDesc 	= aCrudReq.getConfigMap().get("sqlname."+sSqlName+".desc");
						if(sSqlDesc==null || sSqlDesc.trim().length()==0)
							sSqlDesc = sSqlName;
						jsonSqlName = new JSONObject();
						jsonSqlName.put(sSqlName, sSqlDesc);
						jarrResult.put(jsonSqlName);

					}
				}
				json.put("result", jarrResult);			
			}
			else
			{
				String sConfigSQL = (String) req.getAttribute(aCrudReq.getReqUniqueID()+".sql");
				@SuppressWarnings("unchecked")
				List<Object> listParamObjs = (List<Object>) req.getAttribute(aCrudReq.getReqUniqueID()+".param.obj[]");
				
				if(sConfigSQL!=null)
				{
					json = aCrudReq.retrieveBySQL(sConfigSQL, 
							listParamObjs.toArray(new Object[listParamObjs.size()]));
				}
			}
			
			if(json!=null)
			{
				JSONObject jsonMeta = json.optJSONObject("meta");
				if(jsonMeta==null)
				{
					jsonMeta = new JSONObject();
				}
				jsonMeta.put("elapsed_ms", System.currentTimeMillis() - lStartTime);
				json.put("meta", jsonMeta);
				
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