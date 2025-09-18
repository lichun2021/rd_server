package com.hawk.game.service.mail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.hawk.game.battle.BattleAnalysisInfo;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.battleIncome.IBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.MailCombatReminderCfg;
import com.hawk.game.config.WorldPlunderupLimitCfg;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.lianmengcyb.CYBORGConst.CYBORGState;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.spacemecha.MechaSpaceConst.SpacePointIndex;
import com.hawk.game.module.lianmengtaiboliya.TBLYConst.TBLYState;
import com.hawk.game.module.spacemecha.MechaSpaceInfo;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.module.spacemecha.config.SpaceMechaStrongholdCfg;
import com.hawk.game.module.spacemecha.worldpoint.SpaceWorldPoint;
import com.hawk.game.module.spacemecha.worldpoint.StrongHoldWorldPoint;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.FightResult;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.GuildChampionship.GCPlayerInfo;
import com.hawk.game.protocol.Mail.FightMail;
import com.hawk.game.protocol.Mail.MailPlayerInfo;
import com.hawk.game.protocol.Mail.PveFightMail;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.SimulateWar.SimulateWarBasePlayerStruct;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.MailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.MailBuilderUtil;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 战斗邮件服务类
 * 
 * @author Nannan.Gao
 * @date 2016-11-30 18:23:18
 */
public class FightMailService extends MailService {

	private static final FightMailService instance = new FightMailService();

	public static FightMailService getInstance() {

		return instance;
	}

	/**
	 * 发送战斗邮件
	 * 
	 * @param attackPlayers
	 *            进攻方
	 * @param defensePlayers
	 *            防御方
	 * @param worldPoint
	 *            战斗发生地点
	 * @param isAttackersWin
	 *            攻方胜则为true
	 * @param attackArmies
	 *            攻方士兵列表
	 * @param defenseArmies
	 *            守方士兵列表
	 * @param cannonKillCnt
	 * @param resources
	 *            掠夺的资源奖励
	 * @param captureState
	 */
	public void sendFightMail(int pointType, IBattleIncome battleIncome, BattleOutcome battleOutcome, Map<String, long[]> resources) {
		battleIncome.calNationMilitary(battleOutcome);
		
		boolean isAtksWin = battleOutcome.isAtkWin();
		// 进攻方玩家
		List<Player> attackers = battleIncome.getAtkPlayers();
		// 防御方玩家
		List<Player> defenderes = battleIncome.getDefPlayers();

		// 战斗邮件
		FightMail.Builder attackMail = MailBuilderUtil.createFightMail(battleIncome, battleOutcome, true);
		FightMail.Builder defenseMail = MailBuilderUtil.createFightMail(battleIncome, battleOutcome, false);
		
		attackMail.setIsLmjy(battleOutcome.getDuntype() == DungeonMailType.LMJY);// 废弃
		defenseMail.setIsLmjy(battleOutcome.getDuntype() == DungeonMailType.LMJY);
		
		attackMail.setIsTBLY(battleOutcome.getDuntype() == DungeonMailType.TBLY);
		defenseMail.setIsTBLY(battleOutcome.getDuntype() == DungeonMailType.TBLY);

		attackMail.setIsSW(battleOutcome.getDuntype() == DungeonMailType.SW);
		defenseMail.setIsSW(battleOutcome.getDuntype() == DungeonMailType.SW);
		
		attackMail.setDuntype(battleOutcome.getDuntype().intValue()); // 推荐
		defenseMail.setDuntype(battleOutcome.getDuntype().intValue());
		
		if (isAtksWin) {
			attackMail.setResult(FightResult.ATTACK_SUCC);
			defenseMail.setResult(FightResult.DEFENCE_FAIL);
		} else {
			attackMail.setResult(FightResult.ATTACK_FAIL);
			defenseMail.setResult(FightResult.DEFENCE_SUCC);
		}

		// 战报分析
		BattleAnalysisInfo analysisInfo = makeBattleAnalysis(battleIncome, battleOutcome, isAtksWin);

		attackMail.setBattleAnalysis(analysisInfo.getAtkAnalysis());
		attackMail.setBattlePrompt(analysisInfo.getAtkPrompt());
		attackMail.setBattleGreat(analysisInfo.getBattleGreat());

		attackMail.setOppFight(defenseMail.getSelfFight());
		attackMail.addAllOppArmy(defenseMail.getSelfArmyList());
		attackMail.setOppPlayer(defenseMail.getSelfPlayer());
		attackMail.addAllOppfEffs(defenseMail.getSelfEffsList());
		if (defenderes != null) {
			defenseMail.setOppPlayer(attackMail.getSelfPlayer());
			defenseMail.setOppFight(attackMail.getSelfFight());
			defenseMail.addAllOppArmy(attackMail.getSelfArmyList());
			defenseMail.addAllOppfEffs(attackMail.getSelfEffsList());
			defenseMail.setBattleAnalysis(analysisInfo.getDefAnalysis());
			defenseMail.setBattlePrompt(analysisInfo.getDefPrompt());
			defenseMail.setBattleGreat(analysisInfo.getBattleGreat());
		}

		MailPlayerInfo defPlayer = attackMail.getOppPlayer();
		Object[] atktips = { defPlayer.getName() };
		String defGuildTag = null;
		if (defPlayer.hasGuildName()) {
			defGuildTag = defPlayer.getGuildTag();
		}

		MailPlayerInfo atkPlayer = defenseMail.getOppPlayer();
		String atkGuildTag = null;
		Object[] defTips = { atkPlayer.getName() };
		if (atkPlayer.hasGuildTag()) {
			atkGuildTag = atkPlayer.getGuildTag();
		}

		MailId attackMailId = null;
		MailId defenseMailId = null;
		if (isAtksWin) {
			if (pointType == WorldPointType.PLAYER_VALUE) {
				if(battleIncome.getAtkCalcParames().duel){
					attackMailId = MailId.DUEL_ATTACK_BASE_SUCC_TO_FROM;
					defenseMailId = MailId.DUEL_ATTACK_BASE_SUCC_TO_TARGET;
				}else{
					attackMailId = MailId.ATTACK_BASE_SUCC_TO_FROM;
					defenseMailId = MailId.ATTACK_BASE_SUCC_TO_TARGET;
				}
			} else if (pointType == WorldPointType.QUARTERED_VALUE) {
				attackMailId = MailId.ATTACK_CAMP_SUCC_TO_FROM;
				defenseMailId = MailId.ATTACK_CAMP_SUCC_TO_TARGET;
			} else if (pointType == WorldPointType.RESOURCE_VALUE) {
				attackMailId = MailId.ATTACK_RES_SUCC_TO_FROM;
				defenseMailId = MailId.ATTACK_RES_SUCC_TO_TARGET;
			} else if (pointType == WorldPointType.KING_PALACE_VALUE) {
				attackMailId = MailId.ATTACK_CAPITAL_SUCC_TO_FROM;
				defenseMailId = MailId.ATTACK_CAPITAL_SUCC_TO_TARGET;
			} else if (pointType == WorldPointType.GUILD_TERRITORY_VALUE) {
				attackMailId = MailId.ATTACK_GUILD_BASTION_SUCC_TO_FROM;
				defenseMailId = MailId.ATTACK_GUILD_BASTION_SUCC_TO_TARGET;
			} else if (pointType == WorldPointType.CAPITAL_TOWER_VALUE) {
				attackMailId = MailId.ATTACK_CAPITAL_TOWER_SUCC_TO_FROM;
				defenseMailId = MailId.ATTACK_CAPITAL_TOWER_SUCC_TO_TARGET;
			} else if (pointType == WorldPointType.STRONG_POINT_VALUE) {
				attackMailId = MailId.ATTACK_STRONG_POINT_PVP_SUCC_TO_FROM;
				defenseMailId = MailId.ATTACK_STRONG_POINT_PVP_SUCC_TO_TARGET;
			} else if (pointType == WorldPointType.CHRISTMAS_BOX_VALUE) {
				attackMailId = MailId.ATTACK_CHRISTMAS_SUCC_TO_FROM;
				defenseMailId = MailId.ATTACK_CHRISTMAS_SUCC_TO_TARGET;
			} else if (pointType == WorldPointType.FOGGY_FORTRESS_VALUE) {
				attackMailId = MailId.ATTACK_FOGGY_SUCC_TO_FROM;
				atktips = new Object[] { battleIncome.getMonsterId() };
			} else if (pointType == WorldPointType.SUPER_WEAPON_VALUE) {
				attackMailId = MailId.ATTACK_SUPER_WEAPON_SUCC_TO_FROM;
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defenseMailId = MailId.ATTACK_SUPER_WEAPON_SUCC_TO_TARGET;
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY(), atkPlayer.getName() };
			} else if (pointType == WorldPointType.XIAO_ZHAN_QU_VALUE) {
				attackMailId = MailId.ATTACK_XZQ_SUCC_TO_FROM;
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defenseMailId = MailId.ATTACK_XZQ_SUCC_TO_TARGET;
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY(), atkPlayer.getName() };
			}else if(pointType == WorldPointType.TH_RESOURCE_VALUE){
				attackMailId = MailId.ATTACK_TREASURE_HUNT_RES_SUCC_TO_FROM;
				defenseMailId = MailId.ATTACK_TREASURE_HUNT_RES_SUCC_TO_TARGET;
			} else if(pointType == WorldPointType.WAR_FLAG_POINT_VALUE){
				attackMailId = MailId.ATTACK_WAR_FLAG_SUCC_TO_FROM;
				defenseMailId = MailId.ATTACK_WAR_FLAG_SUCC_TO_TARGET;
			} else if (pointType == WorldPointType.CROSS_FORTRESS_VALUE) {
				attackMailId = MailId.ATTACK_FORTRESS_SUCC_TO_FROM;
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defenseMailId = MailId.ATTACK_FORTRESS_SUCC_TO_TARGET;
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY(), atkPlayer.getName() };
			} else if (pointType == WorldPointType.TBLY_CHRONO_SPHERE_VALUE || pointType == WorldPointType.TBLY_COMMAND_CENTER_VALUE
					|| pointType == WorldPointType.TBLY_HEADQUARTERS_VALUE || pointType == WorldPointType.TBLY_IRON_CRUTAIN_DIVICE_VALUE
					|| pointType == WorldPointType.TBLY_MILITARY_BASE_VALUE || pointType == WorldPointType.TBLY_NUCLEAR_MISSILE_SILO_VALUE
					|| pointType == WorldPointType.TBLY_WEATHER_CONTROLLER_VALUE || pointType == WorldPointType.TBLY_TECHNOLOGY_LAB_VALUE
					|| pointType == WorldPointType.TBLY_COMMAND_POST_VALUE) {
				attackMailId = MailId.ATTACK_TW_BUILD_SUCC_TO_FROM;
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defenseMailId = MailId.ATTACK_TW_BUILD_SUCC_TO_TARGET;
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY(), atkPlayer.getName() };
			} else if (pointType == WorldPointType.TBLY_FUELBANK_VALUE) {
				attackMailId = MailId.ATTACK_TW_RES_SUCC_TO_FROM;
				defenseMailId = MailId.ATTACK_TW_RES_SUCC_TO_TARGET;
				atktips = new Object[] { 0, 0, defPlayer.getName() };
				defTips = new Object[] { 0, 0, atkPlayer.getName() };
			} else if (pointType == WorldPointType.SW_COMMAND_CENTER_VALUE || pointType == WorldPointType.SW_HEADQUARTERS_VALUE) {
				switch (battleOutcome.getSwWarType()) {
				case FIRST_WAR:
					attackMailId = MailId.ATTACK_SW_ONE_BUILD_SUCC_TO_FROM;
					defenseMailId = MailId.ATTACK_SW_ONE_BUILD_SUCC_TO_TARGET;
					break;
				case SECOND_WAR:
					attackMailId = MailId.ATTACK_SW_BUILD_SUCC_TO_FROM;
					defenseMailId = MailId.ATTACK_SW_BUILD_SUCC_TO_TARGET;
					break;
				case THIRD_WAR:
					attackMailId = MailId.ATTACK_SW_TERMINAL_BUILD_SUCC_TO_FROM;
					defenseMailId = MailId.ATTACK_SW_TERMINAL_BUILD_SUCC_TO_TARGET;
					break;
				}
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY(), atkPlayer.getName() };
			}else if (pointType == WorldPointType.PYLON_VALUE) {
				attackMailId = MailId.ATK_PYLON_SUCCESS;
				defenseMailId = MailId.DEF_PYLON_FAIL;
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
			} else if (pointType == WorldPointType.XQHX_PYLON_VALUE) {
				attackMailId = MailId.ATK_XQHX_PYLON_SUCCESS;
				defenseMailId = MailId.DEF_XQHX_PYLON_FAIL;
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
			} else if (pointType == WorldPointType.CYBORG_CHRONO_SPHERE_VALUE || pointType == WorldPointType.CYBORG_COMMAND_CENTER_VALUE
					|| pointType == WorldPointType.CYBORG_HEADQUARTERS_VALUE || pointType == WorldPointType.CYBORG_IRON_CRUTAIN_DIVICE_VALUE
					|| pointType == WorldPointType.CYBORG_MILITARY_BASE_VALUE || pointType == WorldPointType.CYBORG_NUCLEAR_MISSILE_SILO_VALUE
					|| pointType == WorldPointType.CYBORG_WEATHER_CONTROLLER_VALUE) {
				attackMailId = MailId.CYBORG_ATTACK_BUILD_SUCC_TO_FROM;
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defenseMailId = MailId.CYBORG_ATTACK_BUILD_SUCC_TO_TARGET;
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY(), atkPlayer.getName() };
			}else if (pointType == WorldPointType.DYZZ_ENERGY_WELL_VALUE
					|| pointType == WorldPointType.DYZZ_TOWER_VALUE
					|| pointType == WorldPointType.DYZZ_HIGH_TOWER_VALUE) {
				attackMailId = MailId.ATTACK_DYZZ_BUILD_SUCC_TO_FROM;
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defenseMailId = MailId.ATTACK_DYZZ_BUILD_SUCC_TO_TARGET;
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY(), atkPlayer.getName() };
			} else if (pointType == WorldPointType.DYZZ_FUELBANK_VALUE) {
				attackMailId = MailId.ATTACK_DYZZ_RES_SUCC_TO_FROM;
				defenseMailId = MailId.ATTACK_DYZZ_RES_SUCC_TO_TARGET;
				atktips = new Object[] { 0, 0, defPlayer.getName() };
				defTips = new Object[] { 0, 0, atkPlayer.getName() };
			} else if (pointType == WorldPointType.YQZZ_BUILDING_VALUE) {
				attackMailId = MailId.ATTACK_YQZZ_BUILD_SUCC_TO_FROM;
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defenseMailId = MailId.ATTACK_YQZZ_BUILD_SUCC_TO_TARGET;
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY(), atkPlayer.getName() };
			} else if (pointType == WorldPointType.XQHX_BUILDING_VALUE) {
				attackMailId = MailId.ATTACK_XQHX_BUILD_SUCC_TO_FROM;
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defenseMailId = MailId.ATTACK_XQHX_BUILD_SUCC_TO_TARGET;
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY(), atkPlayer.getName() };
			} else if( pointType == WorldPointType.MEDAL_FACTORY_VALUE){
				attackMailId = MailId.ATTACK_MEDALF_SUCC_TO_FROM;
				defenseMailId = MailId.ATTACK_MEDALF_SUCC_TO_TARGET;
			}else if (pointType == WorldPointType.XHJZ_BUILDING_VALUE) {
				attackMailId = MailId.ATTACK_XHJZ_BUILD_SUCC_TO_FROM;
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defenseMailId = MailId.ATTACK_XHJZ_BUILD_SUCC_TO_TARGET;
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY(), atkPlayer.getName() };
			} 
			
		} else {
			if (pointType == WorldPointType.PLAYER_VALUE) {
				if (battleIncome.getAtkCalcParames().duel) {
					attackMailId = MailId.DUEL_ATTACK_BASE_FAILED_TO_FROM;
					defenseMailId = MailId.DUEL_ATTACK_BASE_FAILED_TO_TARGET;
				} else {
					attackMailId = MailId.ATTACK_BASE_FAILED_TO_FROM;
					defenseMailId = MailId.ATTACK_BASE_FAILED_TO_TARGET;
				}
			} else if (pointType == WorldPointType.QUARTERED_VALUE) {
				attackMailId = MailId.ATTACK_CAMP_FAILED_TO_FROM;
				defenseMailId = MailId.ATTACK_CAMP_FAILED_TO_TARGET;
			} else if (pointType == WorldPointType.RESOURCE_VALUE) {
				attackMailId = MailId.ATTACK_RES_FAILED_TO_FROM;
				defenseMailId = MailId.ATTACK_RES_FAILED_TO_TARGET;
			} else if (pointType == WorldPointType.KING_PALACE_VALUE) {
				attackMailId = MailId.ATTACK_CAPITAL_FAILED_TO_FROM;
				defenseMailId = MailId.ATTACK_CAPITAL_FAILED_TO_TARGET;
			} else if (pointType == WorldPointType.GUILD_TERRITORY_VALUE) {
				attackMailId = MailId.ATTACK_GUILD_BASTION_FAILED_TO_FROM;
				defenseMailId = MailId.ATTACK_GUILD_BASTION_FAILED_TO_TARGET;
			} else if (pointType == WorldPointType.CAPITAL_TOWER_VALUE) {
				attackMailId = MailId.ATTACK_CAPITAL_TOWER_FAILED_TO_FROM;
				defenseMailId = MailId.ATTACK_CAPITAL_TOWER_FAILED_TO_TARGET;
			} else if (pointType == WorldPointType.STRONG_POINT_VALUE) {
				attackMailId = MailId.ATTACK_STRONG_POINT_PVP_FAILED_TO_FROM;
				defenseMailId = MailId.ATTACK_STRONG_POINT_PVP_FAILED_TO_TARGET;
			} else if (pointType == WorldPointType.CHRISTMAS_BOX_VALUE) {
				attackMailId = MailId.ATTACK_CHRISTMAS_FAILED_TO_FROM;
				defenseMailId = MailId.ATTACK_CHRISTMAS_FAILED_TO_TARGET;
			} else if (pointType == WorldPointType.FOGGY_FORTRESS_VALUE) {
				attackMailId = MailId.ATTACK_FOGGY_FAILED_TO_FROM;
				atktips = new Object[] { battleIncome.getMonsterId() };
			} else if (pointType == WorldPointType.SUPER_WEAPON_VALUE) {
				attackMailId = MailId.ATTACK_SUPER_WEAPON_FAILED_TO_FROM;
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defenseMailId = MailId.ATTACK_SUPER_WEAPON_FAILED_TO_TARGET;
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY(), atkPlayer.getName() };
			}else if (pointType == WorldPointType.XIAO_ZHAN_QU_VALUE) {
				attackMailId = MailId.ATTACK_XZQ_FAILED_TO_FROM;
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defenseMailId = MailId.ATTACK_XZQ_FAILED_TO_TARGET;
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY(), atkPlayer.getName() };
			}else if(pointType == WorldPointType.TH_RESOURCE_VALUE){
				attackMailId = MailId.ATTACK_TREASURE_HUNT_RES_FAILED_TO_FROM;
				defenseMailId = MailId.ATTACK_TREASURE_HUNT_RES_FAILED_TO_TARGET;
			} else if(pointType == WorldPointType.WAR_FLAG_POINT_VALUE){
				attackMailId = MailId.ATTACK_WAR_FLAG_FAILED_TO_FROM;
				defenseMailId = MailId.ATTACK_WAR_FLAG_FAILED_TO_TARGET;
			} else if (pointType == WorldPointType.CROSS_FORTRESS_VALUE) {
				attackMailId = MailId.ATTACK_FORTRESS_FAILED_TO_FROM;
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defenseMailId = MailId.ATTACK_FORTRESS_FAILED_TO_TARGET;
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY(), atkPlayer.getName() };
			} else if (pointType == WorldPointType.TBLY_CHRONO_SPHERE_VALUE || pointType == WorldPointType.TBLY_COMMAND_CENTER_VALUE
					|| pointType == WorldPointType.TBLY_HEADQUARTERS_VALUE || pointType == WorldPointType.TBLY_IRON_CRUTAIN_DIVICE_VALUE
					|| pointType == WorldPointType.TBLY_MILITARY_BASE_VALUE || pointType == WorldPointType.TBLY_NUCLEAR_MISSILE_SILO_VALUE
					|| pointType == WorldPointType.TBLY_WEATHER_CONTROLLER_VALUE || pointType == WorldPointType.TBLY_TECHNOLOGY_LAB_VALUE
					|| pointType == WorldPointType.TBLY_COMMAND_POST_VALUE) {
				attackMailId = MailId.ATTACK_TW_BUILD_FAILED_TO_FROM;
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defenseMailId = MailId.ATTACK_TW_BUILD_FAILED_TO_TARGET;
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY(), atkPlayer.getName() };
			}else if (pointType == WorldPointType.TBLY_FUELBANK_VALUE) {
				attackMailId = MailId.ATTACK_TW_RES_FAILED_TO_FROM;
				defenseMailId = MailId.ATTACK_TW_RES_FAILED_TO_TARGET;
				atktips = new Object[] { 0, 0, defPlayer.getName()};
				defTips = new Object[] { 0, 0, atkPlayer.getName() };
			}
			else if (pointType == WorldPointType.SW_COMMAND_CENTER_VALUE || pointType == WorldPointType.SW_HEADQUARTERS_VALUE) {
				switch (battleOutcome.getSwWarType()) {
				case FIRST_WAR:
					attackMailId = MailId.ATTACK_SW_ONE_BUILD_FAILED_TO_FROM;
					defenseMailId = MailId.ATTACK_SW_ONE_BUILD_FAILED_TO_TARGET;
					break;
				case SECOND_WAR:
					attackMailId = MailId.ATTACK_SW_BUILD_FAILED_TO_FROM;
					defenseMailId = MailId.ATTACK_SW_BUILD_FAILED_TO_TARGET;
					break;
				case THIRD_WAR:
					attackMailId = MailId.ATTACK_SW_TERMINAL_BUILD_FAILED_TO_FROM;
					defenseMailId = MailId.ATTACK_SW_TERMINAL_BUILD_FAILED_TO_TARGET;
					break;
				}
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY(), atkPlayer.getName() };
			}else if (pointType == WorldPointType.PYLON_VALUE) {
				attackMailId = MailId.ATK_PYLON_FAIL;
				defenseMailId = MailId.DEF_PYLON_SUCCESS;
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
			} else if (pointType == WorldPointType.XQHX_PYLON_VALUE) {
				attackMailId = MailId.ATK_XQHX_PYLON_FAIL;
				defenseMailId = MailId.DEF_XQHX_PYLON_SUCCESS;
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
			} else if (pointType == WorldPointType.CYBORG_CHRONO_SPHERE_VALUE || pointType == WorldPointType.CYBORG_COMMAND_CENTER_VALUE
					|| pointType == WorldPointType.CYBORG_HEADQUARTERS_VALUE || pointType == WorldPointType.CYBORG_IRON_CRUTAIN_DIVICE_VALUE
					|| pointType == WorldPointType.CYBORG_MILITARY_BASE_VALUE || pointType == WorldPointType.CYBORG_NUCLEAR_MISSILE_SILO_VALUE
					|| pointType == WorldPointType.CYBORG_WEATHER_CONTROLLER_VALUE) {
				attackMailId = MailId.CYBORG_ATTACK_BUILD_FAILED_TO_FROM;
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defenseMailId = MailId.CYBORG_ATTACK_BUILD_FAILED_TO_TARGET;
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY(), atkPlayer.getName() };
			}else if (pointType == WorldPointType.DYZZ_ENERGY_WELL_VALUE
					|| pointType == WorldPointType.DYZZ_TOWER_VALUE
					|| pointType == WorldPointType.DYZZ_HIGH_TOWER_VALUE) {
				attackMailId = MailId.ATTACK_DYZZ_BUILD_FAILED_TO_FROM;
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defenseMailId = MailId.ATTACK_DYZZ_BUILD_FAILED_TO_TARGET;
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY(), atkPlayer.getName() };
			}else if (pointType == WorldPointType.DYZZ_FUELBANK_VALUE) {
				attackMailId = MailId.ATTACK_DYZZ_RES_FAILED_TO_FROM;
				defenseMailId = MailId.ATTACK_DYZZ_RES_FAILED_TO_TARGET;
				atktips = new Object[] { 0, 0, defPlayer.getName()};
				defTips = new Object[] { 0, 0, atkPlayer.getName() };
			}else if (pointType == WorldPointType.YQZZ_BUILDING_VALUE) {
				attackMailId = MailId.ATTACK_YQZZ_BUILD_FAILED_TO_FROM;
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defenseMailId = MailId.ATTACK_YQZZ_BUILD_FAILED_TO_TARGET;
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY(), atkPlayer.getName() };
			} else if (pointType == WorldPointType.XQHX_BUILDING_VALUE) {
				attackMailId = MailId.ATTACK_XQHX_BUILD_FAILED_TO_FROM;
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defenseMailId = MailId.ATTACK_XQHX_BUILD_FAILED_TO_TARGET;
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY(), atkPlayer.getName() };
			} else if( pointType == WorldPointType.MEDAL_FACTORY_VALUE){
				attackMailId = MailId.ATTACK_MEDALF_FAILED_TO_FROM;
				defenseMailId = MailId.ATTACK_MEDALF_FAILED_TO_TARGET;
			} else if (pointType == WorldPointType.XHJZ_BUILDING_VALUE) {
				attackMailId = MailId.ATTACK_XHJZ_BUILD_FAILED_TO_FROM;
				atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
				defenseMailId = MailId.ATTACK_XHJZ_BUILD_FAILED_TO_TARGET;
				defTips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY(), atkPlayer.getName() };
			} 
		}

		// 战斗奖励
		int length = GsConst.RES_TYPE.length;
		long[] resLost = new long[length];
		long curTime = HawkTime.getMillisecond();

		if (attackMailId != null) {

			for (Player attacker : attackers) {
				String playerId = attacker.getId();
				if (resources != null) {
					attackMail.clearItems();
					long[] js = resources.get(playerId);
					for (int i = 0; i < length; i++) {
						long count = js[i];
						resLost[i] += count;
						attackMail.addItems(ItemInfo.toRewardItem(ItemType.PLAYER_ATTR_VALUE, GsConst.RES_TYPE[i], count));
					}

					if (battleIncome.isGrabResMarch()) {
						WorldPlunderupLimitCfg plunderCfg = HawkConfigManager.getInstance().getCombineConfig(WorldPlunderupLimitCfg.class, attacker.getCityLevel(),
								attacker.getVipLevel());
						if (Objects.nonNull(plunderCfg)) {
							attackMail.setPlunderupLimit1007(js[length + 0] == 1);
							if (attackMail.getPlunderupLimit1007()) {
								LogUtil.logGrabResLimit(attacker, PlayerAttr.GOLDORE_UNSAFE_VALUE, plunderCfg.getPlunderupLimit(PlayerAttr.GOLDORE_UNSAFE_VALUE),
										(int) js[length*2 + 0]);
							}
							attackMail.setPlunderupLimit1008(js[length + 1] == 1);
							if (attackMail.getPlunderupLimit1008()) {
								LogUtil.logGrabResLimit(attacker, PlayerAttr.OIL_UNSAFE_VALUE, plunderCfg.getPlunderupLimit(PlayerAttr.OIL_UNSAFE_VALUE),
										(int) js[length*2 + 1]);
							}
							attackMail.setPlunderupLimit1010(js[length + 2] == 1);
							if (attackMail.getPlunderupLimit1010()) {
								LogUtil.logGrabResLimit(attacker, PlayerAttr.TOMBARTHITE_UNSAFE_VALUE, plunderCfg.getPlunderupLimit(PlayerAttr.TOMBARTHITE_UNSAFE_VALUE),
										(int) js[length*2 + 2]);
							}
							attackMail.setPlunderupLimit1009(js[length + 3] == 1);
							if (attackMail.getPlunderupLimit1009()) {
								LogUtil.logGrabResLimit(attacker, PlayerAttr.STEEL_UNSAFE_VALUE, plunderCfg.getPlunderupLimit(PlayerAttr.STEEL_UNSAFE_VALUE),
										(int) js[length*2 + 3]);
							}
						}

					}
				}
				this.sendFightMail(playerId, attackMailId, attackMail, defGuildTag, atktips);

				// 添加交互信息
				if (defenderes != null) {
					for (Player defender : defenderes) {
						LocalRedis.getInstance().addInteractivePlayer(playerId, defender.getId(), curTime);
						LocalRedis.getInstance().addInteractivePlayer(defender.getId(), playerId, curTime);
					}
				}
			}
		}

		if (defenderes != null && defenseMailId != null) {
			int index = 0;
			for (Player defender : defenderes) {
				String playerId = defender.getId();
				if (resLost != null) {
					defenseMail.clearItems();
					for (int i = 0; i < length; i++) {
						int count = index > 0 ? 0 : (int) resLost[i]; // 第一个防守玩家丢资源
						defenseMail.addItems(ItemInfo.toRewardItem(ItemType.PLAYER_ATTR_VALUE, GsConst.RES_TYPE[i], count));
					}
					index++;
				}

				String mailUUId = this.sendFightMail(playerId, defenseMailId, defenseMail, atkGuildTag, defTips);
				if (defender.getTBLYState() == TBLYState.GAMEING || defender.getCYBORGState() == CYBORGState.GAMEING) {
					battleOutcome.recordDefMail(playerId, mailUUId);
				}
			}
		}
	}
	
	/**
	 * 记录联盟锦标赛战报
	 * @param termId
	 * @param battleId
	 * @param battleIncome
	 * @param battleOutcome
	 * @param playerB 
	 * @param playerA 
	 */
	public void recordGcMail(int termId,String battleId, IBattleIncome battleIncome, BattleOutcome battleOutcome, GCPlayerInfo.Builder playerA, GCPlayerInfo.Builder playerB) {
		String playerId = "GcPlayer" + termId;
		boolean isAtksWin = battleOutcome.isAtkWin();
		// 战斗邮件
		FightMail.Builder attackMail = MailBuilderUtil.createFightMail(battleIncome, battleOutcome, true);
		attackMail.getSelfPlayerBuilder().setName(playerA.getName());
		FightMail.Builder defenseMail = MailBuilderUtil.createFightMail(battleIncome, battleOutcome, false);
		defenseMail.getSelfPlayerBuilder().setName(playerB.getName());
		MailId mailId = null;
		if (isAtksWin) {
			attackMail.setResult(FightResult.ATTACK_SUCC);
			mailId = MailId.CHAMPIONSHIP_ATK_WIN;
		} else {
			attackMail.setResult(FightResult.ATTACK_FAIL);
			mailId = MailId.CHAMPIONSHIP_ATK_FAILED;
		}
		BattleAnalysisInfo analysisInfo = makeBattleAnalysis(battleIncome, battleOutcome, isAtksWin);

		attackMail.setBattleAnalysis(analysisInfo.getAtkAnalysis());
		attackMail.setBattlePrompt(analysisInfo.getAtkPrompt());
		attackMail.setBattleGreat(analysisInfo.getBattleGreat());

		attackMail.setOppFight(defenseMail.getSelfFight());
		attackMail.addAllOppArmy(defenseMail.getSelfArmyList());
		attackMail.setOppPlayer(defenseMail.getSelfPlayer());
		attackMail.addAllOppfEffs(defenseMail.getSelfEffsList());
		sendGCFightMail(playerId, battleId, mailId, attackMail);
	}
	
	public void recordSimulateWarMail(int termId,String battleId, IBattleIncome battleIncome, BattleOutcome battleOutcome, 
			SimulateWarBasePlayerStruct playerStructA, SimulateWarBasePlayerStruct playerStructB) {
		String playerId = "SimulateWarPlayer" + termId;
		boolean isAtksWin = battleOutcome.isAtkWin();
		// 战斗邮件
		FightMail.Builder attackMail = MailBuilderUtil.createFightMail(battleIncome, battleOutcome, true);
		attackMail.getSelfPlayerBuilder().setName(playerStructA.getName());
		FightMail.Builder defenseMail = MailBuilderUtil.createFightMail(battleIncome, battleOutcome, false);
		defenseMail.getSelfPlayerBuilder().setName(playerStructB.getName());
		MailId mailId = null;
		if (isAtksWin) {
			attackMail.setResult(FightResult.ATTACK_SUCC);
			mailId = MailId.CHAMPIONSHIP_ATK_WIN;
		} else {
			attackMail.setResult(FightResult.ATTACK_FAIL);
			mailId = MailId.CHAMPIONSHIP_ATK_FAILED;
		}
		BattleAnalysisInfo analysisInfo = makeBattleAnalysis(battleIncome, battleOutcome, isAtksWin);

		attackMail.setBattleAnalysis(analysisInfo.getAtkAnalysis());
		attackMail.setBattlePrompt(analysisInfo.getAtkPrompt());
		attackMail.setBattleGreat(analysisInfo.getBattleGreat());

		attackMail.setOppFight(defenseMail.getSelfFight());
		attackMail.addAllOppArmy(defenseMail.getSelfArmyList());
		attackMail.setOppPlayer(defenseMail.getSelfPlayer());
		attackMail.addAllOppfEffs(defenseMail.getSelfEffsList());
		sendSimulateWarFightMail(playerId, battleId, mailId, attackMail);
	}


	/**
	 * 类PVP类型PVE战报
	 * 
	 * @param battleIncome
	 * @param battleOutcome
	 * @param mailRewards
	 *            邮件奖励对象
	 */
	public void sendPveFightMail(BattleConst.BattleType battleType, IBattleIncome battleIncome, BattleOutcome battleOutcome, MailRewards mailRewards) {
		boolean isAtksWin = battleOutcome.isAtkWin();
		int monsterId = battleIncome.getMonsterId();
		List<Player> atkPlayers = battleIncome.getAtkPlayers();
		List<Player> defPlayers = battleIncome.getDefPlayers();
		
		// 战斗邮件
		PveFightMail.Builder attackMail = PveFightMail.newBuilder();
		PveFightMail.Builder defenseMail = PveFightMail.newBuilder();
		attackMail.setDuntype(battleOutcome.getDuntype().intValue()); // 推荐
		defenseMail.setDuntype(battleOutcome.getDuntype().intValue());
		
		FightMail.Builder atkMail = MailBuilderUtil.createFightMail(battleIncome, battleOutcome, true);
		FightMail.Builder defMail = MailBuilderUtil.createFightMail(battleIncome, battleOutcome, false);
		MailId attackMailId = null;
		MailId defenseMailId = null;
		
		List<String> extramParams = new ArrayList<>();
		int pointType = 0;
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(battleIncome.getBattle().getPointId());
		if (point != null && (pointType = point.getPointType()) == WorldPointType.SPACE_MECHA_STRONG_HOLD_VALUE) {
			StrongHoldWorldPoint strongPoint = (StrongHoldWorldPoint) point;
			SpaceMechaStrongholdCfg strongHoldCfg = strongPoint.getStrongHoldCfg();
			boolean strongholdBroken = strongHoldCfg != null && strongPoint.getHpNum() >= strongHoldCfg.getHpNumber() && strongPoint.getRemainBlood() <= 0;
			monsterId = strongPoint.getStrongHoldId();
			attackMailId = isAtksWin ? MailId.SPACE_MECHA_STRONG_HOLD_FIGHT_WIN : MailId.SPACE_MECHA_STRONG_HOLD_FIGHT_FAILED;
			attackMailId = strongholdBroken ? MailId.SPACE_MECHA_STRONGHOLD_BROKEN : attackMailId;
			isAtksWin = strongholdBroken ? true : isAtksWin;
		} else if (pointType == WorldPointType.SPACE_MECHA_MAIN_VALUE || pointType == WorldPointType.SPACE_MECHA_SLAVE_VALUE) {
			SpaceWorldPoint spacePoint = (SpaceWorldPoint) point;
			monsterId = spacePoint.getAtkEnemyId();
			MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(spacePoint.getGuildId());
			extramParams.add(String.valueOf(spaceObj.getStageVal().getNumber()));  // 阶段
			extramParams.add(String.valueOf(spacePoint.getAtkEnemyId()));  // 野怪名称
			if (spacePoint.getSpaceBlood() <= 0) {
				isAtksWin = true;
				defenseMailId = spacePoint.getSpaceIndex() == SpacePointIndex.MAIN_SPACE ? MailId.SPACE_MECHA_MAINSPACE_BROKEN : MailId.SPACE_MECHA_SUBSPACE_BROKEN;
				extramParams.add(String.valueOf(spacePoint.getSpaceLevel()));

				int monsterTotalCount = 0;
				Map<String, List<ArmyInfo>> atkArmyMap = battleOutcome.getBattleArmyMapAtk();
				for (Entry<String, List<ArmyInfo>> entry : atkArmyMap.entrySet()) {
					monsterTotalCount += entry.getValue().stream().mapToInt(e -> e.getTotalCount()).sum();
				}
				
				int defTotalKill = 0;
				Map<String, List<ArmyInfo>> battleArmyLeftMap = battleOutcome.getBattleArmyMapDef();
				for (Entry<String, List<ArmyInfo>> entry : battleArmyLeftMap.entrySet()) {
					defTotalKill += entry.getValue().stream().mapToInt(e -> e.getKillCount()).sum();
				}
				extramParams.add(String.valueOf(monsterTotalCount - defTotalKill));
			} else {
				defenseMailId = isAtksWin ? MailId.SPACE_MECHA_FIGHT_FAILED : MailId.SPACE_MECHA_FIGHT_WIN;
			}
		}

		if (isAtksWin) {
			attackMail.setResult(FightResult.ATTACK_SUCC);
			defenseMail.setResult(FightResult.DEFENCE_FAIL);
			// 设置邮件id
			switch (battleType) {
			case ATTACK_STRONG_POINT_PVE:
				attackMailId = MailId.ATTACK_STRONG_POINT_PVE_SUCC_TO_FROM;
				break;
			case ATTACK_FOGGY:
				attackMailId = MailId.ATTACK_FOGGY_SUCC_TO_FROM;
				break;
			case ATTACK_SUPER_WEAPON_PVE:
				attackMailId = MailId.ATTACK_SUPER_WEAPON_PVE_SUCC_TO_FROM;
				break;
			case ATTACK_XZQ_PVE:
				attackMailId = MailId.ATTACK_XZQ_PVE_SUCC_TO_FROM;
				break;
			case ATTACK_YURI_STRIKE_PVE:	
				attackMailId = MailId.ATTACK_YURI_STRIKE_SUCC_TO_FROM;
				break;
			case GHOST_MARCH:
				defenseMailId = MailId.DEF_GHOST_MARCH_FIGHT_FAILED;
				break;
			case ATTACK_FORTRESS_PVE:
				attackMailId = MailId.ATTACK_FORTRESS_PVE_SUCC_TO_FROM;
				break;
			case ATTACK_GOHOST_TOWER:
				attackMailId = MailId.GHOST_TOWER_MONSTER_ATTACK_SUCC_TO_FROM;
				break;
			case YQZZ_BUILD_PVE:
				attackMailId = MailId.ATTACK_YQZZ_BUILD_SUCC_TO_FROM_PVE;
				break;
			case FGYL_BUILD_PVE:
				attackMailId = MailId.ATTACK_FGYL_BUILD_SUCC_TO_FROM_PVE;
				break;
			default:
				break;
			}
		} else {
			attackMail.setResult(FightResult.ATTACK_FAIL);
			defenseMail.setResult(FightResult.DEFENCE_SUCC);
			// 设置邮件id
			switch (battleType) {
			case ATTACK_STRONG_POINT_PVE:
				attackMailId = MailId.ATTACK_STRONG_POINT_PVE_FAILED_TO_FROM;
				break;
			case ATTACK_FOGGY:
				attackMailId = MailId.ATTACK_FOGGY_FAILED_TO_FROM;
				break;
			case ATTACK_SUPER_WEAPON_PVE:
				attackMailId = MailId.ATTACK_SUPER_WEAPON_PVE_FAILED_TO_FROM;
				break;
			case ATTACK_XZQ_PVE:
				attackMailId = MailId.ATTACK_XZQ_PVE_FAILED_TO_FROM;
				break;
			case ATTACK_YURI_STRIKE_PVE:	
				attackMailId = MailId.ATTACK_YURI_STRIKE_FAILED_TO_FROM;
				break;
			case GHOST_MARCH:
				defenseMailId = MailId.DEF_GHOST_MARCH_FIGHT_WIN;
				break;
			case ATTACK_FORTRESS_PVE:
				attackMailId = MailId.ATTACK_FORTRESS_PVE_FAILED_TO_FROM;
				break;
			case ATTACK_GOHOST_TOWER:
				attackMailId = MailId.GHOST_TOWER_MONSTER_ATTACK_FAILED_TO_FROM;
				break;
			case YQZZ_BUILD_PVE:
				attackMailId = MailId.ATTACK_YQZZ_BUILD_FAILED_TO_FROM_PVE;
				break;
			default:
				break;
			}
		}
		// 战报分析
		BattleAnalysisInfo analysisInfo = makeBattleAnalysis(battleIncome, battleOutcome, isAtksWin);
		
		// 需要发送进攻方邮件
		if(attackMailId != null){
			attackMail.setSelfPlayer(atkMail.getSelfPlayer());
			attackMail.setOppPlayer(defMail.getSelfPlayer());
			attackMail.setMonsterId(monsterId);
			attackMail.setSelfFight(atkMail.getSelfFight());
			attackMail.setOppFight(defMail.getSelfFight());
			attackMail.addAllSelfArmy(atkMail.getSelfArmyList());
			attackMail.addAllOppArmy(defMail.getSelfArmyList());
			attackMail.addAllSelfEffs(atkMail.getSelfEffsList());
			attackMail.addAllOppfEffs(defMail.getSelfEffsList());
			attackMail.setBattleX(atkMail.getBattleX());
			attackMail.setBattleY(atkMail.getBattleY());
			attackMail.setBattleAnalysis(analysisInfo.getAtkAnalysis());
			attackMail.setBattlePrompt(analysisInfo.getAtkPrompt());
			attackMail.setBattleGreat(analysisInfo.getBattleGreat());
			attackMail.setFgylMaxBlood(battleOutcome.getFgylMaxBlood());
			attackMail.setFgylRemainBlood(battleOutcome.getFgylRemainBlood());
			attackMail.setFgylKillBlood(battleOutcome.getFgylKillBlood());
			attackMail.setFgylEnemy(battleIncome.getMonsterId());
			
			// 发送邮件
			for (Player atkPlayer : atkPlayers) {
				PveFightMail.Builder selfMail = attackMail.clone();
				// 添加奖励信息
				if (mailRewards != null) {
					List<ItemInfo> awardList = mailRewards.getReward(atkPlayer.getId());
					if (awardList != null) {
						awardList.forEach(e -> selfMail.addItems(e.toRewardItem()));
					}
				}
				Object[] atktips = {};
				switch (attackMailId) {
				case ATTACK_FOGGY_SUCC_TO_FROM:
				case ATTACK_FOGGY_FAILED_TO_FROM:
					int leader = 2; //2表示单打，1表示集结队长，0表示集结队员
					if (atkPlayers.size() > 1) {
						leader = atkPlayer.getId().equals(atkPlayers.get(0).getId()) ? 1: 0;
					}
					atktips = new Object[] { monsterId, leader };
					break;
				case GHOST_TOWER_MONSTER_ATTACK_SUCC_TO_FROM:
				case GHOST_TOWER_MONSTER_ATTACK_FAILED_TO_FROM:
				case ATTACK_FGYL_BUILD_SUCC_TO_FROM_PVE:
					atktips = new Object[] { monsterId };
					break;
				case ATTACK_SUPER_WEAPON_PVE_SUCC_TO_FROM:
				case ATTACK_SUPER_WEAPON_PVE_FAILED_TO_FROM:
				case ATTACK_XZQ_PVE_SUCC_TO_FROM:
				case ATTACK_XZQ_PVE_FAILED_TO_FROM:
				case ATTACK_FORTRESS_PVE_SUCC_TO_FROM:
				case ATTACK_FORTRESS_PVE_FAILED_TO_FROM:
				case ATTACK_YQZZ_BUILD_SUCC_TO_FROM_PVE:
				case ATTACK_YQZZ_BUILD_FAILED_TO_FROM_PVE:
					atktips = new Object[] { attackMail.getBattleX(), attackMail.getBattleY() };
					break;
				case SPACE_MECHA_STRONG_HOLD_FIGHT_WIN:
					int times = SpaceMechaService.getInstance().getAtkStrongHoldAwardTimesToday(atkPlayer);
					StrongHoldWorldPoint strongHoldPoint = (StrongHoldWorldPoint) point;
					extramParams.add(String.valueOf(strongHoldPoint.getX()));
					extramParams.add(String.valueOf(strongHoldPoint.getY()));
					extramParams.add(String.valueOf(strongHoldPoint.getStrongHoldId()));
					extramParams.add(String.valueOf(times));
					break;
				case SPACE_MECHA_STRONG_HOLD_FIGHT_FAILED:
					times = SpaceMechaService.getInstance().getAtkStrongHoldAwardTimesToday(atkPlayer);
					StrongHoldWorldPoint strongHoldPoint1 = (StrongHoldWorldPoint) point;
					extramParams.add(String.valueOf(strongHoldPoint1.getX()));
					extramParams.add(String.valueOf(strongHoldPoint1.getY()));
					extramParams.add(String.valueOf(strongHoldPoint1.getStrongHoldId()));
					extramParams.add(String.valueOf(times));
					break;
				case SPACE_MECHA_STRONGHOLD_BROKEN:
					times = SpaceMechaService.getInstance().getAtkStrongHoldAwardTimesToday(atkPlayer);
					strongHoldPoint = (StrongHoldWorldPoint) point;
					HawkTuple2<Integer, Integer> tuple = strongHoldPoint.getEffectTuple();
					extramParams.add(String.valueOf(strongHoldPoint.getX()));
					extramParams.add(String.valueOf(strongHoldPoint.getY()));
					extramParams.add(String.valueOf(strongHoldPoint.getStrongHoldId()));
					extramParams.add(String.valueOf(tuple.first));
					extramParams.add(String.valueOf(tuple.second));
					extramParams.add(String.valueOf(times));
					break;
				default:
					atktips = new String[] { atkPlayer.getName() };
					break;
				}
				
				selfMail.addAllExtraParam(extramParams);
				MailParames.Builder paraBuilder = MailParames.newBuilder()
						.setPlayerId(atkPlayer.getId())
						.setMailId(attackMailId)
						.addContents(selfMail)
						.addMidTitles(monsterId)
						.addSubTitles(monsterId)
						.setDuntype(battleOutcome.getDuntype())
						.addTips(atktips);
				sendMail(paraBuilder.build());
			}
		}
		
		// 需要发送防御方邮件
		if( defenseMailId != null){
			defenseMail.setSelfPlayer(defMail.getSelfPlayer());
			defenseMail.setOppPlayer(atkMail.getSelfPlayer());
			defenseMail.setMonsterId(monsterId);
			defenseMail.setSelfFight(defMail.getSelfFight());
			defenseMail.setOppFight(atkMail.getSelfFight());
			defenseMail.addAllSelfArmy(defMail.getSelfArmyList());
			defenseMail.addAllOppArmy(atkMail.getSelfArmyList());
			defenseMail.addAllSelfEffs(defMail.getSelfEffsList());
			defenseMail.addAllOppfEffs(atkMail.getSelfEffsList());
			defenseMail.setBattleX(defMail.getBattleX());
			defenseMail.setBattleY(defMail.getBattleY());
			defenseMail.setBattleAnalysis(analysisInfo.getDefAnalysis());
			defenseMail.setBattlePrompt(analysisInfo.getDefPrompt());
			defenseMail.setBattleGreat(analysisInfo.getBattleGreat());
			defenseMail.addAllExtraParam(extramParams);
			
			// 发送邮件
			for (Player defPlayer : defPlayers) {
				PveFightMail.Builder selfMail = defenseMail.clone();
				// 添加奖励信息
				if (mailRewards != null) {
					List<ItemInfo> awardList = mailRewards.getReward(defPlayer.getId());
					if (awardList != null) {
						awardList.forEach(e -> selfMail.addItems(e.toRewardItem()));
					}
				}
				
				MailParames.Builder paraBuilder = MailParames.newBuilder()
						.setPlayerId(defPlayer.getId())
						.setMailId(defenseMailId)
						.addContents(selfMail)
						.setDuntype(battleOutcome.getDuntype())
						.addSubTitles(monsterId);
				sendMail(paraBuilder.build());
			}
		}
		
	}

	/**
	 * 添加战斗分析和战斗提示
	 * 
	 * @param battleIncome
	 * @param battleOutcome
	 * @param attackMail
	 * @param defenseMail
	 * @param isWin
	 */
	public BattleAnalysisInfo makeBattleAnalysis(IBattleIncome battleIncome, BattleOutcome battleOutcome, boolean isWin) {
		BattleAnalysisInfo analysisInfo = new BattleAnalysisInfo();
		ConfigIterator<MailCombatReminderCfg> it = HawkConfigManager.getInstance().getConfigIterator(MailCombatReminderCfg.class);
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(battleIncome.getBattle().getPointId());
		Map<Integer, Integer> dfMap = new HashMap<Integer, Integer>();
		Map<Integer, Integer> resMap = new HashMap<Integer, Integer>();
		Map<Integer, Integer> baseMap = new HashMap<Integer, Integer>();
		while (it.hasNext()) {
			MailCombatReminderCfg cfg = it.next();
			if (cfg.getDefaultWeight() > 0) {
				dfMap.put(cfg.getId(), cfg.getDefaultWeight());
			}
			if (cfg.getBaseWeight() > 0) {
				baseMap.put(cfg.getId(), cfg.getBaseWeight());
			}
			if (cfg.getResourceWeight() > 0) {
				resMap.put(cfg.getId(), cfg.getResourceWeight());
			}
		}

		int id = 0;
		if (point != null && point.getPointType() == WorldPointType.PLAYER_VALUE) {
			id = HawkRand.randomWeightObject(baseMap);
		} else if (point != null && point.getPointType() == WorldPointType.RESOURCE_VALUE) {
			id = HawkRand.randomWeightObject(resMap);
		} else {
			id = HawkRand.randomWeightObject(dfMap);
		}
		analysisInfo.setAtkPrompt(id);
		analysisInfo.setDefPrompt(id);

		// 集结战斗
		if (battleIncome.isMassAtk()) {
			if (isWin) {
				analysisInfo.setAtkAnalysis(GsConst.FightMailAnalysis.MASS_ATK_WIN_ATK);
				analysisInfo.setDefAnalysis(GsConst.FightMailAnalysis.MASS_ATK_WIN_DEF);
			} else {
				analysisInfo.setAtkAnalysis(GsConst.FightMailAnalysis.MASS_ATK_FAIL_ATK);
				analysisInfo.setDefAnalysis(GsConst.FightMailAnalysis.MASS_ATK_FAIL_DEF);
			}
			return analysisInfo;
		}

		// 援助战斗
		if (battleIncome.isAssitanceDef()) {
			if (isWin) {
				analysisInfo.setAtkAnalysis(GsConst.FightMailAnalysis.AISS_ATK_WIN_ATK);
				analysisInfo.setDefAnalysis(GsConst.FightMailAnalysis.AISS_ATK_WIN_DEF);
			} else {
				analysisInfo.setAtkAnalysis(GsConst.FightMailAnalysis.AISS_ATK_FAIL_ATK);
				analysisInfo.setDefAnalysis(GsConst.FightMailAnalysis.AISS_ATK_FAIL_DEF);
			}
			return analysisInfo;
		}
		List<ArmyInfo> winArmyList = null;
		List<ArmyInfo> loseArmyList = null;
		// 大胜 大败
		Player atkLeader = battleIncome.getAtkPlayers().get(0);
		Player defLeader = battleIncome.getDefPlayers().get(0);
		if (isWin) {
			winArmyList = battleOutcome.getBattleArmyMapAtk().get(atkLeader.getId());
			loseArmyList = battleOutcome.getBattleArmyMapDef().get(defLeader.getId());
		} else {
			winArmyList = battleOutcome.getBattleArmyMapDef().get(defLeader.getId());
			loseArmyList = battleOutcome.getBattleArmyMapAtk().get(atkLeader.getId());
		}
		double winDisBattle = getDisBattlePoint(winArmyList);
		double loseDisBattle = getDisBattlePoint(loseArmyList);
		int round = battleIncome.getBattle().getBattleRound();

		int roundParam = ConstProperty.getInstance().getBattleReportBout();
		double grateParam = ConstProperty.getInstance().getBattleReportPower();
		double littleParam = ConstProperty.getInstance().getBattleReportspecific();

		double disPercent = winDisBattle == 0 ? 0 : loseDisBattle / winDisBattle;
		if (round > roundParam) {
			if (winDisBattle == 0 || disPercent >= grateParam) {
				if (isWin) {
					analysisInfo.setAtkAnalysis(GsConst.FightMailAnalysis.GREAT_WIN);
					analysisInfo.setDefAnalysis(GsConst.FightMailAnalysis.GREAT_FAIL);
				} else {
					analysisInfo.setAtkAnalysis(GsConst.FightMailAnalysis.GREAT_FAIL);
					analysisInfo.setDefAnalysis(GsConst.FightMailAnalysis.GREAT_WIN);
				}
				analysisInfo.setBattleGreat(1);
				return analysisInfo;
			} else if (disPercent < littleParam) {
				if (isWin) {
					analysisInfo.setAtkAnalysis(GsConst.FightMailAnalysis.LITTLE_WIN);
					analysisInfo.setDefAnalysis(GsConst.FightMailAnalysis.LITTLE_FAIL);
				} else {
					analysisInfo.setAtkAnalysis(GsConst.FightMailAnalysis.LITTLE_FAIL);
					analysisInfo.setDefAnalysis(GsConst.FightMailAnalysis.LITTLE_WIN);
				}
				return analysisInfo;
			}
		}

		List<ArmyInfo> armyList = null;
		// 防守方兵种单一
		if (isWin) {
			armyList = battleOutcome.getBattleArmyMapDef().get(defLeader.getId());
			if (armyList == null || armyList.size() <= 2) {
				analysisInfo.setAtkAnalysis(GsConst.FightMailAnalysis.SOLDIER_LESS_WIN);
				analysisInfo.setDefAnalysis(GsConst.FightMailAnalysis.SOLDIER_LESS_FAIL);
				return analysisInfo;
			}
		} else {
			armyList = battleOutcome.getBattleArmyMapAtk().get(atkLeader.getId());
			if (armyList == null || armyList.size() <= ConstProperty.getInstance().getBattleReportKind()) {
				analysisInfo.setAtkAnalysis(GsConst.FightMailAnalysis.SOLDIER_LESS_FAIL);
				analysisInfo.setDefAnalysis(GsConst.FightMailAnalysis.SOLDIER_LESS_WIN);
				return analysisInfo;
			}
		}

		// 防守方是否缺少装甲坦克
		double tankNum = 0.0;
		double allSoldier = 0.0;
		for (ArmyInfo armyInfo : armyList) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
			if (cfg == null) {
				continue;
			}
			// 防御建筑
			if (cfg.getType() == SoldierType.TANK_SOLDIER_1_VALUE) {
				tankNum += armyInfo.getFreeCnt();
			}
			allSoldier += armyInfo.getFreeCnt();
		}
		if (allSoldier != 0 && tankNum / allSoldier <= ConstProperty.getInstance().getBattleReportTank()) {
			if (isWin) {
				analysisInfo.setAtkAnalysis(GsConst.FightMailAnalysis.TANK_SOLDIER_LESS_WIN);
				analysisInfo.setDefAnalysis(GsConst.FightMailAnalysis.TANK_SOLDIER_LESS_FAIL);
			} else {
				analysisInfo.setAtkAnalysis(GsConst.FightMailAnalysis.TANK_SOLDIER_LESS_FAIL);
				analysisInfo.setDefAnalysis(GsConst.FightMailAnalysis.TANK_SOLDIER_LESS_WIN);
			}
			return analysisInfo;
		}

		// 哪个兵种消灭数量最高
		List<ArmyInfo> list = null;
		if (isWin) {
			Player atkPlayer = battleIncome.getAtkPlayers().get(0);
			list = battleOutcome.getBattleArmyMapAtk().get(atkPlayer.getId());
		} else {
			Player atkPlayer = battleIncome.getDefPlayers().get(0);
			list = battleOutcome.getAftArmyMapDef().get(atkPlayer.getId());
		}

		int[] killCount = new int[4];
		for (ArmyInfo armyInfo : list) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
			if (cfg == null) {
				continue;
			}
			switch (cfg.getType()) {
			case SoldierType.TANK_SOLDIER_1_VALUE:
			case SoldierType.TANK_SOLDIER_2_VALUE:
				killCount[0] += armyInfo.getKillCount();
				break;
			case SoldierType.PLANE_SOLDIER_3_VALUE:
			case SoldierType.PLANE_SOLDIER_4_VALUE:
				killCount[1] += armyInfo.getKillCount();
				break;
			case SoldierType.FOOT_SOLDIER_5_VALUE:
			case SoldierType.FOOT_SOLDIER_6_VALUE:
				killCount[2] += armyInfo.getKillCount();
				break;
			case SoldierType.CANNON_SOLDIER_7_VALUE:
			case SoldierType.CANNON_SOLDIER_8_VALUE:
				killCount[3] += armyInfo.getKillCount();
				break;
			default:
				break;
			}
		}

		int idx = GameUtil.getMaxIndex(killCount);
		switch (idx) {
		case 0:
			if (isWin) {
				analysisInfo.setAtkAnalysis(GsConst.FightMailAnalysis.KILL_COUNT_TANK_WIN);
				analysisInfo.setDefAnalysis(GsConst.FightMailAnalysis.KILL_COUNT_TANK_FAIL);
			} else {
				analysisInfo.setAtkAnalysis(GsConst.FightMailAnalysis.KILL_COUNT_TANK_FAIL);
				analysisInfo.setDefAnalysis(GsConst.FightMailAnalysis.KILL_COUNT_TANK_WIN);
			}
			break;
		case 1:
			if (isWin) {
				analysisInfo.setAtkAnalysis(GsConst.FightMailAnalysis.KILL_COUNT_FLY_WIN);
				analysisInfo.setDefAnalysis(GsConst.FightMailAnalysis.KILL_COUNT_FLY_FAIL);
			} else {
				analysisInfo.setAtkAnalysis(GsConst.FightMailAnalysis.KILL_COUNT_FLY_FAIL);
				analysisInfo.setDefAnalysis(GsConst.FightMailAnalysis.KILL_COUNT_FLY_WIN);
			}
			break;
		case 2:
			if (isWin) {
				analysisInfo.setAtkAnalysis(GsConst.FightMailAnalysis.KILL_COUNT_SOLDIER_WIN);
				analysisInfo.setDefAnalysis(GsConst.FightMailAnalysis.KILL_COUNT_SOLDIER_FAIL);
			} else {
				analysisInfo.setAtkAnalysis(GsConst.FightMailAnalysis.KILL_COUNT_SOLDIER_FAIL);
				analysisInfo.setDefAnalysis(GsConst.FightMailAnalysis.KILL_COUNT_SOLDIER_WIN);
			}
			break;
		case 3:
			if (isWin) {
				analysisInfo.setAtkAnalysis(GsConst.FightMailAnalysis.KILL_COUNT_CANOON_WIN);
				analysisInfo.setDefAnalysis(GsConst.FightMailAnalysis.KILL_COUNT_CANOON_FAIL);
			} else {
				analysisInfo.setAtkAnalysis(GsConst.FightMailAnalysis.KILL_COUNT_CANOON_FAIL);
				analysisInfo.setDefAnalysis(GsConst.FightMailAnalysis.KILL_COUNT_CANOON_WIN);
			}
			break;
		default:
			break;
		}
		return analysisInfo;
	}
	
	/**
	 * 发送联盟锦标赛邮件
	 * @param playerId
	 * @param mailUid
	 * @param mailId
	 * @param builder
	 * @return
	 */
	private boolean sendGCFightMail(String playerId, String mailUid, MailId mailId, FightMail.Builder builder){
		MailParames.Builder paraBuilder = MailParames.newBuilder()
				.setPlayerId(playerId)
				.setUuid(mailUid)
				.setMailId(mailId)
				.addContents(builder);
		return sendMail(paraBuilder.build());
	}
	
	private boolean sendSimulateWarFightMail(String playerId, String mailUid, MailId mailId, FightMail.Builder builder){
		MailParames.Builder paraBuilder = MailParames.newBuilder()
				.setPlayerId(playerId)
				.setUuid(mailUid)
				.setMailId(mailId)
				.addContents(builder);
		return sendMail(paraBuilder.build());
	}

	/**
	 * 发送战斗报告邮件
	 * 
	 * @param playerId
	 * @param mailId
	 * @param builder
	 * @return
	 */
	private String sendFightMail(String playerId, MailId mailId, FightMail.Builder builder, String guildTag, Object[] tips) {
		try {
			MailParames.Builder paraBuilder = MailParames.newBuilder()
					.setPlayerId(playerId)
					.setMailId(mailId)
					.addContents(builder)
					.setDuntype(DungeonMailType.valueOf(builder.getDuntype()))
					.addMidTitles(builder.getOppPlayer().getGuildTag(),builder.getOppPlayer().getName())
					.addTips(tips);
			switch (mailId) {
			// 攻击玩家基地
			case ATTACK_BASE_SUCC_TO_FROM:
			case ATTACK_BASE_SUCC_TO_TARGET:
			case DUEL_ATTACK_BASE_SUCC_TO_FROM:
			case DUEL_ATTACK_BASE_SUCC_TO_TARGET:
			case ATTACK_BASE_FAILED_TO_FROM:
			case ATTACK_BASE_FAILED_TO_TARGET:
			case DUEL_ATTACK_BASE_FAILED_TO_FROM:
			case DUEL_ATTACK_BASE_FAILED_TO_TARGET:
				// 攻击资源点
			case ATTACK_RES_SUCC_TO_FROM:
			case ATTACK_RES_SUCC_TO_TARGET:
			case ATTACK_RES_FAILED_TO_FROM:
			case ATTACK_RES_FAILED_TO_TARGET:
				// 攻击驻扎点
			case ATTACK_CAMP_SUCC_TO_FROM:
			case ATTACK_CAMP_SUCC_TO_TARGET:
			case ATTACK_CAMP_FAILED_TO_FROM:
			case ATTACK_CAMP_FAILED_TO_TARGET:
				// 攻击联盟堡垒
			case ATTACK_GUILD_BASTION_SUCC_TO_FROM:
			case ATTACK_GUILD_BASTION_SUCC_TO_TARGET:
			case ATTACK_GUILD_BASTION_FAILED_TO_FROM:
			case ATTACK_GUILD_BASTION_FAILED_TO_TARGET:
				// 攻击玩家据点
			case ATTACK_STRONG_POINT_PVP_SUCC_TO_FROM:
			case ATTACK_STRONG_POINT_PVP_SUCC_TO_TARGET:
			case ATTACK_STRONG_POINT_PVP_FAILED_TO_FROM:
			case ATTACK_STRONG_POINT_PVP_FAILED_TO_TARGET:
				// 攻击寻宝资源点
			case ATTACK_TREASURE_HUNT_RES_SUCC_TO_FROM:
			case ATTACK_TREASURE_HUNT_RES_SUCC_TO_TARGET:
			case ATTACK_TREASURE_HUNT_RES_FAILED_TO_FROM:
			case ATTACK_TREASURE_HUNT_RES_FAILED_TO_TARGET:
				
				// 攻击联盟旗帜
			case ATTACK_WAR_FLAG_SUCC_TO_FROM:
			case ATTACK_WAR_FLAG_SUCC_TO_TARGET:
			case ATTACK_WAR_FLAG_FAILED_TO_FROM:
			case ATTACK_WAR_FLAG_FAILED_TO_TARGET:
				// 攻击泰伯利亚资源点
			case ATTACK_TW_RES_SUCC_TO_FROM:
			case ATTACK_TW_RES_SUCC_TO_TARGET:
			case ATTACK_TW_RES_FAILED_TO_FROM:
			case ATTACK_TW_RES_FAILED_TO_TARGET:
				// 攻击泰伯利亚资源点
			case ATTACK_DYZZ_RES_SUCC_TO_FROM:
			case ATTACK_DYZZ_RES_SUCC_TO_TARGET:
			case ATTACK_DYZZ_RES_FAILED_TO_FROM:
			case ATTACK_DYZZ_RES_FAILED_TO_TARGET:
				// 攻击圣诞宝箱
			case ATTACK_CHRISTMAS_SUCC_TO_FROM:
			case ATTACK_CHRISTMAS_SUCC_TO_TARGET:
			case ATTACK_CHRISTMAS_FAILED_TO_FROM:
			case ATTACK_CHRISTMAS_FAILED_TO_TARGET:
//				paraBuilder.addSubTitles(builder.getSelfFight().getKillSoldier(), builder.getOppFight().getKillSoldier());
				break;
			// 攻击首都
			case ATTACK_CAPITAL_SUCC_TO_FROM:
			case ATTACK_CAPITAL_SUCC_TO_TARGET:
			case ATTACK_CAPITAL_FAILED_TO_FROM:
			case ATTACK_CAPITAL_FAILED_TO_TARGET:
			case ATTACK_CAPITAL_TOWER_SUCC_TO_FROM:
			case ATTACK_CAPITAL_TOWER_SUCC_TO_TARGET:
			case ATTACK_CAPITAL_TOWER_FAILED_TO_FROM:
			case ATTACK_CAPITAL_TOWER_FAILED_TO_TARGET:
				paraBuilder.addSubTitles(guildTag);
				break;
			case ATTACK_SUPER_WEAPON_SUCC_TO_FROM:
			case ATTACK_SUPER_WEAPON_SUCC_TO_TARGET:
			case ATTACK_SUPER_WEAPON_FAILED_TO_FROM:
			case ATTACK_XZQ_SUCC_TO_FROM:
			case ATTACK_XZQ_SUCC_TO_TARGET:
			case ATTACK_XZQ_FAILED_TO_FROM:
			case ATTACK_FORTRESS_SUCC_TO_FROM:
			case ATTACK_FORTRESS_SUCC_TO_TARGET:
			case ATTACK_FORTRESS_FAILED_TO_FROM:
			case ATK_PYLON_SUCCESS:
			case DEF_PYLON_FAIL:
			case ATK_PYLON_FAIL:
			case DEF_PYLON_SUCCESS:
			case ATK_XQHX_PYLON_SUCCESS:
			case DEF_XQHX_PYLON_FAIL:
			case ATK_XQHX_PYLON_FAIL:
			case DEF_XQHX_PYLON_SUCCESS:
				paraBuilder.addSubTitles(builder.getBattleX(), builder.getBattleY());
				break;
			case ATTACK_TW_BUILD_SUCC_TO_FROM:
			case ATTACK_TW_BUILD_SUCC_TO_TARGET:
			case ATTACK_TW_BUILD_FAILED_TO_FROM:
				paraBuilder.addSubTitles(builder.getBattleX(), builder.getBattleY());
				paraBuilder.addTitles(builder.getBattleX(), builder.getBattleY());
				break;
			case ATTACK_TW_BUILD_FAILED_TO_TARGET:
				paraBuilder.addSubTitles(builder.getBattleX(), builder.getBattleY(), builder.getSelfPlayer().getName());
				paraBuilder.addTitles(builder.getBattleX(), builder.getBattleY(), builder.getSelfPlayer().getName());
				break;
			case ATTACK_SW_ONE_BUILD_SUCC_TO_FROM:
			case ATTACK_SW_ONE_BUILD_SUCC_TO_TARGET:
			case ATTACK_SW_ONE_BUILD_FAILED_TO_FROM:
			case ATTACK_SW_BUILD_SUCC_TO_FROM:
			case ATTACK_SW_BUILD_SUCC_TO_TARGET:
			case ATTACK_SW_BUILD_FAILED_TO_FROM:
			case ATTACK_SW_TERMINAL_BUILD_SUCC_TO_FROM:
			case ATTACK_SW_TERMINAL_BUILD_SUCC_TO_TARGET:
			case ATTACK_SW_TERMINAL_BUILD_FAILED_TO_FROM:
				paraBuilder.addSubTitles(builder.getBattleX(), builder.getBattleY());
				paraBuilder.addTitles(builder.getBattleX(), builder.getBattleY());
				break;
			case ATTACK_SW_ONE_BUILD_FAILED_TO_TARGET:
			case ATTACK_SW_BUILD_FAILED_TO_TARGET:
			case ATTACK_SW_TERMINAL_BUILD_FAILED_TO_TARGET:
				paraBuilder.addSubTitles(builder.getBattleX(), builder.getBattleY(), builder.getSelfPlayer().getName());
				paraBuilder.addTitles(builder.getBattleX(), builder.getBattleY(), builder.getSelfPlayer().getName());
				break;
			case ATTACK_SUPER_WEAPON_FAILED_TO_TARGET:
			case ATTACK_FORTRESS_FAILED_TO_TARGET:
			case ATTACK_XZQ_FAILED_TO_TARGET:
				paraBuilder.addSubTitles(builder.getBattleX(), builder.getBattleY(), builder.getSelfPlayer().getName());
				break;
				
			case CYBORG_ATTACK_BUILD_SUCC_TO_FROM:
			case CYBORG_ATTACK_BUILD_SUCC_TO_TARGET:
			case CYBORG_ATTACK_BUILD_FAILED_TO_FROM:
				paraBuilder.addSubTitles(builder.getBattleX(), builder.getBattleY());
				paraBuilder.addTitles(builder.getBattleX(), builder.getBattleY());
				break;
			case CYBORG_ATTACK_BUILD_FAILED_TO_TARGET:
				paraBuilder.addSubTitles(builder.getBattleX(), builder.getBattleY(), builder.getSelfPlayer().getName());
				paraBuilder.addTitles(builder.getBattleX(), builder.getBattleY(), builder.getSelfPlayer().getName());
				break;
			case ATTACK_DYZZ_BUILD_SUCC_TO_FROM:
			case ATTACK_DYZZ_BUILD_SUCC_TO_TARGET:
			case ATTACK_DYZZ_BUILD_FAILED_TO_FROM:
				paraBuilder.addSubTitles(builder.getBattleX(), builder.getBattleY());
				paraBuilder.addTitles(builder.getBattleX(), builder.getBattleY());
				break;
			case ATTACK_DYZZ_BUILD_FAILED_TO_TARGET:
				paraBuilder.addSubTitles(builder.getBattleX(), builder.getBattleY(), builder.getSelfPlayer().getName());
				paraBuilder.addTitles(builder.getBattleX(), builder.getBattleY(), builder.getSelfPlayer().getName());
				break;
			case ATTACK_YQZZ_BUILD_SUCC_TO_FROM:
			case ATTACK_YQZZ_BUILD_SUCC_TO_TARGET:
			case ATTACK_YQZZ_BUILD_FAILED_TO_FROM:
				paraBuilder.addSubTitles(builder.getBattleX(), builder.getBattleY());
				paraBuilder.addTitles(builder.getBattleX(), builder.getBattleY());
				break;
			case ATTACK_YQZZ_BUILD_FAILED_TO_TARGET:
				paraBuilder.addSubTitles(builder.getBattleX(), builder.getBattleY(), builder.getSelfPlayer().getName());
				paraBuilder.addTitles(builder.getBattleX(), builder.getBattleY(), builder.getSelfPlayer().getName());
				break;
			case ATTACK_XQHX_BUILD_SUCC_TO_FROM:
			case ATTACK_XQHX_BUILD_SUCC_TO_TARGET:
			case ATTACK_XQHX_BUILD_FAILED_TO_FROM:
				paraBuilder.addSubTitles(builder.getBattleX(), builder.getBattleY());
				paraBuilder.addTitles(builder.getBattleX(), builder.getBattleY());
				break;
			case ATTACK_XQHX_BUILD_FAILED_TO_TARGET:
				paraBuilder.addSubTitles(builder.getBattleX(), builder.getBattleY(), builder.getSelfPlayer().getName());
				paraBuilder.addTitles(builder.getBattleX(), builder.getBattleY(), builder.getSelfPlayer().getName());
				break;
			case ATTACK_MEDALF_SUCC_TO_FROM	:
			case ATTACK_MEDALF_FAILED_TO_FROM :
			case ATTACK_MEDALF_SUCC_TO_TARGET:
			case ATTACK_MEDALF_FAILED_TO_TARGET:
				paraBuilder.addSubTitles(builder.getOppPlayer().getName());
				
			default:
				break;
			}
			paraBuilder.addSubTitles(builder.getSelfFight().getKillSoldier(), builder.getOppFight().getKillSoldier());
			MailParames build = paraBuilder.build();
			if(sendMail(build)){
				return build.getUuid();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return "";
	}

	/**
	 * 获取部队损失战力
	 * 
	 * @param armyList
	 * @return
	 */
	public double getDisBattlePoint(List<ArmyInfo> armyList) {
		double disBattlePoint = 0;
		for (ArmyInfo info : armyList) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, info.getArmyId());
			if (cfg == null) {
				continue;
			}
			disBattlePoint += (info.getWoundedCount() + info.getDeadCount()) * cfg.getPower();
		}
		return disBattlePoint;
	}

	/**
	 * 获取部队全部战力
	 * 
	 * @param armyList
	 * @return
	 */
	public double getTotalBattlePoint(List<ArmyInfo> armyList) {
		double totalBattlePoint = 0;
		for (ArmyInfo info : armyList) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, info.getArmyId());
			if (cfg == null) {
				continue;
			}
			totalBattlePoint += info.getTotalCount() * cfg.getPower();
		}
		return totalBattlePoint;
	}

	/** 发送剪塔杀伤 */
	public void sendTowerKillInfoMail(PlayerMarch march) {
		String towerAttackInfo = march.getMarchEntity().getTowerAttackInfo();
		if (StringUtils.isEmpty(towerAttackInfo)) {
			return;
		}
		HashMap<Integer, Integer> map = JSON.parseObject(towerAttackInfo, new TypeReference<HashMap<Integer, Integer>>() {
		});
		if (Objects.isNull(map) || map.isEmpty()) {
			return;
		}
		int killcount = map.values().stream().mapToInt(Integer::intValue).sum();
		MailParames.Builder playerParamesBuilder = MailParames.newBuilder().setPlayerId(march.getPlayerId())
				.setMailId(MailId.MARCH_TOWER_KILL_INFO)
				.addSubTitles(killcount)
				.addContents(killcount)
				.addTitles(killcount);

		FightMailService.getInstance().sendMail(playerParamesBuilder.build());

	}
}
