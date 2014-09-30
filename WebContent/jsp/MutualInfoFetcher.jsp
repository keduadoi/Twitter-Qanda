<%@ page import="java.io.*" %>
<%@ page import="java.util.*"%>
<%@ page import="java.lang.Math" %>
<%@ page import="java.util.Date" %>
<%@ page import="org.json.*" %>
<%@ page import="utils.*" %>
<%@ page import="javax.servlet.http.Cookie" %>
<%@ page import="com.couchdb.*" %>
<%@ page import="com.google.gson.JsonObject" %>

<%	
	response.setCharacterEncoding("UTF-8");
	response.setContentType("application/x-javascript;charset=UTF-8");
	double threshold = Double.parseDouble(request.getParameter("threshold"));
	
	Cookie[] cookies = request.getCookies();
    String dbName = "qanda";
    for(Cookie cookie : cookies){
    	if(cookie.getName().equals("currentDb")){
    		dbName = cookie.getValue();
    		break;
    	}
    }
    
	CreateMutualInformationNetwork mutualInfoNetwork = new CreateMutualInformationNetwork();
	//Get the tweets and create network nodes and links
	ArrayList<JsonObject> mutualInformationList = mutualInfoNetwork.CreateMutualInformationList(dbName+"_discussions",threshold);
	System.out.println(mutualInformationList.toString());
	JSONObject result = mutualInfoNetwork.GetD3StructureMI(mutualInformationList);
	System.out.println(result.toString());
	out.write(result.toString());
	
	out.flush();
	//conn.close();
	
%>