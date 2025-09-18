package com.hawk.game.guild.manor;

import java.util.List;
import java.util.Set;

import org.hawk.enums.EnumUtil;
import org.hawk.enums.IndexedEnum;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.guild.manor.building.GuildDragonTrap;
import com.hawk.game.guild.manor.building.GuildManorSuperMine;
import com.hawk.game.guild.manor.building.IGuildBuilding;
import com.hawk.game.item.AwardItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.GuildManor.GuildManorList;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.log.LogConst.GuildAction;

/**
 * 联盟行军
 * @author zhenyu.shang
 * @since 2017年7月13日
 */
public enum ManorMarchEnum implements IndexedEnum {
	
	/** 单人攻占联盟领地 */
	MANOR_SINGLE(WorldMarchType.MANOR_SINGLE_VALUE) {
		@Override
		public int checkMarch(WorldPoint point, Player player, boolean reachCheck) {
			return attackCheck(point, player);
		}

		@Override
		public boolean onMarchReach(WorldPoint point, IWorldMarch march) {
			return enemyManorMarchReach(point, march, GuildAction.GUILD_ATK_MANOR);
		}
	},
	/** 集结攻占联盟领地 */
	MANOR_MASS	(WorldMarchType.MANOR_MASS_VALUE) {
		@Override
		public int checkMarch(WorldPoint point, Player player, boolean reachCheck) {
			return attackCheck(point, player);
		}

		@Override
		public boolean onMarchReach(WorldPoint point, IWorldMarch march) {
			return enemyManorMarchReach(point, march, GuildAction.GUILD_ATK_MANOR);
		}
	},
	/** 集结攻占联盟领地加入者 */
	MANOR_MASS_JOIN (WorldMarchType.MANOR_MASS_JOIN_VALUE) {
		@Override
		public int checkMarch(WorldPoint point, Player player, boolean reachCheck) {
			return Status.SysError.SUCCESS_OK_VALUE;
		}

		@Override
		public boolean onMarchReach(WorldPoint point, IWorldMarch march) {
			return enemyManorMarchReach(point, march, null);
		}
	},
	/** 联盟超级矿采集行军类型 */
	MANOR_COLLECT(WorldMarchType.MANOR_COLLECT_VALUE) {
		@Override
		public int checkMarch(WorldPoint point, Player player, boolean reachCheck) {
			// 检查玩家是否在同一联盟
			if (!GuildService.getInstance().isPlayerInGuild(point.getGuildId(), player.getId())) {
				GuildManorService.logger.error("MANOR_COLLECT failed , pointGuildId:{},tarPlayerGuildId:{}", point.getGuildId(), player.getGuildId());
				return Status.Error.GUILD_NOT_MEMBER_VALUE;
			}
			//检查状态
			String guildBuildId = point.getGuildBuildId();
			IGuildBuilding building = GuildManorService.getInstance().getAllBuildings().get(guildBuildId);
			if(building != null && building instanceof GuildManorSuperMine){
				GuildManorSuperMine mine = (GuildManorSuperMine) building;
				if(!mine.getBuildStat().canEnter(GuildBuildingStat.COLLECT)){
					GuildManorService.logger.error("[onWorldManorMarch]MANOR_COLLECT failed, can`t enter stat");
					return Status.Error.MANOR_CAN_NOT_CREATE_VALUE;
				}
				//检查矿总量是否可以挖
				if(mine.getResourceNum() <= 0){
					GuildManorService.logger.error("[onWorldManorMarch]MANOR_COLLECT failed, ResourceNum : {}", mine.getResourceNum());
					return Status.Error.MANOR_MINE_EMPTY_VALUE;
				}
				//检查矿类型和大本等级
				if(mine.getResType() == PlayerAttr.TOMBARTHITE_VALUE){
					if(player.getCityLv() < WorldMapConstProperty.getInstance().getCanCollectTombarthiteLevel()){
						return Status.Error.CAN_NOT_COLLECT_RES_VALUE;
					}
				} else if(mine.getResType() == PlayerAttr.STEEL_VALUE){
					if(player.getCityLv() < WorldMapConstProperty.getInstance().getCanCollectSteelLevel()){
						return Status.Error.CAN_NOT_COLLECT_RES_VALUE;
					}
				}
				if(!reachCheck){//只有发起行军的时候检查, 到达不检查, 因为出发时已经将行军队列加入
					//检查超级矿中是否有自己的行军
					if(mine.checkHasMarch(player.getId())){
						return Status.Error.HAS_ALREADY_COLLECT_RES_VALUE;
					}
				}
				return Status.SysError.SUCCESS_OK_VALUE;
			} else {
				GuildManorService.logger.error("[onWorldManorMarch]world MANOR_COLLECT failed, building is null, point:{}", point);
				return Status.Error.WORLD_POINT_NOT_MANOR_VALUE;
			}	
		}
		
		@Override
		public void addMarch(IWorldMarch march, String id) {
			IGuildBuilding building = GuildManorService.getInstance().getAllBuildings().get(id);
			if(building != null && building instanceof GuildManorSuperMine){
				GuildManorSuperMine mine = (GuildManorSuperMine) building;
				//将行军存入矿点
				mine.addCollectMarch(march.getMarchId());
			}
		}

		@Override
		public boolean onMarchReach(WorldPoint point, IWorldMarch march) {
			//获取当前超级矿
			GuildManorSuperMine superMine = (GuildManorSuperMine) GuildManorService.getInstance().getAllBuildings().get(march.getMarchEntity().getTargetId());
			if(superMine == null){
				WorldMarchService.getInstance().onPlayerNoneAction(march, HawkTime.getMillisecond());
				GuildManorService.logger.error("[onWorldManorMarch]MANOR_COLLECT failed, superMine is null , buildId={}", march.getMarchEntity().getTargetId());
				return false;
			}
			int resType = superMine.getResType();
			// 资源数据初始化
			AwardItems items = AwardItems.valueOf();
			items.addNewItem(Const.ItemType.PLAYER_ATTR_VALUE, resType, 0);
			march.getMarchEntity().setAwardItems(items);
			//每次到达后重置开始时间
			march.getMarchEntity().setResStartTime(0);
			//此处是为了march不再走采集心跳
			march.getMarchEntity().setEndTime(Long.MAX_VALUE);
			// 采集速度
			double speed = WorldUtil.getCollectSpeed(march.getPlayer(), resType, 0, point, march.getMarchEntity().getEffectParams());
			if(speed == 0){
				speed = 1;
			}
			march.getMarchEntity().setCollectSpeed(speed);
			march.getMarchEntity().setCollectBaseSpeed(WorldUtil.getCollectBaseSpeed(march.getPlayer(), resType, march.getMarchEntity().getEffectParams()));
			
			//修改状态
			superMine.tryEnterState(GuildBuildingStat.COLLECT.getIndex());
			//广播状态变化
			WorldPointService.getInstance().notifyPointUpdate(point.getX(), point.getY());
			GuildManorService.getInstance().broadcastGuildBuilding(superMine.getGuildId());
			return true;
		}
	},
	/** 联盟领地集结收复 */
	MANOR_ASSISTANCE_MASS(WorldMarchType.MANOR_ASSISTANCE_MASS_VALUE) {
		@Override
		public int checkMarch(WorldPoint point, Player player, boolean reachCheck) {
			// 检查玩家是否在同一联盟
			if (!GuildService.getInstance().isPlayerInGuild(point.getGuildId(), player.getId())) {
				GuildManorService.logger.error("MANOR_ASSISTANCE_MASS failed , pointGuildId:{},tarPlayerGuildId:{}", point.getGuildId(), player.getGuildId());
				return Status.Error.GUILD_NOT_MEMBER_VALUE;
			}
			String guildBuildId = point.getGuildBuildId();
			GuildManorObj manorObj = GuildManorService.getInstance().getAllManors().get(guildBuildId);
			if(manorObj == null){
				GuildManorService.logger.error("[onWorldManorMarch]world build manor failed, manorObj is null, point:{}", point);
				return Status.Error.WORLD_POINT_NOT_MANOR_VALUE;
			}
			//收复最终变为驻防
			if(!manorObj.getBastionStat().canEnter(ManorBastionStat.GARRISON)){
				GuildManorService.logger.error("[onWorldManorMarch]world build manor failed, can`t MANOR_ASSISTANCE_MASS");
				return Status.Error.MANOR_CAN_NOT_CREATE_VALUE;
			}
			return Status.SysError.SUCCESS_OK_VALUE;
		}

		@Override
		public boolean onMarchReach(WorldPoint worldPoint, IWorldMarch march) {
			return selfManorMarchReach(worldPoint, march);
		}
	}, 
	/** 联盟领地集结收复 */
	MANOR_ASSISTANCE_MASS_JOIN(WorldMarchType.MANOR_ASSISTANCE_MASS_JOIN_VALUE) {
		@Override
		public int checkMarch(WorldPoint point, Player player, boolean reachCheck) {
			return Status.SysError.SUCCESS_OK_VALUE;
		}

		@Override
		public boolean onMarchReach(WorldPoint worldPoint, IWorldMarch march) {
			return selfManorMarchReach(worldPoint, march);
		}
	}, 
	/** 联盟领地单人驻军 */
	MANOR_ASSISTANCE(WorldMarchType.MANOR_ASSISTANCE_VALUE) {
		@Override
		public int checkMarch(WorldPoint point, Player player, boolean reachCheck) {
			// 检查玩家是否在同一联盟
			if (!GuildService.getInstance().isPlayerInGuild(point.getGuildId(), player.getId())) {
				GuildManorService.logger.error("MANOR_ASSISTANCE failed , pointGuildId:{},tarPlayerGuildId:{}", point.getGuildId(), player.getGuildId());
				return Status.Error.GUILD_NOT_MEMBER_VALUE;
			}
			String guildBuildId = point.getGuildBuildId();
			GuildManorObj manorObj = GuildManorService.getInstance().getAllManors().get(guildBuildId);
			if(manorObj == null){
				GuildManorService.logger.error("[onWorldManorMarch]world build manor failed, manorObj is null, point:{}", point);
				return Status.Error.WORLD_POINT_NOT_MANOR_VALUE;
			}
			if(!manorObj.getBastionStat().canEnter(ManorBastionStat.GARRISON)){
				GuildManorService.logger.error("[onWorldManorMarch]world build manor failed, can`t MANOR_ASSISTANCE");
				return Status.Error.MANOR_CAN_NOT_CREATE_VALUE;
			}
			return Status.SysError.SUCCESS_OK_VALUE;
		}

		@Override
		public boolean onMarchReach(WorldPoint worldPoint, IWorldMarch march) {
			return selfManorMarchReach(worldPoint, march);
		}
	}, 
	/** 建造联盟领地（包含建筑） */
	MANOR_BUILD(WorldMarchType.MANOR_BUILD_VALUE) {
		@Override
		public int checkMarch(WorldPoint point, Player player, boolean reachCheck) {
			// 检查玩家是否在同一联盟
			if (!GuildService.getInstance().isPlayerInGuild(point.getGuildId(), player.getId())) {
				GuildManorService.logger.error("MANOR_BUILD failed , pointGuildId:{},tarPlayerGuildId:{}", point.getGuildId(), player.getGuildId());
				return Status.Error.GUILD_NOT_MEMBER_VALUE;
			}
			AbstractBuildable building = GuildManorService.getInstance().getBuildable(point);
			if(building == null){
				GuildManorService.logger.error("[onWorldManorMarch]world build manor failed, point:{}", point);
				return Status.Error.WORLD_POINT_NOT_MANOR_VALUE;
			}
			//是否可以进入建造状态
			if(!building.canEnterBuildState()){
				GuildManorService.logger.error("[onWorldManorMarch]world build manor failed, can`t enter build stat");
				return Status.Error.MANOR_CAN_NOT_CREATE_VALUE;
			}
			//先检查目标是否可以派兵, 大本驻扎, 其他建筑返回
			if(building.isFullLife() && building.getBuildType() != TerritoryType.GUILD_BASTION){
				GuildManorService.logger.error("[onWorldManorMarch]world build manor failed, can`t isFullLife");
				return Status.Error.MANOR_CAN_NOT_BUILD_VALUE;
			}
			return Status.SysError.SUCCESS_OK_VALUE;
		}

		@Override
		public boolean onMarchReach(WorldPoint worldPoint, IWorldMarch march) {
			if (worldPoint == null) {
				GuildManorService.logger.error("[onWorldManorMarch] onMarchReach worldPoint is null");
				return false;
			}
			AbstractBuildable buildable = GuildManorService.getInstance().getBuildable(worldPoint);
			String guildId = buildable.getGuildId();
			
			//满血了直接走驻扎逻辑
			if(buildable.isFullLife() && buildable.getBuildType() == TerritoryType.GUILD_BASTION){
				return this.selfManorMarchReach(worldPoint, march);
			}
			//修改状态
			buildable.tryEnterBuildState();
			//修改行军状态
			march.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MANOR_BUILD_VALUE);
			//此处提前算一次速度,保证在速度不同的时候也进行一次广播
			double lastSpeed = buildable.getCurrentSpeed(false, buildable.getbuildLimtUp());//摧毁速度是负数
			//记录最近一次的建造速度
			if(buildable.getLastBuildSpeed() != lastSpeed){//如果本次摧毁速度有变化,则通知客户端行军
				buildable.setLastBuildSpeed(lastSpeed);
			}
			buildable.broadcastPointMarchUpate();
			//广播状态变化
			WorldPointService.getInstance().notifyPointUpdate(worldPoint.getX(), worldPoint.getY());
			//组织消息
			GuildManorList.Builder builder = GuildManorService.getInstance().makeManorListBuilder(guildId);
			//广播消息
			GuildService.getInstance().broadcastProtocol(guildId, HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_S_VALUE, builder));
			return true;
		}
	},
	/** 修复联盟领地 */
	MANOR_REPAIR(WorldMarchType.MANOR_REPAIR_VALUE) {
		@Override
		public int checkMarch(WorldPoint point, Player player, boolean reachCheck) {
			// 检查玩家是否在同一联盟
			if (!GuildService.getInstance().isPlayerInGuild(point.getGuildId(), player.getId())) {
				GuildManorService.logger.error("MANOR_REPAIR failed , pointGuildId:{},tarPlayerGuildId:{}", point.getGuildId(), player.getGuildId());
				return Status.Error.GUILD_NOT_MEMBER_VALUE;
			}
			String guildBuildId = point.getGuildBuildId();
			GuildManorObj manorObj = GuildManorService.getInstance().getAllManors().get(guildBuildId);
			if(manorObj == null){
				GuildManorService.logger.error("[onWorldManorMarch]world build manor failed, manorObj is null, point:{}", point);
				return Status.Error.WORLD_POINT_NOT_MANOR_VALUE;
			}
			if(!manorObj.getBastionStat().canEnter(ManorBastionStat.REPAIRING) && !manorObj.getBastionStat().canEnter(ManorBastionStat.BUILDING)){
				GuildManorService.logger.error("[onWorldManorMarch]world build manor failed, can`t REPAIR, stat : {}", manorObj.getBastionStat());
				return Status.Error.MANOR_CAN_NOT_CREATE_VALUE;
			}
			if(manorObj.isFullLife()){
				GuildManorService.logger.error("[onWorldManorMarch]world build manor failed, can`t REPAIR, life : {}", manorObj.isFullLife());
				return Status.Error.MANOR_CAN_NOT_BUILD_VALUE;
			}
			return Status.SysError.SUCCESS_OK_VALUE;
		}

		@Override
		public boolean onMarchReach(WorldPoint worldPoint, IWorldMarch march) {
			return selfManorMarchReach(worldPoint, march);
		}
	},
	
	/** 巨龙陷阱集结 */
	DRAGON_ATTACT_MASS(WorldMarchType.DRAGON_ATTACT_MASS_VALUE) {
		@Override
		public int checkMarch(WorldPoint point, Player player, boolean reachCheck) {
			// 检查玩家是否在同一联盟
			if (!GuildService.getInstance().isPlayerInGuild(point.getGuildId(), player.getId())) {
				GuildManorService.logger.error("DRAGON_ATTACT_MASS failed , pointGuildId:{},tarPlayerGuildId:{},playerId:{}", point.getGuildId(), player.getGuildId(),player.getId());
				return Status.Error.GUILD_NOT_MEMBER_VALUE;
			}
			List<IGuildBuilding> list = GuildManorService.getInstance().getGuildBuildByType(point.getGuildId(), TerritoryType.GUILD_DRAGON_TRAP);
			if(list.isEmpty()){
				return Status.Error.GUILD_DRAGON_ACTTACK_BUILD_UNLOCK_VALUE;
			}
			GuildDragonTrap trap = (GuildDragonTrap) list.get(0);
			if(trap.getPositionId() != point.getId()){
				GuildManorService.logger.error("DRAGON_ATTACT_MASS failed , pointGuildId:{},worldPointId：{},buildEntryPointId:{},playerId:{}", point.getGuildId(),point.getId(),trap.getPositionId(), player.getId());
				return Status.Error.GUILD_DRAGON_ACTTACK_BUILD_NO_POINT_VALUE;
			}
			if(trap.getBuildStat() != GuildBuildingStat.COMPELETE){
				GuildManorService.logger.error("DRAGON_ATTACT_MASS failed , trapBuild state err:{},{},{}", point.getGuildId(),trap.getBuildStat(),player.getId());
				return Status.Error.GUILD_DRAGON_ACTTACK_BUILD_UNLOCK_VALUE;
			}
			if(!trap.inFight()){
				GuildManorService.logger.error("DRAGON_ATTACT_MASS failed , trap not in fight err:{},{},", point.getGuildId(),player.getId());
				return Status.Error.GUILD_DRAGON_ACTTACK_CLOSED_VALUE;
			}
			long curTime = HawkTime.getMillisecond();
			long fightEndTime = trap.getFightEndTime();
			if(curTime > fightEndTime){
				GuildManorService.logger.error("DRAGON_ATTACT_MASS failed , trap not in fight err:{},{},", point.getGuildId(),player.getId());
				return Status.Error.GUILD_DRAGON_ACTTACK_OPEN_TIME_ERR_VALUE;
			}
			if(!reachCheck){
				//获取同类行军
				Set<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerTypeMarchs(player.getId(), WorldMarchType.DRAGON_ATTACT_MASS_VALUE);
				for (IWorldMarch march : marchs) {
					if (march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_RETURN_BACK_VALUE) {
						GuildManorService.logger.error("DRAGON_ATTACT_MASS failed , has march err:{},{},", point.getGuildId(),player.getId());
						return Status.Error.GUILD_DRAGON_MASS_MARCH_LIMIT_VALUE;
					}
				}
			}
			return Status.SysError.SUCCESS_OK_VALUE;
		}

		@Override
		public boolean onMarchReach(WorldPoint worldPoint, IWorldMarch march) {
			return true;
		}
	}, 
	
	/** 巨龙陷阱集结加入 */
	DRAGON_ATTACT_MASS_JOIN(WorldMarchType.DRAGON_ATTACT_MASS_JOIN_VALUE) {
		@Override
		public int checkMarch(WorldPoint point, Player player, boolean reachCheck) {
			return Status.SysError.SUCCESS_OK_VALUE;
		}

		@Override
		public boolean onMarchReach(WorldPoint worldPoint, IWorldMarch march) {
			return true;
		}
	},
	;
	
	private ManorMarchEnum(int index) {
		this.index = index;
	}

	private final int index;
	
	@Override
	public int getIndex() {
		return index;
	}
	
	/**
	 * 检查是否可以行军
	 * @param point
	 * @param player
	 * @return 错误号
	 */
	public abstract int checkMarch(WorldPoint point, Player player, boolean reachCheck);
	
	/**
	 * 处理行军到达
	 * @param point
	 * @param player
	 * @return
	 */
	public abstract boolean onMarchReach(WorldPoint point, IWorldMarch march);

	/**
	 * 攻击检查
	 * @param point
	 * @param player
	 * @return
	 */
	public int attackCheck(WorldPoint point, Player player){
		String guildBuildId = point.getGuildBuildId();
		if(guildBuildId == null){
			GuildManorService.logger.error("[onWorldManorMarch]world build manor failed, guildBuildId is null, point:{}", point);
			return Status.Error.WORLD_POINT_NOT_MANOR_VALUE;
		}
		GuildManorObj manorObj = GuildManorService.getInstance().getAllManors().get(guildBuildId);
		//目标不是联盟堡垒
		if(manorObj == null){
			GuildManorService.logger.error("[onWorldManorMarch]world build manor failed, manorObj is null, point:{}", point);
			return Status.Error.WORLD_POINT_NOT_MANOR_VALUE;
		}
		//目标和自己同盟
		if (point.getGuildId().equals(player.getGuildId())) {
			GuildManorService.logger.error("world attack manor failed, in same guild playerGuildId:{}", player.getGuildId());
			return Status.Error.WORLD_POINT_TYPE_ERROR_VALUE;
		}
		//目标状态不对, 或者已经没血
		if((!manorObj.getBastionStat().canEnter(ManorBastionStat.BREAKING) && !manorObj.getBastionStat().canEnter(ManorBastionStat.UN_BREAKING)) || manorObj.getEntity().getBuildingLife() <= 0){
			GuildManorService.logger.error("[onWorldManorMarch]world attackCheck failed, can`t BREAKING");
			return Status.Error.MANOR_CAN_NOT_CREATE_VALUE;
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 通用敌方到达
	 * @param worldPoint
	 * @return
	 */
	public boolean enemyManorMarchReach(WorldPoint worldPoint, IWorldMarch march, GuildAction action){
		if (worldPoint == null) {
			GuildManorService.logger.error("[onWorldManorMarch] onMarchReach worldPoint is null");
			return false;
		}
		String guildBuildId = worldPoint.getGuildBuildId();
		GuildManorObj manorObj = GuildManorService.getInstance().getAllManors().get(guildBuildId);
		if(manorObj == null){
			GuildManorService.logger.error("[onWorldManorMarch]world build manor failed, manorObj is null, point:{}", worldPoint);
			return false;
		}
		String guildId = manorObj.getGuildId();
		if(manorObj.isComplete()){
			//修改状态
			manorObj.tryEnterState(ManorBastionStat.BREAKING.getIndex());
		} else {
			manorObj.tryEnterState(ManorBastionStat.UN_BREAKING.getIndex());
		}
		//修改行军状态
		march.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MANOR_BREAK_VALUE);
		//此处提前算一次速度,保证在联盟战争中数据是正确的
		double lastSpeed = -manorObj.getCurrentSpeed(true, manorObj.getManorCfg().getBuildingUpLimit());//摧毁速度是负数
		//记录最近一次的摧毁速度
		if(manorObj.getLastBuildSpeed() != lastSpeed){//如果本次摧毁速度有变化,则通知客户端行军
			manorObj.setLastBuildSpeed(lastSpeed);
		}
		manorObj.broadcastPointMarchUpate();
		//广播状态变化
		WorldPointService.getInstance().notifyPointUpdate(worldPoint.getX(), worldPoint.getY());
		//组织消息
		GuildManorList.Builder builder = GuildManorService.getInstance().makeManorListBuilder(guildId);
		//广播消息
		GuildService.getInstance().broadcastProtocol(guildId, HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_S_VALUE, builder));
		
		if (action != null) {
			LogUtil.logGuildAction(action, guildId);
		}
		return true;
	}
	
	/**
	 * 通用友方到达
	 * @param worldPoint
	 * @return
	 */
	public boolean selfManorMarchReach(WorldPoint worldPoint, IWorldMarch march){
		if (worldPoint == null) {
			GuildManorService.logger.error("[onWorldManorMarch] onMarchReach worldPoint is null");
			return false;
		}
		String guildBuildId = worldPoint.getGuildBuildId();
		GuildManorObj manorObj = GuildManorService.getInstance().getAllManors().get(guildBuildId);
		if(manorObj == null){
			GuildManorService.logger.error("[onWorldManorMarch]world build manor failed, manorObj is null, point:{}", worldPoint);
			return false;
		}
		String guildId = manorObj.getGuildId();
		if(manorObj.isFullLife()){
			//修改状态
			manorObj.tryEnterState(ManorBastionStat.GARRISON.getIndex());
			//修改行军状态
			march.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MANOR_GARRASION_VALUE);
		} else {
			if(manorObj.isComplete()){
				//修改状态
				manorObj.tryEnterState(ManorBastionStat.REPAIRING.getIndex());
			} else {
				manorObj.tryEnterState(ManorBastionStat.BUILDING.getIndex());
			}
			//修改行军状态
			march.getMarchEntity().setMarchStatus(WorldMarchStatus.MARCH_STATUS_MANOR_REPAIR_VALUE);
			//此处提前算一次速度,保证在联盟战争中数据是正确的
			double lastSpeed = manorObj.getCurrentSpeed(false, manorObj.getManorCfg().getBuildingUpLimit());//摧毁速度是负数
			//记录最近一次的摧毁速度
			if(manorObj.getLastBuildSpeed() != lastSpeed){//如果本次摧毁速度有变化,则通知客户端行军
				manorObj.setLastBuildSpeed(lastSpeed);
			}
			manorObj.broadcastPointMarchUpate();
		}
		//广播状态变化
		WorldPointService.getInstance().notifyPointUpdate(worldPoint.getX(), worldPoint.getY());
		//组织消息
		GuildManorList.Builder builder = GuildManorService.getInstance().makeManorListBuilder(guildId);
		//广播消息
		GuildService.getInstance().broadcastProtocol(guildId, HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_S_VALUE, builder));
		return true;
	}
	
	/**
	 * 发起行军时添加行军， 需要的继承覆盖
	 * @param march
	 */
	public void addMarch(IWorldMarch march, String id){
	}
	
	private static final List<ManorMarchEnum> values = IndexedEnumUtil.toIndexes(ManorMarchEnum.values());

	public static ManorMarchEnum valueOf(int index) {
		return EnumUtil.valueOf(values, index);
	}
}
