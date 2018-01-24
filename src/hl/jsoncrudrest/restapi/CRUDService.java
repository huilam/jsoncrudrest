package hl.jsoncrudrest.restapi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import hl.jsoncrud.JsonCrudConfig;
import hl.jsoncrud.JsonCrudException;
import hl.jsoncrud.JsonCrudRestUtil;
import hl.common.http.HttpResp;
import hl.common.http.RestApiUtil;

public class CRUDService extends HttpServlet {

	private static final long serialVersionUID 		= 1561775564425837002L;

	protected final static String TYPE_APP_JSON 	= "application/json"; 
	protected final static String TYPE_PLAINTEXT 	= "text/plain"; 
	
	protected static String _RESTAPI_PLUGIN_IMPL_CLASSNAME 	= "restapi.plugin.implementation";
	protected static String _RESTAPI_ID_ATTRNAME			= "restapi.id";
	protected static String _RESTAPI_FETCH_LIMIT			= "restapi.fetch.limit";
	protected static String _RESTAPI_ECHO_JSONATTR_PREFIX	= "restapi.echo.jsonattr.prefix";
	
	protected static String _PAGINATION_STARTFROM 	= JsonCrudConfig._LIST_START;
	protected static String _PAGINATION_FETCHSIZE 	= JsonCrudConfig._LIST_FETCHSIZE;
	protected static String _PAGINATION_TOTALCNT 	= JsonCrudConfig._LIST_TOTAL;	
	
	protected static String _PAGINATION_RESULT_SECTION 	= JsonCrudConfig._LIST_RESULT;
	protected static String _PAGINATION_META_SECTION 	= JsonCrudConfig._LIST_META;	
	
	private static String _VERSION = "0.2.1";
	
	
	private static Map<String, String> mapUrlCrudkey = null;
	private static Pattern pattURLmapKey = Pattern.compile("crud\\.([a-zA-Z_]+?)\\.restapi\\.baseurl"); 	
	
	public static final String GET 		= "GET";
	public static final String POST 	= "POST";
	public static final String DELETE	= "DELETE";
	public static final String PUT 		= "PUT";

	public CRUDService() {
        super();
        
        mapUrlCrudkey = new HashMap<String, String>();
        
        Map<String, String> mapConfigs = JsonCrudRestUtil.getCRUDMgr().getAllConfig();
        for(String sKey : mapConfigs.keySet())
        {
        	Matcher m = pattURLmapKey.matcher(sKey);
        	if(m.find())
        	{
        		String sCrudKey = m.group(1);
        		String sURL = mapConfigs.get(sKey);
        		if(sURL!=null && sURL.trim().length()>0)
        		{
        			mapUrlCrudkey.put(sURL, sCrudKey);
        		}
        	}
        }
    }
    
    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {

    	boolean isAbout = request.getMethod().equals(GET) && request.getPathInfo().equals("/about");
    	if(isAbout)
    	{
			try {
				RestApiUtil.processHttpResp(response, 
						HttpServletResponse.SC_NO_CONTENT, 
						TYPE_APP_JSON, getAbout().toString());
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
    	
    	int iBaseUrlLengthAdj = 0;
 
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
		String sCrudKey = sPaths[0];
		Map<String, String> mapCrudConfig = JsonCrudRestUtil.getCRUDMgr().getCrudConfigs(sCrudKey);
		if(mapCrudConfig==null)
		{
			String sURLmappedCrudkey = null;
			//check url-crudkey mapping
			for(String sMapBaseUrl : mapUrlCrudkey.keySet())
			{
				sMapBaseUrl = appendSuffix(sMapBaseUrl, "/");
				if(sPathInfo.startsWith(sMapBaseUrl))
				{
					sURLmappedCrudkey = mapUrlCrudkey.get(sMapBaseUrl);
					iBaseUrlLengthAdj =  CRUDServiceUtil.getUrlSegments(sMapBaseUrl).length-1;
					break;
				}
			}
			
			if(sURLmappedCrudkey!=null)
			{
				mapCrudConfig = JsonCrudRestUtil.getCRUDMgr().getCrudConfigs(sURLmappedCrudkey);
				if(mapCrudConfig!=null)
				{
					sCrudKey = sURLmappedCrudkey;
				}
			}
		}
		
		if(mapCrudConfig!=null)
		{
			
			int iUrlBranch = sPaths.length - iBaseUrlLengthAdj; 

			/*
System.out.println("iUrlBranch="+iUrlBranch);
System.out.println("sPaths.length="+sPaths.length);
System.out.println("iBaseUrlLengthAdj="+iBaseUrlLengthAdj);
*/
			
			boolean isFilterById = iUrlBranch==2;
			
			//
			CRUDServiceReq crudReq = new CRUDServiceReq(req, mapCrudConfig);
			crudReq.setCrudKey(sCrudKey);
	
			if(isFilterById)
			{
				String sIdFieldName = mapCrudConfig.get(_RESTAPI_ID_ATTRNAME);
				if(sIdFieldName==null || sIdFieldName.trim().length()==0)
					sIdFieldName = "id";
				
				if(GET.equalsIgnoreCase(crudReq.getHttpMethod()) 
						|| PUT.equalsIgnoreCase(crudReq.getHttpMethod()) 
						|| DELETE.equalsIgnoreCase(crudReq.getHttpMethod()))
				{
					crudReq.addCrudFilter(sIdFieldName, sPaths[sPaths.length-1]);
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
						switch (iUrlBranch)
						{
							case 1 : //get list
								
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
								break;
								
							case 2 : //get id
								jsonResult = JsonCrudRestUtil.retrieveFirst(sCrudKey, crudReq.getCrudFilters());
								break;
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
						switch (iUrlBranch)
						{
						 	//by id
							case 2 :
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
								break;
						}
					}
					else if(DELETE.equalsIgnoreCase(crudReq.getHttpMethod()))
					{
						JSONArray jsonArrResult = null;
		
						switch (iUrlBranch)
						{
							case 1 : //delete list
								//??
								break;
							case 2 : //get id
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
								break;
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
		String sPluginClassName = aMapCrudConfig.get(_RESTAPI_PLUGIN_IMPL_CLASSNAME);
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
    
    
}
