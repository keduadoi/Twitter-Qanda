/* TweetTracker. Copyright (c) Arizona Board of Regents on behalf of Arizona State University
 * @author shamanth
 */
package Chapter5.text;

import Chapter5.support.DateInfo;
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

public class EventSummaryExtractor
{

    final String DEF_INFILENAME = "ows.json";
    HashMap<String,ArrayList<String>> CATEGORIES = new HashMap<String,ArrayList<String>>();
    SimpleDateFormat twittersdm = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy");
    SimpleDateFormat dayhoursdm = new SimpleDateFormat("yyyy-MM-dd:HH");
//    SimpleDateFormat daysdm = new SimpleDateFormat("MM/dd/yyyy");
    SimpleDateFormat hoursdm = new SimpleDateFormat("HH");

    /**
     *
     */
    public void InitializeCategories()
    {        
        ArrayList<String> people = new ArrayList<String>();
        people.add("protesters");
        people.add("people");
        CATEGORIES.put("People",people);
        ArrayList<String> police = new ArrayList<String>();
        police.add("police");
        police.add("cops");
        police.add("nypd");
        police.add("raid");
        CATEGORIES.put("Police",police);
        ArrayList<String> media = new ArrayList<String>();
        media.add("press");
        media.add("news");
        media.add("media");
        CATEGORIES.put("Media",media);
        ArrayList<String> city = new ArrayList<String>();
        city.add("nyc");
        city.add("zucotti");
        city.add("park");        
        CATEGORIES.put("Location",city);
        ArrayList<String> judiciary = new ArrayList<String>();
        judiciary.add("judge");
        judiciary.add("eviction");
        judiciary.add("order");
        judiciary.add("court");
        CATEGORIES.put("Judiciary", judiciary);
    }

    /**
     * 
     * @param filename
     * @return
     */
    public JSONObject ExtractCategoryTrends(String filename)
    {
        JSONObject result = new JSONObject();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename), "UTF-8"));
            String temp = "";
            Set<String> catkeys = CATEGORIES.keySet();
            HashMap<String,HashMap<String,Integer>> datecount = new HashMap<String,HashMap<String,Integer>>();
            while((temp = br.readLine())!=null)
            {
                Date d = new Date();
                try {
                    JSONObject jobj = new JSONObject(temp);
                     //Published time
                    if(!jobj.isNull("created_at"))
                    {
                        String time = "";
                        try {
                            time = jobj.getString("created_at");
                        } catch (JSONException ex) {
                            Logger.getLogger(EventSummaryExtractor.class.getName()).log(Level.SEVERE, null, ex);
                        }                        
                        if(time.isEmpty())
                        {
                           continue;
                        }
                        else
                        {
                            try {
                                d = twittersdm.parse(time);
                            } catch (ParseException ex) {
                                continue;
                            }
                        }
                    }
                    else
                    if(!jobj.isNull("timestamp"))
                    {
                        long time = new Date().getTime();
                        try{
                            time = jobj.getLong("timestamp");
                        }catch(JSONException ex)
                        {
                            ex.printStackTrace();
                        }
                        d = new Date();
                        d.setTime(time);
                    }
                    String datestr = dayhoursdm.format(d);
                    String text = jobj.getString("text").toLowerCase();
//                    System.out.println(text);
                    for(String key:catkeys)
                    {
                        ArrayList<String> words = CATEGORIES.get(key);
                        for(String word:words)
                        {
                            if(text.contains(word))
                            {
                                HashMap<String,Integer> categorycount = new HashMap<String,Integer>();
                                if(datecount.containsKey(datestr))
                                {
                                    categorycount = datecount.get(datestr);                                   
                                }
                                if(categorycount.containsKey(key))
                                {
                                    categorycount.put(key, categorycount.get(key)+1);
                                }
                                else
                                {
                                    categorycount.put(key, 1);
                                }
                                //update the categorycount for the specific date
                                datecount.put(datestr, categorycount);
                                break;
                            }
                        }
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(EventSummaryExtractor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            //sort the dates
            Set<String> datekeys = datecount.keySet();
            ArrayList<DateInfo> dinfos = new ArrayList<DateInfo>();
            for(String date:datekeys)
            {
                Date d = null;
                try {
                    d = dayhoursdm.parse(date);
                } catch (ParseException ex) {
                    Logger.getLogger(EventSummaryExtractor.class.getName()).log(Level.SEVERE, null, ex);
                }
                if(d!=null)
                {
                    DateInfo info = new DateInfo();
                    info.d = d;
                    info.catcounts = datecount.get(date);
                    dinfos.add(info);
                }
            }
            Collections.sort(dinfos, Collections.reverseOrder());
            try {
                result.put("axisxstep", dinfos.size()-1);
            } catch (JSONException ex) {
                Logger.getLogger(EventSummaryExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }            
            try {
                result.put("axisystep", CATEGORIES.size()-1);
            } catch (JSONException ex) {
                Logger.getLogger(EventSummaryExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
            JSONArray xcoordinates = new JSONArray();
            JSONArray ycoordinates = new JSONArray();
            //now add the data and the axis labels
            JSONArray axisxlabels = new JSONArray();
            JSONArray axisylabels = new JSONArray();
            JSONArray data = new JSONArray();            
            for(String key:catkeys)
            {
                axisylabels.put(key);
            }
            //counters to mark the indices of the values added to data field. i is the x coordinate and j is the y coordinate
            int i=0,j=0;
            
            for(DateInfo date:dinfos)
            {
                String strdate = hoursdm.format(date.d);
                axisxlabels.put(strdate);
                HashMap<String,Integer> catcounts = date.catcounts;
                for(String key:catkeys)
                {
                    xcoordinates.put(j);
                    ycoordinates.put(i++);
                    if(catcounts.containsKey(key))
                    {
                        data.put(catcounts.get(key));
                    }
                    else
                    {
                        data.put(0);
                    }
                }
                //reset the x coordinate as we move to the next y item
                i=0;
                j++;
            }
            try {
                result.put("xcoordinates", xcoordinates);
            } catch (JSONException ex) {
                Logger.getLogger(EventSummaryExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                result.put("ycoordinates", ycoordinates);
            } catch (JSONException ex) {
                Logger.getLogger(EventSummaryExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                result.put("axisxlabels", axisxlabels);
            } catch (JSONException ex) {
                Logger.getLogger(EventSummaryExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                result.put("axisylabels", axisylabels);
            } catch (JSONException ex) {
                Logger.getLogger(EventSummaryExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                result.put("data", data);
            } catch (JSONException ex) {
                Logger.getLogger(EventSummaryExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
            br.close();
        } catch (IOException ex) {
            Logger.getLogger(EventSummaryExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public static void main(String[] args)
    {
        EventSummaryExtractor ese = new EventSummaryExtractor();
        String infilename = ese.DEF_INFILENAME;
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
        ese.InitializeCategories();
        System.out.println(ese.ExtractCategoryTrends(infilename).toString());
    }
}
