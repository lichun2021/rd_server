package com.hawk.activity.type.impl.commandAcademy.rank;

public class FinalRankMember extends CommandAcademyRankMember{
	
	private int stage;
	
	private int param;

	
	public FinalRankMember() {
		
	}
	
	
	public FinalRankMember(String playerId,double score,int stage,int param) {
		super(playerId, score);
		this.stage = stage;
		this.param = param;
	}
	
	
	public String toString(){
		StringBuilder sbd = new StringBuilder();
		sbd.append(this.getPlayerId()).
		append("_").
		append(this.getRank()).
		append("_").
		append(this.getScore()).
		append("_").
		append(this.getStage()).
		append("_").
		append(this.getParam());
		return sbd.toString();
	}
	
	
	public void loadString(String string){
		String[] itemArray = string.split("_");
		if (itemArray.length != 5) {
			return;
		}
		this.setPlayerId(itemArray[0]);
		this.setRank(Integer.parseInt(itemArray[1]));
		this.setScore(Double.parseDouble(itemArray[2]));
		this.setStage(Integer.parseInt(itemArray[3]));
		this.setParam(Integer.parseInt(itemArray[4]));
	}
	
	
	public int getStage() {
		return stage;
	}

	public void setStage(int stage) {
		this.stage = stage;
	}

	public int getParam() {
		return param;
	}

	public void setParam(int param) {
		this.param = param;
	}
	
	

	

}
