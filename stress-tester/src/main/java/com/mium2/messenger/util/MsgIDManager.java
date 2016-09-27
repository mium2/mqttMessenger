package com.mium2.messenger.util;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Y.B.H(mium2) on 16. 5. 18..
 */
public class MsgIDManager {

    private static MsgIDManager instance = null;
    private final String dataDir;
    private final String MSGIDFILE = "MSGID.txt";
    private static final int MAX_MSG_ID = 65534;
    private AtomicInteger automicInteger = new AtomicInteger(0);

    private MsgIDManager() throws IOException{
        dataDir = System.getProperty("user.dir");
        final File f = new File(dataDir+"/"+MSGIDFILE);
        if(!f.exists()) {
            f.createNewFile();
            //새 파일이므로 라인증가번호값 초기화
            automicInteger.set(0);
            try {
                BufferedWriter fio = new BufferedWriter(new FileWriter(dataDir+"/"+MSGIDFILE));
                fio.write("100");
                fio.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            FileReader fr = new FileReader(f.getAbsolutePath());
            BufferedReader br = new BufferedReader(fr);
            String str = null;
            while ((str = br.readLine()) != null) {
                int messageID =Integer.parseInt(str.trim());
                if(messageID>=MAX_MSG_ID){
                    automicInteger.set(0);
                }else {
                    automicInteger.set(messageID);
                }

            }
            if(br!=null){
                br.close();
            }
            if(fr!=null){
                fr.close();
            }
        }
    }

    public static MsgIDManager getInstance() throws Exception{
        if(instance==null){
            instance = new MsgIDManager();
        }
        return instance;
    }

    public synchronized int getNextMessgeID() throws Exception{
        int nextMessageID = automicInteger.incrementAndGet();
        BufferedWriter fio = new BufferedWriter(new FileWriter(dataDir+"/"+MSGIDFILE));
        fio.write("" + nextMessageID);
        fio.close();
        if(nextMessageID>=MAX_MSG_ID){
            automicInteger.set(0);
        }
        return nextMessageID;
    }
}
