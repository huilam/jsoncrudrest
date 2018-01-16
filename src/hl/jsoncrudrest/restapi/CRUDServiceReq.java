package hl.jsoncrudrest.restapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import hl.common.http.RestApiUtil;

public class CRUDServiceReq {

	//
	private String urlPath 						= null;
	private HttpServletRequest httpServletReq	= null;
	//
	private String reqInputContentType		= null;
	private String reqInputContentData		= null;
	
	private Map<String, String> mapConfigs	= null;
	
	//
	private String jsonCrudKey 				= null;	
	private JSONObject jsonFilters 			= null;	
	private JSONObject jsonEchoAttrs 		= null;
	private List<String> listSorting 		= null;
	private List<String> listReturns 		= null;
	private long pagination_startfrom		= 0;
	private long pagination_fetchsize		= 0;
	
	private long fetchlimit					= 0;
	//
	
	public CRUDServiceReq(HttpServletRequest aReq, Map<String, String> aCrudConfigMap)
	{
		this.httpServletReq = aReq;
		this.mapConfigs = aCrudConfigMap;
			
		init(aReq, aCrudConfigMap);
	}
	
	private void init(HttpServletRequest aReq, Map<String, String> aMapCrudConfig)
	{
		this.urlPath = aReq.getPathInfo();  //without context root
		
		this.reqInputContentType = aReq.getContentType();
		this.reqInputContentData = RestApiUtil.getReqContent(aReq);
		
		String[] sPaths = CRUDServiceUtil.getUrlSegments(this.urlPath);
		this.jsonCrudKey = sPaths[0];
		
		Map<String, Map<String, String>> mapQueryParams = CRUDServiceUtil.getQueryParamsMap(aReq);
		jsonFilters = CRUDServiceUtil.getFilters(mapQueryParams);		
		listSorting = CRUDServiceUtil.getSorting(mapQueryParams);
		listReturns = CRUDServiceUtil.getReturns(mapQueryParams);
		
		long[] lStartNFetchSize = CRUDServiceUtil.getPaginationStartNFetchSize(mapQueryParams);
		this.pagination_startfrom = lStartNFetchSize[0];
		this.pagination_fetchsize = lStartNFetchSize[1];
		
		String sFetchLimit = aMapCrudConfig.get(CRUDService._RESTAPI_FETCH_LIMIT);
		if(sFetchLimit!=null)
		{
			try {
				this.fetchlimit = Long.parseLong(sFetchLimit);
			} catch(NumberFormatException ex)
			{
				this.fetchlimit = 0;
			}
		}
		
		String sJsonAttrEchoPrefix = aMapCrudConfig.get(CRUDService._RESTAPI_ECHO_JSONATTR_PREFIX);
		if(sJsonAttrEchoPrefix!=null && sJsonAttrEchoPrefix.trim().length()>0)
		{
			if(this.reqInputContentData!=null && this.reqInputContentData.length()>0)
			{
				if(this.reqInputContentData.startsWith("{"))
				{
					JSONObject jsonTmp = new JSONObject(this.reqInputContentData);
					for(String sKey : jsonTmp.keySet())
					{
						if(sKey.startsWith(sJsonAttrEchoPrefix))
						{
							jsonEchoAttrs.put(sKey, jsonTmp.get(sKey));
						}
					}
				}
			}
		}
	}
	///
	
	public String getCrudKey()
	{
		return jsonCrudKey;
	}
	
	public Map<String, String> getCrudConfigMap()
	{
		if(mapConfigs==null)
			return new HashMap<String, String>();
		return mapConfigs;
	}
	
	public Map<String, String> addCrudConfigMap(String aKey, String aValue)
	{
		if(mapConfigs==null)
			mapConfigs = new HashMap<String, String>();
		mapConfigs.put(aKey, aValue);
		return mapConfigs;
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
			jsonFilters = new JSONObject();
		return jsonFilters.put(aKey, aValue);
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
			return new ArrayList<String>();
		listReturns.add(aSortingStr);
		return listReturns;
	}	
	
	public JSONObject getEchoJsonAttrs()
	{
		return jsonEchoAttrs;
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
			return new ArrayList<String>();
		listSorting.add(aSortingStr);
		return listSorting;
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
	
	public String getInputContentData()
	{
		return this.reqInputContentData;
	}
	
	public void setInputContentData(String aContentData)
	{
		this.reqInputContentData = aContentData;
	}

	public String getInputContentType()
	{
		return this.reqInputContentType;
	}
	
	public void setInputContentType(String aContentType)
	{
		this.reqInputContentType = aContentType;
	}		
	
	public String getHttpMethod()
	{
		return this.httpServletReq.getMethod();
	}
	
	public HttpServletRequest getHttpServletReq()
	{
		return this.httpServletReq;
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
		sb.append("\n").append("PaginationStartFrom:").append(getPaginationStartFrom());
		sb.append("\n").append("PaginationFetchSize:").append(getPaginationFetchSize());
		//
		sb.append("\n").append("FetchLimit:").append(getFetchLimit());
		sb.append("\n").append("EchoAttrs:").append(getEchoJsonAttrs());
		
		return sb.toString();
	}
}
