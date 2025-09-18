package com.hawk.game.module.dayazhizhan.battleroom.player;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.hawk.game.protocol.DYZZ;
import org.hawk.app.HawkApp;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.config.EffectCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager.DYZZCAMP;
import com.hawk.game.module.dayazhizhan.battleroom.msg.DYZZQuitRoomMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Building.CityDefPB;
import com.hawk.game.protocol.Building.PushBuildingStatus;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.GuildManager.HPGuildInfoSync;
import com.hawk.game.protocol.GuildWar.HPGuildWarCountPush;
import com.hawk.game.protocol.Hero.PBStaffOfficeSync;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Player.EffectPB;
import com.hawk.game.protocol.Player.HPPlayerEffectSync;
import com.hawk.game.protocol.Player.HPPlayerInfoSync;
import com.hawk.game.protocol.Player.PlayerInfo;
import com.hawk.game.protocol.World.HPWorldFavoriteSync;
import com.hawk.game.protocol.World.WorldFavoritePB;
import com.hawk.game.protocol.World.WorldInfoPush;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.log.LogConst.PowerChangeReason;

public class DYZZPlayerPush extends IDYZZPlayerPush {
	final long PORTECTED_A = TimeUnit.MINUTES.toMillis(11);
	final long PORTECTED_B = TimeUnit.MINUTES.toMillis(3);
	final int CITY_SHIELD_BUFF_ID = 23001;

	public DYZZPlayerPush(Player player) {
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
		IDYZZPlayer player = getPlayer();
		HPGuildWarCountPush.Builder builder = HPGuildWarCountPush.newBuilder();
		int count = player.getParent().getCampAGuildWarCount();
		if (player.getCamp() == DYZZCAMP.B) {
			count = player.getParent().getCampBGuildWarCount();
		}
		if (player.isAnchor()) {
			count = 0;
		}
		builder.setCount(count);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_GUILD_WAR_COUNT_VALUE, builder));
	}

	@Override
	public void syncPlayerInfo() {
//		getPlayer().getSource().getPush().syncPlayerInfo();

		HPPlayerInfoSync.Builder builder = HPPlayerInfoSync.newBuilder();
		PlayerInfo.Builder newBuilder = PlayerInfo.newBuilder();
		newBuilder.setDyzzCamp(getPlayer().getCamp().intValue());
		newBuilder.setIsRegisterPuidCtrlPlayer(GameUtil.isOBPuidCtrlPlayer(player.getOpenId()));
		newBuilder.setLmjyState(getPlayer().getDYZZState().intValue());
		newBuilder.setBattlePoint(getPlayer().getPower());
		newBuilder.setDyzzSpeedItemFree(getPlayer().isSpeedItemFree());
		newBuilder.setDyzzSpeedItemBuyCnt(getPlayer().getSpeedItemBuyCnt());
		builder.setPlayerInfo(newBuilder);

		player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_INFO_SYNC_S, builder));

	}

	/** 隐藏真是数据 */
	@Override
	public void pushJoinGame() {
		// DYZZBattleRoom battleRoom = getPlayer().getParent();
		DYZZPlayerData playerData = (DYZZPlayerData) getData();
		// playerData.unLockOriginalData();
		// 隐藏真实数据
		getPlayer().sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_CLEAN_CLAIENT_DATA_VALUE));
		// PlayerData source = playerData.getSource();
		// hideArmy(source.getArmyEntities());
		// 锁定数据
		playerData.lockOriginalData();

		/** 推送军演数据 */
		// 同步队列新
		this.syncQueueEntityInfo();
		// 同步部队数据
		this.syncBuildingEntityInfo();
		this.syncGuildInfo();
		this.syncArmyInfo(ArmyChangeCause.DEFAULT);
		this.pushHeroList();
		this.pushSuperSoldier();
		this.syncSecondaryBuildQueue();
		this.syncPlayerWorldInfo();
		this.syncCityDef(true);
		this.syncPlayerInfo();
		this.syncPlayerEffect();
//		GameUtil.checkBuildingStatus(player);
		this.synUnlockedArea();
		this.syncGuildWarCount();
		this.pushCampNoticeTime();
		this.getData().getItemsByItemId(getPlayer().getParent().getCfg().getSpeedupItem()).forEach(si -> si.setItemCount(0));
		this.syncItemInfo();
		getPlayer().refreshPowerElectric(PowerChangeReason.DYZZ_INIT);
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
		// DYZZBattleRoom battleRoom = getPlayer().getParent();
		getPlayer().quitGame();
		Player source = getPlayer().getSource();
		source.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_CLEAN_CLAIENT_DATA_VALUE));
		// 清除军演数据
		// hideArmy(getData().getArmyEntities());// 消除军演部队
		/** 重新推送真实数据 */
		// 同步队列新
		source.getPush().syncQueueEntityInfo();
		source.getPush().syncBuildingEntityInfo();
		source.getPush().syncGuildInfo();
		source.getPush().syncArmyInfo(ArmyChangeCause.DEFAULT);
		source.getPush().pushHeroList();
		source.getPush().pushSuperSoldier();
		source.getPush().clearLastPlayerInfoBuilder();
		source.getPush().syncPlayerInfo();
		source.getPush().syncSecondaryBuildQueue();
		source.getPush().syncPlayerWorldInfo();
		source.getPush().syncCityDef(true);
		source.getPush().syncPlayerEffect();
//		GameUtil.checkBuildingStatus(player);
		source.getPush().synUnlockedArea();
		source.getPush().syncClientCfg();
		source.refreshPowerElectric(PowerChangeReason.DYZZ_INIT);
		source.getData().getItemsByItemId(getPlayer().getParent().getCfg().getSpeedupItem()).forEach(si -> si.setItemCount(0));
		source.getPush().syncItemInfo();
		PBStaffOfficeSync sync = source.getStaffOffic().buildSyncPB();
		source.sendProtocol(HawkProtocol.valueOf(HP.code2.STAFFOFFICE_SYNC_VALUE, sync.toBuilder()));
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
		DYZZQuitRoomMsg msg = DYZZQuitRoomMsg.valueOf(getPlayer().getQuitReason(), getPlayer().getParent().getId());
		HawkApp.getInstance().postMsg(player.getXid(), msg);
		
		DungeonRedisLog.log(getPlayer().getId(), "{} kill{} loseTank{} collect {} kad {}", 
				getPlayer().getParent().getId(), 
				getPlayer().getKillCount(), 
				getPlayer().getHurtTankCount(),
				getPlayer().getCollectHonor(),
				getPlayer().getKda());
	}

	public DYZZPlayer getPlayer() {
		return (DYZZPlayer) player;
	}

	@Override
	public IDYZZPlayerData getData() {
		return (IDYZZPlayerData) player.getData();
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
		IDYZZPlayer player = getPlayer();

		CityDefPB.Builder builder = CityDefPB.newBuilder();
		// 实际城防值
		int cityDefVal = player.getCityDefVal();
		// 城防值上限
		int maxCityDef = player.getRealMaxCityDef();
		// 城防修复时间
		long repairTime = player.getCityDefNextRepairTime();
		builder.setCityDefVal(cityDefVal);

		long now = player.getParent().getCurTimeMil();
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

	/**
	 * 同步玩家作用号数据
	 * 
	 * @param types
	 *            作用号列表，为空推送所有作用号数据
	 */
	@Override
	public void syncPlayerEffect(EffType... types) {
		if (types == null || types.length == 0) {
			types = EffType.values();
		}

		HPPlayerEffectSync.Builder builder = HPPlayerEffectSync.newBuilder();
		for (EffType effType : types) {
			if (effType == null) {
				continue;
			}
			if (EffectCfg.effectNotVisible(effType)) {
				continue;
			}
			int effVal = player.getData().getEffVal(effType);
			EffectPB.Builder effPB = EffectPB.newBuilder();
			effPB.setEffId(effType.getNumber());
			effPB.setEffVal(effVal);
			builder.addEffList(effPB);
		}

		if (builder.getEffListCount() <= 0) {
			return;
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_EFFECT_INFO_SYNC_S_VALUE, builder));

	}
	
	/**
	 * 同步联盟信息
	 */
	public void syncGuildInfo() {
		HPGuildInfoSync.Builder builder = GuildService.getInstance().buildGuildSyncInfo(player);
		builder.setGuildId(getPlayer().getDYZZGuildId());
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.GUILD_BASIC_INFO_SYNC_S, builder);
		player.sendProtocol(protocol);
	}

	@Override
	public void pushCampNoticeTime() {
		DYZZ.PBDYZZCampNoticeTime.Builder builder = DYZZ.PBDYZZCampNoticeTime.newBuilder();
		IDYZZPlayer player = getPlayer();
		builder.setTime(player.getCampNoticeTime());
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code2.DYZZ_CAMP_NOTICES_TIME, builder);
		player.sendProtocol(protocol);
	}
}
