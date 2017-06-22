package com.tavant.rally.confluence.macro;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import com.tavant.rally.confluence.util.RestApiFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Created by madhan.reddy on 6/1/2017.
 */
public class RallyMacro implements Macro {

  public String execute(Map<String, String> params, String s, ConversionContext conversionContext) throws MacroExecutionException {
    return testRally(params);
  }

  public BodyType getBodyType() { return BodyType.NONE; }

  public OutputType getOutputType() { return OutputType.BLOCK; }

  public static String testRally(Map<String, String> params) {
    RallyRestApi restApi = RestApiFactory.getRallyRestApi();
    StringBuilder out = new StringBuilder("");

    try {
      QueryResponse queryResponse = createQuery(restApi, params);
      wrapResponse(out, queryResponse, params);

    } catch (IOException e) {
      out.append(e.getStackTrace());
    } catch (Exception e) {
      out.append(e.getStackTrace());
    }
    return out.toString();
  }

  private static QueryResponse createQuery(RallyRestApi restApi, Map<String, String> params) throws IOException {
    QueryRequest defects = new QueryRequest("defect");
    defects.setFetch(new Fetch("FormattedID", "Name", "State", "Priority"));
    QueryFilter query = new QueryFilter("State", "<", "Fixed");
    if (params.get("DefectID") != null) {
      query = query.and(new QueryFilter("OID", "=", params.get("DefectID")));
    } else if (params.get("FormattedID") != null) {
      query = query.and(new QueryFilter("FormattedID", "=", params.get("FormattedID")));
    }
    if (params.get("Priority") != null) {
      query = query.and(new QueryFilter("Priority", "=", params.get("Priority")));
    }
    defects.setQueryFilter(query);
    defects.setOrder("Priority ASC,FormattedID ASC");
    defects.setPageSize(1);
    defects.setLimit(100);
    return restApi.query(defects);
  }

  private static void wrapResponse(StringBuilder html, QueryResponse queryResponse, Map<String, String> params) {
    if (queryResponse.wasSuccessful()) {
      System.out.println(String.format("\nTotal results: %d", queryResponse.getTotalResultCount()));

      html.append("<h3>Total results:</h3>"+queryResponse.getTotalResultCount());
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

