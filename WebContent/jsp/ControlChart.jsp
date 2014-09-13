<%@ page import="java.util.ArrayList" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="com.qanda.trends.ControlChartExample" %>
<% 
	String callbackName = request.getParameter("callback");
	response.setCharacterEncoding("UTF-8");
	response.setContentType("application/x-javascript;charset=UTF-8");
	String infilename = request.getSession().getServletContext().getRealPath("/")+"/"+request.getParameter("filename");	
	ControlChartExample tce = new ControlChartExample();		
	JSONArray result = tce.GenerateDataTrend(infilename);
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