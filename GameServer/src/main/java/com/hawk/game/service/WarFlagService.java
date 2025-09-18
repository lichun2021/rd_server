package com.hawk.game.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.AddBannerEvent;
import com.hawk.activity.type.ActivityState;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.config.WarFlagConstProperty;
import com.hawk.game.config.WarFlagLevelCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.entity.WarFlagEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.guild.manor.AbstractBuildable;
import com.hawk.game.guild.manor.GuildManorObj;
import com.hawk.game.invoker.WorldAwardPushInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.item.WarFlagSignUpItem;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Common.KeyValuePairInt;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.GuildManor.ManorPlayerInfo;
import com.hawk.game.protocol.GuildManor.ManorPlayerInfoList;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.WarFlag.CenterFlagInfoResp;
import com.hawk.game.protocol.WarFlag.CenterFlagSingUpInfo;
import com.hawk.game.protocol.WarFlag.FlagPatternResp;
import com.hawk.game.protocol.WarFlag.FlagRedPointPush;
import com.hawk.game.protocol.WarFlag.FlagViewState;
import com.hawk.game.protocol.WarFlag.FlageState;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.flag.FlagCollection;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.warFlag.IFlag;
import com.hawk.game.util.AlgorithmPoint;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.MapBlock;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.proxy.WorldPointRedisProxy;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.WarFlagOwnChangeType;

/**
 * 战地之王
 * @author golden
 *
 */
public class WarFlagService extends HawkAppObj {

	/**
	 * 日志
	 */
	public static Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 单例对象
	 */
	private static WarFlagService instance = null;
	
	/**
	 * 上一次关闭检测时间
	 */
	private static long lastCloseTick;
	
	/**
	 * 旗帜产出资源
	 */
	private Map<String, List<ItemInfo>> flagResource = null;
	
	
	/**
	 * 构造
	 */
	public WarFlagService(HawkXID xid) {
		super(xid);
		instance = this;
	}
	
	/**
	 * 获取实体对象
	 */
	public static WarFlagService getInstance() {
		return instance;
	}
	

	/**
	 * 初始化
	 */
	public boolean init() {
		
		List<WarFlagEntity> flags = HawkDBManager.getInstance().query("from WarFlagEntity where invalid = 0");
		FlagCollection.getInstance().init(flags);
		
		// 战旗tick
		long tickPeriod = WarFlagConstProperty.getInstance().getTickPeriod();
		WorldThreadScheduler.getInstance().addWorldTickable(new HawkPeriodTickable(tickPeriod) {
			@Override
			public void onPeriodTick() {
				try {
					tick();
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		});
		
		flagResource = new ConcurrentHashMap<>();
		
		return true;
	}
	
	public void tick() {
		
		Map<String, IFlag> flagMap = FlagCollection.getInstance().getFlags();
		
		for (IFlag flag : flagMap.values()) {
			flag.tick();
		}
		
		// 活动关闭tick
		activityCloseTick();
	}
	
	/**
	 * 活动关闭tick
	 */
	public void activityCloseTick() {
		
		long closeTickPeriod = WarFlagConstProperty.getInstance().getCloseTickPeriod();
		if (HawkTime.getMillisecond() - lastCloseTick < closeTickPeriod) {
			return;
		}
		lastCloseTick = HawkTime.getMillisecond();
		
		if (isActivityOpen()) {
			return;
		}

		// 一次最大移除数量
		int maxCount = WarFlagConstProperty.getInstance().getCloseOnceRemove();
		
		int count = 0;
		
		// 需要删除旗子列表
		List<IFlag> removeList = new ArrayList<>();
		
		// 需要删除点列表
		List<WorldPoint> removePoint = new ArrayList<>();
		
		Map<String, IFlag> flags = FlagCollection.getInstance().getFlags();
		if (flags.isEmpty()) {
			List<WorldPoint> warFlagPoints = WorldPointService.getInstance().getWorldPointsByType(WorldPointType.WAR_FLAG_POINT);
			if (!warFlagPoints.isEmpty()) {
				for (WorldPoint point : warFlagPoints) {
					WorldPointService.getInstance().removeWorldPoint(point.getId(), false);
				}
				WorldPointRedisProxy.getInstance().batchDelete(removePoint);
			}
		}
		
		for (IFlag flag : flags.values()) {
			
			if (count >= maxCount) {
				break;
			}
			
			if (flag == null) {
				continue;
			}
			
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(flag.getPointId());
			if (worldPoint != null && worldPoint.getPointType() == WorldPointType.WAR_FLAG_POINT_VALUE) {
				removePoint.add(worldPoint);
			}
			
			removeList.add(flag);
			count++;
		}
		
		// 解散行军
		for (IFlag flag : removeList) {
			dissolveFlagPointMarch(flag);
		}
		
		// 删除点
		for (WorldPoint point : removePoint) {
			WorldPointService.getInstance().removeWorldPoint(point.getId(), false);
		}
		WorldPointRedisProxy.getInstance().batchDelete(removePoint);
		
		// 删除实体
		for (IFlag flag : removeList) {
			logger.info("flagCloseRemove, flagId:{}, ownerId:{}, currId:{}, pointId:{}, state:{}", flag.getFlagId(), flag.getOwnerId(), flag.getCurrentId(), flag.getPointId(), flag.getState());
			FlagCollection.getInstance().removeFlag(flag);
			flag.delete();
		}
	}
	
	/**
	 * 活动是否开放
	 */
	public boolean isActivityOpen() {
		Optional<ActivityBase> activity = ActivityManager.getInstance().getGameActivityByType(ActivityType.GUILD_BANNER_VALUE);
		return activity.isPresent() && activity.get().getActivityEntity().getActivityState() == ActivityState.OPEN;
	}
	
	/**
	 * 联盟当前最大旗帜数量
	 */
	public int guildWarFlagMaxCount(String guildId) {
		long power = GuildService.getInstance().getGuildBattlePoint(guildId);
		int memberCount = GuildService.getInstance().getGuildMemberNum(guildId);
		
		int maxCount = 0;
		List<List<Integer>> limits = WarFlagConstProperty.getInstance().getFlagCountLimit();
		for (List<Integer> limit : limits) {
			// 联盟战力
			if (power < limit.get(0)) {
				continue;
			}
			// 联盟人数
			if (memberCount < limit.get(1)) {
				continue;
			}
			// 旗帜数量
			if (maxCount > limit.get(2)) {
				continue;
			}
			maxCount = limit.get(2);
		}
		
		return maxCount;
	}
	
	
	/**
	 * 检测联盟旗帜数量(世界线程操作)
	 */
	public void checkWarFlagCount(String guildId) {
		if (!isActivityOpen()) {
			return;
		}
		
		int ownFlagCount = FlagCollection.getInstance().getOwnerFlagCount(guildId);
		int addCount = guildWarFlagMaxCount(guildId) - ownFlagCount;
		if (addCount <= 0) {
			return;
		}
		
		List<WarFlagEntity> addFlags = new ArrayList<>();
		for (int i = 0; i < addCount; i++) {
			WarFlagEntity flagEntity = new WarFlagEntity();
			flagEntity.setOwnerId(guildId);
			flagEntity.setCurrentId(guildId);
			flagEntity.setState(FlageState.FLAG_UNLOCKED_VALUE);
			flagEntity.setOwnIndex(ownFlagCount + i + 1);
			addFlags.add(flagEntity);
		}
		
		// 添加联盟旗帜
		if (HawkDBEntity.batchCreate(addFlags)) {
			for (WarFlagEntity flag : addFlags) {
				FlagCollection.getInstance().addFlag(IFlag.create(flag));
				logger.info("addGuildFlag, flagId:{}, guildId:{}", flag.getFlagId(), guildId);
			}
		}
	}

	/**
	 * 增加母旗
	 */
	public void checkCenterFlagCreate(String guildId) {
		if (!isActivityOpen()) {
			return;
		}
		
		// 子旗数量
		List<IFlag> ownCompFlags = FlagCollection.getInstance().getOwnCompFlags(guildId, true, true, true);
		int flagCount = ownCompFlags.size();
		
		// 母旗数量上限
		int centerFlagCountLimit = WarFlagConstProperty.getInstance().getCenterFlagCountLimit(flagCount);
		int centerFlagCount = FlagCollection.getInstance().getCenterFlagCount(guildId);
		
		List<WarFlagEntity> addFlags = new ArrayList<>();
		if (centerFlagCountLimit > centerFlagCount) {
			for (int i = 0; i < centerFlagCountLimit - centerFlagCount; i++) {
				WarFlagEntity flagEntity = new WarFlagEntity();
				flagEntity.setOwnerId(guildId);
				flagEntity.setCurrentId(guildId);
				flagEntity.setState(FlageState.FLAG_UNLOCKED_VALUE);
				flagEntity.setOwnIndex(centerFlagCount + i + 1);
				flagEntity.setCenterFlag(1);
				addFlags.add(flagEntity);
			}
		}
		
		// 添加联盟旗帜
		if (HawkDBEntity.batchCreate(addFlags)) {
			for (WarFlagEntity flag : addFlags) {
				FlagCollection.getInstance().addFlag(IFlag.create(flag));
				logger.info("addGuildFlag, flagId:{}, guildId:{}, isCenter:true", flag.getFlagId(), guildId);
			}
		}
	}
	
	/**
	 * 是否可以收回联盟旗帜
	 */
	public boolean canWarFlagTakeBack(int protocol, Player player, String flagId) {
		
		if (!player.hasGuild()) {
			player.sendError(protocol, Status.WarFlagError.TAKE_BACK_FLAG_HAVE_NO_GUILD_VALUE, 0);
			return false;
		}
		
		// 权限
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.WAR_FLAG_AUTH);
		if (!guildAuthority) {
			player.sendError(protocol, Status.WarFlagError.TAKE_BACK_FLAG_AUTH_NOT_ENOUGTH_VALUE, 0);
			return false;
		}
		
		// 旗帜不存在
		IFlag flag = FlagCollection.getInstance().getFlag(flagId);
		if (flag == null) {
			return false;
		}
		
		// 不是自己联盟的旗帜，不能拆除
		if (!flag.getOwnerId().equals(player.getGuildId())) {
			player.sendError(protocol, Status.WarFlagError.TAKE_BACK_FLAG_NOT_OWN_VALUE, 0);
			return false;
		}
		
		// 不是自己联盟占领的旗帜，不能拆除
		if (!flag.getCurrentId().equals(player.getGuildId())) {
			player.sendError(protocol, Status.WarFlagError.TAKE_BACK_FLAG_NOT_CURRENT_VALUE, 0);
			return false;
		}
		
		if (!flag.canTakeBack()) {
			player.sendError(protocol, Status.WarFlagError.FLAG_ALREADY_TAKE_BACK_VALUE, 0);
			return false;
		}
		
		return true;
	}
	
	/**
	 * 收回旗帜
	 */
	public void takeBackWarFlag(Player player, String flagId) {
		IFlag flag = FlagCollection.getInstance().getFlag(flagId);
		
		// 解散行军
		dissolveFlagPointMarch(flag);

		rmFlagTargetMarch(flag);
		
		// 删除点
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(flag.getPointId());
		if (point != null && point.getPointType() == WorldPointType.WAR_FLAG_POINT_VALUE) {
			LogUtil.logWarFlag(flag, flag.getCurrentId(), WarFlagOwnChangeType.DESTROY, point.getZoneId());
			WorldPointService.getInstance().removeWorldPoint(flag.getPointId());
		}
		
		// 设置已解锁
		flag.setState(FlageState.FLAG_UNLOCKED_VALUE);
		flag.setCompleteTime(0);
		flag.setLife(0);
		
		// 清空报名信息
		flag.clearSignUpInfo();
		
		notifyArroundPointFlagUpdate(flag);
		
		// 拆旗子，隔壁方回行军
		List<IFlag> arroundCompFlags = getArroundCompFlags(flag.getPointId(), flag.isCenter());
		for (IFlag arrFlag : arroundCompFlags) {
			if (!inOtherManorGuildIds(flag.getCurrentId(), flag.getPointId(), flag.isCenter()).isEmpty()) {
				continue;
			}
			dissolveFlagPointMarch(arrFlag);
		}
		
		ActivityManager.getInstance().postEvent(new AddBannerEvent(flag.getCurrentId(), FlagCollection.getInstance().getGuildCompFlagCount(flag.getCurrentId())), true);
		
		GuildManorService.getInstance().notifyManorBuffChange(point.getId());
		
		int[] pos = GameUtil.splitXAndY(flag.getPointId());
		if (player != null) {
			WarFlagService.logger.info("takeBackFlag, playerId:{}, playerGuildId:{}, flagId:{}, owner:{}, curr:{}, state:{}, life:{}, posX:{}, posY:{}",
					player.getId(), player.getGuildId(), flag.getFlagId(), flag.getOwnerId(), flag.getCurrentId(), flag.getState(), flag.getLife(), pos[0], pos[1]);
		}
	}
	
	/**
	 * 是否可以放置战地旗帜
	 */
	public boolean canWarFlagPlace(Player player, int posX, int posY, String flagId, int protocol) {
		
		// 活动未开启
		if (!isActivityOpen()) {
			return false;
		}
		
		IFlag flag = FlagCollection.getInstance().getFlag(flagId);
		if (flag == null) {
			return false;
		}
		
		// 目标点
		int pointId = GameUtil.combineXAndY(posX, posY);
		
		// 超出地图范围
		if (WorldPointService.getInstance().isOutOfMapRange(posX, posY)) {
			player.sendError(protocol, Status.WarFlagError.PLACE_WAR_FLAG_POS_ERROR, 0);
			return false;
		}
		
		// 没有联盟不能放置旗帜
		if (!player.hasGuild()) {
			player.sendError(protocol, Status.WarFlagError.PLACE_WAR_FlAG_HAVE_NO_GUILD_VALUE, 0);
			return false;
		}
		
		// 权限
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.WAR_FLAG_AUTH);
		if (!guildAuthority) {
			player.sendError(protocol, Status.WarFlagError.PLACE_WAR_FLAG_AUTH_NOT_ENOUGTH_VALUE, 0);
			return false;
		}
		
		// 目标点为阻挡点
		if (MapBlock.getInstance().isStopPoint(pointId)) {
			player.sendError(protocol, Status.WarFlagError.PLACE_WAR_FLAG_POS_ERROR, 0);
			return false;
		}
		
		// 目标点已被占用
		if (WorldPointService.getInstance().getWorldPoint(pointId) != null) {
			player.sendError(protocol, Status.WarFlagError.PLACE_WAR_FLAG_POINT_HAS_REFRESH_VALUE, 0);
			return false;
		}
		
		// 目标点已被占用
		final Point point = WorldPointService.getInstance().getAreaPoint(posX, posY, true);
		if (point == null) {
			player.sendError(protocol, Status.WarFlagError.PLACE_WAR_FLAG_POINT_HAS_REFRESH_VALUE, 0);
			return false;
		}

		// 目标点奇偶判断
		if (!point.canRMSeat()) {
			player.sendError(protocol, Status.WarFlagError.PLACE_WAR_FLAG_POS_ERROR, 0);
			return false;
		}
		
		// 是否在其它旗帜范围或领地内
		if (isInFlagRange(pointId)) {
			player.sendError(protocol, Status.WarFlagError.PLACE_WAR_FLAG_IN_OTHER_RANGE_VALUE, 0);
			return false;
		}
		
		// 半径范围内不能有其它旗帜(不能坐落在其它旗帜范围内)
		int flagRadius = WarFlagConstProperty.getInstance().getFlagRadius(flag.isCenter());
		List<Point> innerPoints = WorldPointService.getInstance().getRhoAroundPointsAll(pointId, flagRadius);
		for (Point iPoint : innerPoints) {
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(iPoint.getId());
			if (worldPoint == null) {
				continue;
			}
			if (worldPoint.getPointType() != WorldPointType.WAR_FLAG_POINT_VALUE) {
				continue;
			}
			IFlag thisFlag = FlagCollection.getInstance().getFlag(worldPoint.getGuildBuildId());
			if (thisFlag != null) {
				player.sendError(protocol, Status.WarFlagError.PLACE_WAR_FLAG_IN_OTHER_RANGE_VALUE, 0);
				return false;
			}
		}
		
		// 是否和自己旗帜范围或领地重叠
		if (!isRangeInOwnManor(player, point, flag.isCenter())) {
			player.sendError(protocol, Status.WarFlagError.PLACE_WAR_FLAG_NOT_IN_RANGE_VALUE, 0);
			return false;
		}
		
		if (!flag.canPlace()) {
			player.sendError(protocol, Status.WarFlagError.FLAG_ALREADY_PLACED_VALUE, 0);
			return false;
		}
		
		return true;
	}

	/**
	 * 是否在旗帜范围或领地内
	 * @param pointId
	 * @return
	 */
	public boolean isInFlagRange(int pointId) {
		boolean inOtherRange = false;
		
		// 半径范围内不能有其它旗帜(不能坐落在其它旗帜范围内)
		int flagRadius = WarFlagConstProperty.getInstance().getFlagRadius(true);
		List<Point> innerPoints = WorldPointService.getInstance().getRhoAroundPointsAll(pointId, flagRadius + 1);
		for (Point iPoint : innerPoints) {
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(iPoint.getId());
			if (worldPoint == null) {
				continue;
			}
			if (worldPoint.getPointType() != WorldPointType.WAR_FLAG_POINT_VALUE) {
				continue;
			}
			IFlag flag = FlagCollection.getInstance().getFlag(worldPoint.getGuildBuildId());
			if (flag == null) {
				continue;
			}
			if (flag.isCenter()) {
				inOtherRange = true;
				break;
			} else {
				int[] pos = GameUtil.splitXAndY(pointId);
				int checkRadius = WarFlagConstProperty.getInstance().getFlagRadius(false);
				if (Math.abs(iPoint.getX() - pos[0]) + Math.abs(iPoint.getY() - pos[1]) <= checkRadius) {
					inOtherRange = true;
					break;
				}
			}
		}
		
		// 不能坐落在其它联盟领地范围内
		if (!inOtherRange) {
			int manorRadius = GuildConstProperty.getInstance().getManorRadius();
			
			innerPoints = WorldPointService.getInstance().getRhoAroundPointsAll(pointId, manorRadius + 1);
			for (Point iPoint : innerPoints) {
				WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(iPoint.getId());
				if (worldPoint == null) {
					continue;
				}
				if (worldPoint.getPointType() != WorldPointType.GUILD_TERRITORY_VALUE) {
					continue;
				}
				AbstractBuildable buildable = GuildManorService.getInstance().getBuildable(worldPoint);
				if (buildable == null) {
					continue;
				}
				if (buildable.getBuildType() != TerritoryType.GUILD_BASTION) {
					continue;
				}
				GuildManorObj obj = (GuildManorObj) buildable;
				if (!obj.isComplete()) {
					continue;
				}
				inOtherRange = true;
				break;
			}
		}
		
		return inOtherRange;
	}

	/**
	 * 是否和自己旗帜范围或领地重叠
	 */
	public boolean isRangeInOwnManor(Player player, Point point, boolean isCenter) {
		// 这段是想判断 想要放置的旗帜范围，必须和自己之前已经放置的旗帜范围有重合的地方。但是只重合一个点不算(所以去除顶点)
		// 判断的方法是 找两倍半径范围内是否有已放置的点，有的话代表两个旗帜范围有重合
		boolean hasOwnerFlag = false;
		
		int flagRadius = WarFlagConstProperty.getInstance().getFlagRadius(true);
		List<Point> outerPoints = WorldPointService.getInstance().getRhoAroundPointsAll(point.getId(), flagRadius * 2 + 1);
		for (Point oPoint : outerPoints) {
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(oPoint.getId());
			if (worldPoint == null) {
				continue;
			}
			if (worldPoint.getPointType() != WorldPointType.WAR_FLAG_POINT_VALUE) {
				continue;
			}
			IFlag flag = FlagCollection.getInstance().getFlag(worldPoint.getGuildBuildId());
			if (!flag.getCurrentId().equals(player.getGuildId())) {
				continue;
			}
			
			int checkRadius = WarFlagConstProperty.getInstance().getFlagRadius(isCenter) + WarFlagConstProperty.getInstance().getFlagRadius(flag.isCenter());
			
			if (Math.abs(oPoint.getX() - point.getX()) + Math.abs(oPoint.getY() - point.getY()) < checkRadius) {
				return false;
			}
			
			if (!flag.hasManor()) {
				continue;
			}
			
			// 四个顶点上的不要
			if (WorldUtil.distance(point.getX(), point.getY(), oPoint.getX(), oPoint.getY()) == checkRadius) {
				continue;
			}
			
			
			// 战旗二期修改：己方两个旗子的范围要刚巧贴上，不能重合
			if (Math.abs(oPoint.getX() - point.getX()) + Math.abs(oPoint.getY() - point.getY()) != checkRadius) {
				continue;
			}
			
			
			hasOwnerFlag = true;
		}
		
		
		// 如果没有和其它旗子范围重合，再判断是否和联盟堡垒范围重合
		flagRadius = WarFlagConstProperty.getInstance().getFlagRadius(isCenter);
		if (!hasOwnerFlag) {
			int manorRadius = GuildConstProperty.getInstance().getManorRadius();
			List<Point> mPoints = WorldPointService.getInstance().getRhoAroundPointsAll(point.getId(), manorRadius + flagRadius + 1);
			for (Point oPoint : mPoints) {
				// 四个顶点上的不要
				if (WorldUtil.distance(point.getX(), point.getY(), oPoint.getX(), oPoint.getY()) == manorRadius + flagRadius) {
					continue;
				}
				
				WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(oPoint.getId());
				if (worldPoint == null) {
					continue;
				}
				if (worldPoint.getPointType() != WorldPointType.GUILD_TERRITORY_VALUE) {
					continue;
				}
				AbstractBuildable buildable = GuildManorService.getInstance().getBuildable(worldPoint);
				if (buildable == null) {
					continue;
				}
				if (!buildable.getGuildId().equals(player.getGuildId())) {
					continue;
				}
				if (buildable.getBuildType() != TerritoryType.GUILD_BASTION) {
					continue;
				}
				GuildManorObj obj = (GuildManorObj) buildable;
				if (!obj.isComplete()) {
					continue;
				}
				
				if (Math.abs(oPoint.getX() - point.getX()) + Math.abs(oPoint.getY() - point.getY()) < manorRadius + flagRadius) {
					return false;
				}
				
				hasOwnerFlag = true;
				break;
			}
		}
		
		return hasOwnerFlag;
	}
	
	/**
	 * 旗帜范围是否和其它旗帜范围有重叠(不包含联盟堡垒)
	 */
	public List<String> inOtherManorGuildIds(String guildId, int pointId, boolean isCenter) {
		
		List<String> guildIds = new ArrayList<>();
		
		int pos[] = GameUtil.splitXAndY(pointId);
		
		// 这段是想判断 想要放置的旗帜范围，必须和自己之前已经放置的旗帜范围有重合的地方。但是只重合一个点不算(所以去除顶点)
		// 判断的方法是 找两倍半径范围内是否有已放置的点，有的话代表两个旗帜范围有重合
		
		int flagRadius = WarFlagConstProperty.getInstance().getFlagRadius(true);
		List<Point> outerPoints = WorldPointService.getInstance().getRhoAroundPointsAll(pointId, flagRadius * 2 + 1);
		for (Point oPoint : outerPoints) {
			// 四个顶点上的不要
			if (WorldUtil.distance(pos[0], pos[1], oPoint.getX(), oPoint.getY()) == flagRadius * 2) {
				continue;
			}
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(oPoint.getId());
			if (worldPoint == null) {
				continue;
			}
			if (worldPoint.getPointType() != WorldPointType.WAR_FLAG_POINT_VALUE) {
				continue;
			}
			IFlag flag = FlagCollection.getInstance().getFlag(worldPoint.getGuildBuildId());
			if (flag == null) {
				WorldPointService.getInstance().removeWorldPoint(worldPoint.getId());
				continue;
			}
			if (flag.getCurrentId().equals(guildId)) {
				continue;
			}
			if (!flag.hasManor()) {
				continue;
			}
			int checkRadius = WarFlagConstProperty.getInstance().getFlagRadius(isCenter) + WarFlagConstProperty.getInstance().getFlagRadius(flag.isCenter());
			// 战旗二期修改
			if (Math.abs(oPoint.getX() - pos[0]) + Math.abs(oPoint.getY() - pos[1]) > checkRadius) {
				continue;
			}
			
			guildIds.add(flag.getCurrentId());
		}
		
		return guildIds;
	}
	
	/**
	 * 放置战地旗帜
	 */
	public void placeWarFlag(Player player, int posX, int posY, String flagId) {
		
		// 创建点
		AreaObject areaObj = WorldPointService.getInstance().getArea(posX, posY);
		int zoneId = WorldUtil.getPointResourceZone(posX, posY);
		WorldPoint worldPoint = new WorldPoint(posX, posY, areaObj.getId(), zoneId, WorldPointType.WAR_FLAG_POINT_VALUE);
		worldPoint.setGuildId(player.getGuildId());
		worldPoint.setOwnerId(player.getGuildId());
		worldPoint.setGuildBuildId(flagId);
		WorldPointService.getInstance().createWorldPoint(worldPoint);
		
		// 设置旗帜状态
		IFlag flag = FlagCollection.getInstance().getFlag(flagId);
		flag.setState(FlageState.FLAG_PLACED_VALUE);
		flag.setPointId(worldPoint.getId());
		flag.setPlaceTime(HawkTime.getMillisecond());
		flag.clearSignUpInfo();
		flag.setCenterAvtive(false);
		notifyArroundPointFlagUpdate(flag);
		
		WarFlagService.logger.info("placeFlag, playerId:{}, playerGuildId:{}, flagId:{}, owner:{}, curr:{}, state:{}, life:{}, posX:{}, posY:{}",
				player.getId(), player.getGuildId(), flag.getFlagId(), flag.getOwnerId(), flag.getCurrentId(), flag.getState(), flag.getLife(), worldPoint.getX(), worldPoint.getY());
	}
	
	/**
	 * 点在哪些旗帜范围内
	 */
	public List<IFlag> getArroundCompFlags(int pointId, boolean isCenter) {
		int pos[] = GameUtil.splitXAndY(pointId);
		List<IFlag> flags = new ArrayList<>();
		
		int flagRadius = WarFlagConstProperty.getInstance().getFlagRadius(true);
		List<Point> outerPoints = WorldPointService.getInstance().getRhoAroundPointsAll(pointId, flagRadius * 2 + 1);
		for (Point oPoint : outerPoints) {
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(oPoint.getId());
			if (worldPoint == null) {
				continue;
			}
			if (worldPoint.getPointType() != WorldPointType.WAR_FLAG_POINT_VALUE) {
				continue;
			}
			IFlag flag = FlagCollection.getInstance().getFlag(worldPoint.getGuildBuildId());
			if (!flag.hasManor()) {
				continue;
			}
			int checkRadius = flagRadius + WarFlagConstProperty.getInstance().getFlagRadius(flag.isCenter());
			// 战旗二期修改
			if (Math.abs(oPoint.getX() - pos[0]) + Math.abs(oPoint.getY() - pos[1]) > checkRadius) {
				continue;
			}
			flags.add(flag);
		}
		return flags;
	}
	
	/**
	 * 是否在自己联盟的旗帜范围内 
	 */
//	public boolean isInOwnFlagRange(int pointId, String guildId) {
//		if (!HawkOSOperator.isEmptyString(guildId)) {
//			return false;
//		}
//		
//		IFlag rangeFlag = null;
//		
//		List<IFlag> flags = getArroundCompFlags(pointId);
//		
//		if (flags.isEmpty()) {
//			return false;
//		}
//		
//		for (IFlag flag : flags) {
//			if (rangeFlag == null) {
//				rangeFlag = flag;
//				continue;
//			}
//			if (flag.getCompleteTime() == 0) {
//				continue;
//			}
//			if (flag.getCompleteTime() < rangeFlag.getCompleteTime()) {
//				rangeFlag = flag;
//			}
//		}
//		
//		return rangeFlag.getCurrentId().equals(guildId);
//	}
	
	/**
	 * 是否在其它联盟的旗帜范围内 
	 */
//	public boolean isInOtherFlagRange(int pointId, String guildId) {
//		IFlag rangeFlag = null;
//		
//		List<IFlag> flags = getArroundCompFlags(pointId);
//		
//		if (flags.isEmpty()) {
//			return false;
//		}
//		
//		for (IFlag flag : flags) {
//			if (rangeFlag == null) {
//				rangeFlag = flag;
//				continue;
//			}
//			if (flag.getCompleteTime() == 0) {
//				continue;
//			}
//			if (flag.getCompleteTime() < rangeFlag.getCompleteTime()) {
//				rangeFlag = flag;
//			}
//		}
//		
//		return HawkOSOperator.isEmptyString(guildId) || !rangeFlag.getCurrentId().equals(guildId);
//	}
	
	/**
	 * 通知旗帜开始被摧毁
	 */
	public void notifyFlagBeginDestroy(IFlag flag) {
		flag.setState(FlageState.FLAG_BEINVADED_VALUE);
		flag.setLastBuildTick(HawkTime.getMillisecond());
		flag.setSpeed(WarFlagService.getInstance().getCurrentSpeed(flag, true, false));
		
		long overTime = WarFlagService.getInstance().overTime(flag, true, false);
		Collection<IWorldMarch> marchs = getFlagPointMarch(flag);
		for (IWorldMarch worldMarch : marchs) {
			if (worldMarch.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_OCCUPY_VALUE) {
				continue;
			}
			worldMarch.getMarchEntity().setEndTime(overTime);
			worldMarch.updateMarch();
		}
		
		notifyArroundPointFlagUpdate(flag);
	}
	
	/**
	 * 通知旗帜变为修复状态
	 */
	public void notifyFlagToFix(IFlag flag) {
		flag.setState(FlageState.FLAG_FIX_VALUE);
		flag.setLastBuildTick(HawkTime.getMillisecond());
		notifyArroundPointFlagUpdate(flag);
	}

	/**
	 * 通知周边旗帜点状态刷新
	 */
	public void notifyArroundPointFlagUpdate(IFlag flag) {
		if (flag == null) {
			return;
		}
		
		List<IFlag> arroundCompFlags = getArroundCompFlags(flag.getPointId(), flag.isCenter());
		for(IFlag thisFlag : arroundCompFlags) {
			int[] pos = GameUtil.splitXAndY(thisFlag.getPointId());
			WorldPointService.getInstance().notifyPointUpdate(pos[0], pos[1]);
		}
		
		int[] pos = GameUtil.splitXAndY(flag.getPointId());
		WorldPointService.getInstance().notifyPointUpdate(pos[0], pos[1]);
	}
	
	/**
	 * 发母旗奖励
	 */
	public void sendCenterFlagAward(IFlag flag) {
		if (!flag.isCenter()) {
			return;
		}

		// 最终分配奖励
		Map<String, List<ItemInfo>> rewardInfo = new HashMap<>();
		// 需要分配的玩家列表
		List<String> playerIds = getPlayerIds(flag);
		// 母旗内所有奖励
		List<ItemInfo> boxItems = getBoxItem(flag);
		
		int playerIdIndex = 0;
		List<Integer> bigFlagRewardSortList = WarFlagConstProperty.getInstance().getBigFlagRewardSortList();
		for (int itemId : bigFlagRewardSortList) {
			for (ItemInfo box : boxItems) {
				if (box.getItemId() != itemId) {
					continue;
				}
				
				for (int i = 0; i < box.getCount(); i++) {
					String playerId = playerIds.get(playerIdIndex % playerIds.size());
					playerIdIndex++;
					
					List<ItemInfo> rewardList = rewardInfo.get(playerId);
					if (rewardList == null) {
						rewardList = new ArrayList<>();
						rewardInfo.put(playerId, rewardList);
					}
					rewardList.add(new ItemInfo(box.getType(), box.getItemId(), box.getCount()));
				}
			}
		}
		
		AwardItems centerAwards = AwardItems.valueOf();
		centerAwards.addItemInfos(boxItems);
		
		String guildTag = GuildService.getInstance().getGuildTag(flag.getOwnerId());
		guildTag = HawkOSOperator.isEmptyString(guildTag) ? "" : guildTag;
		int[] pos = GameUtil.splitXAndY(flag.getPointId());
		
		// 发奖
		for (Entry<String, List<ItemInfo>> entry : rewardInfo.entrySet()) {
			
			AwardItems award = AwardItems.valueOf();
			award.addItemInfos(entry.getValue());
			
			String playerId = entry.getKey();
			
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setMailId(MailId.WAR_FLAG_CENTER_ATK_REWARD)
					.setPlayerId(playerId)
					.setRewards(award.getAwardItems())
					.addContents(guildTag, pos[0], pos[1], ItemInfo.toString(centerAwards.getAwardItems()))
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.build());
		}
	}

	
	/**
	 * 获取旗子内所有奖励
	 */
	private List<ItemInfo> getBoxItem(IFlag flag) {
		List<ItemInfo> boxItem = new ArrayList<>();
		Collection<WarFlagSignUpItem> signUpInfos = flag.getSignUpInfos().values();
		for (WarFlagSignUpItem signUpInfo : signUpInfos) {
			for (String box : signUpInfo.getBox()) {
				boxItem.add(ItemInfo.valueOf(box));
			}
			for (int i = 0; i < signUpInfo.getSpecialBoxCount(); i++) {
				boxItem.add(ItemInfo.valueOf(WarFlagConstProperty.getInstance().getBigFlagSpecialReward()));
			}
		}
		return boxItem;
	}

	/**
	 * 获取抢夺玩家列表 有序
	 */
	private List<String> getPlayerIds(IFlag flag) {
		// 所有参与roll点的玩家
		List<String> playerIds = new ArrayList<>();
		for (IWorldMarch worldMarch : getFlagPointMarch(flag)) {
			if (playerIds.contains(worldMarch.getPlayerId())) {
				continue;
			}
			playerIds.add(worldMarch.getPlayerId());
		}
		Collections.shuffle(playerIds);
		return playerIds;
	}
	
	/**
	 * 解散战旗点上的行军
	 */
	public void dissolveFlagPointMarch(IFlag flag) {
		for (IWorldMarch worldMarch : getFlagPointMarch(flag)) {
			WorldMarchService.getInstance().onPlayerNoneAction(worldMarch, HawkTime.getMillisecond());
		}
	}
	
	/**
	 * 获取旗帜点上的行军
	 * @param flag
	 * @return
	 */
	public List<IWorldMarch> getFlagPointMarch(IFlag flag) {
		List<IWorldMarch> retMarchs = new ArrayList<>();
		List<IWorldMarch> marchs = new ArrayList<>();
		for ( String marchId : WorldMarchService.getInstance().getFlagMarchs(flag.getFlagId())) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (march != null) {
				marchs.add(march);
			}
		}
		for (IWorldMarch march : marchs) {
			retMarchs.add(march);
		}
		return retMarchs;
	}
	
	/**
	 * 获取当前建筑的 建设/摧毁 速度
	 * @return
	 */
	public double getCurrentSpeed(IFlag flag, boolean isBreak, boolean isBuildLife){
		int maxSpeed = GuildConstProperty.getInstance().getDefaultBuildSpeed();
		
		List<IWorldMarch> marchs = getFlagPointMarch(flag);
		
		//此处算兵力数量待定, 战斗力算法不一定
		float p1 = GuildConstProperty.getInstance().getBuildSpeedparameter1();
		float p2 = GuildConstProperty.getInstance().getBuildSpeedparameter2();
		float d1 = GuildConstProperty.getInstance().getDestroySpeedparameter();
		
		//速度值
		double totalSpeed = 0;
		for (IWorldMarch worldMarch : marchs) {
			String playerId = worldMarch.getMarchEntity().getPlayerId();
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			int effValue = player.getEffect().getEffVal(EffType.GUILD_BUILD_SPEED_UP, worldMarch.getMarchEntity().getEffectParams());
			
			//判断队列状态是否已经正确
			for (ArmyInfo armyInfo : worldMarch.getMarchEntity().getArmys()) {
				int cnt = armyInfo.getFreeCnt();
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
				float unitPower = cfg.getPower();
				//速度=（战斗力^p1）* p2
				double speed = Math.pow(unitPower, p1) * p2 * (1 + effValue * GsConst.EFF_PER);
				totalSpeed += cnt * speed;
			}
		}
		if(isBreak){
			totalSpeed *= d1;
		}
		
		if (!isBuildLife) {
			int flagOccupyDouble = WarFlagConstProperty.getInstance().getFlagOccupyDouble();
			totalSpeed *= flagOccupyDouble;
		}
		return Math.min(totalSpeed, maxSpeed);
	}
	
	/**
	 * 获取结束时间
	 */
	public long overTime(IFlag flag, boolean isBreak, boolean isBuildLife) {
		int maxLife = WarFlagConstProperty.getInstance().getMaxBuildLife(flag.isCenter());
		if (!isBuildLife) {
			maxLife = WarFlagConstProperty.getInstance().getFlagOccupy(flag.isCenter());
		}
		
		int curLife = flag.getLife();
		if (!isBuildLife) {
			curLife = flag.getOccupyLife();
		}
		
		int leftLife = maxLife - curLife;
		if (isBreak) {
			leftLife = curLife;
		}
		
		leftLife = Math.max(0, leftLife);
		return flag.getLastBuildTick() + (long)Math.ceil(leftLife / getCurrentSpeed(flag, isBreak, isBuildLife)) * 1000L;
	}
	
	/**
	 * 战旗是否是可交战状态
	 */
	public boolean canFlagFight(IFlag flag, String ownGuildId) {
		if (!flag.hasManor()) {
			return false;
		}
		
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(flag.getPointId());
		if (worldPoint == null) {
			return false;
		}
		
		if (worldPoint.getPointType() != WorldPointType.WAR_FLAG_POINT_VALUE) {
			return false;
		}
		
		List<String> inOtherManorGuildIds = inOtherManorGuildIds(flag.getCurrentId(), flag.getPointId(), flag.isCenter());
		boolean isMy = flag.getCurrentId().equals(ownGuildId);
		return inOtherManorGuildIds.contains(ownGuildId) || (isMy && !inOtherManorGuildIds.isEmpty());
	}

	/**
	 * 获取所有可交战旗帜
	 */
	public List<IFlag> getAllCanFightFlag(String guildId) {
		List<IFlag> canFightFlags = new ArrayList<>();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return canFightFlags;
		}
		
		List<String> currFlagIds = FlagCollection.getInstance().getCurrFlagIds(guildId);
		for (String flagId : currFlagIds) {
			IFlag flag = FlagCollection.getInstance().getFlag(flagId);
			if (!canFlagFight(flag, guildId)) {
				continue;
			}
			canFightFlags.add(flag);
		}
		
		return canFightFlags;
	}
	
	/**
	 * 解散联盟
	 */
	public void onDismissGuild(String guildId) {
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.REMOVE_GUILD_WAR_FLAG) {
			@Override
			public boolean onInvoke() {
				
				List<String> currFlagIds = new ArrayList<>();
				currFlagIds.addAll(FlagCollection.getInstance().getOwnerFlagIds(guildId));
				currFlagIds.addAll(FlagCollection.getInstance().getCenterFlagIds(guildId));
				
				for (String flagId : currFlagIds) {
					IFlag flag = FlagCollection.getInstance().getFlag(flagId);
					if (flag == null) {
						continue;
					}
					
					if (flag.getState() == FlageState.FLAG_UNLOCKED_VALUE
							|| flag.getState() == FlageState.FLAG_LOCKED_VALUE) {
						continue;
					}
					
					// 解散行军
					dissolveFlagPointMarch(flag);
					
					WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(flag.getPointId());
					if (worldPoint != null && worldPoint.getPointType() == WorldPointType.WAR_FLAG_POINT_VALUE) {
						WorldPointService.getInstance().removeWorldPoint(flag.getPointId(), false);						
					}
					
					FlagCollection.getInstance().removeFlag(flag);
					flag.delete();
				}
				return true;
			}
		});
	}
	
	/**
	 * 获取玩家战地旗帜资源
	 */
	public List<ItemInfo> getPlayerFlagResource(String playerId) {
		List<ItemInfo> resource = flagResource.get(playerId);
		if (resource != null) {
			return resource;
		}
		resource = new CopyOnWriteArrayList<ItemInfo>();
		for (ItemInfo item : LocalRedis.getInstance().getFlagResource(playerId)) {
			resource.add(item);
		}
		flagResource.put(playerId, resource);
		return resource;
	}
	
	/**
	 * 增加玩家战旗资源
	 * @param playerId
	 * @param itemInfo
	 */
	public void addPlayerFlagResource(String playerId, ItemInfo itemInfo) {
		List<ItemInfo> playerResource = getPlayerFlagResource(playerId);
		
		boolean hasAdd = false;
		for (ItemInfo item : playerResource) {
			if (item.getItemId() != itemInfo.getItemId()) {
				continue;
			}
			item.addCount((int)itemInfo.getCount());
			hasAdd = true;
		}
		if (!hasAdd) {
			playerResource.add(itemInfo);
		}
	}
	
	/**
	 * 玩家增加战旗产出资源
	 */
	public void addFlagTickResource(IFlag flag, String guildId) {
	
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(flag.getPointId());
		if (point == null) {
			return;
		}
		
		List<ItemInfo> resource = WarFlagConstProperty.getInstance().getResource();
		WarFlagLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(WarFlagLevelCfg.class, point.getZoneId());
		if (cfg != null) {
			resource = cfg.getResourceInfo();
		}
		
		Collection<String> members = GuildService.getInstance().getGuildMembers(guildId);
		for (String playerId : members) {
			
			for (ItemInfo item : resource) {
				int addCount = getPlayerAddResCount(playerId, item.getItemId(), (int)item.getCount(), members.size());
				addPlayerFlagResource(playerId, new ItemInfo(ItemType.PLAYER_ATTR_VALUE, item.getItemId(), addCount));
			}
		}
		
	}
	
	/**
	 * 玩家增加战旗内资源点产出资源
	 */
	public void addPointResource(int pointId, int itemId, int itemCount) {
		try {
			int rate = WarFlagConstProperty.getInstance().getPointProRate();
			itemCount = (int)Math.ceil(GsConst.EFF_PER * itemCount * rate);
			
			IFlag flag = WarFlagService.getInstance().getUnderRangeFlag(pointId);
			if (flag != null) {
				Collection<String> members = GuildService.getInstance().getGuildMembers(flag.getCurrentId());
				for (String playerId : members) {
					int addCount = getPlayerAddResCount(playerId, itemId, itemCount, members.size());
					addPlayerFlagResource(playerId, new ItemInfo(ItemType.PLAYER_ATTR_VALUE, itemId, addCount));	
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 添加玩家战旗资源 
	 */
	private int getPlayerAddResCount(String playerId, int resourceId, int count, int memberCount) {
		int limit = WarFlagConstProperty.getInstance().getResourceLimit(resourceId);
		
		int playerCount = 0;
		List<ItemInfo> playerResource = getPlayerFlagResource(playerId);
		for (ItemInfo pr : playerResource) {
			if (pr.getItemId() != resourceId) {
				continue;
			}
			if (pr.getCount() >= limit) {
				return 0;
			}
			playerCount = (int)pr.getCount();
		}
		return Math.min(limit - playerCount, count / memberCount);
	}
	
	/**
	 * 收取战旗资源
	 */
	public void collectFlaResource(Player player) {
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.COLLECT_FLAG_RESOURCE) {
			@Override
			public boolean onInvoke() {
				
				List<ItemInfo> resource = getPlayerFlagResource(player.getId());
				AwardItems reward = AwardItems.valueOf();
				reward.addItemInfos(resource);
				player.dealMsg(MsgId.WORLD_AWARD_PUSH, new WorldAwardPushInvoker(player, reward, Action.COLLECT_WAR_FLAG_RESOURCE, false, null));
				
				List<ItemInfo> afterResource = new CopyOnWriteArrayList<ItemInfo>();
				flagResource.put(player.getId(), afterResource);
				
				return true;
			}
		});
	}
	
	/**
	 * 存储玩家资源，停服时调用
	 */
	public void savePlayerResource() {
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.SAVE_FLAG_RESOURCE) {
			@Override
			public boolean onInvoke() {
				for (Entry<String, List<ItemInfo>> resource : flagResource.entrySet()) {
					LocalRedis.getInstance().updateFlagResource(resource.getKey(), ItemInfo.toString(resource.getValue()));
				}
				return true;
			}
		});
	}
	
	/**
	 * 点所属的范围的旗帜
	 */
	public IFlag getUnderRangeFlag(int pointId) {
		int pos[] = GameUtil.splitXAndY(pointId);
		
		List<Point> points = null;
		IFlag retFlag = null;
		
		int flagRadius = WarFlagConstProperty.getInstance().getFlagRadius(true);
		points = WorldPointService.getInstance().getRhoAroundPointsAll(pointId, flagRadius + 1);
		for (Point oPoint : points) {
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(oPoint.getId());
			if (worldPoint == null) {
				continue;
			}
			if (worldPoint.getPointType() != WorldPointType.WAR_FLAG_POINT_VALUE) {
				continue;
			}
			IFlag flag = FlagCollection.getInstance().getFlag(worldPoint.getGuildBuildId());
			if (flag == null) {
				continue;
			}
			if (!flag.hasManor()) {
				continue;
			}
			
			// 战旗二期修改
			int checkRadius = WarFlagConstProperty.getInstance().getFlagRadius(flag.isCenter());
			if (Math.abs(oPoint.getX() - pos[0]) + Math.abs(oPoint.getY() - pos[1]) > checkRadius) {
				continue;
			}
			
			if (retFlag == null) {
				retFlag = flag;
				continue;
			}
			if (retFlag.getCompleteTime() < flag.getCompleteTime()) {
				continue;
			}
			retFlag = flag;
		}
		
		return retFlag;
	}
	
	/**
	 * 领地范围是否有战旗
	 */
	public boolean isManorRangeHasWarFlag(int manorPos) {
		int manorRadius = GuildConstProperty.getInstance().getManorRadius();
		
		List<Point> innerPoints = WorldPointService.getInstance().getRhoAroundPointsAll(manorPos, manorRadius + 1);
		for (Point iPoint : innerPoints) {
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(iPoint.getId());
			if (worldPoint == null) {
				continue;
			}
			if (worldPoint.getPointType() != WorldPointType.WAR_FLAG_POINT_VALUE) {
				continue;
			}
			return true;
		}
		
		return false;
	}
	
	/**
	 * 获取旗帜显示状态
	 */
	public int getFlagViewStatus(String viewerId, String flagId) {
		Player viewer = GlobalData.getInstance().makesurePlayer(viewerId);
		if (viewer == null) {
			return FlagViewState.FLAG_VIEW_NONE_VALUE;
		}
		
		if (!viewer.hasGuild()) {
			return FlagViewState.FLAG_VIEW_NONE_VALUE;
		}
		
		IFlag flag = FlagCollection.getInstance().getFlag(flagId);
		if (flag == null) {
			return FlagViewState.FLAG_VIEW_NONE_VALUE;
		}
		
		if (!flag.hasManor()) {
			return FlagViewState.FLAG_VIEW_NONE_VALUE;
		}
		
		// 已驻防（我方可见）
		if (viewer.getGuildId().equals(flag.getCurrentId())) {
			Player flagLeader = WorldMarchService.getInstance().getFlagLeader(flagId);
			if (flagLeader != null && flagLeader.hasGuild() && viewer.getGuildId().equals(flagLeader.getGuildId())) {
				return FlagViewState.FLAG_VIEW_QUARTER_VALUE;
			}
		}
		
		// 可交战（敌我双方均显示）
		if (canFlagFight(flag, viewer.getGuildId())) {
			if (flag.getCurrentId().equals(viewer.getGuildId())) {
				return FlagViewState.FLAG_VIEW_CAN_DEF_VALUE;
			} else {
				return FlagViewState.FLAG_VIEW_CAN_ATK_VALUE;
			}
		}
		
		// 已被占领 （他人占我）
		if (viewer.getGuildId().equals(flag.getOwnerId()) && !viewer.getGuildId().equals(flag.getCurrentId())) {
			return FlagViewState.FLAG_VIEW_BE_OCCUPY_VALUE;
		}
		
		// 已占领（我占他人）
		if (!viewer.getGuildId().equals(flag.getOwnerId()) && viewer.getGuildId().equals(flag.getCurrentId())) {
			return FlagViewState.FLAG_VIEW_OCCUPY_VALUE;
		}
		
		return FlagViewState.FLAG_VIEW_NONE_VALUE;
	}
	
	/**
	 * 推送小红点儿 
	 */
	public void pushRedPoint(Player player) {
		if (!player.hasGuild()) {
			return;
		}
		
		FlagRedPointPush.Builder builder = FlagRedPointPush.newBuilder();
		builder.setFlagCount(FlagCollection.getInstance().getCurrFlagIds(player.getGuildId()).size());
		List<ItemInfo> resource = getPlayerFlagResource(player.getId());
		int resCount = 0;
		for (ItemInfo item : resource) {
			resCount += item.getCount();
		}
		builder.setHasReward(resCount > 0);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.FLAG_RED_POINT_PUSH, builder));		
	}
	
	/**
	 * 推送战旗格局 
	 */
	public void  pushFlagPatternInfo(Player player) {
		
		int placedFlagCount = 0;
		
		int color = 0;
		Map<String, List<String>> allCurrFlags = FlagCollection.getInstance().getAllCurrFlagIds();
		FlagPatternResp.Builder resp = FlagPatternResp.newBuilder();
		
		Set<Integer> hasPush = new HashSet<>();
		
		int flagMapRatio = WarFlagConstProperty.getInstance().getFlagMapRatio();
		for (Entry<String, List<String>> entry : allCurrFlags.entrySet()) {
			color++;
			
			List<String> allFlagIds = new ArrayList<>();
			allFlagIds.addAll(entry.getValue());
			allFlagIds.addAll(FlagCollection.getInstance().getCenterFlagIds(entry.getKey()));
			
			for (String flagId : allFlagIds) {
				IFlag flag = FlagCollection.getInstance().getFlag(flagId);
				if (flag.getState() == FlageState.FLAG_UNLOCKED_VALUE) {
					continue;
				}
				if (flag.getState() == FlageState.FLAG_PLACED_VALUE) {
					continue;
				}
				
				boolean isCenter = false;
				if (flag.isCenter()) {
					isCenter = true;
				}
				
				placedFlagCount++;
				
				boolean change = false;
				
				int pos[] = GameUtil.splitXAndY(flag.getPointId());
				int afterPosX = pos[0] / flagMapRatio;
				int afterPosY = pos[1] / flagMapRatio;
				if ((afterPosX + afterPosY) % 2 != 0) {
					afterPosX++;
					change = true;
				}
				
				int afterPointId = GameUtil.combineXAndY(afterPosX, afterPosY);
				if (hasPush.contains(afterPointId)) {
					continue;
				}
				hasPush.add(afterPointId);
				
				long flagInfo = afterPosX * GsConst.FLAG_MAP_PARAM * GsConst.FLAG_MAP_PARAM + afterPosY * GsConst.FLAG_MAP_PARAM + color;
				
				if (player.hasGuild() && player.getGuildId().equals(flag.getCurrentId())) {
					flagInfo = afterPosX * GsConst.FLAG_MAP_PARAM * GsConst.FLAG_MAP_PARAM + afterPosY * GsConst.FLAG_MAP_PARAM + 0;
				}
				
				if (change) {
					flagInfo += 1 * GsConst.FLAG_MAP_PARAM * GsConst.FLAG_MAP_PARAM * GsConst.FLAG_MAP_PARAM;
				}
				
				if (isCenter) {
					flagInfo += 1L * GsConst.FLAG_MAP_PARAM * GsConst.FLAG_MAP_PARAM * GsConst.FLAG_MAP_PARAM * 10;
				}
				
				resp.addFlagInfo(flagInfo);
			}
		}
		
		logger.info("worldFlagInfo, placedCount:{}, flagCount:{}", placedFlagCount, FlagCollection.getInstance().getFlagsCount());
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.FLAG_PATTERN_RESP, resp));
	}
	
	/**
	 * 推送战旗驻军信息 
	 */
	public void  pushFlagQuarterInfo(Player player, String flagId) {
		
		ManorPlayerInfoList.Builder builder = ManorPlayerInfoList.newBuilder();
		
		IFlag flag = FlagCollection.getInstance().getFlag(flagId);
		if (flag == null) {
			return;
		}
		
		builder.setRemoveTime(flag.getRemoveTime());
		builder.setShowList(false);
		builder.setBuildLife(flag.getCurrBuildLife());
		builder.setLevel(0);
		builder.setOccupyLife(flag.getCurrOccupyLife());
		
		if (!player.hasGuild()) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.MANOR_GARRSION_LIST_S_VALUE, builder));
			return;
		}
		
		BlockingDeque<String> marchs = WorldMarchService.getInstance().getFlagMarchs(flagId);
		if (marchs.isEmpty()) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.MANOR_GARRSION_LIST_S_VALUE, builder));
			return;
		}
		
		String leaderMarchId = WorldMarchService.getInstance().getFlagLeaderMarchId(flag.getFlagId());
		IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(leaderMarchId);
		Player leader = WorldMarchService.getInstance().getFlagLeader(flag.getFlagId());
		
		if (leader != null && leader.hasGuild() && !leader.getGuildId().equals(player.getGuildId())) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.MANOR_GARRSION_LIST_S_VALUE, builder));
			return;
		}
		
		if(leaderMarch != null && leader != null){
			int maxMassSoldierNum = leaderMarch.getMaxMassJoinSoldierNum(leader);
			builder.setMaxArmyCount(maxMassSoldierNum);
			builder.setMaxMarchCount(0);
			builder.setShowList(true);
			if (flag.getState() == FlageState.FLAG_BUILDING_VALUE || flag.getState() == FlageState.FLAG_BEINVADED_VALUE
					 || flag.getState() == FlageState.FLAG_FIX_VALUE) {
				boolean isBreak = flag.getState() != FlageState.FLAG_BUILDING_VALUE && flag.getState() != FlageState.FLAG_FIX_VALUE;
				boolean isBuildLife = flag.getState() == FlageState.FLAG_BUILDING_VALUE;
				long overTime = overTime(flag, isBreak, isBuildLife);
				builder.setOverTime((int)((overTime - HawkTime.getMillisecond()) / 1000));
			}
		}
		
		for (String marchId : marchs) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			ManorPlayerInfo.Builder infoBuilder = ManorPlayerInfo.newBuilder();
			// 获取玩家对象
			Player member = GlobalData.getInstance().makesurePlayer(march.getPlayerId());
			infoBuilder.setPlayerId(member.getId());
			infoBuilder.setName(member.getName());
			infoBuilder.setPfIcon(member.getPfIcon());
			infoBuilder.setIcon(member.getIcon());
			infoBuilder.setGuildTag(member.getGuildTag());
			List<ArmyInfo> armyInfos = march.getMarchEntity().getArmys();
			for (ArmyInfo armyInfo : armyInfos) {
				KeyValuePairInt.Builder kb = KeyValuePairInt.newBuilder();
				kb.setKey(armyInfo.getArmyId());
				kb.setVal(armyInfo.getFreeCnt());
				kb.setSoldierStar(member.getSoldierStar(armyInfo.getArmyId()));
				kb.setSoldierPlantStep(member.getSoldierStep(armyInfo.getArmyId()));
				kb.setPlantSkillLevel(member.getSoldierPlantSkillLevel(armyInfo.getArmyId()));
				kb.setPlantMilitaryLevel(member.getSoldierPlantMilitaryLevel(armyInfo.getArmyId()));
				infoBuilder.addArmy(kb.build());
			}
			List<PlayerHero> hero = march.getHeros();
			for (PlayerHero playerHero : hero) {
				infoBuilder.addHeros(playerHero.toPBobj());
			}
			Optional<SuperSoldier> sups = member.getSuperSoldierByCfgId(march.getSuperSoldierId());
			if(sups.isPresent()){
				infoBuilder.setSsoldier(sups.get().toPBobj());
			}
			int maxMassSoldierNum = march.getMaxMassJoinSoldierNum(member);
			infoBuilder.setMaxArmyCount(maxMassSoldierNum);
			builder.addInfos(infoBuilder.build());
		}
		
		//发送消息
		player.sendProtocol(HawkProtocol.valueOf(HP.code.MANOR_GARRSION_LIST_S_VALUE, builder));
	}
	
	/**
	 * 任命队长
	 */
	public boolean cheangeQuarterLeader(int protocol, Player player, String targetId, String flagId) {
		IFlag flag = FlagCollection.getInstance().getFlag(flagId);
		if (flag == null) {
			return false;
		}
		
		if (!player.hasGuild()) {
			return false;
		}
		
		// 队长
		Player leader = WorldMarchService.getInstance().getFlagLeader(flagId);
		
		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.WAR_FLAG_MARCH);
		boolean isLeader = player.getId().equals(leader.getId());
		if (!guildAuthority && !isLeader) {
			player.sendError(protocol, Status.WarFlagError.FLAG_CHANGE_LEADER_AUTH_VALUE, 0);
			return false;
		}
		
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.CHANGE_QUARTER_LEADER) {
			@Override
			public boolean onInvoke() {
				Player targetPlayer = GlobalData.getInstance().makesurePlayer(targetId);
				if (!player.getGuildId().equals(targetPlayer.getGuildId())) {
					return false;
				}
				WorldMarchService.getInstance().changeFlagMarchLeader(flagId, targetId);
				pushFlagQuarterInfo(player, flagId);
				return true;
			}
		});
		
		return true;
	}
	
	/**
	 * 遣返行军
	 */
	public boolean repatriateMarch(int protocol, Player player, String targetPlayerId, String flagId) {
		IFlag flag = FlagCollection.getInstance().getFlag(flagId);
		if (flag == null) {
			return false;
		}
		
		if (!player.hasGuild()) {
			return false;
		}
		
		// 队长
		Player leader = WorldMarchService.getInstance().getFlagLeader(flagId);
		
		// R4盟主队长可以遣返
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.WAR_FLAG_MARCH);
		boolean isLeader = player.getId().equals(leader.getId());
		if (!guildAuthority && !isLeader) {
			player.sendError(protocol, Status.WarFlagError.FLAG_REPATRIATE_AUTH_VALUE, 0);
			return false;
		}
		
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.REPATRIATE_MARCH) {
			@Override
			public boolean onInvoke() {
				BlockingDeque<String> marchIds = WorldMarchService.getInstance().getFlagMarchs(flagId);
				for (String marchId : marchIds) {
					IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
					if (march.isReturnBackMarch()) {
						continue;
					}
					if (!march.getPlayerId().equals(targetPlayerId)) {
						continue;
					}
					if (!player.getGuildId().equals(march.getPlayer().getGuildId())) {
						continue;
					}
					
					WorldMarchService.logger.info("marchRepatriate, playerId:{}, tarPlayerId:{}, marchId:{}", player.getId(), targetPlayerId, marchId);
					
					WorldMarchService.getInstance().onPlayerNoneAction(march, HawkApp.getInstance().getCurrentTime());
				}
				pushFlagQuarterInfo(player, flagId);
				return true;
			}
		});

		return true;
	}
	
	/**
	 * 同步母旗信息
	 */
	public void syncCenterFlagInfo(Player player, String flagId) {
		IFlag flag = FlagCollection.getInstance().getFlag(flagId);
		if (flag == null) {
			return;
		}
		
		if (!flag.isCenter()) {
			return;
		}
		
		checkCenterFlag(flag);
		
		CenterFlagInfoResp.Builder builder = CenterFlagInfoResp.newBuilder();
		for (WarFlagSignUpItem signUpInfo : flag.getSignUpInfos().values()) {
			Player suPlayer = GlobalData.getInstance().makesurePlayer(signUpInfo.getPlayerId());
			if (suPlayer == null) {
				continue;
			}
			
			CenterFlagSingUpInfo.Builder signUpInfoBuilder = CenterFlagSingUpInfo.newBuilder();
			signUpInfoBuilder.setIcon(suPlayer.getIcon());
			signUpInfoBuilder.setPficon(suPlayer.getPfIcon());
			signUpInfoBuilder.setGuildTag(suPlayer.getGuildTag());
			signUpInfoBuilder.setName(suPlayer.getName());
			for (String boxStr : signUpInfo.box) {
				signUpInfoBuilder.addBox(boxStr);
			}
			signUpInfoBuilder.setSpecialBoxCount(signUpInfo.getSpecialBoxCount());
			signUpInfoBuilder.setPlayerId(suPlayer.getId());
			signUpInfoBuilder.setProRemainTime(signUpInfo.getNextTickTime());
			builder.addSignUpInfo(signUpInfoBuilder);
		}
		builder.setMarchTime(flag.getCenterNextTickTime());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.FLAG_CENTER_INFO_RESP, builder));
	}

	/**
	 * 检测母旗
	 */
	public boolean checkCenterFlag(IFlag flag) {
		
		// TODO 旗子状态检测  世界点存在可是旗子状态不对/旗子是放置状态可是世界点不在
		
		// 如果有状态错误，或者已经不在本联盟的玩家，则移除这个玩家的报名信息
		List<String> playerIds = new ArrayList<>();
		for (WarFlagSignUpItem signUpInfo : flag.getSignUpInfos().values()) {
			Player suPlayer = GlobalData.getInstance().makesurePlayer(signUpInfo.getPlayerId());
			if (suPlayer == null) {
				playerIds.add(signUpInfo.getPlayerId());
				continue;
			}
			if (!suPlayer.hasGuild() || !suPlayer.getGuildId().equals(flag.getOwnerId())) {
				playerIds.add(signUpInfo.getPlayerId());
				continue;
			}
		}
		for (String playerId : playerIds) {
			flag.rmSignUpInfo(playerId);
		}

		
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(flag.getPointId());
		
		// 计算产出
		boolean needUpdate = false;
		for (WarFlagSignUpItem signUpInfo : flag.getSignUpInfos().values()) {
			if (checkCenterProduct(signUpInfo, worldPoint.getZoneId(), flag.isCenterActive())) {
				needUpdate = true;
			}
		}
		
		if (needUpdate) {
			flag.notifyUpdate();
		}
		
		return true;
	}
	
	/**
	 * 检测母旗产出
	 */
	public boolean checkCenterProduct(WarFlagSignUpItem signUpInfo, int areaLevel, boolean isCenterActive) {
		long currentTime = HawkTime.getMillisecond();
		if (currentTime < signUpInfo.getNextTickTime()) {
			return false;
		}
		
		// tick周期
		long tickPeroid = WarFlagConstProperty.getInstance().getBigFlagCellsTickTime();

		// tick次数
		int tickTimes = 1 + (int)((HawkTime.getMillisecond() - signUpInfo.getNextTickTime()) / tickPeroid);
		
		// 下次tick时间
		long remainTime = (HawkTime.getMillisecond() - signUpInfo.getNextTickTime()) % tickPeroid;
		long nextTickTime = HawkTime.getMillisecond() + tickPeroid - remainTime;
		signUpInfo.setNextTickTime(nextTickTime);
		
		// 根据地块等级确认奖励配置
		WarFlagLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(WarFlagLevelCfg.class, areaLevel);
		if (levelCfg == null) {
			HawkConfigManager.getInstance().getConfigByIndex(WarFlagLevelCfg.class, 0);
		}
		
		// 添加宝箱
		for (int i = 0; i < tickTimes; i++) {
			AwardItems award = AwardItems.valueOf(levelCfg.getAward());
			signUpInfo.getBox().add(award.getAwardItems().get(0).toString());
			
			// 添加特殊宝箱奖励
			if (isCenterActive) {
				signUpInfo.setSpecialBoxCount(signUpInfo.getSpecialBoxCount() + 1);
			}
			
		}
		return true;
	}
	
	/**
	 * 获取母旗下次结算时间(行军发奖)
	 */
	public long calcCentNextTickTime(IFlag flag) {
		if (!flag.isCenter()) {
			return 0L;
		}
		
		// 下次tick时间
		long tickTime = 0L;
		
		// 当前时间
		long currentTime = HawkTime.getMillisecond();
		
		// 今日零点时间
		long zeroTime = HawkTime.getAM0Date().getTime();
		
		List<Integer> bigFlagAccountTime = WarFlagConstProperty.getInstance().getBigFlagAccountTime();
		for (int clock : bigFlagAccountTime) {
			long calcTime = zeroTime + clock * 3600 * 1000L;
			if (currentTime >= calcTime) {
				continue;
			}
			tickTime = calcTime;
			break;
		}
		
		if (tickTime == 0L) {
			// 第二天的第一个整点
			tickTime = zeroTime + 86400 * 1000L + bigFlagAccountTime.get(0) * 3600 * 1000L;
		}
		
		// 加上延迟时间
		tickTime += HawkRand.randInt(WarFlagConstProperty.getInstance().getBigFlagAccountTimeDelay());
		
		return tickTime;
	}
	
	/**
	 * 发母旗奖励
	 */
	public void sendCenterAwardMail(WarFlagSignUpItem signUpInfo, int pointId) {
		String playerId = signUpInfo.getPlayerId();

		List<ItemInfo> itmes = new ArrayList<>();
		List<String> boxInfos = signUpInfo.getBox();
		for (String box : boxInfos) {
			itmes.add(ItemInfo.valueOf(box));
		}
		int specialBoxCount = signUpInfo.getSpecialBoxCount();
		if (specialBoxCount > 0) {
			ItemInfo item = ItemInfo.valueOf(WarFlagConstProperty.getInstance().getBigFlagSpecialReward());
			item.setCount(item.getCount() * specialBoxCount);
			itmes.add(item);
		}

		AwardItems award = AwardItems.valueOf();
		award.addItemInfos(itmes);
		
		int[] pos = GameUtil.splitXAndY(pointId);
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		String guildTag = GuildService.getInstance().getGuildTag(player.getId());
		if (guildTag == null) {
			guildTag = "";
		}
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.setMailId(MailId.WAR_FLAG_CENTER_REWARD)
				.setPlayerId(playerId)
				.addContents(guildTag, pos[0], pos[1])
				.setRewards(award.getAwardItems())
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.build());
	}
	
	/**
	 * 母旗激活数量
	 * 母旗范围连接到总部范围就算是激活
	 */
	public int getCenterActiveCount() {
		
		int count = 0;
		
		// 王座坐标和王座半径
		int kingPointId = WorldMapConstProperty.getInstance().getCenterPointId();
		int[] kingPos = GameUtil.splitXAndY(kingPointId);
		int kingRadius = WorldMapConstProperty.getInstance().getKingPalaceRange()[0];
		
		// 母旗半径
		int bigFlagRadius = WarFlagConstProperty.getInstance().getBigFlagRadius();
		
		List<Point> outerPoints = WorldPointService.getInstance().getRhoAroundPointsAll(kingPointId, kingRadius + bigFlagRadius + 1);
		for (Point oPoint : outerPoints) {
			// 四个顶点上的不要
			if (WorldUtil.distance(kingPos[0], kingPos[1], oPoint.getX(), oPoint.getY()) == kingRadius + bigFlagRadius) {
				continue;
			}
			WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(oPoint.getId());
			if (worldPoint == null) {
				continue;
			}
			if (worldPoint.getPointType() != WorldPointType.WAR_FLAG_POINT_VALUE) {
				continue;
			}
			IFlag flag = FlagCollection.getInstance().getFlag(worldPoint.getGuildBuildId());
			if (!flag.hasManor()) {
				continue;
			}
			if (!flag.isCenter()) {
				continue;
			}
			if (!flag.isCenterActive()) {
				continue;
			}
			count++;
		}
		
		return count;
	}
	
	/**
	 * 移除向旗子集结或者攻击的队伍
	 */
	public void rmFlagTargetMarch(IFlag flag) {
		if (flag == null) {
			return;
		}
		
		WorldPoint wp = WorldPointService.getInstance().getWorldPoint(flag.getPointId());
		
		// 联盟战争界面移除
		Collection<IWorldMarch> guildMarchs = WorldMarchService.getInstance().getGuildMarchs(wp.getGuildId());
		for (IWorldMarch guildMarch : guildMarchs) {
			if (guildMarch.getMarchEntity().getTerminalId() == wp.getId()) {
				WorldMarchService.getInstance().rmGuildMarch(guildMarch.getMarchId());
			}
		}
		
		Collection<IWorldMarch> worldPointMarch = WorldMarchService.getInstance().getWorldPointMarch(wp.getX(), wp.getY());
		for (IWorldMarch march : worldPointMarch) {
			
			// 向联盟堡垒集结类型并且等待中的行军解散
			if (march.isMassMarch() && march.getMarchEntity().getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
				Set<IWorldMarch> massJoinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(march, true);
				for (IWorldMarch massJoinMarch : massJoinMarchs) {
					WorldMarchService.getInstance().onPlayerNoneAction(massJoinMarch, HawkTime.getMillisecond());
				}
				WorldMarchService.getInstance().onMarchReturnImmediately(march, march.getMarchEntity().getArmys());
			}
			
			// 向联盟堡垒集结类型并且行军中的行军解散
			if (march.isMassMarch() && march.isMarchState()) {
				AlgorithmPoint point = WorldUtil.getMarchCurrentPosition(march.getMarchEntity());
				Set<IWorldMarch> joinMarchs = WorldMarchService.getInstance().getMassJoinMarchs(march, true);
				for (IWorldMarch joinMarch : joinMarchs) {
					joinMarch.getMarchEntity().setCallBackX(point.getX());
					joinMarch.getMarchEntity().setCallBackY(point.getY());
					WorldMarchService.getInstance().onMarchCallBack(joinMarch);
				}
				
				march.getMarchEntity().setCallBackX(point.getX());
				march.getMarchEntity().setCallBackY(point.getY());
				WorldMarchService.getInstance().onMarchCallBack(march);
			}
			
			if (march.isMarchState() && 
					(march.getMarchEntity().getMarchType() == WorldMarchType.MANOR_ASSISTANCE_VALUE
					|| march.getMarchEntity().getMarchType() == WorldMarchType.MANOR_SINGLE_VALUE
					|| march.getMarchEntity().getMarchType() == WorldMarchType.MANOR_BUILD_VALUE
					|| march.getMarchEntity().getMarchType() == WorldMarchType.MANOR_REPAIR_VALUE)) {
				AlgorithmPoint point = WorldUtil.getMarchCurrentPosition(march.getMarchEntity());
				march.getMarchEntity().setCallBackX(point.getX());
				march.getMarchEntity().setCallBackY(point.getY());
				WorldMarchService.getInstance().onMarchCallBack(march);
			}
		}
	}
}
