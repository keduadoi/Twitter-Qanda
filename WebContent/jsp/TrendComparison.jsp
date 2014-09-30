<%@ page import="java.util.ArrayList" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="com.qanda.trends.TrendComparisonExample" %>
<%@ page import="javax.servlet.http.Cookie" %>
<% 
	String callbackName = request.getParameter("callback");
	response.setCharacterEncoding("UTF-8");
	response.setContentType("application/x-javascript;charset=UTF-8");
	//String infilename = request.getSession().getServletContext().getRealPath("/")+"/"+request.getParameter("filename");
	Cookie[] cookies = request.getCookies();
    String dbName = "qanda";
    for(Cookie cookie : cookies){
    	if(cookie.getName().equals("currentDb")){
    		dbName = cookie.getValue();
    		break;
    	}
    }
	String wordsstring = request.getParameter("keywords");
	ArrayList<String> words = new ArrayList<String>();
	if(wordsstring!=null)
	{
		String[] splitwords = wordsstring.split(";");
		for(int i=0;i<splitwords.length;i++)
		{
			words.add(splitwords[i]);			
		}
	}
	if(words.isEmpty())
	{
		//words.add("");
		words.add("#qanda");
		words.add("#auspol");
	}	
	
	String from = "01/01/2011";
	String to = "12/31/2014";
	try{	
		from = request.getParameter("from").toString();
		to = request.getParameter("to").toString();
	}catch(Exception ex)
	{
		ex.printStackTrace();
	}
	
	TrendComparisonExample tce = new TrendComparisonExample();		
	JSONArray result = tce.GenerateDataTrend(dbName,words,from,to);
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