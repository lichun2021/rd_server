package com.hawk.activity.type.impl.questTreasure.cfg;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import com.hawk.activity.type.impl.exchangeTip.AExchangeTipConfig;

/**
 *  中部养成计划
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/quest_treasure/quest_treasure_box.xml")
public class QuestTreasureBoxCfg extends AExchangeTipConfig {
	// 奖励ID
	@Id
	private final int id;
	private final String count;
	private final String coordinate;
	private final int boxScore;
	private final int reward;
	
	
	public QuestTreasureBoxCfg() {
		id = 0;
		count = "";
		coordinate = "";
		boxScore = 0;
		reward = 0;
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}

	@Override
	public int getId() {
		return id;
	}


	
	public HawkTuple2<Integer, Integer> getBoxCount(){
		String[] arr = this.count.trim().split(",");
		return HawkTuples.tuple(Integer.parseInt(arr[0]),Integer.parseInt(arr[1]));
	}

	
	public List<HawkTuple2<Integer, Integer>> getCoordinateList(){
		List<HawkTuple2<Integer, Integer>> list = new ArrayList<>();
		if(!HawkOSOperator.isEmptyString(this.coordinate)){
			String[] arr = this.coordinate.trim().split(",");
			for(String str : arr){
				String[] posArr = str.split("_");
				HawkTuple2<Integer, Integer> tuple = HawkTuples.tuple(Integer.parseInt(posArr[0]),Integer.parseInt(posArr[1]));
				list.add(tuple);
			}
		}
		return list;
	}

	
	
	public int getBoxScore() {
		return boxScore;
	}
	
	public int getReward() {
		return reward;
	}

	@Override
	protected final boolean checkValid() {
		QuestTreasureKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(QuestTreasureKVCfg.class);
		HawkTuple2<Integer, Integer> range = kvcfg.getRange();
		for(HawkTuple2<Integer, Integer> tuple : this.getCoordinateList()){
			int x = tuple.first;
			int y = tuple.second;
			if(x >= range.first || x < 0){
				throw new InvalidParameterException(String.format("QuestTreasureBoxCfg id: %s,range X error, range: %s,boxPos:  %s",
						this.id,range.toString(),tuple.toString()));
			}
			if(y >= range.second || y < 0){
				throw new InvalidParameterException(String.format("QuestTreasureBoxCfg id: %s,range Y error, range: %s,boxPos:  %s", 
						this.id,range.toString(),tuple.toString()));
			}
		}
		return super.checkValid();
	}
	

}
