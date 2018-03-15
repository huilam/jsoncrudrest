package hl.jsoncrudrest.restapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import org.json.JSONObject;

import hl.jsoncrud.JsonCrudConfig;
import hl.restapi.service.RESTApiUtil;
import hl.restapi.service.RESTServiceReq;

public class CRUDServiceReq extends RESTServiceReq {

	//
	//	
	private String jsonCrudKey				= null;
	//
	private JSONObject jsonFilters 			= null;
	private List<String> listSorting 		= null;
	private List<String> listReturns 		= null;
	private long pagination_startfrom		= 0;
	private long pagination_fetchsize		= 0;
	
	private long fetchlimit					= 0;
	private boolean isSkipJsonCrudDbProcess	= false;
	//
	
	public CRUDServiceReq(HttpServletRequest aReq, Map<String, String> aCrudConfigMap)
	{
		super(aReq, aCrudConfigMap);
		init(aReq, aCrudConfigMap);
	}
	
	private void init(HttpServletRequest aReq, Map<String, String> aMapCrudConfig)
	{
		addToConfigMap(aMapCrudConfig);
		Map<String, Map<String, String>> mapQueryParams = CRUDServiceUtil.getQueryParamsMap(aReq);
		jsonFilters = CRUDServiceUtil.getFilters(mapQueryParams);		
		listSorting = CRUDServiceUtil.getSorting(mapQueryParams);
		listReturns = CRUDServiceUtil.getReturns(mapQueryParams);
		
		long[] lStartNFetchSize = CRUDServiceUtil.getPaginationStartNFetchSize(mapQueryParams);
		this.pagination_startfrom = lStartNFetchSize[0];
		this.pagination_fetchsize = lStartNFetchSize[1];
		
		String sFetchLimit = getConfigMap().get(CRUDService._RESTAPI_FETCH_LIMIT);
		if(sFetchLimit!=null)
		{
			try {
				this.fetchlimit = Long.parseLong(sFetchLimit);
			} catch(NumberFormatException ex)
			{
				this.fetchlimit = 0;
			}
		}
		
		
		//
		String sJsonAttrEchoPrefix = getConfigMap().get(CRUDService._RESTAPI_ECHO_PREFIX);
		if(sJsonAttrEchoPrefix!=null)
		{
			this.jsonEchoAttrs = RESTApiUtil.extractEchoAttrs(aReq, this.reqInputContentData, sJsonAttrEchoPrefix);
		}
		//
	}
	///
	
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
		return jsonFilters.put(aKey, aValue.toString());
	}
	
	
	public boolean removeCrudFilter(String aKey)
	{
		if(jsonFilters!=null)
		{
			return (jsonFilters.remove(aKey)!=null);
		}
		return false;
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
