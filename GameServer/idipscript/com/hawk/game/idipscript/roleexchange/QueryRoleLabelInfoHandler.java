package com.hawk.game.idipscript.roleexchange;

import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.config.EquipResearchCfg;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.entity.EquipResearchEntity;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.module.plantsoldier.strengthen.PlantSoldierSchool;
import com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.SoldierStrengthen;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.Dress.DressType;
import com.hawk.game.util.GsConst;
import com.hawk.game.player.roleexchange.XinyueConst.XinyueRoleExchangeFailReason;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 获取角色标签信息（心悦角色交易）请求 -- 10282183
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4515")
public class QueryRoleLabelInfoHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			HawkLog.errPrintln("idip/4515 QueryRoleLabelInfo error, resultCode: {}, msg: {}", result.getBody().get("Result"), result.getBody().get("RetMsg"));
			result.getBody().put("Result", XinyueRoleExchangeFailReason.ERROR_1005);
			return result;
		}
		
		JSONObject json = new JSONObject();
		JSONObject subJson = new JSONObject();
		json.put("gameTags", subJson);
		JSONArray allTagsArr = new JSONArray();
		subJson.put("allTags", allTagsArr);
		
		/** 标签文本	        达成条件             展示优先级  */
		/** 满红装	任意一套装备的量子聚变等级均为60级	  1 */
		manHongZhuangTagsCollect(player, subJson, allTagsArr, "manhongzhuang", "满红装", "manhongzhuangTags", 60);
		
		/** 满泰能	任意泰能战士达到尉官5阶	2 */
		manTainengTagsCollect(player, subJson, allTagsArr, "mantaineng", "满泰能", "mantainengTags", 5);
		
		/** 满研究	装备的拓展研究，2个都达到300级	3 */
		researchTagsCollect(player, subJson, allTagsArr, "manyanjiu", "满研究", "manyanjiuTags", 300);
		
		/** 多装扮	永久的基地装扮数量大于等于15个	4 */
		dressTagsCollect(player, subJson, allTagsArr, "duozhuangban", "多装扮", "duozhuangbanTags", 15);
		
		/** 高等级	指挥官等级大于等于56级	        5 */
		roleLevelTagsCollect(player, subJson, allTagsArr, "gaodengji", "高等级", "gaodengjiTags", 56);
		
		/** 高战力	战斗力大于等于5000万	            6 */
		powerTagsCollect(player, subJson, allTagsArr, "gaozhanli", "高战力", "gaozhanliTags", 50000000);
		
		/** N红	 红色英雄数量=N，N取1~100，设置100个标签     7 */
		someRedHeroCollect(player, subJson, allTagsArr, "someredheros", "红", "someredherosTags", 0);
		
		String resultData = json.toJSONString();
		if (GsConfig.getInstance().isXinyueRoleEncode()) {
			try {
				byte[] textByte = resultData.getBytes("UTF-8");
				resultData = Base64.getEncoder().encodeToString(textByte);
				resultData = URLEncoder.encode(resultData, "UTF-8");
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		result.getBody().put("Data", resultData);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	/**
	 * 判断满红装（任意一套装备的量子聚变等级均为60级）
	 */
	private void manHongZhuangTagsCollect(Player player, JSONObject json, JSONArray allTagsArr, String typeStr, String name, String tagName, int... dimensionVals) {
		int suitCount = player.getEntity().getArmourSuitCount();
		for (int suitIndex = 1; suitIndex <= suitCount; suitIndex++) {
			Map<Integer, String> map = player.getArmourSuit(suitIndex);
			if (map.isEmpty()) {
				continue;
			}
			boolean touchCondition = true;
			for (String id : map.values()) {
				ArmourEntity armour = player.getData().getArmourEntity(id);
				if (armour.getQuantum() < dimensionVals[0]) {
					touchCondition = false;
				}
			}
			if (touchCondition) {
				assemble(json, allTagsArr, typeStr, name, tagName, dimensionVals);
				break;
			}
		}
	}
	
	/**
	 * 满泰能判断（任意泰能战士达到尉官5阶）
	 */
	private void manTainengTagsCollect(Player player, JSONObject json, JSONArray allTagsArr, String typeStr, String name, String tagName, int... dimensionVals) {
		boolean plantStrengthFull = false;
		PlantSoldierSchool school = player.getData().getPlantSoldierSchoolEntity().getPlantSchoolObj();
		for (SoldierStrengthen crack : school.getStrengthens()) {
			if (crack.getPlantStrengthLevel() >= dimensionVals[0]) {
				plantStrengthFull = true;
				break;
			}
		}
		if (plantStrengthFull) {
			assemble(json, allTagsArr, typeStr, name, tagName, dimensionVals);
		}
	}
	
	/**
	 * 满研究判断（装备的拓展研究，2个都达到300级）
	 */
	private void researchTagsCollect(Player player, JSONObject json, JSONArray allTagsArr, String typeStr, String name, String tagName, int... dimensionVals) {
		Map<Integer, Integer> researchLevelMap = new HashMap<>();
		List<EquipResearchEntity> equipResearchEntityList = player.getData().getEquipResearchEntityList();
		for (EquipResearchEntity researchEntity : equipResearchEntityList) {
			EquipResearchCfg researchCfg = HawkConfigManager.getInstance().getConfigByKey(EquipResearchCfg.class, researchEntity.getResearchId());
			if (researchCfg != null && researchCfg.getPhaseTwo() > 0) {
				int totalLevel = researchLevelMap.getOrDefault(researchCfg.getId(), 0);
				totalLevel += researchEntity.getResearchLevel();
				researchLevelMap.put(researchCfg.getId(), totalLevel);
			}
		}
		boolean researchLevelTouch = researchLevelMap.isEmpty() ? false : true;
		for (int lvl : researchLevelMap.values()) {
			if (lvl < dimensionVals[0]) {
				researchLevelTouch = false;
				break;
			}
		}
		if (researchLevelTouch) {
			assemble(json, allTagsArr, typeStr, name, tagName, dimensionVals);
		}
	}
	
	/**
	 * 多装扮判断（永久的基地装扮数量大于等于15个）
	 */
	private void dressTagsCollect(Player player, JSONObject json, JSONArray allTagsArr, String typeStr, String name, String tagName, int... dimensionVals) {
		int dressCount = 0;
		BlockingDeque<DressItem> dressInfos = player.getData().getDressEntity().getDressInfo();
		for (DressItem dressInfo : dressInfos) {
			if (dressInfo.getDressType() == DressType.DERMA_VALUE && dressInfo.getContinueTime() >= GsConst.PERPETUAL_MILL_SECOND) {
				dressCount += 1;
			}
		}
		if (dressCount >= dimensionVals[0]) {
			assemble(json, allTagsArr, typeStr, name, tagName, dimensionVals);
		}
	}
	
	/**
	 * 高等级判断（指挥官等级大于等于56级）
	 */
	private void roleLevelTagsCollect(Player player, JSONObject json, JSONArray allTagsArr, String typeStr, String name, String tagName, int... dimensionVals) {
		if (player.getLevel() < dimensionVals[0]) {
			return;
		}
		assemble(json, allTagsArr, typeStr, name, tagName, dimensionVals);
	}
	
	/**
	 * 高战力判断（战斗力大于等于5000万）
	 */
	private void powerTagsCollect(Player player, JSONObject json, JSONArray allTagsArr, String typeStr, String name, String tagName, int... dimensionVals) {
		if (player.getPower() < dimensionVals[0]) {
			return;
		}
		assemble(json, allTagsArr, typeStr, name, tagName, dimensionVals);
	}
	
	/**
	 * 红色英雄数量判断（红色英雄数量=N，N取1~100，设置100个标签）
	 */
	private void someRedHeroCollect(Player player, JSONObject json, JSONArray allTagsArr, String typeStr, String name, String tagName, int... dimensionVals) {
		int redHeroCount = 0;
		for (HeroEntity entity : player.getData().getHeroEntityList()) {
			PlayerHero hero = entity.getHeroObj();
			if (hero.getConfig().getQualityColor() == 6) {
				redHeroCount += 1;
			}
		}
		if (redHeroCount > 0) {
			assemble(json, allTagsArr, typeStr, redHeroCount+name, tagName, dimensionVals);
		}
	}
	
	private void assemble(JSONObject subJson, JSONArray allTagsArr, String typeStr, String name, String tagName, int... dimensionVals) {
		JSONArray array = new JSONArray();
		JSONObject obj = new JSONObject();
		obj.put("type", typeStr);
		obj.put("name", name);
		JSONArray dimensionArr = new JSONArray();
		for (int val : dimensionVals) {
			dimensionArr.add(val);
		}
		obj.put("dimension", dimensionArr);
		array.add(obj);
		allTagsArr.add(obj.clone());
		subJson.put(tagName, array);
	}
	
}
