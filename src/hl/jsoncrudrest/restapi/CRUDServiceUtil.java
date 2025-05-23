package hl.jsoncrudrest.restapi;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import hl.jsoncrud.CRUDMgr;
import hl.jsoncrud.JsonCrudConfig;
import hl.jsoncrud.JsonCrudException;
import hl.jsoncrud.JsonCrudExceptionList;

public class CRUDServiceUtil {

	private static Logger logger 			= Logger.getLogger(CRUDServiceUtil.class.getName());
	//
	private static String URL_SEPARATOR		= "/";
	
	//
	protected static String _QPARAM_PAGINATION 		= "pagination";
	protected static String _QPARAM_FILTERS 		= "filters";
	protected static String _QPARAM_SORTING 		= "sorting";
	
	protected static String _QPARAM_RETURNS 		= "returns";
	protected static String _QPARAM_RETURNS_EXCLUDE	= "returns.exclude";
	
	public static List<String> reservedParamsList = null;
	static
	{
		reservedParamsList = new ArrayList<String>();
		reservedParamsList.add(_QPARAM_PAGINATION);
		reservedParamsList.add(_QPARAM_FILTERS);
		reservedParamsList.add(_QPARAM_SORTING);
		reservedParamsList.add(_QPARAM_RETURNS);
		reservedParamsList.add(_QPARAM_RETURNS_EXCLUDE);
	}
	//
	private static final String QPARAM_SEPARATOR 			= "&";
	private static final String QPARAM_KEYVALUE_SEPARATOR 	= "=";
	
	public static String QPARAM_MULTIKEY_KEYVALUE_SEPARATOR = ":";
	public static String QPARAM_MULTIKEY_SEPARATOR 			= ",";
	
	public static  Pattern pattInVals = Pattern.compile("(\\.in:.+)(?:,.+?\\:)");

    protected static Map<String, Map<String,String>> getQueryParamsMap(HttpServletRequest req)
    {
		return getQueryParamsMap(req.getQueryString());	
    }
    
    public static Map<String, Map<String,String>> getQueryParamsMap(String aQueryString)
    {
    	Map<String, Map<String,String>> mapQueryParams = new LinkedHashMap<String, Map<String,String>>();
    	String sQueryString = urlDecode(aQueryString);
    	if(sQueryString!=null)
    	{
			for(String sQueryParam : sQueryString.split(QPARAM_SEPARATOR))
			{
				String[] sQParam = sQueryParam.split(QPARAM_KEYVALUE_SEPARATOR);
				if(sQParam.length>1)
				{
					mapQueryParams.put(sQParam[0], parseMultiKV(sQParam[1]));
				}
			}
    	}
    	return mapQueryParams;
    }
    
    protected static Map<String, String> parseMultiKV(String aMultiKVConfig)
    {
		Map<String, String> mapParamVals = new LinkedHashMap<String,String>();
		
		if(aMultiKVConfig!=null && aMultiKVConfig.trim().length()>0)
		{
			String[] sQParamVals = aMultiKVConfig.split(QPARAM_MULTIKEY_SEPARATOR);
			for(String sQParamVal : sQParamVals)
			{
				String[] sKVals = sQParamVal.split(QPARAM_MULTIKEY_KEYVALUE_SEPARATOR);
				sKVals[0] = urlDecode(sKVals[0]);
				if(sKVals.length==1)
				{
					sKVals = new String[] {sKVals[0], ""};
				}
				mapParamVals.put(sKVals[0], urlDecode(sKVals[1]));
			}
		}
		return mapParamVals;
    }

    public static long[] getPaginationStartNFetchSize(Map<String, Map<String, String>> mapQueryParams) throws JsonCrudExceptionList
    {
    	JsonCrudExceptionList exList = new JsonCrudExceptionList();
    	
    	if(mapQueryParams==null)
    		return new long[] {0,0};
    	
    	Map<String,String> mapPagination 	= mapQueryParams.get(_QPARAM_PAGINATION);
		
		long iFetchStartFrom 	= 0;
		long iFetchSize 		= 0;
		
		if(mapPagination!=null)
		{
			String sFetchStartFrom 	= mapPagination.get(JsonCrudConfig._LIST_START);
			if(sFetchStartFrom!=null && sFetchStartFrom.trim().length()>0)
			{
				try {
					iFetchStartFrom = Long.parseLong(sFetchStartFrom);
				}
				catch(NumberFormatException ex)
				{
					JsonCrudException e = new JsonCrudException(JsonCrudConfig.ERRCODE_INVALID_PAGINATION, 
							"Invalid "+_QPARAM_PAGINATION+" value !");
					e.setErrorSubject(JsonCrudConfig._LIST_START+":"+sFetchStartFrom);
					exList.addException(e);
				}
			}
			//
			String sFetchSize 		= mapPagination.get(JsonCrudConfig._LIST_FETCHSIZE);
			if(sFetchSize!=null && sFetchSize.trim().length()>0)
			{
				try {
					iFetchSize = Long.parseLong(sFetchSize);
				}
				catch(NumberFormatException ex)
				{
					JsonCrudException e = new JsonCrudException(JsonCrudConfig.ERRCODE_INVALID_PAGINATION, 
							"Invalid "+_QPARAM_PAGINATION+" value !");
					e.setErrorSubject(JsonCrudConfig._LIST_FETCHSIZE+":"+sFetchSize);
					exList.addException(e);
				}
			}
		}
		
		if(exList.hasExceptions())
		{
			throw exList;
		}
		
		return new long[] {iFetchStartFrom, iFetchSize};
    }
    
    public static JSONObject getFilters(Map<String, Map<String, String>> mapQueryParams)
    {
    	if(mapQueryParams==null)
    		return null;
    	
    	Map<String, String> mapFilters = mapQueryParams.get(_QPARAM_FILTERS);
    	return getKeyValue(mapFilters);
    }
    
    public static List<String> getReturns(Map<String, Map<String, String>> mapQueryParams)
    {
    	return getReturns(_QPARAM_RETURNS, mapQueryParams);
    }
    
    public static List<String> getReturnsExclude(Map<String, Map<String, String>> mapQueryParams)
    {
    	return getReturns(_QPARAM_RETURNS_EXCLUDE, mapQueryParams);
    }
    
    private static List<String> getReturns(String aReturnKey, Map<String, Map<String, String>> mapQueryParams)
    {
    	//include take precedence over exclude
    	Map<String, String> mapReturn = mapQueryParams.get(aReturnKey);
    	
    	if(mapReturn==null)
    	{
        	return null;
    	}
    	
    	List<String> listReturn = new ArrayList<String>();
    	for(String sAttrName : mapReturn.keySet())
    	{
    		listReturn.add(sAttrName);
    	}
    	
    	return listReturn;
    }

    public static List<String> getSorting(Map<String, Map<String, String>> mapQueryParams) throws JsonCrudExceptionList
    {
    	JsonCrudExceptionList exList = new JsonCrudExceptionList();
    	
    	Map<String, String> mapSorting = mapQueryParams.get(_QPARAM_SORTING);
    	
    	if(mapSorting==null)
    		return null;
    	
    	List<String> listSortFields = null;
    	if(mapSorting!=null)
    	{
    		listSortFields = new ArrayList<String>();
	    	for(String sKey : mapSorting.keySet())
	    	{
	    		String sSortField 	= sKey;
	    		String sSortDir 	= mapSorting.get(sKey);
	    		if(sSortDir==null)
	    			sSortDir = "";

	    		if(sSortDir.trim().length()>0)
	    		{
	    			if(!(sSortDir.equalsIgnoreCase(CRUDMgr.JSONSORTING_DESC) || sSortDir.equalsIgnoreCase(CRUDMgr.JSONSORTING_ASC)))
	    			{
	    				JsonCrudException e = new JsonCrudException(JsonCrudConfig.ERRCODE_INVALID_SORTING, 
								"Invalid "+_QPARAM_SORTING+" value ! ");
	    				e.setErrorSubject(sSortField+":"+sSortDir);
	    				exList.addException(e);
	    				continue;
	    			}	    			

	    			sSortField = sSortField + "."+sSortDir;
	    		}
	    		
	    		listSortFields.add(sSortField);
	    	}
	    	
	    	if(exList.hasExceptions())
	    		throw exList;
    	}
    	return listSortFields;
    }
    

    private static JSONObject getKeyValue(Map<String, String> mapKV)
    {
    	JSONObject jsonKV = null;
    	
    	if(mapKV!=null && mapKV.size()>0)
    	{
    		jsonKV = new JSONObject();
    	
	    	for(String sFilterKey : mapKV.keySet())
	    	{
	    		String sVal = mapKV.get(sFilterKey);
	    		if(!"".equalsIgnoreCase(sVal))
	    		{
	    			jsonKV.put(sFilterKey, sVal);
	    		}
	    	}
       	}
    	    	
    	return jsonKV;
    }    
    

	private static String urlDecode(String aUrl)
	{
		if(aUrl==null)
			return "";
		
		try {
			return URLDecoder.decode(aUrl, "UTF-8");
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			return aUrl;
		}
	}
	
    
	
	///
	public static String[] getUrlSegments(String aURL)
	{
		if(aURL==null)
			return new String[]{};
		
		return aURL.trim().substring(1).split(URL_SEPARATOR);
	}
	
	
	private static String replaceInMultiValsComma(final String aInputStr)
	{
		if(aInputStr==null || aInputStr.indexOf(".in:")==-1)
			return aInputStr;
		
		int iInPos = 0;
		
		String sTmp = "";
		Matcher m = pattInVals.matcher(aInputStr);
		
		while(true)
		{
			sTmp = "";
			
			if(m.find(iInPos))
			{
				sTmp = m.group(1);
				iInPos = m.end();
			}
			else
			{
				iInPos = aInputStr.indexOf(".in:", iInPos);
				if(iInPos>-1)
				{
					sTmp = aInputStr.substring(iInPos);
						
					int iLastPos = sTmp.lastIndexOf(",");
					if(iLastPos>-1)
					{
						String sTmp2 = sTmp.substring(iLastPos+1);
						if(sTmp2.matches("[:=]"))
						{
							sTmp = sTmp2.substring(0, iLastPos);
						}
					}
				}
			}
			
			if(sTmp.length()>0)
			{	
				return aInputStr.replaceAll(sTmp, sTmp.replaceAll(",", ";"));
			}
			else
			{
				break;
			}
		}
		
		return aInputStr;
	}
	
	public static void main(String args[]) throws Exception
	{
		String sTestURL1 = "xxx.in:111;222,333,alertTimestamp.to:1620785324386";
		String sTestURL2 = "xxx.in:111;222;333,yyy.in:777;888;000";
		String sTestURL3 = "xxx.in:111,222,333,yyy.in:777,888,000";
		String sTestURL4 = "xxx.in:111,222,333";
		String sTestURL5 = "xxx.in:111,222,333,yyy.from:2";
				
		String[] strTest = new String[] {sTestURL1, sTestURL2, sTestURL3, sTestURL4, sTestURL5};
		
		for(String sTest : strTest)
		{
			
			System.out.println("1:"+sTest);
			System.out.println("2:"+replaceInMultiValsComma(sTest));
			System.out.println();
		}
		
	}
	
}
