package com.hawk.game.cfgElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hawk.game.config.ArmourStarExploreCollectCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.LogConst;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.ArmourStarExploreCfg;
import com.hawk.game.entity.CommanderEntity;
import com.hawk.game.protocol.Armour.ArmourStarExploreInfoSync;

/**
 * 星能探索
 * @author Golden
 *
 */
public class ArmourStarExplores {

	/**
	 * 每个球的信息
	 */
	private Map<Integer, ArmourStarExploreObj> stars = new HashMap<>();

	private Map<Integer, ArmourStarExploreCollect> collects = new HashMap<>();

	private Map<Integer, Integer> effMap = new HashMap<>();

	private int isActive = 0;
	/**
	 * 获取星球
	 * @param starId
	 * @return
	 */
	public ArmourStarExploreObj getStar(int starId) {
		return stars.get(starId);
	}

	public ArmourStarExploreCollect getCollect(int collectId) {
		return collects.get(collectId);
	}
	
	/**
	 * 序列化
	 * @param str
	 */
	public String serialize() {
		JSONArray starArr = new JSONArray();
		for (ArmourStarExploreObj info : stars.values()) {
			starArr.add(info.serialize());
		}
		return starArr.toJSONString();
	}
	
	/**
	 * 反序列化
	 * @param dataArray
	 */
	public static ArmourStarExplores unSerialize(CommanderEntity entity, String str, String collectStr) {
		ArmourStarExplores explore = new ArmourStarExplores();
		
		// 如果是空的,则初始化创建
		if (HawkOSOperator.isEmptyString(str)) {
			Map<Integer, ArmourStarExploreObj> stars = new HashMap<>();
			for (int starId = 1; starId <= ArmourStarExploreCfg.getStarCount(); starId++) {
				stars.put(starId, new ArmourStarExploreObj(starId));
			}
			explore.stars = stars;
			entity.notifyUpdate();
			return explore;
		}
		
		Map<Integer, ArmourStarExploreObj> stars = new HashMap<>();
		JSONArray starArr = JSONArray.parseArray(str);
		for (int i = 0; i < starArr.size(); i++) {
			JSONObject starObj = starArr.getJSONObject(i);
			ArmourStarExploreObj info = ArmourStarExploreObj.unSerialize(starObj);
			stars.put(info.getStarId(), info);
		}
		explore.stars = stars;

		if (!HawkOSOperator.isEmptyString(collectStr)) {
			Map<Integer, ArmourStarExploreCollect> collects = new HashMap<>();
			JSONArray collectArr = JSONArray.parseArray(collectStr);
			for (int i = 0; i < collectArr.size(); i++) {
				JSONObject collectObj = collectArr.getJSONObject(i);
				ArmourStarExploreCollect collect = ArmourStarExploreCollect.unSerialize(collectObj);
				collects.put(collect.getCollectId(), collect);
			}
			explore.collects = collects;
		}
		return explore;
	}


	public String serializeCollect() {
		JSONArray collectArr = new JSONArray();
		for (ArmourStarExploreCollect collect : collects.values()) {
			collectArr.add(collect.serialize());
		}
		return collectArr.toJSONString();
	}

	/**
	 * toPB
	 * @return
	 */
	public ArmourStarExploreInfoSync.Builder toPB(String playerId) {
		ArmourStarExploreInfoSync.Builder builder = ArmourStarExploreInfoSync.newBuilder();
		for (ArmourStarExploreObj star : stars.values()) {
			builder.addInfo(star.toPB());
		}
		for (ArmourStarExploreCollect collect : collects.values()) {
			builder.addCollectInfo(collect.toPB());
		}
		builder.setStarExploreShow(WorldPointService.getInstance().getStarExploreShow(playerId));
		builder.setIsActive(isActive);
		return builder;
	}

	public int getMaxCount(){
		int count = 0;
		for (ArmourStarExploreObj info : stars.values()) {
			if(info.isMax()){
				count++;
			}
		}
		return count;
	}

	public void checkCollect(String playerId){
		int count = getMaxCount();
		ConfigIterator<ArmourStarExploreCollectCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(ArmourStarExploreCollectCfg.class);
		for(ArmourStarExploreCollectCfg cfg : iterator){
			if(cfg.getUnlockNum() <= count && !collects.containsKey(cfg.getId())){
				collects.put(cfg.getId(), ArmourStarExploreCollect.creat(cfg.getId()));
			}
		}
		int show = WorldPointService.getInstance().getStarExploreShow(playerId);
		if(show >= 0 && count > show){
			WorldPointService.getInstance().updateStarExploreShow(playerId, count);
		}
	}

	private void mergeMap(Map<Integer, Integer> mainMap, Map<Integer, Integer> slaveMap){
		for (Map.Entry<Integer, Integer> entry : slaveMap.entrySet()) {
			if(mainMap.containsKey(entry.getKey())){
				mainMap.put(entry.getKey(), mainMap.get(entry.getKey()) + entry.getValue());
			}else {
				mainMap.put(entry.getKey(), entry.getValue());
			}
		}
	}

	public void loadEffMap(){
		try {
			Map<Integer, Integer> effMap = new HashMap<>();
			for (ArmourStarExploreObj info : stars.values()) {
				mergeMap(effMap, info.getEffectMap());
			}
			for (ArmourStarExploreCollect collect : collects.values()) {
				mergeMap(effMap, collect.getEffectMap());
			}
			this.effMap = effMap;
		}catch (Exception e){
			HawkException.catchException(e);
		}
	}

	public int getEffValue(int effId){
		return this.effMap.getOrDefault(effId, 0);
	}

	public Map<Integer, Integer> getEffMap() {
		return effMap;
	}

	public int power(){
		int power = 0;
		for (ArmourStarExploreObj info : stars.values()) {
			power += info.power();
		}
		return power;
	}

	public int getIsActive() {
		return isActive;
	}

	public void loadIsActive(String playerId) {
		try {
			String str = RedisProxy.getInstance().getRedisSession().hGet("STAR_EXPLORE_OPEN", playerId);
			if(!HawkOSOperator.isEmptyString(str)){
				isActive = 1;
			}
		}catch (Exception e){
			HawkException.catchException(e);
		}
	}

	public void active(String playerId){
		try {
			this.isActive = 1;
			RedisProxy.getInstance().getRedisSession().hSet("STAR_EXPLORE_OPEN", playerId, "1");
		}catch (Exception e){
			HawkException.catchException(e);
		}
	}

	public void logInfo(Player player){
		for (ArmourStarExploreObj star : stars.values()) {
			Map<String, Object> param = new HashMap<>();
			param.put("starId", star.getStarId());
			param.put("level", star.getCurrentLevel()); //升级后
			LogUtil.logActivityCommon(player, LogConst.LogInfoType.star_explore_up_log, param);
		}
		for (ArmourStarExploreCollect collect : collects.values()) {
			Map<String, Object> param = new HashMap<>();
			param.put("collectId", collect.getCollectId());
			param.put("fix", SerializeHelper.mapToString(collect.getFixAttrMap()));//固定
			param.put("random",SerializeHelper.mapToString(collect.getRandomAttrMap()));//随机
			LogUtil.logActivityCommon(player, LogConst.LogInfoType.star_explore_jump_log, param);
		}
	}
}
