package Chapter4.util;

import edu.uci.ics.jung.algorithms.scoring.VertexScorer;
import edu.uci.ics.jung.graph.Hypergraph;

/**
 * This is a Jung Node Scorer that computes the 
 * In-Degree Centrality of nodes.
 */
public class InDegreeScorer<T> implements VertexScorer<T, Double>{

	//The graph representation in JUNG.
	private Hypergraph<T, ?> graph;
	
	/**
	 * Initialize the graph scorer.
	 * @param graph
	 * The graph we wish to score.
	 */
	public InDegreeScorer(Hypergraph<T, ?> graph){
		this.graph = graph;
	}
	
	/**
	 * @return The In-Degree Centrality of the vertex. 
	 */
	public Double getVertexScore(T node) {
		return (double) graph.getInEdges(node).size();
	}
}