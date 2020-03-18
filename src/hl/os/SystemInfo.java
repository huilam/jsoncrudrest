package hl.os;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;


public class SystemInfo {

	private static Map<String, List<String>> mapSysInfo = null;
	
	static {
		
		try {
			mapSysInfo = parseSystemInfo();
		} catch (Throwable t) {
			// TODO Auto-generated catch block
			t.printStackTrace();
		}
	}
	
    private static Map<String, List<String>> parseSystemInfo() throws Exception
    {
    	if(mapSysInfo!=null)
    		return mapSysInfo;
    	
    	mapSysInfo = new LinkedHashMap<String, List<String>>();
    	
    	Process p = Runtime.getRuntime().exec("systeminfo");
    	BufferedReader reader =
                new BufferedReader(new InputStreamReader(p.getInputStream()));

        List<String> listValues = new ArrayList<String>();
        String sPrevKey = null;
        String sLine 	= null;
        while ((sLine = reader.readLine()) != null) 
        {
        	if(sLine.startsWith(" "))
        	{
        		listValues.add(sLine.trim());
        		continue;
        	}
        	
        	int iPos = sLine.indexOf(":");
        	if(iPos>-1)
        	{
        		String sKey = sLine.substring(0, iPos);
        		String sVal = sLine.substring(iPos+1);
        		
        		if(listValues.size()>0)
        		{
        			List<String> listPrevVals = mapSysInfo.get(sPrevKey);
        			if(listPrevVals!=null)
        			{
        				listPrevVals.addAll(listValues);
        			}
        			mapSysInfo.put(sPrevKey, listPrevVals);
        			listValues.clear();
        		}
        		
        		if(sVal!=null && sVal.trim().length()>0)
	        	{
	        		List<String> list = new ArrayList<String>();
	        		list.add(sVal.trim());
	        		mapSysInfo.put(sKey.trim(), list);
        		}
        		
        		sPrevKey = sKey;
        	}
        }
    	return mapSysInfo;
    }

    public static void printSystemInfo()
    {
    	for(String sKey : mapSysInfo.keySet())
    	{
    		System.out.println(sKey+":");
    		List<String> listValues = mapSysInfo.get(sKey);
    		for(String sTmp : listValues)
    		{
    			System.out.println("    - "+sTmp);
    		}
    	}
    }
    

    public static JSONObject getJson()
    {   
    	JSONObject jsonAll = new JSONObject();
    	
    	JSONObject jsonTmp = new JSONObject();
    	String sOSName 		= getSystemInfoVal("OS Name", 0);
    	String sOSVer 		= getSystemInfoVal("OS Version", 0);
       	String sTimeZone 	= getSystemInfoVal("Time Zone", 0);
       	String sLastHotfix 	= getSystemInfoVal("Hotfix(s)", 1000);
       	
       	jsonTmp.put("os.name", sOSName);
       	jsonTmp.put("os.version", sOSVer);
       	jsonTmp.put("os.timezone", sTimeZone);
       	jsonTmp.put("os.last.hotfix", sLastHotfix);
       	
       	jsonAll.put("os", jsonTmp);
       	
    	String sTotalRAM 	= getSystemInfoVal("Total Physical Memory", 0);
       	String sAvailRAM 	= getSystemInfoVal("Available Physical Memory", 0);
       	jsonTmp = new JSONObject();
       	jsonTmp.put("memory.total", sTotalRAM);
       	jsonTmp.put("memory.available", sAvailRAM);
       	jsonAll.put("memory", jsonTmp);
       	

       	JSONObject jsonDrives = new JSONObject();
       	
       	for(File drive : File.listRoots())
       	{
           	long lTotalSpace 	= drive.getTotalSpace();
           	long lAvailSpace 	= drive.getFreeSpace();
           	long lUsableSpace   = drive.getUsableSpace();
           	jsonTmp = new JSONObject();
           	jsonTmp.put("total", toWords(lTotalSpace));
           	jsonTmp.put("available", toWords(lAvailSpace));
           	jsonTmp.put("usable", toWords(lUsableSpace));
       		       		
           	jsonDrives.put(drive.getPath(), jsonTmp);
       	}
       	jsonAll.put("storage", jsonDrives);
       	
    	return jsonAll;
    }
    
    
    private static String getSystemInfoVal(String aKey, int aValIndex)
    {
    	if(aValIndex<0)
    		aValIndex = 0;
    	
    	List<String> listVal = mapSysInfo.get(aKey);
    	if(listVal==null)
    		return null;
    	
    	if(aValIndex>listVal.size())
    		aValIndex = listVal.size()-1;
    	
    	return listVal.get(aValIndex);
    }
    
    private static String toWords(long aBytes)
    {
    	long _KB = 1000;
    	long _MB = 1000 * _KB;
    	long _GB = 1000 * _MB;
    	
    	StringBuffer sb = new StringBuffer();
    	
    	if(aBytes >= _GB)
    	{
    		long lGB = aBytes / _GB;
    		sb.append(lGB).append(" GB ");
    		aBytes = aBytes % _GB;
    	}
    	
    	if(aBytes >= _MB)
    	{
    		long lMB = aBytes / _MB;
    		sb.append(lMB).append(" MB ");
    		aBytes = aBytes % _MB;
    	}
    	/*
    	if(aBytes >= _KB)
    	{
    		long lKB = aBytes / _KB;
    		sb.append(lKB).append(" KB ");    		
    		aBytes = aBytes % _KB;
    	}
    	*/
    	
    	/**
		if(aBytes>0)
    		sb.append(aBytes).append(" bytes ");
    	**/
    	
    	if(sb.length()==0)
    	{
    		sb.append("0");
    	}
    	
    	return sb.toString().trim();
    }
    
    public static void main(String args[]) throws Exception
    {
    	//System.out.println(getJson());
    	
    	JSONObject jsonTmp 		= new JSONObject();
    	JSONObject jsonDrives 	= new JSONObject();
       	for(File drive : File.listRoots())
       	{
           	long lTotalSpace 	= drive.getTotalSpace();
           	long lAvailSpace 	= drive.getFreeSpace();
           	jsonTmp = new JSONObject();
           	jsonTmp.put("storage.total", toWords(lTotalSpace));
           	jsonTmp.put("storage.available", toWords(lAvailSpace));
       		
           	jsonDrives.put(drive.getPath(), jsonTmp);
       	}
       	System.out.println(jsonDrives);
    	
    }
}
