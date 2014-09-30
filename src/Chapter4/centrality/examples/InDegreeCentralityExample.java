package Chapter4.centrality.examples;

import Chapter4.util.TweetFileToGraph;
import java.io.File;
import Chapter4.GraphElements.RetweetEdge;
import Chapter4.GraphElements.UserNode;
import edu.uci.ics.jung.graph.DirectedGraph;

public class InDegreeCentralityExample {
	
	public static void main(String[] args){
		
		File tweetFile;
		
		if(args.length > 0){
			tweetFile = new File(args[0]);
		}
		else{
			tweetFile = new File("synthetic_retweet_network.json");
		}
		
		DirectedGraph<UserNode, RetweetEdge> retweetGraph = TweetFileToGraph.getRetweetNetwork(tweetFile);
		
		//calculate the betweenness centrality
		for(UserNode node : retweetGraph.getVertices()){
			System.out.println(node + " - " + retweetGraph.getInEdges(node).size());
		}
		
	}
}
