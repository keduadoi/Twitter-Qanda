/* TweetTracker. Copyright (c) Arizona Board of Regents on behalf of Arizona State University
 * @author shamanth
 */
package com.qanda.text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lightcouch.CouchDbClient;

import com.google.gson.JsonObject;

import utils.Tags;
import utils.TextUtils;

public class ExtractTopKeywords
{

    //static final String DEF_INFILENAME = "tweets_single_lines.dat";
    static final int DEF_K = 60;
    //private CouchDbClient dbClient = new CouchDbClient("couchdb.properties");
    private CouchDbClient dbClient;
    
    /**
     * Extracts the most frequently occurring keywords from the tweets by processing them sequentially. Stopwords are ignored.
     * @param inFilename File containing a list of tweets as JSON objects
     * @param K Count of the top keywords to return
     * @param ignoreHashtags If true, hashtags are not considered while counting the most frequent keywords
     * @param ignoreUsernames If true, usernames are not considered while counting the most frequent keywords
     * @param tu TextUtils object which handles the stopwords
     * @return a JSONArray containing an array of JSONObjects. Each object contains two elements "text" and "size" referring to the word and it's frequency
     */
    public JSONArray GetTopKeywords(int K, boolean ignoreHashtags, boolean ignoreUsernames, TextUtils tu,String dbName)
    {
        HashMap<String, Integer> words = new HashMap<String,Integer>();
        dbClient = new CouchDbClient(dbName, false, "http", "localhost", 5984, null, null);
    	try{
    		List<JsonObject> allTweets = dbClient.view("_all_docs").includeDocs(true).limit(5001).startKeyDocId(null).query(JsonObject.class);
    		while(allTweets.size()>1)
			{ 
    			String lastDocId = "";
    			for(int k=0;k<allTweets.size()-2;++k){
        			JsonObject tweetJson = allTweets.get(k);
	        		lastDocId = tweetJson.get("id_str").toString().replaceAll("\"", "");
				if(tweetJson.get("text")!=null)
				{
				    String text = TextUtils.GetCleanText(tweetJson.get("text").toString());
				    //System.out.println(text);
				    text = text.toLowerCase().replaceAll("\\s+", " ");
				    /** Step 1: Tokenize tweets into individual words. and count their frequency in the corpus
				       * Remove stop words and special characters. Ignore user names and hashtags if the user chooses to.
				       */
				    HashMap<String,Integer> tokens = tu.TokenizeText(text,ignoreHashtags,ignoreUsernames);
				    Set<String> keys = tokens.keySet();
				    for(String key:keys)
				    {
				        if(words.containsKey(key))
				        {
				            words.put(key, words.get(key)+tokens.get(key));
				        }
				        else
				        {
				            words.put(key, tokens.get(key));
				        }
				    }
				}
            }
    			lastDocId = allTweets.get(allTweets.size()-1).get("id_str").toString().replace("\"", "");
        		allTweets = dbClient.view("_all_docs").includeDocs(true).limit(5001).startKeyDocId(lastDocId).query(JsonObject.class);
			}
    		
    		//last document
			for(int k=0;k<allTweets.size()-1;++k){
    			JsonObject tweetJson = allTweets.get(k);
			if(tweetJson.get("text")!=null)
			{
			    String text = TextUtils.GetCleanText(tweetJson.get("text").toString());
			    //System.out.println(text);
			    text = text.toLowerCase().replaceAll("\\s+", " ");
			    /** Step 1: Tokenize tweets into individual words. and count their frequency in the corpus
			       * Remove stop words and special characters. Ignore user names and hashtags if the user chooses to.
			       */
			    HashMap<String,Integer> tokens = tu.TokenizeText(text,ignoreHashtags,ignoreUsernames);
			    Set<String> keys = tokens.keySet();
			    for(String key:keys)
			    {
			        if(words.containsKey(key))
			        {
			            words.put(key, words.get(key)+tokens.get(key));
			        }
			        else
			        {
			            words.put(key, tokens.get(key));
			        }
			    }
			}
        }
        }catch(Exception e){
        	e.printStackTrace();
        }

        Set<String> keys = words.keySet();
        ArrayList<Tags> tags = new ArrayList<Tags>();
        for(String key:keys)
        {
            Tags tag = new Tags();
            tag.setKey(key);
            tag.setValue(words.get(key));
            tags.add(tag);
        }
        // Step 2: Sort the words in descending order of frequency
        Collections.sort(tags, Collections.reverseOrder());
        JSONArray cloudwords = new JSONArray();
        int numwords = K;
        if(tags.size()<numwords)
        {
            numwords = tags.size();
        }        
        for(int i=0;i<numwords;i++)
        {
            JSONObject wordfreq = new JSONObject();
            Tags tag = tags.get(i);
            try{
                wordfreq.put("text", tag.getKey());
                wordfreq.put("size",tag.getValue());
                //System.out.println(wordfreq.toString());
                cloudwords.put(wordfreq);
            }catch(JSONException ex)
            {
                ex.printStackTrace();
            }
        }
        //System.out.println(cloudwords.toString());
        return cloudwords;
    }

    //DEBUG USE ONLY
    public static void main(String[] args)
    {
        ExtractTopKeywords etk = new ExtractTopKeywords();

        //Initialize the TextUtils class which handles all the processing of text.
        TextUtils tu = new TextUtils();
        tu.LoadStopWords("/home/duc/workspace/TwitterQanda/WebContent/stopwords.txt");        
        //String infilename = DEF_INFILENAME;
        int K = DEF_K;
        if(args!=null)
        {
            if(args.length>=1&&!args[0].isEmpty())
            {
                File fl = new File(args[0]);
                if(fl.exists())
                {
                    //infilename = args[0];
                }
            }
            if(args.length>=2&&!args[1].isEmpty())
            {
                try{
                    K = Integer.parseInt(args[1]);
                }catch(NumberFormatException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        System.out.println(etk.GetTopKeywords(K, false,true,tu,"qanda"));
    }

}
