package hl.jsoncrud.restapi;

import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import hl.jsoncrud.JsonCrudConfig;
import hl.jsoncrud.JsonCrudRestUtil;
import hl.jsoncrud.common.http.HttpResp;
import hl.jsoncrud.common.http.RestApiUtil;

public class CRUDService extends HttpServlet {

	private static final long serialVersionUID 		= 1561775564425837002L;

	protected final static String TYPE_APP_JSON 	= "application/json"; 

	protected static String _PAGINATION_STARTFROM 	= JsonCrudConfig._LIST_START;
	protected static String _PAGINATION_FETCHSIZE 	= JsonCrudConfig._LIST_FETCHSIZE;
	protected static String _PAGINATION_TOTALCNT 	= JsonCrudConfig._LIST_TOTAL;	
	
	protected static String _PAGINATION_RESULT_SECTION 	= JsonCrudConfig._LIST_RESULT;
	protected static String _PAGINATION_META_SECTION 	= JsonCrudConfig._LIST_META;	
	
	public static final String GET 		= "GET";
	public static final String POST 	= "POST";
	public static final String DELETE	= "DELETE";
	public static final String PUT 		= "PUT";

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
    	String sPathInfo 			= req.getPathInfo();  //{crudkey}/xx/xx
    	
    	JSONObject jsonResult 		= null;
    	
    	HttpResp httpReq = new HttpResp();
    	httpReq.setHttp_status(HttpServletResponse.SC_NOT_FOUND);
 
/*
System.out.println("sReqUri:"+sReqUri);
System.out.println("sPathInfo:"+sPathInfo);
System.out.println("sHttpMethod:"+sHttpMethod);
System.out.println("sInputContentType:"+sInputContentType);
System.out.println("sInputData:"+sInputData);
System.out.println();
*/
    	
  	
		String[] sPaths = CRUDServiceUtil.getUrlSegments(sPathInfo);
		String sCrudKey = sPaths[0];
		System.out.println("sCrudKey ["+ sCrudKey+"]");
		
		Map<String, Map<String, String>> mapQueryParams = CRUDServiceUtil.getQueryParamsMap(req);
		Map<String, String> mapCrudConfig = JsonCrudRestUtil.getCRUDMgr().getCrudConfigs(sCrudKey);
		
		//
		CRUDServiceReq crudReq = new CRUDServiceReq(req, mapCrudConfig);
		if(sPaths.length==2)
		{
			if(GET.equalsIgnoreCase(crudReq.getHttpMethod()) || PUT.equalsIgnoreCase(crudReq.getHttpMethod()) || DELETE.equalsIgnoreCase(crudReq.getHttpMethod()))
			{
				crudReq.addCrudFilter("id", sPaths[1]);
			}
		}
		//
		
		
		crudReq = preProcess(crudReq);
		
 		try {
			
			if(GET.equalsIgnoreCase(crudReq.getHttpMethod()))
			{
				switch (sPaths.length)
				{
					case 1 : //get list

						jsonResult = JsonCrudRestUtil.retrieveList(sCrudKey, 
								crudReq.getCrudFilters(), 
								crudReq.getPaginationStartFrom(),
								crudReq.getPaginationFetchSize(), 
								crudReq.getCrudSorting());
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
				switch (sPaths.length)
				{
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

				switch (sPaths.length)
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
			///////////////////////////
			
			if(httpReq.getHttp_status() == HttpServletResponse.SC_OK);
			{
				httpReq.setContent_type(TYPE_APP_JSON); //200 = ;
			}
			httpReq.setContent_data(jsonResult.toString());
			
			
			httpReq = postProcess(req, crudReq, httpReq);
			RestApiUtil.processHttpResp(res, httpReq.getHttp_status(), httpReq.getContent_type(), httpReq.getContent_data());
			
		} catch (Exception e) {
			throw new ServletException(e);
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

    
    public CRUDServiceReq preProcess(CRUDServiceReq aCrudReq)
    {
    	return aCrudReq;
    }
    
    public HttpResp postProcess(HttpServletRequest req, CRUDServiceReq aCrudReq, HttpResp aHttpResp)
    {
    	return aHttpResp;
    }
    
    
}
