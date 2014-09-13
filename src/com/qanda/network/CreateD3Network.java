/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.qanda.network;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lightcouch.CouchDbClient;
import com.google.gson.JsonObject;
import com.qanda.support.HashTagDS;
import com.qanda.support.NetworkNode;
import com.qanda.support.NodeIDComparator;
import com.qanda.support.NodeSizeComparator;
import com.qanda.support.ToNodeInfo;
import com.qanda.support.Tweet;

import utils.TextUtils;

/**
 *
 * @author shamanth
 */
public class CreateD3Network
{
	//private CouchDbClient dbClient = new CouchDbClient("couchdb.properties");
	private CouchDbClient dbClient;
    //static final String DEF_INFILENAME = "tweets_single_lines.dat";
    private String RTPATTERN = "rt @[_a-zA-Z0-9]+";
    private final int DEFAULT_NODE_SIZE = 0;
    private JSONArray rawTweets; 
//    private final int NODE_COUNT_LIMIT = 1;
    //private final String[] node_color_scheme = new String[]{"#FFFFD9","#EDF8B1","#C7E9B4","#7FCDBB","#41B6C4","#1D91C0","#225EA8","#253494","#081D58"};
    //private final String[] node_color_scheme = new String[]{"#A6BDDB","#74A9CF","#3690C0","#0570B0","#045A8D","#023858"};

    /**
     * Extracts the users who have been retweeted using the RTPATTERN
     * @param text
     * @return
     */
    public ArrayList<String> GetRTUsers(String text)
    {
        Pattern p = Pattern.compile(RTPATTERN, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        ArrayList<String> rtusers = new ArrayList<String>();
        while(m.find())
        {
            String nuser = text.substring(m.start(),m.end());
            nuser = nuser.replaceAll("rt @|RT @", "");
//            nuser = nuser.replaceAll("RT @", "");
            rtusers.add(nuser.toLowerCase());
        }
        return rtusers;
    }

    /**
     * Identifies the category to which the tweet belongs. Each category is defined by a group of words/hashtags
     * @param tweet
     * @param usercategories
     * @return
     */
    public int GetCategory(String tweet, HashTagDS[] usercategories)
    {
        HashMap<Integer,Integer> categoryvotes = new HashMap<Integer,Integer>();
        tweet = tweet.toLowerCase();
        int i=0;
        for(HashTagDS cat:usercategories)
        {
             
             for(String s :cat.tags)
             {
                 if(tweet.indexOf(s)!=-1)
                 {
                    if(categoryvotes.containsKey(i))
                    {
                        categoryvotes.put(i, categoryvotes.get(i)+1);
                    }
                    else
                    {
                        categoryvotes.put(i, 1);
                    }
                 }
             }
             i++;
        }
        Set<Integer> keyset = categoryvotes.keySet();
        int maxvote = 0;
        //by default the tweet will be in the first category
        int maxcategoryindex = 0;
        for(int key:keyset)
        {
           if(categoryvotes.get(key)>maxvote)
            {
                maxvote = categoryvotes.get(key);
                maxcategoryindex = key;
            }
        }
        return maxcategoryindex;
    }

    /**
     * Converts the input jsonobject containing category descriptions to an array for processing.
     * @param hashtagcoll JSONObject containing the list of hashtags, color, and the topic information
     * @return An array of hashtags
     */
    public HashTagDS[] ConvertJSONArrayToArray(JSONObject hashtagcoll)
    {
        HashTagDS[] hashtags = new HashTagDS[hashtagcoll.length()];
        int j=0;
        try{
            if(hashtagcoll!=null)
            {
                Iterator keyit = hashtagcoll.keys();
                while(keyit.hasNext())
                {
                   HashTagDS ht = new HashTagDS();
                   JSONObject tags = (JSONObject) hashtagcoll.get((String)keyit.next());
                   ht.groupname = keyit.toString();                   
                   ht.color = tags.getString("color");
                   JSONArray tagjson = tags.getJSONArray("hts");
                   ht.tags = new String[tagjson.length()];
                   for(int i=0;i<tagjson.length();i++)
                   {
                        ht.tags[i] = tagjson.getString(i);
                   }
                   hashtags[j++] = ht;
                }
            }
        }catch(JSONException ex)
        {
            ex.printStackTrace();
        }
        return hashtags;
    }

    /**
     * Identifies the category of a node based on the content of his tweets(each tweet can be assigned a category based on it's text). A simple majority is sufficient to make this decision.
     * @param tnfs
     * @param hashtagarray
     * @return
     */
    public int GetMajorityTopicColor(NetworkNode tnfs,HashTagDS[] hashtagarray)
    {
        HashMap<Integer,Integer> catcount = new HashMap<Integer,Integer>();
         //if the node has no tolinks then look at the node that it retweeted to decide the color of the node
        for(String tweet:tnfs.data)
        {
            int id = this.GetCategory(tweet, hashtagarray);
            if(catcount.containsKey(id))
            {
                catcount.put(id, catcount.get(id)+1);
            }
            else
                catcount.put(id, 1);
        }
        Set<Integer> keys = catcount.keySet();
        int maxcatID = -1;
        int maxcount = 0;
        for(int k:keys)
        {
           if(maxcatID==-1)
           {
               maxcatID = k;
               maxcount = catcount.get(k);
           }
           else
           {
               if(maxcount<catcount.get(k))
               {
                   maxcount = catcount.get(k);
                   maxcatID = k;
               }
           }
        }
        return maxcatID;
    }

    /**
     * Takes as input a JSON file and reads through the file sequentially to process and create a retweet network from the tweets.
     * @param inFilename
     * @param numNodeClasses
     * @param hashtags  category info containing hashtags
     * @param num_nodes number of seed nodes to be included in the network
     * @return a JSONObject consisting of nodes and links of the network
     */
    public JSONObject ConvertTweetsToDiffusionPath(int numNodeClasses,
            JSONObject hashtags, int num_nodes, String from, String to, String dbName)
    {
    	//fetch all_docs couchdb
        HashMap<String,NetworkNode> userconnections = new HashMap<String,NetworkNode>();
//        HashMap<String,Integer> tweet_class_codes = new HashMap<String,Integer>();
//        int tweet_class_counter = 1;
        dbClient = new CouchDbClient(dbName, false, "http", "localhost", 5984, null, null);
        rawTweets = new JSONArray();
        HashTagDS[] hashtagarray = ConvertJSONArrayToArray(hashtags);
    	List<JsonObject> allTweets = dbClient.view("_all_docs").includeDocs(true).limit(5001).startKeyDocId(null).query(JsonObject.class);
    	while(allTweets.size()>1)
		{ 
    		String lastDocId = "";
    		for(int k=0;k<allTweets.size()-2;++k){
    			JsonObject tweetJson = allTweets.get(k);
        		lastDocId = tweetJson.get("id_str").toString().replaceAll("\"", "");
			    Date createdDate = new Date();
			    Date fromDate = new Date();
			    Date toDate = new Date();
			    try {
			    	String createdStr = tweetJson.get("created_at").toString();
			        String[] createdArr = createdStr.split("\\s+");
			        
			        //convert month in name to number
			        Date monthInName = new SimpleDateFormat("MMM",Locale.ENGLISH).parse(createdArr[1]);
			    	Calendar cal = Calendar.getInstance();
			    	cal.setTime(monthInName);
			    	int monthInNumber = cal.get(Calendar.MONTH);
			    	
			        String createdInFormat = monthInNumber+"/"+createdArr[2]+"/"+createdArr[createdArr.length-1];
			        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

			        createdDate = dateFormat.parse(createdInFormat);                   
			        fromDate = dateFormat.parse(from);
			        toDate = dateFormat.parse(to);
			    }  catch(Exception e){
			        e.printStackTrace();
			        continue;
			    } 
			    
			    //Check if the tweet is on the selected period
			    if(createdDate.before(fromDate) || createdDate.after(toDate)){
			    	//System.out.println("False: Created: "+createdDate.toString()+" From: "+fromDate.toString()+" To: "+toDate.toString());
			    	continue;
			    }
			    //System.out.println("True: Created: "+createdDate.toString()+" From: "+fromDate.toString()+" To: "+toDate.toString());
			    
			    
			    //Extract the tweet first
			    Tweet t = new Tweet();
			    String text="";
			    text = TextUtils.GetCleanText(tweetJson.get("text").toString()).toLowerCase();
			    //Check that the tweet matches at least one of the topics
			    boolean groupmatch = false;
			    for(HashTagDS ht:hashtagarray)
			    {
			        String[] tags = ht.tags;
			        for(String tg:tags)
			        {
			            if(text.contains(tg))
			            {
			                groupmatch = true;
			                break;
			            }
			        }
			        if(groupmatch)
			        {
			            break;
			        }
			    }
			    if(!groupmatch)
			    {
			        continue;
			    }
			   
			    //
			    ArrayList<String> fromusers = new ArrayList<String>();
			    if(tweetJson.get("retweeted_status")!=null)
			    {
			    	JsonObject rtstatus;
			        rtstatus = tweetJson.getAsJsonObject("retweeted_status");
					if(rtstatus.get("user")==null)
					{
						JsonObject rtuserobj = rtstatus.getAsJsonObject("user");
					    fromusers.add(rtuserobj.get("screen_name").toString());
					}
			    }
			    else
			    {
			        //use the tweet text to retrieve the pattern "RT @username:"
			        fromusers = GetRTUsers(text);
			    }
			    if(fromusers.isEmpty())
			    {
			        continue;
			    }
			    
			    //identify the class values to be applied to all the nodes and
			    //edges.
//                    String prunedtext = TextUtils.RemoveTwitterElements(text);
//                    Integer class_code = tweet_class_codes.get(prunedtext);
//                    if(class_code==null)
//                    {
//                        class_code = tweet_class_counter;
//                        tweet_class_codes.put(prunedtext, class_code);   //set the unique id for this tweet
//                        tweet_class_counter++;
//                    }
			    t.text = TextUtils.RemoveRTElements(text);
			    if(tweetJson.get("user")!=null)
			    {
			    	JsonObject userobj = null;
					userobj = tweetJson.getAsJsonObject("user");
					t.user = userobj.get("screen_name").toString().toLowerCase();                        
			    }
//                    try {
//                        t.pubdate = String.valueOf(tweetobj.get("timestamp"));
//                    } catch (JSONException ex) {
//                        Logger.getLogger(CreateD3Network.class.getName()).log(Level.SEVERE, null, ex);
//                    }
			    t.catColor = hashtagarray[t.catID].color;
			    //update the size of the from fromuser
			    int cur_level = 0;
			    for(int i=fromusers.size()-1;i>=0;i--)
			    {
			        String touser = "";
			        if(i==0)
			        {//if this is the last user in the retweet sequence then use the user of the tweet as the next link
			            touser = t.user;
			        }
			        else
			        {   //if there are still fromuser in the retweet chain then use them as the next link
			            touser = fromusers.get(i-1);
			        }
			        //don't add any selflinks
			        if(fromusers.get(i).equals(touser))
			        {
			            continue;
			        }
			        NetworkNode fromuser = null;
			        if(userconnections.containsKey(fromusers.get(i)))
			        {
			            //from node already exists simply add this new connection to it
			            fromuser  = userconnections.get(fromusers.get(i));
			        }
			        else
			        {
			             //the from user was not found. add the node
			            fromuser = new NetworkNode();
			           // fromuser.id = nodeid++;
			            fromuser.username = fromusers.get(i);
			            fromuser.tonodes = new ArrayList<ToNodeInfo>();
			            fromuser.class_codes = new ArrayList<Integer>();
			            fromuser.size = DEFAULT_NODE_SIZE;
			            fromuser.level = cur_level;
			            fromuser.data = new ArrayList<String>();
			            fromuser.data.add(t.text);
			            //fromuser.category = ;
			        }
//                        if(!fromuser.class_codes.contains(class_code))
//                        {
//                            //add the marker to from node if it does not have it already
//                            fromuser.class_codes.add(class_code);
//                        }
			        //if to node is not in the list then create it
			        NetworkNode tonode = null;
			        if(!userconnections.containsKey(touser))
			        {
			            tonode = new NetworkNode();
      //                    System.out.println(touser+" "+nodeid);
			          //  tonode.id= nodeid++;
			            tonode.username = touser;
			            tonode.tonodes= new ArrayList<ToNodeInfo>();
			            tonode.class_codes = new ArrayList<Integer>();
			            tonode.catID = t.catID;
			            tonode.catColor = t.catColor;
			            tonode.size = DEFAULT_NODE_SIZE;
			            tonode.data= new ArrayList<String>();
			            tonode.data.add(t.text);
			            tonode.level = cur_level+1;
			            //add the classcode to the node if it doesn't already exist
//                            if(!tonode.class_codes.contains(class_code))
//                            {
//                                tonode.class_codes.add(class_code);
//                            }
			            //add the touser info
			            userconnections.put(touser, tonode);
			        }
			        else
			        {
			            tonode = userconnections.get(touser);
			            tonode.data.add(t.text);
			            if(tonode.level<cur_level+1)
			            {
			                tonode.level = cur_level;
			            }
			            //add the classcode to the node if it doesn't already exist
//                            if(!tonode.class_codes.contains(class_code))
//                            {
//                                tonode.class_codes.add(class_code);
//                            }
			        }
			        ToNodeInfo inf = new ToNodeInfo();
			        inf.tonodeid = tonode.id;
			        inf.text = t.text;
//                        inf.date = t.pubdate;
//                        inf.class_code = class_code;
			        inf.tousername = touser;
			        inf.catID = t.catID;
			        inf.catColor = t.catColor;
			        fromuser.tonodes.add(inf);
			        //update from node size
			        fromuser.size++;
			        //add back updated fromuser
			        userconnections.put(fromusers.get(i), fromuser);
			        //update the level for next iteration
			        cur_level++;
			    }
			    rawTweets.put(tweetJson);
		}
    		lastDocId = allTweets.get(allTweets.size()-1).get("id_str").toString().replace("\"", "");
    		allTweets = dbClient.view("_all_docs").includeDocs(true).limit(5001).startKeyDocId(lastDocId).query(JsonObject.class);
		}
        	
        	//last document
    		for(int k=0;k<allTweets.size()-1;++k){
    			JsonObject tweetJson = allTweets.get(k);
			    Date createdDate = new Date();
			    Date fromDate = new Date();
			    Date toDate = new Date();
			    try {
			    	String createdStr = tweetJson.get("created_at").toString();
			        String[] createdArr = createdStr.split("\\s+");
			        
			        //convert month in name to number
			        Date monthInName = new SimpleDateFormat("MMM",Locale.ENGLISH).parse(createdArr[1]);
			    	Calendar cal = Calendar.getInstance();
			    	cal.setTime(monthInName);
			    	int monthInNumber = cal.get(Calendar.MONTH);
			    	
			        String createdInFormat = monthInNumber+"/"+createdArr[2]+"/"+createdArr[createdArr.length-1];
			        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

			        createdDate = dateFormat.parse(createdInFormat);                   
			        fromDate = dateFormat.parse(from);
			        toDate = dateFormat.parse(to);
			    }  catch(Exception e){
			        e.printStackTrace();
			        continue;
			    } 
			    
			    //Check if the tweet is on the selected period
			    if(createdDate.before(fromDate) || createdDate.after(toDate)){
			    	//System.out.println("False: Created: "+createdDate.toString()+" From: "+fromDate.toString()+" To: "+toDate.toString());
			    	continue;
			    }
			    //System.out.println("True: Created: "+createdDate.toString()+" From: "+fromDate.toString()+" To: "+toDate.toString());
			    
			    
			    //Extract the tweet first
			    Tweet t = new Tweet();
			    String text="";
			    text = TextUtils.GetCleanText(tweetJson.get("text").toString()).toLowerCase();
			    //Check that the tweet matches at least one of the topics
			    boolean groupmatch = false;
			    for(HashTagDS ht:hashtagarray)
			    {
			        String[] tags = ht.tags;
			        for(String tg:tags)
			        {
			            if(text.contains(tg))
			            {
			                groupmatch = true;
			                break;
			            }
			        }
			        if(groupmatch)
			        {
			            break;
			        }
			    }
			    if(!groupmatch)
			    {
			        continue;
			    }
			   
			    //
			    ArrayList<String> fromusers = new ArrayList<String>();
			    if(tweetJson.get("retweeted_status")!=null)
			    {
			    	JsonObject rtstatus;
			        rtstatus = tweetJson.getAsJsonObject("retweeted_status");
					if(rtstatus.get("user")==null)
					{
						JsonObject rtuserobj = rtstatus.getAsJsonObject("user");
					    fromusers.add(rtuserobj.get("screen_name").toString());
					}
			    }
			    else
			    {
			        //use the tweet text to retrieve the pattern "RT @username:"
			        fromusers = GetRTUsers(text);
			    }
			    if(fromusers.isEmpty())
			    {
			        continue;
			    }
			    
			    //identify the class values to be applied to all the nodes and
			    //edges.
//                    String prunedtext = TextUtils.RemoveTwitterElements(text);
//                    Integer class_code = tweet_class_codes.get(prunedtext);
//                    if(class_code==null)
//                    {
//                        class_code = tweet_class_counter;
//                        tweet_class_codes.put(prunedtext, class_code);   //set the unique id for this tweet
//                        tweet_class_counter++;
//                    }
			    t.text = TextUtils.RemoveRTElements(text);
			    if(tweetJson.get("user")!=null)
			    {
			    	JsonObject userobj = null;
					userobj = tweetJson.getAsJsonObject("user");
					t.user = userobj.get("screen_name").toString().toLowerCase();                        
			    }
//                    try {
//                        t.pubdate = String.valueOf(tweetobj.get("timestamp"));
//                    } catch (JSONException ex) {
//                        Logger.getLogger(CreateD3Network.class.getName()).log(Level.SEVERE, null, ex);
//                    }
			    t.catColor = hashtagarray[t.catID].color;
			    //update the size of the from fromuser
			    int cur_level = 0;
			    for(int i=fromusers.size()-1;i>=0;i--)
			    {
			        String touser = "";
			        if(i==0)
			        {//if this is the last user in the retweet sequence then use the user of the tweet as the next link
			            touser = t.user;
			        }
			        else
			        {   //if there are still fromuser in the retweet chain then use them as the next link
			            touser = fromusers.get(i-1);
			        }
			        //don't add any selflinks
			        if(fromusers.get(i).equals(touser))
			        {
			            continue;
			        }
			        NetworkNode fromuser = null;
			        if(userconnections.containsKey(fromusers.get(i)))
			        {
			            //from node already exists simply add this new connection to it
			            fromuser  = userconnections.get(fromusers.get(i));
			        }
			        else
			        {
			             //the from user was not found. add the node
			            fromuser = new NetworkNode();
			           // fromuser.id = nodeid++;
			            fromuser.username = fromusers.get(i);
			            fromuser.tonodes = new ArrayList<ToNodeInfo>();
			            fromuser.class_codes = new ArrayList<Integer>();
			            fromuser.size = DEFAULT_NODE_SIZE;
			            fromuser.level = cur_level;
			            fromuser.data = new ArrayList<String>();
			            fromuser.data.add(t.text);
			            //fromuser.category = ;
			        }
//                        if(!fromuser.class_codes.contains(class_code))
//                        {
//                            //add the marker to from node if it does not have it already
//                            fromuser.class_codes.add(class_code);
//                        }
			        //if to node is not in the list then create it
			        NetworkNode tonode = null;
			        if(!userconnections.containsKey(touser))
			        {
			            tonode = new NetworkNode();
      //                    System.out.println(touser+" "+nodeid);
			          //  tonode.id= nodeid++;
			            tonode.username = touser;
			            tonode.tonodes= new ArrayList<ToNodeInfo>();
			            tonode.class_codes = new ArrayList<Integer>();
			            tonode.catID = t.catID;
			            tonode.catColor = t.catColor;
			            tonode.size = DEFAULT_NODE_SIZE;
			            tonode.data= new ArrayList<String>();
			            tonode.data.add(t.text);
			            tonode.level = cur_level+1;
			            //add the classcode to the node if it doesn't already exist
//                            if(!tonode.class_codes.contains(class_code))
//                            {
//                                tonode.class_codes.add(class_code);
//                            }
			            //add the touser info
			            userconnections.put(touser, tonode);
			        }
			        else
			        {
			            tonode = userconnections.get(touser);
			            tonode.data.add(t.text);
			            if(tonode.level<cur_level+1)
			            {
			                tonode.level = cur_level;
			            }
			            //add the classcode to the node if it doesn't already exist
//                            if(!tonode.class_codes.contains(class_code))
//                            {
//                                tonode.class_codes.add(class_code);
//                            }
			        }
			        ToNodeInfo inf = new ToNodeInfo();
			        inf.tonodeid = tonode.id;
			        inf.text = t.text;
//                        inf.date = t.pubdate;
//                        inf.class_code = class_code;
			        inf.tousername = touser;
			        inf.catID = t.catID;
			        inf.catColor = t.catColor;
			        fromuser.tonodes.add(inf);
			        //update from node size
			        fromuser.size++;
			        //add back updated fromuser
			        userconnections.put(fromusers.get(i), fromuser);
			        //update the level for next iteration
			        cur_level++;
			    }
			    rawTweets.put(tweetJson);
    		}

         Set<String> keys = userconnections.keySet();
         ArrayList<NetworkNode> returnnodes = new ArrayList<NetworkNode>();
         //its +1 because nodes with size 0 are not going to be used to calculate the class
         int min = DEFAULT_NODE_SIZE+1;
         int max = DEFAULT_NODE_SIZE+1;
         for(String k:keys)
         {
            NetworkNode n = userconnections.get(k);
            int maxcat = GetMajorityTopicColor(n,hashtagarray);
            n.catID = maxcat;
            n.catColor = hashtagarray[maxcat].color;
            userconnections.put(k, n);
            //
//            if(n.size==0)
//            {//mark the node as a zero node
//                n.class_codes.add(-1);
//            }
//            else
//            {
                if(n.size>max)
                {
                    max = n.size;
                }
                if(n.size<min)
                {
                    min = n.size;
                }
//            }
            returnnodes.add(n);
         }         
      //create node groups to assign unique colors to nodes in different Categories based upon the number of connections
      ArrayList<NetworkNode> nodes = ComputeGroupsSqrt(returnnodes, max, min, numNodeClasses);
      Collections.sort(nodes,Collections.reverseOrder(new NodeSizeComparator()));
      //select how many nodes to show.
      int nodes_to_visit = 0;
      if(nodes.size()>=num_nodes)
      {
        nodes_to_visit = num_nodes;
      }
      else
      {
          nodes_to_visit = nodes.size();
      }

      HashMap<String,NetworkNode> prunednodes = new HashMap<String,NetworkNode>();
      HashMap<String,Integer> nodeidlist = new HashMap<String,Integer>();
      int nodeid = 0; //node nodeid counter
      for(int k=0;k<nodes_to_visit;k++)
      {
          NetworkNode nd = nodes.get(k);
//          System.out.println("visiting node "+nd.username);
          nd.level = 0;
          HashMap<String,NetworkNode> rtnodes = GetNextHopConnections(userconnections,nd,new HashMap<String,NetworkNode>());
          Set<String> names = rtnodes.keySet();
          for(String n:names)
          {             
              if(!prunednodes.containsKey(n))
              {
                  NetworkNode newnode = rtnodes.get(n);
                  if(newnode.size>0)
                  {
                    prunednodes.put(n, newnode);
                    nodeidlist.put(n, nodeid++);
                  }
              }
          }
      }

   /** We now have all the nodes of the network. compute their ids sequentially
     * and assign them to the respective nodes. Simultaneously compact the nodes
     * of the network to remove all nodes which have not been retweeted and are
     * of size 0
     */
      
      Set<String> allnodes = prunednodes.keySet();
//      System.out.println(prunednodes.size());
      ArrayList<NetworkNode> finalnodes = new ArrayList<NetworkNode>();
//      HashMap<Integer,ArrayList<Integer>> conninfo = new HashMap<Integer,ArrayList<Integer>>();
      for(String n:allnodes)
      {
          NetworkNode nd = prunednodes.get(n);
          nd.id = nodeidlist.get(nd.username);
          ArrayList<Integer> connids = new ArrayList<Integer>();
//          ArrayList<ToNodeInfo> compact_To_nodes = new ArrayList<ToNodeInfo>();
          int counter = 0;
          for(ToNodeInfo tnf: nd.tonodes)
          {
              //user has never been retweeted. the chain terminates here, so remove it
              if(nodeidlist.containsKey(tnf.tousername))
              {
                  tnf.tonodeid = nodeidlist.get(tnf.tousername);
                  connids.add(tnf.tonodeid);                  
                  nd.tonodes.set(counter, tnf);
                  counter++;
              }                          
          }
          finalnodes.add(nd);
          //store the connections to compute the clusterids later
//          if(!conninfo.containsKey(nd.id))
//          {
//              conninfo.put(nd.id, connids);
//          }
      }
      //generate the clusterids
//      ArrayList<Integer>[] clusterids = (ArrayList<Integer>[])new ArrayList[allnodes.size()];
//      Set<Integer> idkeys = conninfo.keySet();
//      for(int id:idkeys)
//      {
//          for(int x:conninfo.get(id))
//          {
//            if(clusterids[x]==null)
//            {
//                ArrayList<Integer> toclusterid = new ArrayList<Integer>();
//                toclusterid.add(id);
//                clusterids[x] = toclusterid;
//            }
//            else
//            {
//                ArrayList<Integer> toclusterid = clusterids[x];
//                if(!toclusterid.contains(id))
//                {
//                    toclusterid.add(id);
//                    clusterids[x] = toclusterid;
//                }
//            }
//          }
//      }
      //now create the final node list with the clusterids
//      for(String n:allnodes)
//      {
//          NetworkNode nd = prunednodes.get(n);
//          ArrayList<Integer> cids = clusterids[nd.id];
//          if(cids!=null)
//          {
//            int size =  cids.size();
//            nd.clusterID = new int[size+1];
//            int counter=0;
//            nd.clusterID[counter++] = nd.id;
//            for(int c:cids)
//            {
//               nd.clusterID[counter++] = c;
//            }
//          }
          //System.out.println(nd.class_codes.toString());
//          finalnodes.add(nd);
//      }
      Collections.sort(finalnodes,new NodeIDComparator());
      
        for(NetworkNode node:finalnodes)
        {
            System.out.println(node.id+" "+node.username+" "+node.level+" "+node.size+" "+node.catColor+node.data.get(0));
        }
        
      for(int i=0;i<rawTweets.length();++i){
    	  try {
			System.out.println(rawTweets.get(i).toString());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      }
      
      System.out.println("Final nodes: "+finalnodes.size()+"\tRaw: "+rawTweets.length());
      return GetD3Structure(finalnodes);
    }

    public JSONArray getRawTweets() {
		return rawTweets;
	}

	/**
     * Creates a D3 representation of the nodes, consisting of two JSONArray a set of nodes and a set of links between the nodes
     * @param finalnodes
     * @return
     */
    public JSONObject GetD3Structure(ArrayList<NetworkNode> finalnodes)
    {
        JSONObject alltweets = new JSONObject();
        try {
            JSONArray nodes = new JSONArray();
            JSONArray links = new JSONArray();
            for (NetworkNode node : finalnodes)
            {
                try {
                    //create adjacencies
                    JSONArray nodedata = new JSONArray();
                    for (ToNodeInfo tnf : node.tonodes) {
                        JSONObject jsadj = new JSONObject();
                        jsadj.put("source", node.id);
                        jsadj.put("target", tnf.tonodeid);
                        //weight of the edge
                        jsadj.put("value", 1);
                        //class code is a unique id corresponding to the text
                        jsadj.put("data", tnf.class_code);
                        links.put(jsadj);
                        //create a data object for the node
                        JSONObject jsdata = new JSONObject();
                        jsdata.put("tonodeid", tnf.tonodeid);
                        jsdata.put("nodefrom", node.username);
                        jsdata.put("nodeto", tnf.tousername);
                        jsdata.put("tweet", tnf.text);
//                        jsdata.put("pubtime", tnf.date);
                        //class code for tweet to be used to filter
//                        jsdata.put("classcode", tnf.class_code);
                        nodedata.put(jsdata);
                    }
                    //add node
                    JSONObject nd = new JSONObject();
                    nd.put("name", node.username);
                    nd.put("group", node.group);
                    nd.put("id", node.id);
                    nd.put("size", node.size);
                    nd.put("catColor", node.catColor);
                    nd.put("catID", node.catID);
                    nd.put("data", nodedata);
                    nd.put("level", node.level);
                    //clusterids for the node
//                    JSONArray cids = new JSONArray();
//                    if (node.clusterID != null) {
//                        for (int code : node.clusterID) {
//                            cids.put(code);
//                        }
//                    } else {
//                        cids.put(node.id);
//                    }
//                    nd.put("clusterids", cids);
                    //classcodes for the node
//                    JSONArray codes = new JSONArray();
//                    for (int c : node.class_codes) {
//                        codes.put(c);
//                    }
//                    nd.put("classcodes", codes);
                    nodes.put(nd);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
            }
            alltweets.put("nodes", nodes);
            alltweets.put("links", links);
            alltweets.put("raw", rawTweets);
        } catch (JSONException ex) {
            Logger.getLogger(CreateD3Network.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return alltweets;
    }

    /**
     * Recursively traverses the list of nodes to identify all nodes reachable from a starting node.
     * @param userconnections A map containing the usernames as keys and the node information as value
     * @param cur_node Node currently being processed.
     * @param newnodes A list of nodes which can be reached from the current node
     * @return A map of the usernames and the node information for all nodes reachable
     */
    public HashMap<String,NetworkNode> GetNextHopConnections(HashMap<String,NetworkNode> userconnections,NetworkNode cur_node,HashMap<String,NetworkNode> newnodes)
    {
        cur_node.level = cur_node.level+1;
        newnodes.put(cur_node.username,cur_node);
        for(int i=0;i<cur_node.tonodes.size();i++)
        {
           ToNodeInfo tnf = cur_node.tonodes.get(i);           
           if(newnodes.containsKey(tnf.tousername))
           {
               continue;
           }

           HashMap<String,NetworkNode> rtnodes = GetNextHopConnections(userconnections, userconnections.get(tnf.tousername),newnodes);
           newnodes = rtnodes;
        }
        return newnodes;
    }

   /**
     * Divides a list of nodes into groups using the square root binning 
     * technique. If a node has size x and there are y groups in total. Then the
     * group of the node is computed as ceil((sqrt(x)/sqrt(max))*y), where max is
     * the size of the largest node.
     * @param nodes A list of nodes
     * @param max The maximum size of a node
     * @param min The minimum size of a node
     * @param noofclasses Number of classes into which the nodes must be classified
     * @return A list of nodes along with their class
     */
    public ArrayList<NetworkNode> ComputeGroupsSqrt(ArrayList<NetworkNode> nodes, int max, int min, int noofclasses)
    {
        ArrayList<NetworkNode> finalnodes = new ArrayList<NetworkNode>();
        for(int i=0;i<nodes.size();i++)
        {
            NetworkNode node = nodes.get(i);
            int color_index = 0;
            if(node.size>0)
            {           
                color_index = (int) Math.ceil(((double)Math.sqrt(node.size)/Math.sqrt(max))*noofclasses)-1;
//                node.size = color_index*6;
            }
            node.group = color_index;
            finalnodes.add(node);
        }
        return finalnodes;
    }


    //DEBUG use only
    public static void main(String[] args)
    {
        try {
            CreateD3Network cdn = new CreateD3Network();
            JSONObject jobj = new JSONObject();
            JSONObject obj = new JSONObject();
            obj.put("color", "#800000");
            JSONArray ja = new JSONArray();
            ja.put("zuccotti");
            obj.put("hts", ja);
            jobj.put("Group 1", obj);
            obj = new JSONObject();
            obj.put("color", "#0FFF00");
            ja = new JSONArray();
            ja.put("#nypd");
            obj.put("hts", ja);
            jobj.put("Group 2", obj);         
            String filename = "C:\\Users\\NguyenTrong\\Documents\\Downloads\\Compressed\\TDA\\Data\\ows.json";
            JSONObject nodes = cdn.ConvertTweetsToDiffusionPath(7, jobj,5,"03/24/2011","04/24/2011","qanda");
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
}
