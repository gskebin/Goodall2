package kr.co.dunet.goodall;

public class UserInfo {
	String mId = "";
	String mName = "";
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		
		UserInfo robot = (UserInfo) o;
		
		if (mId != null ? !mId.equals(robot.mId)
				: robot.mId != null)
			return false;
		
		return true;
	}
	
	@Override
	public int hashCode() {
		return mId != null ? mId.hashCode() : 0;
	}
}