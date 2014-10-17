package com.couchdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lightcouch.CouchDbClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.qanda.network.CreateD3Network;

public class CreateMutualInformationNetwork {
	
	private CouchDbClient dbDiscussionClient;
	
	public HashMap<String,ArrayList<String>> CreateJoinedDiscussionMap(String dbName){
		dbDiscussionClient = new CouchDbClient(dbName, true, "http", "localhost", 5984, null, null);
		List<JsonObject> allDiscussions = dbDiscussionClient.view("_all_docs").includeDocs(true).query(JsonObject.class);
		HashMap<String,ArrayList<String>> discussionMap = new HashMap<String, ArrayList<String>>();
		
		for(JsonObject discussion : allDiscussions){
			JsonArray users = discussion.getAsJsonArray("users");
			String discussionId = discussion.get("_id").toString();
			for(int i=0;i<users.size();++i){
				JsonObject user = users.get(i).getAsJsonObject();
				String screen_name = user.get("screen_name").toString().replaceAll("\"", "");
				if(!discussionMap.containsKey(screen_name)){				
					ArrayList<String> joinDiscussions = new ArrayList<String>();
					joinDiscussions.add(discussionId);
					discussionMap.put(screen_name, joinDiscussions);
				}else{
					ArrayList<String> joinDiscussions = discussionMap.get(screen_name);
					if(!joinDiscussions.contains(discussionId)){
						joinDiscussions.add(discussionId);
					}
					discussionMap.put(screen_name, joinDiscussions);
				}
			}
		}
		
		//System.out.println(discussionMap.toString());
		
		dbDiscussionClient.shutdown();
		
		return discussionMap;
	}
	
	public HashMap<String,Double> CalculateMarginalPropability(String dbName, HashMap<String,ArrayList<String>> discussionMap){
		dbDiscussionClient = new CouchDbClient(dbName, true, "http", "localhost", 5984, null, null);
		List<JsonObject> allDiscussions = dbDiscussionClient.view("_all_docs").includeDocs(false).query(JsonObject.class);
		HashMap<String,Double> marginalMap = new HashMap<String, Double>();
		
		for(String screen_name : discussionMap.keySet()){
			ArrayList<String> joinDiscussions = discussionMap.get(screen_name);
			double marginalPr = joinDiscussions.size()*1.0/allDiscussions.size();
			marginalMap.put(screen_name,marginalPr);
		}
		
		dbDiscussionClient.shutdown();
		//System.out.println(marginalMap.toString());
		
		return marginalMap;
	}
	
	public ArrayList<JsonObject> CreateMutualInformationList(String dbName, double threshold){
		dbDiscussionClient = new CouchDbClient(dbName, true, "http", "localhost", 5984, null, null);
		int numOfDiscussions = dbDiscussionClient.view("_all_docs").includeDocs(false).query(JsonObject.class).size();
		dbDiscussionClient.shutdown();
		
		HashMap<String,ArrayList<String>> discussionMap = CreateJoinedDiscussionMap(dbName);
		HashMap<String,Double> marginalMap = CalculateMarginalPropability(dbName, discussionMap);
		
		ArrayList<JsonObject> mutualInformation = new ArrayList<JsonObject>();
		for(String firstUsername : discussionMap.keySet()){
			ArrayList<String> joinDiscussions1 = discussionMap.get(firstUsername);
			for(String secondUsername : discussionMap.keySet()){
				int numCommonDiscussion = 0;
				if(!firstUsername.equals(secondUsername)){
					ArrayList<String> joinDiscussions2 = discussionMap.get(secondUsername);
					for(int i=0;i<joinDiscussions1.size();++i){
						if(joinDiscussions2.contains(joinDiscussions1.get(i))){
							numCommonDiscussion++;
						}
					}
				}
				else{
					continue;
				}
				
				//check if MI of these two calculated
				boolean miCalculated = false;
				for(int i=0;i<mutualInformation.size();++i){
					JsonObject miJson = mutualInformation.get(i);
					ArrayList<String> twoUsers = new ArrayList<String>();
					twoUsers.add(miJson.get("firstUser").toString());
					twoUsers.add(miJson.get("secondUser").toString());
					if(twoUsers.contains(firstUsername) && twoUsers.contains(secondUsername)){
						miCalculated = true;
						break;
					}
				}				
				
				
				if(miCalculated == false){
					double joinPr = numCommonDiscussion*1.0/numOfDiscussions;
					double marginal1 = marginalMap.get(firstUsername);
					double marginal2 = marginalMap.get(secondUsername);
					double mi = joinPr * (Math.log(joinPr/(marginal1*marginal2)));
					
					if(mi >= threshold){
						JsonObject newPair = new JsonObject();
						newPair.addProperty("firstUser", firstUsername);
						newPair.addProperty("secondUser", secondUsername);
						newPair.addProperty("mi", mi);
						mutualInformation.add(newPair);
					}
				}
			}
		}
		
		for(JsonObject pair : mutualInformation){
			System.out.println(pair.get("firstUser").toString()+"&&"+pair.get("secondUser").toString()+": "+pair.get("mi").toString());
		}
		
		return mutualInformation;
	}
	
	public JSONObject GetD3StructureMI(ArrayList<JsonObject> finalnodes)
    {
        JSONObject allUsers = new JSONObject();
        try {
            JSONArray nodes = new JSONArray();
            JSONArray links = new JSONArray();
            for (JsonObject node : finalnodes)
            {
                try {
                    //create adjacencies
                    JSONArray nodedata = new JSONArray();
                    JSONObject jsadj = new JSONObject();
                    jsadj.put("source", node.get("firstUser").toString().replaceAll("\"", ""));
                    jsadj.put("target", node.get("secondUser").toString().replaceAll("\"", ""));
                    //weight of the edge
                    jsadj.put("value", Double.parseDouble(node.get("mi").toString()));
                    links.put(jsadj);
                        
                    //add nodes
//                    String firstUser = node.get("firstUser").toString().replaceAll("\"", "");
//                    boolean firstNodeAdded = false;
//                    for(int i=0;i<nodes.length();++i){
//                    	JSONObject user = nodes.getJSONObject(i);
//                    	if(user.get("name").toString().equals(firstUser)){
//                    		firstNodeAdded = true;
//                    		break;
//                    	}
//                    }
//                    if(firstNodeAdded==false){
//                    	JSONObject nd = new JSONObject();
//                    	nd.put("name", firstUser);
//                    	nodes.put(nd);
//                    }
//                    
//                    String secondUser = node.get("secondUser").toString().replaceAll("\"", "");
//                    boolean secondNodeAdded = false;
//                    for(int i=0;i<nodes.length();++i){
//                    	JSONObject user = nodes.getJSONObject(i);
//                    	if(user.get("name").toString().equals(secondUser)){
//                    		secondNodeAdded = true;
//                    		break;
//                    	}
//                    }
//                    if(secondNodeAdded==false){
//                    	JSONObject nd = new JSONObject();
//                    	nd.put("name", secondUser);
//                    	nodes.put(nd);
//                    }
                    
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
            //allUsers.put("nodes", nodes);
            allUsers.put("links", links);

        } catch (JSONException ex) {
            Logger.getLogger(CreateD3Network.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return allUsers;
    }

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CreateMutualInformationNetwork miNetwork = new CreateMutualInformationNetwork();
		ArrayList<JsonObject> miList = miNetwork.CreateMutualInformationList("temp_discussions", 0);
		for(JsonObject pair : miList){
			System.out.println(pair.get("firstUser").toString()+"&&"+pair.get("secondUser").toString()+": "+pair.get("mi").toString());
		}
		JSONObject network = miNetwork.GetD3StructureMI(miList);
		System.out.println(network.toString());
	}

}
