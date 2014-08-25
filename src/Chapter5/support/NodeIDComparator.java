/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Chapter5.support;

import java.util.Comparator;

/**
 *
 * @author shamanth
 */
public class NodeIDComparator implements Comparator
{

    public int compare(Object o1, Object o2) {
        int id1 = ((NetworkNode) o1).id;
        int id2 = ((NetworkNode) o2).id;
        if(id1>id2)
        {
            return 1;
        }
        else
            if(id1<id2)
                return -1;
            else
                return 0;
    }


}
