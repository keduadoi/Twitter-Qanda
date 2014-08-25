/* TweetTracker. Copyright (c) Arizona Board of Regents on behalf of Arizona State University
 * @author shamanth
 */
package Chapter5.support;

import java.util.Date;
import java.util.HashMap;

public class DateInfo implements Comparable
{
    public Date d;
    public HashMap<String,Integer> catcounts = new HashMap<String,Integer>();

    public int compareTo(Object o) {
        DateInfo temp = (DateInfo) o;
        if(temp.d.after(this.d))
        {
            return 1;
        }
        else
        if(temp.d.before(this.d))
        {
            return -1;
        }
        else
        {
            return 0;
        }
    }
}
