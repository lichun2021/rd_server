package com.hawk.game.module;

import java.util.Arrays;
import java.util.List;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.CastSkillEvent;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.PlayerLevelExpCfg;
import com.hawk.game.config.ShopCfg;
import com.hawk.game.config.SkillCfg;
import com.hawk.game.config.TalentCfg;
import com.hawk.game.config.TalentLevelCfg;
import com.hawk.game.config.VipCfg;
import com.hawk.game.config.VipSuperCfg;
import com.hawk.game.controler.SystemControler;
import com.hawk.game.entity.TalentEntity;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.skill.talent.ITalentSkill;
import com.hawk.game.player.skill.talent.TalentSkillContext;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Player.PlayerFlagPosition;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Talent.HPCastTalentSkillReq;
import com.hawk.game.protocol.Talent.HPCastTalentSkillSucc;
import com.hawk.game.protocol.Talent.HPTalentChangeReq;
import com.hawk.game.protocol.Talent.HPTalentClearReq;
import com.hawk.game.protocol.Talent.HPTalentUpgradeReq;
import com.hawk.game.protocol.Talent.HPTalentUpgradeResp;
import com.hawk.game.protocol.Talent.TalentType;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventTalentUpgrade;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ControlerModule;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.log.Action;
import com.hawk.log.Source;

/**
 * 领主天赋模块
 *
 * @author lating 
 */
public class PlayerTalentModule extends PlayerModule {

	static final Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 构造函数
	 *
	 * @param player
	 */
	public PlayerTalentModule(Player player) {
		super(player);
	}

	/**
	 * 玩家登陆处理(数据同步)
	 */
	@Override
	protected boolean onPlayerLogin() {
		player.getPush().syncTalentInfo();
		player.getPush().syncTalentSkillInfo();
		
		for(ITalentSkill skill : TalentSkillContext.getInstance().getSkills()){
			skill.loginCheck(player);
		}
		return true;
	}
	
	@Override
	public boolean onTick() {
		List<TalentEntity> skills = player.getData().getTalentSkills();
		for (TalentEntity skill : skills) {
			if (skill.getSkillRefTime() < HawkTime.getMillisecond() && skill.getSkillState() == GsConst.TalentSkill.IN_CD) {
				skill.setSkillState(GsConst.TalentSkill.CAN_USE);
				player.getPush().syncTalentSkillInfo();
				break;
			}
		}
		return super.onTick();
	}
	
	/**
	 * 天赋切换
	 */
	@ProtocolHandler(code = HP.code.TALENT_CHANGE_C_VALUE)
	private boolean onTalentChange(HawkProtocol protocol) {
		HPTalentChangeReq req = protocol.parseProtocol(HPTalentChangeReq.getDefaultInstance());
		int talentType = req.getType();
		int talentTypeOld = player.getEntity().getTalentType();
		
		if (talentType == talentTypeOld || TalentType.valueOf(talentType) == null) {
			sendError(protocol.getType(), Status.Error.TALENT_CHANGE_FAILED_VALUE);
			return false;
		}
		
		//------------------别问为啥这么写！我也不知道~~--=￣ω￣=----------------
		// 玩家大本等级
		int cityLevel = player.getCityLv();
		
		if (TalentType.valueOf(talentType) == TalentType.TALENT_TYPE_2 && cityLevel < ConstProperty.getInstance().getUnlockTalentLine2NeedCityLevel()) {
			sendError(protocol.getType(), Status.Error.TALENT_CHANGE_FAILED_VALUE);
			return false;
		}
		
		if (TalentType.valueOf(talentType) == TalentType.TALENT_TYPE_3 && cityLevel < ConstProperty.getInstance().getUnlockTalentLine3NeedCityLevel()) {
			sendError(protocol.getType(), Status.Error.TALENT_CHANGE_FAILED_VALUE);
			return false;
		}
		
		if (TalentType.valueOf(talentType) == TalentType.TALENT_TYPE_4 && cityLevel < ConstProperty.getInstance().getUnlockTalentLine4NeedCityLevel()) {
			sendError(protocol.getType(), Status.Error.TALENT_CHANGE_FAILED_VALUE);
			return false;
		}
		
		if (TalentType.valueOf(talentType) == TalentType.TALENT_TYPE_5 && cityLevel < ConstProperty.getInstance().getUnlockTalentLine5NeedCityLevel()) {
			sendError(protocol.getType(), Status.Error.TALENT_CHANGE_FAILED_VALUE);
			return false;
		}
		
		if (TalentType.valueOf(talentType) == TalentType.TALENT_TYPE_6 && cityLevel < ConstProperty.getInstance().getUnlockTalentLine6NeedCityLevel()) {
			sendError(protocol.getType(), Status.Error.TALENT_CHANGE_FAILED_VALUE);
			return false;
		}
		
		if (TalentType.valueOf(talentType) == TalentType.TALENT_TYPE_7 && cityLevel < ConstProperty.getInstance().getUnlockTalentLine7NeedCityLevel()) {
			sendError(protocol.getType(), Status.Error.TALENT_CHANGE_FAILED_VALUE);
			return false;
		}
		
		if (TalentType.valueOf(talentType) == TalentType.TALENT_TYPE_8 && cityLevel < ConstProperty.getInstance().getUnlockTalentLine8NeedCityLevel()) {
			sendError(protocol.getType(), Status.Error.TALENT_CHANGE_FAILED_VALUE);
			return false;
		}
		
		// vip等级
		VipCfg vipCfg = HawkConfigManager.getInstance().getConfigByKey(VipCfg.class, player.getVipLevel());
		if (TalentType.valueOf(talentType) == TalentType.TALENT_TYPE_3 && (vipCfg == null || vipCfg.getUnlockTalentLine3() == 0)) {
			sendError(protocol.getType(), Status.Error.TALENT_CHANGE_FAILED_VALUE);
			return false;
		}
		
		if (TalentType.valueOf(talentType) == TalentType.TALENT_TYPE_4 && (vipCfg == null || vipCfg.getUnlockTalentLine4() == 0)) {
			sendError(protocol.getType(), Status.Error.TALENT_CHANGE_FAILED_VALUE);
			return false;
		}
		
		if (TalentType.valueOf(talentType) == TalentType.TALENT_TYPE_5 && (vipCfg == null || vipCfg.getUnlockTalentLine5() == 0)) {
			sendError(protocol.getType(), Status.Error.TALENT_CHANGE_FAILED_VALUE);
			return false;
		}
		
		VipSuperCfg vipSuperCfg = HawkConfigManager.getInstance().getConfigByKey(VipSuperCfg.class, player.getActivatedVipSuperLevel());
		if (TalentType.valueOf(talentType) == TalentType.TALENT_TYPE_6 && (vipSuperCfg == null || vipSuperCfg.getUnlockTalentLine6() == 0)) {
			sendError(protocol.getType(), Status.Error.TALENT_CHANGE_FAILED_VALUE);
			return false;
		}
		
		if (TalentType.valueOf(talentType) == TalentType.TALENT_TYPE_7 && (vipSuperCfg == null || vipSuperCfg.getUnlockTalentLine7() == 0)) {
			sendError(protocol.getType(), Status.Error.TALENT_CHANGE_FAILED_VALUE);
			return false;
		}
		
		if (TalentType.valueOf(talentType) == TalentType.TALENT_TYPE_8 && (vipSuperCfg == null || vipSuperCfg.getUnlockTalentLine8() == 0)) {
			sendError(protocol.getType(), Status.Error.TALENT_CHANGE_FAILED_VALUE);
			return false;
		}
		//---------------------------------------------------------------------------------
		
		
		// 是否可以免费切换天赋
		boolean FreeToExchangeTalent = (vipCfg != null) && vipCfg.getFreeToExchangeTalent() == 1;
		Action action = Action.TALENT_CHANGE;
		if (!FreeToExchangeTalent) {
			int shopId = ConstProperty.getInstance().getTalentExchangeItemSaleId();
			ShopCfg shopCfg = HawkConfigManager.getInstance().getConfigByKey(ShopCfg.class, shopId);
			int errCode = -1;
			if (req.getUseGold()) {
				action = Action.BUY_TALENT_CHANGE_ITEM;
				errCode = costGoldFromItem(protocol.getType(), shopCfg, 1, action);
			} else {
				action = Action.USE_TALENT_CHANGE_ITEM;
				errCode = costItem(protocol.getType(), shopCfg.getShopItemID(), 1, action);
			}
			
			if (errCode != Status.SysError.SUCCESS_OK_VALUE) {
				logger.error("talent change failed, playerId: {}, talentType: {}, errCode: {}", player.getId(), talentType, errCode);
				sendError(protocol.getType(), errCode);
				return false;
			}
		}

		player.getEntity().setTalentType(talentType);
		player.getPush().syncTalentInfo();
		player.getEffect().initEffectTalent(player);
		player.responseSuccess(protocol.getType());
		player.getPush().syncTalentSkillInfo();
		
		// 日志
		BehaviorLogger.log4Service(player, Source.TALENT_CHANGE, action,
				Params.valueOf("oldTalentType", talentTypeOld),
				Params.valueOf("newTalentType", talentType));

		return true;
	}

	/**
	 * 天赋升级
	 */
	@ProtocolHandler(code = HP.code.TALENT_UPGRADE_C_VALUE)
	private boolean onTalentUpgrade(HawkProtocol protocol) {
		HPTalentUpgradeReq cmd = protocol.parseProtocol(HPTalentUpgradeReq.getDefaultInstance());
		int talentId = cmd.getTalentId();
		int talentType = cmd.getType();
		int tarLevel = cmd.getTargetLevel();
		HawkAssert.checkPositive(talentId);
		HawkAssert.checkPositive(tarLevel);
		
		TalentLevelCfg talentLevelCfg = AssembleDataManager.getInstance().getTalentLevelCfg(talentId, tarLevel);
		TalentCfg talentCfg = HawkConfigManager.getInstance().getConfigByKey(TalentCfg.class, talentId);
		if (talentLevelCfg == null || talentCfg == null) {
			logger.error("talent upgrade failed, config error, playerId: {}, talentType: {}, talentId: {}, tarLevel: {}", 
					player.getId(), talentType, talentId, tarLevel);
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			return false;
		}

		//检查是否最大级
		PlayerLevelExpCfg levelCfg = player.getCurPlayerLevelCfg();
		int totalPoint = levelCfg.getSkillPoint();
		int usedPoint = 0;
		int curLevel = 0;
		List<TalentEntity> talentEntities = player.getData().getTalentEntities();
		for (TalentEntity talentEntity : talentEntities) {
			if (talentEntity.getType() != talentType) {
				continue;
			}
			usedPoint += talentEntity.getLevel();
			if (talentEntity.getTalentId() == talentId) {
				curLevel = talentEntity.getLevel();
			}
		}
		
		if (tarLevel <= curLevel) {
			logger.error("talent upgrade failed, tarLevel is lower, playerId: {}, talentType: {}, talentId: {}, tarLevel: {}, curLevel: {}", 
					player.getId(), talentType, talentId, tarLevel, curLevel);
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}
		
		//检查点数是否够用
		if (usedPoint + tarLevel - curLevel > totalPoint) {
			logger.error("talent upgrade failed, point not enough, playerId: {}, talentType: {}, talentId: {}, tarLevel: {}, curLevel: {}, usedPoint: {}, totalPoint: {}", 
					player.getId(), talentType, talentId, tarLevel, curLevel, usedPoint, totalPoint);
			sendError(protocol.getType(), Status.Error.TALENT_POINT_NOT_ENOUGH);
			return false;
		}

		//检查升级条件
		if (!checkCondition(talentCfg.getFrontTalents(), talentType)) {
			logger.error("talent upgrade failed, condition check failed, playerId: {}, talentType: {}, talentId: {}", 
					player.getId(), talentType, talentId);
			sendError(protocol.getType(), Status.Error.TALENT_CONDTION_NOT_ENOUGH);
			return false;
		}

		TalentEntity talentEntity = player.getData().getTalentByTalentId(talentId, talentType);
		if (talentEntity == null) {
			talentEntity = player.getData().createTalentEntity(talentId, talentType, tarLevel, talentLevelCfg);
		} else {
			talentEntity.setLevel(tarLevel);
		}

		//推送作用号更新
		if (talentType == player.getEntity().getTalentType() && !HawkOSOperator.isEmptyString(talentLevelCfg.getEffect())) {
			player.getEffect().initEffectTalent(player);
		}
		
		// 日志
		BehaviorLogger.log4Service(player, Source.TALENT_UPGRADE, Action.TALENT_UPGRADE,
				Params.valueOf("talentType", talentType), 
				Params.valueOf("talentId", talentId),
				Params.valueOf("curLevel", curLevel),
				Params.valueOf("tarLevel", tarLevel));

		HPTalentUpgradeResp.Builder resp = HPTalentUpgradeResp.newBuilder();
		resp.setResult(true);
		resp.setLevel(tarLevel);
		resp.setTalentId(talentId);
		resp.setType(talentType);
		
		MissionManager.getInstance().postMsg(player, new EventTalentUpgrade(talentId, curLevel, tarLevel));
		player.sendProtocol(HawkProtocol.valueOf(HP.code.TALENT_UPGRADE_S_VALUE, resp));

		if (talentEntity.getSkillId() != 0) {
			player.getPush().syncTalentSkillInfo();
		}
		
		GameUtil.setFlagAndPush(player, PlayerFlagPosition.ADD_TALENT_POINT, 1);
		return true;
	}

	/**
	 * 洗点
	 */
	@ProtocolHandler(code = HP.code.TALENT_CLEAR_C_VALUE)
	private boolean onTalentClear(HawkProtocol protocol) {
		HPTalentClearReq req = protocol.parseProtocol(HPTalentClearReq.getDefaultInstance());

		int shopId = ConstProperty.getInstance().getTalentResetItemSaleId();
		ShopCfg shopCfg = HawkConfigManager.getInstance().getConfigByKey(ShopCfg.class, shopId);
		Action action = Action.USE_TALENT_CLEAR_ITEM;
		int errCode = Status.SysError.SUCCESS_OK_VALUE;
		if (req.getUseGold()) {
			action = Action.BUY_TALENT_CLEAR_ITEM;
			errCode = costGoldFromItem(protocol.getType(), shopCfg, 1, action);
		} else {
			errCode = costItem(protocol.getType(), shopCfg.getShopItemID(), 1, action);
		}
		
		if (errCode != Status.SysError.SUCCESS_OK_VALUE) {
			logger.error("talent clear failed, playerId: {}, talentType: {}, errCode: {}", player.getId(), req.getType(), errCode);
			sendError(protocol.getType(), errCode);
			return false;
		}

		int talentType = req.getType();
		List<TalentEntity> talentEntities = player.getData().getTalentEntities();
		for (TalentEntity talentEntity : talentEntities) {
			if (talentType == talentEntity.getType() && talentEntity.getLevel() != 0) {
				talentEntity.setLevel(0);
			}
		}

		//推送作用号更新
		if (talentType == player.getEntity().getTalentType()) {
			player.getEffect().clearEffectTalent(player);
		}

		player.responseSuccess(protocol.getType());
		player.getEffect().initEffectTalent(player);
		player.getPush().syncTalentSkillInfo();
		// 日志
		BehaviorLogger.log4Service(player, Source.TALENT_CLEAR, action, 
				Params.valueOf("talentType", talentType));

		return true;
	}

	/**
	 * 使用天赋技能
	 */
	@ProtocolHandler(code = HP.code.CAST_TALENT_SKILL_C_VALUE)
	private boolean onCastTalentSkill(HawkProtocol protocol) {
		HPCastTalentSkillReq req = protocol.parseProtocol(HPCastTalentSkillReq.getDefaultInstance());
		int skillId = req.getSkillId();
		List<Integer> skillParamsList = req.getSkillParamsList();
		return castSkill(protocol, skillId, skillParamsList);
	}

	public boolean castSkill(HawkProtocol protocol, int skillId, List<Integer> skillParamsList) {
		if (SystemControler.getInstance().isSystemItemsClosed(ControlerModule.SKILL_USE, skillId)) {
			logger.error("skill use closed, skillId: {}", skillId);
			sendError(protocol.getType(), Status.SysError.SKILL_USE_OFF);
			return false;
		}
		
		// 参数错误
		if (skillId <= 0) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}
		
		ITalentSkill skill = TalentSkillContext.getInstance().getSkill(skillId);
		TalentEntity talentEntity = player.getData().getTalentSkill(skillId);
		
		// 技能未解锁 协议测试的时候会发任意技能.
		if (skill == null || talentEntity == null || talentEntity.getLevel() <= 0) {
			sendError(protocol.getType(), Status.Error.TALENT_SKILL_NOT_UNLOCK);
			return false;
		}
		
		// 配置文件错误
		SkillCfg skillCfg = HawkConfigManager.getInstance().getConfigByKey(SkillCfg.class, skillId);
		if (skillCfg == null) {
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			return false;
		}
		if (player.isInDungeonMap() && skillCfg.getTblyUse() != 1) {
			return false;
		}
		
		// 技能未刷新
		if (!skill.canCastSkill(talentEntity)) {
			sendError(protocol.getType(), Status.Error.TALENT_SKILL_NOT_REFRESH);
			return false;
		}
		
		// 技能互斥
		List<Integer> usedSkill = player.getCastedSkill();
		if (usedSkill.contains(skillCfg.getMutexSkills())) {
			sendError(protocol.getType(), Status.Error.TALENT_SKILL_MUTUAL_EXCLUSION_VALUE);
			return false;
		}
		
		ActivityManager.getInstance().postEvent(new CastSkillEvent(player.getId(), skillId));
		
		logger.info("onCastSkill, playerId:{}, skillId:{}", player.getId(), skillId);
		
		if(skill.isWorldSkill()){
			WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.USE_SKILL) {
				@Override
				public boolean onInvoke() {
					return doSkill(protocol, skillParamsList, skillId, skill, skillCfg);
				}
			});
		} else {
			return doSkill(protocol, skillParamsList, skillId, skill, skillCfg);
		}
		
		return true;
	}

	/**
	 * 使用技能
	 * @param protocol
	 * @param req
	 * @param skillId
	 * @param skill
	 * @param skillCfg
	 */
	private boolean doSkill(HawkProtocol protocol, List<Integer> skillParamsList, int skillId, ITalentSkill skill,
			SkillCfg skillCfg) {
		// 技能使用失败
		Result<?> result = skill.onCastSkill(player, skillCfg, skillParamsList);
		if (result.isFail()) {
			sendError(protocol.getType(), result.getStatus());
			return false;
		}
		
		// 技能进入cd
		skill.skillEnterCd(player, skillId, skillCfg);
		
		player.getPush().syncTalentSkillInfo();

		HPCastTalentSkillSucc.Builder builder = HPCastTalentSkillSucc.newBuilder();
		builder.setSkillId(skillId);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CAST_TALENT_SKILL_S, builder));
		
		int[] paramArr = Arrays.copyOf(skillParamsList.stream().mapToInt(Integer::intValue).toArray(), 5); 
		LogUtil.logSkillCasting(player,skillId,paramArr[0],paramArr[1],paramArr[2],paramArr[3],paramArr[4]);
		
		return true;
	}
	
	/**
	 * 检查升级前置条件
	 * @param frontTalent 前置天赋
	 * @param talentType 天赋类型
	 * @return true表示可以
	 */
	private boolean checkCondition(List<String> frontTalents, int talentType) {
		if (frontTalents == null || frontTalents.isEmpty()) {
			return true;
		}
		return frontTalents.stream().anyMatch(frontTalent -> (checkAndCondition(frontTalent, talentType)));
	}

	/**
	 * 检查升级前置条件
	 * @param frontTalent 前置天赋
	 * @param talentType 天赋类型
	 * @return true表示可以
	 */
	private boolean checkAndCondition(String frontTalent, int talentType) {
		if (HawkOSOperator.isEmptyString(frontTalent)) {
			return true;
		}
		
		List<String> aryAll = Arrays.asList(frontTalent.split(";"));
		for (String aryOne : aryAll) {
			int talentId = Integer.parseInt(aryOne.split("_")[0]);
			int talentLv = Integer.parseInt(aryOne.split("_")[1]);
			TalentEntity tmpEntity = player.getData().getTalentByTalentId(talentId, talentType);
			if (tmpEntity == null || tmpEntity.getLevel() < talentLv) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 玩家操作没有物品（如改名，行军加速，迁城等），调用协议时直接扣金币
	 * 
	 * @param hpCode 协议号
	 * @param shopId 商店里的商品常量
	 * @param itemId 道具ID常量
	 * @param count  道具数量
	 * @param action Action类型 错误码
	 */
	private int costGoldFromItem(int hpCode, ShopCfg shopCfg, int count, Action action) {
		//检查钱够不够，以及扣钱
        ItemInfo priceItem = shopCfg.getPriceItemInfo();
        priceItem.setCount(priceItem.getCount() * count);
        
        ConsumeItems consume = ConsumeItems.valueOf();
        consume.addConsumeInfo(priceItem, true);
		if (!consume.checkConsume(player, hpCode)) {
			return Status.Error.GOLD_NOT_ENOUGH_VALUE;
		}

		consume.consumeAndPush(player, action);
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 玩家操作扣物品（如改名，行军加速，迁城等），判断物品是否存在，足够
	 * 
	 * @param hpCode 协议号
	 * @param itemId 道具常量
	 * @param count  道具数量
	 * @param action 操作类型 错误码
	 */
	private int costItem(int hpCode, int itemId, int count, Action action) {
		// 是否存在本物品
		int itemCnt = player.getData().getItemNumByItemId(itemId);
		if (itemCnt == 0) {
			return Status.Error.ITEM_NOT_FOUND_VALUE;
		}

		// 物品配置
		ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId);
		if (itemCfg == null) {
			return Status.SysError.CONFIG_ERROR_VALUE;
		}

		//消耗物品
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addItemConsume(itemId, count);
		if (!consume.checkConsume(player, hpCode)) {
			return Status.Error.ITEM_NOT_ENOUGH_VALUE;
		}

		consume.consumeAndPush(player, action);

		return Status.SysError.SUCCESS_OK_VALUE;
	}
}
