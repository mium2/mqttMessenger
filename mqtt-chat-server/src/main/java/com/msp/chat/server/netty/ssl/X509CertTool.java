package com.msp.chat.server.netty.ssl;

import org.apache.commons.codec.binary.Base64;
import sun.security.x509.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;


/*
 * This class would require rt.jar in the class path in order to 
 * generated it alternative is using keytool.
 */

public class X509CertTool {

	/** 
	 * Create a self-signed X.509 Certificate
	 * @param dn the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
	 * @param pair the KeyPair
	 * @param days how many days from now the Certificate is valid for
	 * @param algorithm the signing algorithm, eg "SHA1withRSA"
	 */ 
	@SuppressWarnings("restriction")
	X509Certificate generateCertificate(String dn, KeyPair pair, int days,
			String algorithm) throws GeneralSecurityException, IOException {
		PrivateKey privkey = pair.getPrivate();
		X509CertInfo info = new X509CertInfo();
		Date from = new Date();
		Date to = new Date(from.getTime() + days * 86400000l);
		CertificateValidity interval = new CertificateValidity(from, to);
		BigInteger sn = new BigInteger(64, new SecureRandom());
		X500Name owner = new X500Name(dn);

		info.set(X509CertInfo.VALIDITY, interval);
		info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
		info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
		info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
		info.set(X509CertInfo.KEY, new CertificateX509Key(pair.getPublic()));
		info.set(X509CertInfo.VERSION, new CertificateVersion(
				CertificateVersion.V3));
		AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
		info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));

		// Sign the cert to identify the algorithm that's used.
		X509CertImpl cert = new X509CertImpl(info);
		cert.sign(privkey, algorithm);

		// Update the algorith, and resign.
		algo = (AlgorithmId) cert.get(X509CertImpl.SIG_ALG);
		info.set(CertificateAlgorithmId.NAME + "."
				+ CertificateAlgorithmId.ALGORITHM, algo);
		cert = new X509CertImpl(info);
		cert.sign(privkey, algorithm);
		return cert;
	}

	public static void main(String[] args) {
		try {

//			String makeKeySrc = "/Users/mium2/project/java/MqttChat/certificate/private/test01.key";
			String makeKeySrc = "/Users/mium2/project/git_repository/mqttMessenger/mqtt-chat-server/certificate/msp-chat.key";
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(makeKeySrc));
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			kpg.initialize(1024);
			KeyPair mkeyPair = kpg.genKeyPair();

			os.writeObject(mkeyPair);
			os.close();
			System.out.println("Private key : " + mkeyPair.getPrivate() + "\n");
			System.out.println("Public key : " + mkeyPair.getPublic());
			X509Certificate x509Certificate = new X509CertTool().generateCertificate("CN=mium2, OU=Test Team, O=Company, L=Seoul, C=KR", mkeyPair, 365 * 10, "SHA1withRSA");

			// BASE64로 인코딩한 인증서 만들기 (PEM)
//			convertToPemCrt(x509Certificate);

			// BASE64로 인코딩 하지 않은 인증서 만들기
			FileOutputStream fos = new FileOutputStream("/Users/mium2/project/git_repository/mqttMessenger/mqtt-chat-server/certificate/msp-chat.crt");
			fos.write(x509Certificate.getEncoded());
			fos.close();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private static void convertToPemCrt(X509Certificate cert) throws CertificateEncodingException {
		Base64 encoder = new Base64(64);
		String cert_begin = "-----BEGIN CERTIFICATE-----\n";
		String end_cert = "-----END CERTIFICATE-----";

		byte[] derCert = cert.getEncoded();
		String pemCertPre = new String(encoder.encode(derCert));
		String pemCert = cert_begin + pemCertPre + end_cert;

		try {
			FileOutputStream fos = new FileOutputStream("/Users/mium2/project/git_repository/mqttMessenger/mqtt-chat-server/certificate/msp-chat-pem.crt");
			fos.write(pemCert.getBytes());
			fos.close();
		}catch (Exception e){
			e.printStackTrace();
		}

	}


}
