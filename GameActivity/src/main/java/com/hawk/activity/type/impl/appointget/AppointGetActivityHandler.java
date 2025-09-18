package com.hawk.activity.type.impl.appointget;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;

import com.google.common.collect.ImmutableList;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.event.impl.AppointGetBox1Event;
import com.hawk.activity.event.impl.AppointGetBox2Event;
import com.hawk.activity.event.impl.AppointGetBox3Event;
import com.hawk.activity.event.impl.AppointGetBox4Event;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.appointget.cfg.AppointGetDrawCfg;
import com.hawk.activity.type.impl.appointget.cfg.AppointGetKVCfg;
import com.hawk.activity.type.impl.appointget.cfg.AppointGetRandObj;
import com.hawk.activity.type.impl.appointget.entity.AppointGetEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.PBAppointGetBox;
import com.hawk.game.protocol.Activity.PBAppointGetRandReq;
import com.hawk.game.protocol.Activity.PBAppointGetRandResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.log.Action;

public class AppointGetActivityHandler extends ActivityProtocolHandler {

	@ProtocolHandler(code = HP.code2.APPOINT_GET_RAND_C_VALUE)
	public void onChoujiangReq(HawkProtocol protocol, String playerId) {
		AppointGetActivity activity = getActivity(ActivityType.APPOINT_GET);
		if (null == activity) {
			return;
		}
		if (!activity.isOpening(playerId)) {
			return;
		}
		Optional<AppointGetEntity> opPlayerDataEntity = activity.getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		AppointGetEntity playerDataEntity = opPlayerDataEntity.get();
		PBAppointGetRandReq req = protocol.parseProtocol(PBAppointGetRandReq.getDefaultInstance());

		AppointGetKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(AppointGetKVCfg.class);
		if (playerDataEntity.getTrainCnt() + req.getTimes() > kvcfg.getLimitTimes()) {
			return;
		}

		final int cnt = req.getTimes();
		List<RewardItem.Builder> costList = new LinkedList<>();
		RewardItem.Builder oneCost = RewardHelper.toRewardItem(kvcfg.getOneCost());
		RewardItem.Builder oneGoldCost = RewardHelper.toRewardItem(kvcfg.getOneGoldCost());
		// 拥有道具数
		final int num = activity.getDataGeter().getItemNum(playerId, oneCost.getItemId());
		for (int i = 0; i < cnt && i < num; i++) {
			costList.add(oneCost);
		}
		for (int i = 0; i < cnt - num; i++) {
			costList.add(oneGoldCost);
		}

		boolean consumeResult = activity.getDataGeter().consumeItems(playerId, costList, protocol.getType(), Action.APPOINT_GET);
		if (consumeResult == false) {
			return;
		}

		List<RewardItem.Builder> rewardAll = new ArrayList<>();
		PBAppointGetRandResp.Builder resp = PBAppointGetRandResp.newBuilder();
		resp.setTimes(cnt);
		for (int i = 0; i < cnt; i++) {
			AppointGetRandObj fen = kvcfg.randObj();
			int randVal = fen.getVal();
			switch (fen.getIndex()) {
			case 1:
				ActivityManager.getInstance().postEvent(new AppointGetBox1Event(playerId, randVal), true);
				break;
			case 2:
				ActivityManager.getInstance().postEvent(new AppointGetBox2Event(playerId, randVal), true);
				break;
			case 3:
				ActivityManager.getInstance().postEvent(new AppointGetBox3Event(playerId, randVal), true);
				break;
			case 4:
				randVal = 0;
				break;

			default:
				break;
			}
			resp.addBoxs(PBAppointGetBox.newBuilder().setIndex(fen.getIndex()).setVal(randVal));
			AppointGetDrawCfg award = HawkRand.randomWeightObject(HawkConfigManager.getInstance().getConfigIterator(AppointGetDrawCfg.class).toList());
			ImmutableList<RewardItem.Builder> rewardList = RewardHelper.toRewardItemImmutableList(award.getGainItem());
			rewardList.forEach(aw -> resp.addRewardItem(aw));
			rewardAll.addAll(rewardList);
			rewardAll.addAll(RewardHelper.toRewardItemImmutableList(kvcfg.getFixReward()));
		}
		activity.getDataGeter().takeReward(playerId, rewardAll, 1, Action.APPOINT_GET, false, RewardOrginType.ACTIVITY_REWARD);

		boolean allFinish = true;
		for (AchieveItem ait : playerDataEntity.getItemList()) {
			AchieveType atype = activity.getAchieveCfg(ait.getAchieveId()).getAchieveType();
			if (atype == AchieveType.Appoint_Get331001 || atype == AchieveType.Appoint_Get331002 || atype == AchieveType.Appoint_Get331003) {
				if (ait.getState() == Activity.AchieveState.NOT_ACHIEVE_VALUE) {
					allFinish = false;
				}
			}
		}

		if (allFinish) {
			ActivityManager.getInstance().postEvent(new AppointGetBox4Event(playerId, 1), true);
		}
		playerDataEntity.setTrainCnt(playerDataEntity.getTrainCnt() + cnt);
		activity.syncActivityDataInfo(playerId);

		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.APPOINT_GET_RAND_S_VALUE, resp));
	}

}
