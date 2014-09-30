package Chapter4.GraphElements;



public class UserNode {
	private String username;
	
	public UserNode(String username){
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public boolean equals(Object un){
		if(un instanceof UserNode){
			return username.equals(((UserNode)un).username);
		}
		return false;
	}
	
	public String toString(){
		return username;
	}
	
	public int hashCode(){
		return username.hashCode();
	}
}
