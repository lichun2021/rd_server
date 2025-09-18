package com.hawk.game.module.obelisk;

import java.util.List;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.config.ObeliskCfg;
import com.hawk.game.entity.ObeliskEntity;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.obelisk.service.ObeliskService;
import com.hawk.game.module.obelisk.service.mission.ObeliskMissionType;
import com.hawk.game.module.obelisk.service.mission.data.ObeliskMissionItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Obelisk;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Status;
import com.hawk.game.util.LogUtil;
import com.hawk.log.Action;


/**方尖碑任务模块
 * @author hf
 */
public class PlayerObeliskModule extends PlayerModule {
	/**
	 * 日志
	 */
	 static final Logger logger = LoggerFactory.getLogger("Server");

	public PlayerObeliskModule(Player player) {
		super(player);
	}


	@Override
	public boolean onTick() {
		return true;
	}


	@ProtocolHandler(code = HP.code.OBELISK_MISSION_REQ_VALUE)
	private void obeliskMissionReq(HawkProtocol protocol) {
		ObeliskService.getInstance().getMissionList(player);
	}

	@ProtocolHandler(code = HP.code.OBELISK_MISSION_REWARD_VALUE)
	private void obeliskMissionRewardReq(HawkProtocol protocol) {
		Obelisk.OPBbeliskRecRewardReq req = protocol.parseProtocol(Obelisk.OPBbeliskRecRewardReq.getDefaultInstance());
		if (req.getId() <= 0){
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return;
		}
		ObeliskEntity entity = this.getPlayerData().getObeliskByCfgId(req.getId());
		if (entity == null){
			sendError(protocol.getType(), Status.ObeliskError.OBELISK_NOT_FOUND);
			return;
		}
		//非完成状态
		if (entity.getState() != Obelisk.PBObeliskPlayerState.FINISHED){
			sendError(protocol.getType(), Status.ObeliskError.OBELISK_MISSION_NO_FINISH);
			return;
		}
		ObeliskCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ObeliskCfg.class, req.getId());
		if (cfg == null){
			sendError(protocol.getType(), Status.ObeliskError.OBELISK_CFG_NO_EXIST);
			return;
		}
		//主城等级不满足
		if (player.getCityLevel() < cfg.getRewardLevelLimit()){
			sendError(protocol.getType(), Status.ObeliskError.OBELISK_FACTORY_LV_LIMIT);
			return;
		}
		//更新领奖的状态
		entity.setState(Obelisk.PBObeliskPlayerState.REWARDED);

		//领主等级条件
		AwardItems award = AwardItems.valueOf();
		List<ItemInfo> rewardList;
		if (cfg.getTaskType() == ObeliskMissionType.GUIlD_POWER_RANK.intValue() || cfg.getTaskType() == ObeliskMissionType.PLAYER_POWER_RANK.intValue()){
			int rank = entity.getContribution();
			rewardList = cfg.getRankReward(rank);
		}else {
			rewardList = cfg.getRewardList();
		}
		if (rewardList != null){
			award.addItemInfos(rewardList);
		}
		award.rewardTakeAffectAndPush(player, Action.OBELISK_MISSION_REWARD, true, Reward.RewardOrginType.OBELISK_MISSION_REWARD);
		player.responseSuccess(protocol.getType());
		ObeliskService.getInstance().getMissionList(player);

		logger.info("PlayerObeliskModule obeliskMissionRewardReq Success playerId:{}", player.getId());
		ObeliskMissionItem missionItem = ObeliskService.getInstance().getObeliskMissionItem(entity.getCfgId());
		LogUtil.logPlayerObeliskAward(player, entity.getCfgId(), ItemInfo.toString(rewardList), missionItem.getNum(), missionItem.getGuildValue(player.getGuildId()),
				entity.getContribution());
	}

}
