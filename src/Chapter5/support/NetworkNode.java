package Chapter5.support;


import java.util.ArrayList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author shamanth
 */
public class NetworkNode
{
    public int id;
    public String username;
    public int size;
    public String catColor;
    public int group;
//    public int[] clusterID;
    public int catID;
//    public double lat;
//    public double lng;
    public ArrayList<String> data;
    public int level;
    public ArrayList<Integer> class_codes;
    public ArrayList<ToNodeInfo> tonodes;

    public NetworkNode Copy()
    {
        NetworkNode tempnode = new NetworkNode();
        tempnode.catColor = this.catColor;
        tempnode.id = this.id;
        tempnode.username= this.username;
        tempnode.size = this.size;
        tempnode.group = this.group;
//        tempnode.clusterID = this.clusterID;
        tempnode.catID = this.catID;
//        tempnode.lat = this.lat;
//        tempnode.lng = this.lng;
        tempnode.data = this.data;
//        tempnode.level = this.level;
        tempnode.class_codes = this.class_codes;
        tempnode.tonodes = this.tonodes;
        return tempnode;
    }   
}
