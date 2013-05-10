package com.indicrowd.wowza.studiomanager.stream;

public class StreamInfo {
	public enum StreamInfoState {
		FAILED,
		PLAY
	}
	
	StreamInfoState state;
	String concertId;
	int nowCount;
	
	public StreamInfo(String concertId) {
		this.concertId = concertId;
		this.nowCount = 0;
	}
	
	public StreamInfoState getState() {
		return state;
	}
	public void setState(StreamInfoState state) {
		this.state = state;
	}
	public String getConcertId() {
		return concertId;
	}
	public int getNowCount() {
		return nowCount;
	}
	public void setNowCount(int nowCount) {
		this.nowCount = nowCount;
	}
	
	public String serializeJSON() {
		StringBuilder sb = new StringBuilder(1000);
		
		sb.append("{");
		sb.append("\"command\": ");
			sb.append("{");
				sb.append("\"concertId\": \"" + concertId + "\", ");
				sb.append("\"state\": \"" + state + "\", ");
				sb.append("\"nowCount\": \"" + nowCount + "\" ");
			sb.append("}");
		sb.append("}");
		
		return sb.toString();
	}
}
