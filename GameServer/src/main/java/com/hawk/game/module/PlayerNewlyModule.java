package com.hawk.game.module;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.config.CustomKeyCfg;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.msg.SuperSoldierTriggeTaskMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Newly.HPGenNewlyData;
import com.hawk.game.protocol.Newly.NewlyDataType;
import com.hawk.game.protocol.SuperSoldier.SupersoldierTaskType;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventMonsterAttack;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldPlayerService;

/**
 * 新手模块
 * 
 * @author hawk
 *
 */
public class PlayerNewlyModule extends PlayerModule {
	/**
	 * 构造
	 * 
	 * @param player
	 */
	public PlayerNewlyModule(Player player) {
		super(player);
	}

	/**
	 * 玩家登陆处理(数据同步)
	 */
	@Override
	protected boolean onPlayerLogin() {
		return true;
	}

	/**
	 * 生成新手数据
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GEN_NEWLY_DATA_C_VALUE)
	protected boolean onGenNewlyData(HawkProtocol protocol) {
		HPGenNewlyData cmd = protocol.parseProtocol(HPGenNewlyData.getDefaultInstance());
		int newlyDataType = cmd.getType();
		
		// 更新新手数据状态
		CustomDataEntity entity = player.getData().getCustomDataEntity(CustomKeyCfg.getNewlyDataStateKey());
		if (entity == null) {
 			player.getData().createCustomDataEntity(CustomKeyCfg.getNewlyDataStateKey(), newlyDataType, "");
 		} else {
 			entity.setValue(newlyDataType);
 		}
        
		// 创建新手专属野怪点
		if (newlyDataType != NewlyDataType.MONSTER_RESOURCE_VALUE) {
			player.getPush().syncNewlyPointSucc(false, 0);
		}
		
		// 生成新手数据点
		WorldPoint wp = WorldPlayerService.getInstance().createNewlyData(player.getId());
		if (wp == null) {
			MissionManager.getInstance().postMsg(player, new EventMonsterAttack(0, 1, true));
			MissionManager.getInstance().postSuperSoldierTaskMsg(player, new SuperSoldierTriggeTaskMsg(SupersoldierTaskType.ATTACK_MONSTER_TASK, 1));
			player.getPush().syncNewlyPointSucc(false, 0);
		} else {
			player.getPush().syncNewlyPointSucc(true, wp.getId());
		}
		return true;
	}
}
