package Chapter4.util;

import Chapter4.GraphElements.RetweetEdge;
import Chapter4.GraphElements.UserNode;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import edu.uci.ics.jung.algorithms.scoring.VertexScorer;
import edu.uci.ics.jung.graph.Hypergraph;

/**
 * This is a Jung Node Scorer that computes the Eigenvector Centrality for each node.
 */
public class EigenVectorScorer implements VertexScorer<UserNode, Double> {

	private UserNode[] users;
	private DoubleMatrix2D eigenVectors;
	private int dominantEigenvectorIdx;

	public EigenVectorScorer(Hypergraph<UserNode, RetweetEdge> graph){
		users = new UserNode[graph.getVertexCount()];
		graph.getVertices().toArray(users);
		
		/* Step 1: Create the adjacency matrix.
		 * 
		 * An adjacency matrix is a matrix with N users and N columns, 
		 * where N is the number of nodes in the network.
		 * An entry in the matrix is 1 when node i connects to node j,
		 * and 0 otherwise.
		 */
		SparseDoubleMatrix2D matrix = new SparseDoubleMatrix2D(users.length, users.length);
		for(int i = 0; i < users.length; i++){
			for(int j = 0; j < users.length; j++){
				matrix.setQuick(i, j, graph.containsEdge(new RetweetEdge(users[i], users[j])) ? 1 : 0);
			}
		}
		
		/* Step 2: Find the principle eigenvector.
		 * For more information on eigen-decomposition please see
		 * http://mathworld.wolfram.com/EigenDecomposition.html
		 */
		EigenvalueDecomposition eig = new EigenvalueDecomposition(matrix);
		DoubleMatrix2D eigenVals = eig.getD();
		eigenVectors = eig.getV();
		
		dominantEigenvectorIdx = 0;
		for(int i = 1; i < eigenVals.columns(); i++){
			if(eigenVals.getQuick(dominantEigenvectorIdx, dominantEigenvectorIdx) <
					eigenVals.getQuick(i, i)){
				dominantEigenvectorIdx = i;
			}
		}
	}
	
	public Double getVertexScore(UserNode arg0) {
		for(int i = 0; i < users.length; i++){
			if(users[i].equals(arg0)){
				return Math.abs(eigenVectors.getQuick(i, dominantEigenvectorIdx));
			}
		}
		return null;
	}

}
