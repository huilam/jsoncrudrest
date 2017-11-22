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

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.json.JSONObject;

import hl.jsoncrud.JsonCrudRestUtil;

@Path(value = "/v1") 
public class JsonCrudSampleRestApi extends JsonCrudBaseRestApi {
	
    @POST 
    @Path(value = "/{crudkey}")
	public Response create(
			@Context UriInfo uriInfo, 
			@PathParam("crudkey") String aCrudKey,
			String jsonString)
	{
    	Response resp = checkInputMediaType(jsonString);
    	if(resp==null) resp = checkMandatoryInput(aCrudKey);
    	if(resp!=null) return resp;    	
    	////////    	
    	try {
	    	String sJsonstr = super.create(aCrudKey, jsonString);
	    	return Response.status(Status.OK).type(TYPE_APP_JSON).entity(sJsonstr).build();
		}
    	catch(Throwable ex)
		{
			return getBadRequestResp(aCrudKey, uriInfo, ex);
		}	    	
	}
    
    @GET //List
    @Path(value = "/{crudkey}")
	public Response retrieveList(
			@Context UriInfo uriInfo,
			@PathParam("crudkey") String aCrudKey,
			@QueryParam(_PARAM_RESULTMETA) boolean includeResultMeta)
	{
    	Response resp = checkMandatoryInput(aCrudKey);
    	if(resp!=null) return resp;    	
    	///////
    	try {
	    	String sJsonstr = super.retrieveList(aCrudKey, uriInfo.getQueryParameters(), includeResultMeta);
	    	return Response.status(Status.OK).type(TYPE_APP_JSON).entity(sJsonstr).build();
		}
    	catch(Exception ex)
		{
			return getBadRequestResp(aCrudKey, uriInfo, ex);
		}
	}
    
    @GET
    @Path(value = "/{crudkey}/{id}")
	public Response retrieve(
			@Context UriInfo uriInfo,
			@PathParam("crudkey") String aCrudKey,
			@PathParam("id") String aIdValue)
	{
    	Response resp = checkMandatoryInput(aCrudKey, aIdValue);
    	if(resp!=null) return resp;
    	////////    	
    	JSONObject jsonWhere = new JSONObject();
    	jsonWhere.put("id", aIdValue);
    	//
    	try {
    		String sJsonstr = super.retrieveFirst(aCrudKey, jsonWhere);
    		return Response.status(Status.OK).type(TYPE_APP_JSON).entity(sJsonstr).build();
    	}
    	catch(Exception ex)
    	{
    		return getBadRequestResp(aCrudKey, uriInfo, ex);
    	}
    	
	}

    @PUT
    @Path(value = "/{crudkey}/{id}")
	public Response update(
			@Context UriInfo uriInfo,
			@PathParam("crudkey") String aCrudKey,
			@PathParam("id") String aIdValue,
			String jsonString)
	{
    	Response resp = checkMandatoryInput(aCrudKey, aIdValue, jsonString);
    	if(resp!=null) return resp;
    	////////
    	JSONObject jsonWhere = new JSONObject();
    	jsonWhere.put("id", aIdValue);
    	//
    	try {
    		String sJsonstr = super.update(aCrudKey, new JSONObject(jsonString), jsonWhere);
    		return Response.status(Status.OK).type(TYPE_APP_JSON).entity(sJsonstr).build();
    	}
    	catch(Exception ex)
    	{
    		return getBadRequestResp(aCrudKey, uriInfo, ex);
    	}
    	
	}
    
    @DELETE
    @Path(value = "/{crudkey}/{id}")
	public Response delete(
			@Context UriInfo uriInfo,
			@PathParam("crudkey") String aCrudKey,
			@PathParam("id") String aIdValue)
	{
    	Response resp = checkMandatoryInput(aCrudKey, aIdValue);
    	if(resp!=null) return resp;
    	////////
    	JSONObject jsonWhere = new JSONObject();
    	jsonWhere.put("id", aIdValue);
    	//
    	try {
	    	String sJsonstr =  super.delete(aCrudKey, jsonWhere);
	    	return Response.status(Status.OK).type(TYPE_APP_JSON).entity(sJsonstr).build();
		}
    	catch(Exception ex)
		{
			return getBadRequestResp(aCrudKey, uriInfo, ex);
		}
	}
    
    @GET
    @Path(value = "/about")
	public Response getJsonCrudVersion()
	{
    	return Response.status(Status.OK).type(TYPE_APP_JSON).entity(JsonCrudRestUtil.getJsonCrudVersion()).build();
	}
    
    /////////////////////////////////////////////////////////////////
    
}