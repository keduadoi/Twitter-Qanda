<%@ page import="java.io.*" %>
<%@ page import="java.util.*"%>
<%@ page import="java.util.regex.Matcher"%>
<%@ page import="java.util.regex.Pattern"%>
<%@ page import="java.lang.Math" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.lang.NumberFormatException" %>
<%@ page import="org.json.*" %>
<%@ page import="Chapter5.network.*" %>
<%@ page import="Chapter5.support.*" %>
<%@ page import="discussions.*" %>

<%	
	response.setCharacterEncoding("UTF-8");
	response.setContentType("application/x-javascript;charset=UTF-8");
	String tracePythonFile = request.getSession().getServletContext().getRealPath("/")+"\\"+"list1.py";
	System.out.println(tracePythonFile);
	CreateDiscussionGraphs discussionGraphs = new CreateDiscussionGraphs();
	String graphs = discussionGraphs.GetDiscussions(tracePythonFile);
	System.out.println(graphs);
	JSONObject testObj = new JSONObject();
	testObj.put("graph",graphs.toString());
	//out.write(graphs.toString());
	out.write(testObj.toString());
	out.flush();
%>