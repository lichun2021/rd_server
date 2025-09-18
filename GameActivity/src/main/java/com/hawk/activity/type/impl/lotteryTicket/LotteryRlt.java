package com.hawk.activity.type.impl.lotteryTicket;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.type.impl.lotteryTicket.config.LotteryTicketRewardCfg;
import com.hawk.game.protocol.Activity.PBLotteryTicketAssistRlt;
import com.hawk.game.protocol.Activity.PBLotteryTicketReward;

public class LotteryRlt {
	
	
	private String id;
	
	private long lotteryTime;
	
	private List<HawkTuple3<Integer, Integer, Integer>> rewards = new CopyOnWriteArrayList<>();
	

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public long getLotteryTime() {
		return lotteryTime;
	}
	
	public void setLotteryTime(long lotteryTime) {
		this.lotteryTime = lotteryTime;
	}
	
	public List<HawkTuple3<Integer, Integer, Integer>> getRewards() {
		return rewards;
	}
	
	public void setRewards(List<HawkTuple3<Integer, Integer, Integer>> rewards) {
		this.rewards = rewards;
	}
	
	public int hasBigReward(){
		for(HawkTuple3<Integer, Integer, Integer> rewardTuple : this.rewards){
	    	LotteryTicketRewardCfg rewardCfg = HawkConfigManager.getInstance().getConfigByKey(LotteryTicketRewardCfg.class, rewardTuple.first);
	    	if(Objects.nonNull(rewardCfg) && !HawkOSOperator.isEmptyString(rewardCfg.getAssistReward())){
	    		return 1;
	    	}
	    }
		return 0;
	}
	
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("id", this.id);
		obj.put("lotteryTime", this.lotteryTime);
		if(Objects.nonNull(this.rewards) && 
				!this.rewards.isEmpty()){
			JSONArray arr = new JSONArray();
			for(HawkTuple3<Integer, Integer, Integer> tuple : rewards){
				String str = tuple.first+"_"+tuple.second+"_"+tuple.third;
				arr.add(str);
			}
			obj.put("rewards", arr.toJSONString());
		}
		return obj.toJSONString();
	}
	
	

	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.id = obj.getString("id");
		this.lotteryTime = obj.getLongValue("lotteryTime");
		
		this.rewards.clear();
		if(obj.containsKey("rewards")){
			String rewardStr = obj.getString("rewards");
			if(!HawkOSOperator.isEmptyString(rewardStr)){
				JSONArray arr = JSONArray.parseArray(rewardStr);
				for(int i =0;i<arr.size();i++){
					String str = arr.getString(i);
					String strArr[] = str.split("_");
					HawkTuple3<Integer, Integer, Integer> tuple = 
							HawkTuples.tuple(Integer.parseInt(strArr[0]), Integer.parseInt(strArr[1]), Integer.parseInt(strArr[2]));
					this.rewards.add(tuple);
				}
			}
			
		}
	}
	
	public PBLotteryTicketAssistRlt genBuilder(){
		PBLotteryTicketAssistRlt.Builder builder = PBLotteryTicketAssistRlt.newBuilder();
		builder.setRltId(this.id);
		builder.setLotteryTime(this.lotteryTime);
		for(HawkTuple3<Integer, Integer, Integer> tuple : this.rewards){
			PBLotteryTicketReward.Builder rbuilder = PBLotteryTicketReward.newBuilder();
			rbuilder.setReward(tuple.first);
			rbuilder.setShowNum(tuple.second);
			rbuilder.setShowCount(tuple.third);
			builder.addRewards(rbuilder);
		}
		return builder.build();
		
	}
}
