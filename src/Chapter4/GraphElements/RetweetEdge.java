package Chapter4.GraphElements;


public class RetweetEdge {
	private UserNode to, from;
	private int retweetCount;
	
	public RetweetEdge(UserNode to, UserNode from){
		this.to = to;
		this.from = from;
		retweetCount = 1;
	}
	
	public void incrementRTCount(){
		retweetCount++;
	}
	
	public UserNode getTo() {
		return to;
	}
	public void setTo(UserNode to) {
		this.to = to;
	}
	public UserNode getFrom() {
		return from;
	}
	public void setFrom(UserNode from) {
		this.from = from;
	}
	public int getRetweetCount() {
		return retweetCount;
	}
	public void setRetweetCount(int retweetCount) {
		this.retweetCount = retweetCount;
	}
	
	public boolean equals(Object maybeEdge){
		if(maybeEdge instanceof RetweetEdge){
			RetweetEdge edge = (RetweetEdge) maybeEdge;
			return edge.to.equals(to) && edge.from.equals(from);
		}
		return false;
		
	}
	
	public String toString(){
		return from + " -> " + to;
	}
	
	public int hashCode(){
		return toString().hashCode();
	}
}
