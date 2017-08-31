/*
 Copyright (c) 2017 onghuilam@gmail.com
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.
 The Software shall be used for Good, not Evil.
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 
 */

package hl.jsoncrud.restapi;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hl.jsoncrud.JsonCrudConfig;
import hl.jsoncrud.JsonCrudRestUtil;

public class JsonCrudBaseRestApi {
	
	protected final static String TYPE_APP_JSON 		= "application/json"; 
	//
	protected static final String _PARAM_INPUT_PREFIX 	= "d.";
	protected static final String _PARAM_STARTFROM 		= _PARAM_INPUT_PREFIX+"start";
	protected static final String _PARAM_FETCHSIZE 		= _PARAM_INPUT_PREFIX+"fetchsize";
	protected static final String _PARAM_ORDERBY 		= _PARAM_INPUT_PREFIX+"orderby";
	protected static final String _PARAM_ORDERDESC 		= _PARAM_INPUT_PREFIX+"orderdesc";
	//
	protected static final String _PARAM_RESULTMETA 	= _PARAM_INPUT_PREFIX+"meta";

	public String create(String aCrudKey, String jsonString) throws Throwable
	{
		boolean debug = JsonCrudRestUtil.isDebugEnabled(aCrudKey);
			
		jsonString = jsonString.trim();
		boolean isJsonInputArray = jsonString.startsWith("[") && jsonString.endsWith("]");
		
		JSONArray jsonObjArray = new JSONArray();
		if(isJsonInputArray)
		{
			jsonObjArray = new JSONArray(jsonString);
		}
		else
		{
			JSONObject jsonInput = new JSONObject(jsonString);
			jsonObjArray.put(jsonInput);
		}
		
		JSONArray jsonOutputArray = JsonCrudRestUtil.create(aCrudKey, jsonObjArray);
		
		if(jsonOutputArray.length()==0)
		{
			return jsonOutputArray.toString();
		}
		
		if(isJsonInputArray)
		{
			return jsonOutputArray.toString();
		}
		else
		{
			return jsonOutputArray.get(0).toString();
		}
	}
    

	public String retrieveList(MultivaluedMap<String, String> aMapQueryParam, String aCrudKey, boolean includeResultMeta) throws Exception
	{
		boolean debug = JsonCrudRestUtil.isDebugEnabled(aCrudKey);

		JSONObject jsonWhere = new JSONObject();
    	
    	int iStartFrom 		= 0;
    	int iFetchSize 		= 0;
    	boolean isOrderDesc	= false;
    	List<String> listOrderBy = new ArrayList<String>();

    	for(String sQueryKey : aMapQueryParam.keySet())
    	{
    		if(sQueryKey.startsWith(_PARAM_INPUT_PREFIX))
    		{
	    		if(_PARAM_STARTFROM.equals(sQueryKey))
	    		{
	    			String sStartFrom = aMapQueryParam.getFirst(_PARAM_STARTFROM);
	    			try {
	    				iStartFrom = Integer.parseInt(sStartFrom);
	    			}catch(NumberFormatException ex) {
	    				iStartFrom = 0;
	    			}
	    		}
	    		else if(_PARAM_FETCHSIZE.equals(sQueryKey))
	    		{
	    			String sFetchSize = aMapQueryParam.getFirst(_PARAM_FETCHSIZE);
	    			try {
	    				iFetchSize = Integer.parseInt(sFetchSize);
	    			}catch(NumberFormatException ex) {
	    				iFetchSize = 0;
	    			}
	    		}
	    		else if(_PARAM_ORDERDESC.equals(sQueryKey))
	    		{
	    			String sOrderDesc = aMapQueryParam.getFirst(_PARAM_ORDERDESC);
	    			try {
	    				if("desc".equalsIgnoreCase(sOrderDesc))
	    				{
	    					sOrderDesc = "true";
	    				}
	    				isOrderDesc = Boolean.parseBoolean(sOrderDesc);
	    			}catch(NumberFormatException ex) {
	    				isOrderDesc = true;
	    			}
	    		}
	    		else if(_PARAM_ORDERBY.equals(sQueryKey))
	    		{
	    			String sOrderBy = aMapQueryParam.getFirst(_PARAM_ORDERBY);
	    			if(sOrderBy!=null)
	    			{
	    				
	    				StringTokenizer tk = new StringTokenizer(sOrderBy,",");
	    				while(tk.hasMoreTokens())
	    				{
	    					String sOrderField = tk.nextToken();
	    					listOrderBy.add(sOrderField.trim());
	    				}
	    			}
	    		}
    		}
    		else
    		{    		
	    		String sQueryVal = aMapQueryParam.getFirst(sQueryKey);
	    		//
	    		jsonWhere.put(sQueryKey, sQueryVal);
    		}
    	}

    	JSONObject jsonOutput = JsonCrudRestUtil.retrieveList(
    			aCrudKey, jsonWhere, iStartFrom, iFetchSize, 
				listOrderBy, isOrderDesc);
    	
		if(jsonOutput==null)
		{
			jsonOutput = new JSONObject();
		}
		
		JSONArray jsonResultOnlyData = (JSONArray) jsonOutput.get(JsonCrudRestUtil.getCRUDMgr()._LIST_RESULT);
		
		if(jsonResultOnlyData==null)
			jsonResultOnlyData = new JSONArray();
		
		if(includeResultMeta)
		{
			return jsonOutput.toString();
		}
		else 
		{
    		return jsonResultOnlyData.toString();
		}
    		
	}
    
	public String retrieve(String aCrudKey, JSONObject aJsonWhere) throws Exception
	{
    	boolean debug = JsonCrudRestUtil.isDebugEnabled(aCrudKey);
   		
		JSONObject jsonOutput = JsonCrudRestUtil.retrieve(aCrudKey, aJsonWhere);
		if(jsonOutput==null)
		{
    		return new JSONObject().toString();
		}
		return jsonOutput.toString();
	}

	public String update(String aCrudKey, JSONObject aJsonData, JSONObject aJsonWhere) throws Exception
	{
    	boolean debug = JsonCrudRestUtil.isDebugEnabled(aCrudKey);
		
		JSONArray jsonArrOutput = JsonCrudRestUtil.update(aCrudKey, aJsonData, aJsonWhere);
		if(jsonArrOutput==null)
		{
			jsonArrOutput = new JSONArray();
		}
		if(jsonArrOutput.length()==0)
		{
			return jsonArrOutput.toString();
		}
		
		if(debug)
			return jsonArrOutput.toString();
		else
			return "";
	}
    

	public String delete(String aCrudKey, JSONObject aJsonWhere) throws Exception
	{
    	boolean debug = JsonCrudRestUtil.isDebugEnabled(aCrudKey);
		
		JSONArray jsonArrOutput = JsonCrudRestUtil.delete(aCrudKey, aJsonWhere);
		if(jsonArrOutput==null)
		{
			jsonArrOutput = new JSONArray();
		}
		if(jsonArrOutput.length()==0)
		{
			return jsonArrOutput.toString();
		}
		
		if(debug)
			return jsonArrOutput.toString();
		else
			return "";
	}
    
    /////////////////////////////////////////////////////////////////
    
	protected String getConfig(UriInfo uriInfo)
	{
    	boolean debug = true;
    	
    	if(debug)
    	{
       		JSONObject json = JsonCrudRestUtil.getCrudConfigJson(null);
       		if(json==null)
       			json = new JSONObject();
       	    return json.toString();
    	}
    	
    	return null;
	}
    
    protected Response getBadRequestResp(String aCrudKey, UriInfo uriInfo, Throwable aException, boolean isDebugMode)
    {
    	if(!aCrudKey.startsWith(JsonCrudConfig._PROP_KEY_CRUD))
    	{
    		aCrudKey = JsonCrudConfig._PROP_KEY_CRUD+"."+aCrudKey;
    	}
    	
    	if(isDebugMode)
		{
        	JSONObject jsonResp = new JSONObject();
			JSONObject jsonErr = new JSONObject();
			
			//
			if(aException!=null)
			{
				if(aException.getStackTrace()!=null)
				{
					StackTraceElement[] errTraces = aException.getStackTrace();
					if(errTraces!=null)
					{
						JSONArray jsonArr = new JSONArray();
						for(int i=0; i<errTraces.length; i++)
						{
							JSONObject jsonTrace = new JSONObject();
							jsonTrace.put("id", i);
							jsonTrace.put("classname", errTraces[i].getClassName());
							jsonTrace.put("linenumber", errTraces[i].getLineNumber());
							jsonArr.put(jsonTrace);
							
							if(i>=2)
							{
								break;
							}
						}
						jsonErr.put("stacktrace", jsonArr);
					}
				}
				
				String sErrMessage = aException.getMessage();
				if(aException.getCause()!=null && aException.getCause().getMessage()!=null)
				{
					sErrMessage = aException.getCause().getMessage();
				}
				
				if(sErrMessage==null)
					sErrMessage = "null";
				
				jsonErr.put("errmsg", sErrMessage.replaceAll("\"", "'"));
			}
			
			JSONObject jsonConfig = new JSONObject();
			
			jsonConfig.put("jsoncrud", JsonCrudRestUtil.getCrudConfigJson(aCrudKey));
			//
			JSONObject jsonPathParams = new JSONObject();
    		for(String sKey : uriInfo.getPathParameters().keySet())
    		{
    			jsonPathParams.put(sKey, uriInfo.getPathParameters().getFirst(sKey));
    		}
    		jsonConfig.put("uriinfo.pathparameters", jsonPathParams);
    		//
    		JSONObject jsonQueryParams = new JSONObject();
    		for(String sKey : uriInfo.getQueryParameters().keySet())
    		{
    			jsonQueryParams.put(sKey, uriInfo.getQueryParameters().getFirst(sKey));
    		}
    		jsonConfig.put("uriinfo.queryparameters", jsonQueryParams);
			//
			jsonResp.put("error", jsonErr);
			jsonResp.put("config", jsonConfig);
			return Response.status(Status.BAD_REQUEST).type(TYPE_APP_JSON).entity(jsonResp.toString()).build();
		}
    	
    	//hide implementation from public 
		return Response.status(Status.NOT_FOUND).build();
     }

    
    protected Response checkMandatoryInput(String... aStrings)
    {
    	for(String sStr : aStrings)
    	{
    		if(sStr==null || sStr.trim().length()==0)
    			return Response.status(Status.BAD_REQUEST).build(); ;
    	}
    	return null;
    }
    
    protected Response checkInputMediaType(String... aJsonInputs)
    {
    	for(String sJsonInput : aJsonInputs)
    	{
	    	if(sJsonInput==null || sJsonInput.trim().length()==0)
	    	{
	    		return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).build(); 
	    	}
	    	sJsonInput = sJsonInput.trim();
			try{
				if(sJsonInput.startsWith("[") && sJsonInput.endsWith("]"))
					new JSONArray(sJsonInput);
				else	
					new JSONObject(sJsonInput);
			}
			catch(JSONException ex)
			{
				return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).build(); 
			}
    	}
		return null;
    }
    
    protected Response allowCORS(Response aResp)
    {
    	ResponseBuilder respBuilder = Response.fromResponse(aResp);
    	return respBuilder.header("Access-Control-Allow-Origin", "*").build();
    }
    
}