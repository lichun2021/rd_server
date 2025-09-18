package com.hawk.activity.type.impl.submarineWar.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.submarineWar.cfg.SubmarineWarLevelCfg;
import com.hawk.activity.type.impl.submarineWar.cfg.SubmarineWarMonsterCfg;
import com.hawk.activity.type.impl.submarineWar.cfg.SubmarineWarSkillItemCfg;
import com.hawk.game.protocol.Activity.SubmarineWarGameData;
import com.hawk.game.protocol.Activity.SubmarineWarGameMonsterKill;
import com.hawk.game.protocol.Activity.SubmarineWarGameStage;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.log.Action;

public class SubmarineWarGame {
	
	public static final int PASS_TYPE_FAIL = 0;  //通过失败
	public static final int PASS_TYPE_PLAY = 1;  //通关手动
	public static final int PASS_TYPE_FAST = 2;  //通关扫荡
	
	public static final int GAME_WIN = 1;
	public static final int GAME_FAIL = 2;
	//开始时间
	private long startTime;
	//结束时间
	private long endTime;
	//技能道具ID
	private int skillItemId;
	//获得总分
	private int score;
	//结算时关卡
	private int finalStage;
	//是否通关 
	private int pass;
	//是否是强制结算
	private boolean forceOver;
	//关卡信息
	private List<SubmarineWarGameStage> stages = new ArrayList<>();
	
	
	public void startGame(){
		long curTime = HawkTime.getMillisecond();
		this.startTime = curTime;
		this.endTime = 0;
		this.skillItemId = 0;
		this.score = 0;
		this.finalStage =0;
		this.pass = 0;
		this.forceOver = false;
		stages = new ArrayList<>();
	}
	
	public void start(long starTime,int skillItemId){
		this.setStartTime(starTime);
		this.setSkillItemId(skillItemId);
	}
	
	public void startFast(long starTime,int fastLevel,int skillItemId){
		this.setStartTime(starTime);
		this.setSkillItemId(skillItemId);
		for(int level = 1;level <= fastLevel;level ++){
			SubmarineWarLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SubmarineWarLevelCfg.class, level);
    		if(Objects.isNull(cfg)){
    			continue;
    		}
    		SubmarineWarGameStage.Builder builder = SubmarineWarGameStage.newBuilder();
    		builder.setStageId(cfg.getId());
    		builder.setCase(PASS_TYPE_FAST);
    		builder.setScore(cfg.getQuickScore());
    		builder.setStageTime(0);
    		builder.setUseItemCD(0);
    		this.score += cfg.getQuickScore();
    		this.stages.add(builder.build());
		}
	}
	
	public boolean stagePass(String playerId,SubmarineWarGameStage stage,long curTime){
		boolean add = this.stagePassVerify(playerId,stage);
		if(!add){
			return false;
		}
		int next = stage.getStageId() + 1;
		SubmarineWarLevelCfg nextCfg = HawkConfigManager.getInstance().getConfigByKey(SubmarineWarLevelCfg.class, next);
		this.stages.add(stage);
		this.score += stage.getScore();
		//失败结束
		if(stage.getCase() == PASS_TYPE_FAIL){
			this.pass = GAME_FAIL;
			this.endTime = curTime;
			this.finalStage = stage.getStageId();
		}
		//手动通过结束
		if(stage.getCase() == PASS_TYPE_PLAY && Objects.isNull(nextCfg)){
			//最后一关了
			this.pass = GAME_WIN;
			this.endTime = curTime;
			this.finalStage = stage.getStageId();
		}
		HawkLog.logPrintln("SubmarineWarActivity,stagePass,playerId:{},game:{}",
                playerId, this.serializ());
		return true;
	}
	
	
	/**
	 * 数据验证
	 * @param playerId
	 * @param stage
	 * @return
	 */
	public boolean stagePassVerify(String playerId,SubmarineWarGameStage stage){
		int lastLevel = 0;
		int size = this.stages.size();
		if(size > 0){
			lastLevel = this.stages.get(size-1).getStageId();
		}
		if(stage.getStageId() != lastLevel + 1){
			HawkLog.logPrintln("SubmarineWarActivity,stagePassVerify,stageERR,playerId:{},last:{},cur:{}",
	                playerId, lastLevel,stage.getStageId());
			return false;
		}
		if(stage.getCase() != PASS_TYPE_FAIL &&
				stage.getCase() != PASS_TYPE_PLAY){
			HawkLog.logPrintln("SubmarineWarActivity,stagePassVerify,caseErr,playerId:{},case:{},",
	                playerId, stage.getCase());
			return false;
		}
		//道具使用
		boolean checkItemUse = this.checkItemUse(playerId, stage);
		if(!checkItemUse){
			HawkLog.logPrintln("SubmarineWarActivity,stagePassVerify,checkItemUse err,playerId:{}",
	                playerId);
			return false;
		}
		//验证得分
		boolean checkScore = this.checkScore(playerId, stage);
		if(!checkScore){
			HawkLog.logPrintln("SubmarineWarActivity,stagePassVerify,checkScore err,playerId:{},,score:{}",
	                playerId,stage.getScore());
			return false;
		}
		//消耗道具
		boolean costItem = this.costItem(playerId, stage);
		if(!costItem){
			HawkLog.logPrintln("SubmarineWarActivity,stagePassVerify,costItem err,playerId:{},",
	                playerId);
			return false;
		}
		return true;
	}
	
	/**
	 * 验证道具使用
	 * @param playerId
	 * @param stage
	 * @return
	 */
	public boolean checkItemUse(String playerId,SubmarineWarGameStage stage){
		if(stage.getUseItemList().size() <= 0){
			return true;
		}
		//已经使用的过的道具
		Map<Integer,Integer> itemUseMap = new HashMap<>();
		for(SubmarineWarGameStage gs : this.stages){
			List<Integer> itemlist = gs.getUseItemList();
			for(int id : itemlist){
				int count = itemUseMap.getOrDefault(id, 0);
				count += 1;
				itemUseMap.put(id, count);
			}
		}
		//此关使用数量
		Map<Integer,Integer> stageItemUse = new HashMap<>();
		for(int id : stage.getUseItemList()){
			if(id != this.skillItemId){
				return false;
			}
			int count = stageItemUse.getOrDefault(id, 0);
			count += 1;
			stageItemUse.put(id, count);
		}
		//查看是否还有剩余数量可以使用
		for(Map.Entry<Integer,Integer> entry : stageItemUse.entrySet()){
			int id = entry.getKey();
			int count = entry.getValue();
			int useBef = itemUseMap.getOrDefault(id, 0);
			SubmarineWarSkillItemCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SubmarineWarSkillItemCfg.class, id);
			if(Objects.isNull(cfg)){
				return false;
			}
			int last = cfg.getUseLimit() - useBef;
			if(count > last){
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 验证得分
	 * @param playerId
	 * @param stage
	 * @return
	 */
	public boolean checkScore(String playerId,SubmarineWarGameStage stage){
		int stageId = stage.getStageId();
		SubmarineWarLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SubmarineWarLevelCfg.class, stageId);
		if(Objects.isNull(cfg)){
			return false;
		}
		Map<Integer,Integer> kills = this.getMonsterKill(stage);
		Map<Integer,Integer>  killCfgMax = cfg.getMonsterScore();
		int killScore = 0;
		for(Map.Entry<Integer,Integer> entry : kills.entrySet()){
			int mid = entry.getKey();
			int count = entry.getValue();
			if(!killCfgMax.containsKey(mid)){
				return false;
			}
			if(count > killCfgMax.get(mid)){
				return false;
			}
			SubmarineWarMonsterCfg monsterCfg =  HawkConfigManager.getInstance().getConfigByKey(SubmarineWarMonsterCfg.class, mid);
			killScore += monsterCfg.getScore() * count;
			
		}
		if(stage.getScore() > killScore){
			return false;
		}
		return true;
	}
	
	
	
	/**
	 * 消耗道具
	 * @param playerId
	 * @param stage
	 * @return
	 */
	public boolean costItem(String playerId,SubmarineWarGameStage stage){
		if(stage.getUseItemList().size() <= 0){
			return true;
		}
		Map<Integer,Integer> itemUseMap = new HashMap<>();
		for(int id : stage.getUseItemList()){
			int count = itemUseMap.getOrDefault(id, 0);
			count += 1;
			itemUseMap.put(id, count);
		}
		List<RewardItem.Builder> itemList = new ArrayList<>();
		for (Map.Entry<Integer, Integer> entry : itemUseMap.entrySet()) {
			RewardItem.Builder item =  RewardHelper.toRewardItem(ItemType.TOOL_VALUE, entry.getKey(),entry.getValue());
			itemList.add(item);
		}
		boolean flag = ActivityManager.getInstance().getDataGeter().cost(playerId,itemList, 1, Action.SUBMARINE_WAR_GAME_SKILL_COST, false);
		return flag;
	}
	
	
	public int getNextStage(){
		int next = 1;
		int passSize = this.stages.size();
		if(passSize > 0){
			SubmarineWarGameStage stage = this.stages.get(passSize -1);
			next = stage.getStageId() +1;
		}
		return next;
	}
	
	
	public int getPassMax(){
		if(this.pass == GAME_WIN){
			return this.finalStage;
		}
		return this.finalStage -1;
	}
	
	
	public Map<Integer,Integer> getMonsterKill(){
		Map<Integer,Integer> map = new HashMap<>();
		for(SubmarineWarGameStage stage : this.stages){
			List<SubmarineWarGameMonsterKill> klist = stage.getKillsList();
			for(SubmarineWarGameMonsterKill kill : klist){
				int mid = kill.getMonsterId();
				int count = kill.getCount();
				int val = map.getOrDefault(mid, 0);
				val += count;
				map.put(mid, val);
			}
		}
		return map;
	}
	
	public Map<Integer,Integer> getMonsterKill(SubmarineWarGameStage stage){
		Map<Integer,Integer> map = new HashMap<>();
		List<SubmarineWarGameMonsterKill> klist = stage.getKillsList();
		for(SubmarineWarGameMonsterKill kill : klist){
			int mid = kill.getMonsterId();
			int count = kill.getCount();
			int val = map.getOrDefault(mid, 0);
			val += count;
			map.put(mid, val);
		}
		return map;
	}
	
	
	
	public void checkRest(String playerId){
		boolean over = this.gameOver();
		boolean stageMax = false;
		int nextStageId = this.getNextStage();
		SubmarineWarLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SubmarineWarLevelCfg.class, nextStageId);
		if(Objects.isNull(cfg)){
			stageMax = true;
		}
		//重置游戏
		if(over || stageMax){
			//错误码
            HawkLog.logPrintln("SubmarineWarActivity,gameRest,countless,playerId:{},game:{}",
                    playerId, this.serializ());
			this.startTime = 0;
			this.endTime = 0;
			this.skillItemId = 0;
			this.score = 0;
			this.finalStage =0;
			this.pass = 0;
			this.forceOver = false;
			stages = new ArrayList<>();
		}
	}
	
	
	public boolean gameOver(){
		return this.getEndTime() > 0;
	}
	
	
	public long getStartTime() {
		return startTime;
	}



	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}



	public long getEndTime() {
		return endTime;
	}



	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	
	public int getSkillItemId() {
		return skillItemId;
	}
	
	public void setSkillItemId(int skillItemId) {
		this.skillItemId = skillItemId;
	}



	public int getScore() {
		return score;
	}



	public void setScore(int score) {
		this.score = score;
	}



	public int getFinalStage() {
		return finalStage;
	}



	public void setFinalStage(int finalStage) {
		this.finalStage = finalStage;
	}



	public int getPass() {
		return pass;
	}



	public void setPass(int pass) {
		this.pass = pass;
	}



	public boolean isForceOver() {
		return forceOver;
	}



	public void setForceOver(boolean forceOver) {
		this.forceOver = forceOver;
	}



	public List<SubmarineWarGameStage> getStages() {
		return stages;
	}



	public void setStages(List<SubmarineWarGameStage> stages) {
		this.stages = stages;
	}



	public SubmarineWarGameData.Builder genBuilder(){
		SubmarineWarGameData.Builder builder = SubmarineWarGameData.newBuilder();
		builder.setStartTime(this.startTime);
		builder.setSkillItemId(this.skillItemId);
		if(!this.stages.isEmpty()){
			builder.addAllStages(this.stages);
		}
		return builder;
	}
	
	
	public String serializ(){
		JSONObject obj = new JSONObject();
		obj.put("startTime", this.startTime);
		obj.put("endTime", this.endTime);
		obj.put("skillItemId", this.skillItemId);
		obj.put("score", this.score);
		obj.put("finalStage", this.finalStage);
		obj.put("pass", this.pass);
		obj.put("forceOver", this.forceOver);
		if(this.stages.size() > 0){
			JSONArray arr = new JSONArray();
			for(SubmarineWarGameStage stage : stages){
				String value = JsonFormat.printToString(stage);
				arr.add(value);
			}
			obj.put("stages", arr.toJSONString());
		}
		return obj.toJSONString();
	}
	
	

	public void mergeFrom(String serialiedStr){
		if(HawkOSOperator.isEmptyString(serialiedStr)){
			return;
		}
		JSONObject obj = JSONObject.parseObject(serialiedStr);
		this.startTime = obj.getLongValue("startTime");
		this.endTime = obj.getLongValue("endTime");
		this.skillItemId = obj.getIntValue("skillItemId");
		this.score = obj.getIntValue("score");
		this.finalStage = obj.getIntValue("finalStage");
		this.pass = obj.getIntValue("pass");
		this.forceOver = obj.getBooleanValue("forceOver");
		List<SubmarineWarGameStage> stagesTemp = new ArrayList<>();
		if(obj.containsKey("stages")){
			String str = obj.getString("stages");
			JSONArray jarr = JSONArray.parseArray(str);
			for(int i=0;i<jarr.size();i++){
				String val = jarr.getString(i);
				SubmarineWarGameStage.Builder builder = SubmarineWarGameStage.newBuilder();
				try {
					JsonFormat.merge(val, builder);
				} catch (ParseException e) {
					HawkException.catchException(e);
				}
				stagesTemp.add(builder.build());
			}
		}
		this.stages = stagesTemp;
		
	}
	
	
}
