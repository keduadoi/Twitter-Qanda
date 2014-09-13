<%@ page import="org.json.JSONArray" %>
<%@ page import="com.qanda.trends.ExtractDatasetTrend"%>

<%
	String callbackName = request.getParameter("callback");
	response.setCharacterEncoding("UTF-8");
	response.setContentType("application/x-javascript;charset=UTF-8");
	String infilename = request.getSession().getServletContext().getRealPath("/")+"/"+request.getParameter("filename");	
	ExtractDatasetTrend edt = new ExtractDatasetTrend();
	JSONArray result = edt.GenerateDataTrend(infilename);
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