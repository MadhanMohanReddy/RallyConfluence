package com.tavant.rally.confluence.util;

import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.client.HttpClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.scheme.Scheme;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by madhan.reddy on 6/21/2017.
 */
public class RestApiFactory {

  public static final String CONFIG_PROPERTIES_FILE = "config.properties";

  public static RallyRestApi getRallyRestApi() {
    RallyRestApi restApi = null;
    Properties configProps = new Properties();
    try {
      configProps.load(RestApiFactory.class.getClassLoader().getResourceAsStream(CONFIG_PROPERTIES_FILE));
    } catch (IOException e) {
      System.out.println("Properties file '" + CONFIG_PROPERTIES_FILE + "' not found in the classpath");
    }
    try {
      restApi = new RallyRestApi(
        new URI(configProps.getProperty("RALLY_SERVER")),
        configProps.getProperty("API_KEY"));
      restApi.setProxy(
        new URI(configProps.getProperty("RALLY_PROXY_SERVER")),
        configProps.getProperty("USERNAME"),
        configProps.getProperty("PASSWORD"));

    } catch (URISyntaxException e) {
      System.out.println(e.getStackTrace());
    }
    trustRallyRestAPI(restApi);
    restApi.setApplicationName(configProps.getProperty("APPLICATION_NAME"));
    restApi.setWsapiVersion(configProps.getProperty("WSAPI_VERSION"));

    return restApi;
  }

  private static void trustRallyRestAPI(RallyRestApi restApi) {
    HttpClient client = restApi.getClient();
    SSLSocketFactory sf = null;
    try {
      sf = new SSLSocketFactory(new TrustStrategy() {
        public boolean isTrusted(X509Certificate[] certificate, String authType)
          throws CertificateException {
          //trust all certs
          return true;
        }
      }, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    } catch (Exception e) {
      System.out.println(e.getStackTrace());
    }
    client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, sf));
  }
}
