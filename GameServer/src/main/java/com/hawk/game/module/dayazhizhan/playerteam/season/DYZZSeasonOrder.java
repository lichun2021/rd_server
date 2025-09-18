package com.hawk.game.module.dayazhizhan.playerteam.season;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.DYZZWar;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.item.AwardItems;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZSeasonOrderCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.SerializJsonStrAble;
import com.hawk.log.Action;

public class DYZZSeasonOrder implements SerializJsonStrAble{

	private int termId;
	
	private String playerId;
	
	private int score;
	
	private int buyOrderId;
	
	private List<Integer> rewardLevel = new ArrayList<>();
	
	private List<Integer> advanceRewardLevel = new ArrayList<>();

	private Map<Integer, Integer> rewardChoose = new HashMap<>();
	private Map<Integer, Integer> advanceRewardChoose = new HashMap<>();
	
	/**
	 * 是否可以购买礼包
	 * @return
	 */
	public boolean canBuyAdvacne(){
		if(this.buyOrderId > 0){
			return false;
		}
		return true;
	}
	
	/**
	 * 购买升级
	 * @param giftId
	 * @return
	 */
	public boolean buyAdvance(int giftId){
		if(this.buyOrderId > 0){
			return false;
		}
		this.buyOrderId = giftId;
		DYZZSeasonRedisData.getInstance().updateDYZZSeasonOrder(this);
		return true;
	}

	public void rewardChoose(int type,int level, int choose){
		if(type == 1){
			rewardChoose.put(level, choose);
		}else if(type == 2){
			advanceRewardChoose.put(level, choose);
		}
		DYZZSeasonRedisData.getInstance().updateDYZZSeasonOrder(this);
	}

	public void fillChooseData(DYZZWar.PBDYZZSeasonOrder.Builder builder){
		for(Map.Entry<Integer, Integer> entry : rewardChoose.entrySet()){
			DYZZWar.PBDYZZSeasonOrderChoose.Builder choose = DYZZWar.PBDYZZSeasonOrderChoose.newBuilder();
			choose.setType(1);
			choose.setLevel(entry.getKey());
			choose.setChoose(entry.getValue());
			builder.addRewardChoose(choose);
		}
		for(Map.Entry<Integer, Integer> entry : advanceRewardChoose.entrySet()){
			DYZZWar.PBDYZZSeasonOrderChoose.Builder choose = DYZZWar.PBDYZZSeasonOrderChoose.newBuilder();
			choose.setType(2);
			choose.setLevel(entry.getKey());
			choose.setChoose(entry.getValue());
			builder.addRewardChoose(choose);
		}
	}

	/**
	 * 获取奖励
	 * @param type
	 * @param level
	 */
	public void achiveReward(Player player,int type,int level){
		if(type == 1){
			this.achiveRewardComm(player,level);
		}else if(type == 2){
			this.achiveRewardAdvance(player,level);
		}else if(type == 3){
			this.achiveRewardAll(player);
		}
	}
	
	/**
	 * 获取普通奖励
	 * @param player
	 * @param level
	 */
	private void achiveRewardComm(Player player,int level){
		boolean canReward = this.canAchiveReward(level, false);
		if(!canReward){
			return;
		}
		DYZZSeasonOrderCfg cfg = HawkConfigManager.getInstance()
				.getConfigByKey(DYZZSeasonOrderCfg.class, level);
		
		this.rewardLevel.add(level);
		DYZZSeasonRedisData.getInstance().updateDYZZSeasonOrder(this);
		// 发奖励
		AwardItems award = AwardItems.valueOf();
		award.addItemInfos(cfg.getRewardItems(rewardChoose.getOrDefault(cfg.getId(), 0)));
		award.rewardTakeAffectAndPush(player, Action.DYZZ_SEASON_ORDER_ACHIVE, true, null);
	}
	
	/**
	 * 获取进阶奖励
	 * @param player
	 * @param level
	 */
	private void achiveRewardAdvance(Player player,int level){
		boolean canReward = this.canAchiveReward(level, true);
		if(!canReward){
			return;
		}
		DYZZSeasonOrderCfg cfg = HawkConfigManager.getInstance()
				.getConfigByKey(DYZZSeasonOrderCfg.class, level);
		
		this.advanceRewardLevel.add(level);
		DYZZSeasonRedisData.getInstance().updateDYZZSeasonOrder(this);
		// 发奖励
		AwardItems award = AwardItems.valueOf();
		award.addItemInfos(cfg.getAdvanceRewardItems(advanceRewardChoose.getOrDefault(cfg.getId(), 0)));
		award.rewardTakeAffectAndPush(player, Action.DYZZ_SEASON_ORDER_ACHIVE, true, null);
	}
	
	/**
	 * 全部领取
	 * @param player
	 */
	private void achiveRewardAll(Player player){
		List<DYZZSeasonOrderCfg> cfgList = HawkConfigManager.getInstance()
				.getConfigIterator(DYZZSeasonOrderCfg.class).toList();
		AwardItems award = AwardItems.valueOf();
		for(DYZZSeasonOrderCfg cfg : cfgList){
			//普通奖励
			if(this.canAchiveReward(cfg.getId(), false)){
				award.addItemInfos(cfg.getRewardItems(rewardChoose.getOrDefault(cfg.getId(), 0)));
				this.rewardLevel.add(cfg.getId());
			}
			//进阶奖励
			if(this.canAchiveReward(cfg.getId(), true)){
				award.addItemInfos(cfg.getAdvanceRewardItems(advanceRewardChoose.getOrDefault(cfg.getId(),0)));
				this.advanceRewardLevel.add(cfg.getId());
			}
		}
		if(award.getAwardItemsCount().size() > 0){
			// 发奖励
			DYZZSeasonRedisData.getInstance().updateDYZZSeasonOrder(this);
			award.rewardTakeAffectAndPush(player, Action.DYZZ_SEASON_ORDER_ACHIVE, true, null);
		}
	}

	public void sendAchiveRewardAll(Player player){
		List<DYZZSeasonOrderCfg> cfgList = HawkConfigManager.getInstance()
				.getConfigIterator(DYZZSeasonOrderCfg.class).toList();
		AwardItems award = AwardItems.valueOf();
		for(DYZZSeasonOrderCfg cfg : cfgList){
			//普通奖励
			if(this.canAchiveReward(cfg.getId(), false)){
				award.addItemInfos(cfg.getRewardItems(rewardChoose.getOrDefault(cfg.getId(), 0)));
				this.rewardLevel.add(cfg.getId());
			}
			//进阶奖励
			if(this.canAchiveReward(cfg.getId(), true)){
				award.addItemInfos(cfg.getAdvanceRewardItems(advanceRewardChoose.getOrDefault(cfg.getId(),0)));
				this.advanceRewardLevel.add(cfg.getId());
			}
		}
		if(award.getAwardItemsCount().size() > 0){
			DYZZSeasonRedisData.getInstance().updateDYZZSeasonOrder(this);
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setMailId(MailConst.MailId.DYZZ_SEASON_LAST_ORDER)
					.setPlayerId(playerId)
					.addContents(termId)
					.setRewards(award.getAwardItems())
					.setAwardStatus(Const.MailRewardStatus.NOT_GET)
					.build());
		}
	}
	/**
	 * 是否可以领奖
	 * @param level
	 * @param advance
	 * @return
	 */
	private boolean canAchiveReward(int level,boolean advance){
		DYZZSeasonOrderCfg cfg = HawkConfigManager.getInstance()
				.getConfigByKey(DYZZSeasonOrderCfg.class, level);
		if(cfg == null){
			return false;
		}
		if(cfg.getSeason() != termId){
			return false;
		}
		if(this.score >= cfg.getScore() && !this.rewardLevel.contains(level) && !advance){
			return true;
		}
		if(this.score >= cfg.getScore() && !this.advanceRewardLevel.contains(level) 
				&& advance && this.buyOrderId > 0){
			return true;
		}
		
		return false;
	}
	
	/**
	 * 更新分数
	 * @param socre
	 * @return
	 */
	public boolean updateScore(int socre){
		if(this.score < socre){
			this.score = socre;
			return true;
		}
		return false;
	}

	@Override
	public String serializ() {
		JSONObject obj = new JSONObject();
		obj.put("termId", termId);
		obj.put("playerId", playerId);
		obj.put("score", score);
		obj.put("buyOrderId", this.buyOrderId);
		
		JSONArray levelArr = new JSONArray();
		for(int level : this.rewardLevel){
			levelArr.add(level);
		}
		obj.put("rewardLevel", levelArr.toJSONString());
		
		JSONArray advanceLevelArr = new JSONArray();
		for(int level : this.advanceRewardLevel){
			advanceLevelArr.add(level);
		}
		obj.put("advanceRewardLevel", advanceLevelArr.toJSONString());
		JSONArray rewardChooseArr = new JSONArray();
		for(Map.Entry<Integer, Integer> entry : rewardChoose.entrySet()){
			JSONObject choose = new JSONObject();
			choose.put("type", 1);
			choose.put("level",entry.getKey());
			choose.put("choose",entry.getValue());
			rewardChooseArr.add(choose);
		}
		for(Map.Entry<Integer, Integer> entry : advanceRewardChoose.entrySet()){
			JSONObject choose = new JSONObject();
			choose.put("type", 2);
			choose.put("level",entry.getKey());
			choose.put("choose",entry.getValue());
			rewardChooseArr.add(choose);
		}
		obj.put("rewardChoose", rewardChooseArr.toJSONString());
		return obj.toJSONString();
	}

	@Override
	public void mergeFrom(String serialiedStr) {
		JSONObject obj = JSON.parseObject(serialiedStr);
		this.termId = obj.getIntValue("termId");
		this.playerId = obj.getString("playerId");
		this.score = obj.getIntValue("score");
		this.buyOrderId = obj.getIntValue("buyOrderId");
		
		String rewardLevelStr = obj.getString("rewardLevel");
		if(!HawkOSOperator.isEmptyString(rewardLevelStr)){
			JSONArray levelArr = JSONArray.parseArray(rewardLevelStr);
			for(int i =0; i<levelArr.size();i++){
				int level = levelArr.getInteger(i);
				this.rewardLevel.add(level);
			}
		}
		
		String advanceRewardLevelStr = obj.getString("advanceRewardLevel");
		if(!HawkOSOperator.isEmptyString(advanceRewardLevelStr)){
			JSONArray levelArr = JSONArray.parseArray(advanceRewardLevelStr);
			for(int i =0; i<levelArr.size();i++){
				int level = levelArr.getInteger(i);
				this.advanceRewardLevel.add(level);
			}
		}
		String rewardChooseStr = obj.getString("rewardChoose");
		if(!HawkOSOperator.isEmptyString(rewardChooseStr)){
			JSONArray rewardChooseArr = JSONArray.parseArray(rewardChooseStr);
			for(int i =0; i<rewardChooseArr.size();i++){
				JSONObject choose = rewardChooseArr.getJSONObject(i);
				int type = choose.getIntValue("type");
				int level = choose.getIntValue("level");
				int itemId = choose.getIntValue("choose");
				if(type == 1){
					rewardChoose.put(level, itemId);
				}else if(type == 2){
					advanceRewardChoose.put(level, itemId);
				}
			}
		}
		
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getBuyOrderId() {
		return buyOrderId;
	}

	public void setBuyOrderId(int buyOrderId) {
		this.buyOrderId = buyOrderId;
	}

	public List<Integer> getRewardLevel() {
		return rewardLevel;
	}

	
	public List<Integer> getAdvanceRewardLevel() {
		return advanceRewardLevel;
	}
	
	public int getScore() {
		return score;
	}
	
}
