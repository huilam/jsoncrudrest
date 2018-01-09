package hl.jsoncrud.restapi;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;

import hl.jsoncrud.common.http.RestApiUtil;

public class CRUDServiceReq {

	//
	private String urlPath 						= null;
	private HttpServletRequest httpServletReq	= null;
	//
	private String reqInputContentType		= null;
	private String reqInputContentData		= null;
	
	private Map<String, String> mapConfigs	= null;
	
	//
	private String jsonCrudKey 			= null;	
	private JSONObject jsonFilters 		= null;	
	private List<String> listSorting 	= null;
	private long pagination_startfrom	= 0;
	private long pagination_fetchsize	= 0;
	//
	private boolean isInit				= false;
	
	
	
	public CRUDServiceReq(HttpServletRequest aReq, Map<String, String> aCrudConfigMap)
	{
		this.httpServletReq = aReq;
		this.mapConfigs = aCrudConfigMap;
			
		init(aReq);
	}
	
	private void init(HttpServletRequest aReq)
	{
		this.urlPath = aReq.getPathInfo();  //without context root
		
		this.reqInputContentType = aReq.getContentType();
		this.reqInputContentData = RestApiUtil.getReqContent(aReq);
		
		String[] sPaths = CRUDServiceUtil.getUrlSegments(this.urlPath);
		this.jsonCrudKey = sPaths[0];
		
		Map<String, Map<String, String>> mapQueryParams = CRUDServiceUtil.getQueryParamsMap(aReq);
		jsonFilters = CRUDServiceUtil.getFilters(mapQueryParams);		
		listSorting = CRUDServiceUtil.getSorting(mapQueryParams);
		
		long[] lStartNFetchSize = CRUDServiceUtil.getPaginationStartNFetchSize(mapQueryParams);
		this.pagination_startfrom = lStartNFetchSize[0];
		this.pagination_fetchsize = lStartNFetchSize[1];
		
		isInit = true;
	}
	///
	
	public JSONObject getCrudFilters()
	{
		return jsonFilters;
	}
	
	public JSONObject addCrudFilter(String aKey, Object aValue)
	{
		return jsonFilters.put(aKey, aValue);
	}
	
	public List<String> getCrudSorting()
	{
		return listSorting;
	}	
	
	public long getPaginationStartFrom()
	{
		return this.pagination_startfrom;
	}
	
	public long getPaginationFetchSize()
	{
		return this.pagination_fetchsize;
	}
	
	public String getInputContentData()
	{
		return this.reqInputContentData;
	}
	
	public String getInputContentType()
	{
		return this.reqInputContentType;
	}
	
	public String getHttpMethod()
	{
		return this.httpServletReq.getMethod();
	}
	
	public HttpServletRequest getHttpServletReq()
	{
		return this.httpServletReq;
	}
}
