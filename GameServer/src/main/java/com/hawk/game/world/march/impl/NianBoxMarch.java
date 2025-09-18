package com.hawk.game.world.march.impl;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.config.WorldNianBoxCfg;
import com.hawk.game.item.AwardItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 年兽宝箱行军
 * @author golden
 *
 */
public class NianBoxMarch extends PlayerMarch implements BasedMarch {

	public NianBoxMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.NIAN_BOX_MARCH;
	}

	@Override
	public void onMarchReach(Player player) {
		
		int ghostBox = player.getData().getDailyDataEntity().getGhostBox();
		
		if (ghostBox >= WorldMapConstProperty.getInstance().getNianBoxReceiveLimit()) {
			player.sendError(HP.code.WORLD_NIAN_BOX_MARCH_C_VALUE, Status.Error.GHOST_BOX_TIMES_LIMIT_VALUE, 0);
			WorldMarchService.getInstance().onPlayerNoneAction(this, getMarchEntity().getReachTime());
			return;
		}
		
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(this.getTerminalId());
		
		if (worldPoint == null || worldPoint.getPointType() != WorldPointType.NIAN_BOX_VALUE) {
			sendDisapperaMail();
			WorldMarchService.getInstance().onPlayerNoneAction(this, getMarchEntity().getReachTime());
			
		} else {
			
			try {
				
				sendAwardMail();
				
				WorldNianBoxCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldNianBoxCfg.class, worldPoint.getMonsterId());
				if (cfg != null && cfg.needNotice()) {
					Const.NoticeCfgId noticeId = Const.NoticeCfgId.GHOST_9;
					ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.SPECIAL_BROADCAST, noticeId, null, this.getPlayer().getName(), worldPoint.getX(), worldPoint.getY());
				}
				
				player.getData().getDailyDataEntity().setGhostBox(ghostBox + 1);
				
				WorldMarchService.getInstance().onPlayerNoneAction(this, getMarchEntity().getReachTime());
				
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
			WorldPointService.getInstance().removeWorldPoint(worldPoint.getId(), true);
		}
		
	}

	/**
	 * 宝箱消失
	 */
	public void sendDisapperaMail() {
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setMailId(MailId.NIAN_BOX_NOT_FOUND)
				.setPlayerId(this.getPlayerId())
				.addContents(this.getMarchEntity().getTargetId())
				.build());
	}
	
	/**
	 * 奖励
	 */
	public void sendAwardMail() {
		WorldNianBoxCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WorldNianBoxCfg.class, Integer.valueOf(this.getMarchEntity().getTargetId()));
		if (cfg == null) {
			return;
		}
		
		AwardItems award = AwardItems.valueOf();
		award.addAwards(cfg.getAwards());
		
		Object[] title = new Object[1];
		Object[] subTitle = new Object[1];
		title[0] = cfg.getId();
		subTitle[0] = cfg.getId();
		
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setMailId(MailId.NIAN_BOX_AWARD)
				.setPlayerId(this.getPlayerId())
				.setRewards(award.getAwardItems())
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.addContents(cfg.getId())
				.addSubTitles(subTitle)
				.addTitles(title)
				.build());
	}
}
