package com.hawk.game.world.march.impl;

import com.hawk.game.config.TreasureHuntConstProperty;
import com.hawk.game.item.AwardItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.TreasureHuntType;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.service.WorldTreasureHuntService;

/**
 * 寻宝行军
 * @author golden
 *
 */
public class TreasureHuntMarch extends PlayerMarch implements BasedMarch {

	public TreasureHuntMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.TREASURE_HUNT;
	}

	@Override
	public void onMarchReach(Player player) {
		
		// 行军返回
		WorldMarchService.getInstance().onPlayerNoneAction(this, getMarchEntity().getReachTime());
		
		int treasureHuntType = TreasureHuntConstProperty.getInstance().randomTreasureHuntType();
		
		TreasureHuntType type = TreasureHuntType.valueOf(treasureHuntType);
		if (type == null) {
			WorldMarchService.logger.error("treasureHuntMarch reach, random treasure hunt type error, playerId:{}, treasureHuntType:{}", player.getId(), treasureHuntType);
			return;
		}
		
		// 事件处理
		doTreasureHuntMarchReach(player, type);
		
		// 推送寻宝事件
		player.getPush().pushTreasureHuntEvent(type);
	}

	/**
	 * 寻宝事件处理
	 * @param player
	 * @param type
	 */
	public void doTreasureHuntMarchReach(Player player, TreasureHuntType type) {
		
		WorldMarchService.logger.info("doTreasureHuntMarchReach, playerId:{}, type:{}", player.getId(), type);
		
		switch (type) {
		
		
		// 直接给奖励
		case TH_TYPE_REWARD:
			
			AwardItems award = AwardItems.valueOf();
			award.addAward(TreasureHuntConstProperty.getInstance().randomRewardId());
			
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setMailId(MailId.TH_REWARD)
					.setPlayerId(this.getPlayerId())
					.setRewards(award.getAwardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.build());
			
			LogUtil.logTreasureHuntTouceReward();
			break;
			
			
		// 世界生成野怪
		case TH_TYPE_MONSTER:
			WorldTreasureHuntService.getInstance().touchCreateMonster();
			
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(this.getPlayerId())
					.setMailId(MailId.TH_MONSTER)
					.build());
			
			// ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, Const.NoticeCfgId.REFRESH_TREASURE_HUNT_MONSTER, null, player.getName());
			break;
			
			
		// 世界生成资源点
		case TH_TYPE_RESOURCE:
			WorldTreasureHuntService.getInstance().touchCreateResource();
			
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(this.getPlayerId())
					.setMailId(MailId.TH_RESOURCE)
					.build());
			
			// ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, Const.NoticeCfgId.REFRESH_TREASURE_HUNT_RESOURCE, null, player.getName());
			break;
			
		default:
			break;
			
		}
	}
}
