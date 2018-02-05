package hl.jsoncrudrest.restapi;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import hl.jsoncrud.JsonCrudConfig;

public class CRUDServiceUtil {

	private static String URL_SEPARATOR		= "/";
	
	//
	protected static String _QPARAM_PAGINATION 		= "pagination";
	protected static String _QPARAM_FILTERS 		= "filters";
	protected static String _QPARAM_SORTING 		= "sorting";
	protected static String _QPARAM_RETURNS 		= "returns";	
	//
	private static final String QPARAM_SEPARATOR 			= "&";
	private static final String QPARAM_KEYVALUE_SEPARATOR 	= "=";
	
	private static String QPARAM_MULTIKEY_KEYVALUE_SEPARATOR = ":";
	private static String QPARAM_MULTIKEY_SEPARATOR = ",";

    protected static Map<String, Map<String,String>> getQueryParamsMap(HttpServletRequest req)
    {
		Map<String, Map<String,String>> mapQueryParams = new HashMap<String, Map<String,String>>();
		
		String sQueryString = req.getQueryString();
		if(sQueryString!=null)
		{
			for(String sQueryParam : req.getQueryString().split(QPARAM_SEPARATOR))
			{
				String[] sQParam = sQueryParam.split(QPARAM_KEYVALUE_SEPARATOR);
				Map<String, String> mapParamVals = new HashMap<String,String>();
				
				if(sQParam.length>1)
				{
					String[] sQParamVals = sQParam[1].split(QPARAM_MULTIKEY_SEPARATOR);
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
				mapQueryParams.put(sQParam[0], mapParamVals);
			}
		}
		return mapQueryParams;
    }

    public static long[] getPaginationStartNFetchSize(Map<String, Map<String, String>> mapQueryParams)
    {
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
				iFetchStartFrom = Long.parseLong(sFetchStartFrom);
			}
			//
			String sFetchSize 		= mapPagination.get(JsonCrudConfig._LIST_FETCHSIZE);
			if(sFetchSize!=null && sFetchSize.trim().length()>0)
			{
				iFetchSize = Long.parseLong(sFetchSize);
			}
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
    	Map<String, String> mapReturn = mapQueryParams.get(_QPARAM_RETURNS);
    	
    	if(mapReturn==null)
    		return null;
    	
    	List<String> listReturn = new ArrayList<String>();
    	for(String sAttrName : mapReturn.keySet())
    	{
    		listReturn.add(sAttrName);
    	}
    	
    	return listReturn;
    }

    public static List<String> getSorting(Map<String, Map<String, String>> mapQueryParams)
    {
    	Map<String, String> mapSorting = mapQueryParams.get(_QPARAM_SORTING);
    	
    	if(mapSorting==null)
    		return null;
    	
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
    

    private static JSONObject getKeyValue(Map<String, String> mapKV)
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
    

	private static String urlDecode(String aUrl)
	{
		try {
			return URLDecoder.decode(aUrl, "UTF-8");
		} catch (UnsupportedEncodingException e) {
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
}
