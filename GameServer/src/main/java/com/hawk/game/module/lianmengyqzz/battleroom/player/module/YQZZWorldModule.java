package com.hawk.game.module.lianmengyqzz.battleroom.player.module;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.hawk.game.player.manhattan.PlayerManhattanModule;
import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.FoggyFortressCfg;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.ShopCfg;
import com.hawk.game.config.VipCfg;
import com.hawk.game.config.WorldEnemyCfg;
import com.hawk.game.config.WorldMapConstProperty;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.march.AutoMonsterMarchParam;
import com.hawk.game.march.AutoMonsterMarchParam.AutoMarchInfo;
import com.hawk.game.march.MarchSet;
import com.hawk.game.module.PlayerDressModule;
import com.hawk.game.module.lianmengyqzz.battleroom.IYQZZWorldPoint;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZMapBlock;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZRoomManager;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZWorldPointService;
import com.hawk.game.module.lianmengyqzz.battleroom.msg.YQZZQuitReason;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.IYQZZWorldMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.YQZZAttackMonsterMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.IYQZZBuilding;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZBase;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZBuildType;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZFoggyFortress;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZMonster;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZPylon;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZResource;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;
import com.hawk.game.msg.AutoSearchMonsterMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.player.hero.NPCHero;
import com.hawk.game.player.hero.NPCHeroFactory;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Army.ArmyHeroPB;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.GuildAssistant.HPAssistantMarchPB;
import com.hawk.game.protocol.GuildAssistant.HPGuildAssistantResp;
import com.hawk.game.protocol.GuildManager.GetGuildAssistenceInfoReq;
import com.hawk.game.protocol.GuildManager.GetGuildAssistenceInfoResp;
import com.hawk.game.protocol.GuildWar.HPGetGuildWarInfoReq;
import com.hawk.game.protocol.GuildWar.HPGetGuildWarInfoResp;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Player.EffectPB;
import com.hawk.game.protocol.Player.HPPlayerEffectSync;
import com.hawk.game.protocol.Player.OtherPlayerDetailResp;
import com.hawk.game.protocol.Player.OtherPlayerEffectReq;
import com.hawk.game.protocol.Player.PlayerDetailReq;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SuperWeapon.SuperWeaponQuarterInfoReq;
import com.hawk.game.protocol.Talent.TalentType;
import com.hawk.game.protocol.World.AutoMarchPB;
import com.hawk.game.protocol.World.CityMoveType;
import com.hawk.game.protocol.World.FoggyDetailInfo;
import com.hawk.game.protocol.World.GetFoggyDetailInfo;
import com.hawk.game.protocol.World.MarchData;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.MarchEventSync;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.game.protocol.World.PlayerEnterWorld;
import com.hawk.game.protocol.World.PlayerWorldMove;
import com.hawk.game.protocol.World.ReqWorldPointDetail;
import com.hawk.game.protocol.World.RespWorldPointDetail;
import com.hawk.game.protocol.World.SearchType;
import com.hawk.game.protocol.World.ShareCoordinateReq;
import com.hawk.game.protocol.World.SwitchAtkMonsterAutoMarchReq;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldMoveCityReq;
import com.hawk.game.protocol.World.WorldMoveCityResp;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointSync;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.protocol.World.WorldSearchReq;
import com.hawk.game.protocol.World.WorldSearchResp;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventGenOldMonsterMarch;
import com.hawk.game.util.AlgorithmPoint;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.AutoSearchMonsterResultCode;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.object.FoggyInfo;
import com.hawk.log.Action;
import com.hawk.log.Source;

public class YQZZWorldModule extends PlayerModule {
	private IYQZZPlayer player;
	private int moveCnt;
	public YQZZWorldModule(IYQZZPlayer player) {
		super(player);
		this.player = player;
	}

	@ProtocolHandler(code = HP.code.SHARE_COORDINATE_C_VALUE)
	private boolean onShareCoordinate(HawkProtocol protocol) {
		ShareCoordinateReq req = protocol.parseProtocol(ShareCoordinateReq.getDefaultInstance());
		int posX = req.getPosX();
		int posY = req.getPosY();
		String pointName = req.getPointName();

		ChatParames parames = ChatParames.newBuilder().setChatType(Const.ChatType.CHAT_FUBEN_TEAM).setKey(Const.NoticeCfgId.YQZZ_SHARECOORDINATE).setPlayer(player)
				.addParms(pointName, posX, posY).build();
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

		player.worldPointUpdate();
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

		player.worldPointUpdate();
	}

	@ProtocolHandler(code = HP.code2.YQZZ_SECOND_MAP_C_VALUE)
	private void getSecondMap(HawkProtocol protocol) {
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_SECOND_MAP_S, player.getParent().getSecondMap(player.getGuildId())));
	}

	/**
	 * 帮助联盟玩家被援助的信息
	 */
	@ProtocolHandler(code = HP.code.GUILD_MEMBER_ASSISTENCE_INFO_C_VALUE)
	private boolean onGetGuildAssistenceInfo(HawkProtocol protocol) {
		GetGuildAssistenceInfoReq req = protocol.parseProtocol(GetGuildAssistenceInfoReq.getDefaultInstance());
		IYQZZPlayer tarPlayer = player.getParent().getPlayer(req.getPlayerId());
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
				WorldMarchType.ASSISTANCE).stream().map(IYQZZWorldMarch::getMarchEntity).collect(Collectors.toList());
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
		IYQZZBuilding weapon = (IYQZZBuilding) player.getParent().getWorldPoint(req.getPosX(), req.getPosY()).orElse(null);
		if (weapon == null) {
			return;
		}
		weapon.syncQuarterInfo(player);
		return;
	}

	@ProtocolHandler(code = HP.code2.YQZZ_GAME_SYNC_C_VALUE)
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
		if (player.getParent().getGameStartTime() > HawkTime.getMillisecond()) {
			player.sendError(protocol.getType(), Status.Error.TBLY_PRETIME_CANNOT_MOVE);
			return false;
		}
		WorldMoveCityReq req = protocol.parseProtocol(WorldMoveCityReq.getDefaultInstance());

		// 迁城类型
		int moveCityType = 2;// req.getType();
		// 迁移前城点
		// IYQZZWorldPoint beforePoint = player;

		// 城点保护结束时间
		// long protectedEndTime = player.getParent().getStartTime();
		// 迁城消耗
		ConsumeItems consumeItems = ConsumeItems.valueOf();
		int x = req.getX();
		int y = req.getY();

		int tarScirl = YQZZMapBlock.getInstance().circleNumber(GameUtil.combineXAndY(x, y));
		int circle = YQZZMapBlock.getInstance().circleNumber(player.getPointId());
		if (tarScirl != circle) { // 不同圈层
			YQZZBase baseByCamp = player.getParent().getBaseByCamp(player.getCamp());
			if (!baseByCamp.controlBuildTypes.contains(YQZZBuildType.JIU)) {
				player.sendError(protocol.getType(), Status.YQZZError.YQZZ_NO_TYPE9_BUILD);
				return false;
			}
		}

		// 迁城检测
		if (!moveCityCheck(player, protocol.getType(), req, consumeItems)) {
			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_INVALID);
			return false;
		}
		if (!consumeItems.checkConsume(player, protocol.getType())) {
			player.sendError(protocol.getType(), Status.Error.WORLD_POINT_INVALID);
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

	private boolean moveCityCheck(IYQZZPlayer player, int hp, WorldMoveCityReq req, ConsumeItems consumeItems) {
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
	private boolean checkMoveCityConsume(IYQZZPlayer player, int hp, int type, ConsumeItems consumeItems, boolean forceMove) {
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
	private boolean randomMoveCityCheck(IYQZZPlayer player, boolean forceMove) {
		// 自己有出征队伍
		if (!forceMove && player.getParent().getPlayerMarchCount(player.getId()) > 0) {
			player.sendError(HP.code.WORLD_MOVE_CITY_C_VALUE, Status.Error.HAS_MARCH_IN_WORLD);
			return false;
		}

		// 是否被攻击
		List<IYQZZWorldMarch> marchList = player.getParent().getPointMarches(player.getPointId(), WorldMarchStatus.MARCH_STATUS_MARCH, WorldMarchType.ATTACK_PLAYER);
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
	private boolean selectMoveCityCheck(IYQZZPlayer player, int hp, WorldMoveCityReq req) {
		if (player.getX() == req.getX() && player.getY() == req.getY()) {
			return true;
		}

		YQZZWorldPointService worldPointService = player.getParent().getWorldPointService();
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
	private boolean guildMoveCityCheck(IYQZZPlayer player) {
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
	 * 离开世界地图
	 *
	 * @param session
	 * @param protocol
	 */
	@ProtocolHandler(code = HP.code.PLAYER_LEAVE_WORLD_VALUE)
	private boolean onPlayerLeaveWorld(HawkProtocol protocol) {

		player.getParent().getWorldPointService().getWorldScene().leave(player.getEye().getAoiObjId());

		player.getInviewMarchs().clear();
		player.getPush().syncPlayerEffect(EffType.CITY_HURT_NUM, EffType.PLANT_SOLDIER_4101);

		return true;
	}

	/**
	 * 观察者视角切换
	 * 
	 * @param player
	 */
	public void onPlayerMove(IYQZZPlayer player, int x, int y) {
		// 当前的

		MarchEventSync.Builder addbuilder = MarchEventSync.newBuilder();
		addbuilder.setEventType(MarchEvent.MARCH_ADD_VALUE);

		MarchEventSync.Builder delbuilder = MarchEventSync.newBuilder();
		delbuilder.setEventType(MarchEvent.MARCH_DELETE_VALUE);
		MarchSet currentSet = new MarchSet();
		for (IYQZZWorldMarch march : player.getParent().getWorldMarchList()) {
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

	/** 主动退出 */
	@ProtocolHandler(code = HP.code2.YQZZ_QUIT_ROOM_REQ_VALUE)
	private void onPlayerQuitRoom(HawkProtocol protocol) {
		if (!YQZZMatchService.getInstance().isOperateTime()) {
			player.sendError(protocol.getType(), Status.YQZZError.YQZZ_UNOPERATE_TIME_VALUE, 0);
			return;
		}

		if (player.getParent().getPlayerMarchCount(player.getId()) > 0) {
			player.sendError(protocol.getType(), Status.YQZZError.YQZZ_ROOM_PLYAER_MARCH);
			return;
		}
		if (player.assisReachMarches().size() > 0) {
			player.sendError(protocol.getType(), Status.YQZZError.YQZZ_ROOM_HAS_ASSISTANCE_MARCH);
			return;
		}
		if (player.getParent().getPointMarches(player.getPointId()).size() > 0) {
			player.sendError(protocol.getType(), Status.YQZZError.YQZZ_ROOM_HAS_PASSIVE_MARCH);
			return;
		}

		player.getParent().quitWorld(player, YQZZQuitReason.LEAVE);
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

		player.worldPointUpdate();
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

		Player otherPlayer = YQZZRoomManager.getInstance().makesurePlayer(playerId);
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
		Player snapshot = YQZZRoomManager.getInstance().makesurePlayer(playerId);
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
		IYQZZWorldPoint worldPoint = player.getParent().getWorldPoint(req.getPointX(), req.getPointY()).orElse(null);
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
		List<IYQZZWorldMarch> guildMarchs = player.getParent().getGuildWarMarch(player.getGuildId());
		for (IYQZZWorldMarch guildMarch : guildMarchs) {
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
		IYQZZWorldMarch guildMarch = player.getParent().getMarch(marchId);
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
		List<IYQZZWorldMarch> marchList = player.assisReachMarches();
		HPGuildAssistantResp.Builder resp = HPGuildAssistantResp.newBuilder();
		int totalForces = 0;
		if (marchList != null) {
			Set<String> playerIdSet = new HashSet<String>();
			for (IYQZZWorldMarch march : marchList) {
				playerIdSet.add(march.getPlayerId());
			}
			for (IYQZZWorldMarch march : marchList) {
				HPAssistantMarchPB.Builder marchBuilder = HPAssistantMarchPB.newBuilder();
				String playerId = march.getPlayerId();
				marchBuilder.setUuid(march.getMarchId());
				marchBuilder.setPlayerId(playerId);

				IYQZZPlayer assistPlayer = march.getParent();
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
	 * 获取迷雾要塞敌军信息
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.FOGGY_DETAIL_INFO_REQ_C_VALUE)
	private boolean onGetFoggyDetailInfo(HawkProtocol protocol) {
		GetFoggyDetailInfo req = protocol.parseProtocol(GetFoggyDetailInfo.getDefaultInstance());
		int x = req.getX();
		int y = req.getY();

		Optional<IYQZZWorldPoint> worldPointOP = player.getParent().getWorldPoint(x, y);
		if (!worldPointOP.isPresent()) {
			player.sendError(protocol.getType(), Status.Error.POINT_NOT_EXIST_OR_TYPE_ERROR);
			return false;
		}
		FoggyInfo info = null;
		if (worldPointOP.get() instanceof YQZZFoggyFortress) {
			YQZZFoggyFortress worldPoint = (YQZZFoggyFortress) worldPointOP.get();
			info = worldPoint.getFoggyInfoObj();
		}
		if (worldPointOP.get() instanceof IYQZZBuilding) {
			IYQZZBuilding worldPoint = (IYQZZBuilding) worldPointOP.get();
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
	
	/**
	 * 世界地图搜索功能
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.WORLD_SEARCH_C_VALUE)
	private boolean onWorldSearch(HawkProtocol protocol) {
		WorldSearchReq req = protocol.parseProtocol(WorldSearchReq.getDefaultInstance());
//		//搜索野怪1 2 特殊处理,,小范围先搜,搜不到走生成的逻辑
//		boolean result = searchFreePointForCreateMonster(protocol);
//		if (result){
//			return true;
//		}
		//搜怪
		WorldSearchResp.Builder builder = worldSearch(protocol);
		if (builder != null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_SEARCH_S_VALUE, builder));
		} else {
			WorldSearchResp.Builder resp = WorldSearchResp.newBuilder();
			resp.setTargetX(0);
			resp.setTargetY(0);
			resp.setSuccess(false);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_SEARCH_S_VALUE, resp));
		}
		return true;
	}
	
	private WorldSearchResp.Builder worldSearch(HawkProtocol protocol) {
		WorldSearchReq req = protocol.parseProtocol(WorldSearchReq.getDefaultInstance());
		SearchType type = req.getType();
		// 第n次查找
		int index = req.getIndex();
		
		int[] pos = player.getPos();
		IYQZZWorldPoint worldPoint = null;
		switch (type.getNumber()) {
		case SearchType.SEARCH_RESOURCE_VALUE:
			worldPoint = getTargetPoint(pos, WorldPointType.RESOURCE_VALUE, req.getId(), index, type.getNumber());
			break;

		case SearchType.SEARCH_MONSTER_VALUE:
			worldPoint = getTargetPoint(pos, WorldPointType.MONSTER_VALUE, req.getLevel(), index, type.getNumber());
			break;

		case SearchType.SEARCH_BOX_VALUE:
			worldPoint = getTargetPoint(pos, WorldPointType.BOX_VALUE, req.getId(), index, type.getNumber());
			break;
		
		case SearchType.SEARCH_YURI_FACTORY_VALUE:
			worldPoint = getTargetPoint(pos, WorldPointType.YURI_FACTORY_VALUE, req.getId(), index, type.getNumber());
			break;
		
		case SearchType.SEARCH_STRONGPOINT_VALUE:
			worldPoint = getTargetPoint(pos, WorldPointType.STRONG_POINT_VALUE, req.getId(), index, type.getNumber());
			break;
			
		case SearchType.SEARCH_FOGGY_VALUE:
			worldPoint = getTargetPoint(pos, WorldPointType.FOGGY_FORTRESS_VALUE, req.getLevel(), index, type.getNumber());
			break;
		case SearchType.SEARCH_NEW_MONSTER_VALUE:
			worldPoint = getTargetPoint(pos, WorldPointType.MONSTER_VALUE, req.getLevel(), index, type.getNumber());
			break;
		case SearchType.SEARCH_YURI_MONSTER_VALUE:
			worldPoint = getTargetPoint(pos, WorldPointType.MONSTER_VALUE, req.getLevel(), index, type.getNumber());
			break;
//		case SearchType.SEARCH_SNOWBALL_VALUE:
//			worldPoint = WorldSnowballService.getInstance().searchSnowball(pos, index);
//			break;
//		case SearchType.SEARCH_EMPTY_POINT_VALUE:
//			WorldMapConstProperty worldMapConstProperty = WorldMapConstProperty.getInstance();
//			int minDis = worldMapConstProperty.getMinStoryMonsterSerachRadius();
//			int maxDis = worldMapConstProperty.getMaxStoryMonsterSearchRadius();
//			worldPoint = searchFreePoint(pos, minDis, maxDis);
//			break;
//		case SearchType.SEARCH_GUNDAM_VALUE:
//			worldPoint = searchGundamPoint(pos, index);
//			break;
//		case SearchType.SEARCH_NIAN_VALUE:
//			worldPoint = searchNianPoint(pos, index);
//			break;
		case SearchType.SEARCH_PYLON_VALUE:
			worldPoint = getTargetPoint(pos, WorldPointType.PYLON_VALUE, req.getId(), index, type.getNumber());
			break;
//		case SearchType.SEARCH_CHRISTMAS_BOSS_VALUE:
//			worldPoint = searchChristmasBoss(pos, index);
//			break;
//		case SearchType.SEARCH_CAKE_VALUE:
//			worldPoint = searchCakePoint();
//			break;
		default:
			player.sendError(protocol.getType(), Status.SysError.PARAMS_INVALID);
			return null;
		}

//		logger.info("world search, type:{}, reqId:{}, manorType:{}, worldPoint:{}", type, req.getId(), req.getManorType(), worldPoint);

		// 没有找到合适的点
		if (worldPoint == null) {
			if (type == SearchType.SEARCH_GUILD_MANOR) {
				sendError(protocol.getType(), Status.Error.WORLD_SEARCH_NO_MANOR);
			} else if (type == SearchType.SEARCH_SNOWBALL) {
				sendError(protocol.getType(), Status.Error.SEARCH_SNOWBALL_NULL);
			} else {
				sendError(protocol.getType(), Status.Error.WORLD_SEARCH_NO_TARGET);
			}
			return null;
		}

		// 回复
		WorldSearchResp.Builder resp = WorldSearchResp.newBuilder();
		resp.setTargetX(worldPoint.getX());
		resp.setTargetY(worldPoint.getY());
		resp.setType(type);
		resp.setSuccess(true);
		return resp;
	}
	
	private IYQZZWorldPoint getTargetPoint(int[] pos, int pointType, int id, int index, int searchType) {
		IYQZZWorldPoint worldPoint = getTargetPoint(pos, pointType, id, index, searchType, 0);
		return worldPoint;
	}
	
	/**
	 * 找到目标点的内容
	 * 
	 * @param pos
	 *            目标位置
	 * @param pointType
	 *            点类型
	 * @param id
	 *            怪物ID或者资源ID
	 * @return
	 */
	private IYQZZWorldPoint getTargetPoint(int[] pos, int pointType, int id, int index, int searchType, int minDistance) {
		index = index < 0 ? 0 : index;
		
		// 距离判断
		int[] disArr = WorldMapConstProperty.getInstance().getWorldSearchRadius();
		int dis = disArr[disArr.length - 1];
		if (minDistance > 0){
			dis = minDistance;
		}
		// 中心点
		AlgorithmPoint centerPoint = new AlgorithmPoint(pos[0], pos[1]);
		
		// 查找到的点集合
		TreeSet<IYQZZWorldPoint> searchPoints = new TreeSet<>(new Comparator<IYQZZWorldPoint>() {
			@Override
			public int compare(IYQZZWorldPoint o1, IYQZZWorldPoint o2) {
				double distance1 = centerPoint.distanceTo(new AlgorithmPoint(o1.getX(), o1.getY()));
				double distance2 = centerPoint.distanceTo(new AlgorithmPoint(o2.getX(), o2.getY()));
				return distance1 >= distance2 ? 1 : -1;
			}
		});
		
//		// 野怪快速查找
//		if (pointType == WorldPointType.MONSTER_VALUE
//				&& (searchType == SearchType.SEARCH_MONSTER_VALUE || searchType == SearchType.SEARCH_YURI_MONSTER_VALUE)
//				&& id == 0) {
//			return monsterFastSearch(pos, pointType, dis, searchPoints);
//		}
		
		List<Integer> marchPoint = new ArrayList<>();
		List<IYQZZWorldMarch> playerMarch = player.getParent().getPlayerMarches(player.getId());
		for (IWorldMarch march : playerMarch) {
			marchPoint.add(march.getOrigionId());
			marchPoint.add(march.getTerminalId());
		}
		
		List<IYQZZWorldPoint> points = player.getParent().getWorldPointService().getAroundWorldPointsWithType(pos[0], pos[1], dis, dis, pointType);
		for (IYQZZWorldPoint point : points) {
			if (point.getPointType().getNumber() != pointType) {
				continue;
			}
			int tarScirl = YQZZMapBlock.getInstance().circleNumber(point.getPointId());
			int circle = YQZZMapBlock.getInstance().circleNumber(player.getPointId());
			if (tarScirl != circle) { // 不同圈层
				continue;
			}
			
			if (marchPoint.contains(point.getPointId())) {
				continue;
			}
			
			// 尤里工厂
			if (pointType == WorldPointType.YURI_FACTORY_VALUE) {
				searchPoints.add(point);
			}
			// 迷雾要赛
			if (pointType == WorldPointType.FOGGY_FORTRESS_VALUE) {
				YQZZFoggyFortress foggy = (YQZZFoggyFortress) point;
				FoggyFortressCfg foggyCfg = foggy.getFoggyFortressCfg();
				if (foggyCfg != null && foggyCfg.getLevel() == id) {
					searchPoints.add(point);
				}
			}
//			// 宝箱
//			if (pointType == WorldPointType.BOX_VALUE && point.getMonsterId() == id) {
//				searchPoints.add(point);
//			}
			// 据点
//			if (pointType == WorldPointType.STRONG_POINT_VALUE && point.getMonsterId() == id) {
//				searchPoints.add(point);
//			}
			// 野怪
			if ((pointType == WorldPointType.MONSTER_VALUE) && (searchType == SearchType.SEARCH_MONSTER_VALUE)) {
				YQZZMonster monster = (YQZZMonster) point;
				WorldEnemyCfg monsterCfg = monster.getWorldEnemyCfg();// HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, point.getMonsterId());
				if (monsterCfg != null && monsterCfg.getType() == MonsterType.TYPE_1_VALUE && monsterCfg.getLevel() == id) {
					searchPoints.add(point);
				}
			}
//			// 叛军野怪
//			if ((pointType == WorldPointType.MONSTER_VALUE) && (searchType == SearchType.SEARCH_YURI_MONSTER_VALUE)) {
//				WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, point.getMonsterId());
//				if (monsterCfg != null && monsterCfg.getType() == MonsterType.TYPE_2_VALUE && monsterCfg.getLevel() == id) {
//					searchPoints.add(point);
//				}
//			}
//			// 新版野怪
//			if ((pointType == WorldPointType.MONSTER_VALUE) && (searchType == SearchType.SEARCH_NEW_MONSTER_VALUE) && (point.getCityLevel() == id)) {
//				WorldEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(WorldEnemyCfg.class, point.getMonsterId());
//				if (monsterCfg != null && monsterCfg.getType() == MonsterType.TYPE_7_VALUE ) {
//					searchPoints.add(point);
//				}
//			}
			// 资源点
			if (pointType == WorldPointType.RESOURCE_VALUE) {
				YQZZResource res = (YQZZResource) point;
				if (res.getMarch() != null) {
					continue;
				}
				if(id == res.getResourceId()){
					searchPoints.add(point);
				}
			}
			// 能量塔
			if (pointType == WorldPointType.PYLON_VALUE ) {
				YQZZPylon res = (YQZZPylon) point;
				if (res.getCollectMarch() != null) {
					continue;
				}
				if(id == res.getResourceId()){
					searchPoints.add(point);
				}
			}
//			// 雪球
//			if (pointType == WorldPointType.SNOWBALL_VALUE) {
//				searchPoints.add(point);
//			}
		}
		
		if (searchPoints.isEmpty()) {
			return null;
		}
		
		List<IYQZZWorldPoint> searchPointList = new ArrayList<>(searchPoints.size());
		searchPointList.addAll(searchPoints);
		return searchPointList.get(index % searchPointList.size());
	}
	
	
	/**
	 * 开启自动打野行军
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.SWITCH_ATK_MONSTER_AUTO_MARCH_C_VALUE)
	private boolean onSwitchAtkMonsterAutoMarch(HawkProtocol protocol) {
		SwitchAtkMonsterAutoMarchReq req = protocol.parseProtocol(SwitchAtkMonsterAutoMarchReq.getDefaultInstance());
		// 关闭自动打野
		if (req.getIsOpen() == 0) {
			WorldMarchService.getInstance().breakAutoMarch(player, 0);
			LogUtil.logAutoMonsterSwitch(player, false);
			HawkLog.logPrintln("AtkMonsterAutoMarch close, playerId: {}", player.getId());
			return true;
		}
		if (isMonsterLimit()) {
			player.sendError(protocol.getType(), Status.YQZZError.YQZZ_MONSTER_LIMIT_VALUE, 0);
			return false;
		}

//		PlayerAutoModule atuoModel = player.getModule(AUTO_GATHER);
//		if( atuoModel.isAutoPut() ){
//			WorldMarchService.getInstance().breakAutoMarch(player, 0);
//			LogUtil.logAutoMonsterSwitch(player, false);
//			HawkLog.logPrintln("AtkMonsterAutoMarch close, auto put is on, playerId: {}", player.getId());
//			sendError(protocol.getType(), AutoGatherErr.AUTO_GATHER_RESOURCE_VALUE);
//			return true;
//		}

		StatusDataEntity statusEntity = player.getData().getStatusById(EffType.AUTO_ATK_MONSTER_VALUE);
		long timeNow = HawkTime.getMillisecond();
		if (statusEntity == null || statusEntity.getEndTime() < timeNow) {
			sendError(protocol.getType(), Status.Error.AUTO_ATK_MONSTER_NOT_FUNCTION_VALUE); 
			return false;
		}
		
		int autoMarchCount = 1, vipLevel = player.getVipLevel();
		VipCfg vipCfg = HawkConfigManager.getInstance().getConfigByKey(VipCfg.class, vipLevel);
		if (vipCfg != null) {
			autoMarchCount += vipCfg.getAutoFightQueue();
			autoMarchCount = Math.min(autoMarchCount, player.getMaxMarchNum());
		} else {
			HawkLog.logPrintln("OpenAtkMonsterAutoMarch exception, vipCfg error, playerId: {}, vipLevel: {}", player.getId(), vipLevel);
		}
		
		// 判断是否允许开启自动打野
		if (autoMarchCount <= 0) {
			sendError(protocol.getType(), Status.Error.AUTO_ATK_MONSTER_NOT_FUNCTION_VALUE); 
			return false;
		}
		
		List<AutoMarchPB> autoMarchPBList = req.getMarchInfoList();
		if (autoMarchPBList.isEmpty() || autoMarchPBList.size() > autoMarchCount) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, marchCount error, playerId: {}, req count: {}, server count: {}", player.getId(), autoMarchPBList.size(), autoMarchCount);
			return false;
		}
		
		int minLevel = req.getMinLevel();
		int maxLevel = req.getMaxLevel();
		List<SearchType> searchTypeList = req.getSearchTypeList();
		
		int maxLevelKilled = 1000;// player.getData().getMonsterEntity().getMaxLevel();
		if (minLevel <= 0 || minLevel > maxLevel || maxLevel > maxLevelKilled + 1 || searchTypeList.isEmpty()) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, playerId: {}, minLevel: {}, maxLevel: {}, maxLevelKilled: {}, searchType empty: {}", player.getId(), minLevel, maxLevel, maxLevelKilled, searchTypeList.isEmpty());
			return false;
		}
		
		for (SearchType type : searchTypeList) {
			if (!GsConst.SEARCH_MONSTER_AUTO_ORDER.contains(type)) {
				sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
				HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, playerId: {}, searchType: {}", player.getId(), type);
				return false;
			}
		}
		
		AutoMonsterMarchParam autoMarchParam = WorldMarchService.getInstance().getAutoMarchParam(player.getId());
		List<AutoMarchInfo> autoMarchSetInfoList = new ArrayList<AutoMarchInfo>();
		List<IYQZZWorldMarch> marchList = player.getParent().getAutoMonsterMarch(player.getId());
		
		for (AutoMarchPB info : autoMarchPBList) {
			if (!autoMonterCheck(protocol, autoMarchParam, info, autoMarchSetInfoList, marchList)) {
				return false;
			}
		}
		
		List<Integer> searchType = new ArrayList<Integer>();
		for (SearchType type : GsConst.SEARCH_MONSTER_AUTO_ORDER) {
			if (searchTypeList.contains(type)) {
				searchType.add(type.getNumber());
			}
		}
		
		String paramBefore = "";
		if (autoMarchParam == null) {
			autoMarchParam = new AutoMonsterMarchParam();
			WorldMarchService.getInstance().addAutoMarchParam(player.getId(), autoMarchParam);
		} else {
			paramBefore = autoMarchParam.toString();
		}
		
		autoMarchParam.setMaxLevel(maxLevel);
		autoMarchParam.setMinLevel(minLevel);
		autoMarchParam.setSearchType(searchType);
		autoMarchParam.addAutoMarchInfo(autoMarchSetInfoList);

		// 同步状态信息
		WorldMarchService.getInstance().pushAutoMarchStatus(player, 1);
		
		for (int i = 0; i < autoMarchPBList.size(); i++) {
			try {
				searchMonsterAuto();
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		StringJoiner sj = new StringJoiner(",");
		for (AutoMarchInfo info : autoMarchSetInfoList) {
			sj.add(String.valueOf(info.getTroops()));
		}
		// 打点日志
		LogUtil.logAutoMonsterSwitch(player, true, minLevel, maxLevel, autoMarchPBList.size(), sj.toString());
		
		HawkLog.logPrintln("OpenAtkMonsterAutoMarch success, playerId: {}, paramBefore: {}, paramsAfter: {}", player.getId(), paramBefore, autoMarchParam);
		
		return true;
	}
	
	/**
	 * 自动打野条件检测
	 * 
	 * @param protocol
	 * @param autoMarchParam    原有的自动打野行军参数
	 * @param autoMarchPB  客户端传的自动打野行军参数
	 * @param autoMarchSetInfos 已校验通过的自动打野行军队列信息
	 * @param marchList    已出征的自动打野行军队列
	 * @return
	 */
	private boolean autoMonterCheck(HawkProtocol protocol, AutoMonsterMarchParam autoMarchParam, AutoMarchPB autoMarchPB, 
			List<AutoMarchInfo> autoMarchSetInfos, List<IYQZZWorldMarch> marchList) {
		List<ArmySoldierPB> armyList = autoMarchPB.getArmyList();
		List<Integer> heroIds = autoMarchPB.getHeroIdsList();
		int superSoldierId = autoMarchPB.getSuperSoldierId();
		ArmourSuitType armourSuit = autoMarchPB.getArmourSuit();
		
		EffectParams effParams = new EffectParams();
		effParams.setHeroIds(heroIds);
		effParams.setArmourSuit(armourSuit);
		effParams.setMechacoreSuit(autoMarchPB.getMechacoreSuit());
		effParams.setSuperSoliderId(superSoldierId);
		TalentType talentType = autoMarchPB.getTalentType();
		effParams.setTalent(talentType != null ? talentType.getNumber() : TalentType.TALENT_TYPE_DEFAULT_VALUE);
		effParams.setSuperLab(autoMarchPB.getSuperLab());
		
		if (autoMarchPB.hasSuperLab() && autoMarchPB.getSuperLab() != 0 && !player.isSuperLabActive(autoMarchPB.getSuperLab())) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, super lab not active, playerId: {}, superLab: {}", player.getId(), autoMarchPB.getSuperLab());
			return false;
		}
		if (autoMarchPB.getManhattan().getManhattanAtkSwId() > 0 || autoMarchPB.getManhattan().getManhattanDefSwId() > 0) {
			// 检测超武信息,判断是否解锁，
			PlayerManhattanModule manhattanModule = player.getModule(GsConst.ModuleType.MANHATTAN);
			if (!manhattanModule.checkMarchReq(autoMarchPB.getManhattan())) {
				sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
				HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, manhattan not active, playerId: {}, swAtkId: {}, swDeId: {}", player.getId(), autoMarchPB.getManhattan().getManhattanAtkSwId(),autoMarchPB.getManhattan().getManhattanDefSwId());
				return false;
			}
		}
		// 还未开启自动打野时，要先判断体力是否够打一次野
		if (autoMarchParam == null) {
			int buff = player.getEffect().getEffVal(EffType.ATK_MONSTER_VIT_ADD, effParams);
			int needVitMin = (int)(5 * (1 + buff * GsConst.EFF_PER));
			//体力减少
			int buffReduce =  player.getEffect().getEffVal(EffType.BACK_PRIVILEGE_ATK_MONSTER_VIT_REDUCE);
			needVitMin = (int) (needVitMin * (1 - buffReduce * GsConst.EFF_PER));
			needVitMin = Math.max(needVitMin, 1);
			
			if (player.getVit() < needVitMin) {
				sendError(protocol.getType(), Status.Error.VIT_NOT_ENOUGH_VALUE);
				return false;
			}
		}
		
		// 所传兵种为空
		if (armyList.isEmpty()) {
			sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
			HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, army empty, playerId: {}", player.getId());
			return false;
		}
		
		List<ArmyInfo> armyInfoList = new ArrayList<ArmyInfo>();
		for (ArmySoldierPB armySoldier : armyList) {
			// 不存在的兵种（未训练过的兵种）
			ArmyEntity entity = player.getData().getArmyEntity(armySoldier.getArmyId());
			if (entity == null || armySoldier.getCount() <= 0) {
				sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
				HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, playerId: {}, armyId: {}, count: {}", player.getId(), armySoldier.getArmyId(), armySoldier.getCount());
				return false;
			}
			
			ArmyInfo armyInfo = new ArmyInfo(armySoldier.getArmyId(), armySoldier.getCount());
			armyInfoList.add(armyInfo);
		}
		
		for (int heroId : heroIds) {
			Optional<PlayerHero> heroOP = player.getHeroByCfgId(heroId);
			// 不存在的英雄
			if (!heroOP.isPresent()) {
				sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
				HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, hero not exist, playerId: {}, heroId: {}", player.getId(), heroId);
				return false;
			}
			
			for (AutoMarchInfo info : autoMarchSetInfos) {
				List<Integer> heroList = info.getHeroIds();
				if (heroList != null && heroList.contains(heroId)) {
					sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
					HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, hero repeated, playerId: {}, heroId: {}, march1: {}, march2: {}", player.getId(), heroId, info.getPriority(), autoMarchPB.getPriority());
					return false;
				}
			}
		}
		
		if (superSoldierId > 0) {
			Optional<SuperSoldier> sso = player.getSuperSoldierByCfgId(superSoldierId);
			// 不存在的机甲
			if(!sso.isPresent()) {
				sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
				HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, superSoldier not exist, playerId: {}, superSoldierId: {}", player.getId(), superSoldierId);
				return false;
			}
			
			for (AutoMarchInfo info : autoMarchSetInfos) {
				if (info.getSuperSoldierId() == superSoldierId) {
					sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
					HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, superSoldier repeated, playerId: {}, superSoldierId: {}, march1: {}, march2: {}", player.getId(), superSoldierId, info.getPriority(), autoMarchPB.getPriority());
					return false;
				}
			}
		}
		
		MechaCoreSuitType mechacoreSuit = autoMarchPB.getMechacoreSuit();
		AutoMarchInfo autoMarchInfo = new AutoMarchInfo();
		autoMarchInfo.setArmy(armyInfoList);
		autoMarchInfo.setHeroIds(heroIds.subList(0, Math.min(heroIds.size(), 2)));
		autoMarchInfo.setSuperSoldierId(superSoldierId);
		autoMarchInfo.setPriority(autoMarchPB.getPriority());
		autoMarchInfo.setTroops(autoMarchPB.getTroops());
		autoMarchInfo.setArmourSuitType(armourSuit != null ? armourSuit.getNumber() : 0);
		autoMarchInfo.setMechacoreSuit(mechacoreSuit != null ? mechacoreSuit.getNumber() : MechaCoreSuitType.MECHA_ONE_VALUE);
		autoMarchInfo.setTalent(effParams.getTalent());
		autoMarchInfo.setSuperLab(autoMarchPB.getSuperLab());
		for (AutoMarchInfo info : autoMarchSetInfos) {
			if (info.getId() == autoMarchInfo.getId()) {
				sendError(protocol.getType(), Status.SysError.PARAMS_INVALID_VALUE);
				HawkLog.errPrintln("OpenAtkMonsterAutoMarch failed, auto march identify same, playerId: {}", player.getId());
				return false;
			}
		}
		
		autoMarchSetInfos.add(autoMarchInfo);
		for (IWorldMarch march : marchList) {
			if (march.getMarchEntity().getAutoMarchIdentify() == autoMarchInfo.getId()) {
				autoMarchInfo.setStatus(1);
				break;
			}
		}
		
		return true;
	}
	
	/**
	 * 自动搜索野怪
	 * 
	 * @param msg
	 * @return
	 */
	@MessageHandler
	private boolean onSearchMonsterAuto(AutoSearchMonsterMsg msg) {
		if (isMonsterLimit()) {
			WorldMarchService.getInstance().breakAutoMarch(player, 0);
			LogUtil.logAutoMonsterSwitch(player, false);
			HawkLog.logPrintln("AtkMonsterAutoMarch close, playerId: {}", player.getId());
			return true;
		}
		return searchMonsterAuto();
	}
	
	/**
	 * 自动打野
	 * 
	 * @return
	 */
	private boolean searchMonsterAuto() {
		AutoMonsterMarchParam autoMarchParam = WorldMarchService.getInstance().getAutoMarchParam(player.getId());
		if (autoMarchParam == null) {
			return false;
		}
		
		// 下线达到一定时长后关闭自动打野（防止断线重连的情况）
		if (!player.isActiveOnline() && autoMarchParam.isAutoMarchCDEnd()) {
			WorldMarchService.getInstance().closeAutoMarch(player.getId());
			return false;
		}
		
		// 所有的自动打野行军都已发出
		if (player.getParent().getAutoMonsterMarch(player.getId()).size() >= autoMarchParam.getAutoMarchCount()) {
			HawkLog.logPrintln("AtkMonsterAutoMarch broken, no autoMarch left, playerId: {}", player.getId());
			return false;
		}
		
		// 作用号是否结束
		StatusDataEntity statusEntity = player.getData().getStatusById(EffType.AUTO_ATK_MONSTER_VALUE);
		if (statusEntity == null || statusEntity.getEndTime() < HawkTime.getMillisecond()) {
			HawkLog.logPrintln("AtkMonsterAutoMarch broken, monthCardBuff end, playerId: {}", player.getId());
			WorldMarchService.getInstance().breakAutoMarch(player, Status.Error.AUTO_ATK_MONSTER_BUFF_BREAK_VALUE);
			return false;
		}
			
		// 没有空闲队列了
		if (!player.isHasFreeMarch()) {
			HawkLog.logPrintln("AtkMonsterAutoMarch broken, no free march, playerId: {}", player.getId());
			return false;
		}
		
		// 玩家主动召回所有行军的状态（迁城情况也算召回所有行军）
		if (autoMarchParam.isCityMoving()) {
			sendError(HP.code.SWITCH_ATK_MONSTER_AUTO_MARCH_C_VALUE, Status.Error.AUTO_ATK_MONSTER_MARCH_BACK);
			//结束自动打野  
			WorldMarchService.getInstance().breakAutoMarch(player, Status.Error.AUTO_ATK_MONSTER_MARCH_BACK_VALUE);
			HawkLog.logPrintln("AtkMonsterAutoMarch broken, city moving, playerId: {}", player.getId());
			return false;
		}
		
		
		AutoMarchInfo autoMarchInfo = autoMarchParam.getAutoMarchByPriority();
		// 还是无队列可出征
		if (autoMarchInfo == null) {
			HawkLog.logPrintln("AtkMonsterAutoMarch broken, enable autoMarchInfo not found, playerId: {}", player.getId());
			return false;
		}
		
		int[] pos = player.getPos();
		List<Integer> searchType = autoMarchParam.getSearchType();
		int level = autoMarchParam.getMaxLevel();
		int minLevel = autoMarchParam.getMinLevel();
		
		int status = Status.Error.AUTO_ATK_MONSTER_SEARCH_BREAK_VALUE;
		
		while (level >= minLevel) {
			int result = AutoSearchMonsterResultCode.KEEPTRYING;
			for (Integer type : searchType) {
				// index参数默认填0
				IYQZZWorldPoint worldPoint = getTargetPoint(pos, WorldPointType.MONSTER_VALUE, level, 0, type);
				if (worldPoint != null) {
					result = checkAutoAtkMonster((YQZZMonster) worldPoint, autoMarchInfo);
					if (result != AutoSearchMonsterResultCode.KEEPTRYING) {
						break;
					}
				}
			}
			
			// 搜索到野怪并且成功发起行军，中断搜索
			if (result == AutoSearchMonsterResultCode.SUCCESS) {
				autoMarchInfo.setStatus(1);
				return true;
			}
			
			// 条件中断搜索
			if (result > AutoSearchMonsterResultCode.SUCCESS) {
				status = result;
				break;
			}
			
			// 降低野怪等级，继续搜索
			level--;
		}
		
		// 失败了，关闭打野功能
		autoMarchParam.removeAutoMarch(autoMarchInfo.getId());
		int remainCount = autoMarchParam.getAutoMarchCount();
		if (remainCount == 0 || Status.Error.AUTO_ATK_MONSTER_VIT_BREAK_VALUE == status) {
			WorldMarchService.getInstance().breakAutoMarch(player, status);
		}
		
		HawkLog.logPrintln("AtkMonsterAutoMarch broken, playerId: {}, level: {}, minLevel: {}, status: {}, removeId: {}, remainCount: {}", 
				player.getId(), level, minLevel, status, autoMarchInfo.getId(), remainCount);
		
		return true;
	}
	
	/**
	 * 发起自动打怪行军
	 * 
	 * @param targetPoint
	 * @param autoMonsterInfo
	 * 
	 * @return -1：继续搜索野怪，0：成功发起行军，1：中断搜索，关闭自动打野
	 * 
	 */
	private int checkAutoAtkMonster(YQZZMonster targetPoint, AutoMarchInfo autoMonsterInfo) {
//		// 专属怪, 不能被别人打
//		if (!HawkOSOperator.isEmptyString(targetPoint.getOwnerId()) && !targetPoint.getOwnerId().equals(player.getId())) {
//			HawkLog.errPrintln("world auto-attack monster failed, exclusive point, playerId: {}, x: {}, y: {}, ownerId: {}", 
//					player.getId(), targetPoint.getX(), targetPoint.getY(), targetPoint.getOwnerId());
//			return AutoSearchMonsterResultCode.KEEPTRYING;
//		}

		// 野怪配置
		WorldEnemyCfg cfg = targetPoint.getWorldEnemyCfg();

		// 怪物配置错误
		if (cfg == null) {
//			HawkLog.errPrintln("world auto-attack monster failed, cfg null, playerId: {}, monsterId: {}", player.getId(), targetPoint.getMonsterId());
			return AutoSearchMonsterResultCode.KEEPTRYING;
		}

//		// 等级不足
//		if (player.getLevel() < cfg.getLowerLimit()) {
//			HawkLog.errPrintln("world auto-attack monster failed, playerId: {}, playerLevel: {}, lowerLimit: {}", player.getId(), player.getLevel(), cfg.getLowerLimit());
//			return AutoSearchMonsterResultCode.KEEPTRYING;
//		}

		int vitCost = cfg.getCostPhysicalPower();
		
		EffectParams effParams = new EffectParams();
		effParams.setHeroIds(autoMonsterInfo.getHeroIds());
		effParams.setArmourSuit(ArmourSuitType.valueOf(autoMonsterInfo.getArmourSuitType()));
		effParams.setMechacoreSuit(MechaCoreSuitType.valueOf(autoMonsterInfo.getMechacoreSuit()));
		effParams.setSuperSoliderId(autoMonsterInfo.getSuperSoldierId());
		effParams.setTalent(autoMonsterInfo.getTalent());
		effParams.setSuperLab(autoMonsterInfo.getSuperLab());
		
		// 超能实验室算双倍体力消耗
		int buff = player.getEffect().getEffVal(EffType.ATK_MONSTER_VIT_ADD, effParams);
		vitCost = (int)(vitCost * (1 + buff * GsConst.EFF_PER));
		//体力减少
		int buffReduce =  player.getEffect().getEffVal(EffType.BACK_PRIVILEGE_ATK_MONSTER_VIT_REDUCE);
		vitCost = (int) (vitCost * (1 - buffReduce * GsConst.EFF_PER));
		vitCost = Math.max(vitCost, 1);
		// 体力消耗
		ConsumeItems consumeItems = ConsumeItems.valueOf(PlayerAttr.VIT, vitCost);
		// 体力不足
		if (!consumeItems.checkConsume(player)) {
			HawkLog.errPrintln("world auto-attack monster failed, vit not enough, playerId: {}, vit: {}", player.getId(), player.getVit());
			return Status.Error.AUTO_ATK_MONSTER_VIT_BREAK_VALUE;
		}
		
		List<Integer> heroIds = new ArrayList<Integer>();
		// 英雄是否可出征
		if (ArmyService.getInstance().heroCanMarch(player, autoMonsterInfo.getHeroIds())) {
			heroIds.addAll(heroIds);
		}
		
		int superSoldierId = 0;
		// 机甲是否可出征
		if (ArmyService.getInstance().superSoldierCsnMarch(player, autoMonsterInfo.getSuperSoldierId())) {
			superSoldierId = autoMonsterInfo.getSuperSoldierId();
		}
		
		List<ArmyInfo> armyList = autoMonsterInfo.getArmy();
		// 兵不足
		List<ArmyInfo> armyInfoList = ArmyService.getInstance().checkArmyInfo(player, armyList, heroIds, superSoldierId); 
		if (armyInfoList.isEmpty()) {
			HawkLog.errPrintln("world auto-attack monster failed, no free army, playerId: {}", player.getId());
			return Status.Error.AUTO_ATK_MONSTER_ARMY_BREAK_VALUE;
		}

		// 开启行军,目标放怪物ID
		String targetId = String.valueOf(cfg.getId());
		
		effParams.setArmys(armyInfoList);
		effParams.setAutoMarchIdentify(autoMonsterInfo.getId());
//		IWorldMarch march = WorldMarchService.getInstance().startMarch(player, WorldMarchType.ATTACK_MONSTER_VALUE, targetPoint.getId(), targetId, null, 0, autoMonsterInfo.getId(),0,0, effParams);
		IYQZZWorldMarch march = player.getParent().startMarch(player, player, targetPoint, WorldMarchType.ATTACK_MONSTER, targetId, 0, effParams);
		
		if (march == null) {
			HawkLog.errPrintln("world auto-attack monster failed, start march failed, playerId: {}", player.getId());
			return AutoSearchMonsterResultCode.KEEPTRYING;
		}
		YQZZAttackMonsterMarch attackMonsterMarch = (YQZZAttackMonsterMarch) march;
		attackMonsterMarch.setVitBack(vitCost);
		// 扣除体力
		consumeItems.consumeAndPush(player, Action.FIGHT_MONSTER);
		// 行为日志
		BehaviorLogger.log4Service(player, Source.WORLD_ACTION, Action.WORLD_START_FIGHT_MONSTER, 
				Params.valueOf("marchData", march),
				Params.valueOf("autoMarch", "true"));
		
		MissionManager.getInstance().postMsg(player, new EventGenOldMonsterMarch(cfg.getLevel()));
		
		return AutoSearchMonsterResultCode.SUCCESS;
	}
	
	private boolean isMonsterLimit() {
		int attackFoggyWinTimes = player.getKillMonster();
		if (attackFoggyWinTimes < player.getParent().getCfg().getMonsterLimit()) {
			return false;
		}
		return true;
	}
}
