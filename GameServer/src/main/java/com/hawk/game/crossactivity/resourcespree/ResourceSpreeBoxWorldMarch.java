package com.hawk.game.crossactivity.resourcespree;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.game.config.AwardCfg;
import com.hawk.game.config.CrossBoxCfg;
import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.crossactivity.CActivityInfo;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.AwardItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.PlayerMarch;
import com.hawk.game.world.march.submarch.BasedMarch;
import com.hawk.game.world.service.WorldPointService;

public class ResourceSpreeBoxWorldMarch  extends PlayerMarch implements BasedMarch{

	public ResourceSpreeBoxWorldMarch(WorldMarch marchEntity) {
		super(marchEntity);
	}

	@Override
	public WorldMarchType getMarchType() {
		return WorldMarchType.RESOURCE_SPREE_BOX_MARCH;
	}

	@Override
	public void onMarchReach(Player player) {
		// 行军
		WorldMarch march = getMarchEntity();
		// 目标点
		int terminalId = march.getTerminalId();
		// 目标野怪
		int boxPointId = Integer.valueOf(march.getTargetId());
		// 点和怪信息
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(terminalId);
		// 点为空
		if (point == null || point.getPointType() != WorldPointType.RESOURCE_SPREE_BOX_VALUE) {
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			int[] pos = GameUtil.splitXAndY(terminalId);
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.RESOURCE_SPREE_BOX_DISAPPEAR)
					.addContents(pos[0], pos[1])
					.addTips(boxPointId)
					.build());
			WorldMarchService.logger.error("ResourceSpreeBoxWorldMarch march reach error, point null, playerId:{},terminalId:{}",player.getId(), terminalId);
			return;
		}
		//数量是否超标
		CActivityInfo crossInfo = CrossActivityService.getInstance().getActivityInfo();
		int termId = crossInfo.getTermId();
		int count = RedisProxy.getInstance().getCrossResourceBoxGetCount(player.getId(), termId);
		if(termId == 0 ||count >= CrossConstCfg.getInstance().getResBoxSoloMax()){
			WorldMarchService.getInstance().onPlayerNoneAction(this, HawkTime.getMillisecond());
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.RESOURCE_SPREE_BOX_COUNT_LIMIT)
					.addContents(point.getX(),point.getY())
					.addTips(boxPointId)
					.build());
			WorldMarchService.logger.error("ResourceSpreeBoxWorldMarch march reach error, box count limit, playerId:{},terminalId:{},count:{}",player.getId(), terminalId,count);
			return;
		}
		// 行军返回
		WorldMarchService.getInstance().onPlayerNoneAction(this, getMarchEntity().getReachTime());
		//发奖
		RedisProxy.getInstance().incrCrossResourceBoxGetCount(player.getId(), termId, 1);
		ResourceSpreeBoxWorldPoint boxPoint = (ResourceSpreeBoxWorldPoint) point;
		int cfgId = boxPoint.getResourceSpreeBoxId();
		CrossBoxCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CrossBoxCfg.class, cfgId);
		if(cfg != null){
			int randomProduction = cfg.getAward();
			AwardCfg awardCfg = HawkConfigManager.getInstance().
					getConfigByKey(AwardCfg.class, randomProduction);
			AwardItems radnomAwardItems = awardCfg.getRandomAward();
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setMailId(MailId.RESOURCE_SPREE_BOX_REWARD)
					.setPlayerId(this.getPlayerId())
					.setRewards(radnomAwardItems.getAwardItems())
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.addContents(point.getX(),point.getY())
					.addTips(boxPointId)
					.build());
		}
		//删除点
		WorldPointService.getInstance().removeWorldPoint(boxPoint.getX(), boxPoint.getY());
		
		LogUtil.logCrossActivtyReceiveBox(player, player.getGuildId(), cfgId);
	}
}
