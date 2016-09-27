package com.msp.chat.client.ssl;

import java.io.FileInputStream;
import java.io.InputStream;

public class StreamReader {

	
	public String toByteArray(InputStream fin)
	{
		int i = -1;
		StringBuilder buf = new StringBuilder();
		try{
			int loop = 0;
			while((i=fin.read())!=-1){
				if(buf.length()>0) buf.append(",");
				loop++;
				if(loop%10==0){
					buf.append("\n");
				}
				buf.append("(byte)");
				buf.append(i);
			}

		}catch(Throwable e){
			;
		}
		
		return buf.toString();
	}
	
	public static void main(String[] args) {
		StreamReader reader = new StreamReader();
//		InputStream keystoreInputStream = StreamReader.class.getResourceAsStream("/Users/mium2/project/java/MqttChat/certificate/private/keystore.jks");
        try {
            InputStream keystoreInputStream = new FileInputStream("/Users/mium2/project/git_repository/mqttMessenger/mqtt-chat-server/certificate/keystore.jks");
            System.out.println(reader.toByteArray(keystoreInputStream));
        }catch (Exception e){
            e.printStackTrace();
        }


	}

}
