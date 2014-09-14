package com.couchdb;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lightcouch.CouchDbClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Servlet implementation class DatabaseOperations
 */
@WebServlet("/DatabaseOperations")
public class DatabaseOperations extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DatabaseOperations() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setContentType("text/html");  
		PrintWriter out = response.getWriter();
		
		String dbName = "qanda";
		
    	if(request.getParameter("dbInput").length()>0){
    		dbName = request.getParameter("dbInput");
    	}
		
    	CouchDbClient dbClient = new CouchDbClient();
    	List<String> allDbs = dbClient.context().getAllDbs();
    	JsonObject dbInfo = new JsonObject();
    	if(allDbs.contains(dbName)){
    		dbClient = new CouchDbClient(dbName, false, "http", "localhost", 5984, null, null);
    		dbInfo.addProperty("isExist", "true");
    		
    		if(request.getParameter("operation")!=null){
    			String operation = request.getParameter("operation");
    			if(operation.equalsIgnoreCase("fetch")){
    				String pythonFile = request.getServletContext().getRealPath("/")+"fetchtweet.py";
    				String[] arguments = new String[]{"python",pythonFile,"-C",dbName};
    				String results = "";
    				try {
    					ProcessBuilder pb = new ProcessBuilder(arguments);
    					Process p = pb.start();
    					BufferedReader buffer = new BufferedReader(new InputStreamReader(p.getInputStream()));
    					String line = buffer.readLine();
    					while(line!=null){
    						System.out.println(line);
    						results = results+line+"\n";
    						line = buffer.readLine();
    					}
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    				System.out.println("End fetching");
    			}else if(operation.equalsIgnoreCase("trace")){
    				String pythonFile = request.getServletContext().getRealPath("/")+"trace.py";
    				System.out.println(pythonFile);
    				String[] arguments = new String[]{"python",pythonFile,"-T",dbName};
    				String graphs = "";
    				try {
    					PrintWriter writer = new PrintWriter(request.getServletContext().getRealPath("/")+dbName+".dot", "UTF-8");
    					ProcessBuilder pb = new ProcessBuilder(arguments);
    					Process p = pb.start();
    					BufferedReader buffer = new BufferedReader(new InputStreamReader(p.getInputStream()));
    					String line = buffer.readLine();
    					while(line!=null){
    						System.out.println(line);
    						writer.println(line);
    						graphs = graphs+line+"\n";
    						line = buffer.readLine();
    					}
    					writer.close();
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    				System.out.println("\nEnd tracing");
    				
    				//decompose graph
    				arguments = new String[]{"ccomps","-x","-o",request.getServletContext().getRealPath("/")+"dot_files/"+"graph.dot",request.getServletContext().getRealPath("/")+dbName+".dot"};
    				try {
    					ProcessBuilder pb = new ProcessBuilder(arguments);
    					System.out.println(pb.command().toString());
    					pb.start();
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    				System.out.println("\nEnd decomposing");
    				
    				//store discussions
    				CouchDbClient dbDiscussionClient = new CouchDbClient(dbName+"_discussions", true, "http", "localhost", 5984, null, null);
    				File[] graphFiles = new File(request.getServletContext().getRealPath("/")+"dot_files").listFiles();
    				for(File file: graphFiles){
    					List<JsonObject> allDiscussions = dbDiscussionClient.view("_all_docs").query(JsonObject.class);
    					
    					String wholeFile = new String(Files.readAllBytes(file.toPath()),StandardCharsets.UTF_8);
    					wholeFile = wholeFile.substring(wholeFile.indexOf("{")+1);
    					wholeFile = wholeFile.substring(0, wholeFile.lastIndexOf(";"));

    					String[] discussionElements = wholeFile.split(";\n");
//    					for(String s : discussionElements){
//    						System.out.println(s);
//    					}
    					
    					JsonObject discussion = new JsonObject();
						discussion.addProperty("_id",String.valueOf(allDiscussions.size()));
						JsonArray tweets = new JsonArray();
						JsonArray users = new JsonArray();
						JsonArray edges = new JsonArray();
						
    					for(String elem : discussionElements){
    						if(elem.contains("[label=")){
    							String tweet_id = elem.substring(0, elem.indexOf("[")).replaceAll("\\s+", "");
    							String created_at = elem.substring(elem.indexOf("<b>")+3, elem.indexOf("</b>"));
    							String username = elem.substring(elem.indexOf("<br/>")+5, elem.indexOf(":",elem.indexOf("<br/>")+5));
    							String text = elem.substring(elem.indexOf(username)+username.length()+2, elem.length()-2);
    							
    							JsonObject tweet = new JsonObject();
    							tweet.addProperty("_id", tweet_id);
    							tweet.addProperty("id_str", tweet_id);
    							tweet.addProperty("created_at", created_at);
    							tweet.addProperty("from", username);
    							tweet.addProperty("text", text);
    							tweets.add(tweet);
    							
    							JsonObject user = new JsonObject();
    							user.addProperty("_id", username);
    							user.addProperty("screen_name", username);
    							users.add(user);
    							
    						}else if(elem.contains("->")){
    							elem = elem.replaceAll("\\s+", "");
    							String reply_root_id = elem.split("->")[0];
    							String reply_id = elem.split("->")[1];
    							
    							JsonObject edge = new JsonObject();
    							edge.addProperty("_id", String.valueOf(edges.size()));
    							edge.addProperty("reply_root_id_str", reply_root_id);
    							edge.addProperty("reply_id_str", reply_id);
    							edges.add(edge);
    						}	 						
    					}
    					
    					discussion.add("tweets", tweets);
    					discussion.add("users",users);
    					discussion.add("edges", edges);
    					
    					//System.out.println(discussion.toString());
    					dbDiscussionClient.save(discussion);
    				}
    				
    			}
    		}
    		
    		dbInfo.addProperty("doc_count", String.valueOf(dbClient.context().info().getDocCount()));
    		out.write(dbInfo.toString());
    	}else{
    		dbInfo.addProperty("isExist", "false");
    		if(request.getParameter("operation")!=null){
    			String operation = request.getParameter("operation");
    			if(operation.equalsIgnoreCase("create")){
    				dbClient.context().createDB(dbName);
    			}else if(operation.equalsIgnoreCase("fetch")){
    				String pythonFile = request.getServletContext().getRealPath("/")+"fetchtweet.py";
    				String[] arguments = new String[]{"python",pythonFile,"-C",dbName};
    				String graphs = "";
    				try {
    					ProcessBuilder pb = new ProcessBuilder(arguments);
    					Process p = pb.start();
    					BufferedReader buffer = new BufferedReader(new InputStreamReader(p.getInputStream()));
    					String line = buffer.readLine();
    					while(line!=null){
    						graphs = graphs+line+"\n";
    						line = buffer.readLine();
    					}
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
    				System.out.println(graphs+"\nEnd python script");
    			}
    		}
    		
    		out.write(dbInfo.toString());
    	}
	}

}
