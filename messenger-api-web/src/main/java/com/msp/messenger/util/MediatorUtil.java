package com.msp.messenger.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MediatorUtil 
{
	private String MEDIATOR_URL = "http://211.241.199.216:9090/mediator/";
	private String MEDIATOR_ALT_URL = "http://211.241.199.216:9090/mediator/";
	private boolean useAlt = false;

	static MediatorUtil INSTANCE = new MediatorUtil();

	Logger logger = LoggerFactory.getLogger(MediatorUtil.class);

	private MediatorUtil()
	{
		super();
	}

	public static MediatorUtil getInstance()
	{
		return INSTANCE;
	}

	public void setMediatorUrl(String url)
	{
		MEDIATOR_URL = url;

		logger.info("MEDIATOR URL : " + MEDIATOR_URL);	
	}

	public void setMediatorAltUrl(String url) {
		MEDIATOR_ALT_URL = url;

		logger.info("MEDIATOR ALT URL : " + MEDIATOR_ALT_URL);
	}

	public HttpURLConnection getMediatorConnection() throws Exception
	{
		HttpURLConnection urlConn = null;
		URL url = null;
		if (!useAlt) {
			url = new URL(MEDIATOR_URL);
		} else {
			url = new URL(MEDIATOR_ALT_URL);
		}
		urlConn = (HttpURLConnection)url.openConnection();
		urlConn.setDoOutput(true);
		urlConn.setRequestMethod("POST");

		return urlConn;
	}

	public void returnMediatorConnection(HttpURLConnection conn)
	{

	}

	public boolean sendPsidRegToMediator(String strPsid, String strDeviceId)
	{
		StringBuilder sb 		= new StringBuilder();
		BufferedWriter bw 		= null;
		BufferedReader br 		= null;
		HttpURLConnection urlConn = null; 


		JsonObject joWhole 	= new JsonObject();
		JsonObject joHeader 	= new JsonObject();
		JsonObject joBody 		= new JsonObject();
		JsonParser jpResult     = new JsonParser();

		joHeader.addProperty("ACTION", "REGPSID");
		joBody.addProperty("PSID", strPsid);
		joBody.addProperty("DEVICEID", strDeviceId);

		joWhole.add("HEADER", 	joHeader);
		joWhole.add("BODY", 	joBody);		

		logger.info("sendPsidRegToMediator : " + joWhole.toString());

		try{
			/* set up */
			urlConn = getMediatorConnection();

			/* write */
			bw = new BufferedWriter(new OutputStreamWriter(urlConn.getOutputStream()));

			bw.write(joWhole.toString());
			bw.flush();

			/* read */
			br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
			String inputLine = "";
			try {
				while((inputLine = br.readLine()) != null) {
					sb.append(inputLine);
				}
			} catch (IOException e) {
				logger.error("", e);
			}

			logger.info("Mediator responds:" + sb.toString() + "-");

			JsonElement je = jpResult.parse(sb.toString());

			JsonObject  jobject = je.getAsJsonObject();

			jobject = jobject.getAsJsonObject("BODY");

			JsonPrimitive jpSuccess = jobject.getAsJsonPrimitive("SUCCESS");

			/*
			 * 00 : 성공
			 * 01 : 실패 (PSID 중복)
			 * 02 : 실패 (PSID 없음)
			 * 03 : 실패 (기타오류)
			 */
			String result = jpSuccess.toString();
			result = result.replace("\"", "");

			logger.info("Result of REGPSID : " + result + " (PSID : [" + strPsid + "])");

			if("00".equals(result))
				return true;
			else if("01".equals(result))
				return true;
			else if("02".equals(result))
				return false;
			else if("03".equals(result))
				return false;

			return false;
		}
		catch (Exception e) {
			e.printStackTrace();
			useAlt = !useAlt;
			new RuntimeException("Exception in sendPsidRegToMediator : " + e.toString());
		}
		finally {
			try {
				if(bw != null)
					bw.close();
			} catch (IOException e) { }
			try {
				if(br != null)
					br.close();
			} catch (IOException e) { }
		}

		return false;
	}

	public boolean sendPsidDelToMediator(String strPsid, String strDeviceId)
	{
		StringBuilder sb 		= new StringBuilder();
		BufferedWriter bw 		= null;
		BufferedReader br 		= null;
		HttpURLConnection urlConn = null; 

		JsonObject joWhole 	= new JsonObject();
		JsonObject joHeader 	= new JsonObject();
		JsonObject joBody 		= new JsonObject();
		JsonParser jpResult     = new JsonParser();

		joHeader.addProperty("ACTION", "DELPSID");
		joBody.addProperty("PSID", strPsid);
		joBody.addProperty("DEVICEID", strDeviceId);

		joWhole.add("HEADER", 	joHeader);
		joWhole.add("BODY", 	joBody);		

		try {
			/* set up */
			urlConn = getMediatorConnection();

			/* write */
			bw = new BufferedWriter(new OutputStreamWriter(urlConn.getOutputStream()));
			bw.write(joWhole.toString());
			bw.flush();

			/* read */
			br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
			String inputLine;
			while((inputLine = br.readLine()) != null){
				sb.append(inputLine);
			}

			JsonElement je = jpResult.parse(sb.toString());

			JsonObject  jobject = je.getAsJsonObject();

			jobject = jobject.getAsJsonObject("BODY");

			JsonPrimitive jpSuccess = jobject.getAsJsonPrimitive("SUCCESS");

			/*
			 * 00 : 성공
			 * 01 : 실패 (PSID 중복)
			 * 02 : 실패 (PSID 없음)
			 * 03 : 실패 (기타오류)
			 */
			String result = jpSuccess.toString();
			result = result.replace("\"", "");

			logger.info("Result of DELPSID : " + result + " (PSID : [" + strPsid + "])");

			if("00".equals(result))
				return true;
			else if("01".equals(result))
				return true;
			else if("02".equals(result))
				return true;
			else if("03".equals(result))
				return true;

		}
		catch (Exception e) {
			useAlt = !useAlt;
			throw new RuntimeException(e);
		}
		finally {
			try {
				if(bw != null)
					bw.close();
			} catch (IOException e) { }
			try {
				if(br != null)
					br.close();
			} catch (IOException e) { }
		}

		return false;
	}

}
