package hl.jsoncrud.restapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;


import hl.jsoncrud.JsonCrudRestUtil;
import hl.jsoncrud.common.http.RestApiUtil;

public class CRUDService extends HttpServlet {

	private static final long serialVersionUID 			= 1561775564425837002L;
	

	protected final static String TYPE_APP_JSON 		= "application/json"; 
	//
	protected static final String _QPARAM_PAGINATION 	= "pagination";
	protected static final String _QPARAM_FILTERS 		= "filters";
	protected static final String _QPARAM_SORTING 		= "sorting";
	
	protected static final String _PAGINATION_STARTFROM = "start";
	protected static final String _PAGINATION_FETCHSIZE = "fetchsize";
	protected static final String _PAGINATION_TOTALCNT 	= "total";	
	
	protected static final String _PAGINATION_RESULT_SECTION 	= "result";
	protected static final String _PAGINATION_META_SECTION 		= "meta";	
	
	
	private final String GET 	= "GET";
	private final String POST 	= "POST";
	private final String DELETE	= "DELETE";
	private final String PUT 	= "PUT";

	public CRUDService() {
        super();
    }
    
    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
    	processHttpMethods(request, response);
	}

    @Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
    	processHttpMethods(request, response);
	}

    @Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException {
    	processHttpMethods(request, response);
	}

    @Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException {
    	processHttpMethods(request, response);
	}

    private void processHttpMethods(HttpServletRequest req, HttpServletResponse res) throws ServletException
    {
    	String sReqUri 				= req.getRequestURI(); //with context root : /jsoncrudrest/{crudkey}
    	String sPathInfo 			= req.getPathInfo();  //{crudkey}
    	String sHttpMethod 			= req.getMethod();
    	
    	String sInputContentType 	= req.getContentType();
    	String sInputData 			= RestApiUtil.getReqContent(req);
    	
    	int iOutputHttpStatus 		= HttpServletResponse.SC_NOT_FOUND;
    	JSONObject jsonResult 		= null;
    	String sOutputContentType  	= null;
 
/*
System.out.println("sReqUri:"+sReqUri);
System.out.println("sPathInfo:"+sPathInfo);
System.out.println("sHttpMethod:"+sHttpMethod);
System.out.println("sInputContentType:"+sInputContentType);
System.out.println("sInputData:"+sInputData);
System.out.println();
*/
    	
		String[] sPaths = sPathInfo.substring(1).split("/");
		String sCrudKey = sPaths[0];
		Map<String, Map<String, String>> mapQueryParams = getQueryParamsMap(req);

		JSONObject jsonWhereFilters 	= getFilters(mapQueryParams.get(_QPARAM_FILTERS));
		if(jsonWhereFilters==null)
			jsonWhereFilters = new JSONObject();
		
		System.out.println("sCrudKey ["+ sCrudKey+"]");
		
		try {
			
			if(GET.equalsIgnoreCase(sHttpMethod))
			{
				
				
				switch (sPaths.length)
				{
					case 1 : //get list
						
						Map<String,String> mapPagination 	= mapQueryParams.get(_QPARAM_PAGINATION);
						List<String> listSorting 			= getSorting(mapQueryParams.get(_QPARAM_SORTING));
						
						long iFetchStartFrom 	= 0;
						long iFetchSize 		= 0;
						
						if(mapPagination!=null)
						{
							String sFetchStartFrom 	= mapPagination.get(_PAGINATION_STARTFROM);
							if(sFetchStartFrom!=null && sFetchStartFrom.trim().length()>0)
							{
								iFetchStartFrom = Long.parseLong(sFetchStartFrom);
							}
							//
							String sFetchSize 		= mapPagination.get(_PAGINATION_FETCHSIZE);
							if(sFetchSize!=null && sFetchSize.trim().length()>0)
							{
								iFetchSize = Long.parseLong(sFetchSize);
							}
						}
						jsonResult = JsonCrudRestUtil.retrieveList(sCrudKey, jsonWhereFilters, iFetchStartFrom ,iFetchSize, listSorting);
						break;
						
					case 2 : //get id
						jsonWhereFilters.put("id", sPaths[1]);
						jsonResult = JsonCrudRestUtil.retrieveFirst(sCrudKey, jsonWhereFilters);
						break;
				}
				
				if(jsonResult!=null)
				{
					iOutputHttpStatus  = 200;
				}
				
			}
			else if(POST.equalsIgnoreCase(sHttpMethod))
			{
				jsonResult = JsonCrudRestUtil.create(sCrudKey, sInputData);
				
				if(jsonResult!=null)
				{
					iOutputHttpStatus  = 200;
				}
			}
			else if(PUT.equalsIgnoreCase(sHttpMethod))
			{
				switch (sPaths.length)
				{
					case 2 : 
						
						jsonWhereFilters.put("id", sPaths[1]);
						JSONObject jsonUpdateData = new JSONObject(sInputData);
						JSONArray jsonArrResult = JsonCrudRestUtil.update(sCrudKey, jsonUpdateData, jsonWhereFilters);
						
						if(jsonArrResult!=null)
						{
							iOutputHttpStatus  = 200;
							
							if(jsonArrResult.length()==1)
							{
								jsonResult = jsonArrResult.getJSONObject(0);
							}
							else
							{
								jsonResult = toPaginationResult(jsonArrResult);
							}
						}
						break;
				}
			}
			else if(DELETE.equalsIgnoreCase(sHttpMethod))
			{
				JSONArray jsonArrResult = null;

				switch (sPaths.length)
				{
					case 1 : //delete list
						//??
						break;
					case 2 : //get id
						jsonWhereFilters.put("id", sPaths[1]);
						jsonArrResult = JsonCrudRestUtil.delete(sCrudKey, jsonWhereFilters);
						if(jsonArrResult!=null)
						{
							iOutputHttpStatus  = 200;
							
							if(jsonArrResult.length()==1)
							{
								jsonResult = jsonArrResult.getJSONObject(0);
							}
							else
							{
								jsonResult = toPaginationResult(jsonArrResult);
							}
						}
						break;
				}
			}
			///////////////////////////
			
			if(iOutputHttpStatus == 200);
			{
				sOutputContentType = TYPE_APP_JSON;
			}
					
			RestApiUtil.processHttpResp(res, iOutputHttpStatus, sOutputContentType, jsonResult.toString());
			
		} catch (Exception e) {
			throw new ServletException(e);
		}
    }
    
    private JSONObject getFilters(Map<String, String> mapFilters)
    {
    	return getKeyValue(mapFilters);
    }
    
    private List<String> getSorting(Map<String, String> mapSorting)
    {
    	List<String> listSortFields = null;
    	JSONObject jsonSorting = getKeyValue(mapSorting);
    	if(jsonSorting!=null)
    	{
    		listSortFields = new ArrayList<String>();
	    	for(String sKey : jsonSorting.keySet())
	    	{
	    		String sSortDir = jsonSorting.getString(sKey);
	    		
	    		String sSorting = "";
	    		
	    		if(sSortDir.trim().equalsIgnoreCase(""))
	    		{
	    			sSorting = sKey;
	    		}
	    		else
	    		{
	    			sSorting = sKey+"."+sSortDir;
	    		}
	    		listSortFields.add(sSorting);
	    	}
    	}
    	return listSortFields;
    }
    
    private JSONObject getKeyValue(Map<String, String> mapKV)
    {
    	JSONObject jsonKV = null;
    	
    	if(mapKV!=null && mapKV.size()>0)
    	{
    		jsonKV = new JSONObject();
    	
	    	for(String sFilterKey : mapKV.keySet())
	    	{
	    		jsonKV.put(sFilterKey, mapKV.get(sFilterKey));
	    	}
       	}
    	    	
    	return jsonKV;
    }
    
    private JSONObject toPaginationResult(JSONArray aJsonArrResult)
    {
    	JSONObject jsonResult = null;
    	if(aJsonArrResult.length()==1)
		{
			jsonResult = aJsonArrResult.getJSONObject(0);
		}
		else
		{
			JSONObject jsonMeta = new JSONObject();
			jsonMeta.put(_PAGINATION_TOTALCNT, aJsonArrResult.length());
			
			jsonResult = new JSONObject();
			jsonResult.put(_PAGINATION_META_SECTION, jsonMeta);
			jsonResult.put(_PAGINATION_RESULT_SECTION, aJsonArrResult);
		}
    	return jsonResult;
    }
    
    private Map<String, Map<String,String>> getQueryParamsMap(HttpServletRequest req)
    {
		Map<String, Map<String,String>> mapQueryParams = new HashMap<String, Map<String,String>>();
		
		String sQueryString = req.getQueryString();
		if(sQueryString!=null)
		{
			for(String sQueryParam : req.getQueryString().split("&"))
			{
				String[] sQParam = sQueryParam.split("=");
				Map<String, String> mapParamVals = new HashMap<String,String>();
				
				if(sQParam.length>1)
				{
					String[] sQParamVals = sQParam[1].split(",");
					for(String sQParamVal : sQParamVals)
					{
						String[] sKVals = sQParamVal.split(":");
						if(sKVals.length==1)
						{
							sKVals = new String[] {sKVals[0], ""};
						}
						mapParamVals.put(sKVals[0], sKVals[1]);
					}
				}
				mapQueryParams.put(sQParam[0], mapParamVals);
			}
		}
		
		/*
		for(String sKey : mapQueryParams.keySet())
		{
			List<String> listParamVals = mapQueryParams.get(sKey);
			System.out.println(sKey+" "+ listParamVals.toString());
		}
		*/
		
		return mapQueryParams;
    }
    
}
