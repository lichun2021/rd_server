package com.hawk.game.idipscript.roleexchange;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingDeque;
import java.util.zip.GZIPOutputStream;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.cfgElement.ArmourEffObject;
import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.config.ArmourChargeLabCfg;
import com.hawk.game.config.DressCfg;
import com.hawk.game.config.EquipResearchCfg;
import com.hawk.game.config.SuperSoldierEnergyCfg;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.DressEntity;
import com.hawk.game.entity.EquipResearchEntity;
import com.hawk.game.entity.HeroEntity;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.entity.LaboratoryEntity;
import com.hawk.game.entity.SuperSoldierEntity;
import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.plantsoldier.science.PlantScience;
import com.hawk.game.module.plantsoldier.science.PlantScienceComponent;
import com.hawk.game.module.plantsoldier.strengthen.PlantSoldierSchool;
import com.hawk.game.module.plantsoldier.strengthen.soldierCrack.PlantSoldierCrack;
import com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.SoldierStrengthen;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.hero.SkillSlot;
import com.hawk.game.player.hero.TalentSlot;
import com.hawk.game.player.hero.skill.IHeroSkill;
import com.hawk.game.player.laboratory.PowerBlock;
import com.hawk.game.player.laboratory.PowerCore;
import com.hawk.game.player.laboratory.LaboratoryEnum.PowerBlockIndex;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.player.supersoldier.energy.ISuperSoldierEnergy;
import com.hawk.game.protocol.Armour.ArmourAttrType;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.player.roleexchange.XinyueConst.XinyueRoleExchangeFailReason;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 获取角色详细信息（心悦角色交易）请求 -- 10282182
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4513")
public class QueryRoleDetailInfoHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			HawkLog.errPrintln("idip/4513 QueryRoleDetailInfo error, resultCode: {}, msg: {}", result.getBody().get("Result"), result.getBody().get("RetMsg"));
			result.getBody().put("Result", XinyueRoleExchangeFailReason.ERROR_1005);
			return result;
		}
		
		int partId = request.getJSONObject("body").getIntValue("PartId");
		JSONObject json = new JSONObject();
		switch(partId) {
		case 1: // 1.个人信息：建筑工厂等级、指挥官等级、战力、去兵战力、贵族等级、金币数量
			fetchBasicInfo(player, json);
			break;
		case 2: // 2.城建兵力
			fetchBuildArmyInfo(player, json);
			break;
		case 3: // 3.英雄 ->  列出各英雄星级、等级; 点击英雄头像可查看详细信息（英雄星级、等级、技能、英雄天赋/英雄军魂信息）
			fetchHeroInfo(player, json);
			break;
		case 4: // 4.科技 -> 作战实验室:列出各科技的等级; 泰能科技实验室:列出各科技的等级
			fetchScienceInfo(player, json);
			break;
		case 5: // 5.泰能战士 -> 已破译兵种:列出已破译的兵种; 泰能进阶:列出已破译兵种的进阶等级（士官1~5、尉官1~5）
			fetchPlantSoldierInfo(player, json);
			break;
		case 6: // 6.机甲 -> 机甲等级: 列出已拥有的机甲的阶级; 赋能等级: 列出解锁了机甲赋能的机甲的赋能等级。每个模块的物理、精神赋能单独列出
			fetchMechaInfo(player, json);
			break;
		case 7: // 7.超能实验室 -> 能量矩阵最高总和等级; 折算能量源数量
			fetchSuperLabInfo(player, json);
			break;
		case 8: // 8.外显皮肤 -> 基地装扮、铭牌装扮、签名装扮、称号装扮、挂件装扮、护卫装扮、行军装扮：列出已拥有的装扮、及时限。点击可查看装扮属性
			fetchDressInfo(player, json);
			break;
		case 9: // 9.背包道具 -> 列出玩家拥有的道具及数量。点击可查看道具说明
			fetchItemInfo(player, json);
			break;
		case 10: // 10.装备 -> ........
			fetchEquipInfo(player, json);
			break;
		case 11: // 11.装备研究 -> 基础研究:列出各装备的研究进度。如：主武器：***/***; 拓展研究: 时空信标研究进度：xxx/300, 时空腰带研究进度xxx/300
			fetchEquipResearchInfo(player, json);
			break;
		default:
			break;
		}
		
		JSONObject dataJson = new JSONObject();
		dataJson.put("gameDetail", json);
		String resultData = dataJson.toJSONString();
		if (GsConfig.getInstance().isXinyueRoleEncode()) {
			try {
				//先进行gzip压缩，再编码
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
		        GZIPOutputStream gzip = new GZIPOutputStream(bos);
		        gzip.write(resultData.getBytes());
		        gzip.close();
		        
				byte[] textByte = bos.toByteArray();
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
	 * 个人信息：建筑工厂等级、指挥官等级、战力、去兵战力、贵族等级、金币数量
	 * @param player
	 * @param json
	 */
	private void fetchBasicInfo(Player player, JSONObject json) {
		long totalPower = player.getPower();
		JSONObject obj = new JSONObject();
		obj.put("cityLevel", player.getCityLevel());
		obj.put("commanderLevel", player.getLevel());
		obj.put("totalPower", totalPower);
		obj.put("excludeArmyPower", player.getNoArmyPower());
		obj.put("vipLevel", player.getVipLevel());
		obj.put("goldCount", player.getGold());
		json.put("basicInfo", obj);
	}
	
	/**
	 * 城建兵力
	 * @param player
	 * @param json
	 */
	private void fetchBuildArmyInfo(Player player, JSONObject json) {
		JSONObject obj = new JSONObject();
		obj.put("mainBuildLevel", player.getData().getBuildingMaxLevel(BuildingType.CONSTRUCTION_FACTORY_VALUE)); //建筑工厂
		obj.put("fightingFactoryBuildLevel", player.getData().getBuildingMaxLevel(BuildingType.FIGHTING_LABORATORY_VALUE)); //作战实验室
		obj.put("commandCenterBuildLevel", player.getData().getBuildingMaxLevel(BuildingType.FIGHTING_COMMAND_VALUE)); //指挥中心
		obj.put("guildBuildLevel", player.getData().getBuildingMaxLevel(BuildingType.EMBASSY_VALUE)); //联盟大厦
		obj.put("massCenterBuildLevel", player.getData().getBuildingMaxLevel(BuildingType.SATELLITE_COMMUNICATIONS_VALUE)); //集结指挥部
		obj.put("chariotFactoryBuildLevel", player.getData().getBuildingMaxLevel(BuildingType.REMOTE_FIRE_FACTORY_VALUE)); //战车工厂
		obj.put("tankBuildLevel", player.getData().getBuildingMaxLevel(BuildingType.WAR_FACTORY_VALUE)); //坦克工厂
		obj.put("airforceBuildLevel", player.getData().getBuildingMaxLevel(BuildingType.AIR_FORCE_COMMAND_VALUE)); //空指部
		obj.put("barracksBuildLevel", player.getData().getBuildingMaxLevel(BuildingType.BARRACKS_VALUE)); //兵营
		obj.put("warehouseBuildLevel", player.getData().getBuildingMaxLevel(BuildingType.WAREHOUSE_VALUE)); //仓库
		obj.put("cityWallBuildLevel", player.getData().getBuildingMaxLevel(BuildingType.CITY_WALL_VALUE)); //围墙
		obj.put("radarBuildLevel", player.getData().getBuildingMaxLevel(BuildingType.RADAR_VALUE)); //雷达
		json.put("buildingInfo", obj);
		// 各兵种及数量
		JSONArray armyArray = new JSONArray();
		for (ArmyEntity entity : player.getData().getArmyEntities()) {
			int count = entity.getTotal() - entity.getWoundedCount() - entity.getCureCount();
			if (count <= 0) {
				continue;
			}
			JSONObject armyObj = new JSONObject();
			armyObj.put("armyId", entity.getArmyId());
			armyObj.put("count", count);
			armyArray.add(armyObj);
		}
		json.put("armyInfo", armyArray);
	}
	
	/**
	 * 英雄 ->  列出各英雄星级、等级; 点击英雄头像可查看详细信息（英雄星级、等级、技能、英雄天赋/英雄军魂信息）
	 * @param player
	 * @param json
	 */
	private void fetchHeroInfo(Player player, JSONObject json) {
		JSONArray heroArray = new JSONArray();
		for (HeroEntity entity : player.getData().getHeroEntityList()) {
			PlayerHero hero = entity.getHeroObj();
			JSONObject heroObj = new JSONObject();
			heroObj.put("heroId", entity.getHeroId());
			heroObj.put("level", hero.getLevel());
			heroObj.put("star", entity.getStar());
			// 英雄技能
			if (hero.getSkillSlots() != null) {
				JSONArray skillArray = new JSONArray();
				for (SkillSlot skillSlot : hero.getSkillSlots()) {
					IHeroSkill skill = skillSlot.getSkill();
					if (skill == null || skill.getCfg() == null) {
						continue;
					}
					JSONObject skillObj = new JSONObject();
					skillObj.put("skillId", skill.getCfg().getSkillId());
					skillObj.put("skillLevel", skill.getLevel());
					skillArray.add(skillObj);
				}
				heroObj.put("skill", skillArray);
			}
			
			// 英雄天赋
			if (hero.getTalentSlots() != null) {
				JSONArray talentArray = new JSONArray();
				for (TalentSlot slot : hero.getTalentSlots()) {
					if (slot.getTalent() == null) {
						continue;
					}
					JSONObject talentObj = new JSONObject();
					talentObj.put("talentId", slot.getTalent().getSkillID());
					talentObj.put("talentExp", slot.getTalent().getExp());
					talentArray.add(talentObj);
				}
				heroObj.put("talent", talentArray);
			}
			
			// 英雄军魂信息
			if (hero.getSoul() != null && hero.getSoul().getSoulLevel() != null) {
				JSONArray heroSoulArray = new JSONArray();
				for (Entry<Integer, Integer> entry : hero.getSoul().getSoulLevel().entrySet()) {
					JSONObject soulObj = new JSONObject();
					soulObj.put("stage", entry.getKey());
					soulObj.put("level", entry.getValue());
					heroSoulArray.add(soulObj);
				}
				heroObj.put("soul", heroSoulArray);
			}
			heroArray.add(heroObj);
		}
		json.put("heroInfo", heroArray);
	}
	
	/**
	 * 科技 -> 作战实验室:列出各科技的等级; 泰能科技实验室:列出各科技的等级
	 * @param player
	 * @param json
	 */
	private void fetchScienceInfo(Player player, JSONObject json) {
		JSONObject sobj = new JSONObject();
		JSONArray commonScienceArray = new JSONArray();
		for (TechnologyEntity entity : player.getData().getTechnologyEntities()) {
			if (entity.getLevel() <= 0) {
				continue;
			}
			JSONObject obj = new JSONObject();
			obj.put("scienceId", entity.getTechId());
			obj.put("level", entity.getLevel());
			commonScienceArray.add(obj);
		}
		sobj.put("commonScience", commonScienceArray);
		JSONArray plantScienceArray = new JSONArray();
		PlantScience sciencObj = player.getData().getPlantScienceEntity().getSciencObj();
		if (sciencObj != null) {
			for (PlantScienceComponent component : sciencObj.getComponents().values()) {
				if (component.getLevel() <= 0) {
					continue;
				}
				JSONObject obj = new JSONObject();
				obj.put("scienceId", component.getScienceId());
				obj.put("level", component.getLevel());
				plantScienceArray.add(obj);
			}
			sobj.put("plantScience", plantScienceArray);
		}
		json.put("scienceInfo", sobj);
	}
	
	/**
	 * 泰能战士 -> 已破译兵种:列出已破译的兵种; 泰能进阶:列出已破译兵种的进阶等级（士官1~5、尉官1~5）
	 * @param player
	 * @param json
	 */
	private void fetchPlantSoldierInfo(Player player, JSONObject json) {
		JSONObject sobj = new JSONObject();
		JSONArray plantSoldierArray = new JSONArray();
		PlantSoldierSchool school = player.getData().getPlantSoldierSchoolEntity().getPlantSchoolObj();
		for (PlantSoldierCrack crack : school.getCracks()) {
			if (crack.isUnlock() && crack.isMax()) {
				plantSoldierArray.add(crack.getCfgId());
			}
		}
		sobj.put("unlockedPlantSoldier", plantSoldierArray);
		
		JSONArray plantStrengthLevelArray = new JSONArray();
		for (SoldierStrengthen crack : school.getStrengthens()) {
			if (crack.getPlantStrengthLevel() > 0) {
				JSONObject obj = new JSONObject();
				obj.put("cfgId", crack.getCfgId());
				obj.put("level", crack.getPlantStrengthLevel());
				plantStrengthLevelArray.add(obj);
			}
		}
		sobj.put("plantStrengthLevel", plantStrengthLevelArray);
		json.put("plantSoldierInfo", sobj);
	}
	
	/**
	 * 机甲 -> 机甲等级: 列出已拥有的机甲的阶级; 赋能等级: 列出解锁了机甲赋能的机甲的赋能等级。每个模块的物理、精神赋能单独列出
	 * @param player
	 * @param json
	 */
	private void fetchMechaInfo(Player player, JSONObject json) {
		JSONArray mechaArray = new JSONArray();
		for(SuperSoldierEntity entity : player.getData().getSuperSoldierEntityList()) {
			JSONObject obj = new JSONObject();
			obj.put("mechaId", entity.getSoldierId());
			obj.put("star", entity.getStar());
			obj.put("step", entity.getStep());
			
			SuperSoldier superSoldier = entity.getSoldierObj();
			if (superSoldier.getSoldierEnergy() != null && superSoldier.getSoldierEnergy().getEnergys() != null) {
				JSONArray array = new JSONArray();
				for (ISuperSoldierEnergy eng : superSoldier.getSoldierEnergy().getEnergys()) {
					SuperSoldierEnergyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierEnergyCfg.class, eng.getCfgId());
					JSONObject energyObj = new JSONObject();
					energyObj.put("energyId", eng.getCfgId());
					//energyObj.put("type", cfg.getEnablingType());
					energyObj.put("pos", cfg.getEnablingPosition());
					energyObj.put("level", cfg.getEnablingLevel());
					array.add(energyObj);
				}
				obj.put("energy", array);
			}
			
			mechaArray.add(obj);
		}
		json.put("mechaInfo", mechaArray);
	}
	
	/**
	 * 超能实验室 -> 能量矩阵最高总和等级; 折算能量源数量
	 * 
	 * @param player
	 * @param json
	 */
	private void fetchSuperLabInfo(Player player, JSONObject json) {
		JSONArray superLabArray = new JSONArray();
		for (LaboratoryEntity entity : player.getData().getLaboratoryEntityList()) {
			JSONArray array = new JSONArray();
			int energyNumTotal = 0, levelTotal = 0;
			for(PowerCore core : entity.getLabObj().getPowerCores().values()) {
				JSONObject coreObj = new JSONObject();
				coreObj.put("index", core.getType().INT_VAL); //能量核心类型
				coreObj.put("id", core.getCoreCfgId()); //能量核心ID
				coreObj.put("level", core.getLevel());  //能量核心等级
				array.add(coreObj);
				levelTotal += core.getLevel();
				energyNumTotal += core.getLockEnergyNum();
			}
			if (energyNumTotal <= 0) {
				continue;
			}
			JSONObject obj = new JSONObject();
			obj.put("pageIndex", entity.getPageIndex()); //能量矩阵ID
			obj.put("pageLabs", array); //能量核心信息
			obj.put("pageLabLevelTotal", levelTotal); //能量核心等级总和
			obj.put("pageLabEnergyNumTotal", energyNumTotal); //能量核心锁定的能量源总数
			
			JSONArray talentArray = new JSONArray();
			PowerBlock block = entity.getLabObj().getPowerBlock();
			for (Entry<PowerBlockIndex, Integer> bi : block.getTalentMap().entrySet()) {
				if (!block.isIndexUnlock(bi.getKey())) {
					continue;
				}
				JSONObject talentObj = new JSONObject();
				talentObj.put("index", bi.getKey().INT_VAL); //超能核心槽位ID
				talentObj.put("cfgId", bi.getValue());  //超能核心槽位锁定的属性ID
				talentArray.add(talentObj);
			}
			
			obj.put("talent", talentArray); // 超能核心信息
			superLabArray.add(obj);
		}
		
		json.put("laboratoryInfo", superLabArray);
	}
	
	/**
	 * 外显皮肤 -> 基地装扮、铭牌装扮、签名装扮、称号装扮、挂件装扮、护卫装扮、行军装扮：列出已拥有的装扮、及时限。点击可查看装扮属性
	 * @param player
	 * @param json
	 */
	private void fetchDressInfo(Player player, JSONObject json) {
		DressEntity dressEntity = player.getData().getDressEntity();
		BlockingDeque<DressItem> dressInfos = dressEntity.getDressInfo();
		Map<Integer, List<DressItem>> map = new HashMap<>();
		for (DressItem dressInfo : dressInfos) {
			List<DressItem> list = map.get(dressInfo.getDressType());
			if (list == null) {
				list = new ArrayList<>();
				map.put(dressInfo.getDressType(), list);
			}
			list.add(dressInfo);
		}
		
		long nowTime = HawkTime.getMillisecond();
		JSONArray array = new JSONArray();
		for (Entry<Integer, List<DressItem>> entry : map.entrySet()) {
			JSONObject dressTypeObj = new JSONObject();
			dressTypeObj.put("dressType", entry.getKey());
			JSONArray innArray = new JSONArray();
			for(DressItem dressItem : entry.getValue()) {
				long endTime = dressItem.getStartTime() + dressItem.getContinueTime();
				if (endTime <= nowTime) {
					continue;
				}
				DressCfg dressCfg = AssembleDataManager.getInstance().getDressCfg(dressItem.getDressType(), dressItem.getModelType());
				JSONObject obj = new JSONObject();
				obj.put("dressId", dressCfg == null ? 0 : dressCfg.getDressId());
				obj.put("modelType", dressItem.getModelType());
				obj.put("endTime", endTime);
				innArray.add(obj);
			}
			dressTypeObj.put("dressDetail", innArray);
			array.add(dressTypeObj);
		}
		json.put("dressInfo", array);
	}
	
	/**
	 * 背包道具 -> 列出玩家拥有的道具及数量。点击可查看道具说明
	 * @param player
	 * @param json
	 */
	private void fetchItemInfo(Player player, JSONObject json) {
		JSONArray array = new JSONArray();
		for (ItemEntity entity : player.getData().getItemEntities()) {
			if (entity.getItemCount() <= 0) {
				continue;
			}
			JSONObject obj = new JSONObject();
			obj.put("itemId", entity.getItemId());
			obj.put("count", entity.getItemCount());
			array.add(obj);
		}
		json.put("itemsInfo", array);
	}
	
	/**
	 * 装备 -> ........
	 * 
	 * 所属兵种：X
	 * 改装强化等级：X
	 * 泰晶聚能等级：X
	 * 量子聚变等级：X
	 * 附加属性：xxx\xxx\xxx\xx
	 * 泰能属性：xxx\xxx\xxx\xx
	 * 
	 * @param player
	 * @param json
	 */
	private void fetchEquipInfo(Player player, JSONObject json) {
		JSONArray array = new JSONArray();
		int suitCount = player.getEntity().getArmourSuitCount();
		for (int suitIndex = 1; suitIndex <= suitCount; suitIndex++) {
			JSONObject suitObj = new JSONObject();
			JSONArray suitArmourArray = new JSONArray();
			suitObj.put("suitId", suitIndex);
			suitObj.put("suitArmour", suitArmourArray);
			Map<Integer, String> map = player.getArmourSuit(suitIndex);
			for (String id : map.values()) {
				ArmourEntity armour = player.getData().getArmourEntity(id);
				JSONObject obj = new JSONObject();
				obj.put("armourId", armour.getId());
				obj.put("cfgId", armour.getArmourId());
				obj.put("level", armour.getLevel());
				obj.put("quality", armour.getQuality());
				obj.put("quantumLevel", armour.getQuantum() > 0 ? armour.getQuantum() : 0);
				
				if (armour.getStar() > 0) {
					obj.put("starLevel", armour.getStar());
					JSONArray starAttrArray = new JSONArray();
					for (ArmourEffObject starEff : armour.getStarEff()) {
						ArmourChargeLabCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ArmourChargeLabCfg.class, starEff.getAttrId());
						EffectObject attributeValue = cfg.getAttributeValue();
						JSONObject starObj = new JSONObject();
						starObj.put("attrId", starEff.getAttrId());
						starObj.put("attrType", attributeValue.getEffectType());
						starObj.put("attrValue", attributeValue.getEffectValue());
						starObj.put("rate", starEff.getRate());
						starObj.put("replaceAttrId", starEff.getReplaceAttrId());
						starObj.put("type", ArmourAttrType.STAR_EXTR_VALUE);
						starObj.put("breakthrough", starEff.getBreakthrough());
						starAttrArray.add(starObj);
					}
					obj.put("starAttr", starAttrArray);
				}
				
				// 额外属性
				JSONArray extraAttrArray = new JSONArray();
				for (ArmourEffObject extrEff : armour.getExtraAttrEff()) {
					JSONObject extraAttrObj = new JSONObject();
					extraAttrObj.put("attrId", extrEff.getAttrId());
					extraAttrObj.put("attrType", extrEff.getEffectType());
					extraAttrObj.put("attrValue", extrEff.getEffectValue());
					int attrType = armour.isSuper() ? ArmourAttrType.SUPER_EXTR_VALUE : ArmourAttrType.EXTR_VALUE;
					extraAttrObj.put("type", attrType);
					extraAttrArray.add(extraAttrObj);
				}
				obj.put("extraAttr", extraAttrArray);
				
				// 特技属性
				JSONArray skillAttrArray = new JSONArray();
				for (ArmourEffObject skillEff : armour.getSkillEff()) {
					JSONObject skillAttrObj = new JSONObject();
					skillAttrObj.put("attrId", skillEff.getAttrId());
					skillAttrObj.put("attrType", skillEff.getEffectType());
					skillAttrObj.put("attrValue", skillEff.getEffectValue());
					skillAttrObj.put("type", ArmourAttrType.SPECIAL_VALUE);
					skillAttrArray.add(skillAttrObj);
				}
				obj.put("skillAttr", skillAttrArray);
				suitArmourArray.add(obj);
			}
			
			array.add(suitObj);
		}
		
		json.put("armourInfo", array);
	}
	
	/**
	 * 装备研究 -> 基础研究:列出各装备的研究进度。如：主武器：***\/***; 拓展研究: 时空信标研究进度：xxx/300, 时空腰带研究进度xxx/300
	 * @param player
	 * @param json
	 */
	private void fetchEquipResearchInfo(Player player, JSONObject json) {
		JSONObject eobj = new JSONObject();
		JSONArray basicArray = new JSONArray();
		JSONArray extendArray = new JSONArray();
		List<EquipResearchEntity> equipResearchEntityList = player.getData().getEquipResearchEntityList();
		for (EquipResearchEntity researchEntity : equipResearchEntityList) {
			JSONObject obj = new JSONObject();
			obj.put("researchId", researchEntity.getResearchId());
			obj.put("level", researchEntity.getResearchLevel());
			EquipResearchCfg researchCfg = HawkConfigManager.getInstance().getConfigByKey(EquipResearchCfg.class, researchEntity.getResearchId());
			if (researchCfg != null && researchCfg.getPhaseTwo() > 0) {
				extendArray.add(obj);
			} else {
				basicArray.add(obj);
			}
		}
		
		eobj.put("basic", basicArray);
		eobj.put("extend", extendArray);
		json.put("equipResearchInfo", eobj);
	}
	
}
