package com.hawk.game.lianmengcyb.module;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;

import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.ShopCfg;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.lianmengcyb.CYBORGBattleRoom;
import com.hawk.game.lianmengcyb.CYBORGProtocol;
import com.hawk.game.lianmengcyb.CYBORGRoomManager;
import com.hawk.game.lianmengcyb.CYBORGRoomManager.CYBORG_CAMP;
import com.hawk.game.lianmengcyb.ICYBORGWorldPoint;
import com.hawk.game.lianmengcyb.invoker.CYBORGMoveCityMsgInvoker;
import com.hawk.game.lianmengcyb.msg.CYBORGQuitReason;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.lianmengcyb.worldmarch.ICYBORGWorldMarch;
import com.hawk.game.lianmengcyb.worldpoint.CYBORGChronoSphere;
import com.hawk.game.lianmengcyb.worldpoint.CYBORGNuclearMissileSilo;
import com.hawk.game.lianmengcyb.worldpoint.ICYBORGBuilding;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.march.MarchSet;
import com.hawk.game.module.PlayerDressModule;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Army.ArmyHeroPB;
import com.hawk.game.protocol.CYBORG.PBCYBORGAnchorGetGuildWarCellInfoReq;
import com.hawk.game.protocol.CYBORG.PBCYBORGAnchorGetGuildWarInfoReq;
import com.hawk.game.protocol.CYBORG.PBCYBORGAnchorListMailReq;
import com.hawk.game.protocol.CYBORG.PBCYBORGNuclearStartReq;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.GuildAssistant.HPAssistantMarchPB;
import com.hawk.game.protocol.GuildAssistant.HPGuildAssistantResp;
import com.hawk.game.protocol.GuildManager.AuthId;
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
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

public class CYBORGWorldModule extends CYBORGBattleRoomModule {
	private int moveCnt;
	public CYBORGWorldModule(CYBORGBattleRoom appObj) {
		super(appObj);
	}

	/**
	 * 核弹发射
	 */
	@ProtocolHandler(code = HP.code.CYBORG_NUCLEAR_SEND_C_VALUE)
	private void onNuclearStart(CYBORGProtocol protocol) {
		ICYBORGPlayer player = protocol.getPlayer();
		PBCYBORGNuclearStartReq req = protocol.parseProtocol(PBCYBORGNuclearStartReq.getDefaultInstance());

		ICYBORGWorldPoint worldPoint = battleRoom.getWorldPoint(req.getX(), req.getY()).orElse(null);
		if (worldPoint == null) {
			return;
		}
		// R4盟主队长可以
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET);
		if (!guildAuthority) {
			player.sendError(protocol.getType(), Status.CYBORGError.CYBORG_ORDER_USE_GUILD_AUTHORITY_LIMIT_VALUE, 0);
			return;
		}

		CYBORGNuclearMissileSilo bulShoot = null;
		for (CYBORGNuclearMissileSilo bul : getParent().getNuclearBuildList()) {
			if (Objects.equals(bul.getPlayerId(), player.getId()) && bul.isCoolDown()) {
				bulShoot = bul;
				break;
			}
		}

		if (Objects.isNull(bulShoot)) {
			for (CYBORGNuclearMissileSilo bul : getParent().getNuclearBuildList()) {
				if (bul.onNuclearShoot(worldPoint, player)) {
					break;
				}
			}
		} else {
			bulShoot.onNuclearShoot(worldPoint, player);
		}

		player.responseSuccess(protocol.getType());
	}

	/**
	 * 主播查看建筑战斗邮件
	 */
	@ProtocolHandler(code = HP.code.CYBORG_ANCHOR_LIST_MAIL_C_VALUE)
	private boolean onListBuildMail(CYBORGProtocol protocol) {
		ICYBORGPlayer player = protocol.getPlayer();
		if (!player.isAnchor()) {
			return false;
		}
		PBCYBORGAnchorListMailReq req = protocol.parseProtocol(PBCYBORGAnchorListMailReq.getDefaultInstance());
		int posX = req.getX();
		int posY = req.getY();
		int type = req.getType();
		ICYBORGBuilding build = (ICYBORGBuilding) getParent().getWorldPoint(posX, posY).orElse(null);
		build.listMail(getParent().getAnchor(), type);
		return true;
	}

	@ProtocolHandler(code = HP.code.SHARE_COORDINATE_C_VALUE)
	private boolean onShareCoordinate(CYBORGProtocol protocol) {
		ICYBORGPlayer player = protocol.getPlayer();
		ShareCoordinateReq req = protocol.parseProtocol(ShareCoordinateReq.getDefaultInstance());
		int posX = req.getPosX();
		int posY = req.getPosY();
		String pointName = req.getPointName();

		ChatParames parames = ChatParames.newBuilder().setChatType(Const.ChatType.CHAT_FUBEN_TEAM).setKey(Const.NoticeCfgId.CYBORG_SHARECOORDINATE).setPlayer(player)
				.addParms(pointName, posX, posY).build();
		getParent().addWorldBroadcastMsg(parames);
		player.responseSuccess(protocol.getType());
		return true;
	}

	/**
	 * 装扮
	 */
	@ProtocolHandler(code = HP.code.DO_DRESS_REQ_VALUE)
	private void doDress(CYBORGProtocol protocol) {
		ICYBORGPlayer player = protocol.getPlayer();
		PlayerDressModule module = player.getModule(GsConst.ModuleType.PLAYER_DRESS);
		module.doDress(protocol);

		getParent().worldPointUpdate(player);
	}

	/**
	 * 请求更换装扮外观
	 * 
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.DO_CHANGE_DRESS_SHOW_REQ_VALUE)
	private void doChangeDressShowType(CYBORGProtocol protocol) {
		ICYBORGPlayer player = protocol.getPlayer();
		PlayerDressModule module = player.getModule(GsConst.ModuleType.PLAYER_DRESS);
		module.doChangeDressShowType(protocol);

		getParent().worldPointUpdate(player);
	}

	@ProtocolHandler(code = HP.code.CYBORG_SECOND_MAP_C_VALUE)
	private void getSecondMap(CYBORGProtocol protocol) {
		ICYBORGPlayer player = protocol.getPlayer();
		getParent().getSecondMap(player);
	}

	/**
	 * 帮助联盟玩家被援助的信息
	 */
	@ProtocolHandler(code = HP.code.GUILD_MEMBER_ASSISTENCE_INFO_C_VALUE)
	private boolean onGetGuildAssistenceInfo(CYBORGProtocol protocol) {
		ICYBORGPlayer player = protocol.getPlayer();
		GetGuildAssistenceInfoReq req = protocol.parseProtocol(GetGuildAssistenceInfoReq.getDefaultInstance());
		ICYBORGPlayer tarPlayer = getParent().getPlayer(req.getPlayerId());
		if (tarPlayer == null) {
			return false;
		}

		// 检查是否同盟
		if (!player.isInSameGuild(tarPlayer)) {
			player.sendError(protocol.getType(), Status.Error.GUILD_NOT_MEMBER);
			return false;
		}

		int maxCnt = tarPlayer.getMaxAssistSoldier();

		List<WorldMarch> helpMarchList = getParent().getPointMarches(tarPlayer.getPointId(),
				WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST,
				WorldMarchType.ASSISTANCE).stream().map(ICYBORGWorldMarch::getMarchEntity).collect(Collectors.toList());
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
	private void getPresidentTowerQuarterInfo(CYBORGProtocol protocol) {
		ICYBORGPlayer player = protocol.getPlayer();
		SuperWeaponQuarterInfoReq req = protocol.parseProtocol(SuperWeaponQuarterInfoReq.getDefaultInstance());
		ICYBORGBuilding weapon = (ICYBORGBuilding) getParent().getWorldPoint(req.getPosX(), req.getPosY()).orElse(null);
		if (weapon == null) {
			return;
		}
		weapon.syncQuarterInfo(player);
		return;
	}

	@ProtocolHandler(code = HP.code.CYBORG_GAME_SYNC_C_VALUE)
	private boolean onGameSync(CYBORGProtocol protocol) {
		ICYBORGPlayer player = protocol.getPlayer();
		getParent().sync(player);
		return true;
	}

	/**
	 * 玩家迁城
	 * 
	 * @param session
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.WORLD_MOVE_CITY_C_VALUE)
	private boolean onPlayerMoveCity(CYBORGProtocol protocol) {
		ICYBORGPlayer player = protocol.getPlayer();
		if (battleRoom.getStartTime() > HawkTime.getMillisecond()) {
			player.sendError(protocol.getType(), Status.Error.CYBORG_PRETIME_CANNOT_MOVE);
			return false;
		}
		WorldMoveCityReq req = protocol.parseProtocol(WorldMoveCityReq.getDefaultInstance());

		// 迁城类型
		int moveCityType = 2;// req.getType();
		// 迁移前城点
		// ICYBORGWorldPoint beforePoint = player;

		// 城点保护结束时间
		// long protectedEndTime = getParent().getStartTime();
		// 迁城消耗
		ConsumeItems consumeItems = ConsumeItems.valueOf();

		// 迁城检测
		if (!moveCityCheck(player, protocol.getType(), req, consumeItems)) {
			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_INVALID);
			return false;
		}
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return false;
		}

		int x = req.getX();
		int y = req.getY();

		// 目标点
		if (WorldUtil.isRandomMoveCity(moveCityType)) {
			// 随机迁城
			int[] xy = getParent().randomFreePoint(getParent().randomPoint(), player.getPointType());
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
			player.dealMsg(MsgId.MOVE_CITY, new CYBORGMoveCityMsgInvoker(player, consumeItems, moveCityType));
		}

		// 迁城成功
		if (getParent().getCurTimeMil() > player.getNextCityMoveTime()) {

			int moveCityCd = getParent().getCfg().getMoveCityCd();
			for (CYBORGChronoSphere w : getParent().getCYBORGBuildingByClass(CYBORGChronoSphere.class)) {
				if (w.underGuildControl(player.getGuildId())) {
					moveCityCd = (int) (moveCityCd * 0.01 * CYBORGChronoSphere.getCfg().getCoolDownReducePercentage());
				}
			}
			// 记录迁城时间
			player.setNextCityMoveTime(getParent().getCurTimeMil() + moveCityCd * 1000);
		}
		int[] toPos = new int[] { x, y };
		getParent().doMoveCitySuccess(player, toPos);

		player.setCostCityMoveCount(player.getCostCityMoveCount() + 1);
		player.setGameMoveCityCount(player.getGameMoveCityCount() + 1);
		player.moveCityCDSync();
		return true;
	}

	private boolean moveCityCheck(ICYBORGPlayer player, int hp, WorldMoveCityReq req, ConsumeItems consumeItems) {
		if (player.isAnchor()) {
			return false;
		}

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
	private boolean checkMoveCityConsume(ICYBORGPlayer player, int hp, int type, ConsumeItems consumeItems, boolean forceMove) {
		if (getParent().getCurTimeMil() > player.getNextCityMoveTime()) {
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
	private boolean randomMoveCityCheck(ICYBORGPlayer player, boolean forceMove) {
		// 自己有出征队伍
		if (!forceMove && getParent().getPlayerMarchCount(player.getId()) > 0) {
			player.sendError(HP.code.WORLD_MOVE_CITY_C_VALUE, Status.Error.HAS_MARCH_IN_WORLD);
			return false;
		}

		// 是否被攻击
		List<ICYBORGWorldMarch> marchList = getParent().getPointMarches(player.getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH, WorldMarchType.ATTACK_PLAYER);
		if (marchList != null && marchList.size() > 0) {
			player.sendError(HP.code.WORLD_MOVE_CITY_C_VALUE, Status.Error.RANDOM_MOVE_CITY_BEING_ATTACTED);
			return false;
		}

		// 是否被侦查
		boolean beSpy = false;
		marchList = getParent().getPointMarches(player.getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH, WorldMarchType.SPY);
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
	private boolean selectMoveCityCheck(ICYBORGPlayer player, int hp, WorldMoveCityReq req) {
		if (getParent().checkPlayerCanOccupy(player, req.getX(), req.getY())) {
			return true;
		}
		return false;
	}

	/**
	 * 联盟迁城检测
	 * 
	 * @return
	 */
	private boolean guildMoveCityCheck(ICYBORGPlayer player) {
		return false;
	}

	/**
	 * 进入世界地图
	 *
	 * @param session
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PLAYER_ENTER_WORLD_VALUE)
	private void onPlayerEnterWorld(CYBORGProtocol protocol) {
		PlayerEnterWorld cmd = protocol.parseProtocol(PlayerEnterWorld.getDefaultInstance());
		ICYBORGPlayer player = protocol.getPlayer();

		battleRoom.enterWorld(player);
		getParent().sync(player);

		{
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			for (ICYBORGWorldPoint point : getParent().getViewPoints()) {
				try {
					builder.addPoints(point.toBuilder(player));
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
			player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder));
		}

		// 自己的行军不走这种同步模式
		player.getInviewMarchs().clear();
		onPlayerMove(player, cmd.getX(), cmd.getY());

	}

	/**
	 * 离开世界地图
	 *
	 * @param session
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PLAYER_LEAVE_WORLD_VALUE)
	private boolean onPlayerLeaveWorld(CYBORGProtocol protocol) {
		ICYBORGPlayer player = protocol.getPlayer();
		player.getInviewMarchs().clear();
		player.setWorldMovePos(null);
		return true;
	}

	/**
	 * 玩家在世界地图滑动视野(移动)
	 *
	 * @param session
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PLAYER_WORLD_MOVE_VALUE)
	private boolean onPlayerWorldMove(CYBORGProtocol protocol) {
		ICYBORGPlayer player = protocol.getPlayer();
		if (player.isAnchor()) {
			return true;
		}
		PlayerWorldMove cmd = protocol.parseProtocol(PlayerWorldMove.getDefaultInstance());
		if (cmd.getSpeed() == 0.01f && moveCnt++ % 2 == 0) { // 快速滑动
			return true;
		}
		
		onPlayerMove(player, cmd.getX(), cmd.getY());
		return true;
	}

	/**
	 * 观察者视角切换
	 * 
	 * @param player
	 */
	public void onPlayerMove(ICYBORGPlayer player, int x, int y) {
		player.setWorldMovePos(new int[] { x, y });
		// 当前的
		MarchEventSync.Builder addbuilder = MarchEventSync.newBuilder();
		addbuilder.setEventType(MarchEvent.MARCH_ADD_VALUE);

		MarchEventSync.Builder delbuilder = MarchEventSync.newBuilder();
		delbuilder.setEventType(MarchEvent.MARCH_DELETE_VALUE);
		MarchSet currentSet = new MarchSet();
		for (ICYBORGWorldMarch march : player.getParent().getWorldMarchList()) {
			WorldMarchRelation relation = march.getRelation(player);
			if (relation.equals(WorldMarchRelation.SELF)) {
				continue;
			}
			boolean hasPush = player.getInviewMarchs().contains(march.getMarchId());
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

	// /** 手动进入房间 */
	// @ProtocolHandler(code = HP.code.CYBORG_JOIN_ROOM_REQ_VALUE)
	// private void onPlayerrJoinRoom(CYBORGProtocol protocol) {
	// ICYBORGPlayer player = protocol.getPlayer();
	// CYBORGJoinRoomMsg msg = CYBORGJoinRoomMsg.valueOf(player.getParent(),
	// player);
	// HawkApp.getInstance().postMsg(player.getXid(), msg);
	// player.responseSuccess(protocol.getType());
	// }

	/** 主动退出 */
	@ProtocolHandler(code = HP.code.CYBORG_QUIT_ROOM_REQ_VALUE)
	private void onPlayerQuitRoom(CYBORGProtocol protocol) {
		ICYBORGPlayer player = protocol.getPlayer();
		battleRoom.quitWorld(player, CYBORGQuitReason.LEAVE);
	}

	/**
	 * 城防信息请求
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.CITYDEF_REQ_C_VALUE)
	private boolean onCityDefReq(CYBORGProtocol protocol) {
		ICYBORGPlayer player = protocol.getPlayer();
		player.getPush().syncCityDef(false);
		return true;
	}

	/** 城墙灭火 */
	@ProtocolHandler(code = HP.code.BUILDING_OUTFIRE_C_VALUE)
	private boolean onOutFire(CYBORGProtocol protocol) {
		ICYBORGPlayer player = protocol.getPlayer();
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

		battleRoom.worldPointUpdate(player);
		return true;
	}

	/**
	 * 修复城防
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.BUILDING_REPAIR_C_VALUE)
	private boolean onCityDefRepair(CYBORGProtocol protocol) {
		ICYBORGPlayer player = protocol.getPlayer();
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
	private boolean onGetOtherPlayerEffect(CYBORGProtocol protocol) {
		ICYBORGPlayer player = protocol.getPlayer();
		OtherPlayerEffectReq req = protocol.parseProtocol(OtherPlayerEffectReq.getDefaultInstance());
		String playerId = req.getPlayerId();
		List<Integer> effectIds = req.getEffectIdList();
		if (HawkOSOperator.isEmptyString(playerId) || effectIds == null || effectIds.isEmpty()) {
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return false;
		}

		Player otherPlayer = CYBORGRoomManager.getInstance().makesurePlayer(playerId);
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
	private boolean onPlayerDetailOtherRequest(CYBORGProtocol protocol) {
		ICYBORGPlayer player = protocol.getPlayer();
		PlayerDetailReq req = protocol.parseProtocol(PlayerDetailReq.getDefaultInstance());
		String playerId = req.getPlayerId();
		// 快照数据
		Player snapshot = CYBORGRoomManager.getInstance().makesurePlayer(playerId);
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
	private void onReqWorldPointDetail(CYBORGProtocol protocol) {
		ICYBORGPlayer player = protocol.getPlayer();
		ReqWorldPointDetail req = protocol.parseProtocol(ReqWorldPointDetail.getDefaultInstance());
		ICYBORGWorldPoint worldPoint = battleRoom.getWorldPoint(req.getPointX(), req.getPointY()).orElse(null);
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
	protected boolean onGetGuildWarInfo(CYBORGProtocol protocol) {
		ICYBORGPlayer player = protocol.getPlayer();
		String viwerGuildId = player.getGuildId();

		HPGetGuildWarInfoResp.Builder resp = buildGuildWarInfo(viwerGuildId);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GET_GUILD_WAR_INFO_S_VALUE, resp));
		return true;
	}

	@ProtocolHandler(code = HP.code.GET_GUILD_WAR_CELL_INFO_C_VALUE)
	protected boolean onGetGuildWarCellInfo(CYBORGProtocol protocol) {
		ICYBORGPlayer player = protocol.getPlayer();
		HPGetGuildWarInfoReq req = protocol.parseProtocol(HPGetGuildWarInfoReq.getDefaultInstance());
		final String marchId = req.getMarchId();
		String viwerGuildId = player.getGuildId();
		// 回复协议
		HPGetGuildWarInfoResp.Builder resp = buildGuildWarCellInfo(marchId, viwerGuildId);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GET_GUILD_WAR_CELL_INFO_S_VALUE, resp));
		return true;
	}

	// 主播主播主播主播主播主播主播主播主播主播主播主播主播主播主播主播主播主播
	@ProtocolHandler(code = HP.code.CYBORG_ANCHOR_GET_GUILD_WAR_INFO_C_VALUE)
	protected boolean onAnchorGetGuildWarInfo(CYBORGProtocol protocol) {

		PBCYBORGAnchorGetGuildWarInfoReq req = protocol.parseProtocol(PBCYBORGAnchorGetGuildWarInfoReq.getDefaultInstance());
		ICYBORGPlayer player = protocol.getPlayer();
		if (!player.isAnchor()) {
			return false;
		}
		String viwerGuildId = getParent().getCampBase(CYBORG_CAMP.valueOf(req.getCamp())).campGuild;

		HPGetGuildWarInfoResp.Builder resp = buildGuildWarInfo(viwerGuildId);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_ANCHOR_GET_GUILD_WAR_INFO_S_VALUE, resp));
		return true;
	}

	@ProtocolHandler(code = HP.code.CYBORG_ANCHOR_GET_GUILD_WAR_CELL_INFO_C_VALUE)
	protected boolean onAnchorGetGuildWarCellInfo(CYBORGProtocol protocol) {
		ICYBORGPlayer player = protocol.getPlayer();
		if (!player.isAnchor()) {
			return false;
		}
		PBCYBORGAnchorGetGuildWarCellInfoReq req = protocol.parseProtocol(PBCYBORGAnchorGetGuildWarCellInfoReq.getDefaultInstance());
		final String marchId = req.getMarchId();
		String viwerGuildId = getParent().getCampBase(CYBORG_CAMP.valueOf(req.getCamp())).campGuild;
		// 回复协议
		HPGetGuildWarInfoResp.Builder resp = buildGuildWarCellInfo(marchId, viwerGuildId);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_ANCHOR_GET_GUILD_WAR_CELL_INFO_S_VALUE, resp));
		return true;
	}

	private HPGetGuildWarInfoResp.Builder buildGuildWarInfo(String viwerGuildId) {
		// 回复协议
		HPGetGuildWarInfoResp.Builder resp = HPGetGuildWarInfoResp.newBuilder();
		// 联盟行军
		List<ICYBORGWorldMarch> guildMarchs = battleRoom.getGuildWarMarch(viwerGuildId);
		for (ICYBORGWorldMarch guildMarch : guildMarchs) {
			try {
				resp.addGuildWar(guildMarch.getGuildWarShoPb(viwerGuildId));
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return resp;
	}

	private HPGetGuildWarInfoResp.Builder buildGuildWarCellInfo(final String marchId, String viwerGuildId) {
		HPGetGuildWarInfoResp.Builder resp = HPGetGuildWarInfoResp.newBuilder();
		ICYBORGWorldMarch guildMarch = getParent().getMarch(marchId);
		if (guildMarch != null) {
			boolean bfalse = guildMarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE || guildMarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE;
			if (bfalse) {
				resp.addGuildWar(guildMarch.getGuildWarShoPb(viwerGuildId));
			}
		}
		return resp;
	}

	/**
	 * 获取联盟士兵援助信息
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GUILD_ASSISTANT_INFO_C_VALUE)
	private boolean onGetGuildAssistantInfo(CYBORGProtocol protocol) {
		ICYBORGPlayer player = protocol.getPlayer();
		List<ICYBORGWorldMarch> marchList = player.assisReachMarches();
		HPGuildAssistantResp.Builder resp = HPGuildAssistantResp.newBuilder();
		int totalForces = 0;
		if (marchList != null) {
			Set<String> playerIdSet = new HashSet<String>();
			for (ICYBORGWorldMarch march : marchList) {
				playerIdSet.add(march.getPlayerId());
			}
			for (ICYBORGWorldMarch march : marchList) {
				HPAssistantMarchPB.Builder marchBuilder = HPAssistantMarchPB.newBuilder();
				String playerId = march.getPlayerId();
				marchBuilder.setUuid(march.getMarchId());
				marchBuilder.setPlayerId(playerId);

				ICYBORGPlayer assistPlayer = march.getParent();
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
