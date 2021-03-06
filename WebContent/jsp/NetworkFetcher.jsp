<%@ page import="java.io.*" %>
<%@ page import="java.util.*"%>
<%@ page import="java.util.regex.Matcher"%>
<%@ page import="java.util.regex.Pattern"%>
<%@ page import="java.lang.Math" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.lang.NumberFormatException" %>
<%@ page import="org.json.*" %>
<%@ page import="com.qanda.network.*" %>
<%@ page import="com.qanda.support.*" %>
<%@ page import="utils.*" %>
<%@ page import="javax.servlet.http.Cookie" %>

<%	
	response.setCharacterEncoding("UTF-8");
	response.setContentType("application/x-javascript;charset=UTF-8");
	int nof_classes = Integer.parseInt(request.getParameter("nclasses"));
	String callbackName = request.getParameter("callback");
	//String infilename = request.getSession().getServletContext().getRealPath("/")+request.getParameter("filename");
	//System.out.println(infilename);
	JSONObject tagjson = new JSONObject(request.getParameter("hashtagjson"));
	int num_nodes = 10;
	String from = "01/01/2011";
	String to = "12/31/2014";
	try{	
		num_nodes = Integer.parseInt(request.getParameter("num_nodes").toString());
		from = request.getParameter("from").toString();
		to = request.getParameter("to").toString();
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
    
	CreateD3Network crtnode = new CreateD3Network();
	//Get the tweets and create network nodes and links
	JSONObject result = crtnode.ConvertTweetsToDiffusionPath(nof_classes,tagjson,num_nodes,from,to,dbName);	
	if(callbackName != null){
		out.write(callbackName + "(" + result.toString() + ");");
	}
	else{
		out.write(result.toString());
	}
	
	out.flush();
	//conn.close();
	
%>