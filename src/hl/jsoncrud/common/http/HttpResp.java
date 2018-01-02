package hl.jsoncrud.common.http;

public class HttpResp {
	//
	private int http_status 			= 0;
	private String http_status_message 	= null;
	//
	private String content_data 		= null;
	private String content_type 		= null;
	//
	public int getHttp_status() {
		return http_status;
	}
	public void setHttp_status(int http_status) {
		this.http_status = http_status;
	}
	public String getHttp_status_message() {
		return http_status_message;
	}
	public void setHttp_status_message(String http_status_message) {
		this.http_status_message = http_status_message;
	}
	public String getContent_data() {
		return content_data;
	}
	public void setContent_data(String content_data) {
		this.content_data = content_data;
	}
	public String getContent_type() {
		return content_type;
	}
	public void setContent_type(String content_type) {
		this.content_type = content_type;
	}	
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("status:").append(getHttp_status()).append(" ").append(getHttp_status_message());
		sb.append("\n").append("content-type:").append(getContent_type());
		sb.append("\n").append("body:").append(getContent_data());
		return sb.toString();
		
	}
}