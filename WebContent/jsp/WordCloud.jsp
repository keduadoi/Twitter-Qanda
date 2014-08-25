<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONException" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="Chapter5.text.ExtractTopKeywords" %>
<%@ page import="utils.TextUtils" %>
<%
	String callbackName = request.getParameter("callback");
	response.setCharacterEncoding("UTF-8");
	response.setContentType("application/x-javascript;charset=UTF-8");
	String infilename = request.getSession().getServletContext().getRealPath("/")+"/"+request.getParameter("filename");
	int k = Integer.parseInt(request.getParameter("k").toString());
	ExtractTopKeywords etk = new ExtractTopKeywords();
	TextUtils tu = new TextUtils();
	tu.LoadStopWords(request.getSession().getServletContext().getRealPath("/")+"\\"+"stopwords.txt");	
	if(k==0||k==-1)
	{
		k = 40;
	}
	JSONArray result = etk.GetTopKeywords(infilename, k, true,true,tu);
	System.out.println(result.toString());
	for(int i=0;i<result.length();i++)
	{
		JSONObject tempobj = result.getJSONObject(i);
		tempobj.put("size",tempobj.getInt("size")/240);
		result.put(i,tempobj);
	}
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