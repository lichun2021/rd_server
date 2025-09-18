package com.hawk.game.module.lianmengfgyl.battleroom.player.module;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.march.MarchSet;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLRoomManager;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLWorldPointService;
import com.hawk.game.module.lianmengfgyl.battleroom.IFGYLWorldPoint;
import com.hawk.game.module.lianmengfgyl.battleroom.msg.FGYLQuitReason;
import com.hawk.game.module.lianmengfgyl.battleroom.player.IFGYLPlayer;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.IFGYLWorldMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.IFGYLBuilding;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.hero.NPCHero;
import com.hawk.game.player.hero.NPCHeroFactory;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Army.ArmyHeroPB;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.GuildAssistant.HPAssistantMarchPB;
import com.hawk.game.protocol.GuildAssistant.HPGuildAssistantResp;
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
import com.hawk.game.protocol.World.FoggyDetailInfo;
import com.hawk.game.protocol.World.GetFoggyDetailInfo;
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
import com.hawk.game.protocol.World.WorldMoveCityReq;
import com.hawk.game.protocol.World.WorldMoveCityResp;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointSync;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.object.FoggyInfo;
import com.hawk.log.Action;

public class FGYLWorldModule extends PlayerModule {
	private IFGYLPlayer player;
	private int moveCnt;

	public FGYLWorldModule(IFGYLPlayer player) {
		super(player);
		this.player = player;
	}

	@ProtocolHandler(code = HP.code.SHARE_COORDINATE_C_VALUE)
	private boolean onShareCoordinate(HawkProtocol protocol) {

		ShareCoordinateReq req = protocol.parseProtocol(ShareCoordinateReq.getDefaultInstance());
		int posX = req.getPosX();
		int posY = req.getPosY();
		String pointName = req.getPointName();

		ChatParames parames = ChatParames.newBuilder().setChatType(Const.ChatType.CHAT_FUBEN_TEAM).setKey(Const.NoticeCfgId.FGYL_SHARECOORDINATE).setPlayer(player)
				.addParms(pointName, posX, posY).build();
		player.getParent().addWorldBroadcastMsg(parames);
		player.responseSuccess(protocol.getType());
		return true;
	}

	@ProtocolHandler(code = HP.code2.FGYL_SECOND_MAP_C_VALUE)
	private void getSecondMap(HawkProtocol protocol) {

		if (player.isAnchor()) {
			// player.getParent().getAnchorSecondMap(player);
		} else {
			player.getParent().getSecondMap(player);
		}
	}

	/**
	 * 获取超级武器驻军列表
	 */
	@ProtocolHandler(code = HP.code.SUPER_WEAPON_QUARTER_INFO_C_VALUE)
	private void getPresidentTowerQuarterInfo(HawkProtocol protocol) {

		SuperWeaponQuarterInfoReq req = protocol.parseProtocol(SuperWeaponQuarterInfoReq.getDefaultInstance());
		IFGYLBuilding weapon = (IFGYLBuilding) player.getParent().getWorldPoint(req.getPosX(), req.getPosY()).orElse(null);
		if (weapon == null) {
			return;
		}
		weapon.syncQuarterInfo(player);
		return;
	}

	@ProtocolHandler(code = HP.code2.FGYL_GAME_SYNC_C_VALUE)
	private boolean onGameSync(HawkProtocol protocol) {

		player.getParent().sync(player);
		return true;
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
		if (cmd.getSpeed() == 0.01f && moveCnt++ % 2 == 0) { // 快速滑动
			return true;
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
	 * 观察者视角切换
	 * 
	 * @param player
	 */
	public void onPlayerMove(IFGYLPlayer player, int x, int y) {
		// 当前的
		MarchEventSync.Builder addbuilder = MarchEventSync.newBuilder();
		addbuilder.setEventType(MarchEvent.MARCH_ADD_VALUE);

		MarchEventSync.Builder delbuilder = MarchEventSync.newBuilder();
		delbuilder.setEventType(MarchEvent.MARCH_DELETE_VALUE);
		MarchSet currentSet = new MarchSet();
		for (IFGYLWorldMarch march : player.getParent().getWorldMarchList()) {
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
	// @ProtocolHandler(code = HP.code.FGYL_JOIN_ROOM_REQ_VALUE)
	// private void onPlayerrJoinRoom(HawkProtocol protocol) {
	//
	// FGYLJoinRoomMsg msg = FGYLJoinRoomMsg.valueOf(player.player.getParent(),
	// player);
	// HawkApp.getInstance().postMsg(player.getXid(), msg);
	// player.responseSuccess(protocol.getType());
	// }

	/** 主动退出 */
	@ProtocolHandler(code = HP.code2.FGYL_QUIT_ROOM_REQ_VALUE)
	private void onPlayerQuitRoom(HawkProtocol protocol) {

		player.getParent().quitWorld(player, FGYLQuitReason.LEAVE);
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

		Player otherPlayer = FGYLRoomManager.getInstance().makesurePlayer(playerId);
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
		Player snapshot = FGYLRoomManager.getInstance().makesurePlayer(playerId);
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
		IFGYLWorldPoint worldPoint = player.getParent().getWorldPoint(req.getPointX(), req.getPointY()).orElse(null);
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

		HPGetGuildWarInfoResp.Builder resp = buildGuildWarInfo(player);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GET_GUILD_WAR_INFO_S_VALUE, resp));
		return true;
	}

	@ProtocolHandler(code = HP.code.GET_GUILD_WAR_CELL_INFO_C_VALUE)
	protected boolean onGetGuildWarCellInfo(HawkProtocol protocol) {

		HPGetGuildWarInfoReq req = protocol.parseProtocol(HPGetGuildWarInfoReq.getDefaultInstance());
		final String marchId = req.getMarchId();
		// 回复协议
		HPGetGuildWarInfoResp.Builder resp = buildGuildWarCellInfo(marchId, player);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GET_GUILD_WAR_CELL_INFO_S_VALUE, resp));
		return true;
	}

	private HPGetGuildWarInfoResp.Builder buildGuildWarInfo(IFGYLPlayer player) {
		// 回复协议
		HPGetGuildWarInfoResp.Builder resp = HPGetGuildWarInfoResp.newBuilder();
		// 联盟行军
		List<IFGYLWorldMarch> guildMarchs = player.getParent().getGuildWarMarch(player.getGuildId());
		for (IFGYLWorldMarch guildMarch : guildMarchs) {
			try {
				resp.addGuildWar(guildMarch.getGuildWarShoPb(player));
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return resp;
	}

	private HPGetGuildWarInfoResp.Builder buildGuildWarCellInfo(final String marchId, IFGYLPlayer player) {
		HPGetGuildWarInfoResp.Builder resp = HPGetGuildWarInfoResp.newBuilder();
		IFGYLWorldMarch guildMarch = player.getParent().getMarch(marchId);
		if (guildMarch != null) {
			boolean bfalse = guildMarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE || guildMarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE;
			if (bfalse) {
				resp.addGuildWar(guildMarch.getGuildWarShoPb(player));
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
	private boolean onGetGuildAssistantInfo(HawkProtocol protocol) {

		List<IFGYLWorldMarch> marchList = player.assisReachMarches();
		HPGuildAssistantResp.Builder resp = HPGuildAssistantResp.newBuilder();
		int totalForces = 0;
		if (marchList != null) {
			Set<String> playerIdSet = new HashSet<String>();
			for (IFGYLWorldMarch march : marchList) {
				playerIdSet.add(march.getPlayerId());
			}
			for (IFGYLWorldMarch march : marchList) {
				HPAssistantMarchPB.Builder marchBuilder = HPAssistantMarchPB.newBuilder();
				String playerId = march.getPlayerId();
				marchBuilder.setUuid(march.getMarchId());
				marchBuilder.setPlayerId(playerId);

				IFGYLPlayer assistPlayer = march.getParent();
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

	/**
	 * 玩家迁城
	 * 
	 * @param session
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.WORLD_MOVE_CITY_C_VALUE)
	private boolean onPlayerMoveCity(HawkProtocol protocol) {
		WorldMoveCityReq req = protocol.parseProtocol(WorldMoveCityReq.getDefaultInstance());

		// 迁城类型
		int moveCityType = 2;// req.getType();
		// 迁移前城点
		// IFGYLWorldPoint beforePoint = player;

		// 城点保护结束时间
		// long protectedEndTime = player.getParent().getStartTime();
		// 迁城消耗
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		int x = req.getX();
		int y = req.getY();

		// 迁城检测
		if (!moveCityCheck(player, protocol.getType(), req, consumeItems)) {
			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_INVALID);
			return false;
		}
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			return false;
		}
		// 目标点
		if (WorldUtil.isRandomMoveCity(moveCityType)) {
			// 随机迁城
			int[] xy = player.getParent().getWorldPointService().randomFreePoint(player.getParent().randomPoint(), player.getGridCnt());
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
			consumeItems.consumeAndPush(player, Action.SW_MOVE_CITY);
		}

		// 迁城成功
		if (player.getParent().getCurTimeMil() > player.getNextCityMoveTime()) {

			int moveCityCd = player.getParent().getCfg().getTeleportCooldown();
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
	
	private boolean moveCityCheck(IFGYLPlayer player, int hp, WorldMoveCityReq req, ConsumeItems consumeItems) {
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
	private boolean checkMoveCityConsume(IFGYLPlayer player, int hp, int type, ConsumeItems consumeItems, boolean forceMove) {
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
	private boolean randomMoveCityCheck(IFGYLPlayer player, boolean forceMove) {

		return false;
	}

	/**
	 * 定点迁城检测
	 * 
	 * @return
	 */
	private boolean selectMoveCityCheck(IFGYLPlayer player, int hp, WorldMoveCityReq req) {
		if (player.getX() == req.getX() && player.getY() == req.getY()) {
			return true;
		}

		FGYLWorldPointService worldPointService = player.getParent().getWorldPointService();
		if (!worldPointService.tryOccupied(req.getX(), req.getY(), player.getGridCnt())) {
			worldPointService.addToAreaFreePoint(player);
			if (!worldPointService.tryOccupied(req.getX(), req.getY(), player.getGridCnt())) {
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
	private boolean guildMoveCityCheck(IFGYLPlayer player) {
		return false;
	}

	/**
	 * 获取迷雾要塞敌军信息
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.FOGGY_DETAIL_INFO_REQ_C_VALUE)
	private boolean onGetFoggyDetailInfo(HawkProtocol protocol) {
		GetFoggyDetailInfo req = protocol.parseProtocol(GetFoggyDetailInfo.getDefaultInstance());
		int x = req.getX();
		int y = req.getY();

		Optional<IFGYLWorldPoint> worldPointOP = player.getParent().getWorldPoint(x, y);
		if (!worldPointOP.isPresent()) {
			player.sendError(protocol.getType(), Status.Error.POINT_NOT_EXIST_OR_TYPE_ERROR);
			return false;
		}
		FoggyInfo info = null;
		if (worldPointOP.get() instanceof IFGYLBuilding) {
			IFGYLBuilding worldPoint = (IFGYLBuilding) worldPointOP.get();
			info = worldPoint.getFoggyInfoObj();
		}

		FoggyDetailInfo.Builder builder = FoggyDetailInfo.newBuilder();
		if (Objects.nonNull(info)) {
			// 组装部队信息
			List<ArmyInfo> armylist = info.getArmyList();
			for (ArmyInfo armyInfo : armylist) {
				builder.addArmyInfo(armyInfo.toArmySoldierPB(info.getNpcPlayer()));
			}
			// 组装英雄信息
			List<Integer> heroInfoIds = info.getHeroIds();
			for (Integer heroInfoId : heroInfoIds) {
				NPCHero npcHero = NPCHeroFactory.getInstance().get(heroInfoId);
				builder.addHeros(npcHero.toPBobj());
			}
		}
		// 发送给客户端
		player.sendProtocol(HawkProtocol.valueOf(HP.code.FOGGY_DETAIL_INFO_RESP_S_VALUE, builder));
		return true;
	}
}
