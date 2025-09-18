package com.hawk.activity.event.callback;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.event.MsgCallBack;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;

public class SendCrossDayNoTakeRewardCallBack implements MsgCallBack{
	
	private String playerId;
	
	private List<AchieveItem> achieveList;
	
	private Class<? extends HawkConfigBase> configClass;
	
	private ActivityBase activity;
	
	public SendCrossDayNoTakeRewardCallBack(String playerId, List<AchieveItem> achieveList, Class<? extends HawkConfigBase> configClass, ActivityBase activity) {
		this.playerId = playerId;
		this.achieveList = achieveList;
		this.configClass = configClass;
		this.activity = activity;
	}



	@Override
	public void execute() {
		if(achieveList == null || achieveList.isEmpty()){
			return;
		}
		List<RewardItem.Builder> items = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		for(AchieveItem achieveItem : achieveList){
			if(achieveItem.getState() != AchieveState.NOT_REWARD_VALUE){
				continue;
			}
			AchieveConfig cfg = (AchieveConfig) HawkConfigManager.getInstance().getConfigByKey(configClass, achieveItem.getAchieveId());
			if(cfg == null){
				activity.logger.info("{} not found! AchieveId: {}",configClass.getName(), achieveItem.getAchieveId());
				continue;
			}
			items.addAll(cfg.getRewardList());
			sb.append(achieveItem).append(",");
		}
		
		if (items.isEmpty()) {
			return;
		}
		Object[] subTitle = new Object[] {};
		Object[] content = new Object[] { activity.getActivityCfg().getActivityName() };
		activity.sendMailToPlayer(playerId, MailId.ACTIVITY_END_SEND_NOT_TAKE_REWARD, subTitle, content, items);
		activity.logger.info("AccumulateConsumeActivity send no take reward! playerId: {}, achieveItem: {}", playerId, sb.toString());
	}
	

}
