import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Y.B.H(mium2) on 2015. 10. 16..
 */
public class ForceKillDataManager {

    private static ForceKillDataManager instance = null;
    private static final int BUFFER_SIZE = 1024*4;
    private static final int DIGEST_BASE_RADIX = 16;
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private AtomicInteger automicInteger = new AtomicInteger(0);
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyyMMddHH",Locale.KOREA);
    private String mLogDir;
    private String mLogFileName;
    FileOutputStream logFile;
    private FileChannel mLogOutputChannel;
    private ByteBuffer mByteBuffer;
    private int maxHistorySize = 24;
    private List<String> logFileNameList = new ArrayList<String>();

    private ForceKillDataManager() throws IOException {
//        mLogDir = PushDaemonMain.webProperties.getProperty("FORCE_KILL_LOG_DIR", "/Users/mium2/project/push/push_3.7/logs/");
//        String configHistorySize = PushDaemonMain.webProperties.getProperty("FORCE_KILL_LOG_HISTORYSIZE","24");

        mLogDir = "/Users/mium2/project/push/push_3.7/logs/";
        String configHistorySize = "10";

        maxHistorySize = Integer.parseInt(configHistorySize);
        Date currentTime = new Date();
        mLogFileName = mSimpleDateFormat.format(currentTime);

        new File(mLogDir).mkdirs();
        initLogDirFileList(); //나중에 삭제를 위해 로그디렉토리에 있는 로그파일리스트를 메모리에 넣어둔다

        final String logFilePath = mLogDir + File.separatorChar + mLogFileName;
        final File f = new File(logFilePath);
        if(!f.exists()) {
            f.createNewFile();
            logFileNameList.add(f.getName());
            int delCnt = logFileNameList.size()-maxHistorySize;
            if(delCnt>0){
                for(int i=0; i<delCnt; i++) {
                    String delFilePath = mLogDir + File.separatorChar + logFileNameList.get(0);
                    FileUtils.forceDelete(new File(delFilePath));
                    logFileNameList.remove(0);
                }
            }
        }

        logFile = new FileOutputStream(logFilePath, true);
        mLogOutputChannel = logFile.getChannel();
        mByteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    }

    public static ForceKillDataManager getInstance() throws IOException{
        if(instance==null){
            instance = new ForceKillDataManager();
        }
        return instance;
    }

    public void write(String message) throws IOException{
        Date currentTime = new Date();
        String chkNowDate = mSimpleDateFormat.format(currentTime);

        if(!mLogFileName.equals(chkNowDate)){ //현재 시간과 다를 경우 파일을 만든다
            final String logFilePath = mLogDir + File.separatorChar + chkNowDate;
            final File f = new File(logFilePath);
            if(!f.exists()) {
                if(logFile!=null) {
                    logFile.close();
                }
                if(mLogOutputChannel!=null) {
                    mLogOutputChannel.close();
                }
                f.createNewFile();
                // 새로운 파일에 대한 Channel 생성
                logFile = new FileOutputStream(logFilePath, true);
                mLogOutputChannel = logFile.getChannel();

                logFileNameList.add(f.getName());
                // 날짜가 변경되어 파일이 생성될때 이전 파일 중 설정파일의 조건에 따라 지운다
                int delCnt = logFileNameList.size()-maxHistorySize;
                if(delCnt>0){
                    String delFilePath = mLogDir + File.separatorChar + logFileNameList.get(0);
                    FileUtils.forceDelete(new File(delFilePath));
                    logFileNameList.remove(0);
                }
            }
        }

        // write the log message to the log file
        if (mLogOutputChannel != null) {
            mByteBuffer.put(message.getBytes());
            mByteBuffer.put(LINE_SEPARATOR.getBytes());
            mByteBuffer.flip();
            try {
                mLogOutputChannel.write(mByteBuffer);
                // ensure that the data we just wrote to the log file is pushed to the disk right away
                mLogOutputChannel.force(true);
            } catch (IOException e) {
                // Could not write to log file output channel
                System.out.println("[ERROR]:"+e.getMessage());
            }
        }

        if(mByteBuffer != null) {
            mByteBuffer.clear();
        }

//        return chkNowDate;
    }

    private void initLogDirFileList(){
        File dir = new File(mLogDir);
        File[] fileList = dir.listFiles();
        for(int i=0; i<fileList.length; i++){
            File file = fileList[i];
            if(file.isFile() && !file.getName().startsWith(".")){
                logFileNameList.add(file.getName());
            }
        }
        Collections.sort(logFileNameList); //로그파일 이름을 오름차순으로 정렬
    }

    public static void main(String[] args){
        try {
            ForceKillDataManager.getInstance().testRun();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testRun(){
        Thread t1 = new Thread1();
        Thread t2 = new Thread2();
        t1.start();
//        t2.start();
    }

    class Thread1 extends Thread{
        @Override
        public void run() {
            try {
                long startTime = System.currentTimeMillis();
                for(int i=0; i<1000000; i++) {
                    ForceKillDataManager.getInstance().write("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
//                    Thread.sleep(10);
                }
                System.out.println("###### elepse Time:"+(System.currentTimeMillis()-startTime));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class Thread2 extends Thread{
        @Override
        public void run() {
            try {
                for(int i=0; i<1000000; i++) {
                    ForceKillDataManager.getInstance().write("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
//                    Thread.sleep(10);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
