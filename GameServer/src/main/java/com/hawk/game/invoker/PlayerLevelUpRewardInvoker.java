package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.config.PlayerLevelExpCfg;
import com.hawk.game.item.AwardItems;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.log.Action;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.log.Source;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.HPPlayerLevelUpInfo;
import com.hawk.game.protocol.Reward.PlayerLevelUpReward;
import com.hawk.game.protocol.Reward.RewardInfo;
import com.hawk.game.protocol.Reward.RewardOrginType;

public class PlayerLevelUpRewardInvoker extends HawkMsgInvoker {
	private Player player;
	private int oldLevel;
	
	public PlayerLevelUpRewardInvoker(Player player, int oldLevel) {
		this.player = player;
		this.oldLevel = oldLevel;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		HPPlayerLevelUpInfo.Builder levelUpBuilder = HPPlayerLevelUpInfo.newBuilder();
		for (int level = oldLevel + 1; level <= player.getLevel(); level++) {
			PlayerLevelExpCfg levelExpCfg = HawkConfigManager.getInstance().getConfigByKey(PlayerLevelExpCfg.class, level);
			if (levelExpCfg == null || levelExpCfg.getBonusList().size() <= 0) {
				HawkLog.errPrintln("player level up reward failed, levelExpCfg error, playerId: {}, playerLv: {}, rewardLv: {}", player.getId(), player.getLevel(), level);
				continue;
			}

			BehaviorLogger.log4Service(player, Source.ATTR_CHANGE, Action.PLAYER_LEVEL_UP_AWARD,
					Params.valueOf("curLv", player.getLevel()), Params.valueOf("rewardLv", level));

			AwardItems award = AwardItems.valueOf();
			award.addItemInfos(levelExpCfg.getBonusList());
			RewardInfo.Builder rewrdInfo = award.rewardTakeAffectAndPush(player, Action.PLAYER_LEVEL_UP_AWARD, true, RewardOrginType.COMMANDER_REWARD);

			PlayerLevelUpReward.Builder builder = PlayerLevelUpReward.newBuilder();
			builder.setRewardInfo(rewrdInfo == null ? RewardInfo.newBuilder() : rewrdInfo);
			builder.setLevel(level);
			builder.setBattlePoint(levelExpCfg.getBattlePoint());
			PlayerLevelExpCfg preLevelExpCfg = HawkConfigManager.getInstance().getConfigByKey(PlayerLevelExpCfg.class, level - 1);
			if (preLevelExpCfg != null) {
				builder.setBattlePointBefore(preLevelExpCfg.getBattlePoint());
			}
			levelUpBuilder.addLevelUpInfo(builder);
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_LEVEL_UP_SYNC_S, levelUpBuilder));
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public int getOldLevel() {
		return oldLevel;
	}
	
}
