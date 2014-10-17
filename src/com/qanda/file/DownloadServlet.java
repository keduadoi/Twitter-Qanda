package com.qanda.file;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lightcouch.CouchDbClient;

import com.google.gson.JsonObject;
import com.qanda.network.CreateD3Network;

/**
 * Servlet implementation class DownloadServlet
 */
@WebServlet("/DownloadServlet")
public class DownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	/**
     * @see HttpServlet#HttpServlet()
     */
    public DownloadServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		String filePath = "C:" + File.separator + "temp" + File.separator + "tweets.dat";
        File file = new File(filePath);
 
        // set response headers (it is always better to write header's before writing to response stream)
        response.setContentType(getServletContext().getMimeType(file.getAbsolutePath()));
        // if Content-Disposition header value is set to attachment, file can be downloaded as attachment
        response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName());
		
		String tweetsStr = "No TWEET";
		if(request.getParameter("tweetsJson")!=null){
			tweetsStr = request.getParameter("tweetsJson");
		}
		
		System.out.println(tweetsStr);
		JSONObject tweetsObj = null;
		try {
			tweetsObj = new JSONObject(tweetsStr);
		} catch (JSONException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		
		int nof_classes = 1;
		try {
			nof_classes = Integer.parseInt(tweetsObj.get("nclasses").toString());
		} catch (NumberFormatException | JSONException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		String infilename = "tweets_single_lines.dat";
		try {
			infilename = request.getSession().getServletContext().getRealPath("/")+tweetsObj.get("filename").toString();
		} catch (JSONException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}	
		JSONObject tagjson = null;
		try {
			tagjson = tweetsObj.getJSONObject("hashtagjson");
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		int num_nodes = 10;
		String from = "01/01/2011";
		String to = "12/31/2014";
		try{	
			num_nodes = Integer.parseInt(tweetsObj.get("num_nodes").toString());
			from = tweetsObj.get("from").toString();
			to = tweetsObj.get("to").toString();
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
		
		Cookie[] cookies = request.getCookies();
        String dbName = "qanda";
        for(Cookie cookie : cookies){
        	if(cookie.getName().equals("currentDb")){
        		dbName = cookie.getValue();
        		break;
        	}
        }
		
		System.out.println("File: "+infilename);
		System.out.println("nof_classes: "+nof_classes);
		System.out.println("tagjson: "+tagjson.toString());
		CreateD3Network crtnode = new CreateD3Network();
		System.out.println("num_nodes: "+num_nodes);
		System.out.println("from: "+from);
		System.out.println("to: "+to);
		JSONObject result = crtnode.ConvertTweetsToDiffusionPath(nof_classes,tagjson,num_nodes,from,to,dbName);
		
		JSONArray rawTweetsArray = null;
		try {
			rawTweetsArray = result.getJSONArray("raw");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PrintWriter printWriter = new PrintWriter(response.getOutputStream());
		for(int i=0;i<rawTweetsArray.length();++i){
			try {
				printWriter.println(rawTweetsArray.get(i).toString());
				printWriter.flush();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		printWriter.close();	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
		String filePath = "C:" + File.separator + "temp" + File.separator + "tweets.dat";
        File file = new File(filePath);
 
        // set response headers (it is always better to write header's before writing to response stream)
        response.setContentType(getServletContext().getMimeType(file.getAbsolutePath()));
        // if Content-Disposition header value is set to attachment, file can be downloaded as attachment
        response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName());
        
		String dbName = "qanda";
		String from = "01/01/2011";
		String to = "12/31/2014";
		String period = "";
		
		if(request.getParameter("period")==null){
			return;
		}else{
			period = request.getParameter("period");
			dbName = request.getParameter("currentDb");
			if(period.equalsIgnoreCase("specifyTime")){
				from = request.getParameter("from");
				to = request.getParameter("to");
			}
		}
        
		JSONArray rawTweetsArray = new JSONArray();
		DateTimeFormatter fmt = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss Z yyyy");
		DateTime now = new DateTime();
		
        if(period.equalsIgnoreCase("lastHour")){	
        	DateTime lastHour = now.minusHours(1);
        	rawTweetsArray = getRawTweets(lastHour.toString(fmt), now.toString(fmt), dbName);
        }else if(period.equalsIgnoreCase("today")){
        	DateTime midnight = new DateTime(new Date()).withTimeAtStartOfDay();
        	rawTweetsArray = getRawTweets(midnight.toString(fmt), now.toString(fmt), dbName);
        }else if(period.equalsIgnoreCase("lastWeek")){
        	DateTime midnight = new DateTime(new Date()).withTimeAtStartOfDay();
        	DateTime lastWeek = midnight.minusWeeks(1);
        	rawTweetsArray = getRawTweets(lastWeek.toString(fmt), now.toString(fmt), dbName);
        }else if(period.equalsIgnoreCase("lastMonth")){
        	DateTime midnight = new DateTime(new Date()).withTimeAtStartOfDay();
        	DateTime lastMonth = midnight.minusMonths(1);
        	rawTweetsArray = getRawTweets(lastMonth.toString(fmt), now.toString(fmt), dbName);
        }else if(period.equalsIgnoreCase("specifyTime")){
        	DateTimeFormatter tempFmt = DateTimeFormat.forPattern("MM/dd/yyyy");
        	DateTime fromDT = tempFmt.parseDateTime(from);
        	DateTime toDT = tempFmt.parseDateTime(to).plusDays(1).minusSeconds(1);
        	rawTweetsArray = getRawTweets(fromDT.toString(fmt), toDT.toString(fmt), dbName);
        }
        
        PrintWriter printWriter = new PrintWriter(response.getOutputStream());
		for(int i=0;i<rawTweetsArray.length();++i){
			try {
				printWriter.println(rawTweetsArray.get(i).toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			printWriter.flush();
		}
		if(rawTweetsArray.length()==0){
			printWriter.println("No tweet in this interval");
			printWriter.flush();
		}
		printWriter.close();	
	}
	
	private JSONArray getRawTweets(String from, String to, String dbName){
		JSONArray tweetsArray = new JSONArray();
		CouchDbClient dbClient = new CouchDbClient(dbName, false, "http", "localhost", 5984, null, null);
		List<JsonObject> allTweets = dbClient.view("_all_docs").includeDocs(true).limit(5001).startKeyDocId(null).query(JsonObject.class);
		DateTimeFormatter fmt = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss Z yyyy");

		while(allTweets.size()>1)
		{
    		String lastDocId = "";
    		for(int i=0;i<allTweets.size()-1;++i){   	
    			try{
	    			JsonObject tweetJson = allTweets.get(i);
				    DateTime createdDate = fmt.parseDateTime(tweetJson.get("created_at").toString().replaceAll("\"", ""));
				    DateTime fromDate = fmt.parseDateTime(from);
				    DateTime toDate = fmt.parseDateTime(to);
				    
				    //Check if the tweet is on the selected period
				    if(createdDate.isAfter(fromDate) && createdDate.isBefore(toDate)){
				    	tweetsArray.put(new JSONObject(tweetJson.toString()));
				    }			    
    			}catch(Exception e){
    				e.printStackTrace();
    			}
    		}
    		lastDocId = allTweets.get(allTweets.size()-1).get("id_str").toString().replace("\"", "");
    		allTweets = dbClient.view("_all_docs").includeDocs(true).limit(5001).startKeyDocId(lastDocId).query(JsonObject.class);
		}

		//last document
		JsonObject tweetJson = allTweets.get(0);
	    DateTime createdDate = fmt.parseDateTime(tweetJson.get("created_at").toString().replaceAll("\"", ""));
	    DateTime fromDate = fmt.parseDateTime(from);
	    DateTime toDate = fmt.parseDateTime(to);
	    
	    //Check if the tweet is on the selected period
	    if(!(createdDate.isBefore(fromDate) || createdDate.isAfter(toDate))){
	    	try {
				tweetsArray.put(new JSONObject(tweetJson.toString()));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }	    
		
		return tweetsArray;
	}
	

}
