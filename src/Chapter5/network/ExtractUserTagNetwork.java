/* TweetTracker. Copyright (c) Arizona Board of Regents on behalf of Arizona State University
 * @author shamanth
 */
package Chapter5.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExtractUserTagNetwork
{

    static final String DEF_INFILENAME = "ows.json";    

    /**
     * Extracts a map of all the hashtags a user has used in his tweets resulting in a bipartite network. The frequency of each tag is also returned in the form of a map.
     * @param inFilename File containing a list of tweets as JSON objects
     * @return A map containing the users as keys and a map containing the hashtags they use along with their frequency.
     */
    public HashMap<String,HashMap<String,Integer>> ExtractUserHashtagNetwork(String inFilename)
    {
        HashMap<String,HashMap<String,Integer>> usertagmap = new HashMap<String,HashMap<String,Integer>>();
         BufferedReader br = null;
        try{
            br = new BufferedReader(new InputStreamReader(new FileInputStream(inFilename),"UTF-8"));
            String temp = "";
            while((temp = br.readLine())!=null)
            {
                try{
                    JSONObject tweetobj = new JSONObject(temp);
                    String text;
                    String username;
                    HashMap<String,Integer> tags = new HashMap<String,Integer>();
                    if(!tweetobj.isNull("entities"))
                    {
                        JSONObject entities = tweetobj.getJSONObject("entities");
                         JSONArray hashtags;
                            try {
                                hashtags = entities.getJSONArray("hashtags");
                                for(int i=0;i<hashtags.length();i++)
                                {
                                    JSONObject tag = hashtags.getJSONObject(i);
                                    String tg = tag.getString("text").toLowerCase();
                                    if(!tags.containsKey(tg))
                                    {
                                        tags.put(tg,1);
                                    }
                                    else
                                    {
                                        tags.put(tg, tags.get(tg)+1);
                                    }
                                }
                            }catch(JSONException ex)
                            {
                                ex.printStackTrace();
                            }
                    }
                    else
                    if(!tweetobj.isNull("text"))
                    {
                        text = tweetobj.getString("text");
                        tags = ExtractHashTags(text);
                    }
                    if(!tweetobj.isNull("user"))
                    {
                        JSONObject userobj = tweetobj.getJSONObject("user");
                        username = "@"+userobj.getString("screen_name").toLowerCase();
                        if(usertagmap.containsKey(username))
                        {
                            HashMap<String,Integer> usertags = usertagmap.get(username);
                            Set<String> keys = tags.keySet();
                            for(String k:keys)
                            {
                                if(usertags.containsKey(k))
                                {
                                    usertags.put(k, usertags.get(k)+tags.get(k));
                                }
                                else
                                {
                                    usertags.put(k, tags.get(k));
                                }
                            }
                            usertagmap.put(username, usertags);
                        }
                        else
                        {
                            usertagmap.put(username, tags);
                        }
                    }
                }catch(JSONException ex)
                {
                    ex.printStackTrace();
                }
            }
        }catch(IOException ex)
        {
            ex.printStackTrace();
        }finally{
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(ExtractUserTagNetwork.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return usertagmap;
    }

    /**
     * Extracts all the hashtags mentioned in a tweet and creates a map with the frequency of their occurrence.
     * @param text
     * @return A map containing the hashtags as keys and their frequency as value
     */
    public HashMap<String,Integer> ExtractHashTags(String text)
    {
        Pattern p = Pattern.compile("#[a-zA-Z0-9]+");
        Matcher m = p.matcher(text);
        HashMap<String,Integer> tags = new HashMap<String,Integer>();
        while(m.find())
        {
            String tag = text.substring(m.start(),m.end()).toLowerCase();
            if(!tags.containsKey(tag))
            {
                tags.put(tag,1);
            }
            else
            {
                tags.put(tag, tags.get(tag)+1);
            }
        }
        return tags;
    }

    public static void main(String[] args)
    {
        ExtractUserTagNetwork eutn = new ExtractUserTagNetwork();

        String infilename = DEF_INFILENAME;
        if(args!=null)
        {
            if(args.length>=1&&!args[0].isEmpty())
            {
                File fl = new File(args[0]);
                if(fl.exists())
                {
                    infilename = args[0];
                }
            }
        }
        HashMap<String, HashMap<String,Integer>> usertagmap = eutn.ExtractUserHashtagNetwork(infilename);
        Set<String> keys = usertagmap.keySet();
        for(String key:keys)
        {
            System.out.println(key);
            HashMap<String,Integer> tags = usertagmap.get(key);
            Set<String> tagkeys = tags.keySet();
            for(String tag:tagkeys)
            {
                System.out.println(tag+","+tags.get(tag));
            }
        }
    }
}
