package com.hawk.game.world.march.submarch;

import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.ResourceCollectEvent;
import com.hawk.game.GsConfig;
import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.PushMsgType;
import com.hawk.game.protocol.Mail.CollectMail;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.PushService;
import com.hawk.game.service.guildtask.event.ResourceCollectCountEvent;
import com.hawk.game.service.mail.CollectMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventResourceCollectCount;
import com.hawk.game.util.MailBuilderUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.log.Action;
import com.hawk.log.Source;

/**
 * 采集行军
 * @author zhenyu.shang
 * @since 2017年8月30日
 */
public interface CollectMarch extends BasedMarch{

	@Override
	default void onWorldMarchReturn(Player player) {
		WorldMarch march = getMarchEntity();
		// 下发采集物品奖励
		AwardItems items = march.getAwardItems();
		if (items == null) {
			return;
		}
		
		ResourceCollectEvent event = new ResourceCollectEvent(player.getId());
		for (ItemInfo itemInfo : items.getAwardItems()) {
			final int itemId = itemInfo.getItemId();
			final int count = (int) itemInfo.getCount();
			MissionManager.getInstance().postMsg(player, new EventResourceCollectCount(itemId, count));
			// 联盟任务
			GuildService.getInstance().postGuildTaskMsg(new ResourceCollectCountEvent(player.getGuildId(), itemId, count));
			// 刷新周期性任务活动积分
			int resWeight = WorldMarchConstProperty.getInstance().getResWeightByType(itemId);
			event.addCollectResource(itemId, count, resWeight);
		}
		long curMillTime = HawkTime.getMillisecond();
		//计算此次采集的时间
		if(this.getMarchEntity().getResStartTime() != 0){
			int costTime = (int)((curMillTime - (curMillTime -this.getMarchEntity().getStartTime()) - this.getMarchEntity().getResStartTime()) / 1000);
			event.setCollectTime(costTime);
		}
		
		WorldMarchService.getInstance().calcExtraDrop2(this);
		WorldMarchService.getInstance().calcExtraDrop3(this);
		
		ActivityManager.getInstance().postEvent(event);
		
		// 跨服消息投递-资源采集
		CrossActivityService.getInstance().postEvent(event);

		// 交税
		String hasTax = CrossActivityService.getInstance().addTax(player, items);
		
		if (items.getAwardItems().size() > 0) {
			// 发送邮件---采集成功邮件
			if (march.getMarchType() == WorldMarchType.MANOR_COLLECT_VALUE) { // 超级矿
				CollectMail.Builder builder = MailBuilderUtil.createCollectMail(march,
						MailId.COLLECT_SUPERMINE_SUCC_VALUE, true);
				CollectMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(march.getPlayerId())
						.setMailId(MailId.COLLECT_SUPERMINE_SUCC).addContents(builder).build());
			} else { // 普通资源
				CollectMail.Builder builder = MailBuilderUtil.createCollectMail(march, MailId.COLLECT_SUCC_VALUE, true);
				if (hasTax != null) {
					if (hasTax.equals(GsConfig.getInstance().getServerId())) {
						builder.setTax(CrossConstCfg.getInstance().getTaxRateOwnServer());
					} else {
						builder.setTax(CrossConstCfg.getInstance().getTaxRate());
						builder.setIsCrossTax(1);
					}
					
				}
				CollectMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(march.getPlayerId())
						.setMailId(MailId.COLLECT_SUCC).addContents(builder).build());
			}
		}
		
		// 计算石油转化作用号(注意会改变award，在发奖前调用)
		player.calcOilChangeEff(items);
		
		items.rewardTakeAffectAndPush(player, Action.WORLD_COLLECT_RES, false);

		// 额外奖励发放(366作用号采集资源每X秒给一份奖励)
		AwardItems extraAward = AwardItems.valueOf(march.getAwardExtraStr());
		extraAward.rewardTakeAffectAndPush(player, Action.WORLD_MARCH_COLLECT_RETURN, false);
		WorldMarchService.logger.info("collect res extraAward push, playerId:{}, award:{}", player.getId(), extraAward.toString());
		
		// 推送消息
		PushService.getInstance().pushMsg(player.getId(), PushMsgType.COLLECT_ARMY_RETURNED_VALUE);
		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_MARCH_COLLECT_RETURN,
				Params.valueOf("march", march), Params.valueOf("items", items.toString()));
	}
}
