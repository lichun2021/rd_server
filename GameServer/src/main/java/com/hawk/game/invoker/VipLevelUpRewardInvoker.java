package com.hawk.game.invoker;

import java.util.ArrayList;
import java.util.List;

import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.tuple.HawkTuple2;

import com.hawk.game.GsApp;
import com.hawk.game.config.VipCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.log.Action;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.log.Source;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.HPPlayerVipLevelUpInfo;
import com.hawk.game.protocol.Reward.PlayerVipLevelUpReward;
import com.hawk.game.protocol.Reward.RewardInfo;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.VipRelatedDateType;

public class VipLevelUpRewardInvoker extends HawkMsgInvoker {
	private Player player;
	private int oldLevel;
	
	public VipLevelUpRewardInvoker(Player player, int oldLevel) {
		this.player = player;
		this.oldLevel = oldLevel;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		if (player.getVipLevel() <= oldLevel) {
			return false;
		}
		
		HPPlayerVipLevelUpInfo.Builder vipLevelUpBuilder = HPPlayerVipLevelUpInfo.newBuilder();
		List<String> vipLevels = new ArrayList<>();
		boolean taken = RedisProxy.getInstance().getVipBoxStatus(player.getId(), 0);
		RedisProxy.getInstance().updateVipBoxStatus(player.getId(), 0, false);
		if (oldLevel > 0 && !taken) {
			vipLevels.add(String.valueOf(oldLevel));
		} else if (oldLevel == 0) {
			RedisProxy.getInstance().vipGiftRefresh(player.getId(), VipRelatedDateType.VIP_BENEFIT_TAKEN, GsApp.getInstance().getCurrentTime() - GsConst.DAY_MILLI_SECONDS);
		}
		
		for (int iLevel = oldLevel + 1; iLevel <= player.getVipLevel(); iLevel++) {
			vipLevels.add(String.valueOf(iLevel));
			RedisProxy.getInstance().updateVipBoxStatus(player.getId(), iLevel, false);
			VipCfg vipCfg = HawkConfigManager.getInstance().getConfigByKey(VipCfg.class, iLevel);
			if (vipCfg == null) {
				HawkLog.errPrintln("player vip level up reward failed, vipCfg error, playerId: {}, playerLv: {}, rewardLv: {}", 
						player.getId(), player.getLevel(), iLevel);
				continue;
			}

			BehaviorLogger.log4Service(player, Source.ATTR_CHANGE, Action.VIP_LEVEL_UP,
					Params.valueOf("curLv", player.getLevel()), Params.valueOf("rewardLv", iLevel));

			// 推送vip作用号变化
			player.getEffect().initEffectVip(player);

			PlayerVipLevelUpReward.Builder builder = PlayerVipLevelUpReward.newBuilder();
			builder.setLevel(iLevel);
			if (!vipCfg.getVipGiftItems().isEmpty()) {
				AwardItems award = AwardItems.valueOf();
				award.addItemInfos(vipCfg.getVipGiftItems());
				HawkTuple2<Boolean, RewardInfo.Builder> rewrdInfo = award.rewardTakeAffect(player, Action.VIP_LEVEL_UP);
				builder.setRewardInfo(rewrdInfo == null ? RewardInfo.newBuilder() : rewrdInfo.second);
			}
			vipLevelUpBuilder.addLevelUpInfo(builder);
		}
		
		vipLevels.remove(String.valueOf(player.getVipLevel()));
		if (vipLevels.size() > 0) {
			RedisProxy.getInstance().pushUnreceivedBenefitBox(player.getId(), vipLevels.toArray(new String[vipLevels.size()]));
			player.getPush().syncAllVipBoxStatus(true, false);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.VIP_LEVEL_UP_SYNC_S, vipLevelUpBuilder));
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public int getOldLevel() {
		return oldLevel;
	}

	public void setOldLevel(int oldLevel) {
		this.oldLevel = oldLevel;
	}
	
}
