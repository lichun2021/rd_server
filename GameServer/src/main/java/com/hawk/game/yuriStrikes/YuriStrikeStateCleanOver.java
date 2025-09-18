package com.hawk.game.yuriStrikes;

import java.util.Objects;

import org.hawk.db.HawkDBManager;

import com.hawk.game.config.YuristrikeCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.item.AwardHelper;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.YuriStrike.YuriState;
import com.hawk.game.protocol.YuriStrike.YuriStrikeInfo;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;

@YuriStrikeState(pbState = YuriState.CLEAN_OVER)
public class YuriStrikeStateCleanOver implements IYuriStrikeState {

	@Override
	public YuriStrikeInfo.Builder toPBBuilder(Player player, YuriStrike obj) {
		int hasReward = obj.getDbEntity().getHasReward();
		YuriState pbState = hasReward == 1 ? YuriState.CLEAN_OVER : YuriState.LOCK;
		YuriStrikeInfo.Builder result = YuriStrikeInfo.newBuilder()
				.setState(pbState)
				.setHasReward(hasReward)
				.setYuriCfgId(obj.getDbEntity().getCfgId());
		return result;
	}

	@Override
	public void login(Player player, YuriStrike obj) {
		if (obj.getDbEntity().getHasReward() == 1) {// 奖励还没收
			return;
		}

		toNextState(player, obj);
	}

	public void obtainReward(Player player, YuriStrike obj) {
		if (obj.getDbEntity().getHasReward() == 0) {
			return;
		}
		AwardHelper awardHelper = new AwardHelper();
		for (ArmyInfo ai : obj.getCfg().getRewardArmyList()) {
			int armyId = ai.getArmyId();
			int count = ai.getTotalCount();
			ArmyEntity armyEntity = player.getData().getArmyEntity(armyId);
			if (armyEntity == null) {
				armyEntity = new ArmyEntity();
				armyEntity.setPlayerId(player.getId());
				armyEntity.setArmyId(armyId);
				armyEntity.addFree(count);
				if (HawkDBManager.getInstance().create(armyEntity)) {
					player.getData().addArmyEntity(armyEntity);
				}
			} else {
				armyEntity.addFree(count);
			}
			GameUtil.soldierAddRefresh(player, armyId, count);
			awardHelper.addSoldier(armyId, count);
			
			LogUtil.logArmyChange(player, armyEntity, count, ArmySection.FREE, ArmyChangeReason.AWARD);
		}
		awardHelper.syncAward(player);

		obj.getDbEntity().setHasReward(0);
		this.toNextState(player, obj);
	}

	private void toNextState(Player player, YuriStrike obj) {
		Integer nextCfg = YuristrikeCfg.higherCfgId(obj.getDbEntity().getCfgId());
		if (Objects.isNull(nextCfg)) { // 全部完成
			obj.syncInfo(player);
			return;
		}
		obj.getDbEntity().setCfgId(nextCfg.intValue());
		obj.setState(player, IYuriStrikeState.valueOf(YuriState.LOCK));
	}
}
