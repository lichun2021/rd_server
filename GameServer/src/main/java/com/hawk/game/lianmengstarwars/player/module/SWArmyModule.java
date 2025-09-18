package com.hawk.game.lianmengstarwars.player.module;

import java.util.Objects;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.entity.QueueEntity;
import com.hawk.game.lianmengstarwars.player.ISWPlayer;
import com.hawk.game.module.PlayerArmyModule;
import com.hawk.game.module.PlayerQueueModule;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Army.HPAddSoldierReq;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Queue.QueueSpeedUpReq;
import com.hawk.game.protocol.Status;
import com.hawk.game.util.GsConst;

public class SWArmyModule extends PlayerModule {
	private ISWPlayer player;
	public SWArmyModule(ISWPlayer player) {
		super(player);
		this.player = player;
	}
	
	/**
	 * 队列加速
	 *
	 * @param hpCode
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.QUEUE_SPEED_UP_C_VALUE)
	private boolean onQueueSpeedUp(HawkProtocol protocol) {
		QueueSpeedUpReq req = protocol.parseProtocol(QueueSpeedUpReq.getDefaultInstance());
		QueueEntity queueEntity = player.getData().getQueueEntity(req.getId());
		if (Objects.isNull(queueEntity)) {
			return false;
		}
		// 指定队列可以加速
		boolean canSpeedUp = queueEntity.getQueueType() == QueueType.SOILDER_QUEUE_VALUE
				|| queueEntity.getQueueType() == QueueType.CURE_QUEUE_VALUE
				|| queueEntity.getQueueType() == QueueType.SOLDIER_ADVANCE_QUEUE_VALUE;
		if (!canSpeedUp) {
			player.sendError(protocol.getType(), Status.Error.SW_BAN_OP_VALUE, 0);
		}
		PlayerQueueModule module = player.getModule(GsConst.ModuleType.QUEUE_MODULE);
		module.onQueueSpeedUp(protocol);

		return true;
	}

	/** 训练士兵 */
	@ProtocolHandler(code = HP.code.ADD_SOLDIER_C_VALUE)
	private boolean onCreateSoldier(HawkProtocol protocol) {
		if (player.getParent().maShangOver()) {
			player.responseSuccess(protocol.getType());
			return true;
		}
		HPAddSoldierReq req = protocol.parseProtocol(HPAddSoldierReq.getDefaultInstance());
		final boolean immediate = req.getIsImmediate();
		if (!immediate) {
			return false;
		}

		PlayerArmyModule armyModule = player.getModule(GsConst.ModuleType.ARMY_MODULE);
		if (armyModule == null) {
			return false; // haha 怎么可能
		}

		boolean success = armyModule.onCreateSoldier(protocol);
		if (success) {
			// int soldierCount = req.getSoldierCount();
			// int armyId = req.getArmyId();
			// RedisProxy.getInstance().jbsIncreaseCreateSoldier(player.getId(),
			// armyId, soldierCount);
		}

		return true;
	}

	/**
	 * 治疗伤兵
	 * 
	 * @return
	 */
	@ProtocolHandler(code = HP.code.CURE_SOLDIER_C_VALUE)
	private boolean onCureSoldier(HawkProtocol protocol) {

		PlayerArmyModule armyModule = player.getModule(GsConst.ModuleType.ARMY_MODULE);
		if (armyModule == null) {
			return false; // haha 怎么可能
		}
		boolean success = armyModule.onCureSoldier(protocol);
		if (success) {

		}

		return true;
	}

}
