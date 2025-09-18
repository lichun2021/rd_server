package com.hawk.activity.type.impl.strongestGuild.cache;

import java.util.HashMap;
import java.util.Map;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import com.hawk.activity.type.impl.strongestGuild.cfg.StrongestGuildCfg;

/***
 * 个人总积分数据类
 * @author yang.rao
 */

public class PersonData {
	
	private String playerId;
	
	private int termId;
	
	//stageId <--> Score
	private Map<Integer, Long> map;
	
	public PersonData(){}
	
	public PersonData(String playerId, int termId){
		this.termId = termId;
		this.playerId = playerId;
		this.map = new HashMap<>();
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public Map<Integer, Long> getMap() {
		return map;
	}

	public void setMap(Map<Integer, Long> map) {
		this.map = map;
	}
	
	/***
	 * 增加积分
	 */
	public void addScore(int stageId, long score){
		Long cur = map.get(stageId);
		if(cur == null){
			cur = new Long(score);
			map.put(stageId, cur);
		}else{
			long nowScore = cur + score;
			map.put(stageId, nowScore);
		}
	}
	
	/***
	 * 计算总积分
	 * @return
	 */
	public long calTotalScore(){
		long totalScore = 0;
		for(Integer key : map.keySet()){
			long score = map.get(key);
			StrongestGuildCfg circularCfg = HawkConfigManager.getInstance().getConfigByKey(StrongestGuildCfg.class, key);
			if(circularCfg == null){
				HawkLog.errPrintln("StrongestGuildActivity cal person total score error, stageId:{}", key);
				continue;
			}
			totalScore += (long)(circularCfg.getScoreWeightCof() * score / 10000.0f);
		}
		return totalScore;
	}
}
