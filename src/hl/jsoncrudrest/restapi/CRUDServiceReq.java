package hl.jsoncrudrest.restapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import org.json.JSONObject;

import hl.jsoncrud.CRUDMgr;
import hl.jsoncrud.DBColMeta;
import hl.jsoncrud.JsonCrudConfig;
import hl.jsoncrud.JsonCrudException;
import hl.jsoncrud.JsonCrudExceptionList;
import hl.jsoncrud.JsonCrudRestUtil;
import hl.restapi.service.RESTApiUtil;
import hl.restapi.service.RESTServiceReq;

public class CRUDServiceReq extends RESTServiceReq {

	//
	protected static String _RESTAPI_ID_ATTRNAME	= "restapi.id";
	//	
	private String jsonCrudKey				= null;
	private DBColMeta colIdField			= null;
	//
	private JSONObject jsonFilters 			= null;
	private List<String> listSorting 		= null;
	private List<String> listReturns 		= null;
	private boolean isReturnsExclude		= false;
	private long pagination_startfrom		= 0;
	private long pagination_fetchsize		= 0;
	
	private long fetchlimit					= 0;
	private boolean isSkipJsonCrudDbProcess	= false;
	private boolean isDebug					= false;
	//
	
	public CRUDServiceReq(HttpServletRequest aReq, Map<String, String> aCrudConfigMap) throws JsonCrudExceptionList
	{
		super(aReq, aCrudConfigMap);
		init(aReq, aCrudConfigMap);
	}
	
	private String[] parseMultiStringConfig(String aConfigValue, String aSeparator)
	{
		if(aConfigValue==null || aConfigValue.trim().length()==0)
			return null;
		return aConfigValue.trim().split(aSeparator);
	}
	
	private void init(HttpServletRequest aReq, Map<String, String> aMapCrudConfig) throws JsonCrudExceptionList
	{
		JsonCrudExceptionList exceptionList = new JsonCrudExceptionList();
		
		addToConfigMap(aMapCrudConfig);
		Map<String, Map<String, String>> mapQueryParams = CRUDServiceUtil.getQueryParamsMap(aReq);
		jsonFilters = CRUDServiceUtil.getFilters(mapQueryParams);		
		try {
			listSorting = CRUDServiceUtil.getSorting(mapQueryParams);
		} catch (JsonCrudExceptionList e) {
			exceptionList.addExceptionList(e);
		}
		listReturns = CRUDServiceUtil.getReturns(mapQueryParams);
		isReturnsExclude = false;
		if(listReturns==null)
		{
			listReturns = CRUDServiceUtil.getReturnsExclude(mapQueryParams);
			if(listReturns!=null)
			{
				isReturnsExclude = true;
			}
		}
		
		long[] lStartNFetchSize = null;
		try {
			lStartNFetchSize = CRUDServiceUtil.getPaginationStartNFetchSize(mapQueryParams);
			this.pagination_startfrom = lStartNFetchSize[0];
			this.pagination_fetchsize = lStartNFetchSize[1];
		} catch (JsonCrudExceptionList e) {
			exceptionList.addExceptionList(e);
		}
		
		Map<String, String> mapConfig = getConfigMap();
		//////////
		if(jsonFilters==null)
		{
			String sDefFilters = mapConfig.get(CRUDService._RESTAPI_DEF_FILTERS);
			Map<String, String> mapFilters = CRUDServiceUtil.parseMultiKV(sDefFilters);
			if(mapFilters!=null && mapFilters.size()>0)
			{
				jsonFilters = new JSONObject();
				for(String sKey : mapFilters.keySet())
				{
					jsonFilters.put(sKey, mapFilters.get(sKey));
				}
			}
		}
		//////////
		if(listSorting==null)
		{
			String sDefSorting = mapConfig.get(CRUDService._RESTAPI_DEF_SORTING);
			String configs[] = parseMultiStringConfig(sDefSorting, ",");
			if(configs!=null && configs.length>0)
			{
				listSorting = new ArrayList<String>();
				for(String c: configs)
				{
					listSorting.add(c);
				}
			}
		}
		//////////
		if(listReturns==null)
		{
			isReturnsExclude 	= false;
			String sDefReturns 	= mapConfig.get(CRUDService._RESTAPI_DEF_RETURNS);
			if(sDefReturns==null || sDefReturns.trim().length()==0)
			{
				sDefReturns = mapConfig.get(CRUDService._RESTAPI_DEF_RETURNS_EXCLUDE);
				if(sDefReturns!=null && sDefReturns.trim().length()>0)
				{
					isReturnsExclude = true;
				}
			}
			
			String configs[] = parseMultiStringConfig(sDefReturns, ",");
			if(configs!=null && configs.length>0)
			{
				listReturns = new ArrayList<String>();
				for(String c: configs)
				{
					listReturns.add(c);
				}
			}
		}
		//////////		
		
		if(this.pagination_startfrom==0)
		{
			String sFetchStart = mapConfig.get(CRUDService._RESTAPI_DEF_PAGINATION_START);
			
			if(sFetchStart!=null && sFetchStart.trim().length()>0)
			{
				try {
					this.pagination_startfrom = Long.parseLong(sFetchStart);
				} catch(NumberFormatException ex)
				{
					this.pagination_startfrom = 0;
				}
			}
		}
		
		//////////		
		
		if(this.pagination_fetchsize==0)
		{
			String sFetchSize = mapConfig.get(CRUDService._RESTAPI_DEF_PAGINATION_FETCHSIZE);
			
			if(sFetchSize!=null && sFetchSize.trim().length()>0)
			{
				try {
					this.pagination_fetchsize = Long.parseLong(sFetchSize);
				} catch(NumberFormatException ex)
				{
					this.pagination_fetchsize = 0;
				}
			}
		}		
		//////////
		String sFetchLimit = mapConfig.get(CRUDService._RESTAPI_FETCH_LIMIT);
		
		if(sFetchLimit!=null && sFetchLimit.trim().length()>0)
		{
			try {
				this.fetchlimit = Long.parseLong(sFetchLimit);
			} catch(NumberFormatException ex)
			{
				this.fetchlimit = 0;
			}
		}
		
		//////////
		String sJsonAttrEchoPrefix = mapConfig.get(CRUDService._RESTAPI_ECHO_PREFIX);
		if(sJsonAttrEchoPrefix!=null)
		{
			this.jsonEchoAttrs = RESTApiUtil.extractEchoAttrs(aReq, this.reqInputContentData, sJsonAttrEchoPrefix);
		}
		//
		
		if(exceptionList.hasExceptions())
			throw exceptionList;
		
	}
	///
	
	public boolean isReturnsExclude()
	{
		return isReturnsExclude;
	}
	
	
	public boolean isIdFieldNumericOnly()
	{
		if(colIdField==null)
		{
			String sIdFieldName = getConfigMap().get(_RESTAPI_ID_ATTRNAME);
			if(sIdFieldName==null)
				sIdFieldName = "Id";
				
			CRUDMgr m = JsonCrudRestUtil.getCRUDMgr();
			String sCrudKey = getCrudKey();
			if(m!=null && sCrudKey!=null)
				colIdField = m.getDBColMetaByJsonName(sCrudKey, sIdFieldName);		
		}
		
		if(colIdField!=null)
			return colIdField.isNumeric();
		else
			return false;
	}
	
	public void setCrudKey(String aCrudkey)
	{
		if(!aCrudkey.startsWith(JsonCrudConfig._PROP_KEY_CRUD+"."))
		{
			aCrudkey = JsonCrudConfig._PROP_KEY_CRUD+"."+aCrudkey;
		}
		
		this.jsonCrudKey = aCrudkey;
	}
	
	public String getCrudKey()
	{
		return this.jsonCrudKey;
	}
	
	public boolean isSkipJsonCrudDbProcess() {
		return isSkipJsonCrudDbProcess;
	}

	public void setSkipJsonCrudDbProcess(boolean isSkipJsonCrudDbProcess) {
		this.isSkipJsonCrudDbProcess = isSkipJsonCrudDbProcess;
	}

	public JSONObject getCrudFilters()
	{
		if(jsonFilters==null)
			return new JSONObject();
		return jsonFilters;
	}
	
	public JSONObject addCrudFilter(String aKey, Object aValue)
	{
		if(jsonFilters==null)
		{
			jsonFilters = new JSONObject();
		}
		return jsonFilters.put(aKey, aValue);
	}
	
	
	public boolean removeCrudFilter(String aKey)
	{
		if(jsonFilters!=null)
		{
			return (jsonFilters.remove(aKey)!=null);
		}
		return false;
	}
	
	public String[] getCrudReturnsArray()
	{
		List<String> list = getCrudReturns();
		return list.toArray(new String[list.size()]);
	}
	
	public List<String> getCrudReturns()
	{
		if(listReturns==null)
			return new ArrayList<String>();
		return listReturns;
	}
	
	public List<String> addCrudReturns(String aSortingStr)
	{
		if(listReturns==null)
		{
			listReturns = new ArrayList<String>();
		}
		listReturns.add(aSortingStr);
		return listReturns;
	}	
	
	public boolean removeCrudReturns(String aSortingStr)
	{
		if(listReturns!=null)
		{
			return listReturns.remove(aSortingStr);
		}
		return false;
	}	
	
	public String[] getCrudSortingArray()
	{
		List<String> list = getCrudSorting();
		return list.toArray(new String[list.size()]);
	}
	
	public List<String> getCrudSorting()
	{
		if(listSorting==null)
			return new ArrayList<String>();
		return listSorting;
	}
	
	public List<String> addCrudSorting(String aSortingStr)
	{
		if(listSorting==null)
		{
			listSorting = new ArrayList<String>();
		}
		listSorting.add(aSortingStr);
		return listSorting;
	}	
	
	public boolean removeCrudSorting(String aSortingStr)
	{
		if(listSorting!=null)
		{
			return listSorting.remove(aSortingStr);
		}
		return false;
	}	
	
	public long getPaginationStartFrom()
	{
		return this.pagination_startfrom;
	}
	
	public void setPaginationStartFrom(long aStarFrom)
	{
		this.pagination_startfrom = aStarFrom;
	}
	
	public long getPaginationFetchSize()
	{
		return this.pagination_fetchsize;
	}
	
	public void setPaginationFetchSize(long aFetchSize)
	{
		this.pagination_fetchsize = aFetchSize;
	}
	
	public long getFetchLimit()
	{
		return this.fetchlimit;
	}
	
	public boolean isDebug() {
		return isDebug;
	}

	public void setDebug(boolean isDebug) {
		this.isDebug = isDebug;
	}

	public long executeSQL(String aSQL, Object[] aObjParams) throws JsonCrudException
	{
		return JsonCrudRestUtil.getCRUDMgr().executeSQL(
				getCrudKey(), 
				aSQL, aObjParams);
	}
	
	public JSONObject retrieveBySQL(String aSQL, Object[] aObjParams) throws JsonCrudException
	{
		return JsonCrudRestUtil.getCRUDMgr().retrieveBySQL(
				getCrudKey(), 
				aSQL, aObjParams, 
				getPaginationStartFrom(), getPaginationFetchSize());
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("req.getPathInfo:").append(getHttpServletReq().getPathInfo());
		sb.append("\n").append("CrudKey:").append(getCrudKey());
		sb.append("\n").append("HttpMethod:").append(getHttpMethod());
		sb.append("\n").append("InputContentType:").append(getInputContentType());
		sb.append("\n").append("InputContentData:").append(getInputContentData());
		sb.append("\n").append("CrudFilters:").append(getCrudFilters());
		sb.append("\n").append("CrudSorting:").append(getCrudSorting());
		sb.append("\n").append("CrudReturns:").append(getCrudReturns());
		sb.append("\n").append("urlPathParam:").append(getUrlPathParam());
		sb.append("\n").append("PaginationStartFrom:").append(getPaginationStartFrom());
		sb.append("\n").append("PaginationFetchSize:").append(getPaginationFetchSize());
		//
		sb.append("\n").append("FetchLimit:").append(getFetchLimit());
		sb.append("\n").append("EchoAttrs:").append(getEchoJsonAttrs());
		
		return sb.toString();
	}
}
