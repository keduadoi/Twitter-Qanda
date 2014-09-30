/* TweetTracker. Copyright (c) Arizona Board of Regents on behalf of Arizona State University
 * @author shamanth
 */
package com.qanda.trends;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lightcouch.CouchDbClient;

import com.google.gson.JsonObject;

public class TrendComparisonExample
{
    //static final String DEF_INFILENAME = "ows.json";
	private CouchDbClient dbClient;
    static final SimpleDateFormat SDM = new SimpleDateFormat("dd MMM yyyy hh:mm");

    public JSONArray GenerateDataTrend(String dbName, ArrayList<String> keywords, String from, String to)
    {
        dbClient = new CouchDbClient(dbName, false, "http", "localhost", 5984, null, null);
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
        SimpleDateFormat checkSDF = new SimpleDateFormat("dd MMM yyyy");
        JSONArray result = new JSONArray();
        HashMap<String,HashMap<String,Integer>> datecount = new HashMap<String,HashMap<String,Integer>>();
        	List<JsonObject> allTweets = dbClient.view("_all_docs").includeDocs(true).limit(5001).startKeyDocId(null).query(JsonObject.class);
        	while(allTweets.size()>1){
        		String lastDocId = "";
        		for(int k=0;k<allTweets.size()-2;++k){
                    JsonObject jobj = allTweets.get(k);
                    lastDocId = jobj.get("id_str").toString().replaceAll("\"", "");
                    String text = jobj.get("text").toString().toLowerCase();
                    
                    Date createdDate = new Date();
    			    Date fromDate = new Date();
    			    Date toDate = new Date();
    			    try {
    			    	String strdate = jobj.get("created_at").toString();
    			        String[] createdArr = strdate.split("\\s+");
    			        
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
                    
                    String strdate = jobj.get("created_at").toString().replaceAll("\"", "");
                    try {
						strdate = SDM.format(df.parse(strdate));
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

                    for(String word:keywords)
                    {
                        if(text.contains(word))
                        {
                            HashMap<String,Integer> wordcount = new HashMap<String,Integer>();
                            if(datecount.containsKey(strdate))
                            {
                                wordcount = datecount.get(strdate);
                            }
                            if(wordcount.containsKey(word))
                            {
                                wordcount.put(word, wordcount.get(word)+1);
                            }
                            else
                            {
                                wordcount.put(word, 1);
                            }
                            //update the wordcount for the specific date
                            datecount.put(strdate, wordcount);
                        }
                    }
                }
        		lastDocId = allTweets.get(allTweets.size()-1).get("id_str").toString().replace("\"", "");
        		allTweets = dbClient.view("_all_docs").includeDocs(true).limit(5001).startKeyDocId(lastDocId).query(JsonObject.class);
        	}
        	
        	//last document
        	JsonObject jobj = allTweets.get(0);
            String text = jobj.get("text").toString().toLowerCase();
            
            Date createdDate = new Date();
		    Date fromDate = new Date();
		    Date toDate = new Date();
		    try {
		    	String strdate = jobj.get("created_at").toString();
		        String[] createdArr = strdate.split("\\s+");
		        
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
		    } 
		    
		    //Check if the tweet is on the selected period
		    if(createdDate.before(fromDate) || createdDate.after(toDate)){
		    	//System.out.println("False: Created: "+createdDate.toString()+" From: "+fromDate.toString()+" To: "+toDate.toString());
		    	String strdateLastDoc = jobj.get("created_at").toString().replaceAll("\"", "");
	            try {
	            	strdateLastDoc = SDM.format(df.parse(strdateLastDoc));
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

	            for(String word:keywords)
	            {
	                if(text.contains(word))
	                {
	                    HashMap<String,Integer> wordcount = new HashMap<String,Integer>();
	                    if(datecount.containsKey(strdateLastDoc))
	                    {
	                        wordcount = datecount.get(strdateLastDoc);
	                    }
	                    if(wordcount.containsKey(word))
	                    {
	                        wordcount.put(word, wordcount.get(word)+1);
	                    }
	                    else
	                    {
	                        wordcount.put(word, 1);
	                    }
	                    //update the wordcount for the specific date
	                    datecount.put(strdateLastDoc, wordcount);
	                }
	            }
		    }
            
            
        	
            //sort the dates
            ArrayList<TCDateInfo> dinfos = new ArrayList<TCDateInfo>();
            Set<String> keys = datecount.keySet();
            for(String key:keys)
            {
                TCDateInfo dinfo = new TCDateInfo();
                try {
                    dinfo.d = SDM.parse(key);
                } catch (ParseException ex) {
                    ex.printStackTrace();
                    continue;
                }
                dinfo.wordcount = datecount.get(key);
                dinfos.add(dinfo);
            }            
            Collections.sort(dinfos);
            //prepare the output
            for(TCDateInfo date:dinfos)
            {
                JSONObject item = new JSONObject();            
                String strdate = SDM.format(date.d);
                try{
                    item.put("date",strdate);
                    HashMap<String,Integer> wordcount = date.wordcount;
                    for(String word:keywords)
                    {
                        if(wordcount.containsKey(word))
                        {
                            item.put(word, wordcount.get(word));
                        }
                        else
                        {
                            item.put(word, 0);
                        }
                    }
                    result.put(item);
                }catch(JSONException ex)
                {
                    ex.printStackTrace();
                }
            }

        return result;
    }

    public static void main(String[] args)
    {
        TrendComparisonExample tce = new TrendComparisonExample();
        ArrayList<String> words = new ArrayList<String>();
        //String infilename = DEF_INFILENAME;
//        if(args!=null)
//        {
//            if(args.length>=1&&!args[0].isEmpty())
//            {
//                File fl = new File(args[0]);
//                if(fl.exists())
//                {
//                    infilename = args[0];
//                }
//            }
//            for(int i=1;i<args.length;i++)
//            {
//                if(args[i]!=null&&!args[i].isEmpty())
//                {
//                    words.add(args[i]);
//                }
//            }
//        }
        if(words.isEmpty())
        {
            words.add("#qanda");
            words.add("#auspol");
        }
        JSONArray wordsArray = tce.GenerateDataTrend("qanda",words,"01/01/2011","12/31/2014");
        for(int i=0;i<wordsArray.length();++i){
        	try {
				System.out.println(wordsArray.get(i).toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

}
