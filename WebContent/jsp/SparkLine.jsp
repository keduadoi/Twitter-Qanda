<%@ page import="org.json.JSONObject" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="Chapter5.trends.SparkLineExample" %>
<%
	String callbackName = request.getParameter("callback");
	response.setCharacterEncoding("UTF-8");
	response.setContentType("application/x-javascript;charset=UTF-8");
	String infilename = request.getSession().getServletContext().getRealPath("/")+"/"+request.getParameter("filename");	
	String wordsstring = request.getParameter("words");
	ArrayList<String> words = new ArrayList<String>();
	if(wordsstring!=null)
	{
		String[] splitwords = wordsstring.split(",");
		for(int i=0;i<splitwords.length;i++)
		{
			words.add(splitwords[i]);			
		}
	}	
	SparkLineExample sle = new SparkLineExample();		
	JSONObject result = sle.GenerateDataTrend(infilename,words);
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