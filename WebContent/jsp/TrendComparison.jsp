<%@ page import="java.util.ArrayList" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="Chapter5.trends.TrendComparisonExample" %>
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
	if(words.isEmpty())
	{
		//words.add("");
		words.add("#nypd");
		words.add("protest");
	}	
	TrendComparisonExample tce = new TrendComparisonExample();		
	JSONArray result = tce.GenerateDataTrend(infilename,words);
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