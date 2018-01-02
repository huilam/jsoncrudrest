package hl.jsoncrud.common.http;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;


public class RestApiUtil {

	public final static String HEADER_CONTENT_TYPE 	= "content-type";
	public final static String TYPE_APP_JSON 		= "application/json";
	public final static String TYPE_TEXT_PLAIN 		= "text/plain";
	
	//private final static String CRLF = "\\r\\n";
	
	public static String getReqContent(HttpServletRequest req)
	{
		StringBuffer sb = new StringBuffer();
		BufferedReader rdr = null;
		try {
			rdr = req.getReader();
			String sLine = null;
			while((sLine=rdr.readLine())!=null)
			{
				sb.append(sLine);
			}
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		finally
		{
			if(rdr!=null)
			{
				try { rdr.close(); } catch (IOException e) { e.printStackTrace(); }
			}
		}
	}
	
    
	public static void processHttpResp(HttpServletResponse res, 
    		int aHttpStatus, JSONObject aJsonContent) throws IOException
	{
		processHttpResp(res, aHttpStatus, TYPE_APP_JSON, aJsonContent.toString());
	}
	
    public static void processHttpResp(HttpServletResponse res, 
    		int aHttpStatus, String aContentType, String aOutputContent) throws IOException
    {
		if(aHttpStatus>0)
			res.setStatus(aHttpStatus);
		
		if(aContentType!=null)
			res.setContentType(aContentType);
		
		if(aOutputContent!=null && aOutputContent.length()>0)
		{
			res.getWriter().println(aOutputContent);
		}

		res.flushBuffer();
    }
	
    public static HttpResp httpPost(String aEndpointURL, 
    		String aContentType, String aContentData) throws IOException
    {
    	HttpResp httpResp 	= new HttpResp();
    	
		HttpURLConnection conn 	= null;
		try {
			URL url = new URL(aEndpointURL);
			
			conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty(HEADER_CONTENT_TYPE, aContentType);
			
			OutputStream out 		= conn.getOutputStream();
			BufferedWriter writer 	= null;
			
			try {
				writer = new BufferedWriter(new OutputStreamWriter(out));
				writer.write(aContentData);
				
				out.flush();
			}
			finally
			{
				if(writer!=null)
					writer.close();
			}
			httpResp.setHttp_status(conn.getResponseCode());
			httpResp.setHttp_status_message(conn.getResponseMessage());
			
			if(conn.getResponseCode()>=200 || conn.getResponseCode()<300)
			{
		    	StringBuffer sb = new StringBuffer();
		    	InputStream in 	= null;
				try {
					BufferedReader reader = null;
					try {
						in = conn.getInputStream();
						reader = new BufferedReader(new InputStreamReader(in));
						String sLine = null;
						while((sLine = reader.readLine())!=null)
						{
							sb.append(sLine);
						}
					}finally
					{
						if(reader!=null)
							reader.close();
					}
				}
				finally
				{
					if(in!=null)
						in.close();
				}
				
				httpResp.setContent_type(conn.getHeaderField(HEADER_CONTENT_TYPE));
				
				if(sb.length()>0)
				{
					httpResp.setContent_data(sb.toString());
				}
				
			}
			
		}
		catch(FileNotFoundException ex)
		{
			throw new IOException("Invalid URL : "+aEndpointURL);
		}
 	
    	return httpResp;
    }
    
    public static HttpResp httpGet(String aEndpointURL) throws IOException
    {
    	HttpResp httpResp 		= new HttpResp();
    	
		HttpURLConnection conn 	= null;
		InputStream in 			= null;
		try {
	    	
	    	URL url = new URL(aEndpointURL);
	    	
			conn = (HttpURLConnection) url.openConnection();
			try {
				
		    	StringBuffer sb = new StringBuffer();
				try {
					BufferedReader reader = null;
					try {
						in = conn.getInputStream();
						reader = new BufferedReader(new InputStreamReader(in));
						String sLine = null;
						while((sLine = reader.readLine())!=null)
						{
							sb.append(sLine);
						}
					}finally
					{
						if(reader!=null)
							reader.close();
					}
				}
				finally
				{
					if(in!=null)
						in.close();
				}
				
				httpResp.setHttp_status(conn.getResponseCode());
				httpResp.setHttp_status_message(conn.getResponseMessage());
				httpResp.setContent_type(conn.getHeaderField(HEADER_CONTENT_TYPE));
				
				if(sb.length()>0)
				{
					httpResp.setContent_data(sb.toString());
				}
			}
			finally
			{
				if(in!=null)
					in.close();
			}
		}
		catch(FileNotFoundException ex)
		{
			throw new IOException("Invalid URL : "+aEndpointURL);
		}
		
		return httpResp;
    }    
}