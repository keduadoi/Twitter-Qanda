package Chapter4.util;

import Chapter4.GraphElements.RetweetEdge;
import Chapter4.GraphElements.UserNode;
import edu.uci.ics.jung.algorithms.scoring.VertexScorer;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;

public class BetweennessScorer implements VertexScorer<UserNode, Double>{

	public BetweennessScorer(Hypergraph<UserNode, RetweetEdge> graph){
		/*
		 * Step 1: Calculate the shortest path between each pair of nodes.
		 */
		DijkstraShortestPath<UserNode, RetweetEdge> paths = new DijkstraShortestPath<UserNode, RetweetEdge>((Graph<UserNode, RetweetEdge>) graph);
//		paths.getDistance(source, target);
	}
	
	public Double getVertexScore(UserNode arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
