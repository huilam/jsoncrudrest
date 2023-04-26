package hl.jsoncrudrest.restapi;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import hl.jsoncrud.JsonCrudExceptionList;
import hl.jsoncrud.JsonCrudRestUtil;
import hl.restapi.service.RESTApiUtil;
import hl.common.http.HttpResp;
import hl.common.http.RestApiUtil;
import hl.common.system.CommonInfo;
import hl.common.system.Windows;

public class CRUDService extends HttpServlet {

	private static final long serialVersionUID 		= 1561775564425837002L;

	protected final static String TYPE_APP_JSON 	= "application/json"; 
	protected final static String TYPE_PLAINTEXT 	= "text/plain"; 
	
	private static final String _ERROR_PROXY		= "PROXY_ERROR";
	
	protected static String _RESTAPI_PROXY_URL			= "restapi.proxy.url";
	protected static String _RESTAPI_PROXY_HOSTNAME2IP	= "restapi.proxy.hostname-to-ip";
	
	protected static String _RESTAPI_DISABLED		= "restapi.disabled";
	protected static String _RESTAPI_ID_ATTRNAME	= "restapi.id";
	protected static String _RESTAPI_ECHO_PREFIX	= "restapi.echo.jsonattr.prefix";
	protected static String _RESTAPI_FETCH_LIMIT	= "restapi.fetch.limit";
	protected static String _RESTAPI_RESULT_ONLY	= "restapi.result.only";	
	protected static String _RESTAPI_MAPPED_URL		= "restapi.mapped.url";	
	protected static String _RESTAPI_GZIP_THRESHOLD = "restapi.gzip.threshold.bytes";	
	protected static String _RESTAPI_SERVE_STATICWEB= "restapi.static.web";	
	
	protected static String _RESTAPI_DEF_PAGINATION_START 		= "restapi.default.pagination.start";
	protected static String _RESTAPI_DEF_PAGINATION_FETCHSIZE 	= "restapi.default.pagination.fetchsize";
	protected static String _RESTAPI_DEF_FILTERS 				= "restapi.default.filters";
	protected static String _RESTAPI_DEF_SORTING 				= "restapi.default.sorting";
	protected static String _RESTAPI_DEF_RETURNS 				= "restapi.default.returns";
	protected static String _RESTAPI_DEF_RETURNS_EXCLUDE		= "restapi.default.returns.exclude";
	
	protected static String _RESTAPI_DEF_CONTENT_TYPE			= "restapi.default.noHeader.content-type";
	
	protected static String _RESTAPI_PLUGIN_CLASSNAME 			= "restapi.plugin.implementation";
	
	protected static String _PAGINATION_STARTFROM 	= JsonCrudConfig._LIST_START;
	protected static String _PAGINATION_FETCHSIZE 	= JsonCrudConfig._LIST_FETCHSIZE;
	protected static String _PAGINATION_TOTALCNT 	= JsonCrudConfig._LIST_TOTAL;	
	
	protected static String _PAGINATION_RESULT_SECTION 	= JsonCrudConfig._LIST_RESULT;
	protected static String _PAGINATION_META_SECTION 	= JsonCrudConfig._LIST_META;	
	
	private static String _VERSION = "0.5.8 beta";
		
	private Map<Integer, Map<String, String>> mapAutoUrlCrudkey 	= null;
	private Map<Integer, Map<String, String>> mapMappedUrlCrudkey 	= null;
	
	private Map<String, List<String>> mapApiDisabledHttpMethods 	= new HashMap<String, List<String>>();
	
	private Map<String, String> mapDefaultConfig 					= null;
	private String default_content_type								= TYPE_APP_JSON;
	
	private static Logger logger = Logger.getLogger(CRUDService.class.getName());
	
	public static final String GET 		= "GET";
	public static final String POST 	= "POST";
	public static final String DELETE	= "DELETE";
	public static final String PUT 		= "PUT";
	
	private static Pattern pattNumberic = Pattern.compile("[0-9\\.]+");
	private static Pattern pattDebugMode = Pattern.compile("/about/framework/debug/(.+?)/(true|false)");
	private static Pattern pattConfig = Pattern.compile("/about/framework/config/(.+)");
	
	public CRUDService() {
        super();
	}
	
	public void init()
	{
        logger.log(Level.INFO, "CRUDService.init() start.");
		
        mapAutoUrlCrudkey 	= new HashMap<Integer, Map<String, String>>();
        mapMappedUrlCrudkey = new HashMap<Integer, Map<String, String>>();
        mapDefaultConfig 	= new HashMap<String, String>();
        
        JsonCrudConfig config = JsonCrudRestUtil.getCRUDMgr().getJsonCrudConfig();
        for(String sKey : config.getConfigCrudKeys())
        {
        	if(sKey.equalsIgnoreCase("framework.default"))
        	{
        		Map<String, String> mapConfig = config.getConfig(sKey);
        		for(String sDefKey : mapConfig.keySet())
        		{
        			String sDefVal = mapConfig.get(sDefKey);

        			if(sDefKey.equalsIgnoreCase(_RESTAPI_DEF_CONTENT_TYPE))
        			{
        				default_content_type = sDefVal;
        			}
        			
        			mapDefaultConfig.put(sDefKey, sDefVal);
        		}
        	}
        	else if(sKey.toLowerCase().startsWith("crud."))
    		{
	    		String sCrudKey = sKey.substring("crud.".length());
	    		
	   			Map<String, String> mapConfig = config.getConfig(sKey);
	   			
	   			//Check if disabled HttpMethods : GET, DELETE, UPDATE, POST, TRUE/ALL 
	   			List<String> listDisabledHttpMethods = mapApiDisabledHttpMethods.get(sKey);
	   			if(listDisabledHttpMethods==null)
	   			{
	   				listDisabledHttpMethods = new ArrayList<String>();
		   			String sDisabledHttpMethods = mapConfig.get(_RESTAPI_DISABLED);
		   			if(sDisabledHttpMethods!=null)
		   			{
			   			StringTokenizer tk = new StringTokenizer(sDisabledHttpMethods,",");
			   			while(tk.hasMoreTokens())
			   			{
			   				String sMethodName = tk.nextToken().trim().toUpperCase();
			   				if(sMethodName.length()>0)
			   				{
			   					listDisabledHttpMethods.add(sMethodName);
			   				}	
			   			}
		   			}
		   			mapApiDisabledHttpMethods.put(sKey, listDisabledHttpMethods);
	   			}
	   			
	   			boolean isDisabledAll = mapApiDisabledHttpMethods.containsKey("TRUE") || mapApiDisabledHttpMethods.containsKey("ALL");
	   			if(!isDisabledAll)
	   			{
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
	   			else
	   			{
	   				logger.log(Level.INFO, "[skipped] crud.{0}.restapi is disabled.",sKey);
	   			}
    		}
        }
        logger.log(Level.INFO, "CRUDService.init() completed.");
   }
    
    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {

    	String sHttpMethod = request.getMethod();
    	String sPath = request.getPathInfo();
    	if(sPath==null)
    		sPath = "";
    	
    	if(sPath.endsWith("/"))
    		sPath = sPath.substring(0, sPath.length()-1);
    	
    	boolean isAbout = GET.equals(sHttpMethod) && sPath.startsWith("/about/framework");
    	
    	if(isAbout)
    	{
    		JSONObject jsonAbout = getAbout();
    		
    		if(sPath.startsWith("/about/framework/config"))
    		{
    			Matcher m = pattConfig.matcher(sPath);
	        	if(m.find())
	        	{
	        		String sCrudKey = m.group(1);
	        		
	        		if(!sCrudKey.startsWith("crud."))
	        			sCrudKey = "crud."+sCrudKey;
	        		
	        		JsonCrudConfig config = JsonCrudRestUtil.getCRUDMgr().getJsonCrudConfig();
	        		Map<String, String> mapConfig = config.getConfig(sCrudKey);
	        		
	        		JSONObject jsonConfigVals = new JSONObject();
	        		for(String sKey : mapConfig.keySet())
	        		{
	        			jsonConfigVals.put(sKey, mapConfig.get(sKey));
	        		}
	        		JSONObject jsonConfig = new JSONObject();
	        		jsonConfig.put(sCrudKey, jsonConfigVals);
	        		
	        		jsonAbout.put("config", jsonConfig);
	        	}
    		}
    		else if(sPath.startsWith("/about/framework/debug"))
    		{
	        	Matcher m = pattDebugMode.matcher(sPath);
	        	if(m.find())
	        	{
	        		String sCrudKey = m.group(1);
	        		boolean isDebug = "true".equalsIgnoreCase(m.group(2));
	        		
	        		JsonCrudRestUtil.getCRUDMgr().getJsonCrudConfig().setDebug(sCrudKey, isDebug);
	        	}
        		try {
    				RestApiUtil.processHttpResp(
    						response, 
    						HttpServletResponse.SC_OK, 
    						default_content_type, 
    						getDebugInfo().toString());
    			} catch (IOException e) {
    				throw new ServletException(e);
    			}    			
    		}
    		else if(sPath.equals("/about/framework/sysinfo"))
    		{	
    			jsonAbout.put("SysInfo", getSysInfoJson());
    		}
    		
    		else if(sPath.startsWith("/about/framework/loglevel/"))
    		{
    			if(sPath.endsWith("/DEBUG"))
    			{
    				logger.setLevel(Level.FINEST);
    			}
    			else if(sPath.endsWith("/INFO"))
    			{
    				logger.setLevel(Level.INFO);
    			}
    			else if(sPath.endsWith("/OFF"))
    			{
    				logger.setLevel(Level.OFF);
    			}
    		}
    		
			try {
				RestApiUtil.processHttpResp(
						response, 
						HttpServletResponse.SC_OK, 
						default_content_type, 
						jsonAbout.toString());
			} catch (IOException e) {
				throw new ServletException(e);
			}
    	}
     	else
    	{
     		processHttpMethods(request, response);
    	}
	}
    
    private static JSONObject getSysInfoJson()
    {
    	JSONObject json = new JSONObject();
    	
    	JSONObject jsonWin = Windows.getSystemInfo();
    	if(jsonWin!=null && jsonWin.length()>0)
    	{
    		json.put("windows", jsonWin);
    	}
    	
    	json.put("jvm", CommonInfo.getJDKInfo());
    	json.put("storage", CommonInfo.getDiskInfo());

    	json.put("system.environment", CommonInfo.getEnvProperties());
    	json.put("system.properties", CommonInfo.getSysProperties());
    	
    	return json;
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
   		String sLoggerLevel = "";
   		
   		if(logger.getLevel()!=null)
   		{
   			sLoggerLevel = logger.getLevel().getLocalizedName();
   		}
   		
    	JSONObject json = new JSONObject();
    	json.put("jsoncrud.restapi.version", _VERSION);
    	json.put("jsoncrud.restapi.loglevel", sLoggerLevel);
    	json.put("jsoncrud.framework",JsonCrudRestUtil.getJsonCrudVersion());
    	
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss.SSS");
    	long lCurTimestamp = System.currentTimeMillis();
    	json.put("current.epoch.timestamp", lCurTimestamp);
    	json.put("current.formatted.datetime", df.format(new Date(lCurTimestamp)));
    	TimeZone tz = TimeZone.getDefault();
    	if(tz!=null)
    	{
    		json.put("current.timezone", tz.getDisplayName());
    	}
    	return json;
    }
    
    private JSONObject getDebugInfo()
    {
    	JSONObject json = new JSONObject();
    	JsonCrudConfig config = JsonCrudRestUtil.getCRUDMgr().getJsonCrudConfig();
    	
		Map<String, String> mapAll = config.getAllConfig();
    	for(String sCrudKey : config.getConfigCrudKeys())
    	{
    		if(sCrudKey.startsWith(JsonCrudConfig._PROP_KEY_CRUD))
    		{
	    		boolean isDebug = "true".equalsIgnoreCase(mapAll.get(sCrudKey+"."+JsonCrudConfig._PROP_KEY_DEBUG));
	        	json.put(sCrudKey+"."+JsonCrudConfig._PROP_KEY_DEBUG, isDebug);
    		}
    	}
    	
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
    	long lStartTime  		= System.currentTimeMillis();
    	String sReqUniqueID 	= null;
    	String sDebugAccessLog 	= null;
    	String sbQuery 			= req.getQueryString();
    	
    	StringBuffer sbUrl = new StringBuffer();
    	sbUrl.append(req.getRequestURI());
    	if(sbQuery!=null && sbQuery.trim().length()>0)
    	{
    		sbUrl.append("?").append(sbQuery);
    	}
    	
		String sPathInfo 		= req.getPathInfo();  //{crudkey}/xx/xx
    	
    	JSONObject jsonResult 	= null;
    	JSONArray jArrErrors 	= new JSONArray();
    	
    	boolean isDebug 		= false;

    	long lGzipThresholdBytes= -1;
    	
    	if(sPathInfo==null)
    		sPathInfo = "";

    	File file = RestApiUtil.getWebContentAsFile(req);
    	boolean isWebContent = file!=null?true:false;
    	
    	
    	if(req.getCharacterEncoding()==null || req.getCharacterEncoding().trim().length()==0)
    	{
    		try {
				req.setCharacterEncoding("UTF-8");
			} catch (UnsupportedEncodingException e) {
				logger.log(Level.FINE, e.getMessage(), e);
			}
    	}
    	
    	
    	HttpResp httpReq = new HttpResp();
    	httpReq.setHttp_status(HttpServletResponse.SC_NOT_FOUND);
    	
    	Map<String,String> mapPathParams = new HashMap<String, String>();
		boolean isFilterById = false;

    	sPathInfo = appendSuffix(sPathInfo, "/");
    	
		String[] sPaths = CRUDServiceUtil.getUrlSegments(sPathInfo);

    	int iUrlSeg = sPaths.length;
		
		String sCrudKey = null;
		
		while(sCrudKey==null && iUrlSeg>0)
		{
			//
			Map<String, String> mapMappedUrl = mapMappedUrlCrudkey.get(iUrlSeg);
			if(mapMappedUrl!=null)
			{
				String sConfigUrl = getMatchingConfigUrl(mapMappedUrl, sPaths);
				if(sConfigUrl!=null)
				{
					mapPathParams = getPathParamMap(req, sConfigUrl);
					sCrudKey = mapMappedUrl.get(sConfigUrl);
				}
			}
			//
			if(sCrudKey==null)
			{
				Map<String, String> mapAutoUrl = mapAutoUrlCrudkey.get(iUrlSeg);
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
			//
			if(isWebContent)
			{
				iUrlSeg--;
			}
			else
			{
				break;
			}
		}
		
		
		Map<String, String> mapCrudConfig 	= null;
		CRUDServiceReq crudReq 				= null;
		
		if(sCrudKey!=null)
		{
			mapCrudConfig = JsonCrudRestUtil.getCRUDMgr().getCrudConfigs(sCrudKey);
			if(mapCrudConfig==null)
			{
				sCrudKey = null;
			}
		}
		
		isDebug = JsonCrudRestUtil.isDebugEnabled(sCrudKey);
		// || logger.isLoggable(Level.FINEST); //Ignore logger level
		
		if(mapCrudConfig!=null)
		{			
			String sMethod = req.getMethod().toUpperCase();
			
			List<String> listDisabledMethods = mapApiDisabledHttpMethods.get("crud."+sCrudKey);
			
			boolean isDisabled = listDisabledMethods.contains(sMethod);
			if(isDisabled)
			{
				httpReq.setHttp_status(HttpServletResponse.SC_NOT_FOUND);
			}
			else
			{
				if("true".equalsIgnoreCase(mapCrudConfig.get(_RESTAPI_SERVE_STATICWEB)))
				{
					if(RestApiUtil.serveStaticWeb(req, res))
						return;
				}
				
				ICRUDServicePlugin plugin = null;
				String sPluginClassName = null;
					//
				try {
					try {
						plugin = getPlugin(mapCrudConfig);
						if(plugin!=null)
						{
							sPluginClassName = plugin.getClass().getSimpleName();
						}
						//
						crudReq = new CRUDServiceReq(req, mapCrudConfig);
						crudReq.setCrudKey(sCrudKey);
						crudReq.addUrlPathParam(mapPathParams);
						crudReq.setDebug(isDebug);
						
						sReqUniqueID = RESTApiUtil.getReqUniqueId(crudReq);
						crudReq.setReqUniqueID(sReqUniqueID);
				    	
				    	if(logger.isLoggable(Level.FINE))
				    	{
				    	  	sDebugAccessLog = "[DEBUG] rid:"+sReqUniqueID+" client:"+req.getRemoteAddr()+".start - "+req.getMethod()+" "+sbUrl.toString();
				    	    logger.log(Level.FINE, sDebugAccessLog);
				    		sDebugAccessLog = null;
				    	}
				    	
						
						if(isDebug)
						{
							logger.info("[DEBUG] rid:"+crudReq.getReqUniqueID()+"  echo.attrs:"+crudReq.getEchoJsonAttrs());
						}

						if(plugin!=null)
						{
							long lStart 	= System.currentTimeMillis();
							long lElapsed 	= 0;
							if(isDebug)
							{
								logger.info("[DEBUG] rid:"+crudReq.getReqUniqueID()+" "+sCrudKey+".plugin:"+sPluginClassName+".preProcess.start");
							}
							crudReq = preProcess(plugin, crudReq);
							lElapsed= System.currentTimeMillis()-lStart;
							if(isDebug)
							{
								logger.info("[DEBUG] rid:"+crudReq.getReqUniqueID()+" "+sCrudKey+".plugin:"+sPluginClassName+".preProcess.end - status:"+httpReq.getHttp_status()+" "+lElapsed+"ms");
							}
						}
						
						String sIdFieldName = mapCrudConfig.get(_RESTAPI_ID_ATTRNAME);
						if(sIdFieldName==null || sIdFieldName.trim().length()==0)
							sIdFieldName = "id";
			
						if(!isFilterById)
						{
							//for MappedUrl
							isFilterById = crudReq.getUrlPathParam(sIdFieldName)!=null;
						}
						
						if(isFilterById)
						{
							
							if(GET.equalsIgnoreCase(crudReq.getHttpMethod()) 
									|| PUT.equalsIgnoreCase(crudReq.getHttpMethod()) 
									|| DELETE.equalsIgnoreCase(crudReq.getHttpMethod()))
							{
								String sIdValue = crudReq.getUrlPathParam(sIdFieldName);
								
								if(sIdValue==null && sPaths.length==2)
								{
									sIdValue = sPaths[sPaths.length-1];
								}
								
								if(sIdValue!=null)
								{
									if(crudReq.isIdFieldNumericOnly())
									{
										if(!pattNumberic.matcher(sIdValue).matches())
										{
											crudReq.setSkipJsonCrudDbProcess(true);
											httpReq = getNotFoundResp(crudReq, httpReq);
										}
									}
									crudReq.addCrudFilter(sIdFieldName, sIdValue);
								}
								else
								{
									isFilterById = false;
								}
							}
						}
						//
						
						HttpResp proxyHttpReq = forwardToProxy(crudReq, httpReq);
						
						if(proxyHttpReq!=null)
						{
							httpReq = proxyHttpReq;
						}
						else
						{
							if(!crudReq.isSkipJsonCrudDbProcess() && proxyHttpReq==null)
							{
								if(GET.equalsIgnoreCase(crudReq.getHttpMethod()))
								{
									lGzipThresholdBytes = (long) JsonCrudRestUtil.getCrudConfigNumbericVal(sCrudKey, _RESTAPI_GZIP_THRESHOLD, -1);
									
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
												crudReq.getCrudReturns(),
												crudReq.isReturnsExclude()
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
							
							if(isFilterById && httpReq.getContent_data()==null)
							{
								//valid request id but not found
								httpReq = getNotFoundResp(crudReq, httpReq);
							}
							
							if(httpReq.getHttp_status() == HttpServletResponse.SC_OK 
									&& (httpReq.getContent_type()==null || httpReq.getContent_type().trim().length()==0))
							{
								httpReq.setContent_type(TYPE_APP_JSON); //200 = ;
							}
						}
						///////////////////////////
						if(plugin!=null)
						{
							long lStart = System.currentTimeMillis();
							
							if(isDebug)
							{
								logger.info("[DEBUG] rid:"+crudReq.getReqUniqueID()+" "+sCrudKey+".plugin:"+sPluginClassName+".postProcess.start");
							}
							httpReq = postProcess(plugin, crudReq, httpReq);
							long lElapsed= System.currentTimeMillis()-lStart;
							if(isDebug)
							{
								logger.info("[DEBUG] rid:"+crudReq.getReqUniqueID()+" "+sCrudKey+".plugin:"+sPluginClassName+".postProcess.end - status:"+httpReq.getHttp_status()+" "+lElapsed+"ms");
							}
						}
						
					}
					catch (JsonCrudException e) 
					{
						JsonCrudExceptionList errList = new JsonCrudExceptionList();
						errList.addException(e);
						throw errList;
					}
				}
				catch (JsonCrudExceptionList eList) 
				{
					//unhandled error
					JsonCrudExceptionList handledErrList = new JsonCrudExceptionList();
					
					if(crudReq!=null) 
					{
						long lStart = System.currentTimeMillis();
						if(isDebug)
						{
							logger.info("[DEBUG] rid:"+crudReq.getReqUniqueID()+" "+sCrudKey+".plugin:"+sPluginClassName+".handleException.start - totalException:"+eList.getAllExceptions().size());
						}
						for(JsonCrudException e : eList.getAllExceptions())
						{
							try {
								httpReq = handleException(plugin, crudReq, httpReq, e);
							} catch (JsonCrudException e1) {
								handledErrList.addException(e1);
							}
						}
						if(isDebug)
						{
							long lPluginElapse= System.currentTimeMillis()-lStart;
							logger.info("[DEBUG] rid:"+crudReq.getReqUniqueID()+"  "+sCrudKey+".plugin:"+sPluginClassName+".handleException.end - "+lPluginElapse+"ms");
						}
					}
					
					//Accumulate
					for(JsonCrudException e : handledErrList.getAllExceptions())
					{
						JSONObject jsonEx = new JSONObject();
						jsonEx.put(e.getErrorCode(), e.getErrorMsg());
						jArrErrors.put(jsonEx);
					}
	
				}
				
				boolean isBinary = httpReq.isBytesContent();
				
				if(!isBinary)
				{
					String sContentData = httpReq.getContent_data();
					if(sContentData==null)
						sContentData = "";
					
					boolean isError = (jArrErrors.length()>0) || (sContentData.indexOf("\"errors\":")>-1);
					
					if(isError)
					{
						sContentData = httpReq.getContent_data();
						if(sContentData==null || sContentData.trim().length()==0)
							sContentData = "{}";
						
						jsonResult = new JSONObject(sContentData);
						
						if(jsonResult.has("errors"))
						{
							if(jsonResult.get("errors") instanceof JSONArray)
							{
								JSONArray jArrErrsFromPlugin = jsonResult.getJSONArray("errors");
								for(int i=0; i<jArrErrsFromPlugin.length(); i++)
								{
									jArrErrors.put(jArrErrsFromPlugin.getJSONObject(i));
								}
							}
						}
						
						if(jArrErrors.length()>0)
						{
							jsonResult.put("errors", jArrErrors);
							httpReq.setContent_type(TYPE_APP_JSON);
							httpReq.setContent_data(jsonResult.toString());
							httpReq.setHttp_status(HttpServletResponse.SC_BAD_REQUEST);
						}
					}
					
					//Kepo method, in case lulu forgot to set content type
					String sContentType = httpReq.getContent_type();
					if("".equalsIgnoreCase(sContentType))
					{
						if(sContentData!=null && sContentData.length()>0)
						{
							sContentData = sContentData.trim();
							if(sContentData.startsWith("{") && sContentData.endsWith("}"))
							{
								httpReq.setContent_type(TYPE_APP_JSON);
							}
							else
							{
								httpReq.setContent_type(default_content_type);
							}
						}
					}
					//
				}
			}
		}
		
		try {
			
			RestApiUtil.processHttpResp(
					res, httpReq,
					lGzipThresholdBytes);
		} catch (IOException ex) {
			throw new ServletException(ex.getClass().getSimpleName(), ex);
		}
		
		
		if(isDebug)
		{
			long lElapsed = System.currentTimeMillis()-lStartTime;
    	  	sDebugAccessLog = "[DEBUG] rid:"+sReqUniqueID+" client:"+req.getRemoteAddr()+".end - "+req.getMethod()+" "+sbUrl.toString()+" - "+lElapsed+"ms";;
		    logger.log(Level.FINE, sDebugAccessLog);
		}
    }
    
    private HttpResp getNotFoundResp(CRUDServiceReq crudReq, HttpResp httpReq)
    {
    	return getConfigResp(crudReq, httpReq, "restapi.notfound.");
    }
    
    private HttpResp getConfigResp(CRUDServiceReq crudReq, HttpResp httpReq, String aConfigPrefix)
    {
		int iHttpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

		String sHttpMethod 	= crudReq.getHttpMethod().toLowerCase();
		String sHttpStatus 	= null;
		String sContentType = null;
		String sContentData = null;
		
		List<Map<String, String>> listConfigMaps = new ArrayList<Map<String, String>>();
		listConfigMaps.add(crudReq.getConfigMap()); //endpoint specified
		listConfigMaps.add(mapDefaultConfig); //framework
		
		for(Map<String, String> mapCfg : listConfigMaps)
		{
			if(sHttpStatus==null)
			{
				//restapi 
				sHttpStatus 	= mapCfg.get(aConfigPrefix+sHttpMethod+".status");
				sContentType 	= mapCfg.get(aConfigPrefix+sHttpMethod+".content-type");
				sContentData 	= mapCfg.get(aConfigPrefix+sHttpMethod+".content-data");
			
				if(sHttpStatus==null)
				{
					sHttpStatus 	= mapCfg.get(aConfigPrefix+"status");
					sContentType 	= mapCfg.get(aConfigPrefix+"content-type");
					sContentData 	= mapCfg.get(aConfigPrefix+"content-data");
				}
	    	}
			else
			{
				break;
			}
		}
		
		if(sHttpStatus!=null)
		{
			try {
				iHttpStatus = Integer.parseInt(sHttpStatus.trim());				
			}
			catch(NumberFormatException numEx)
			{
				JSONObject jsonErr 	= new JSONObject();
				jsonErr.put("httpstatus", sHttpStatus);
				jsonErr.put(numEx.getClass().getName(), numEx.getMessage());
				sContentData = jsonErr.toString();
				iHttpStatus = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
			}
    	}
		
		httpReq.setHttp_status(iHttpStatus);
		httpReq.setContent_type(sContentType);
		httpReq.setContent_data(sContentData);

		return httpReq;
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

    private HttpResp forwardToProxy(
    		CRUDServiceReq aCrudReq, HttpResp aHttpResp) throws JsonCrudException
    {
    	boolean isDebug = aCrudReq.isDebug();
    	String sHttpMethod 	= aCrudReq.getHttpMethod().toLowerCase();
    	String sProxyUrlKey = "restapi.proxy."+sHttpMethod+".url";
    	
    	String sProxyUrl 	= aCrudReq.getConfigMap().get(sProxyUrlKey);
    	String sOrgPathInfo = aCrudReq.getHttpServletReq().getPathInfo();
    	
    	if(sProxyUrl==null || sProxyUrl.trim().length()==0)
    	{
    		sProxyUrlKey 	= _RESTAPI_PROXY_URL;
    		sProxyUrl 		= aCrudReq.getConfigMap().get(sProxyUrlKey);
    	}
    	
    	if(sProxyUrl!=null && sProxyUrl.trim().length()>0)
    	{

    		int iPos = sProxyUrl.indexOf("?");
    		
			if(iPos>-1)
			{    				

				String sProxyQuery = sProxyUrl.substring(iPos+1);
				
				if(sProxyQuery!=null && sProxyQuery.length()>0)
				{
					sProxyUrl = sProxyUrl.substring(0, iPos);
					
					Map<String, Map<String,String>> mapParams = CRUDServiceUtil.getQueryParamsMap(sProxyQuery);
					if(mapParams==null)
						mapParams = new HashMap<String, Map<String,String>>();
					
					//
					JSONObject jsonFilters = CRUDServiceUtil.getFilters(mapParams);
					
					if(jsonFilters==null)
						jsonFilters = new JSONObject();
					
					for(String sKey : jsonFilters.keySet())
					{
						aCrudReq.addCrudFilter(sKey, jsonFilters.get(sKey));
					}
					
					//
					try {
						List<String> listSorting = CRUDServiceUtil.getSorting(mapParams);
						
						if(listSorting==null)
							listSorting = new ArrayList<String>();
						
						if(listSorting!=null && listSorting.size()>0)
						{
							for(int i=0; i<listSorting.size(); i++)
							{
								aCrudReq.addCrudSorting(listSorting.get(i));
							}
						}
					} catch (JsonCrudExceptionList e1) {
						e1.printStackTrace();
					}

					//
					List<String> listReturns = CRUDServiceUtil.getReturns(mapParams);
					if(listReturns==null)
					{
						listReturns = CRUDServiceUtil.getReturnsExclude(mapParams);
					}
					
					if(listReturns!=null && listReturns.size()>0)
					{
						for(int i=0; i<listReturns.size(); i++)
						{
							aCrudReq.addCrudReturns(listReturns.get(i));
						}
					}
					
					//
					try {
						long lPagination[] = CRUDServiceUtil.getPaginationStartNFetchSize(mapParams);
						
						if(lPagination.length==2)
						{
							if(lPagination[0]!=0)
							{
								aCrudReq.setPaginationStartFrom(lPagination[0]);
							}
							
							if(lPagination[1]!=0)
							{
								aCrudReq.setPaginationFetchSize(lPagination[1]);
							}
						}
						
					} catch (JsonCrudExceptionList e) {
						e.printStackTrace();
					}
				}
			}
    		
    		StringBuffer sbApiUrl = new StringBuffer();
    		
    		if(sProxyUrl.indexOf("://")==-1) //-- http://  https://   ws://   wss://
    		{
    			HttpServletRequest req 	= aCrudReq.getHttpServletReq();
    			String sApiServer = "127.0.0.1"; //req.getServerName();
    			
    			if("true".equalsIgnoreCase(aCrudReq.getConfigMap().get(_RESTAPI_PROXY_HOSTNAME2IP)))
    			{
	    			try {
						InetAddress apiProvider = InetAddress.getByName(req.getServerName());
						sApiServer = apiProvider.getHostAddress();
					} catch (UnknownHostException e) {
						sApiServer = "127.0.0.1";
					}
    			}
    			//Url Forming  
    			String sProtocol 		= req.getScheme();
    			String sContextRoot 	= req.getContextPath();
    			
    			if(sContextRoot==null || "/".equals(sContextRoot))
    			{
    				sContextRoot = "";
    			}
    			
    			if(!"/".equals(req.getServletPath()))
    			{
    				if(sContextRoot.length()>0 && !"/".equals(sContextRoot))
        			{
        				sContextRoot = sContextRoot + "/";
        			}
	    			sContextRoot = sContextRoot + req.getServletPath();
    			}
    			
    			sbApiUrl.append(sProtocol);
    			sbApiUrl.append("://").append(sApiServer);
    			sbApiUrl.append(":").append(req.getLocalPort()).append(sContextRoot);
    			sbApiUrl.append(sProxyUrl);
    		}
    		else
    		{
    			//full url proxy
    			sbApiUrl.append(sProxyUrl);
    		}

    		if(sbApiUrl.toString().indexOf("?")==-1)
    		{
    			sbApiUrl.append("?1=1");
    		}
    		
    		HttpServletRequest req = aCrudReq.getHttpServletReq();
    		//
   			sbApiUrl.append(constructJsonCrudParamUrl(CRUDServiceUtil._QPARAM_FILTERS, aCrudReq.getCrudFilters()));
  			sbApiUrl.append(constructJsonCrudParamUrl(CRUDServiceUtil._QPARAM_SORTING, aCrudReq.getCrudSorting()));  			   			
  			String sReturnParamName = CRUDServiceUtil._QPARAM_RETURNS;
  			if(req.getParameter(CRUDServiceUtil._QPARAM_RETURNS_EXCLUDE)!=null)
  			{
  				sReturnParamName = CRUDServiceUtil._QPARAM_RETURNS_EXCLUDE;
  			}
  			sbApiUrl.append(constructJsonCrudParamUrl(sReturnParamName, aCrudReq.getCrudReturns()));
  			
  			JSONObject jsonPagination = new JSONObject();
 			jsonPagination.put(_PAGINATION_STARTFROM, aCrudReq.getPaginationStartFrom());
  			jsonPagination.put(_PAGINATION_FETCHSIZE, aCrudReq.getPaginationFetchSize());
  			sbApiUrl.append(constructJsonCrudParamUrl(CRUDServiceUtil._QPARAM_PAGINATION, jsonPagination));
  			
  			
    		//Adding custom Query String
    		List<String> listProcessedParamName = CRUDServiceUtil.reservedParamsList;
    		for(String sParamName : req.getParameterMap().keySet())
    		{
    			if(!listProcessedParamName.contains(sParamName))
    			{
    				String[] paramVals = req.getParameterValues(sParamName);
    				for(int i=0; i<paramVals.length; i++)
    				{
    					sbApiUrl.append("&").append(sParamName).append("=").append(paramVals[i]);
    				}
    			}
    		}
     		
			String sProxyApiUrl = sbApiUrl.toString();
			
    		//Path param
    		JSONObject jsonPathParam = aCrudReq.getUrlPathParam();
    		if(jsonPathParam!=null)
    		{
    			for(String sParamKey : jsonPathParam.keySet())
    			{
    				String sReplaceStr = "\\{"+sParamKey+"\\}";
    				String sReplaceVal = jsonPathParam.getString(sParamKey);
    				sProxyApiUrl = sProxyApiUrl.replaceAll(sReplaceStr, sReplaceVal);
    			}
    		}
    		
    		//debug
			if(isDebug || logger.isLoggable(Level.FINE))
			{
				logger.log(Level.INFO, "[DEBUG] rid:"+aCrudReq.getReqUniqueID()+" jsoncrud.isDebug:"+isDebug+", logger.Level:"+logger.getLevel());
				//
				logger.log(Level.INFO, "[DEBUG] rid:"+aCrudReq.getReqUniqueID()+" Proxy.Request - RequestURL:"+req.getRequestURL().toString());
				logger.log(Level.INFO, "[DEBUG] rid:"+aCrudReq.getReqUniqueID()+" Proxy.Request - RequestURI:"+req.getRequestURI());
				logger.log(Level.INFO, "[DEBUG] rid:"+aCrudReq.getReqUniqueID()+" Proxy.Request - LocalName:LocalPort:"+req.getLocalName()+":"+req.getLocalPort());
				//
				logger.log(Level.INFO, "[DEBUG] rid:"+aCrudReq.getReqUniqueID()+" Proxy.Config  - pathinfo:"+sOrgPathInfo+"  configkey:"+sProxyUrlKey);
				logger.log(Level.INFO, "[DEBUG] rid:"+aCrudReq.getReqUniqueID()+" Proxy.Request - method:"+sHttpMethod+"  url:"+sProxyApiUrl);
				
				String sContentData = aCrudReq.getInputContentData();
				long lContentSize = sContentData!=null?sContentData.length():-1;
				String sContentType = aCrudReq.getInputContentType();				
				if(sContentType!=null)
					sContentType = sContentType.toLowerCase();
				else
					sContentType = "null";
				
				logger.log(Level.INFO, "[DEBUG] rid:"+aCrudReq.getReqUniqueID()+" Proxy.Request - type:"+sContentType+"  data-length:"+lContentSize);
				//
				
				if(lContentSize>-1 && (sContentType.contains("json") || sContentType.contains("text")))
				{
					if(lContentSize>1000)
					{
						sContentData = sContentData.substring(0, 1000) + " ...";
					}
					
					logger.log(Level.INFO, "[DEBUG] rid:"+aCrudReq.getReqUniqueID()+" Proxy.Request - data:"+sContentData);
				}
			}
    		
    		try {
    			HttpResp resp = null;
    			if(sHttpMethod.equalsIgnoreCase("get"))
    			{
    				resp = RestApiUtil.httpGet(sProxyApiUrl);
    			}
    			else if(sHttpMethod.equalsIgnoreCase("post"))
    			{
    				resp = RestApiUtil.httpPost(sProxyApiUrl, 
    						aCrudReq.getInputContentType(), 
    						aCrudReq.getInputContentData());
    			}
    			else if(sHttpMethod.equalsIgnoreCase("delete"))
    			{
    				resp = RestApiUtil.httpDelete(sProxyApiUrl, 
    						aCrudReq.getInputContentType(), 
    						aCrudReq.getInputContentData());
    			}   			
    			else if(sHttpMethod.equalsIgnoreCase("put"))
    			{
    				resp = RestApiUtil.httpPut(sProxyApiUrl, 
    						aCrudReq.getInputContentType(), 
    						aCrudReq.getInputContentData());
    			}   			
    		
    			if(isDebug)
    			{
    				logger.log(Level.INFO, "[DEBUG] rid:"+aCrudReq.getReqUniqueID()+" Proxy.Response - status:"+resp.getHttp_status()+"  type:"+resp.getContent_type()+"  data:"+resp.getContent_data());
    			}
				
    			if(resp!=null)
    			{
    				return resp;				
    			}
			} catch (IOException ioEx) {
				JsonCrudException e = new JsonCrudException(_ERROR_PROXY, ioEx);
				e.setErrorSubject(sProxyUrlKey+":"+sProxyApiUrl);
				throw e;
			}
    	}
    	
    	
    	return null;
    }
    
    private String constructJsonCrudParamUrl(String aParamName, List<String> aList)
    {
		StringBuffer sb = new StringBuffer();
		if(aList!=null && aList.size()>0)
		{
			for(String sVal : aList)
    		{
				if(sb.length()>0)
				{
					sb.append(CRUDServiceUtil.QPARAM_MULTIKEY_SEPARATOR);
				}
				sb.append(sVal);
    		}
		}
		
		if(sb.length()>0)
		{
			sb.insert(0, "&"+aParamName+"=");
		}
		
		return sb.toString();
    }

    private String constructJsonCrudParamUrl(String aParamName, JSONObject aJsonObject)
    {
		StringBuffer sb = new StringBuffer();
		if(aJsonObject!=null && aJsonObject.keySet().size()>0)
		{
			for(String sJsonKeys : aJsonObject.keySet())
    		{
				if(sb.length()>0)
				{
					sb.append(CRUDServiceUtil.QPARAM_MULTIKEY_SEPARATOR);
				}
				sb.append(sJsonKeys).append(CRUDServiceUtil.QPARAM_MULTIKEY_KEYVALUE_SEPARATOR);
				sb.append(aJsonObject.get(sJsonKeys));
    		}
		}
		
		if(sb.length()>0)
		{
			sb.insert(0, "&"+aParamName+"=");
		}

		return sb.toString();
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
    		CRUDServiceReq aCrudReq, HttpResp aHttpResp, JsonCrudException aException) throws JsonCrudException
    {
    	if(aPlugin==null)
    	{
    		throw aException;
    	}
    	return aPlugin.handleException(aCrudReq, aHttpResp, aException);
    }
    
    private ICRUDServicePlugin getPlugin(Map<String, String> aMapCrudConfig) throws JsonCrudException
    {
		ICRUDServicePlugin plugin = null;
		String sPluginClassName = aMapCrudConfig.get(_RESTAPI_PLUGIN_CLASSNAME);
		
		if(sPluginClassName==null || "".equalsIgnoreCase(sPluginClassName))
		{
			//try to get default implementation since no custom plugin
			sPluginClassName = mapDefaultConfig.get(_RESTAPI_PLUGIN_CLASSNAME);
		}
		
    	if(sPluginClassName!=null && sPluginClassName.trim().length()>0)
    	{
    		JsonCrudException e = null;
	    	try {
				plugin = (ICRUDServicePlugin) Class.forName(sPluginClassName).newInstance();
			} catch (InstantiationException ex) {
				e = new JsonCrudException(JsonCrudConfig.ERRCODE_PLUGINEXCEPTION, ex);
			} catch (IllegalAccessException ex) {
				e = new JsonCrudException(JsonCrudConfig.ERRCODE_PLUGINEXCEPTION, ex);
			} catch (ClassNotFoundException ex) {
				e = new JsonCrudException(JsonCrudConfig.ERRCODE_PLUGINEXCEPTION, ex);
			}
	    	
	    	if(e!=null)
	    	{
	    		e.setErrorSubject(sPluginClassName);
	    		throw e;
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
    	
    //**
    public static void main(String args[])
    {
    }
    	
}
