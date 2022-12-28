// package crawler;
package com.example.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Crawler {

  private OkHttpClient okHttpClient = new OkHttpClient();
  public Gson gson = new GsonBuilder().create();

  private HashSet<String> urlBlackList = new HashSet<>();

  {
    urlBlackList.add("https://github.com/events");
    urlBlackList.add("https://github.community");
    urlBlackList.add("https://github.com/about");
    urlBlackList.add("https://github.com/pricing");
    urlBlackList.add("https://github.com/contact");
  }

  public Response getRepo(String repoName) throws IOException {
    String url = repoName;

    Request request = new Request.Builder()
      .url(url)
      .header("Authorization", "token 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")
      .build();
    Call call = okHttpClient.newCall(request);
    Response response = call.execute();
    if (!response.isSuccessful()) {
      System.out.println("访问 Github API 失败! url = " + url);
      call.cancel();
      return null;
    }
    call.cancel();
    return response;
  }

  public int getCompareRelease(
    String repoName,
    String repoName1,
    String repoName2
  ) throws IOException {
    String url =
      "https://api.github.com/repos/" +
      repoName +
      "/compare/" +
      repoName1 +
      "..." +
      repoName2;

    Request request = new Request.Builder()
      .url(url)
      .header("Authorization", "token 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")
      .build();
    Call call = okHttpClient.newCall(request);
    Response response = call.execute();
    if (!response.isSuccessful()) {
      System.out.println("访问 Github API 失败! url = " + url);
      call.cancel();
      return -1;
    }
    JSONObject json = new JSONObject(response.body().string());
    call.cancel();
    return (json.getInt("ahead_by") + json.getInt("behind_by"));
  }

  // 调用 Github API 获取指定仓库的信息
  public String getRepoInfo(String repoName) throws IOException {
    Response response = getRepo(repoName);
    return response.body().string();
  }

  public int getRepoElementCount(String repoName) throws IOException {
    Response response = getRepo(repoName);
    JSONArray jsonArray = new JSONArray(response.body().string());
    return jsonArray.length();
  }

  public int getContributorsCount(String repoName) throws IOException{
    String url = "https://api.github.com/repos/" + repoName + "/contributors?per_page=1&anon=true";
    Request request = new Request.Builder()
      .url(url)
      .header("Authorization", "token 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")
      .build();
    Call call = okHttpClient.newCall(request);
    Response response = call.execute();
    String links = response.headers().get("link");
    links = links.substring(links.length() - 20,links.length() - 13);
    int num = Integer.valueOf(links.replaceAll(".*[^\\d](?=(\\d+))",""));
    call.cancel();
    return num;
  }

  public JSONObject getClosedIssueCount(String repoName) throws IOException {
    String url = "https://api.github.com/search/issues";
    HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
    urlBuilder.addQueryParameter(
      "q",
      "repo:" + repoName + " is:issue" + " state:closed"
    );
    urlBuilder.addQueryParameter("per_page", "1");

    url = urlBuilder.build().toString();
    System.out.println(url);

    Request request = new Request.Builder()
      .url(url)
      .header("Authorization", "token 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")
      .build();
    Call call = okHttpClient.newCall(request);
    Response response = call.execute();
    if (!response.isSuccessful()) {
      System.out.println("访问 Github API 失败! url = " + url);
      call.cancel();
      return null;
    }
    JSONObject json = new JSONObject(response.body().string());
    call.cancel();
    return json;
  }

  public JSONArray getCommitWeekday(String repoName) throws IOException {
    String url =
      "https://api.github.com/repos/"+ repoName +"/stats/commit_activity";

    Request request = new Request.Builder()
      .url(url)
      .header("Authorization", "token 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")
      .build();
    Call call = okHttpClient.newCall(request);
    Response response = call.execute();
    if (!response.isSuccessful()) {
      System.out.println("访问 Github API 失败! url = " + url);
      call.cancel();
      return null;
    }
    JSONArray json = new JSONArray(response.body().string());
    call.cancel();
    return json;
  }

  public String getRepoActiveContributor(String repoName) throws IOException {
    String url =
      "https://api.github.com/repos/" + repoName + "/stats/contributors";

    Request request = new Request.Builder()
      .url(url)
      .header("Authorization", "token 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")
      .build();
    Call call = okHttpClient.newCall(request);
    Response response = call.execute();
    if (!response.isSuccessful()) {
      System.out.println("访问 Github API 失败! url = " + url);
      call.cancel();
      return null;
    }
    JSONArray jsonArray = new JSONArray(response.body().string());
    List<JSONObject> jsonValues = new ArrayList<JSONObject>();
    for (int i = 0; i < jsonArray.length(); i++) {
      jsonValues.add(jsonArray.getJSONObject(i));
    }
    Collections.sort(
      jsonValues,
      new Comparator<JSONObject>() {
        @Override
        public int compare(JSONObject a, JSONObject b) {
          int valA = 0;
          int valB = 0;

          try {
            valA = (int) a.get("total");
            valB = (int) b.get("total");
          } catch (JSONException e) {
            e.printStackTrace();
          }
          return valB - valA;
        }
      }
    );
    call.cancel();
    return jsonValues.get(0).getJSONObject("author").getString("login").toString();
  }

  public List<JSONObject> getRepoIssues(
    String repoName,
    String arg0,
    String arg1
  ) throws IOException {
    String url = repoName;
    HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
    urlBuilder.addQueryParameter("per_page", "100");
    urlBuilder.build();
    urlBuilder.addQueryParameter("state", "open");

    url = urlBuilder.build().toString();
    System.out.println(url);

    Request request = new Request.Builder()
      .url(url)
      .header("Authorization", "token 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")
      .build();
    Call call = okHttpClient.newCall(request);
    Response response = call.execute();
    if (!response.isSuccessful()) {
      System.out.println("访问 Github API 失败! url = " + url);
      return null;
    }
    JSONArray jsonArray = new JSONArray(response.body().string());
    List<JSONObject> jsonValues = new ArrayList<JSONObject>();
    for (int i = 0; i < jsonArray.length(); i++) {
      jsonValues.add(jsonArray.getJSONObject(i));
    }
    call.cancel();
    return jsonValues;
  }
}
