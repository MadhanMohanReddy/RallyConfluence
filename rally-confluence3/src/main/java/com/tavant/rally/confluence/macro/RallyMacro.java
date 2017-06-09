package com.tavant.rally.confluence.macro;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.client.HttpClient;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.scheme.Scheme;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Created by madhan.reddy on 6/1/2017.
 */
public class RallyMacro implements Macro {

  public String execute(Map<String, String> params, String s, ConversionContext conversionContext) throws MacroExecutionException {
    return "<h1>Hello Rally</h1>" + testRally(params);
  }

  public BodyType getBodyType() { return BodyType.NONE; }

  public OutputType getOutputType() { return OutputType.BLOCK; }

  public static String testRally(Map<String, String> params) {
    String rallyURL = "https://rally1.rallydev.com";
    String rallyProxyURL = "http://rally1.rallydev.com";
    String applicationName = "RallyIntegration";
    String apiKey = "_OsgoO1OVRTeAnjm5oaMhC2VkwQXkno7GY7TEdqOw";
    String userName = "madhan.reddy@tavant.com";
    String userPassword = "Welcome123";

    StringBuilder out = new StringBuilder("");
    RallyRestApi restApi = null;

    try {
      restApi = connectRally(rallyURL, rallyProxyURL, applicationName, apiKey, userName, userPassword);

      QueryResponse queryResponse = createQuery(restApi, params);

      wrapResponse(out, queryResponse, params);

    } catch (URISyntaxException e) {
      out.append(e);
    } catch (IOException e) {
      out.append(e);
    } catch (Exception e) {
      out.append(e);
    }
    return out.toString();
  }

  private static String extractParams(Map<String, String> params) {
    if (params.get("Count") != null) {
      return params.get("Count");
    }
    return null;
  }

  private static RallyRestApi connectRally(String rallyURL, String rallyProxyURL, String applicationName,
                                           String apiKey, String userName, String userPassword) throws Exception {
    RallyRestApi restApi = new RallyRestApi(new URI(rallyURL), apiKey);
    restApi.setApplicationName(applicationName);
    setupProxy(rallyProxyURL, userName, userPassword, restApi);
    return restApi;
  }

  private static void setupProxy(String rallyProxyURL, String userName, String userPassword,
                                 RallyRestApi restApi) throws Exception {
    restApi.setProxy(new URI(rallyProxyURL), userName, userPassword);
    HttpClient client = restApi.getClient();

    SSLSocketFactory sf = new SSLSocketFactory(new TrustStrategy() {
      public boolean isTrusted(X509Certificate[] certificate, String authType)
        throws CertificateException {
        //trust all certs
        return true;
      }
    }, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, sf));
  }

  private static QueryResponse createQuery(RallyRestApi restApi, Map<String, String> params) throws IOException {
    QueryRequest defects = new QueryRequest("defect");
    defects.setFetch(new Fetch("FormattedID", "Name", "State", "Priority"));
    defects.setQueryFilter(new QueryFilter("State", "<", "Fixed"));
    defects.setOrder("Priority ASC,FormattedID ASC");
    defects.setPageSize(1);
    defects.setLimit(Integer.parseInt(extractParams(params)));
    return restApi.query(defects);
  }

  private static void wrapResponse(StringBuilder html, QueryResponse queryResponse, Map<String, String> params) {
    if (queryResponse.wasSuccessful()) {
      System.out.println(String.format("\nTotal results: %d", queryResponse.getTotalResultCount()));

      html.append("<h3>Total results:</h3>"+queryResponse.getTotalResultCount());
      html.append("<h3>Top " + extractParams(params) + ":</h3>");
      html.append("<table><tr><th>FormattedID</th><th>Name</th><th>Priority</th><th>State</th></tr>");
      for (JsonElement result : queryResponse.getResults()) {
        JsonObject defect = result.getAsJsonObject();
        html.append("<tr>" + "<td>" + defect.get("FormattedID").getAsString() + "</td>"
          + "<td>" + defect.get("Name").getAsString() + "</td>"
          + "<td>" + defect.get("Priority").getAsString() + "</td>"
          + "<td>" + defect.get("State").getAsString() + "</td>"
          + "</tr>");
      }
      html.append("</table>");
    } else {
      System.err.println("The following errors occurred: ");
      html.append("<h1>The following errors occurred: </h1>");
      for (String err : queryResponse.getErrors()) {
        System.err.println("\t" + err);
        html.append(err);
      }
    }
  }
}

