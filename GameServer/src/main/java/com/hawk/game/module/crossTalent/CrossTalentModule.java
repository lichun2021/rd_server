package com.hawk.game.module.crossTalent;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.ShopCfg;
import com.hawk.game.crossproxy.CrossSkillService;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.module.crossTalent.cfg.CrossTalentCfg;
import com.hawk.game.module.crossTalent.cfg.CrossTalentLevelCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.president.PresidentOfficier;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Talent.HPCrossTalentClearReq;
import com.hawk.game.protocol.Talent.HPCrossTalentInfoReq;
import com.hawk.game.protocol.Talent.HPCrossTalentUpgradeReq;
import com.hawk.game.protocol.Talent.HPCrossTalentUpgradeResp;
import com.hawk.log.Action;
import com.hawk.log.Source;

public class CrossTalentModule extends PlayerModule {

	static final Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 构造函数
	 *
	 * @param player
	 */
	public CrossTalentModule(Player player) {
		super(player);
	}

	/**
	 * 玩家登陆处理(数据同步)
	 */
	@Override
	protected boolean onPlayerLogin() {
		return true;
	}

	@Override
	public boolean onTick() {
		return super.onTick();
	}

	/**
	 * 详情
	 */
	@ProtocolHandler(code = HP.code2.CROSS_TALENT_INFO_C_VALUE)
	private void onTalentInfo(HawkProtocol protocol) {
		HPCrossTalentInfoReq cmd = protocol.parseProtocol(HPCrossTalentInfoReq.getDefaultInstance());
		CrossTalentCollect crossTalentCollect = CrossSkillService.getInstance().getCrossTalentCollect(cmd.getServerId());
		if (Objects.isNull(crossTalentCollect)) {
			return;
		}
		crossTalentCollect.talentInfoSync(player);

	}

	/**
	 * 天赋升级
	 */
	@ProtocolHandler(code = HP.code2.CROSS_TALENT_UPGRADE_C_VALUE)
	private boolean onTalentUpgrade(HawkProtocol protocol) {
		// 是否是是战时司令
		String fightPresident = RedisProxy.getInstance().getCrossFightPresident();
		boolean isFightPresident = !HawkOSOperator.isEmptyString(fightPresident) && player.getId().equals(fightPresident);
		if (!isFightPresident) {
			sendError(protocol.getType(), Status.CrossServerError.CROSS_UP_TALENT_PARM);
			return false;
		}
		
		HPCrossTalentUpgradeReq cmd = protocol.parseProtocol(HPCrossTalentUpgradeReq.getDefaultInstance());
		int talentId = cmd.getTalentId();
		int talentType = 0;
		int tarLevel = cmd.getTargetLevel();
		HawkAssert.checkPositive(talentId);
		HawkAssert.checkPositive(tarLevel);

		CrossTalentLevelCfg tarLevelCfg = HawkConfigManager.getInstance().getCombineConfig(CrossTalentLevelCfg.class, talentId, tarLevel);
		CrossTalentCfg crossTalentCfg = HawkConfigManager.getInstance().getConfigByKey(CrossTalentCfg.class, talentId);
		if (tarLevelCfg == null || crossTalentCfg == null) {
			logger.error("cross talent upgrade failed, config error, playerId: {}, talentType: {}, talentId: {}, tarLevel: {}",
					player.getId(), talentType, talentId, tarLevel);
			sendError(protocol.getType(), Status.SysError.CONFIG_ERROR);
			return false;
		}

		CrossTalentCollect crossTalentCollect = CrossSkillService.getInstance().getCrossTalentCollect(player.getMainServerId());
		// 检查是否最大级
		long totalPoint = crossTalentCollect.getTotalPoint();
		int usedPoint = 0;
		int curLevel = 0; // 当前在这个天赋等级
		int curPoint = 0; // 当前在这个天赋上花费的点数
		List<CrossTalentItem> talentEntities = crossTalentCollect.getTalentEntities();
		for (CrossTalentItem talentEntity : talentEntities) {
			if (talentEntity.getType() != talentType) {
				continue;
			}
			CrossTalentLevelCfg tlcfg = HawkConfigManager.getInstance().getCombineConfig(CrossTalentLevelCfg.class, talentEntity.getTalentId(), talentEntity.getLevel());
			usedPoint += tlcfg.getTotalPoint();
			if (talentEntity.getTalentId() == talentId) {
				curLevel = talentEntity.getLevel();
				curPoint = tlcfg.getTotalPoint();
			}
		}

		if (tarLevel <= curLevel) {
			logger.error("cross talent upgrade failed, tarLevel is lower, playerId: {}, talentType: {}, talentId: {}, tarLevel: {}, curLevel: {}",
					player.getId(), talentType, talentId, tarLevel, curLevel);
			sendError(protocol.getType(), Status.CrossServerError.CROSS_TALENT_CHANGEDD);
			return false;
		}

		// 检查点数是否够用
		if (usedPoint - curPoint + tarLevelCfg.getTotalPoint() > totalPoint) {
			logger.error("cross talent upgrade failed, point not enough, playerId: {}, talentType: {}, talentId: {}, tarLevel: {}, curLevel: {}, usedPoint: {}, totalPoint: {}",
					player.getId(), talentType, talentId, tarLevel, curLevel, usedPoint, totalPoint);
			sendError(protocol.getType(), Status.Error.TALENT_POINT_NOT_ENOUGH);
			return false;
		}

		// 检查升级条件
		if (!checkCondition(crossTalentCfg.getFrontTalents(), talentType)) {
			logger.error("cross talent upgrade failed, condition check failed, playerId: {}, talentType: {}, talentId: {}",
					player.getId(), talentType, talentId);
			sendError(protocol.getType(), Status.Error.TALENT_CONDTION_NOT_ENOUGH);
			return false;
		}

		CrossTalentItem crossTalentItem = crossTalentCollect.getTalentByTalentId(talentId, talentType);
		if (crossTalentItem == null) {
			crossTalentItem = crossTalentCollect.createCrossTalentItem(talentId, talentType, tarLevel, tarLevelCfg);
		} else {
			crossTalentItem.setLevel(tarLevel);
		}

		// 日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.CROSS_TALENT_UPGRADE,
				Params.valueOf("talentType", talentType),
				Params.valueOf("talentId", talentId),
				Params.valueOf("curLevel", curLevel),
				Params.valueOf("tarLevel", tarLevel));

		HPCrossTalentUpgradeResp.Builder resp = HPCrossTalentUpgradeResp.newBuilder();
		resp.setResult(true);
		resp.setLevel(tarLevel);
		resp.setTalentId(talentId);

		player.sendProtocol(HawkProtocol.valueOf(HP.code2.CROSS_TALENT_UPGRADE_S, resp));

		crossTalentCollect.notifyUpdate();
		crossTalentCollect.talentInfoSync(player);
		return true;
	}

	/**
	 * 洗点
	 */
	@ProtocolHandler(code = HP.code2.CROSS_TALENT_CLEAR_REQ_VALUE)
	private boolean onTalentClear(HawkProtocol protocol) {
		HPCrossTalentClearReq req = protocol.parseProtocol(HPCrossTalentClearReq.getDefaultInstance());

		int shopId = ConstProperty.getInstance().getTalentResetItemSaleId();
		ShopCfg shopCfg = HawkConfigManager.getInstance().getConfigByKey(ShopCfg.class, shopId);
		Action action = Action.CROSS_TALENT_CLEAR;
		int errCode = Status.SysError.SUCCESS_OK_VALUE;
		if (req.getUseGold()) {
			errCode = costGoldFromItem(protocol.getType(), shopCfg, 1, action);
		} else {
			errCode = costItem(protocol.getType(), shopCfg.getShopItemID(), 1, action);
		}

		if (errCode != Status.SysError.SUCCESS_OK_VALUE) {
			logger.error("cross talent clear failed, playerId: {}, talentType: {}, errCode: {}", player.getId(), 0, errCode);
			sendError(protocol.getType(), errCode);
			return false;
		}

		int talentType = 0;
		CrossTalentCollect crossTalentCollect = CrossSkillService.getInstance().getCrossTalentCollect(player.getMainServerId());
		List<CrossTalentItem> talentEntities = crossTalentCollect.getTalentEntities();
		for (CrossTalentItem crossTalentItem : talentEntities) {
			if (talentType == crossTalentItem.getType() && crossTalentItem.getLevel() != 0) {
				crossTalentItem.setLevel(0);
			}
		}

		player.responseSuccess(protocol.getType());
		crossTalentCollect.notifyUpdate();
		crossTalentCollect.talentInfoSync(player);
		// 日志
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, action,
				Params.valueOf("talentType", talentType));

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
			CrossTalentItem tmpEntity = CrossSkillService.getInstance().getCrossTalentCollect(player.getMainServerId()).getTalentByTalentId(talentId, talentType);
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
		// 检查钱够不够，以及扣钱
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

		// 消耗物品
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addItemConsume(itemId, count);
		if (!consume.checkConsume(player, hpCode)) {
			return Status.Error.ITEM_NOT_ENOUGH_VALUE;
		}

		consume.consumeAndPush(player, action);

		return Status.SysError.SUCCESS_OK_VALUE;
	}
}
