package com.hawk.game.module.dayazhizhan.battleroom.order;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager.DYZZCAMP;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.service.QueueService;
import com.hawk.game.util.GameUtil;
import com.hawk.log.LogConst.PowerChangeReason;

/**
 * 购买后我方医院种所有部队立刻治疗完成（包括治疗中）
 * @author lwt
 * @date 2022年4月8日
 */
public class DYZZOrder1002 extends DYZZOrder {

	public DYZZOrder1002(DYZZOrderCollection parent) {
		super(parent);
	}

	@Override
	public DYZZOrder startOrder() {
		super.startOrder();
		DYZZCAMP camp = getParent().getCamp();
		List<IDYZZPlayer> campPlayers = getParent().getParent().getCampPlayers(camp);
		for (IDYZZPlayer player : campPlayers) {
			{ // 伤兵
				Map<Integer, Integer> armyIds = new HashMap<Integer, Integer>();
				List<ArmyEntity> armyList = player.getData().getArmyEntities();
				for (ArmyEntity armyEntity : armyList) {
					// 立即治疗或道具加速完成不用等待领取，直接回到兵营
					armyEntity.addFree(armyEntity.getWoundedCount());
					armyEntity.immSetWoundedCountWithoutSync(0);
					
					armyIds.put(armyEntity.getArmyId(), 0);
				}
				player.getPush().syncArmyInfo(ArmyChangeCause.SOLDIER_CURE, armyIds.keySet().toArray(new Integer[armyIds.size()]));
			}
			{ // 普通兵治疗

				Map<String, QueueEntity> cureList = player.getData().getQueueEntitiesByType(QueueType.CURE_QUEUE_VALUE);
				for (QueueEntity queueEntity : cureList.values()) {
					queueEntity.setEndTime(getParent().getParent().getCurTimeMil());
					// 队列结束,直接走完成协议
					QueueService.getInstance().finishOneQueue(player, queueEntity, true);
				}
			}
			{ // 泰能兵治疗
				Map<String, QueueEntity> cureList = player.getData().getQueueEntitiesByType(QueueType.CURE_PLANT_QUEUE_VALUE);
				for (QueueEntity queueEntity : cureList.values()) {
					queueEntity.setEndTime(getParent().getParent().getCurTimeMil());
					// 队列结束,直接走完成协议
					QueueService.getInstance().finishOneQueue(player, queueEntity, true);
				}
			}
			GameUtil.changeBuildingStatus(player, Const.BuildingType.PLANT_HOSPITAL_VALUE, Const.BuildingStatus.COMMON);
		
			player.refreshPowerElectric(PowerChangeReason.CURE_SOLDIER);
		}
		return this;
	}
}
