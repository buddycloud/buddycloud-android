package com.buddycloud.http;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;

import org.apache.http.conn.ssl.SSLSocketFactory;

import android.content.Context;
import android.net.SSLCertificateSocketFactory;
import android.net.SSLSessionCache;

public class AndroidInsecureSSLSocketFactory extends SSLSocketFactory {

	private static final int SSL_HANDSHAKE_TO = 3 * 60 * 1000;
	private javax.net.ssl.SSLSocketFactory innerFactory;

	public AndroidInsecureSSLSocketFactory(KeyStore truststore, Context context) throws Exception {
		super(truststore);
		this.innerFactory = SSLCertificateSocketFactory.getInsecure(SSL_HANDSHAKE_TO, 
				new SSLSessionCache(context));
	}
	
	@Override
	public Socket createSocket() throws IOException {
		return innerFactory.createSocket();
	}

	@Override
	public Socket createSocket(Socket socket, String host, int port,
			boolean autoClose) throws IOException, UnknownHostException {
		return innerFactory.createSocket(socket, host, port, autoClose);
	}
}