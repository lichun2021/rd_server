package com.hawk.game.module.lianmengfgyl.battleroom.player;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.hawk.app.HawkApp;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLRoomManager.FGYL_CAMP;
import com.hawk.game.module.lianmengfgyl.battleroom.msg.FGYLQuitRoomMsg;
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
import com.hawk.game.protocol.World.HPWorldFavoriteSync;
import com.hawk.game.protocol.World.WorldFavoritePB;
import com.hawk.game.protocol.World.WorldInfoPush;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.log.LogConst.PowerChangeReason;

public class FGYLPlayerPush extends IFGYLPlayerPush {
	final long PORTECTED_A = TimeUnit.MINUTES.toMillis(11);
	final long PORTECTED_B = TimeUnit.MINUTES.toMillis(3);
	final int CITY_SHIELD_BUFF_ID = 23001;

	public FGYLPlayerPush(Player player) {
		super(player);
	}

	/**
	 * 同步世界收藏夹
	 * 
	 * @return
	 */
	@Override
	public void syncWorldFavorite() {
		HPWorldFavoriteSync.Builder favoriteBuilder = HPWorldFavoriteSync.newBuilder();
		List<WorldFavoritePB.Builder> favoriteList = getPlayer().getFavoriteList();
		for (WorldFavoritePB.Builder favorite : favoriteList) {
			favoriteBuilder.addFavorites(favorite);
		}
		favoriteBuilder.setSynType(0);
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_FAVORITE_SYNC_S, favoriteBuilder);
		player.sendProtocol(protocol);
	}

	@Override
	public void syncGuildWarCount() {
		IFGYLPlayer player = getPlayer();
		HPGuildWarCountPush.Builder builder = HPGuildWarCountPush.newBuilder();
		int count = player.getParent().getCampAGuildWarCount();
		if(player.isAnchor()){
			count = 0;
		}
		builder.setCount(count);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_GUILD_WAR_COUNT_VALUE, builder));
	}

	@Override
	public void syncPlayerInfo() {
		getPlayer().getSource().getPush().syncPlayerInfo();
		syncFGYLPlayerInfo();

	}
	
	@Override
	public void syncFGYLPlayerInfo() {
		HPPlayerInfoSync.Builder builder = HPPlayerInfoSync.newBuilder();
		PlayerInfo.Builder newBuilder = BuilderUtil.genPlayerBuilder(getData());
		newBuilder.setFgylCamp(getPlayer().getCamp().intValue());
		newBuilder.setFgylSkillOrder(getPlayer().getSkillOrder());
		newBuilder.setFgylMonsterAttack(getPlayer().getMonstKill());
		builder.setPlayerInfo(newBuilder);
		if(getPlayer().getFgylState()!=null){
			newBuilder.setLmjyState(getPlayer().getFgylState().intValue());
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_INFO_SYNC_S, builder));

	}

	/** 隐藏真是数据 */
	@Override
	public void pushJoinGame() {
		// TBLYBattleRoom battleRoom = getPlayer().getParent();
		FGYLPlayerData playerData = (FGYLPlayerData) getData();
//		playerData.unLockOriginalData();
		// 隐藏真实数据
		getPlayer().sendProtocol(HawkProtocol.valueOf(HP.code.TBLY_CLEAN_CLAIENT_DATA_VALUE));
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
		this.syncGuildWarCount();
		getPlayer().refreshPowerElectric(PowerChangeReason.TBLY_INIT);
		// // 世办点开罩子
		// WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
		// long shiledEnd = battleRoom.getOverTime() + PORTECTED_A;
		// if (Objects.nonNull(worldPoint) && worldPoint.getShowProtectedEndTime() < shiledEnd) {
		// getPlayer().addStatusBuff(CITY_SHIELD_BUFF_ID, shiledEnd);
		// }

	}

	@Override
	public void pushGameOver() {
		getPlayer().getData().unLockOriginalData();
		// TBLYBattleRoom battleRoom = getPlayer().getParent();
		Player source = getPlayer().getSource();
		source.refreshPowerElectric(PowerChangeReason.TBLY_INIT);
		getPlayer().quitGame();
		source.sendProtocol(HawkProtocol.valueOf(HP.code.TBLY_CLEAN_CLAIENT_DATA_VALUE));
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
		source.getPush().syncPlayerEffect();
		GameUtil.checkBuildingStatus(player);
		source.getPush().syncBuildingEntityInfo();
		source.getPush().synUnlockedArea();
		// // 世办点开罩子
		// WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
		// long shiledEnd = battleRoom.getOverTime() + PORTECTED_A;
		// if (Objects.nonNull(worldPoint) && worldPoint.getShowProtectedEndTime() == shiledEnd) {
		// shiledEnd = HawkTime.getMillisecond() + PORTECTED_B;
		// StatusDataEntity entity = source.addStatusBuff(CITY_SHIELD_BUFF_ID, shiledEnd);
		// // 同步buff增益效果显示
		// if (entity != null) {
		// source.getPush().syncPlayerStatusInfo(false, entity);
		// }
		// }
		FGYLQuitRoomMsg msg = FGYLQuitRoomMsg.valueOf(getPlayer().getQuitReason());
		HawkApp.getInstance().postMsg(player.getXid(), msg);
		
		DungeonRedisLog.log(getPlayer().getId(), "{} kill {} loseTank {} collect {} honor {}", 
				getPlayer().getParent().getId(), 
				getPlayer().getKillPower(), 
				getPlayer().getHurtTankPower(),
				getPlayer().getCollectGuildHonor(),
				getPlayer().getHonor());
	}

	public FGYLPlayer getPlayer() {
		return (FGYLPlayer) player;
	}

	@Override
	public IFGYLPlayerData getData() {
		return (IFGYLPlayerData) player.getData();
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
		IFGYLPlayer player = getPlayer();

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
		// WorldPlayerService.getInstance().resetCityFireStatus(player, onFireEnd > now ? onFireEnd : 0);
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
