package com.msp.chat.server;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.msp.chat.client.BrokerClientManager;
import com.msp.chat.license.LicenseValidator;
import com.msp.chat.server.commons.utill.BrokerConfig;
import com.msp.chat.server.commons.utill.LocaleUtils;
import com.msp.chat.server.config.ApplicationConfig;
import com.msp.chat.server.netty.*;
import com.msp.chat.server.worker.*;
import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by mium2(Yoo Byung Hee)
 */
public class Server {
    private final static Logger LOGGER = LoggerFactory.getLogger("server");

    public static ApplicationContext ctx = null;

    public final static String CACHE_CONF_FILE = "./config/ehcache.xml";
    public final static String SERVER_CONF_FILE = "./config/config.xml";
    public final static String LOG_CONF_FILE = "./config/logback.xml";

    private static String VERSION = "1.0.0";

    private ServerAcceptor mqttAcceptor;
    private ServerAcceptor sslMqttAcceptor;
    private NettyHttpAcceptor httpAcceptor;
    private WebSocketAcceptor webSocketAcceptor;
    
    public static void main(String[] args) throws Exception {
        System.out.println("Server started, version " + VERSION);
        LOGGER.info("########################################################");
        LOGGER.info("####   MQTT-MESSENGER-SERVER "+ VERSION +"Starting~~!    ####");
        LOGGER.info("########################################################");
        //Logger 설정
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        LOGGER.info("########################################################");
        LOGGER.info("####   LOGGER SET STARTTING...! ####");
        LOGGER.info("########################################################");
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.doConfigure(LOG_CONF_FILE);
        } catch (JoranException je) {
            LOGGER.error(je.getMessage());
            return;
        }
        LOGGER.info("########################################################");
        LOGGER.info("####   LOGGER SET SUCCESS~~! ####");
        LOGGER.info("########################################################");
        //브로커서버 config 설정 로드
        try {
            BrokerConfig.Load(SERVER_CONF_FILE);
        }catch(ConfigurationException e) {
            LOGGER.error(e.getMessage());
            return;
        }

        LOGGER.info("########################################################");
        LOGGER.info("####   CONFIG LOAD SUCCESS  ####");
        LOGGER.info("########################################################");
        //라이센스 체크
        try {
            Properties properties = System.getProperties();
            String HomeDir = properties.getProperty("user.dir");
            String licenDirSrc = HomeDir+properties.getProperty("file.separator")+"config"+properties.getProperty("file.separator");
            System.out.println("##### licenDirSrc:"+licenDirSrc);
            LicenseValidator licenseValidator = LicenseValidator.getInstance();
            licenseValidator.setLicenseFileDir(licenDirSrc);
            licenseValidator.initialize();
            if (!licenseValidator.validate()) {
                System.out.println("#########################################################");
                System.out.println("##############      License Error ~~!     ###############");
                System.out.println("#########################################################");
                System.out.println("!!!!!!!!!!! Check the expiration time of the IP server license file.!!!!!!!!!!!!!!!\n+" +
                    "#########################################################");
                System.exit(-1);
            }
        }catch (Exception e){
            System.out.println("#########################################################");
            System.out.println("##############      License Error ~~!     ###############");
            System.out.println("#########################################################");
            System.out.println("!!!!!!! The license file is incorrect. Please use the license file as received from Uracle !!!!!!!!!\n" +
                "#########################################################");
            System.exit(-1);
        }

        ctx = new AnnotationConfigApplicationContext(ApplicationConfig.class);  //스프링 Config 호출

        LOGGER.info("########################################################");
        LOGGER.info("####   SPRING APPLICATION SUCCESS  ####");
        LOGGER.info("########################################################");

        Object obj = ctx.getBean("masterRedisTemplate");
        if(obj!=null){
            try{
                RedisTemplate redisTemplate = (RedisTemplate)obj;
                redisTemplate.opsForHash().size("TEST");
            }catch (Exception e){
                e.printStackTrace();
                System.exit(-1);
            }
        }else{
            System.exit(-1);
        }

        LOGGER.info("########################################################");
        LOGGER.info("####   REDIS CONNECTED SUCCESS  ####");
        LOGGER.info("########################################################");

        // 다국어 지원
        LocaleUtils.init();
        try {
            //스토리지 ehcache설정 초기화 및 MqttMsgWorkManager 구동
            MqttMsgWorkerManager.getInstance().startWorkers();

            LOGGER.info("########################################################");
            LOGGER.info("####   EHCACHE SETTING SUCCESS  ####");
            LOGGER.info("########################################################");

            // 푸시발송 메니저 구동
            PushSendManager.getInstance().startWorkers();

            // 웹소켓 메세지처리 매니저 구동
            WebSocketMsgManager.getInstance().startWorkers();

            // Http 메세지 처리 구동
            HttpMsgManager.getInstance().startWorkers();

            ExecutorService threadPool = Executors.newFixedThreadPool(1);
            threadPool.execute(new CheckCacheExpireWorker());

        }catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
        final Server server = new Server();

        // 브로커 서버 구동
        server.brokerStart();

        // SSL브로커 서버 구동
        server.sslBrokerStart();

        // http 서버 구동
        server.httpServerStart();

        // webSocket 서버 구동
        server.webSocketStart();

        //브로커 클라이언트 구동
        server.brokerClientStart();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.stopServer();
            }
        });
    }

    public void brokerStart() throws IOException {
        //브로커 서버 구동
        mqttAcceptor = new NettyMqttAcceptor();
        mqttAcceptor.initialize();
    }

    private void sslBrokerStart() throws Exception{
        sslMqttAcceptor = new NettySslMqttAcceptor();
        sslMqttAcceptor.initialize();
    }

    //WEBSocket 서버 구동
    private void webSocketStart() throws Exception{
        webSocketAcceptor = new WebSocketAcceptor();
        webSocketAcceptor.run();

    }

    private void brokerClientStart(){
        BrokerClientManager.getInstance().init();
    }

    private void httpServerStart() throws IOException{
        //http 서버 구동
        httpAcceptor = new NettyHttpAcceptor();
        httpAcceptor.run();
    }


    public static void showMemory() {

        DecimalFormat format = new DecimalFormat("###,###,###.##");
        //JVM이 현재 시스템에 요구 가능한 최대 메모리량, 이 값을 넘으면 OutOfMemory 오류가 발생 합니다.
        long max = Runtime.getRuntime().maxMemory();
        //JVM이 현재 시스템에 얻어 쓴 메모리의 총량
        long total = Runtime.getRuntime().totalMemory();
        //JVM이 현재 시스템에 청구하여 사용중인 최대 메모리(total)중에서 사용 가능한 메모리
        long free = Runtime.getRuntime().freeMemory();
        System.out.println("Max:" + format.format(max) + ", Total:" + format.format(total) + ", Free:" + format.format(free));
    }

    public void stopServer() {
        System.out.println("##############################################");
        System.out.println("##### MIUM2 MQTT MESSENGER SERVER SHUTDOWN Starting...  #######");

        //broker 서버 중지
        if(mqttAcceptor!=null) {
            mqttAcceptor.close();
        }
        //SSL broker 서버 중지
        if(sslMqttAcceptor!=null) {
            sslMqttAcceptor.close();
        }
        //http 서버중지
        if(httpAcceptor!=null) {
            httpAcceptor.close();
        }

        //브로커 클라이언트 중지
        BrokerClientManager.getInstance().releaseExternalResources();

        // Mqtt Message Worker Manager 중지
        MqttMsgWorkerManager.getInstance().processStop();

        System.out.println("#####   MIUM2 MQTT MESSENGER SERVER SHUTDOWN End~!  #######");
        System.out.println("##############################################");
    }
}
