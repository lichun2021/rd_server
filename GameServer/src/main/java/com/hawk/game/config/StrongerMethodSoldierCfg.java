package com.hawk.game.config;

import java.util.Arrays;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;


@HawkConfigManager.XmlResource(file = "xml/stronger_method_soldier.xml")
public class StrongerMethodSoldierCfg extends HawkConfigBase {

	@Override
	protected boolean assemble() {
		try{
			int [] soldierIds = Arrays.stream(soldierList.split("_")).mapToInt(Integer::valueOf).toArray();
			if(soldierIds == null){
				return false;
			}
			
			double [] soldierScores = Arrays.stream(scoreList.split("_")).mapToDouble(Double::parseDouble).toArray();
			if(soldierScores == null){
				return false;
			}
			
			if(soldierIds.length == 0 || soldierIds.length != soldierScores.length){
				return false;
			}
			
			this.soldierIds = soldierIds;
			
			this.soldierScores = soldierScores;
			
		}catch(Exception e){
			HawkException.catchException(e);
			return false;
		}
		return super.assemble();
	}

	@Id
	private final int id;

	private final float percent;

	private final String soldierList;

	private final String scoreList;
	
	//private HashMap<Integer,Double> soldierIdScores = new HashMap<>();
	
	int [] soldierIds;  
	
	double [] soldierScores;
	
	public StrongerMethodSoldierCfg() {
		id = 0;
		percent = 0;
		soldierList = "";
		scoreList = "";
	}

	public int[] getSoldiers(){
		return soldierIds;
	}
	
	public double[] getScores(){
		return soldierScores;
	}
	
	public int getId() {
		return id;
	}

	public float getPercent() {
		return percent;
	}

	public String getSoldierList() {
		return soldierList;
	}

	public String getScoreList() {
		return scoreList;
	}
}
