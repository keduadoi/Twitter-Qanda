package Chapter4.centrality.examples;

import Chapter4.util.TweetFileToGraph;
import java.io.File;
import Chapter4.GraphElements.RetweetEdge;
import Chapter4.GraphElements.UserNode;
import edu.uci.ics.jung.algorithms.scoring.EigenvectorCentrality;
import edu.uci.ics.jung.graph.DirectedGraph;

public class EigenvectorCentralityExample {
	public static void main(String[] args){
		
		File tweetFile;
		
		if(args.length > 0){
			tweetFile = new File(args[0]);
		}
		else{
			tweetFile = new File("tweets_single_lines.dat");
		}
		
		DirectedGraph<UserNode, RetweetEdge> retweetGraph = TweetFileToGraph.getRetweetNetwork(tweetFile);
		
//		EigenVectorScorer scorer = new EigenVectorScorer(retweetGraph);
//		for(UserNode node : retweetGraph.getVertices()){
//			System.out.println(node + " - " + scorer.getVertexScore(node));
//		}
		
		EigenvectorCentrality<UserNode, RetweetEdge> eig = new EigenvectorCentrality<UserNode, RetweetEdge>(retweetGraph);
                eig.acceptDisconnectedGraph(true);
		eig.evaluate();
		
		for(UserNode node : retweetGraph.getVertices()){
                    System.out.println(node + " - " + eig.getVertexScore(node));
		}
	}
}
