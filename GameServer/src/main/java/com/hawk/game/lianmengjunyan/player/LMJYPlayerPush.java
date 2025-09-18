package com.hawk.game.lianmengjunyan.player;

import java.util.concurrent.TimeUnit;

import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.hawk.game.GsApp;
import com.hawk.game.config.WarCollegeTimeControlCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.lianmengjunyan.LMJYBattleRoom;
import com.hawk.game.lianmengjunyan.msg.LMJYGameOverMsg;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Building.CityDefPB;
import com.hawk.game.protocol.Building.PushBuildingStatus;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.GuildWar.HPGuildWarCountPush;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Player.HPPlayerInfoSync;
import com.hawk.game.protocol.Player.PlayerInfo;
import com.hawk.game.protocol.World.WorldInfoPush;
import com.hawk.game.util.GameUtil;
import com.hawk.log.LogConst.PowerChangeReason;

public class LMJYPlayerPush extends ILMJYPlayerPush {
	final long PORTECTED_A = TimeUnit.MINUTES.toMillis(11);
//	final long PORTECTED_B = TimeUnit.MINUTES.toMillis(1);
	final int CITY_SHIELD_BUFF_ID = 23001;
	private long worldShield;

	public LMJYPlayerPush(Player player) {
		super(player);
	}

	@Override
	public void syncGuildWarCount() {
		ILMJYPlayer player = getPlayer();
		HPGuildWarCountPush.Builder builder = HPGuildWarCountPush.newBuilder();
		int count = player.getParent().getGuildWarMarch().size();
		builder.setCount(count);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_GUILD_WAR_COUNT_VALUE, builder));
	}

	/** 隐藏真是数据 */
	@Override
	public void pushJoinGame() {
		LMJYBattleRoom battleRoom = getPlayer().getParent();
		LMJYPlayerData playerData = (LMJYPlayerData) getData();
		playerData.unLockOriginalData();
		// 隐藏真实数据
		getPlayer().sendProtocol(HawkProtocol.valueOf(HP.code.LMJY_CLEAN_CLAIENT_DATA_VALUE));
		// PlayerData source = playerData.getSource();
		// hideArmy(source.getArmyEntities());
		// 锁定数据
		playerData.lockOriginalData();

		/** 推送军演数据 */
		// 同步部队数据
		this.syncArmyInfo(ArmyChangeCause.DEFAULT);
		// 同步队列新
		this.syncQueueEntityInfo();
		this.syncSecondaryBuildQueue();
		this.syncPlayerWorldInfo();
		this.syncCityDef(true);
		this.syncPlayerInfo();
		this.syncPlayerEffect();
		GameUtil.checkBuildingStatus(player);
		this.syncBuildingEntityInfo();
		this.synUnlockedArea();
		getPlayer().refreshPowerElectric(PowerChangeReason.LMJY_INIT);
		// 世办点开罩子
		long shiledEnd = battleRoom.getOverTime() + PORTECTED_A;
		worldShield = playerData.getSource().getCityShieldTime();
		if (worldShield < shiledEnd) {
			getPlayer().addStatusBuff(CITY_SHIELD_BUFF_ID, shiledEnd);
		}

	}

	// private void hideArmy(List<ArmyEntity> arg) {
	// ILMJYPlayer player = getPlayer();
	// // 隐藏兵
	// HPArmyInfoSync.Builder builder = HPArmyInfoSync.newBuilder();
	// for (ArmyEntity army : arg) {
	// builder.addArmyInfos(army.toProtoBuilder().setFreeCount(0).setWoundedCount(0).setCureFinishCount(0).setFinishTrainCount(0).setInTrainCount(0));
	// }
	// builder.setCause(ArmyChangeCause.DEFAULT);
	// HawkProtocol protocol = HawkProtocol.valueOf(HP.code.PLAYER_ARMY_S,
	// builder);
	// player.sendProtocol(protocol);
	// }

	@Override
	public void pushGameOver() {
		getPlayer().getData().unLockOriginalData();
		LMJYBattleRoom battleRoom = getPlayer().getParent();
		getPlayer().quitGame();
		Player source = getPlayer().getSource();
		source.sendProtocol(HawkProtocol.valueOf(HP.code.LMJY_CLEAN_CLAIENT_DATA_VALUE));

		// 清除军演数据
		// hideArmy(getData().getArmyEntities());// 消除军演部队
		/** 重新推送真实数据 */
		source.getPush().syncArmyInfo(ArmyChangeCause.DEFAULT);

		source.getPush().syncPlayerInfo();
		// 同步队列新
		source.getPush().syncQueueEntityInfo();
		source.getPush().syncSecondaryBuildQueue();
		source.getPush().syncPlayerWorldInfo();
		source.getPush().syncCityDef(true);
		GameUtil.checkBuildingStatus(player);
		source.getPush().syncBuildingEntityInfo();
		source.getPush().synUnlockedArea();
		source.getPush().syncPlayerEffect();
		source.refreshPowerElectric(PowerChangeReason.LMJY_INIT);
		// 世办点开罩子
		long shiledEnd = battleRoom.getOverTime() + PORTECTED_A;
		if (source.getData().getCityShieldTime() == shiledEnd) {
			WarCollegeTimeControlCfg wcfig = HawkConfigManager.getInstance().getKVInstance(WarCollegeTimeControlCfg.class);
			shiledEnd = Math.max(worldShield, HawkTime.getMillisecond() + TimeUnit.MINUTES.toMillis(wcfig.getWarCollegeQuitSheld()));
			StatusDataEntity entity = source.addStatusBuff(CITY_SHIELD_BUFF_ID, shiledEnd);
			// 同步buff增益效果显示
			if (entity != null) {
				source.getPush().syncPlayerStatusInfo(false, entity);
			}
		}
		PlayerInfo.Builder newBuilder = PlayerInfo.newBuilder();
		newBuilder.setLmjyState(0);
		source.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_INFO_SYNC_S, HPPlayerInfoSync.newBuilder().setPlayerInfo(newBuilder)));

		// 抛出副本结束消息. 玩家自行处理
		LMJYGameOverMsg msg = LMJYGameOverMsg.valueOf(battleRoom.getBattleCfgId(), battleRoom.isCampAwin());
		msg.setExtParm(battleRoom.getExtParm());
		msg.setWinAward(battleRoom.getExtParm().getWinAward(player.getId()));
		GsApp.getInstance().postMsg(source.getXid(), msg);

		// 增加邮件的行为日志
		try {
			DungeonRedisLog.log(player.getId(), "roomId {} win:{} guildId {} LMJYBattleCfg:{} rewardToGet: {}", battleRoom.getId(), msg.isWin(), player.getGuildId(),
					battleRoom.getBattleCfgId(), msg.getWinAward());
		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}

	public LMJYPlayer getPlayer() {
		return (LMJYPlayer) player;
	}

	@Override
	public ILMJYPlayerData getData() {
		return (ILMJYPlayerData) player.getData();
	}

	/** 同步世界信息 */
	@Override
	public void syncPlayerWorldInfo() {
		int[] posInfo = getPlayer().getPos();
		WorldInfoPush.Builder worldBuilder = WorldInfoPush.newBuilder();
		worldBuilder.setTargetX(posInfo[0]);
		worldBuilder.setTargetY(posInfo[1]);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_PLAYER_WORLD_INFO_PUSH, worldBuilder));
	}

	/** 同步城防信息 */
	public void syncCityDef(boolean cityOnFireStateChange) {
		ILMJYPlayer player = getPlayer();

		CityDefPB.Builder builder = CityDefPB.newBuilder();
		// 实际城防值
		int cityDefVal = player.getCityDefVal();
		// 城防值上限
		int maxCityDef = player.getRealMaxCityDef();
		// 城防修复时间
		long repairTime = player.getCityDefNextRepairTime();
		builder.setCityDefVal(cityDefVal);

		long now = HawkTime.getMillisecond();
		if (repairTime >= now) {
			builder.setNextRepairTime(repairTime);
		}

		long onFireEnd = player.getOnFireEndTime();
		BuildingBaseEntity buildingEntity = player.getData().getBuildingEntityByType(BuildingType.CITY_WALL);
		// 还没有城墙建筑
		if (buildingEntity == null) {
			return;
		}
		if (onFireEnd > now) {
			builder.setOnFireEndTime(onFireEnd);
			pushBuildingStatus(buildingEntity, BuildingStatus.CITYWALL_ONFIRE_STATUS);
		} else if (cityDefVal < maxCityDef) {
			pushBuildingStatus(buildingEntity, BuildingStatus.CITYWALL_DAMAGED_STATUS);
		} else {
			pushBuildingStatus(buildingEntity, BuildingStatus.COMMON);
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.CITY_DEF_PUSH, builder));

		// if (cityOnFireStateChange) {
		// WorldPlayerService.getInstance().resetCityFireStatus(player,
		// onFireEnd > now ? onFireEnd : 0);
		// }
	}

	/**
	 * 推送建筑状态的变化
	 * 
	 * @param buildingEntity
	 *            建筑实体
	 * @param status
	 *            建筑的状态
	 */
	public void pushBuildingStatus(BuildingBaseEntity buildingEntity, BuildingStatus status) {
		PushBuildingStatus.Builder push = PushBuildingStatus.newBuilder();
		push.setBuildId(buildingEntity.getId());
		push.setStatus(status);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.BUILDING_STATUS_CHANGE_PUSH, push);
		player.sendProtocol(protocol);
	}
}
