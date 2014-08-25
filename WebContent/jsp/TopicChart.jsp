<%@ page import="org.json.JSONObject" %>
<%@ page import="Chapter5.text.EventSummaryExtractor" %>

<%
	String callbackName = request.getParameter("callback");
	response.setCharacterEncoding("UTF-8");
	response.setContentType("application/x-javascript;charset=UTF-8");
	String infilename = request.getSession().getServletContext().getRealPath("/")+"/"+request.getParameter("filename");	
	EventSummaryExtractor ese = new EventSummaryExtractor();
	ese.InitializeCategories();
	JSONObject result = ese.ExtractCategoryTrends(infilename);
	if(callbackName != null)
	{
		out.write(callbackName + "(" + result.toString() + ");");
	}
	else
	{
		out.write(result.toString());
	}	
	out.flush();	
%>