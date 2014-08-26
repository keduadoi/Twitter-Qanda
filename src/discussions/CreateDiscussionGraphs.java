package discussions;

import java.io.*;

public class CreateDiscussionGraphs {
	public String GetDiscussions(String pythonFile){
		String graphs = "";
//		try {
//			ProcessBuilder pb = new ProcessBuilder("python",pythonFile);
//			Process p = pb.start();
//			BufferedReader buffer = new BufferedReader(new InputStreamReader(p.getInputStream()));
//			
//			String line = buffer.readLine();
//			while(line!=null){
//				graphs = graphs+line+"\n";
//				line = buffer.readLine();
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println(graphs);
		
		try{
			String dotFile = "C:\\Users\\NguyenTrong\\workspace\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp1\\wtpwebapps\\TwitterQanda\\graph.dot";
			BufferedReader dotReader = new BufferedReader(new FileReader(dotFile));
			String l = dotReader.readLine();
			while(l!=null){
				graphs = graphs+l+"\n";
				l=dotReader.readLine();
			}
			dotReader.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		//System.out.println(graphs);
		
		return graphs;
	}
}
