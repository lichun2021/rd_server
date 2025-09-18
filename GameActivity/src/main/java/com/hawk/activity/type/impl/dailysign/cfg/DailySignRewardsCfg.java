
package com.hawk.activity.type.impl.dailysign.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;
import org.hibernate.util.StringHelper;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;


/**
 * 签到奖励
 * @author RickMei 
 *
 */
@HawkConfigManager.XmlResource(file = "activity/daily_sign/daily_sign_reward.xml")
public class DailySignRewardsCfg extends HawkConfigBase {
	@Id
	private final int itemId;
	
	private final int pool;
	
	private final String vipDouble;
	
	private final int day;
	
	private final String rewards;
	
	private List<RewardItem.Builder> rewardList;
	
	private HawkTuple2<Integer, Integer> vipDoublePair;
	
	private static TreeMap<Integer, Map<Integer, DailySignRewardsCfg>> cfgAll = new TreeMap<Integer, Map<Integer, DailySignRewardsCfg>>();
	
	public DailySignRewardsCfg(){
		itemId = 0;
		pool = 1;
		vipDouble = "";
		day = 0;
		rewards = "";
	}
	
	public static DailySignRewardsCfg getCfg( int pool, int day ){
		if(0 == pool){
			return null;
		}
		Map<Integer, DailySignRewardsCfg> cfgs = cfgAll.get(pool);
		if(null == cfgs){
			cfgs = cfgAll.lastEntry().getValue();
		}
		if(null == cfgs){
			return null;
		}
		return cfgs.get(day);
	}
	
	
	public static Map<Integer, DailySignRewardsCfg> getTermCfg(int pool){
		Map<Integer, DailySignRewardsCfg> cfgs = cfgAll.get(pool); 
		if(null == cfgs){
			return cfgAll.lastEntry().getValue();
		}
		return cfgs;
	}
	
	@Override
	protected boolean assemble() {
		String[] strArray = StringHelper.split("_", vipDouble); 
		vipDoublePair = new HawkTuple2<Integer, Integer>(Integer.valueOf(strArray[0]), Integer.valueOf(strArray[1]));
		if(strArray.length != 2){
			return false;
		}
		rewardList = RewardHelper.toRewardItemImmutableList(rewards);
		Map<Integer, DailySignRewardsCfg> findCfgMap = cfgAll.get(this.pool);
		if(null == findCfgMap){
			findCfgMap = new HashMap<Integer, DailySignRewardsCfg>();
			cfgAll.put(this.pool, findCfgMap);
		}
		findCfgMap.put(this.day, this);
		
		return super.assemble();
	}
	public int getItemId() {
		return itemId;
	}
	public int getPool() {
		return pool;
	}
	public String getVipDouble() {
		return vipDouble;
	}
	public int getDay() {
		return day;
	}
	public String getRewards() {
		return rewards;
	}
	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}
	
	public int getVip(){
		return vipDoublePair.second;
	}
	
	public int getMutiple(){
		return vipDoublePair.first;
	}
}
