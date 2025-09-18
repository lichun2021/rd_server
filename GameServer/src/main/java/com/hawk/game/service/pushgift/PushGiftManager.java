package com.hawk.game.service.pushgift;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

import com.hawk.game.config.PushGiftGroupCfg;
import com.hawk.game.config.PushGiftLevelCfg;
import com.hawk.game.entity.PlayerGiftEntity;
import com.hawk.game.entity.PushGiftEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.PushGift.PushGiftOper;
import com.hawk.game.protocol.PushGift.PushGiftSynInfo;
import com.hawk.game.protocol.PushGift.PushGiftUpdate;
import com.hawk.game.service.pushgift.impl.PushGiftBuildingCondition;
import com.hawk.game.service.pushgift.impl.PushGiftCommanderCondtion;
import com.hawk.game.service.pushgift.impl.PushGiftCommanderExpCondition;
import com.hawk.game.service.pushgift.impl.PushGiftCondition2700;
import com.hawk.game.service.pushgift.impl.PushGiftCondition2800;
import com.hawk.game.service.pushgift.impl.PushGiftCondition2900;
import com.hawk.game.service.pushgift.impl.PushGiftCondition3000;
import com.hawk.game.service.pushgift.impl.PushGiftCondition3100;
import com.hawk.game.service.pushgift.impl.PushGiftCondition3200;
import com.hawk.game.service.pushgift.impl.PushGiftCondition3300;
import com.hawk.game.service.pushgift.impl.PushGiftCondition3400;
import com.hawk.game.service.pushgift.impl.PushGiftCondition3500;
import com.hawk.game.service.pushgift.impl.PushGiftCondition3600;
import com.hawk.game.service.pushgift.impl.PushGiftCondition3800;
import com.hawk.game.service.pushgift.impl.PushGiftCondition3900;
import com.hawk.game.service.pushgift.impl.PushGiftCureSpeedCondition;
import com.hawk.game.service.pushgift.impl.PushGiftEquipEnhanceCondition;
import com.hawk.game.service.pushgift.impl.PushGiftEquipLevelUpCondition;
import com.hawk.game.service.pushgift.impl.PushGiftEquipResearchCondition;
import com.hawk.game.service.pushgift.impl.PushGiftEquipResearchLevelCondition;
import com.hawk.game.service.pushgift.impl.PushGiftEquipResearchUnlockCondition;
import com.hawk.game.service.pushgift.impl.PushGiftHeroLevelUpCondition;
import com.hawk.game.service.pushgift.impl.PushGiftHeroStarUpCondition;
import com.hawk.game.service.pushgift.impl.PushGiftItemConsumeCondition;
import com.hawk.game.service.pushgift.impl.PushGiftLaboratoryLevelCondition;
import com.hawk.game.service.pushgift.impl.PushGiftOneKeyHeroSkillUpCondition;
import com.hawk.game.service.pushgift.impl.PushGiftOneKeyHeroUpCondition;
import com.hawk.game.service.pushgift.impl.PushGiftPveAttackFailCondtion;
import com.hawk.game.service.pushgift.impl.PushGiftPveAttackFailExtraCondtion;
import com.hawk.game.service.pushgift.impl.PushGiftPvpAttackFailCondition;
import com.hawk.game.service.pushgift.impl.PushGiftPvpAttackFailExtraCondition;
import com.hawk.game.service.pushgift.impl.PushGiftPvpDefenceFailCondition;
import com.hawk.game.service.pushgift.impl.PushGiftPvpDefenceFailExtraCondition;
import com.hawk.game.service.pushgift.impl.PushGiftSuperLabLvUpCondition;
import com.hawk.game.service.pushgift.impl.PushGiftTrainSpeedCondition;
import com.hawk.game.service.pushgift.impl.PushGiftUnlockHeroCondtion;
import com.hawk.game.service.pushgift.impl.PushGiftYQZZNationHospitalCondtion;
import com.hawk.game.service.pushgift.impl.SpecialCondition;
import com.hawk.game.util.BuilderUtil;

public class PushGiftManager {
	private Map<String, AbstractPushGiftCondition> typeConditionMap = new HashMap<>();

	/**
	 * 触发礼包次数(只记录终身限购的) <playerId,<giftGroupId, times>>
	 */
	private Map<String, Map<Integer, Integer>> touchedGift = new ConcurrentHashMap<>();
	
	private static PushGiftManager instance = new PushGiftManager();
	
	public static PushGiftManager getInstance() {
		return instance;
	}
	
	public  AbstractPushGiftCondition getCondition(Integer type) {
		return typeConditionMap.get(type+""); 
	}
	
	public AbstractPushGiftCondition getCondition(Integer type, PushGiftConditionFromEnum from) {
		if (from == null) {
			return typeConditionMap.get(type+""); 
		} else {
			return typeConditionMap.get(type+""+from);
		}
	}
	
	public  void init() {
		this.registerCondition(new PushGiftCommanderCondtion());
		this.registerCondition(new PushGiftHeroLevelUpCondition());
		this.registerCondition(new PushGiftHeroStarUpCondition());
		this.registerCondition(new PushGiftBuildingCondition());
		this.registerCondition(new PushGiftPveAttackFailCondtion(), PushGiftConditionFromEnum.PVE_FAIL);
		this.registerCondition(new PushGiftPvpAttackFailCondition(), PushGiftConditionFromEnum.PVP_ATK_FAIL);
		this.registerCondition(new PushGiftPvpDefenceFailCondition(), PushGiftConditionFromEnum.PVP_DEF_FAIL);
		this.registerCondition(new PushGiftUnlockHeroCondtion());
		this.registerCondition(new PushGiftOneKeyHeroUpCondition());
		this.registerCondition(new PushGiftOneKeyHeroSkillUpCondition());
		this.registerCondition(new PushGiftPveAttackFailExtraCondtion(), PushGiftConditionFromEnum.PVE_FAIL);
		this.registerCondition(new PushGiftPvpAttackFailExtraCondition(), PushGiftConditionFromEnum.PVP_ATK_FAIL);
		this.registerCondition(new PushGiftPvpDefenceFailExtraCondition(), PushGiftConditionFromEnum.PVP_DEF_FAIL);
		this.registerCondition(new SpecialCondition());
		
		this.registerCondition(new PushGiftTrainSpeedCondition());
		this.registerCondition(new PushGiftCureSpeedCondition());
		this.registerCondition(new PushGiftItemConsumeCondition());
		this.registerCondition(new PushGiftSuperLabLvUpCondition());
		this.registerCondition(new PushGiftEquipResearchCondition());
		this.registerCondition(new PushGiftEquipEnhanceCondition());
		this.registerCondition(new PushGiftCommanderExpCondition());
		
		this.registerCondition(new PushGiftEquipResearchUnlockCondition());
		this.registerCondition(new PushGiftEquipResearchLevelCondition());
		this.registerCondition(new PushGiftEquipLevelUpCondition());
		this.registerCondition(new PushGiftLaboratoryLevelCondition());
		
		this.registerCondition(new PushGiftCondition2700());
		this.registerCondition(new PushGiftCondition2800());
		this.registerCondition(new PushGiftCondition3300());
		this.registerCondition(new PushGiftCondition3400());
		this.registerCondition(new PushGiftCondition3500());
		this.registerCondition(new PushGiftCondition3600());
		this.registerCondition(new PushGiftCondition2900());
		this.registerCondition(new PushGiftCondition3000());
		this.registerCondition(new PushGiftCondition3100());
		this.registerCondition(new PushGiftCondition3200());
		this.registerCondition(new PushGiftCondition3800());
		this.registerCondition(new PushGiftCondition3900());
		this.registerCondition(new PushGiftYQZZNationHospitalCondtion());
		
		//这段校验只能是放在这里 所以在reload的时候是没有办法校验到的 注意!!!!!!!!!!!!!!!!!!!!!!!
		ConfigIterator<PushGiftGroupCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(PushGiftGroupCfg.class);
		PushGiftGroupCfg pushGiftGroupCfg = null;
		while(configIterator.hasNext()) {
			pushGiftGroupCfg = configIterator.next();
			this.isValidConditionType(pushGiftGroupCfg.getConditionType());
		}
	}
	
	private void registerCondition(AbstractPushGiftCondition condition, PushGiftConditionFromEnum from) {
		Integer type = condition.getConditionType();
		String key = "";
		if (from != null) {
			key = from.toString();
		}
		
		AbstractPushGiftCondition oldCondition = typeConditionMap.put(type+""+key,  condition);
		if (oldCondition != null) {
			throw new InvalidParameterException("push gift condition dumplicate "+type);
		}
	}
	
	private void registerCondition(AbstractPushGiftCondition condition) {
		this.registerCondition(condition, null);
	}
	
	public void updatePushGiftList(Player player, List<Integer> idList, PushGiftOper oper) {
		Integer curTime = HawkTime.getSeconds();
		PushGiftEntity pushGiftEntity = player.getData().getPushGiftEntity();
		Integer endTime = null;
		Map<Integer, Integer> map = new HashMap<>();
		for (Integer id : idList) {
			endTime = pushGiftEntity.getGiftIdTimeMap().get(id);
			if (endTime == null) {
				endTime = curTime;
			} 
			map.put(id, endTime);
		}
		
		updatePushGiftList(player, map, oper);
	}
	
	public void updatePushGiftList(String playerId, List<Integer> idList , PushGiftOper oper) {
		Player player = GlobalData.getInstance().getActivePlayer(playerId);
		if (player == null) {
			return;
		}
		
		updatePushGiftList(player, idList, oper);
	}
	
	public void updatePushGiftList(String playerId, Map<Integer,Integer> map, PushGiftOper oper) {
		Player player = GlobalData.getInstance().getActivePlayer(playerId);
		if (player != null) {
			this.updatePushGiftList(player, map, oper);
		}		
	}
	
	public void updatePushGiftList(Player player, Map<Integer,Integer> map, PushGiftOper oper) {
		PushGiftUpdate.Builder sbuilder = PushGiftUpdate.newBuilder();
		for (Entry<Integer, Integer> entry : map.entrySet()) {
			sbuilder.addPushGifts(BuilderUtil.buildPushGiftMsg(entry.getKey(), entry.getValue() * 1000l));
		}		
		sbuilder.setOper(oper);
		
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.PUSH_GIFT_UPDATE_VALUE, sbuilder);
		player.sendProtocol(protocol);
		
		if (map.size() > 0) {
			// 触发推送礼包的话，重置推荐礼包的cd
			PlayerGiftEntity playerGiftEntity = player.getData().getPlayerGiftEntity();
			playerGiftEntity.resetGiftAdviceCd();
		}
	}
	
	public void synPushGiftList(Player player, PushGiftEntity entity) {
		PushGiftSynInfo.Builder sbuilder = PushGiftSynInfo.newBuilder();
		for (Entry<Integer, Integer>entry : entity.getGiftIdTimeMap().entrySet()) {
			sbuilder.addPushGifts(BuilderUtil.buildPushGiftMsg(entry.getKey(), entry.getValue()*1000l));
		}
		
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.PUSH_GIFT_SYN_INFO_VALUE, sbuilder);
		player.sendProtocol(protocol);
	}
	
	public PushGiftGroupCfg getPushGiftGroupCfg(int giftId) {
		PushGiftLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(PushGiftLevelCfg.class, giftId);
		
		return HawkConfigManager.getInstance().getConfigByKey(PushGiftGroupCfg.class, levelCfg.getGroupId());
	}
	
	/**
	 * 只给配置检验用,正常逻辑请勿使用.
	 * @param conditionType
	 * @return
	 */
	public boolean isValidConditionType(Integer conditionType) {
		AbstractPushGiftCondition apgc = null;
		if (conditionType == PushGiftConditionEnum.ALL_ATTACK_FAIL.getType() || conditionType == PushGiftConditionEnum.ALL_ATTACK_FAIL_EXTRA.getType()) {
			PushGiftConditionFromEnum[] pgcfes = PushGiftConditionFromEnum.values();
			for (PushGiftConditionFromEnum pgcfe : pgcfes) {
				apgc = this.getCondition(conditionType, pgcfe);
				if (apgc == null) {
					throw new InvalidParameterException(String.format("配置的条件类型代码里面找不到 type:%s subType:%s", conditionType, pgcfe.toString()));
				}
			}
		} else {
			apgc = this.getCondition(conditionType);
			if (apgc == null) {
				throw new InvalidParameterException(String.format("配置的条件类型代码里面找不到 type:%s", conditionType));
			}
		}
		
		return true;
	}
	
	/**
	 * 获取触发礼包次数
	 * @return
	 */
	public int getTouchGiftTimes(String playerId, int giftGroupId) {
		Map<Integer, Integer> touchedMap = touchedGift.get(playerId);
		if (touchedMap == null) {
			touchedMap = RedisProxy.getInstance().getTouchPushGiftTimesMap(playerId);
			touchedGift.put(playerId, touchedMap);
		}
		return touchedMap.getOrDefault(giftGroupId, 0);
	}
	
	public void addTouchGiftTimes(String playerId, int giftGroupId) {
		Map<Integer, Integer> touchedMap = touchedGift.get(playerId);
		if (touchedMap == null) {
			touchedMap = RedisProxy.getInstance().getTouchPushGiftTimesMap(playerId);
			touchedGift.put(playerId, touchedMap);
		}
		int afterTimes = touchedMap.getOrDefault(giftGroupId, 0) + 1;
		touchedMap.put(giftGroupId, afterTimes);

		RedisProxy.getInstance().updateTouchPushGiftTimesMap(playerId, giftGroupId);
	}
}
