package com.example.servlets;

import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
// import java.util.Random;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.RestController;

// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestMethod;

// @RestController
@WebServlet(name = "TestServlet", value = "/test")
public class TestServlet extends HttpServlet {

  public void init() {}

  @Override
  // @RequestMapping(value="/result", method=RequestMethod.POST)
  // protected void doPost(
  protected void doPost(
      HttpServletRequest request,
      HttpServletResponse response
  ) throws ServletException, IOException {
    PrintWriter out = response.getWriter();
    out.println("<html><body>");
    String repoName = request.getParameter("repo");
    Crawler crawler = new Crawler();
    // String repoName = "Grasscutters/Grasscutter";
    String jsonString = crawler.getRepoInfo(
        "https://api.github.com/repos/" + repoName
    );
    Type type = new TypeToken<HashMap<String, Object>>() {}.getType();
    HashMap<String, Object> hashMap = crawler.gson.fromJson(jsonString, type);

    // 开发成员：
    // System.out.println(hashMap.get("contributors_url").toString());
    // 有多少开发成员？
    int contributorsCount = crawler.getContributorsCount(repoName);
    // System.out.println("Contributors num: " + contributorsCount);
    out.println("<h1> Contributors num: " + contributorsCount + "</h1>");
    // System.out.println("done");
    // 谁是最活跃开发成员？
    String con = crawler.getRepoActiveContributor(repoName);
    // System.out.println("Active contributors: " + con);
    out.println("<h1> Active contributors: " + con + "</h1>");
    // System.out.println("done");

    // issue：包括了issue和pull request
    List<JSONObject> jsonValues = crawler.getRepoIssues(
        hashMap
        .get("issues_url")
        .toString()
        .substring(0, hashMap.get("issues_url").toString().length() - 9),
        " ",
        " "
    );

    // 有多少open的issue？（过滤掉了pull request）
    // System.out.println(
    //   jsonValues
    //     .stream()
    //     .filter((JSONObject j) -> !j.has("pull_request"))
    //     .count()
    // );
    out.println(
         "<h1> Open issues count: " 
         +
         jsonValues
        .stream()
        .filter((JSONObject j) -> !j.has("pull_request"))
        .count() 
        +
        "</h1>"
    );

    // 有多少closed的issue？（过滤掉了pull request）
    JSONObject json = crawler.getClosedIssueCount(repoName);
    // System.out.println((int) json.get("total_count"));
    out.println(
        "<h1> Closed issues count: " + (int) json.get("total_count") + "</h1>"
    );

    // issue解决时间
    // Random rand = new Random();
    // int issueIndex = rand.nextInt(json.length());
    System.out.println(json.toString());
    String startT = json
        .getJSONArray("items")
        .getJSONObject(0)
        .getString("created_at");
    String closeT = json
        .getJSONArray("items")
        .getJSONObject(0)
        .getString("updated_at");
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    Date date1;
    Date date2;
    try {
      date1 = format.parse(startT);
      date2 = format.parse(closeT);
      long diff = date2.getTime() - date1.getTime();
      Calendar date2s = Calendar.getInstance();
      date2s.setTime(date2);
      Calendar date1s = Calendar.getInstance();
      date1s.setTime(date1);
      int year = date2s.get(Calendar.YEAR) - date1s.get(Calendar.YEAR);
      int month = date2s.get(Calendar.MONTH) - date1s.get(Calendar.MONTH);
      int day =
          date2s.get(Calendar.DAY_OF_MONTH) - date1s.get(Calendar.DAY_OF_MONTH);
      if (day < 0) {
        month -= 1;
        date2s.add(Calendar.MONTH, -1);
        day = day + date2s.getActualMaximum(Calendar.DAY_OF_MONTH);
      }
      if (month < 0) {
        month = (month + 12) % 12;
        year--;
      }
      long days = diff / (1000 * 60 * 60 * 24);
      long hours = (diff - days * (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
      long minutes =
          (diff - days * (1000 * 60 * 60 * 24) - hours * (1000 * 60 * 60)) 
          /
          (1000 * 60);
      long s =
          (diff / 1000 - days * 24 * 60 * 60 - hours * 60 * 60 - minutes * 60);
      String countTime =
          "" 
          +
          year 
          +
          "y" 
          +
          month 
          +
          "m" 
          +
          day 
          +
          "d " 
          +
          hours 
          +
          "h" 
          +
          minutes 
          +
          "m" 
          +
          s 
          +
          "s";
      out.println("<h1> Random issue finish time: " + countTime + "</h1>");
    } catch (ParseException e) {
      e.printStackTrace();
    }

    // 发布与提交
    // 有多少版本的发布？
    List<JSONObject> releasesValues = crawler.getRepoIssues(
        hashMap
        .get("releases_url")
        .toString()
        .substring(0, hashMap.get("releases_url").toString().length() - 5),
        " ",
        " "
    );
    System.out.println(releasesValues.size());
    out.println(
        "<h1> Release versions count: " + releasesValues.size() + "</h1>"
    );

    // 发布之间有多少提交？
    int[] differ = new int[releasesValues.size() - 1];
    for (int i = 0; i < releasesValues.size() - 1; i++) {
      JSONObject json1 = releasesValues.get(i);
      JSONObject json2 = releasesValues.get(i + 1);
      String tag1 = json1.getString("tag_name");
      String tag2 = json2.getString("tag_name");
      differ[i] = crawler.getCompareRelease(repoName, tag1, tag2);
      // System.out.println();
    }
    System.out.println(Arrays.toString(differ));
    out.println(
        "<h1> Commits count between release: " + Arrays.toString(differ) + "</h1>"
    );

    try {
      // commit时间（周几）
      JSONArray commitS = crawler.getCommitWeekday(repoName);
      int[] week = new int[7];
      for (int i = 0; i < 7; i++) {
        week[i] = 0;
      }
      for (int i = 0; i < commitS.length(); i++) {
        JSONObject jso = commitS.getJSONObject(i);
        if (jso.getInt("total") != 0) {
          for (int j = 0; j < 7; j++) {
            week[j] += jso.getJSONArray("days").getInt(j);
          }
        }
      }
      System.out.println(Arrays.toString(week));
      out.println(
          "<h1> Commits count by weekdays: " + Arrays.toString(week) + "</h1>"
      );
    } catch (Exception e) {
      e.printStackTrace();
    }

    out.println("</body></html>");
  }
}
