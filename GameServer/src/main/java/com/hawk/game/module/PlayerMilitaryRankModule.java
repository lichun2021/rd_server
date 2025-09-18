package com.hawk.game.module;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.config.MilitaryRankCfg;
import com.hawk.game.item.AwardItems;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.util.GameUtil;
import com.hawk.log.Action;

/**
 * 军衔
 * @author golden
 *
 */
public class PlayerMilitaryRankModule extends PlayerModule {
	
	private static Logger logger = LoggerFactory.getLogger("Server");
	
	public PlayerMilitaryRankModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		player.getPush().syncMilitaryRankAwardState();
		return true;
	}
	
	/**
	 * 领取每日津贴
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.MILITARY_RANK_GET_REWARD_VALUE)
	private boolean onGetReward(HawkProtocol protocol) {
		boolean isReceived = player.getData().getDailyDataEntity().isMilitaryRankRecieve();
		if (isReceived) {
			return false;
		}
		
		// 军衔等级
		int militaryRank = GameUtil.getMilitaryRankByExp(player.getEntity().getMilitaryExp());
		
		AwardItems awardItems = getMilitaryRankAward(militaryRank);
		awardItems.rewardTakeAffectAndPush(player, Action.GET_MILITARY_RANK_REWARD, RewardOrginType.GET_MILITARY_REWARD);
		
		player.getData().getDailyDataEntity().setMilitaryRankRecieve(true);
		player.getPush().syncMilitaryRankAwardState();
		player.responseSuccess(protocol.getType());
		
		logger.info("get military rank award, playerId:{}, militaryRank:{}, award:{}", player.getId(), militaryRank, awardItems.toString());
		return true;
	}
	
	private AwardItems getMilitaryRankAward(int level) {
		AwardItems awardItems = AwardItems.valueOf();
		ConfigIterator<MilitaryRankCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(MilitaryRankCfg.class);
		while (configIterator.hasNext()) {
			MilitaryRankCfg cfg = configIterator.next();
			if (cfg.getRankLevel() == level) {
				awardItems.addItemInfos(cfg.getAwardList());
			}
		}
		return awardItems;
	}
}
