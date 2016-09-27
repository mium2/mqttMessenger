package com.msp.chat.client.ssl;

import io.netty.util.internal.SystemPropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.security.KeyStore;

/**
 * Created by Y.B.H(mium2) on 16. 7. 27..
 */
public final class SSslContextFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger("server");

    private static final String PROTOCOL = "TLS";
    private static final SSLContext SERVER_CONTEXT;
    private static final SSLContext CLIENT_CONTEXT;

    static {
        String algorithm = SystemPropertyUtil.get("ssl.KeyManagerFactory.algorithm");
        if (algorithm == null) {
            algorithm = "SunX509";
        }

        SSLContext serverContext;
        SSLContext clientContext;
        try {
            //SecureSocketSslContextFactory.class.getResourceAsStream("/securesocket.jks")
            //####################################################################
            // 방법1. OPEN SSL이용 수동 키스토어 만들었을 때
            //####################################################################
/*            InputStream keystoreFile = new FileInputStream("/Users/mium2/project/java/MqttChat/certificate/private/keystore.jks");
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(keystoreFile, "uracle".toCharArray());

            // Set up key manager factory to use our key storage
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            kmf.init(ks, "uracle".toCharArray());*/
            //####################################################################


            //####################################################################
            // 방법2. 자바(JDK) 기본 키스토어 사용
            //####################################################################
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(SecureSocketKeyStore.asInputStream(),
                    SecureSocketKeyStore.getKeyStorePassword());

            // Set up key manager factory to use our key storage
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
            kmf.init(ks, SecureSocketKeyStore.getCertificatePassword());
            //####################################################################


            // Initialize the SSLContext to work with our key managers.
            serverContext = SSLContext.getInstance(PROTOCOL);
            serverContext.init(kmf.getKeyManagers(), null, null);
        } catch (Exception e) {
            throw new Error(
                    "Failed to initialize the server-side SSLContext", e);
        }

        try {
            clientContext = SSLContext.getInstance(PROTOCOL);
            clientContext.init(null, SecureSokcetTrustManagerFactory.getTrustManagers(), null);
        } catch (Exception e) {
            throw new Error(
                    "Failed to initialize the client-side SSLContext", e);
        }

        SERVER_CONTEXT = serverContext;
        CLIENT_CONTEXT = clientContext;
    }

    public static SSLContext getServerContext() {
        return SERVER_CONTEXT;
    }

    public static SSLContext getClientContext() {
        return CLIENT_CONTEXT;
    }

    private SSslContextFactory() {
        // Unused
    }

}
