package Chapter4.util;

import java.io.File;

import Chapter4.GraphElements.RetweetEdge;
import Chapter4.GraphElements.UserNode;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.lightcouch.CouchDbClient;

import com.google.gson.JsonObject;

/**
 *	Some basic functionality to convert files collected 
 *	in Chapter 2 to JUNG graphs.
 */
public class TweetFileToGraph {
	
	private static CouchDbClient dbClient;
	
	public static DirectedGraph<UserNode, RetweetEdge> getRetweetNetwork(File tweetFile){
		
		JSONObject tmp;
		
		TweetFileProcessor tfp = new TweetFileProcessor(tweetFile);
		DirectedSparseGraph<UserNode, RetweetEdge> dsg = new DirectedSparseGraph<UserNode, RetweetEdge>();
		
		while (tfp.hasNext()){
			tmp = tfp.next();
                        if(tmp==null)
                        {
                            continue;
                        }
			//get the author
			String user=null;
                        try {
                            user = tmp.getJSONObject("user").getString("screen_name");
                        } catch (JSONException ex) {
                            Logger.getLogger(TweetFileToGraph.class.getName()).log(Level.SEVERE, null, ex);
                        }
			if(user==null)
                        {
                            continue;
                        }
			//get the retweeted user
			try{
				JSONObject retweet = tmp.getJSONObject("retweeted_status");
				String retweeted_user = retweet.getJSONObject("user").getString("screen_name");
				
				//make an edge or increment the weight if it exists.
				UserNode toUser = new UserNode(retweeted_user);
				UserNode fromUser = new UserNode(user);
				
				dsg.addVertex(toUser);
				dsg.addVertex(fromUser);
			
				RetweetEdge edge = new RetweetEdge(toUser, fromUser); 
				
				if(dsg.containsEdge(edge)){
					dsg.findEdge(fromUser, toUser).incrementRTCount();
				}
				else{
					dsg.addEdge(edge, fromUser, toUser);
				}
				dsg.addEdge(edge, fromUser, toUser, EdgeType.DIRECTED);
			}
			catch(JSONException ex){
				//the tweet is not a retweet. this is not a problem.
			}
			
			
		}
		
		return dsg;
	}
	
	
public static DirectedGraph<UserNode, RetweetEdge> getRetweetNetworkFromDatabase(String dbName){
		
		dbClient = new CouchDbClient(dbName, true, "http", "localhost", 5984, null, null);
		List<JsonObject> allTweets = dbClient.view("_all_docs").includeDocs(true).limit(5001).startKeyDocId(null).query(JsonObject.class);
		DirectedSparseGraph<UserNode, RetweetEdge> dsg = new DirectedSparseGraph<UserNode, RetweetEdge>();
		
		while(allTweets.size()>1)
		{ 
    		String lastDocId = "";
    		for(int i=0;i<allTweets.size()-2;++i){
	    		JsonObject tmp = allTweets.get(i);
	    		lastDocId = tmp.get("id_str").toString().replaceAll("\"", "");
				//get the author
				String user=null;
	                        user = tmp.getAsJsonObject("user").get("screen_name").toString();
				if(user==null)
	                        {
	                            continue;
	                        }
				JsonObject retweet = tmp.getAsJsonObject("retweeted_status");
				if(retweet==null){
					continue;
				}
				String retweeted_user = retweet.getAsJsonObject("user").get("screen_name").toString();
				
				//make an edge or increment the weight if it exists.
				UserNode toUser = new UserNode(retweeted_user);
				UserNode fromUser = new UserNode(user);
				
				dsg.addVertex(toUser);
				dsg.addVertex(fromUser);
	
				RetweetEdge edge = new RetweetEdge(toUser, fromUser); 
				
				if(dsg.containsEdge(edge)){
					dsg.findEdge(fromUser, toUser).incrementRTCount();
				}
				else{
					dsg.addEdge(edge, fromUser, toUser);
				}
				dsg.addEdge(edge, fromUser, toUser, EdgeType.DIRECTED);
			
    		}
    		lastDocId = allTweets.get(allTweets.size()-1).get("id_str").toString().replace("\"", "");
    		allTweets = dbClient.view("_all_docs").includeDocs(true).limit(5001).startKeyDocId(lastDocId).query(JsonObject.class);
		}
		
		//last document
		for(int i=0;i<allTweets.size()-1;++i){
    		JsonObject tmp = allTweets.get(i);
			//get the author
			String user=null;
                        user = tmp.getAsJsonObject("user").get("screen_name").toString();
			if(user==null)
                        {
                            continue;
                        }
			JsonObject retweet = tmp.getAsJsonObject("retweeted_status");
			if(retweet==null){
				continue;
			}
			String retweeted_user = retweet.getAsJsonObject("user").get("screen_name").toString();
			
			//make an edge or increment the weight if it exists.
			UserNode toUser = new UserNode(retweeted_user);
			UserNode fromUser = new UserNode(user);
			
			dsg.addVertex(toUser);
			dsg.addVertex(fromUser);

			RetweetEdge edge = new RetweetEdge(toUser, fromUser); 
			
			if(dsg.containsEdge(edge)){
				dsg.findEdge(fromUser, toUser).incrementRTCount();
			}
			else{
				dsg.addEdge(edge, fromUser, toUser);
			}
			dsg.addEdge(edge, fromUser, toUser, EdgeType.DIRECTED);
		
		}
		
		return dsg;
	}
}
