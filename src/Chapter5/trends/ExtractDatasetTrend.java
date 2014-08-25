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

public class ExtractDatasetTrend
{
    static final String DEF_INFILENAME = "ows.json";
    // Date pattern used to count the volume of tweets
    final SimpleDateFormat SDM = new SimpleDateFormat("dd MMM yyyy HH:mm");

    public JSONArray GenerateDataTrend(String inFilename)
    {
        BufferedReader br = null;
        JSONArray result = new JSONArray();
        HashMap<String,Integer> datecount = new HashMap<String,Integer>();
        try{
            br= new BufferedReader(new InputStreamReader(new FileInputStream(inFilename),"UTF-8"));
            String temp = "";
            while((temp = br.readLine())!=null)
            {
                try {
                    JSONObject jobj = new JSONObject(temp);
                    long timestamp = jobj.getLong("timestamp");
                    Date d = new Date(timestamp);
                    String strdate = SDM.format(d);
                    if(datecount.containsKey(strdate))
                    {
                        datecount.put(strdate, datecount.get(strdate)+1);
                    }
                    else
                    {
                        datecount.put(strdate, 1);
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(ExtractDatasetTrend.class.getName()).log(Level.SEVERE, null, ex);
                }                
            }
            /** DateInfo consists of a date string and the corresponding count.
              * It also implements a Comparator for sorting by date
              */
            ArrayList<DateInfo> dinfos = new ArrayList<DateInfo>();
            Set<String> keys = datecount.keySet();
            for(String key:keys)
            {
                DateInfo dinfo = new DateInfo();
                try {
                    dinfo.d = SDM.parse(key);
                } catch (ParseException ex) {
                    ex.printStackTrace();
                    continue;
                }
                dinfo.count = datecount.get(key);
                dinfos.add(dinfo);
            }
            Collections.sort(dinfos);
            // Format and return the date string and the corresponding count
            for(DateInfo dinfo:dinfos)
            {
                try{
                    JSONObject jobj = new JSONObject();
                    jobj.put("date", SDM.format(dinfo.d));
                    jobj.put("count", dinfo.count);
                    result.put(jobj);
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
                Logger.getLogger(ExtractDatasetTrend.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }

    public static void main(String[] args)
    {
        ExtractDatasetTrend edt = new ExtractDatasetTrend();
        
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
        System.out.println(edt.GenerateDataTrend(infilename));
    }

}
