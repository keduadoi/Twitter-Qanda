<%@ page import="java.io.*" %>
<%@ page import="java.util.*"%>
<%@ page import="java.lang.Math" %>
<%@ page import="java.util.Date" %>
<%@ page import="org.json.*" %>
<%@ page import="utils.*" %>
<%@ page import="javax.servlet.http.Cookie" %>
<%@ page import="Chapter4.util.TweetFileToGraph" %>
<%@ page import="Chapter4.GraphElements.RetweetEdge"%>
<%@ page import="Chapter4.GraphElements.UserNode" %>
<%@ page import="edu.uci.ics.jung.graph.DirectedGraph" %>
<%@ page import="edu.uci.ics.jung.algorithms.scoring.EigenvectorCentrality" %>
<%@ page import="edu.uci.ics.jung.algorithms.importance.BetweennessCentrality" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.text.Format" %>
<%@ page import="edu.uci.ics.jung.algorithms.importance.Ranking" %>
<%	
	response.setCharacterEncoding("UTF-8");
	response.setContentType("application/x-javascript;charset=UTF-8");
	int influenceType = Integer.parseInt(request.getParameter("influenceType"));
	
	Cookie[] cookies = request.getCookies();
    String dbName = "qanda";
    for(Cookie cookie : cookies){
    	if(cookie.getName().equals("currentDb")){
    		dbName = cookie.getValue();
    		break;
    	}
    }
    
    DirectedGraph<UserNode, RetweetEdge> retweetGraph = TweetFileToGraph.getRetweetNetworkFromDatabase(dbName);
    JSONArray nodesArray = new JSONArray();
    ArrayList<JSONObject> nodesArrayList = null;
    
	switch(influenceType){
	case 0:
		//calculate the betweenness centrality
		//Create ArrayList<JsonObject>
		nodesArrayList = new ArrayList<JSONObject>();
		
		for(UserNode node : retweetGraph.getVertices()){
			JSONObject nodeObj = new JSONObject();
			nodeObj.put("name", node.toString().replaceAll("\"",""));
			nodeObj.put("value", retweetGraph.getInEdges(node).size());
			nodesArrayList.add(nodeObj);
		}
		break;
		
	case 1:
		EigenvectorCentrality<UserNode, RetweetEdge> eig = new EigenvectorCentrality<UserNode, RetweetEdge>(retweetGraph);
        eig.acceptDisconnectedGraph(true);
		eig.evaluate();

		//Create ArrayList<JsonObject>
		nodesArrayList = new ArrayList<JSONObject>();
		for(UserNode node : retweetGraph.getVertices()){
 			JSONObject nodeObj = new JSONObject();
 			nodeObj.put("name", node.toString().replaceAll("\"",""));
 			nodeObj.put("value", eig.getVertexScore(node));
			nodesArrayList.add(nodeObj);
		}
		break;
		
	case 2:
		BetweennessCentrality<UserNode, RetweetEdge> betweenness = new BetweennessCentrality<UserNode, RetweetEdge>(retweetGraph);		
		betweenness.evaluate();
		
		//Create ArrayList<JsonObject>
		nodesArrayList = new ArrayList<JSONObject>();
		
		double total = 0;
        Format formatter = new DecimalFormat("#0.#######");
        int rank = 1;

        for (Ranking<?> currentRanking : betweenness.getRankings()) {
            double rankScore = currentRanking.rankScore;
            JSONObject rankObj = new JSONObject();
            rankObj.put("rank", rank);
            rankObj.put("value", formatter.format(rankScore).toString().replaceAll("\"",""));
            rankObj.put("vertex_id", currentRanking.originalPos);
            rankObj.put("name", currentRanking.getRanked().toString().replaceAll("\"",""));
            nodesArrayList.add(rankObj);
            total += rankScore;
            rank++;
        }
		break;
		
	default:
		break;
		
	}
	
	//sort the array result
	Collections.sort(nodesArrayList, new Comparator<JSONObject>(){
		public int compare(JSONObject o1, JSONObject o2){
			double o1Value = 0.0;
			double o2Value = 0.0;
			try{
				o1Value = Double.parseDouble(o1.getString("value"));
				o2Value = Double.parseDouble(o2.getString("value"));
			}catch(JSONException e){
				e.printStackTrace();
			}
			
			if(o1Value < o2Value)
				return 1;
			else if(o1Value > o2Value)
				return -1;
			else
				return 0;
		}
	});
	
	//add nodes to JSONArray
	for(int i=0;i<nodesArrayList.size();++i){
		nodesArray.put(nodesArrayList.get(i));
	}
	
	JSONObject nodes = new JSONObject();
	nodes.put("nodes", nodesArray);
	out.write(nodes.toString());
	out.flush();
	//conn.close();
	
%>