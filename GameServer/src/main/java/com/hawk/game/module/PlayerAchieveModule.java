package com.hawk.game.module;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hawk.activity.msg.PlayerAchieveUpdateMsg;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.game.protocol.Activity;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

import com.hawk.game.config.PlayerAchieveCfg;
import com.hawk.game.entity.PlayerAchieveEntity;
import com.hawk.game.item.PlayerAchieveItem;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.msg.MissionMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.PlayerAchieve.AchieveRewardReq;
import com.hawk.game.service.PlayerAchieveService;
import com.hawk.game.service.mssion.MissionContext;
import com.hawk.game.service.mssion.MissionEvent;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.service.mssion.type.IMission;

/**
 * 玩家成就
 * @author golden
 *
 */
public class PlayerAchieveModule extends PlayerModule {
	private Set<AchieveItem> achieveItemSet = new HashSet<>();

	public PlayerAchieveModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		PlayerAchieveEntity playerAchieveEntity = player.getData().getPlayerAchieveEntity();
		if (playerAchieveEntity.getMissionItems() == null || playerAchieveEntity.getMissionItems().isEmpty()) {
			PlayerAchieveService.getInstance().initPlayerAchieve(player);
		}
		
		// 检测新的成就项
		PlayerAchieveService.getInstance().checkNewAchieve(player);
		
		PlayerAchieveService.getInstance().syncAchieveInfo(player);
		return true;
	}

	@Override
	public boolean onTick() {
		if(achieveItemSet.isEmpty()){
			return true;
		}
		Activity.AchieveItemsInfoSync.Builder builder = Activity.AchieveItemsInfoSync.newBuilder();
		for (AchieveItem achieveItem : achieveItemSet) {
			builder.addItem(achieveItem.createAchieveItemPB());
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_ACHIEVE_CHANGE_S_VALUE, builder));
		//System.out.println("WHC ActivityModule onTick player:"+player.getId()+",items size:"+achieveItemSet.size());
		achieveItemSet = new HashSet<>();
		return true;
	}

	@MessageHandler
	private void onUpdate(PlayerAchieveUpdateMsg msg){
		//System.out.println("WHC ActivityModule onUpdate player:"+player.getId()+",items size:"+msg.getItems().size());
		achieveItemSet.addAll(msg.getItems());
	}
	/**
	 * 刷新战地任务
	 * 
	 * @param msg
	 */
	@MessageHandler
	private void onRefreshAchieve(MissionMsg msg) {
		MissionEvent event = msg.getEvent();
		
		// 事件触发任务列表
		List<MissionType> touchMissions = event.touchMissions();
		if (touchMissions == null || touchMissions.isEmpty()) {
			return;
		}
		
		PlayerAchieveEntity entity = player.getData().getPlayerAchieveEntity();
		Set<Integer> groups = AssembleDataManager.getInstance().getPlayerAchieveGroups();

		for (int groupId : groups) {
			// 任务实体
			PlayerAchieveItem entityItem = entity.getAchieveItem(groupId);
			if (entityItem == null) {
				continue;
			}
			
			PlayerAchieveCfg achieveCfg = AssembleDataManager.getInstance().getPlayerAchieve(groupId, entityItem.getState() + 1);
			if (achieveCfg == null) {
				continue;
			}
			
			MissionType achieveType = MissionType.valueOf(achieveCfg.getAchieveType());
			if (!touchMissions.contains(achieveType)) {
				continue;
			}
			
			long before = entityItem.getValue();
			
			// 刷新成就
			IMission iAchieve = MissionContext.getInstance().getMissions(achieveType);
			iAchieve.refreshMission(player.getData(), event, entityItem, achieveCfg.getMissionCfgItem());
			entity.notifyUpdate();
			long after = entityItem.getValue();
			
			// 进度没变化不推
			if (before == after) {
				continue;
			}
			
			// 设置完成时间
			if (before < achieveCfg.getTarget() && after >= achieveCfg.getTarget()) {
				entityItem.setCompleteTime(HawkTime.getSeconds());
				entity.updateBattleMissionItem(entityItem);
			}
			
			// 同步
			PlayerAchieveService.getInstance().syncAchieveUpdate(player, entityItem);
		}
	}
	
	/**
	 * 领取成就奖励
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PLAYER_ACHIEVE_GET_REWARD_VALUE)
	private void onTakeAward(HawkProtocol protocol) {
		AchieveRewardReq req = protocol.parseProtocol(AchieveRewardReq.getDefaultInstance());
		PlayerAchieveService.getInstance().onTakeAward(player, req.getGroupId());

	}
}
