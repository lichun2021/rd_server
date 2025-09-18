package com.hawk.game.lianmengstarwars.player.module;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.ShopCfg;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.lianmengstarwars.ISWWorldPoint;
import com.hawk.game.lianmengstarwars.SWRoomManager;
import com.hawk.game.lianmengstarwars.SWWorldPointService;
import com.hawk.game.lianmengstarwars.invoker.SWMoveCityMsgInvoker;
import com.hawk.game.lianmengstarwars.msg.SWQuitReason;
import com.hawk.game.lianmengstarwars.player.ISWPlayer;
import com.hawk.game.lianmengstarwars.worldmarch.ISWWorldMarch;
import com.hawk.game.lianmengstarwars.worldpoint.ISWBuilding;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.march.MarchSet;
import com.hawk.game.module.PlayerDressModule;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Army.ArmyHeroPB;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.GuildAssistant.HPAssistantMarchPB;
import com.hawk.game.protocol.GuildAssistant.HPGuildAssistantResp;
import com.hawk.game.protocol.GuildManager.GetGuildAssistenceInfoReq;
import com.hawk.game.protocol.GuildManager.GetGuildAssistenceInfoResp;
import com.hawk.game.protocol.GuildWar.HPGetGuildWarInfoReq;
import com.hawk.game.protocol.GuildWar.HPGetGuildWarInfoResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Player.EffectPB;
import com.hawk.game.protocol.Player.HPPlayerEffectSync;
import com.hawk.game.protocol.Player.OtherPlayerDetailResp;
import com.hawk.game.protocol.Player.OtherPlayerEffectReq;
import com.hawk.game.protocol.Player.PlayerDetailReq;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponQuarterInfoReq;
import com.hawk.game.protocol.World.CityMoveType;
import com.hawk.game.protocol.World.MarchData;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.MarchEventSync;
import com.hawk.game.protocol.World.PlayerEnterWorld;
import com.hawk.game.protocol.World.PlayerWorldMove;
import com.hawk.game.protocol.World.ReqWorldPointDetail;
import com.hawk.game.protocol.World.RespWorldPointDetail;
import com.hawk.game.protocol.World.ShareCoordinateReq;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldMoveCityReq;
import com.hawk.game.protocol.World.WorldMoveCityResp;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointSync;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

public class SWWorldModule extends PlayerModule {

	private ISWPlayer player;
	public SWWorldModule(ISWPlayer player) {
		super(player);
		this.player = player;
	}

	@ProtocolHandler(code = HP.code.SHARE_COORDINATE_C_VALUE)
	private boolean onShareCoordinate(HawkProtocol protocol) {
		
		ShareCoordinateReq req = protocol.parseProtocol(ShareCoordinateReq.getDefaultInstance());
		int posX = req.getPosX();
		int posY = req.getPosY();
		String pointName = req.getPointName();

		ChatParames parames = ChatParames.newBuilder().setChatType(Const.ChatType.CHAT_FUBEN_TEAM).setKey(Const.NoticeCfgId.SW_SHARECOORDINATE).setPlayer(player).addParms(pointName, posX, posY).build();
		player.getParent().addWorldBroadcastMsg(parames);
		player.responseSuccess(protocol.getType());
		return true;
	}

	/**
	 * 装扮
	 */
	@ProtocolHandler(code = HP.code.DO_DRESS_REQ_VALUE)
	private void doDress(HawkProtocol protocol) {
		
		PlayerDressModule module = player.getModule(GsConst.ModuleType.PLAYER_DRESS);
		module.doDress(protocol);
		
		player.getParent().worldPointUpdate(player);
	}
	
	/**
	 * 请求更换装扮外观
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.DO_CHANGE_DRESS_SHOW_REQ_VALUE)
	private void doChangeDressShowType(HawkProtocol protocol) {
		
		PlayerDressModule module = player.getModule(GsConst.ModuleType.PLAYER_DRESS);
		module.doChangeDressShowType(protocol);
		
		player.getParent().worldPointUpdate(player);
	}

	@ProtocolHandler(code = HP.code.SW_SECOND_MAP_C_VALUE)
	private void getSecondMap(HawkProtocol protocol) {
		
		player.getParent().getSecondMap(player);
	}

	/**
	 * 帮助联盟玩家被援助的信息
	 */
	@ProtocolHandler(code = HP.code.GUILD_MEMBER_ASSISTENCE_INFO_C_VALUE)
	private boolean onGetGuildAssistenceInfo(HawkProtocol protocol) {
		
		GetGuildAssistenceInfoReq req = protocol.parseProtocol(GetGuildAssistenceInfoReq.getDefaultInstance());
		ISWPlayer tarPlayer = player.getParent().getPlayer(req.getPlayerId());
		if (tarPlayer == null) {
			return false;
		}

		// 检查是否同盟
		if (!player.isInSameGuild(tarPlayer)) {
			player.sendError(protocol.getType(), Status.Error.GUILD_NOT_MEMBER);
			return false;
		}

		int maxCnt = tarPlayer.getMaxAssistSoldier();

		List<WorldMarch> helpMarchList = player.getParent().getPointMarches(tarPlayer.getPointId(),
				WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST,
				WorldMarchType.ASSISTANCE).stream().map(ISWWorldMarch::getMarchEntity).collect(Collectors.toList());
		int curCnt = WorldUtil.calcMarchsSoldierCnt(helpMarchList);

		GetGuildAssistenceInfoResp.Builder resp = GetGuildAssistenceInfoResp.newBuilder();
		resp.setCurCnt(curCnt);
		resp.setMaxCnt(maxCnt);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_MEMBER_ASSISTENCE_INFO_S_VALUE, resp));

		return true;
	}

	/**
	 * 获取超级武器驻军列表
	 */
	@ProtocolHandler(code = HP.code.SUPER_WEAPON_QUARTER_INFO_C_VALUE)
	private void getPresidentTowerQuarterInfo(HawkProtocol protocol) {
		
		SuperWeaponQuarterInfoReq req = protocol.parseProtocol(SuperWeaponQuarterInfoReq.getDefaultInstance());
		ISWBuilding weapon = (ISWBuilding) player.getParent().getWorldPoint(req.getPosX(), req.getPosY()).orElse(null);
		if (weapon == null) {
			return;
		}
		weapon.syncQuarterInfo(player);
		return;
	}

	@ProtocolHandler(code = HP.code.SW_GAME_SYNC_C_VALUE)
	private boolean onGameSync(HawkProtocol protocol) {
		
		player.getParent().sync(player);
		return true;
	}

	/**
	 * 玩家迁城
	 * 
	 * @param session
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.WORLD_MOVE_CITY_C_VALUE)
	private boolean onPlayerMoveCity(HawkProtocol protocol) {
		
		if (player.getParent().getStartTime() > HawkTime.getMillisecond()) {
			player.sendError(protocol.getType(), Status.Error.SW_PRETIME_CANNOT_MOVE);
			return false;
		}
		WorldMoveCityReq req = protocol.parseProtocol(WorldMoveCityReq.getDefaultInstance());

		// 迁城类型
		int moveCityType = 2;// req.getType();
		// 迁移前城点
		// ISWWorldPoint beforePoint = player;

		// 城点保护结束时间
		// long protectedEndTime = player.getParent().getStartTime();
		// 迁城消耗
		ConsumeItems consumeItems = ConsumeItems.valueOf();

		// 迁城检测
		if (!moveCityCheck(player, protocol.getType(), req, consumeItems)) {
			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_INVALID);
			return false;
		}
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_INVALID);
			return false;
		}

		int x = req.getX();
		int y = req.getY();

		// 目标点
		if (WorldUtil.isRandomMoveCity(moveCityType)) {
			// 随机迁城
			int[] xy = player.getParent().getWorldPointService().randomFreePoint(player.getParent().randomPoint(), player.getWorldPointRadius());
			// 迁城失败
			if (xy == null) {
				player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MOVE_CITY_S, WorldMoveCityResp.newBuilder().setResult(false)));
				return false;
			}
			x = xy[0];
			y = xy[1];
		}
		if ((x + y) % 2 == 0) {
			x += 1;
		}

		if (player.getCostCityMoveCount() > 0) {
			// 投递回玩家线程：消耗道具
			player.dealMsg(MsgId.MOVE_CITY, new SWMoveCityMsgInvoker(player, consumeItems, moveCityType));
		}

		// 迁城成功
		if (player.getParent().getCurTimeMil() > player.getNextCityMoveTime()) {

			int moveCityCd = player.getParent().getCfg().getMoveCityCd();
			// 记录迁城时间
			player.setNextCityMoveTime(player.getParent().getCurTimeMil() + moveCityCd * 1000);
		}
		int[] toPos = new int[] { x, y };
		player.getParent().doMoveCitySuccess(player, toPos);

		player.setCostCityMoveCount(player.getCostCityMoveCount() + 1);
		player.setGameMoveCityCount(player.getGameMoveCityCount() + 1);
		player.moveCityCDSync();
		return true;
	}

	private boolean moveCityCheck(ISWPlayer player, int hp, WorldMoveCityReq req, ConsumeItems consumeItems) {
		// 迁城类型
		int moveCityType = req.getType();

		boolean forceMove = (req.hasForce() && req.getForce()) ? true : false;

		// 消耗检测
		if (!checkMoveCityConsume(player, hp, moveCityType, consumeItems, forceMove)) {
			return false;
		}

		// 迁城检测
		if (WorldUtil.isRandomMoveCity(moveCityType) && !randomMoveCityCheck(player, forceMove)) {
			return false;

		} else if (WorldUtil.isSelectMoveCity(moveCityType) && !selectMoveCityCheck(player, hp, req)) {
			return false;

		} else if (WorldUtil.isGuildMoveCity(moveCityType) && !guildMoveCityCheck(player)) {
			return false;
		}

		return true;
	}

	/**
	 * 检测迁城消耗, 返回值非0表示错误码
	 * 
	 * @param type
	 * @param consumeItems
	 * @return
	 */
	private boolean checkMoveCityConsume(ISWPlayer player, int hp, int type, ConsumeItems consumeItems, boolean forceMove) {
		if (player.getParent().getCurTimeMil() > player.getNextCityMoveTime()) {
			player.setCostCityMoveCount(0);
			return true;
		}
		// 强制迁城不消耗资源
		if (forceMove) {
			return true;
		}

		// 随机迁城判断
		if (type == CityMoveType.RANDOM_MOVE_VALUE) {
			// 迁城道具判断
			if (player.getData().getItemNumByItemId(Const.ItemId.ITEM_RANDOM_MOVE_CITY_VALUE) > 0) {
				consumeItems.addItemConsume(Const.ItemId.ITEM_RANDOM_MOVE_CITY_VALUE, 1);
			} else {
				ShopCfg shopCfg = ShopCfg.getShopCfgByItemId(Const.ItemId.ITEM_RANDOM_MOVE_CITY_VALUE);
				if (shopCfg == null) {
					player.sendError(hp, Status.Error.ITEM_NOT_FOUND);
					return false;
				}
				consumeItems.addConsumeInfo(shopCfg.getPriceItemInfo(), true);
			}
		} else if (type == CityMoveType.SELECT_MOVE_VALUE) {
			int need = player.getCostCityMoveCount();
			int has = player.getData().getItemNumByItemId(Const.ItemId.ITEM_SELECT_MOVE_CITY_VALUE);
			int notEnough = need - has;
			// 迁城道具判断（优先使用新手高迁）
			if (has > 0) {
				consumeItems.addItemConsume(Const.ItemId.ITEM_SELECT_MOVE_CITY_VALUE, Math.min(has, need));
			}
			if (notEnough > 0) {
				ShopCfg shopCfg = ShopCfg.getShopCfgByItemId(Const.ItemId.ITEM_SELECT_MOVE_CITY_VALUE);
				if (shopCfg == null) {
					player.sendError(hp, Status.Error.ITEM_NOT_FOUND);
					return false;
				}
				ItemInfo priceItemInfo = shopCfg.getPriceItemInfo();
				priceItemInfo.setCount(priceItemInfo.getCount() * notEnough);
				consumeItems.addConsumeInfo(priceItemInfo, true);
			}
		} else if (type == CityMoveType.GUILD_MOVE_VALUE) {
			// 迁城道具判断
			if (player.getData().getItemNumByItemId(Const.ItemId.ITEM_GUILD_MOVE_CITY_VALUE) <= 0) {
				return false;
			}
			consumeItems.addItemConsume(Const.ItemId.ITEM_GUILD_MOVE_CITY_VALUE, 1);
		}

		return true;
	}

	/**
	 * 随机迁城检测
	 * 
	 * 1)当玩家有部队在城点外面时，玩家无法使用随机迁城 2)当玩家处于被攻击或被侦查状态时，包括外面的资源点和驻扎点被攻击或侦查，都无法使用随机迁城 3)当玩家援助他人时，无法使用随机迁城
	 * 
	 * @return
	 */
	private boolean randomMoveCityCheck(ISWPlayer player, boolean forceMove) {
		// 自己有出征队伍
		if (!forceMove && player.getParent().getPlayerMarchCount(player.getId()) > 0) {
			player.sendError(HP.code.WORLD_MOVE_CITY_C_VALUE, Status.Error.HAS_MARCH_IN_WORLD);
			return false;
		}

		// 是否被攻击
		List<ISWWorldMarch> marchList = player.getParent().getPointMarches(player.getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH, WorldMarchType.ATTACK_PLAYER);
		if (marchList != null && marchList.size() > 0) {
			player.sendError(HP.code.WORLD_MOVE_CITY_C_VALUE, Status.Error.RANDOM_MOVE_CITY_BEING_ATTACTED);
			return false;
		}

		// 是否被侦查
		boolean beSpy = false;
		marchList = player.getParent().getPointMarches(player.getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH, WorldMarchType.SPY);
		for (IWorldMarch march : marchList) {
			if (march.getTerminalId() == player.getPlayerPos()) {
				beSpy = true;
			}
		}
		if (beSpy) {
			player.sendError(HP.code.WORLD_MOVE_CITY_C_VALUE, Status.Error.CITY_BEEN_SPYING);
			return false;
		}

		return true;
	}

	/**
	 * 定点迁城检测
	 * 
	 * @return
	 */
	private boolean selectMoveCityCheck(ISWPlayer player, int hp, WorldMoveCityReq req) {
		if (player.getX() == req.getX() && player.getY() == req.getY()) {
			return true;
		}

		SWWorldPointService worldPointService = player.getParent().getWorldPointService();
		if (!worldPointService.tryOccupied(req.getX(), req.getY(), player.getWorldPointRadius())) {
			worldPointService.addToAreaFreePoint(player);
			if (!worldPointService.tryOccupied(req.getX(), req.getY(), player.getWorldPointRadius())) {
				worldPointService.rmFromAreaFreePoint(player);
				System.out.println("又阻挡了？");
				return false;
			}
		}
		return true;
	}

	/**
	 * 联盟迁城检测
	 * 
	 * @return
	 */
	private boolean guildMoveCityCheck(ISWPlayer player) {
		return false;
	}

	/**
	 * 进入世界地图
	 *
	 * @param session
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PLAYER_ENTER_WORLD_VALUE)
	private void onPlayerEnterWorld(HawkProtocol protocol) {
		PlayerEnterWorld cmd = protocol.parseProtocol(PlayerEnterWorld.getDefaultInstance());
		

		int aoiObjId = player.getEye().getAoiObjId();
		aoiObjId = player.getParent().getWorldPointService().getWorldScene().add(GsConst.WorldObjType.PLAYER, aoiObjId, cmd.getX(), cmd.getY(),
				0, 0, GameConstCfg.getInstance().getViewXRadius(), GameConstCfg.getInstance().getViewYRadius(), player.getEye());
		player.getEye().setAoiObjId(aoiObjId);

		// 自己的行军不走这种同步模式
		player.getInviewMarchs().clear();

		onPlayerMove(player, cmd.getX(), cmd.getY());

		player.getParent().sync(player);
	}
	
	/**
	 * 玩家在世界地图滑动视野(移动)
	 *
	 * @param session
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PLAYER_WORLD_MOVE_VALUE)
	private boolean onPlayerWorldMove(HawkProtocol protocol) {
		PlayerWorldMove cmd = protocol.parseProtocol(PlayerWorldMove.getDefaultInstance());
		
		float moveSpeed = 0.0f;
		if (cmd.hasSpeed()) {
			moveSpeed = Math.max(0.0f, cmd.getSpeed());
			moveSpeed = Math.min(1.0f, cmd.getSpeed());
		}

		// fillpoint会做速度处理，此处忽略速度判断
		player.getParent().getWorldPointService().getWorldScene().move(player.getEye().getAoiObjId(), cmd.getX(), cmd.getY(), moveSpeed);
		// 行军同步
		if (HawkOSOperator.isZero(GameConstCfg.getInstance().getMoveSyncFactor()) || moveSpeed <= GameConstCfg.getInstance().getMoveSyncFactor() - 1.0f) {
			onPlayerMove(player, cmd.getX(), cmd.getY());
		}
		return true;  
	}
	
	/**
	 * 离开世界地图
	 *
	 * @param session
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PLAYER_LEAVE_WORLD_VALUE)
	private boolean onPlayerLeaveWorld(HawkProtocol protocol) {
		

		player.getParent().getWorldPointService().getWorldScene().leave(player.getEye().getAoiObjId());

		player.getInviewMarchs().clear();

		return true;
	}
	
	/**
	 * 观察者视角切换
	 * 
	 * @param player
	 */
	public void onPlayerMove(ISWPlayer player, int x, int y) {
		// 当前的
		MarchSet inviewMarchs = player.getInviewMarchs();

		MarchEventSync.Builder addbuilder = MarchEventSync.newBuilder();
		addbuilder.setEventType(MarchEvent.MARCH_ADD_VALUE);

		MarchEventSync.Builder delbuilder = MarchEventSync.newBuilder();
		delbuilder.setEventType(MarchEvent.MARCH_DELETE_VALUE);
		MarchSet currentSet = new MarchSet();
		for (ISWWorldMarch march : player.getParent().getWorldMarchList()) {
			WorldMarchRelation relation = march.getRelation(player);
			if (relation.equals(WorldMarchRelation.SELF)) {
				continue;
			}
			boolean hasPush = inviewMarchs.contains(march.getMarchId());
			boolean inview = march.isInview(x, y);
			if (inview) {
				currentSet.add(march.getMarchId());
			}
			if (inview && !hasPush) {
				MarchData.Builder dataBuilder = MarchData.newBuilder();
				dataBuilder.setMarchId(march.getMarchId());
				dataBuilder.setMarchPB(march.toBuilder(relation));
				addbuilder.addMarchData(dataBuilder);
			}

			if (!inview && hasPush) {
				MarchData.Builder dataBuilder = MarchData.newBuilder();
				dataBuilder.setMarchId(march.getMarchId());
				delbuilder.addMarchData(dataBuilder);
			}
		}
		if (addbuilder.getMarchDataCount() > 0) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCH_EVENT_SYNC_VALUE, addbuilder));
		}
		if (delbuilder.getMarchDataCount() > 0) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCH_EVENT_SYNC_VALUE, delbuilder));
		}

		player.setInviewMarchs(currentSet);
	}

	/** 主动退出 */
	@ProtocolHandler(code = HP.code.SW_QUIT_ROOM_REQ_VALUE)
	private void onPlayerQuitRoom(HawkProtocol protocol) {
		
		player.getParent().quitWorld(player, SWQuitReason.LEAVE);
	}

	/**
	 * 城防信息请求
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.CITYDEF_REQ_C_VALUE)
	private boolean onCityDefReq(HawkProtocol protocol) {
		
		player.getPush().syncCityDef(false);
		return true;
	}

	/** 城墙灭火 */
	@ProtocolHandler(code = HP.code.BUILDING_OUTFIRE_C_VALUE)
	private boolean onOutFire(HawkProtocol protocol) {
		
		if (player.getOnFireEndTime() <= HawkTime.getMillisecond()) {
			player.sendError(protocol.getType(), Status.Error.CITY_NOT_ON_FIRE);
			return false;
		}

		ConsumeItems consume = ConsumeItems.valueOf();
		// 城墙灭火消耗水晶的配置
		consume.addConsumeInfo(ConstProperty.getInstance().getOutFireCostItems(), false);
		// 检查需要的资源是否足够
		if (!consume.checkConsume(player, protocol.getType())) {
			return false;
		}
		// 消耗水晶
		consume.consumeAndPush(player, Action.PLAYER_OUT_FIRE);
		player.setOnFireEndTime(0);
		player.getPush().syncCityDef(false);
		player.responseSuccess(protocol.getType());

		player.getParent().worldPointUpdate(player);
		return true;
	}

	/**
	 * 修复城防
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.BUILDING_REPAIR_C_VALUE)
	private boolean onCityDefRepair(HawkProtocol protocol) {
		
		int cityDefVal = player.getCityDefVal();
		int cityDefMax = player.getRealMaxCityDef(); // 城防值上限
		if (cityDefVal >= cityDefMax) {
			player.sendError(protocol.getType(), Status.Error.CITY_DEF_EXCEED_LIMIT);
			return false;
		}

		long cityDefNextRepairTime = player.getCityDefNextRepairTime();
		long now = HawkTime.getMillisecond();
		if (cityDefNextRepairTime > now) {
			player.sendError(protocol.getType(), Status.Error.CITY_REPAIR_EARLY);
			return false;
		}
		int repairCD = ConstProperty.getInstance().getWallRepairCd();
		int cityDefAdd = ConstProperty.getInstance().getOnceWallRepair() + player.getData().getEffVal(Const.EffType.REPAIR_CITY_DEF_ADD);
		player.setCityDefNextRepairTime(now + repairCD * 1000L);
		player.setCityDefVal(cityDefAdd + cityDefVal);
		player.getPush().syncCityDef(false);
		player.responseSuccess(protocol.getType());
		return true;
	}

	/**
	 * 其他玩家作用号数据请求
	 * 
	 * @return
	 */
	@ProtocolHandler(code = HP.code.OTHER_PLAYER_EFFECT_C_VALUE)
	private boolean onGetOtherPlayerEffect(HawkProtocol protocol) {
		
		OtherPlayerEffectReq req = protocol.parseProtocol(OtherPlayerEffectReq.getDefaultInstance());
		String playerId = req.getPlayerId();
		List<Integer> effectIds = req.getEffectIdList();
		if (HawkOSOperator.isEmptyString(playerId) || effectIds == null || effectIds.isEmpty()) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}

		Player otherPlayer = SWRoomManager.getInstance().makesurePlayer(playerId);
		if (otherPlayer == null) {
			player.sendError(protocol.getType(), Status.SysError.ACCOUNT_NOT_EXIST);
			return false;
		}

		HPPlayerEffectSync.Builder builder = HPPlayerEffectSync.newBuilder();
		for (int effectId : effectIds) {
			int effectVal = otherPlayer.getEffect().getEffVal(EffType.valueOf(effectId));

			EffectPB.Builder effPB = EffectPB.newBuilder();
			effPB.setEffId(effectId);
			effPB.setEffVal(effectVal);
			builder.addEffList(effPB);
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.OTHER_PLAYER_EFFECT_S, builder));
		return true;
	}

	/**
	 * @param protocol
	 * @return 其它领主详情
	 */
	@ProtocolHandler(code = HP.code.PLAYER_DETAIL_OTHER_C_VALUE)
	private boolean onPlayerDetailOtherRequest(HawkProtocol protocol) {
		
		PlayerDetailReq req = protocol.parseProtocol(PlayerDetailReq.getDefaultInstance());
		String playerId = req.getPlayerId();
		// 快照数据
		Player snapshot = SWRoomManager.getInstance().makesurePlayer(playerId);
		if (snapshot == null) {
			player.sendError(protocol.getType(), Status.SysError.ACCOUNT_NOT_EXIST);
			return false;
		}

		OtherPlayerDetailResp.Builder builder = OtherPlayerDetailResp.newBuilder();
		builder.setSnapshot(BuilderUtil.buildSnapshotData(snapshot));
		builder.setState(0);
		builder.setTodayDueled(100);
		builder.setArmourInfo(BuilderUtil.genArmourBriefInfo(snapshot.getData()));
		builder.setArmourTechInfo(BuilderUtil.genArmourEquipBriefInfo(snapshot.getData()));
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PLAYER_DETAIL_OTHER_S_VALUE, builder));
		return true;
	}

	/**
	 * 获取世界点详细信息
	 * 
	 * @param session
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.WORLD_POINT_DETAIL_C_VALUE)
	private void onReqWorldPointDetail(HawkProtocol protocol) {
		
		ReqWorldPointDetail req = protocol.parseProtocol(ReqWorldPointDetail.getDefaultInstance());
		ISWWorldPoint worldPoint = player.getParent().getWorldPoint(req.getPointX(), req.getPointY()).orElse(null);
		if (worldPoint == null) {
			// 删除点
			WorldPointPB.Builder pb = WorldPointPB.newBuilder();
			pb.setPointX(req.getPointX());
			pb.setPointY(req.getPointY());
			pb.setPointType(WorldPointType.PLAYER);
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			builder.setIsRemove(true);
			builder.addPoints(pb);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder));
			return;
		}

		RespWorldPointDetail.Builder resp = RespWorldPointDetail.newBuilder();
		resp.setPoint(worldPoint.toDetailBuilder(player));

		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_POINT_DETAIL_S_VALUE, resp));
	}

	/**
	 * 获取联盟战争界面信息
	 * 
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GET_GUILD_WAR_INFO_C_VALUE)
	protected boolean onGetGuildWarInfo(HawkProtocol protocol) {
		

		// 回复协议
		HPGetGuildWarInfoResp.Builder resp = HPGetGuildWarInfoResp.newBuilder();
		// 联盟行军
		List<ISWWorldMarch> guildMarchs = player.getParent().getGuildWarMarch(player.getGuildId());
		for (ISWWorldMarch guildMarch : guildMarchs) {
			try {
				resp.addGuildWar(guildMarch.getGuildWarShoPb(player));
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GET_GUILD_WAR_INFO_S_VALUE, resp));
		return true;
	}

	@ProtocolHandler(code = HP.code.GET_GUILD_WAR_CELL_INFO_C_VALUE)
	protected boolean onGetGuildWarCellInfo(HawkProtocol protocol) {
		
		HPGetGuildWarInfoReq req = protocol.parseProtocol(HPGetGuildWarInfoReq.getDefaultInstance());
		final String marchId = req.getMarchId();

		// 回复协议
		HPGetGuildWarInfoResp.Builder resp = HPGetGuildWarInfoResp.newBuilder();
		ISWWorldMarch guildMarch = player.getParent().getMarch(marchId);
		if (guildMarch != null) {
			boolean bfalse = guildMarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE || guildMarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE;
			if (bfalse) {
				resp.addGuildWar(guildMarch.getGuildWarShoPb(player));
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GET_GUILD_WAR_CELL_INFO_S_VALUE, resp));
		return true;
	}

	/**
	 * 获取联盟士兵援助信息
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_ASSISTANT_INFO_C_VALUE)
	private boolean onGetGuildAssistantInfo(HawkProtocol protocol) {
		
		List<ISWWorldMarch> marchList = player.assisReachMarches();
		HPGuildAssistantResp.Builder resp = HPGuildAssistantResp.newBuilder();
		int totalForces = 0;
		if (marchList != null) {
			Set<String> playerIdSet = new HashSet<String>();
			for (ISWWorldMarch march : marchList) {
				playerIdSet.add(march.getPlayerId());
			}
			for (ISWWorldMarch march : marchList) {
				HPAssistantMarchPB.Builder marchBuilder = HPAssistantMarchPB.newBuilder();
				String playerId = march.getPlayerId();
				marchBuilder.setUuid(march.getMarchId());
				marchBuilder.setPlayerId(playerId);

				ISWPlayer assistPlayer = march.getParent();
				marchBuilder.setIcon(assistPlayer.getIcon());
				if (!HawkOSOperator.isEmptyString(assistPlayer.getPfIcon())) {
					marchBuilder.setPfIcon(assistPlayer.getPfIcon());
				}
				marchBuilder.setPlayerName(assistPlayer.getName());

				List<PlayerHero> heroList = assistPlayer.getHeroByCfgId(march.getMarchEntity().getHeroIdList());
				for (PlayerHero hero : heroList) {
					marchBuilder.addHero(ArmyHeroPB.newBuilder().setHeroId(hero.getCfgId()).setLevel(hero.getLevel()).setStar(hero.getStar()));
					marchBuilder.addHeroList(hero.toPBobj());
				}

				SuperSoldier ssoldier = assistPlayer.getSuperSoldierByCfgId(march.getMarchEntity().getSuperSoldierId()).orElse(null);
				if (Objects.nonNull(ssoldier)) {
					marchBuilder.setSsoldier(ssoldier.toPBobj());
				}

				for (ArmyInfo army : march.getMarchEntity().getArmys()) {
					if (army.getFreeCnt() <= 0) {
						continue;
					}
					totalForces += army.getFreeCnt();
					marchBuilder.addArmySoldier(army.toArmySoldierPB(assistPlayer));
				}

				marchBuilder.setGuildTag(player.getGuildTag());
				resp.addMarchList(marchBuilder);
			}
		}
		resp.setForces(totalForces);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_ASSISTANT_INFO_S, resp));
		return true;
	}

}
