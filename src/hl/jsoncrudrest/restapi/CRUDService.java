package hl.jsoncrudrest.restapi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import hl.jsoncrud.JsonCrudConfig;
import hl.jsoncrud.JsonCrudException;
import hl.jsoncrud.JsonCrudRestUtil;
import hl.restapi.service.RESTApiUtil;
import hl.common.http.HttpResp;
import hl.common.http.RestApiUtil;

public class CRUDService extends HttpServlet {

	private static final long serialVersionUID 		= 1561775564425837002L;

	protected final static String TYPE_APP_JSON 	= "application/json"; 
	protected final static String TYPE_PLAINTEXT 	= "text/plain"; 
	
	protected static String _RESTAPI_ID_ATTRNAME	= "restapi.id";
	protected static String _RESTAPI_FETCH_LIMIT	= "restapi.fetch.limit";
	protected static String _RESTAPI_RESULT_ONLY	= "restapi.result.only";	
	protected static String _RESTAPI_MAPPED_URL		= "restapi.mapped.url";	
	protected static String _RESTAPI_AUTO_QUERYSTR	= "restapi.auto.query-string";
	
	protected static String _RESTAPI_PLUGIN_CLASSNAME = "restapi.plugin.implementation";
	
	protected static String _PAGINATION_STARTFROM 	= JsonCrudConfig._LIST_START;
	protected static String _PAGINATION_FETCHSIZE 	= JsonCrudConfig._LIST_FETCHSIZE;
	protected static String _PAGINATION_TOTALCNT 	= JsonCrudConfig._LIST_TOTAL;	
	
	protected static String _PAGINATION_RESULT_SECTION 	= JsonCrudConfig._LIST_RESULT;
	protected static String _PAGINATION_META_SECTION 	= JsonCrudConfig._LIST_META;	
	
	private static String _VERSION = "0.2.3";
		
	private Map<Integer, Map<String, String>> mapAutoUrlCrudkey 	= null;
	private Map<Integer, Map<String, String>> mapMappedUrlCrudkey 	= null;
	
	public static final String GET 		= "GET";
	public static final String POST 	= "POST";
	public static final String DELETE	= "DELETE";
	public static final String PUT 		= "PUT";

	public CRUDService() {
        super();
        
        mapAutoUrlCrudkey 	= new HashMap<Integer, Map<String, String>>();
        mapMappedUrlCrudkey = new HashMap<Integer, Map<String, String>>();
        
        JsonCrudConfig config = JsonCrudRestUtil.getCRUDMgr().getJsonCrudConfig();
        for(String sKey : config.getConfigCrudKeys())
        {
    		if(!sKey.toLowerCase().startsWith("crud."))
    		{
    			continue;
    		}
    		
    		String sCrudKey = sKey.substring("crud.".length());
    		
   			Map<String, String> mapConfig = config.getConfig(sKey);
        	String sMappedURL = mapConfig.get(_RESTAPI_MAPPED_URL);
        	if(sMappedURL!=null)
        	{
        		String[] sMappedURLs = RESTApiUtil.getUrlSegments(sMappedURL);
        		if(sMappedURLs!=null)
        		{
        			Map<String, String> mapUrl = mapMappedUrlCrudkey.get(sMappedURLs.length);
        			if(mapUrl==null)
        			{
        				mapUrl = new HashMap<String, String>();
        			}
        			mapUrl.put(sMappedURL, sCrudKey);
        			mapMappedUrlCrudkey.put(sMappedURLs.length, mapUrl);
        		}
        	}
        	else //if(sMappedURL==null)
        	{

    			String sId = mapConfig.get(_RESTAPI_ID_ATTRNAME);
    			if(sId==null)
    				sId = "id";
    			
        		Map<String, String> mapOne = mapAutoUrlCrudkey.get(1);
        		if(mapOne==null)
        		{
        			mapOne = new HashMap<String, String>();
        		}
        		Map<String, String> mapTwo = mapAutoUrlCrudkey.get(2);
        		if(mapTwo==null)
        		{
        			mapTwo = new HashMap<String, String>();
        		}
        		mapOne.put("/"+sCrudKey, sCrudKey);
        		mapTwo.put("/"+sCrudKey+"/{"+sId+"}", sCrudKey);
        		
        		mapAutoUrlCrudkey.put(1, mapOne);
        		mapAutoUrlCrudkey.put(2, mapTwo);
        	}
        	
        }
    }
    
    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {

    	String sHttpMethod = request.getMethod();
    	String sPath = request.getPathInfo();
    	
    	boolean isAbout = sHttpMethod.equals(GET) && sPath.equals("/about");
    	if(isAbout)
    	{
			try {
				RestApiUtil.processHttpResp(
						response, 
						HttpServletResponse.SC_OK, 
						TYPE_APP_JSON, 
						getAbout().toString());
			} catch (IOException e) {
				throw new ServletException(e);
			}
    	}
    	else
    	{
    		processHttpMethods(request, response);
    	}
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
    
    private JSONObject getAbout()
    {
    	JSONObject json = new JSONObject();
    	json.put("jsoncrud.restapi", _VERSION);
    	json.put("jsoncrud.framework",JsonCrudRestUtil.getJsonCrudVersion());
    	return json;
    }
    
    private String appendSuffix(String aString, String aSuffix)
    {
    	if(!aString.endsWith(aSuffix))
    	{
    		return aString + aSuffix;
    	}
    	else
    	{
    		return aString;
    	}
    }

    private void processHttpMethods(HttpServletRequest req, HttpServletResponse res) throws ServletException
    {
    	String sPathInfo 			= req.getPathInfo();  //{crudkey}/xx/xx
    	
    	JSONObject jsonResult 		= null;
    	JSONObject jsonErrors 		= new JSONObject();
    	
    	HttpResp httpReq = new HttpResp();
    	httpReq.setHttp_status(HttpServletResponse.SC_NOT_FOUND);
    	
    	Map<String,String> mapPathParams = new HashMap<String, String>();
		boolean isFilterById = false;
 
/*
System.out.println("sReqUri:"+sReqUri);
System.out.println("sPathInfo:"+sPathInfo);
System.out.println("sHttpMethod:"+sHttpMethod);
System.out.println("sInputContentType:"+sInputContentType);
System.out.println("sInputData:"+sInputData);
System.out.println();
*/
    	sPathInfo = appendSuffix(sPathInfo, "/");
    	
		String[] sPaths = CRUDServiceUtil.getUrlSegments(sPathInfo);
		
				
		String sCrudKey = null;
		
		//
		Map<String, String> mapMappedUrl = mapMappedUrlCrudkey.get(sPaths.length);
		if(mapMappedUrl!=null)
		{
			String sConfigUrl = getMatchingConfigUrl(mapMappedUrl, sPaths);
			if(sConfigUrl!=null)
			{
				mapPathParams = getPathParamMap(req, sConfigUrl);
				sCrudKey = mapMappedUrl.get(sConfigUrl);
			}
		}
		
		if(sCrudKey==null)
		{
			Map<String, String> mapAutoUrl = mapAutoUrlCrudkey.get(sPaths.length);
			if(mapAutoUrl!=null)
			{
				String sConfigUrl = getMatchingConfigUrl(mapAutoUrl, sPaths);
				if(sConfigUrl!=null)
				{
					mapPathParams = getPathParamMap(req, sConfigUrl);
					sCrudKey = mapAutoUrl.get(sConfigUrl);
					
					isFilterById = (sPaths.length == 2);
				}
			}
			
		}
		
		
		
		Map<String, String> mapCrudConfig = null;

		if(sCrudKey!=null)
		{
			mapCrudConfig = JsonCrudRestUtil.getCRUDMgr().getCrudConfigs(sCrudKey);
			if(mapCrudConfig==null)
			{
				sCrudKey = null;
			}
		}
				
		
		if(mapCrudConfig!=null)
		{			
			//
			CRUDServiceReq crudReq = new CRUDServiceReq(req, mapCrudConfig);
			crudReq.setCrudKey(sCrudKey);
			crudReq.addUrlPathParam(mapPathParams);
	
			String sIdFieldName = mapCrudConfig.get(_RESTAPI_ID_ATTRNAME);
			if(sIdFieldName==null || sIdFieldName.trim().length()==0)
				sIdFieldName = "id";

			if(!isFilterById)
			{
				//for MappedUrl
				isFilterById = mapPathParams.get(sIdFieldName)!=null;
			}
			
			if(isFilterById)
			{
				
				if(GET.equalsIgnoreCase(crudReq.getHttpMethod()) 
						|| PUT.equalsIgnoreCase(crudReq.getHttpMethod()) 
						|| DELETE.equalsIgnoreCase(crudReq.getHttpMethod()))
				{
					String sIdValue = mapPathParams.get(sIdFieldName);
					
					if(sIdValue==null && sPaths.length==2)
					{
						sIdValue = sPaths[sPaths.length-1];
					}
					
					if(sIdValue!=null)
					{
						crudReq.addCrudFilter(sIdFieldName, sIdValue);
					}
					else
					{
						isFilterById = false;
					}
				}
			}
			//
	
			
			ICRUDServicePlugin plugin = null;
			try {
				plugin = getPlugin(mapCrudConfig);
				crudReq = preProcess(plugin, crudReq);
				
				if(!crudReq.isSkipJsonCrudDbProcess())
				{
					if(GET.equalsIgnoreCase(crudReq.getHttpMethod()))
					{
						if(isFilterById)
						{
							jsonResult = JsonCrudRestUtil.retrieveFirst(sCrudKey, crudReq.getCrudFilters());
						}
						else
						{
							long lfetchSize = crudReq.getPaginationFetchSize();
							
							if(lfetchSize==0 && crudReq.getFetchLimit()>0)
							{
								lfetchSize = crudReq.getFetchLimit();
							}
	
							jsonResult = JsonCrudRestUtil.retrieveList(sCrudKey, 
									crudReq.getCrudFilters(), 
									crudReq.getPaginationStartFrom(),
									lfetchSize, 
									crudReq.getCrudSorting(),
									crudReq.getCrudReturns()
									);
							
						}
						
						if(jsonResult!=null)
						{
							httpReq.setHttp_status(HttpServletResponse.SC_OK); //200
						}
						
					}
					else if(POST.equalsIgnoreCase(crudReq.getHttpMethod()))
					{
						jsonResult = JsonCrudRestUtil.create(sCrudKey, crudReq.getInputContentData());
						
						if(jsonResult!=null)
						{
							httpReq.setHttp_status(HttpServletResponse.SC_OK); //200
						}
					}
					else if(PUT.equalsIgnoreCase(crudReq.getHttpMethod()))
					{
						if(isFilterById)
						{
							JSONObject jsonUpdateData = new JSONObject(crudReq.getInputContentData());
							JSONArray jsonArrResult = JsonCrudRestUtil.update(sCrudKey, jsonUpdateData, crudReq.getCrudFilters());
							
							if(jsonArrResult!=null)
							{
								httpReq.setHttp_status(HttpServletResponse.SC_OK); //200
								
								if(jsonArrResult.length()==1)
								{
									jsonResult = jsonArrResult.getJSONObject(0);
								}
								else
								{
									jsonResult = toPaginationResult(jsonArrResult);
								}
							}
						}
					}
					else if(DELETE.equalsIgnoreCase(crudReq.getHttpMethod()))
					{
						JSONArray jsonArrResult = null;
						if(isFilterById)
						{
							jsonArrResult = JsonCrudRestUtil.delete(sCrudKey, crudReq.getCrudFilters());
							if(jsonArrResult!=null)
							{
								httpReq.setHttp_status(HttpServletResponse.SC_OK); //200
								
								if(jsonArrResult.length()==1)
								{
									jsonResult = jsonArrResult.getJSONObject(0);
								}
								else
								{
									jsonResult = toPaginationResult(jsonArrResult);
								}
							}
						}
					}
				}
				///////////////////////////
				
				if(httpReq.getHttp_status() == HttpServletResponse.SC_OK);
				{
					httpReq.setContent_type(TYPE_APP_JSON); //200 = ;
				}
				if(jsonResult!=null)
				{
					boolean isResultOnly = "true".equalsIgnoreCase(mapCrudConfig.get(_RESTAPI_RESULT_ONLY));
					if(isResultOnly)
					{
						JSONArray jArrResult = JsonCrudRestUtil.getListResult(jsonResult);
						if(jArrResult!=null)
						{
							httpReq.setContent_data(jArrResult.toString());
						}
					}
					
					if(httpReq.getContent_data()==null)
						httpReq.setContent_data(jsonResult.toString());
				}
				
				
				
				httpReq = postProcess(plugin, crudReq, httpReq);
				
			} catch (JsonCrudException e) {
	
				JSONArray jArrErrors = new JSONArray();
				
				if(jsonErrors.has("errors"))
					jArrErrors = jsonErrors.getJSONArray("errors");
				
				jArrErrors.put(e.getErrorCode()+" : "+e.getErrorMsg());
				
				httpReq.setContent_type(TYPE_APP_JSON);
				httpReq.setContent_data(jArrErrors.toString());
				httpReq.setHttp_status(HttpServletResponse.SC_BAD_REQUEST);
				httpReq.setHttp_status_message(e.getErrorMsg());
				
				httpReq = handleException(plugin, crudReq, httpReq, e);
			}
		}
		
		try {
			RestApiUtil.processHttpResp(res, httpReq.getHttp_status(), httpReq.getContent_type(), httpReq.getContent_data());
		} catch (IOException ex) {
			throw new ServletException(ex.getClass().getSimpleName(), ex);
		}
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

    
    public CRUDServiceReq preProcess(
    		ICRUDServicePlugin aPlugin, 
    		CRUDServiceReq aCrudReq) throws JsonCrudException
    {
    	if(aPlugin==null)
    		return aCrudReq;
    	return aPlugin.preProcess(aCrudReq);
    }
    
    public HttpResp postProcess(
    		ICRUDServicePlugin aPlugin, 
    		CRUDServiceReq aCrudReq, HttpResp aHttpResp) throws JsonCrudException
    {
    	if(aCrudReq.getEchoJsonAttrs()!=null)
    	{
    		if(aHttpResp.getContent_data()!=null && aHttpResp.getContent_data().startsWith("{"))
    		{
    			JSONObject jsonEcho = aCrudReq.getEchoJsonAttrs();
    			JSONObject json = new JSONObject(aHttpResp.getContent_data());
    			//
    			for(String sEchoKey : jsonEcho.keySet())
    			{
    				Object oEchoVal = jsonEcho.get(sEchoKey);
    				json.put(sEchoKey, oEchoVal);
    			}
    			aHttpResp.setContent_data(json.toString());
    		}
    	}
    	
    	if(aPlugin==null)
    		return aHttpResp;
    	return aPlugin.postProcess(aCrudReq, aHttpResp);
    }
    
    public HttpResp handleException(
    		ICRUDServicePlugin aPlugin, 
    		CRUDServiceReq aCrudReq, HttpResp aHttpResp, JsonCrudException aException)
    {
    	if(aPlugin==null)
    		return aHttpResp;
    	return aPlugin.handleException(aCrudReq, aHttpResp, aException);
    }
    
    private ICRUDServicePlugin getPlugin(Map<String, String> aMapCrudConfig) throws JsonCrudException
    {
		ICRUDServicePlugin plugin = null;
		String sPluginClassName = aMapCrudConfig.get(_RESTAPI_PLUGIN_CLASSNAME);
    	if(sPluginClassName!=null && sPluginClassName.trim().length()>0)
    	{
	    	try {
				plugin = (ICRUDServicePlugin) Class.forName(sPluginClassName).newInstance();
			} catch (InstantiationException e) {
				throw new JsonCrudException(JsonCrudConfig.ERRCODE_PLUGINEXCEPTION, e);
			} catch (IllegalAccessException e) {
				throw new JsonCrudException(JsonCrudConfig.ERRCODE_PLUGINEXCEPTION, e);
			} catch (ClassNotFoundException e) {
				throw new JsonCrudException(JsonCrudConfig.ERRCODE_PLUGINEXCEPTION, e);
			}
    	}
    	return plugin;
    }
    
    public String getMatchingConfigUrl(Map<String, String> aMapUrl, String[] aPaths)
    {
    	String sMatchedURL = null;
    	if(aMapUrl!=null)
    	{
			for(String sConfigUrl : aMapUrl.keySet())
			{
				sMatchedURL = sConfigUrl;
				String[] configURLs = CRUDServiceUtil.getUrlSegments(sConfigUrl);
				for(int i=0; i<configURLs.length; i++)
				{
					String sUrlSeg = configURLs[i];
					if(sUrlSeg.startsWith("{") && sUrlSeg.endsWith("}")) 
					{
						//wildcard
					}
					else if(!sUrlSeg.equals(aPaths[i]))
					{
						sMatchedURL = null;
						break;
					}
				}
				
				if(sMatchedURL!=null)
				{
					break;
				}
			}
    	}
		return sMatchedURL;
    }
    
    public Map<String,String> getPathParamMap(HttpServletRequest request, String aConfigURL)
    {
    	Map<String,String> mapPathParams = new HashMap<String, String>();
    	
    	String[] sPaths = CRUDServiceUtil.getUrlSegments(request.getPathInfo());
    	String[] configURLs = CRUDServiceUtil.getUrlSegments(aConfigURL);
    	
    	for(int i=0; i<configURLs.length; i++)
		{
			String sUrlSeg = configURLs[i];
			if(sUrlSeg.startsWith("{") && sUrlSeg.endsWith("}")) 
			{
				String sPathParamName 	= sUrlSeg.substring(1, sUrlSeg.length()-1);
				String sPathParamValue 	= sPaths[i];
				mapPathParams.put(sPathParamName, sPathParamValue);
			}
		}
    	
    	return mapPathParams;
    }
    	
    	
}
