package com.couchdb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.lightcouch.CouchDbClient;

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
