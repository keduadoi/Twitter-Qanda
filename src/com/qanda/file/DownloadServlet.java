package com.qanda.file;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.qanda.network.CreateD3Network;

/**
 * Servlet implementation class DownloadServlet
 */
@WebServlet("/DownloadServlet")
public class DownloadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final int BYTES_DOWNLOAD = 1024;
       
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
//		response.setContentType("application/octet-stream");
//		response.setHeader("Content-Disposition",
//	                     "attachment;filename=tweets.txt");
		
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
		
		String callbackName = null;
//		try {
//			callbackName = tweetsObj.get("callback").toString();
//		} catch (JSONException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//		}
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
		
		
//		JSONObject tweetsJson;
//		JSONArray rawTweetsArray = null;
//		try {
//			tweetsJson = new JSONObject(tweetsStr);
//			rawTweetsArray = tweetsJson.getJSONArray("raw");
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
		
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
		doGet(request, response);
	}
	

}
