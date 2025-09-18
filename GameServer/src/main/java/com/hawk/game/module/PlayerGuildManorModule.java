package com.hawk.game.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

import com.hawk.game.global.LocalRedis;
import com.hawk.game.protocol.GuildManager;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.alibaba.fastjson.JSONObject;
import com.hawk.common.IDIPBanInfo;
import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.crossfortress.CrossFortressService;
import com.hawk.game.crossfortress.IFortress;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.manor.AbstractBuildable;
import com.hawk.game.guild.manor.GuildBuildingStat;
import com.hawk.game.guild.manor.GuildManorObj;
import com.hawk.game.guild.manor.ManorBastionStat;
import com.hawk.game.guild.manor.building.GuildManorSuperMine;
import com.hawk.game.guild.manor.building.GuildManorTower;
import com.hawk.game.guild.manor.building.IGuildBuilding;
import com.hawk.game.guild.manor.building.model.TowerDamageInfo;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.lianmengxzq.XZQService;
import com.hawk.game.lianmengxzq.worldpoint.XZQWorldPoint;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.module.spacemecha.worldpoint.SpaceWorldPoint;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.president.PresidentCity;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.president.PresidentTower;
import com.hawk.game.protocol.Common.KeyValuePairStr;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.GuildManor.ClearResourcePoint;
import com.hawk.game.protocol.GuildManor.CreateGuildManor;
import com.hawk.game.protocol.GuildManor.CreateGuildManorBuilding;
import com.hawk.game.protocol.GuildManor.GetManorPlayerInfoList;
import com.hawk.game.protocol.GuildManor.GuildBuildingNorStat;
import com.hawk.game.protocol.GuildManor.GuildClearResouceNum;
import com.hawk.game.protocol.GuildManor.GuildManorList;
import com.hawk.game.protocol.GuildManor.GuildManorStat;
import com.hawk.game.protocol.GuildManor.ManorPlayerInfo;
import com.hawk.game.protocol.GuildManor.ManorPlayerInfoList;
import com.hawk.game.protocol.GuildManor.TakeBackManorBuilding;
import com.hawk.game.protocol.GuildManor.changeManorName;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.protocol.Status.IDIPErrorCode;
import com.hawk.game.protocol.Status.SysError;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Player.MsgCategory;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.AppointedCaptain;
import com.hawk.game.protocol.World.RepatriateManorMarch;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.WarFlagService;
import com.hawk.game.superweapon.SuperWeaponService;
import com.hawk.game.superweapon.weapon.IWeapon;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GuildUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.ChangeContentType;
import com.hawk.game.util.GsConst.GlobalControlType;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.service.WorldResourceService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.log.Action;
import com.hawk.log.LogConst.GuildAction;
import com.hawk.log.Source;

/**
 * 领地模块管理器
 * @author zhenyu.shang
 * @since 2017年7月6日
 */
public class PlayerGuildManorModule extends PlayerModule {

	public PlayerGuildManorModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		if (!player.hasGuild()) {
			return true;
		}
		List<GuildManorObj> manors = GuildManorService.getInstance().getGuildManors(player.getGuildId());
		if (manors == null) {
			return true;
		}
		// 同步领地列表
		GuildManorList.Builder builder = GuildManorService.getInstance().makeManorListBuilder(player.getGuildId());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_S_VALUE, builder));
		return true;
	}
	
	/**
	 * 获取联盟领地列表
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_MANOR_LIST_C_VALUE)
	private boolean onGetGuildManorList(HawkProtocol protocol) {
		//判断玩家联盟
		String guildId = player.getGuildId();
		if(guildId == null){
			sendError(protocol.getType(), Status.Error.GUILD_PLAYER_HASNOT_GUILD);
			return false;
		}
		//检查当前联盟领地是否达到开启条件，如果达到则修改状态，并发送通知邮件
		List<GuildManorObj> manors = GuildManorService.getInstance().getGuildManors(guildId);
		if(manors != null){
			for (GuildManorObj guildManor : manors) {
				//判断联盟领地是否达到开启条件
				if(guildManor.getEntity().getManorState() == GuildManorStat.LOCKED_M_VALUE){
					guildManor.checkUnlockAndChangeStat();
				}
			}
			//组织消息
			GuildManorList.Builder builder = GuildManorService.getInstance().makeManorListBuilder(guildId);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_S_VALUE, builder));
		} else {
			GuildManorService.logger.warn("guild has not init Manor, guildId=" + guildId);
		}
		
		// 检测联盟旗帜数量
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.CHECK_WAR_FLAG_COUNT) {
			@Override
			public boolean onInvoke() {
				WarFlagService.getInstance().checkWarFlagCount(player.getGuildId());
				return false;
			}
		});
		return true;
	}
	
	/**
	 * 放置联盟堡垒
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.CREATE_GUILD_TOWER_C_VALUE)
	private boolean onCreateGuildTower(HawkProtocol protocol) {
		CreateGuildManor req = protocol.parseProtocol(CreateGuildManor.getDefaultInstance());
		//判断权限
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		int index = req.getManorIdx();
		int x = req.getX();
		int y = req.getY();
		String guildId = player.getGuildId();
		GuildManorObj manor = GuildManorService.getInstance().getManorByIdx(guildId, index);
		//判断状态
		if(manor.getBastionStat() != ManorBastionStat.OPENED){
			sendError(protocol.getType(), Status.Error.MANOR_CAN_NOT_CREATE_VALUE);
			return false;
		}
		//判断建筑是否可以落地
		if(!GuildManorService.getInstance().checkGuildBuildCanBuild(guildId, TerritoryType.GUILD_BASTION, GameUtil.combineXAndY(x, y), index)){
			sendError(protocol.getType(), Status.Error.CREATE_MANOR_FAILED);
			return false;
		}
		//建造点的时候投递到世界线程
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.CREATE_GUILD_MANOR) {
			@Override
			public boolean onInvoke() {
				//再次判断状态, 放置多次建造并发
				if(manor.getBastionStat() != ManorBastionStat.OPENED){
					sendError(protocol.getType(), Status.Error.MANOR_CAN_NOT_CREATE_VALUE);
					return false;
				}
				
				if (WarFlagService.getInstance().isManorRangeHasWarFlag(GameUtil.combineXAndY(x, y))) {
					sendError(protocol.getType(), Status.WarFlagError.PLACE_MANOR_RANGE_HAS_FLAG_VALUE);
					return false;
				}
				
				//开始建造
				WorldPoint point = GuildManorService.getInstance().genGuildBuildInWorld(guildId, manor.getEntity().getManorId(), TerritoryType.GUILD_BASTION, GameUtil.combineXAndY(x, y), index);
				if(point == null){
					sendError(protocol.getType(), Status.Error.CREATE_MANOR_FAILED);
					return false;
				}
				//修改状态
				manor.tryEnterState(GuildManorStat.UNCOMPELETE_M_VALUE);
				//设置初始值
				manor.resetBuildLife();
				//记录坐标点
				manor.getEntity().setPos(x + "," + y);
				//记录放置时间
				manor.getEntity().setPlaceTime(HawkTime.getMillisecond());
				//放置时需要清空上次tick时间
				manor.getEntity().setLastTickTime(0);
				//通知状态
				WorldPointService.getInstance().notifyPointUpdate(point.getX(), point.getY());
				//推送变化消息
				GuildManorList.Builder builder = GuildManorList.newBuilder();
				//领地哨塔列表
				GuildManorService.getInstance().makeManorBastion(builder, guildId);
				//广播消息
				GuildService.getInstance().broadcastProtocol(guildId, HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_S_VALUE, builder));
				// 行为日志
				BehaviorLogger.log4Service(player, Source.GUILD_MANOR, Action.CREATE_MANOR_BASTION, 
						Params.valueOf("guildId", guildId),
						Params.valueOf("index", index),
						Params.valueOf("pos", x + "," + y),
						Params.valueOf("lastTakebackTime", manor.getEntity().getLastTakeBackTime()));
				
				// 记录打点日志
				JSONObject changeInfo = new JSONObject();
				changeInfo.put("buildType", TerritoryType.GUILD_BASTION.getNumber());
				changeInfo.put("buildNo", index);
				changeInfo.put("posX", x);
				changeInfo.put("posY", y);
				changeInfo.put("manorName", manor.getEntity().getManorName());
				LogUtil.logGuildDetail(player, guildId, changeInfo.toJSONString(), GuildAction.GUILD_ACTION_23.intVal());
				
				player.responseSuccess(HP.code.CREATE_GUILD_TOWER_C_VALUE);
				return true;
			}
		});
		
		return true;
	}
	
	/**
	 * 放置联盟其他建筑 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.CREATE_GUILD_BUILDING_C_VALUE)
	private boolean onCreateGuildBuilding(HawkProtocol protocol) {
		CreateGuildManorBuilding req = protocol.parseProtocol(CreateGuildManorBuilding.getDefaultInstance());
		//判断权限
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		String guildId = player.getGuildId();
		int idx = req.getManorIdx();
		TerritoryType type = req.getType();
		IGuildBuilding building = GuildManorService.getInstance().getBuildingByTypeAndIdx(guildId, idx, type);
		//判断状态
		if(building.getBuildStat() != GuildBuildingStat.OPENED){
			sendError(protocol.getType(), Status.Error.MANOR_CAN_NOT_CREATE_VALUE);
			return false;
		}
		final int pointId = GameUtil.combineXAndY(req.getX(), req.getY());
		//判断建筑是否可以落地
		if(!GuildManorService.getInstance().checkGuildBuildCanBuild(guildId, type, pointId, idx)){
			sendError(protocol.getType(), Status.Error.CREATE_MANOR_FAILED);
			return false;
		}
		//建造点的时候投递到世界线程
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.CREATE_GUILD_BUILD) {
			@Override
			public boolean onInvoke() {
				//此处需要再次判断状态, 防止多次建造并发
				if(building.getBuildStat() != GuildBuildingStat.OPENED){
					sendError(protocol.getType(), Status.Error.MANOR_CAN_NOT_CREATE_VALUE);
					return false;
				}
				//开始建造
				WorldPoint point = GuildManorService.getInstance().genGuildBuildInWorld(guildId, building.getEntity().getId(), type, pointId, idx);
				if(point == null){
					sendError(protocol.getType(), Status.Error.CREATE_MANOR_FAILED);
					return false;
				}
				//判断如果是联盟矿,则只能4选1. 将其他的置为锁定状态
				if(building.getBuildType() == TerritoryType.GUILD_MINE){
					//将其他联盟矿置为不可建造状态
					List<IGuildBuilding> buildings = GuildManorService.getInstance().getGuildBuildings(guildId);
					for (IGuildBuilding iGuildBuilding : buildings) {
						if(iGuildBuilding.getBuildType() == TerritoryType.GUILD_MINE && iGuildBuilding.getEntity().getBuildingId() != idx){
							iGuildBuilding.tryChangeBuildStat(GuildBuildingStat.LOCKED.getIndex());
						}
					}
				}
				//修改状态
				building.tryChangeBuildStat(GuildBuildingNorStat.UNCOMPELETE_N_VALUE);
				//记录坐标点
				building.getEntity().setPos(req.getX() + "," + req.getY());
				//放置时需要清空上次tick时间
				building.getEntity().setLastTickTime(0);
				//通知状态
				WorldPointService.getInstance().notifyPointUpdate(point.getX(), point.getY());
				//推送变化消息
				GuildManorList.Builder builder = GuildManorList.newBuilder();
				//领地哨塔列表
				GuildManorService.getInstance().makeManorBuilding(builder, guildId);
				//广播消息
				GuildService.getInstance().broadcastProtocol(guildId, HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_S_VALUE, builder));
				// 行为日志
				BehaviorLogger.log4Service(player, Source.GUILD_MANOR, Action.CREATE_MANOR_BUILDING, 
						Params.valueOf("guildId", guildId),
						Params.valueOf("type", type),
						Params.valueOf("index", idx),
						Params.valueOf("pos", req.getX() + "," + req.getY()),
						Params.valueOf("lastTakebackTime", building.getEntity().getLastTakeBackTime()));
				
				player.responseSuccess(HP.code.CREATE_GUILD_BUILDING_C_VALUE);
				
				JSONObject changeInfo = new JSONObject();
				changeInfo.put("buildType", type.getNumber());
				changeInfo.put("buildNo", idx);
				changeInfo.put("posX", req.getX());
				changeInfo.put("posY", req.getY());
				LogUtil.logGuildDetail(player, guildId, changeInfo.toJSONString(), GuildAction.GUILD_ACTION_23.intVal());
				if(type == TerritoryType.GUILD_MINE){
					try {
						GuildManager.HPGuildLog.Builder log = GuildManager.HPGuildLog.newBuilder();
						log.setLogType(GuildManager.GuildLogType.BUILD_RES);
						log.setParam(player.getName());
						log.addParams(player.getName());
						log.setTime(HawkTime.getMillisecond());
						LocalRedis.getInstance().addGuildLog(guildId, log);
					}catch (Exception e){
						HawkException.catchException(e);
					}
				}
				return false;
			}
			
		});
		
		return true;
	}
	
	/**
	 * 修改领地名称
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.CHANGE_MANOR_NAMES_C_VALUE)
	private boolean onChangeManorName(HawkProtocol protocol) {
		// 禁止输入文本
		if (!GameUtil.checkBanMsg(player)) {
			return false;
		}
				
		changeManorName req = protocol.parseProtocol(changeManorName.getDefaultInstance());
		//判断玩家联盟
		String guildId = player.getGuildId();
		
		//判断权限
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		
		// 禁止全服修改联盟堡垒名称
		if (GlobalData.getInstance().isGlobalBan(GlobalControlType.CHANGE_GUILD_MANOR_NAME)) {
			String reason = GlobalData.getInstance().getGlobalBanReason(GlobalControlType.CHANGE_GUILD_MANOR_NAME);
			if (HawkOSOperator.isEmptyString(reason)) {
				sendError(protocol.getType(), SysError.GLOBAL_BAN_CHANGE_MANOR_NAME);
			} else {
				player.sendIdipNotice(NoticeType.BAN_MSG, NoticeMode.NOTICE_MSG, 0, reason);
			}
			return false;
		}
		
		// 修改CD时长检测
		int checkResult = GameUtil.changeContentCDCheck(guildId, ChangeContentType.CHANGE_GUILD_MANOR_NAME);
		if (checkResult < 0) {
			sendError(protocol.getType(), IDIPErrorCode.CHANGE_MANOR_NAME_CD_ING);
			return false;
		}
		
		List<KeyValuePairStr> list = req.getNamesList();
		if(list == null){
			GuildManorService.logger.error("[onChangeManorName] name list is null");
			return false;
		}
		
		IDIPBanInfo banInfo = RedisProxy.getInstance().getIDIPBanInfo(player.getId(), IDIPBanType.BAN_GUILD_MANOR);
		if (banInfo != null && banInfo.getEndTime() >  HawkTime.getMillisecond()) {
			player.sendIdipNotice(NoticeType.SEND_MSG, NoticeMode.NOTICE_MSG, 0, banInfo.getBanMsg());
			return false;
		}
		
		JSONObject callback = new JSONObject();
		StringBuilder sb = new StringBuilder();
		StringJoiner sj = new StringJoiner(",");
		//逐个修改大本名称
		for (KeyValuePairStr keyValuePairStr : list) {
			String name = keyValuePairStr.getVal();
			int op = GuildUtil.checkGuildManorName(name);
			if (op != Status.SysError.SUCCESS_OK_VALUE) {
				sendError(protocol.getType(), op);
				return false;
			}
			int manorIndex = keyValuePairStr.getKey();
			GuildManorObj manor = GuildManorService.getInstance().getManorByIdx(guildId, manorIndex);
			//未完成建造的堡垒不可改名
			if(manor == null || manor.getBastionStat().getIndex() <= ManorBastionStat.UNCOMPELETE.getIndex()){
				HawkLog.logPrintln("guild manor change name bastionStat error, playerId: {}, guildId: {}, index: {}, state: {}", 
						player.getId(), guildId, manorIndex, manor == null ? 0 : manor.getBastionStat().getIndex());
				sendError(protocol.getType(), Status.Error.MANOR_CANNOT_CHANGE_NAME_VALUE);
				return false;
			}
			
			sb.append("idx:" + manorIndex).append(",").append("name:" + name).append("|");
			String idx = String.valueOf(manorIndex);
			sj.add(idx);
			callback.put(idx, name);
		}
		
		String content = sb.toString();
		callback.put("guildId", guildId);
		callback.put("checkResult", checkResult);
		JSONObject gameData = new JSONObject();
		gameData.put("msg_type", 0);
		gameData.put("post_id", 0);
		gameData.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
		gameData.put("param_id", sj.toString());
		GameTssService.getInstance().wordUicChatFilter(player, content, 
				MsgCategory.GUILD_MANOR_NAME.getNumber(), GameMsgCategory.CHANGE_GUILD_MANOR_NAME, 
				callback.toJSONString(), gameData, protocol.getType());
		return true;
	}
	
	/**
	 * 主动移除领地建筑，包括大本
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.TAKE_BACK_MANOR_BUILDING_C_VALUE)
	private boolean onTakeBackManorBuilding(HawkProtocol protocol) {
		TakeBackManorBuilding req = protocol.parseProtocol(TakeBackManorBuilding.getDefaultInstance());
		//判断权限
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		//投递世界线程执行
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.REMOVE_GUILD_BUILD) {
			@Override
			public boolean onInvoke() {
				//玩家联盟
				String guildId = player.getGuildId();
				int idx = req.getManorIdx();
				TerritoryType type = req.getType();
				int x = 0,y = 0;
				switch (type) {
					case GUILD_BASTION:
						GuildManorObj obj = GuildManorService.getInstance().getManorByIdx(guildId, idx);
						x = obj.getEntity().getPosX();
						y = obj.getEntity().getPosY();
						//先调用地图点移除
						GuildManorService.getInstance().rmGuildManor(guildId, obj.getEntity().getManorId());
						//再修改领地本身的影响
						obj.onMonorRemove();
						break;
					case GUILD_BARTIZAN:
					case GUILD_MINE:
					case GUILD_STOREHOUSE:
					case GUILD_DRAGON_TRAP:
						IGuildBuilding building = GuildManorService.getInstance().getBuildingByTypeAndIdx(guildId, idx, type);
						x = building.getEntity().getPosX();
						y = building.getEntity().getPosY();
						//先从地图中移除建筑
						GuildManorService.getInstance().removeManorBuilding(building);
						break;
					default:
						break;
				}
				//通知状态
				WorldPointService.getInstance().notifyPointUpdate(x, y);
				//组织消息
				GuildManorList.Builder builder = GuildManorService.getInstance().makeManorListBuilder(guildId);
				//广播消息
				GuildService.getInstance().broadcastProtocol(guildId, HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_S_VALUE, builder));
				// 行为日志
				BehaviorLogger.log4Service(player, Source.GUILD_MANOR, Action.REMOVE_MANOR_BUILDING, 
						Params.valueOf("guildId", guildId),
						Params.valueOf("type", type),
						Params.valueOf("index", idx),
						Params.valueOf("pos", x + "," + y));
				player.responseSuccess(HP.code.TAKE_BACK_MANOR_BUILDING_C_VALUE);
				
				// 记录打点日志
				JSONObject changeInfo = new JSONObject();
				changeInfo.put("buildType", type.getNumber());
				changeInfo.put("buildNo", idx);
				changeInfo.put("posX", x);
				changeInfo.put("posY", y);
				LogUtil.logGuildDetail(player, guildId, changeInfo.toJSONString(), GuildAction.GUILD_ACTION_24.intVal());
				if(type == TerritoryType.GUILD_MINE){
					try {
						GuildManager.HPGuildLog.Builder log = GuildManager.HPGuildLog.newBuilder();
						log.setLogType(GuildManager.GuildLogType.REMOVE_RES);
						log.setParam(player.getName());
						log.addParams(player.getName());
						log.setTime(HawkTime.getMillisecond());
						LocalRedis.getInstance().addGuildLog(guildId, log);
					}catch (Exception e){
						HawkException.catchException(e);
					}
				}
				return true;
			}
		});
		return true;
	}
	
	/**
	 * 获取单个箭塔伤害列表
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.MANOR_TOWER_DAMAGE_LIST_C_VALUE)
	private boolean onGetManorTowerDamageList(HawkProtocol protocol) {
		GetManorPlayerInfoList req = protocol.parseProtocol(GetManorPlayerInfoList.getDefaultInstance());
		String tarGuildId = req.getGuildId();
		//玩家联盟
		String guildId = player.getGuildId();
		if(guildId == null || !guildId.equals(tarGuildId)){
			return false;
		}
		int index = req.getManorIdx();
		TerritoryType t = req.getType();
		//根据序列获取箭塔
		GuildManorTower tower = GuildManorService.getInstance().getBuildingByTypeAndIdx(guildId, index, t);
		if(tower == null){
			GuildManorService.logger.error("[onGetManorTowerDamageList] tower is null, guildid:{}, index:{}", guildId, index);
			return false;
		}
		ManorPlayerInfoList.Builder builder = ManorPlayerInfoList.newBuilder();
		builder.setBuildLife((int) tower.getEntity().getBuildLife());
		builder.setLevel(tower.getEntity().getLevel());
		List<ManorPlayerInfo> list = new ArrayList<ManorPlayerInfo>();
		for (TowerDamageInfo info : tower.getDamageInfos().values()) {
			list.add(info.changeMsgInfo());
		}
		//倒叙展示
		Collections.reverse(list);
		builder.addAllInfos(list);
		//发送消息
		player.sendProtocol(HawkProtocol.valueOf(HP.code.MANOR_TOWER_DAMAGE_LIST_S, builder));
		return true;
	}
	
	/**
	 * 获取单个超级矿列表
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.MANOR_MINE_COLLECT_LIST_C_VALUE)
	private boolean onGetManorMineList(HawkProtocol protocol) {
		GetManorPlayerInfoList req = protocol.parseProtocol(GetManorPlayerInfoList.getDefaultInstance());
		String tarGuildId = req.getGuildId();
		//玩家联盟
		String guildId = player.getGuildId();
		if(guildId == null || !guildId.equals(tarGuildId)){
			return false;
		}
		int index = req.getManorIdx();
		TerritoryType t = req.getType();
		//根据序列获取超级矿
		GuildManorSuperMine mine = GuildManorService.getInstance().getBuildingByTypeAndIdx(guildId, index, t);
		if(mine == null){
			GuildManorService.logger.error("[onGetManorMineList] mine is null, guildid:{}, index:{}", guildId, index);
			return false;
		}
		//发送消息
		player.sendProtocol(HawkProtocol.valueOf(HP.code.MANOR_MINE_COLLECT_LIST_S_VALUE, mine.makeSuperMineBuilder(player)));
		return true;
	}
	
	/**
	 * 获取驻军列表，包括建设
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.MANOR_GARRSION_LIST_C_VALUE)
	private boolean onGetManorGarrsionList(HawkProtocol protocol) {
		GetManorPlayerInfoList req = protocol.parseProtocol(GetManorPlayerInfoList.getDefaultInstance());
		//玩家联盟
		String guildId = player.getGuildId();
		String tarGuildId = req.getGuildId();
		int index = req.getManorIdx();
		TerritoryType t = req.getType();
		
		//获取建造实体
		AbstractBuildable buildable = GuildManorService.getInstance().getBuildable(tarGuildId, t, index);
		if(buildable == null){
			GuildManorService.logger.error("[onGetManorGarrsionList] manor is null, guildid:{}, index:{}", tarGuildId, index);
			return false;
		}
		
		ManorPlayerInfoList.Builder builder = ManorPlayerInfoList.newBuilder();
		builder.setBuildLife((int) buildable.getbuildLife());
		builder.setLevel(buildable.getLevel());
		builder.setShowList(false);
		//堡垒只有所有者和占领者的联盟人员可见
		if(t == TerritoryType.GUILD_BASTION){
			IWorldMarch march = buildable.getMarchLeader();
			if(march == null){ //没有驻军, 则只能本公会的人见
				if(guildId != null && guildId.equals(tarGuildId)){
					builder.setBuildLife((int) buildable.getbuildLife());
					builder.setLevel(buildable.getLevel());
					builder.setOverTime(buildable.getOverTime());
					builder.setShowList(true);
					buildable.makeUIProtocol(builder);
				}
			} else {
				String marchGuildId = GuildService.getInstance().getPlayerGuildId(march.getMarchEntity().getPlayerId());
				if((guildId == null && player.getId().equals(march.getPlayerId())) ||
						(guildId != null && (guildId.equals(marchGuildId) || guildId.equals(tarGuildId)))){
					builder.setBuildLife((int) buildable.getbuildLife());
					builder.setLevel(buildable.getLevel());
					builder.setOverTime(buildable.getOverTime());
					builder.setShowList(true);
					buildable.makeUIProtocol(builder);
				}
			}
		} else {
			//其他建筑只能本联盟人才可见
			if(guildId != null && guildId.equals(tarGuildId)){
				builder.setBuildLife((int) buildable.getbuildLife());
				builder.setLevel(buildable.getLevel());
				builder.setOverTime(buildable.getOverTime());
				builder.setShowList(true);
				buildable.makeUIProtocol(builder);
			}
		}
		
		//发送消息
		player.sendProtocol(HawkProtocol.valueOf(HP.code.MANOR_GARRSION_LIST_S_VALUE, builder));
		return true;
	}
	
	
	/**
	 * 领地内资源点清理
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.CLEAR_MANOR_RESOURCE_POINT_C_VALUE)
	private boolean onClearResourcePoint(HawkProtocol protocol) {
		ClearResourcePoint req = protocol.parseProtocol(ClearResourcePoint.getDefaultInstance());
		int x = req.getX();
		int y = req.getY();
		String guildId = player.getGuildId();
		if(guildId == null){
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		//判断权限
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.CLEARRESOUCE)) {
			sendError(protocol.getType(), Status.Error.GUILD_LOW_AUTHORITY);
			return false;
		}
		//判断点是否存在, 且是资源
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(x, y);
		if(worldPoint == null || worldPoint.getPointType() != WorldPointType.RESOURCE_VALUE){
			GuildManorService.logger.error("[onClearResourcePoint] world point is not resource, type:{}, x:{}, y:{}", worldPoint == null ? 0 : worldPoint.getPointType(), x, y);
			return false;
		}
		//判断联盟领地是否已经建成，且目标点在联盟领地内
		if(!GuildManorService.getInstance().isInGuild(guildId, GameUtil.combineXAndY(x, y))){
			GuildManorService.logger.error("[onClearResourcePoint] world point is not in guildManor, x:{}, y:{}", x, y);
			return false;
		}
		// 判断资源矿是否正在被采集, 资源点有玩家占领 && 有行军存在
		if (!HawkOSOperator.isEmptyString(worldPoint.getPlayerId()) && !HawkOSOperator.isEmptyString(worldPoint.getMarchId())) {
			sendError(protocol.getType(), Status.Error.GUILD_RES_HAS_BEEN_COLLCET_VALUE);
			return false;
		}
		//判断联盟次数是否已经够了
		GuildInfoObject info = GuildService.getInstance().getGuildInfoObject(guildId);
		if(info == null || info.getEntity().getClearResNum() >= GuildConstProperty.getInstance().getClearMaxNum()){
			sendError(protocol.getType(), Status.Error.GUILD_CLEAR_MAX_NUM_VALUE);
			return false;
		}
		//消耗道具
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(GuildConstProperty.getInstance().getClearResConsume(), false);
		if (!consume.checkConsume(player, HP.code.CLEAR_MANOR_RESOURCE_POINT_C_VALUE)) {
			return false;
		}
		consume.consumeAndPush(player, Action.CLEAR_MANOR_RESOURCE_POINT);
		//投递到世界线程进行刷点操作
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.CLEAR_RESOURCE) {
			@Override
			public boolean onInvoke() {
				//此处再检查一次点是否存在, 防止并发
				if(worldPoint == null || worldPoint.isInvalid() || worldPoint.getPointType() != WorldPointType.RESOURCE_VALUE){
					GuildManorService.logger.error("[onClearResourcePoint] world point is not resource, type:{}, x:{}, y:{}", worldPoint, x, y);
					return false;
				}
				//添加次数
				info.getEntity().setClearResNum(info.getEntity().getClearResNum() + 1);
				//删除老点
				WorldResourceService.getInstance().removeResourcePoint(worldPoint, true);
				player.responseSuccess(HP.code.CLEAR_MANOR_RESOURCE_POINT_C_VALUE);
				
				return true;
			}
		});
		return true;
	}
	
	/**
	 * 领地内资源点清理
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.CLEAR_MANOR_RESOURCE_LEFT_NUM_C_VALUE)
	private boolean onGetClearResourcePointLeft(HawkProtocol protocol) {
		String guildId = player.getGuildId();
		if(guildId == null){
			return false;
		}
		//判断联盟次数是否已经够了
		GuildInfoObject info = GuildService.getInstance().getGuildInfoObject(guildId);
		if(info == null){
			return false;
		}
		int left = GuildConstProperty.getInstance().getClearMaxNum() - info.getEntity().getClearResNum();
		
		GuildClearResouceNum.Builder builder = GuildClearResouceNum.newBuilder();
		builder.setLeftCount(left);
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CLEAR_MANOR_RESOURCE_LEFT_NUM_S_VALUE, builder));
		return true;
	}
	
	/**
	 * 遣返联盟领地行军
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.REPATRIATE_MANOR_MARCH_C_VALUE)
	private boolean onRepatriateManormarch(HawkProtocol protocol){
		if (!player.hasGuild()) {
			return false;
		}
		
		RepatriateManorMarch req = protocol.parseProtocol(RepatriateManorMarch.getDefaultInstance());
		
		// 世界点
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(req.getX(), req.getY());
		if(worldPoint == null){
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST_VALUE);
			return false;
		}
		
		// 被遣返玩家
		String playerId = req.getPlayerId();
		if (HawkOSOperator.isEmptyString(req.getPlayerId())) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}
		
		// 领地
		if(worldPoint.getPointType() == WorldPointType.GUILD_TERRITORY_VALUE){
			AbstractBuildable buildable = GuildManorService.getInstance().getBuildable(worldPoint);
			if(buildable == null){
				return false;
			}
			buildable.repatriateMarch(player, playerId);
		}
		
		// 战区
		if (worldPoint.getPointType() == WorldPointType.SUPER_WEAPON_VALUE) {
			IWeapon weapon = SuperWeaponService.getInstance().getWeapon(worldPoint.getId());
			if (weapon == null) {
				return false;
			}
			weapon.repatriateMarch(player, req.getPlayerId());
		}
		// 小战区
		if (worldPoint.getPointType() == WorldPointType.XIAO_ZHAN_QU_VALUE) {
			XZQWorldPoint weapon = XZQService.getInstance().getXZQPoint(worldPoint.getId());
			if (weapon == null) {
				return false;
			}
			weapon.repatriateMarch(player, req.getPlayerId());
		}
		// 王城
		if (worldPoint.getPointType() == WorldPointType.KING_PALACE_VALUE) {
			PresidentCity presidentCity = PresidentFightService.getInstance().getPresidentCity();
			if (presidentCity == null) {
				return false;
			}
			if (!presidentCity.repatriateMarch(player, req.getPlayerId(), protocol)) {
				return false;
			}
		}
		
		// 王城箭塔
		if (worldPoint.getPointType() == WorldPointType.CAPITAL_TOWER_VALUE) {
			PresidentCity presidentCity = PresidentFightService.getInstance().getPresidentCity();
			if (presidentCity == null) {
				return false;
			}
			PresidentTower tower = presidentCity.getTower(worldPoint.getId());
			if (tower == null) {
				return false;
			}
			if (!tower.repatriateMarch(player, req.getPlayerId(), protocol)) {
				return false;
			}
		}
		
		// 战旗
		if (worldPoint.getPointType() == WorldPointType.WAR_FLAG_POINT_VALUE) {
			WarFlagService.getInstance().repatriateMarch(protocol.getType(), player, req.getPlayerId(), worldPoint.getGuildBuildId());
		}
		
		if (worldPoint.getPointType() == WorldPointType.CROSS_FORTRESS_VALUE) {
			IFortress fortress = CrossFortressService.getInstance().getFortress(worldPoint.getId());
			if (fortress == null) {
				return false;
			}
			fortress.repatriateMarch(player, req.getPlayerId());			
		}
		
		// 星甲召唤舱体
		if (worldPoint.getPointType() == WorldPointType.SPACE_MECHA_MAIN_VALUE || worldPoint.getPointType() == WorldPointType.SPACE_MECHA_SLAVE_VALUE) {
			boolean succ = SpaceMechaService.getInstance().repatriateSpaceDefMarch(player, playerId, (SpaceWorldPoint) worldPoint);
			if (!succ) {
				sendError(protocol.getType(), Status.Error.SPACE_MECHA_REPATRIATE_MARCH_FAILED_VALUE);
				return false;
			}
		}
		
		player.responseSuccess(HP.code.REPATRIATE_MANOR_MARCH_C_VALUE);
		return true;
	}
	
	/**
	 * 任命队长
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.APPOINTED_CAPTAIN_VALUE)
	private boolean onChangeQuarterLeader(HawkProtocol protocol){
		if (!player.hasGuild()) {
			return false;
		}

		AppointedCaptain req = protocol.parseProtocol(AppointedCaptain.getDefaultInstance());
		
		// 世界点
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(req.getX(), req.getY());
		if (worldPoint == null) {
			sendError(protocol.getType(), Status.Error.WORLD_POINT_NOT_EXIST_VALUE);
			return false;
		}
		
		// 被任命玩家
		if (HawkOSOperator.isEmptyString(req.getPlayerId())) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			return false;
		}
		
		// 领地
		if(worldPoint.getPointType() == WorldPointType.GUILD_TERRITORY_VALUE){
			AbstractBuildable buildable = GuildManorService.getInstance().getBuildable(worldPoint);
			if(buildable == null){
				return false;
			}
			buildable.cheangeQuarterLeader(player, req.getPlayerId());
		}
		
		// 战区
		if (worldPoint.getPointType() == WorldPointType.SUPER_WEAPON_VALUE) {
			IWeapon weapon = SuperWeaponService.getInstance().getWeapon(worldPoint.getId());
			if (weapon == null) {
				return false;
			}
			weapon.cheangeQuarterLeader(player, req.getPlayerId());
		}
		// 小战区
		if (worldPoint.getPointType() == WorldPointType.XIAO_ZHAN_QU_VALUE) {
			XZQWorldPoint weapon = XZQService.getInstance().getXZQPoint(worldPoint.getId());
			if (weapon == null) {
				return false;
			}
			weapon.cheangeQuarterLeader(player, req.getPlayerId());
		}
		// 王城
		if (worldPoint.getPointType() == WorldPointType.KING_PALACE_VALUE) {
			PresidentCity presidentCity = PresidentFightService.getInstance().getPresidentCity();
			if (presidentCity == null) {
				return false;
			}
			if (!presidentCity.cheangeQuarterLeader(player, req.getPlayerId(), protocol)) {
				return false;
			}
		}
		
		// 王城箭塔
		if (worldPoint.getPointType() == WorldPointType.CAPITAL_TOWER_VALUE) {
			PresidentCity presidentCity = PresidentFightService.getInstance().getPresidentCity();
			if (presidentCity == null) {
				return false;
			}
			PresidentTower tower = presidentCity.getTower(worldPoint.getId());
			if (tower == null) {
				return false;
			}
			if (!tower.cheangeQuarterLeader(player, req.getPlayerId(), protocol)) {
				return false;
			}
		}
		
		// 战旗
		if (worldPoint.getPointType() == WorldPointType.WAR_FLAG_POINT_VALUE) {
			WarFlagService.getInstance().cheangeQuarterLeader(protocol.getType(), player, req.getPlayerId(), worldPoint.getGuildBuildId());
		}

		if (worldPoint.getPointType() == WorldPointType.CROSS_FORTRESS_VALUE) {
			IFortress fortress = CrossFortressService.getInstance().getFortress(worldPoint.getId());
			if (fortress == null) {
				return false;
			}
			fortress.cheangeQuarterLeader(player, req.getPlayerId());			
		}
		
		// 星甲召唤舱体
		if (worldPoint.getPointType() == WorldPointType.SPACE_MECHA_MAIN_VALUE || worldPoint.getPointType() == WorldPointType.SPACE_MECHA_SLAVE_VALUE) {
			int result = SpaceMechaService.getInstance().changeLeader(player, worldPoint, req.getPlayerId());
			if (result != 0) {
				sendError(protocol.getType(), result);
				return false;
			}
		}
		
		player.responseSuccess(protocol.getType());
		return true;
	}
}
