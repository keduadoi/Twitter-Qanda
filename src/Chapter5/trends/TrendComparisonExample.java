/* TweetTracker. Copyright (c) Arizona Board of Regents on behalf of Arizona State University
 * @author shamanth
 */
package Chapter5.trends;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TrendComparisonExample
{
    static final String DEF_INFILENAME = "ows.json";
    static final SimpleDateFormat SDM = new SimpleDateFormat("dd MMM yyyy HH:mm");

    public JSONArray GenerateDataTrend(String inFilename, ArrayList<String> keywords)
    {
        BufferedReader br = null;
        JSONArray result = new JSONArray();
        HashMap<String,HashMap<String,Integer>> datecount = new HashMap<String,HashMap<String,Integer>>();
        try{
            br= new BufferedReader(new InputStreamReader(new FileInputStream(inFilename),"UTF-8"));
            String temp = "";
            while((temp = br.readLine())!=null)
            {
                try {
                    JSONObject jobj = new JSONObject(temp);
                    String text = jobj.getString("text").toLowerCase();
                    long timestamp = jobj.getLong("timestamp");
                    Date d = new Date(timestamp);
                    String strdate = SDM.format(d);
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
                } catch (JSONException ex) {
                    Logger.getLogger(TrendComparisonExample.class.getName()).log(Level.SEVERE, null, ex);
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
        }catch(IOException ex)
        {
            ex.printStackTrace();
        }finally{
            try {
                br.close();
            } catch (IOException ex) {
                Logger.getLogger(TrendComparisonExample.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }

    public static void main(String[] args)
    {
        TrendComparisonExample tce = new TrendComparisonExample();
        ArrayList<String> words = new ArrayList<String>();
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
            for(int i=1;i<args.length;i++)
            {
                if(args[i]!=null&&!args[i].isEmpty())
                {
                    words.add(args[i]);
                }
            }
        }
        if(words.isEmpty())
        {
            words.add("#nypd");
            words.add("#ows");
        }
        System.out.println(tce.GenerateDataTrend(infilename,words));
    }

}
