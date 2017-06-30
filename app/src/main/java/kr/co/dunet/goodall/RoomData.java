package kr.co.dunet.goodall;

public class RoomData {
	public String roomName = "";
	public String roomCode = "";
	public String roomCode2 = "";
	public String publicYn = "Y";
	public String roomPw = "";
	public String roomDate = "";
	public String roomAdmin = "";
	public String roomAdminNickname = "";
	public String exitView = "N";
	public Integer noRead = 0;
	public String modDate = "";
	
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		
		RoomData robot = (RoomData) o;
		
		if (roomCode != null ? !roomCode.equals(robot.roomCode)
				: robot.roomCode != null)
			return false;
		
		return true;
	}
	
	@Override
	public int hashCode() {
		return roomCode != null ? roomCode.hashCode() : 0;
	}
	
}