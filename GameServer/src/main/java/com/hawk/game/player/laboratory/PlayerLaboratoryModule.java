package com.hawk.game.player.laboratory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.SuperLabLevelUpEvent;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBManager;
import org.hawk.helper.HawkAssert;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.alibaba.fastjson.JSON;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.LaboratoryCoreCfg;
import com.hawk.game.config.LaboratoryKVCfg;
import com.hawk.game.entity.LaboratoryEntity;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.msg.SuperLabLevelUpMsg;
import com.hawk.game.msg.SuperLabLvUpMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.laboratory.LaboratoryEnum.PowerBlockIndex;
import com.hawk.game.player.laboratory.LaboratoryEnum.PowerCoreIndex;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Hero.PBExchangeItem;
import com.hawk.game.protocol.Laboratory.PBChangeLaboryCoreReq;
import com.hawk.game.protocol.Laboratory.PBLaboryBlock;
import com.hawk.game.protocol.Laboratory.PBLaboryCore;
import com.hawk.game.protocol.Laboratory.PBLaboryPageInfo;
import com.hawk.game.protocol.Laboratory.PBLaboryPageSelectReq;
import com.hawk.game.protocol.Laboratory.PBLaboryRemakeCostResp;
import com.hawk.game.protocol.Laboratory.PBLaboryRemakeReq;
import com.hawk.game.protocol.Laboratory.PBLaboryRemakeResp;
import com.hawk.game.protocol.Laboratory.PBLaborySyncResp;
import com.hawk.game.protocol.Status;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.log.Action;
import com.hawk.sdk.msdk.entity.PayItemInfo;

/**
 * @author lwt
 * @date 2020年3月27日
 */
public class PlayerLaboratoryModule extends PlayerModule {

	public PlayerLaboratoryModule(Player player) {
		super(player);
	}

	@Override
	public boolean onTick() {
		return super.onTick();
	}

	@Override
	protected boolean onPlayerLogin() {
		pushLaboratoryList();

		return true;
	}

	private void pushLaboratoryList() {
		PBLaborySyncResp.Builder builder = PBLaborySyncResp.newBuilder();
		for (int i = 1; i <= getMaxPage(); i++) {
			Laboratory lab = getLaboratory(i, false);
			PBLaboryPageInfo page;
			if (Objects.nonNull(lab)) {
				page = lab.toPbObj();
			} else {
				page = emptyPage(i);
			}
			builder.addPage(page);
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_ALL_LABORATORY_PAGE, builder));
	}

	private PBLaboryPageInfo emptyPage(int i) {
		PBLaboryPageInfo.Builder builder = PBLaboryPageInfo.newBuilder();
		builder.setIndex(i);
		return builder.build();
	}

	/** 兑换稳定器 */
	@ProtocolHandler(code = HP.code.LABORATORY_EXCHANGE_ITEM_C_VALUE)
	private void onExchangeItem(HawkProtocol protocol) {
		PBExchangeItem req = protocol.parseProtocol(PBExchangeItem.getDefaultInstance());
		int toItemCount = req.getItemCount();
		int toItemId = req.getToItemId();

		HawkAssert.checkPositive(toItemCount);
		ItemCfg toitemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, toItemId); // 技能碎片道具
		ItemInfo cost = ItemInfo.valueOf(toitemCfg.getExchangeItem());
		if (Objects.isNull(toitemCfg) || cost.getItemId() != getPiceItemId()) {
			return;
		}
		cost.setCount(cost.getCount() * toItemCount);
		int maxLock = 0;
		for (LaboratoryEntity lab : player.getData().getLaboratoryEntityList()) {
			maxLock = Math.max(maxLock, lab.getLabObj().getPowerCoreLockEnergyNum());
		}
		if (player.getData().getItemNumByItemId(getPiceItemId()) - maxLock < cost.getCount()) {
			return;
		}

		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(cost, false);
		if (!consume.checkConsume(player, protocol.getType())) {
			return;
		}
		
		if (consume.getBuilder().hasAttrInfo() && consume.getBuilder().getAttrInfo().getDiamond() > 0) {
			ItemCfg itemCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, toItemId);
			if (itemCfg != null) {
				consume.addPayItemInfo(new PayItemInfo(String.valueOf(toItemId), itemCfg.getSellPrice(), toItemCount));
			}
		}
		consume.consumeAndPush(player, Action.LABORATORY_EXCHANGE_ITEM);// 扣除技能道具

		AwardItems awardItem = AwardItems.valueOf();
		awardItem.addItem(Const.ItemType.TOOL_VALUE, toItemId, toItemCount);
		awardItem.rewardTakeAffectAndPush(player, Action.LABORATORY_EXCHANGE_ITEM);

		player.responseSuccess(protocol.getType());
	}

	/**
	 * 更换和升级
	 */
	@ProtocolHandler(code = HP.code.LABORATORY_CHANGE_CORE_C_VALUE)
	private void onChangeLaboryCore(HawkProtocol protocol) {
		// -----------有行军不让改
		try {
			long marchCnt = WorldMarchService.getInstance().getPlayerMarchCount(player.getId());
			if (marchCnt > 0) {
				sendError(protocol.getType(), Status.Error.HAS_MARCH_IN_WORLD);
				return;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		// -----------有行军不让改

		PBChangeLaboryCoreReq req = protocol.parseProtocol(PBChangeLaboryCoreReq.getDefaultInstance());
		final int pageIndex = req.getPageIndex(); // 第几页

		Laboratory lab = getLaboratory(pageIndex, true);
		if (Objects.isNull(lab) || !lab.isPageUnlock()) {
			return;
		}
		
		for (PBLaboryCore ent : req.getChangeList()) {
			int index = ent.getIndex();
			int coreCfgId = ent.getCoreCfgId();
			if (coreCfgId != 0) {
				LaboratoryCoreCfg cfg = HawkConfigManager.getInstance().getConfigByKey(LaboratoryCoreCfg.class, coreCfgId);
				if (Objects.isNull(cfg) || index != cfg.getIndex()) {
					HawkLog.errPrintln("onChangeLaboryCore index{} cfgId{}", index, coreCfgId);
					return; // 有错 立即终止
				}
			}

			lab.getPowerCore(PowerCoreIndex.valueOf(index)).setPreSetCfgId(coreCfgId);
		}

		if (lab.getPowerCorePreLockEnergyNum() > player.getData().getItemNumByItemId(getPiceItemId())) { // 不够
			return;
		}

		int levelUpCount = 0, lastIndex = 0, lastCoreCfgId = 0;
		for (PBLaboryCore ent : req.getChangeList()) {
			int index = ent.getIndex();
			int coreCfgId = ent.getCoreCfgId();
			int oldCfgId = lab.getPowerCore(PowerCoreIndex.valueOf(index)).getCoreCfgId();
			lab.getPowerCore(PowerCoreIndex.valueOf(index)).setCoreCfgId(coreCfgId);
			if (coreCfgId > 0) {
				levelUpCount += coreCfgId - oldCfgId;
				lastIndex = index;
				lastCoreCfgId = coreCfgId;
			}
		}
		
		lab.notifyChanged();
		player.responseSuccess(protocol.getType());
		
		LaboratoryCoreCfg cfg = HawkConfigManager.getInstance().getConfigByKey(LaboratoryCoreCfg.class, lastCoreCfgId + 1);
		if (!Objects.isNull(cfg) && lastIndex == cfg.getIndex()) {
			int old = lab.getPowerCore(PowerCoreIndex.valueOf(lastIndex)).getPreSetCfgId();
			lab.getPowerCore(PowerCoreIndex.valueOf(lastIndex)).setPreSetCfgId(lastCoreCfgId + 1);
			HawkApp.getInstance().postMsg(player, SuperLabLvUpMsg.valueOf(levelUpCount, lab.getPowerCorePreLockEnergyNum()));
			lab.getPowerCore(PowerCoreIndex.valueOf(lastIndex)).setPreSetCfgId(old);
		}
		
		HawkApp.getInstance().postMsg(player, SuperLabLevelUpMsg.valueOf(lab.getPowerCoreLevel()));
		ActivityManager.getInstance().postEvent(new SuperLabLevelUpEvent(player.getId(), lab.getPowerCoreLevel()));
	}

	/**
	 * 随机魔方
	 */
	@ProtocolHandler(code = HP.code.LABORATORY_BLOCK_ROLL_C_VALUE)
	private void onPreRemakeBlock(HawkProtocol protocol) {
		PBLaboryRemakeReq req = protocol.parseProtocol(PBLaboryRemakeReq.getDefaultInstance());
		final int pageIndex = req.getPageIndex();
		Laboratory lab = getLaboratory(pageIndex, false);

		LaboratoryKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(LaboratoryKVCfg.class);
		if (!kvCfg.isBlockOpen()) {
			return;
		}
		List<ItemInfo> cost = new ArrayList<>(3);
		ItemInfo remakeCost = remakeCost();
		cost.add(remakeCost);
		if (req.getLockIndexCount() == 1) {
			cost.add(ItemInfo.valueOf(kvCfg.getLockOneCost()));
		} else if (req.getLockIndexCount() == 2) {
			cost.add(ItemInfo.valueOf(kvCfg.getLockTwoCost()));
		} else if (req.getLockIndexCount() == 3) {
			cost.add(ItemInfo.valueOf(kvCfg.getLockThreeCost()));
		}

		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(cost);

		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		consumeItems.consumeAndPush(player, Action.LABORATORY_REMAKE_BLOCK);

		lab.getPowerBlock().preRemake();
		for (PowerBlockIndex index : PowerBlockIndex.values()) {
			if (req.getLockIndexList().contains(index.INT_VAL)) {
				continue;
			}
			lab.getPowerBlock().randomBlock(index);
		}

		Map<PowerBlockIndex, Integer> pretalentMap = lab.getPowerBlock().getPretalentMap();
		List<PBLaboryBlock> list = new ArrayList<>(3);
		for (Entry<PowerBlockIndex, Integer> bi : pretalentMap.entrySet()) {
			PBLaboryBlock builder = PBLaboryBlock.newBuilder()
					.setIndex(bi.getKey().INT_VAL)
					.setCfgId(bi.getValue())
					.build();
			list.add(builder);
		}

		PBLaboryRemakeResp.Builder bulder = PBLaboryRemakeResp.newBuilder().addAllBlock(list).setPageIndex(pageIndex);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.LABORATORY_BLOCK_ROLL_S, bulder));
		if (!kvCfg.getRemakeCost().equals(remakeCost.toString())) { // 如果不是使用道具, 就加次数
			RedisProxy.getInstance().dayLaboratoryRemakeInc(player.getId());
		}
		syncPreRemakeBlockCost();
		
		LogUtil.lotLaboratoryOP(player,"1",JSON.toJSONString(pretalentMap), lab.serializPowerCore(), lab.serializPowerBlock());
	}

	/**
	 * 随机魔方
	 */
	@ProtocolHandler(code = HP.code.LABORATORY_BLOCK_ROLL_COST_C_VALUE)
	private void onPreRemakeBlockCost(HawkProtocol protocol) {
		syncPreRemakeBlockCost();
	}

	private void syncPreRemakeBlockCost() {
		PBLaboryRemakeCostResp.Builder resp = PBLaboryRemakeCostResp.newBuilder();
		resp.setCost(remakeCost().toString());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.LABORATORY_BLOCK_ROLL_COST_S, resp));
	}

	private ItemInfo remakeCost() {
		int todayRollcnt = RedisProxy.getInstance().dayLaboratoryRemakeCount(player.getId());
		LaboratoryKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(LaboratoryKVCfg.class);
		List<ItemInfo> goldList = ItemInfo.valueListOf(kvCfg.getRemakeGoldCost());
		int goldIndex = Math.min(todayRollcnt, goldList.size() - 1);

		ItemInfo goldCost = goldList.get(goldIndex);
		if (goldCost.getCount() == 0) {
			return goldCost;
		}

		ItemInfo tickCost = ItemInfo.valueOf(kvCfg.getRemakeCost());
		int ticketCnt = player.getData().getItemNumByItemId(tickCost.getItemId());
		if (ticketCnt > 0) {
			return tickCost;
		}

		return goldCost;
	}

	/**
	 * 确定上次随机魔方
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.LABORATORY_BLOCK_SELECT_C_VALUE)
	private void onRemakeBlock(HawkProtocol protocol) {
		PBLaboryRemakeReq req = protocol.parseProtocol(PBLaboryRemakeReq.getDefaultInstance());
		Laboratory lab = getLaboratory(req.getPageIndex(), false);
		lab.getPowerBlock().remakeBlock();

		lab.notifyChanged();
		player.responseSuccess(protocol.getType());
		
		LogUtil.lotLaboratoryOP(player,"2","{}", lab.serializPowerCore(), lab.serializPowerBlock());
	}
	
	@ProtocolHandler(code = HP.code2.LABORATORY_UNLOCK_PAGE_REQ_VALUE)
	private void onPageUnlock(HawkProtocol protocol) {
		PBLaboryPageSelectReq req = protocol.parseProtocol(PBLaboryPageSelectReq.getDefaultInstance());
		final int pageIndex = req.getPageIndex();
		if (pageIndex < 1 || pageIndex > getMaxPage()) {
			return;
		}
		Laboratory lab = getLaboratory(pageIndex, true);
		if (Objects.isNull(lab) || lab.isPageUnlock()) {
			return;
		}
		
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		consumeItems.addConsumeInfo(ItemInfo.valueListOf(lab.getPageCfg().getUnlockCost()));
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return;
		}
		consumeItems.consumeAndPush(player, Action.LABORATORY_UNLOCK_PAGE);
		lab.pageUnlock();
		lab.notifyChanged();
		player.responseSuccess(protocol.getType());
	}

	@ProtocolHandler(code = HP.code.LABORATORY_PAGE_SELECT_C_VALUE)
	private void onPageSelect(HawkProtocol protocol) {
		PBLaboryPageSelectReq req = protocol.parseProtocol(PBLaboryPageSelectReq.getDefaultInstance());
		final int pageIndex = req.getPageIndex();
		if (pageIndex < 1 || pageIndex > getMaxPage()) {
			return;
		}
		Laboratory lab = getLaboratory(pageIndex, true);
		if (Objects.isNull(lab) || !lab.isPageUnlock()) {
			return;
		}
		
		final int oldPage = Math.max(1, player.getEntity().getLaboratory());
		player.getEntity().setLaboratory(pageIndex);
		Laboratory oldLab = getLaboratory(oldPage, true);

		Set<EffType> allEff = new HashSet<>();
		allEff.addAll(oldLab.getEffVal().keySet());
		allEff.addAll(lab.getEffVal().keySet());
		player.getEffect().syncEffect(player, allEff.toArray(new EffType[allEff.size()]));
		player.getPush().syncPlayerInfo();
		player.responseSuccess(protocol.getType());
	}

	public Laboratory getLaboratory(int pageIndex, boolean createIfNull) {
		if (pageIndex < 1 || pageIndex > getMaxPage()) {
			throw new RuntimeException("pageIndex error " + pageIndex);
		}

		for (LaboratoryEntity lab : player.getData().getLaboratoryEntityList()) {
			if (lab.getPageIndex() == pageIndex) {
				return lab.getLabObj();
			}
		}
		if (!createIfNull) {
			return null;
		}

		LaboratoryEntity entity = new LaboratoryEntity();
		entity.setPlayerId(player.getId());
		entity.setPageIndex(pageIndex);
		Laboratory lab = Laboratory.create(entity);
		lab.loadEffVal();
		HawkDBManager.getInstance().create(entity);
		player.getData().getLaboratoryEntityList().add(entity);
		return lab;
	}

	private int getMaxPage() {
		LaboratoryKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(LaboratoryKVCfg.class);
		return kvCfg.getMaxPage();
	}

	private int getPiceItemId() {
		LaboratoryKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(LaboratoryKVCfg.class);
		return kvCfg.getLockItemId();
	}
}
