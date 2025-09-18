package com.hawk.game.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;

import com.hawk.game.config.ItemCfg;
import com.hawk.game.config.LaboratorySuperlabCfg;
import com.hawk.game.config.SuperLabCfg;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.laboratory.Laboratory;
import com.hawk.game.player.laboratory.LaboratoryEnum.PowerCoreIndex;
import com.hawk.game.player.laboratory.PlayerLaboratoryModule;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.ToolType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SuperLab.PBSuperLabItem;
import com.hawk.game.protocol.SuperLab.PBSuperLabItemComposeReq;
import com.hawk.game.protocol.SuperLab.PBSuperLabItemDeComposeReq;
import com.hawk.game.protocol.SuperLab.PBSuperLabJiHuo;
import com.hawk.game.protocol.SuperLab.PBSuperLabJiHuoLiShi;
import com.hawk.game.protocol.SuperLab.PBSuperLabShouCangReq;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;

/**
 * @author lwt
 * @date 2007年7月25日
 */
public class PlayerSuperLabModule extends PlayerModule {

	public PlayerSuperLabModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		historySync();
		oldSuperLabCancelBeforeLaboratory();
		return super.onPlayerLogin();
	}

	// 老用户超能转新
	public void oldSuperLabCancelBeforeLaboratory() {
		try {
			if (player.getData().getPlayerEntity().getSuperLab() == -1) {// 只对老用户检测
				return;
			}
			quXiaoJiHuo();
			decompseToLvlOne(HP.code.SUPERLAB_NoJihuo_VALUE);
			// 新旧转化
			PlayerLaboratoryModule module = player.getModule(GsConst.ModuleType.LABRATORY);
			List<String> hisList = RedisProxy.getInstance().superLabJiHuoHis(player.getId());
			int pageIndex = 2;
			for (String str : hisList) {
				int superLabId = NumberUtils.toInt(str);
				LaboratorySuperlabCfg scfg = HawkConfigManager.getInstance().getConfigByKey(LaboratorySuperlabCfg.class, superLabId);
				if (Objects.isNull(scfg)) {
					continue;
				}
				Laboratory pageOne = module.getLaboratory(pageIndex, true);
				List<Integer> list = scfg.getPageOneCores();
				for (PowerCoreIndex index : PowerCoreIndex.values()) {
					pageOne.getPowerCore(index).setCoreCfgId(list.get(index.INT_VAL - 1));
				}
				pageOne.getDbEntity().notifyUpdate();
				pageIndex++;
			}

			player.getData().getPlayerEntity().setSuperLab(-1);// 攻能作废

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private void historySync() {
		List<String> hisList = RedisProxy.getInstance().superLabJiHuoHis(player.getId());
		PBSuperLabJiHuoLiShi.Builder builder = PBSuperLabJiHuoLiShi.newBuilder();
		for (String str : hisList) {
			builder.addHistory(NumberUtils.toInt(str));
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SUPERLAB_JIHUOLISAHI_SYNC, builder));
	}

	/** 一健激活 */
	@ProtocolHandler(code = HP.code.SUPERLAB_JIHUOLISAHI_ADD_VALUE)
	private void onShouCangJiHuo(HawkProtocol protocol) {
		PBSuperLabShouCangReq req = protocol.parseProtocol(PBSuperLabShouCangReq.getDefaultInstance());
		if (req.getType() == 1) {
			int yijingJihuo = player.getData().getPlayerEntity().getSuperLab();
			if (yijingJihuo == req.getId()) {
				RedisProxy.getInstance().saveSuperLabJiHuo(player.getId(), req.getId() + "");
			}
		} else if (req.getType() == 2) {
			RedisProxy.getInstance().delSuperLabJiHuo(player.getId(), req.getId() + "");
		}
		historySync();
	}

	/** 一健激活 */
	@ProtocolHandler(code = HP.code.SUPERLAB_KUAI_SU_Jihuo_VALUE)
	private void onOneKeyJiHuo(HawkProtocol protocol) {
		PBSuperLabJiHuo req = protocol.parseProtocol(PBSuperLabJiHuo.getDefaultInstance());
		quXiaoJiHuo();

		// // 看当前材料是否够用
		// {
		// SuperLabCfg jiHuoCfg = HawkConfigManager.getInstance().getConfigByKey(SuperLabCfg.class, req.getId());
		// ConsumeItems consume = ConsumeItems.valueOf();
		// consume.addConsumeInfo(ItemInfo.valueListOf(jiHuoCfg.getNeedMaterial()));
		// if (consume.checkConsume(player)) {
		// consume.consumeAndPush(player, Action.SUPER_LAB_JiHuo);
		// player.getData().getPlayerEntity().setSuperLab(req.getId());
		// player.getPush().syncPlayerInfo();
		// player.getEffect().syncEffect(player, jiHuoCfg.effArr());
		// player.responseSuccess(protocol.getType());
		//
		// return;
		// }
		// }

		decompseToLvlOne(protocol.getType());
		///////////////////
		SuperLabCfg jiHuoCfg = HawkConfigManager.getInstance().getConfigByKey(SuperLabCfg.class, req.getId());
		ConsumeItems consume = ConsumeItems.valueOf();
		for (ItemInfo item : ItemInfo.valueListOf(jiHuoCfg.getNeedMaterial())) {
			ItemCfg dcCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, item.getItemId());
			ItemInfo dcCompose = ItemInfo.valueOf(dcCfg.getSuperLabDecompose()); // 分解可得量
			dcCompose.setCount(dcCompose.getCount() * item.getCount());
			consume.addItemConsume(dcCompose.getItemId(), (int) dcCompose.getCount());
		}

		if (!consume.checkConsume(player, protocol.getType())) {
			return;
		}
		consume.consumeAndPush(player, Action.SUPER_LAB_JiHuo);
		player.getData().getPlayerEntity().setSuperLab(req.getId());
		player.getPush().syncPlayerInfo();
		player.getEffect().syncEffect(player, jiHuoCfg.effArr());
		player.responseSuccess(protocol.getType());
		LogUtil.logSuperLabOp(player, 0, req.getId());
	}

	private void decompseToLvlOne(int protoType) {
		// 当前材料不够
		List<ItemEntity> decomposeList = new ArrayList<>();
		for (ItemEntity item : player.getData().getItemEntities()) {
			int id = item.getItemId();
			if (id > 1480001 && id <= 1480020 && item.getItemCount() > 0) {
				decomposeList.add(item);
			}
		}
		// 全分成一级的
		if (!decomposeList.isEmpty()) {
			ConsumeItems consume = ConsumeItems.valueOf();
			AwardItems award = AwardItems.valueOf();
			for (ItemEntity dc : decomposeList) {
				ItemCfg dcCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, dc.getItemId());
				ItemInfo dcCompose = ItemInfo.valueOf(dcCfg.getSuperLabDecompose()); // 分解可得量
				dcCompose.setCount(dcCompose.getCount() * dc.getItemCount());
				// 获得
				award.addItem(dcCompose);
				// 消耗
				consume.addItemConsume(dc.getItemId(), dc.getItemCount());
			}

			if (!consume.checkConsume(player, protoType)) {
				return;
			}
			consume.consumeAndPush(player, Action.SUPER_LAB_DeCompose);

			award.setCountCheck(false);
			award.rewardTakeAffectAndPush(player, Action.SUPER_LAB_DeCompose);

			LogUtil.logSuperLabItemOp(player, 1, ItemInfo.toString(consume.getItemsCopy()), ItemInfo.toString(award.getAwardItems()), totalLevelOneCount());
		}
	}

	/** 合成 */
	@ProtocolHandler(code = HP.code.SUPERLAB_Compose_VALUE)
	private void onSUPERLAB_Compose(HawkProtocol protocol) {
		PBSuperLabItemComposeReq req = protocol.parseProtocol(PBSuperLabItemComposeReq.getDefaultInstance());
		PBSuperLabItem theGet = req.getGet();
		ItemCfg theGetCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, theGet.getItemId());
		if (theGetCfg.getItemType() != ToolType.SUPER_LAB_STONE_VALUE) {
			return;
		}
		// 合成所需要的量
		ItemInfo composeCost = ItemInfo.valueOf(theGetCfg.getSuperLabCompose());
		composeCost.setCount(composeCost.getCount() * theGet.getItemCount());
		// 计算分解所得
		ConsumeItems consume = ConsumeItems.valueOf();
		ItemInfo deComposeGetAll = null;
		for (PBSuperLabItem dc : req.getConsumeList()) {
			ItemCfg dcCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, dc.getItemId());
			ItemInfo dcCompose = ItemInfo.valueOf(dcCfg.getSuperLabDecompose()); // 分解可得量
			dcCompose.setCount(dcCompose.getCount() * dc.getItemCount());

			if (Objects.isNull(deComposeGetAll)) {
				deComposeGetAll = dcCompose.clone();
			} else {
				deComposeGetAll.setCount(deComposeGetAll.getCount() + dcCompose.getCount());
			}

			// 消耗
			consume.addItemConsume(dc.getItemId(), dc.getItemCount());
		}

		if (!consume.checkConsume(player, protocol.getType()) || deComposeGetAll.getItemId() != composeCost.getItemId() || deComposeGetAll.getCount() < composeCost.getCount()) {
			return;
		}

		consume.consumeAndPush(player, Action.SUPER_LAB_Compose);
		AwardItems award = AwardItems.valueOf();
		award.addItem(ItemType.TOOL_VALUE, theGet.getItemId(), theGet.getItemCount());
		award.rewardTakeAffectAndPush(player, Action.SUPER_LAB_Compose);

		LogUtil.logSuperLabItemOp(player, 0, ItemInfo.toString(consume.getItemsCopy()), ItemInfo.toString(award.getAwardItems()), totalLevelOneCount());
		player.responseSuccess(protocol.getType());
	}

	@ProtocolHandler(code = HP.code.SUPERLAB_DeCompose_VALUE)
	private void onSUPERLAB_DeCompose(HawkProtocol protocol) {
		PBSuperLabItemDeComposeReq req = protocol.parseProtocol(PBSuperLabItemDeComposeReq.getDefaultInstance());
		ConsumeItems consume = ConsumeItems.valueOf();
		AwardItems award = AwardItems.valueOf();
		for (PBSuperLabItem dc : req.getConsumeList()) {
			ItemCfg dcCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, dc.getItemId());
			ItemInfo dcCompose = ItemInfo.valueOf(dcCfg.getSuperLabDecompose()); // 分解可得量
			dcCompose.setCount(dcCompose.getCount() * dc.getItemCount());
			// 获得
			award.addItem(dcCompose);
			// 消耗
			consume.addItemConsume(dc.getItemId(), dc.getItemCount());
		}

		if (!consume.checkConsume(player, protocol.getType())) {
			return;
		}
		consume.consumeAndPush(player, Action.SUPER_LAB_DeCompose);

		award.setCountCheck(false);
		award.rewardTakeAffectAndPush(player, Action.SUPER_LAB_DeCompose);

		LogUtil.logSuperLabItemOp(player, 1, ItemInfo.toString(consume.getItemsCopy()), ItemInfo.toString(award.getAwardItems()), totalLevelOneCount());
		player.responseSuccess(protocol.getType());
	}

	@ProtocolHandler(code = HP.code.SUPERLAB_Jihuo_VALUE)
	private void onSUPERLAB_Jihuo(HawkProtocol protocol) {
		PBSuperLabJiHuo req = protocol.parseProtocol(PBSuperLabJiHuo.getDefaultInstance());
		quXiaoJiHuo();

		SuperLabCfg jiHuoCfg = HawkConfigManager.getInstance().getConfigByKey(SuperLabCfg.class, req.getId());
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(ItemInfo.valueListOf(jiHuoCfg.getNeedMaterial()));
		if (!consume.checkConsume(player, protocol.getType())) {
			return;
		}
		consume.consumeAndPush(player, Action.SUPER_LAB_JiHuo);
		player.getData().getPlayerEntity().setSuperLab(req.getId());
		player.getPush().syncPlayerInfo();
		player.getEffect().syncEffect(player, jiHuoCfg.effArr());
		player.responseSuccess(protocol.getType());

		LogUtil.logSuperLabOp(player, 0, req.getId());
	}

	/** 取消激活 */
	private void quXiaoJiHuo() {
		int yijingJihuo = player.getData().getPlayerEntity().getSuperLab();
		if (yijingJihuo > 0) {
			SuperLabCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SuperLabCfg.class, yijingJihuo);
			AwardItems award = AwardItems.valueOf(cfg.getNeedMaterial());
			award.rewardTakeAffect(player, Action.SUPER_LAB_NoJiHuo);
			player.getData().getPlayerEntity().setSuperLab(0);
			player.getEffect().syncEffect(player, cfg.effArr());

			LogUtil.logSuperLabOp(player, 1, yijingJihuo);
		}
	}

	@ProtocolHandler(code = HP.code.SUPERLAB_NoJihuo_VALUE)
	private void onSUPERLAB_NoJihuo(HawkProtocol protocol) {
		// PBSuperLabNoJiHuo req = protocol.parseProtocol(PBSuperLabNoJiHuo.getDefaultInstance());
		quXiaoJiHuo();
		player.getPush().syncPlayerInfo();
		player.responseSuccess(protocol.getType());
	}

	private int totalLevelOneCount() {
		int total = 0;
		try {
			int yijingJihuo = player.getData().getPlayerEntity().getSuperLab();
			if (yijingJihuo > 0) {
				SuperLabCfg jiHuoCfg = HawkConfigManager.getInstance().getConfigByKey(SuperLabCfg.class, yijingJihuo);
				for (ItemInfo item : ItemInfo.valueListOf(jiHuoCfg.getNeedMaterial())) {
					ItemCfg dcCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, item.getItemId());
					// 分解可得量
					ItemInfo dcCompose = ItemInfo.valueOf(dcCfg.getSuperLabDecompose());

					total += dcCompose.getCount() * item.getCount();
				}
			}
			// 当前材料不够
			for (ItemEntity item : player.getData().getItemEntities()) {
				int id = item.getItemId();
				if (id >= 1480001 && id <= 1480020 && item.getItemCount() > 0) {
					ItemCfg dcCfg = HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, id);
					// 分解可得量
					ItemInfo dcCompose = ItemInfo.valueOf(dcCfg.getSuperLabDecompose());
					total += dcCompose.getCount() * item.getItemCount();
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return total;
	}

}
