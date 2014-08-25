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
public class NodeSizeComparator implements Comparator
{
    public int compare(Object o1, Object o2)
    {
        int size1 = ((NetworkNode) o1).size;
        int size2 = ((NetworkNode) o2).size;        
        if(size1>size2)
        {
            return 1;
        }
        if(size1<size2)
                return -1;
            else
                return 0;
    }

}
