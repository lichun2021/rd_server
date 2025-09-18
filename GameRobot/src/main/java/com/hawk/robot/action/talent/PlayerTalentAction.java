package com.hawk.robot.action.talent;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hawk.annotation.RobotAction;
import org.hawk.config.HawkConfigManager;
import org.hawk.enums.EnumUtil;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;

import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.config.ConstProperty;
import com.hawk.robot.config.PlayerLevelCfg;
import com.hawk.robot.config.ShopCfg;
import com.hawk.robot.config.TalentCfg;
import com.hawk.robot.config.TalentLevelCfg;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Item.ItemInfo;
import com.hawk.game.protocol.Talent.HPTalentChangeReq;
import com.hawk.game.protocol.Talent.HPTalentClearReq;
import com.hawk.game.protocol.Talent.HPTalentUpgradeReq;
import com.hawk.game.protocol.Talent.TalentInfo;
import com.hawk.game.protocol.Talent.TalentType;

/**
 * 
 * 玩家战略（天赋）操作类
 * 
 * @author lating
 *
 */
@RobotAction(valid = false)
public class PlayerTalentAction extends HawkRobotAction {
	
	/**
	 * 战略操作类型
	 */
	private static enum TalentOperType {
		TALENT_UPGRADE_1,  // 战略加点，战略加点有多个的原因，是让该操作随机到的概率更大
		TALENT_UPGRADE_2,  // 战略加点
		TALENT_UPGRADE_3,  // 战略加点
		TALENT_UPGRADE_4,  // 战略加点
		TALENT_UPGRADE_5,  // 战略加点
		TALENT_UPGRADE_6,  // 战略加点
		TALENT_UPGRADE_7,  // 战略加点
		TALENT_CLEAR,      // 战略点重置
		TALENT_CHANGE,     // 战略切换
	}
	
	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity robot = (GameRobotEntity) robotEntity;
		TalentOperType type = EnumUtil.random(TalentOperType.class);
		switch (type) {
		case TALENT_CLEAR:
			talentClear(robot);
			break;
		case TALENT_CHANGE:
			talentChange(robot);
			break;
		default:
			talentUpgrade(robot);
			break;
		}
	}
	
	/**
	 * 战略加点
	 * 
	 * @param robot
	 */
	private void talentUpgrade(GameRobotEntity robot) {
		int freePoint = getFreePoint(robot);
		if (freePoint <= 0) {
			RobotLog.cityErrPrintln("talent levelup action failed, freePoint error, playerId: {}, freePoint: {}", robot.getPlayerId(), freePoint);
			return;
		}
		
		List<TalentInfo> talentList = robot.getTalentObjects();
		if (createNewTalent(robot, talentList, freePoint, TalentCfg.getDefaultUnlockedIds()) > 0) {
			return;
		}
		
		if (HawkRand.randPercentRate(50)) {
			List<Integer> talentIds = robot.getBasicData().getUnlockedTalents();
			if (!talentIds.isEmpty()) {
				Integer talentId = createNewTalent(robot, talentList, freePoint, talentIds);
				if (talentId > 0) {
					talentIds.remove(talentId);
				}
				return;
			}
		} 
		
		HPTalentUpgradeReq.Builder builder = HPTalentUpgradeReq.newBuilder();
		Collections.shuffle(talentList);
		int talentType = robot.getBasicData().getTalentType();
		for (TalentInfo talentInfo : talentList) {
			if (talentInfo.getType() != talentType) {
				continue;
			}
			
			int maxLevel = TalentLevelCfg.getMaxLevelByTalentId(talentInfo.getTalentId());
			if (talentInfo.getLevel() >= maxLevel) {
				continue;
			}
			
			builder.setTalentId(talentInfo.getTalentId());
			builder.setType(talentInfo.getType());
			int tarLevel = HawkRand.randInt(talentInfo.getLevel() + 1, Math.min(maxLevel, talentInfo.getLevel() + freePoint));
			builder.setTargetLevel(tarLevel);
			robot.sendProtocol(HawkProtocol.valueOf(HP.code.TALENT_UPGRADE_C_VALUE, builder));
			RobotLog.cityPrintln("talent levelup action, playerId: {}, talentId: {}, talentType: {}, targetLevel: {}, curLevel: {}", 
					robot.getPlayerId(), builder.getTalentId(), builder.getType(), builder.getTargetLevel(), talentInfo.getLevel());
			return;
		}
		
		RobotLog.cityErrPrintln("talent levelup action failed, talent level error, playerId: {}", robot.getPlayerId());
	}
	
	/**
	 * 创建新的天赋
	 * 
	 * @param robot
	 * @param talentList
	 * @param freePoint
	 * @return
	 */
	private Integer createNewTalent(GameRobotEntity robot, List<TalentInfo> talentList, int freePoint, List<Integer> talentIds) {
		for (Integer talentId : talentIds) {
			Optional<TalentInfo> op = talentList.stream().filter(e -> e.getTalentId() == talentId).findAny();
			if (op.isPresent()) {
				continue;
			}
			
			TalentCfg talentCfg = HawkConfigManager.getInstance().getConfigByKey(TalentCfg.class, talentId);
			if(talentCfg == null) {
				RobotLog.cityErrPrintln("talent levelup action failed, cfg error, playerId: {}, talentId: {}, curLevel: {}", 
						robot.getPlayerId(), talentId, 0);
				continue;
			}
			
			HPTalentUpgradeReq.Builder builder = HPTalentUpgradeReq.newBuilder();
			builder.setTalentId(talentId);
			builder.setType(robot.getBasicData().getTalentType());
			int maxLevel = TalentLevelCfg.getMaxLevelByTalentId(talentCfg.getId());
			int tarLevel = HawkRand.randInt(1, Math.min(maxLevel, freePoint));
			builder.setTargetLevel(tarLevel);
			robot.sendProtocol(HawkProtocol.valueOf(HP.code.TALENT_UPGRADE_C_VALUE, builder));
			RobotLog.cityPrintln("talent levelup action, playerId: {}, talentId: {}, talentType: {}, targetLevel: {}, curLevel: {}", 
					robot.getPlayerId(), builder.getTalentId(), builder.getType(), builder.getTargetLevel(), 0);
			return talentId;
		}
		
		return -1;
	}
	
	/**
	 * 获取可用的天赋点数
	 * @param robot
	 * @return
	 */
	private int getFreePoint(GameRobotEntity robot) {
		PlayerLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(PlayerLevelCfg.class, robot.getBasicData().getPlayerInfo().getLevel());
		int totalPoint = levelCfg.getSkillPoint();
		int usedPoint = 0;
		int talentType = robot.getBasicData().getTalentType();
		Collection<TalentInfo> talentInfos = robot.getBasicData().getTalentObjects().values();
		for (TalentInfo talentInfo : talentInfos) {
			if (talentInfo.getType() != talentType) {
				continue;
			}
			usedPoint += talentInfo.getLevel();
		}
		
		return totalPoint - usedPoint;
	}
	
	/**
	 * 战略切换
	 * 
	 * @param robot
	 */
	private void talentChange(GameRobotEntity robot) {
		HPTalentChangeReq.Builder builder = HPTalentChangeReq.newBuilder();
		int talentType = Math.abs(robot.getBasicData().getTalentType() - 1);
		
		// 玩家大本等级
		int cityLevel = robot.getCityLevel();
		if (TalentType.valueOf(talentType) == TalentType.TALENT_TYPE_3
				&& cityLevel < ConstProperty.getInstance().getUnlockTalentLine3NeedCityLevel()) {
			if (HawkRand.randPercentRate(50)) {
				return;
			}
			talentType--;
		}
		
		if (TalentType.valueOf(talentType) == TalentType.TALENT_TYPE_2
				&& cityLevel < ConstProperty.getInstance().getUnlockTalentLine2NeedCityLevel()) {
			if (HawkRand.randPercentRate(50)) {
				return;
			}
			talentType--;
		}
		
		boolean useGold = HawkRand.randPercentRate(40);
		if (!useGold) {
			int shopId = ConstProperty.getInstance().getTalentExchangeItemSaleId();
			ShopCfg shopCfg = HawkConfigManager.getInstance().getConfigByKey(ShopCfg.class, shopId);
			int itemCnt = robot.getItemObjects().stream().filter(e -> e.getItemId() == shopCfg.getShopItemID()).mapToInt( e -> e.getCount()).sum();
			if (itemCnt == 0) {
				if (HawkRand.randPercentRate(50)) {
					return;
				}
				useGold = true;
			}
		}
		
		builder.setType(talentType);
		builder.setUseGold(useGold);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.TALENT_CHANGE_C_VALUE, builder));
		RobotLog.cityPrintln("talent change action, playerId: {}, useGold: {}", robot.getPlayerId(), useGold);
	}
	
	/**
	 * 战略点重置
	 * 
	 * @param robot
	 */
	private void talentClear(GameRobotEntity robot) {
		boolean useGold = HawkRand.randPercentRate(60);

		if (!useGold) {
			int shopId = ConstProperty.getInstance().getTalentResetItemSaleId();
			ShopCfg shopCfg = HawkConfigManager.getInstance().getConfigByKey(ShopCfg.class, shopId);
			Optional<ItemInfo> op = robot.getItemObjects().stream().filter(e -> e.getItemId() == shopCfg.getShopItemID() && e.getCount() > 0).findAny();
			if (!op.isPresent()) {
				useGold = true;
			}
		}
		
		HPTalentClearReq.Builder builder = HPTalentClearReq.newBuilder();
		builder.setUseGold(useGold);
		builder.setType(HawkRand.randInt(1));
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.TALENT_CLEAR_C_VALUE, builder));
		RobotLog.cityPrintln("talent clear action, playerId: {}, useGold: {}", robot.getPlayerId(), useGold);
	}
}
