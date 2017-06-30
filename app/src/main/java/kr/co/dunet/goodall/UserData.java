package kr.co.dunet.goodall;

public class UserData {
	public String userId = "";
	public String userName = "";
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		
		UserData robot = (UserData) o;
		
		if (userId != null ? !userId.equals(robot.userId)
				: robot.userId != null)
			return false;
		
		return true;
	}
	
	@Override
	public int hashCode() {
		return userId != null ? userId.hashCode() : 0;
	}
	
}