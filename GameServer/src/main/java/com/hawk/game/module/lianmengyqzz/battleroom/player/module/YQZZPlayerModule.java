package com.hawk.game.module.lianmengyqzz.battleroom.player.module;

import java.util.Objects;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.google.common.base.Joiner;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZConst.YQZZState;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZRoomManager;
import com.hawk.game.module.lianmengyqzz.battleroom.msg.YQZZJoinRoomMsg;
import com.hawk.game.module.lianmengyqzz.battleroom.msg.YQZZQuitRoomMsg;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.player.YQZZPlayer;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.YQZZ.PBYQZZSecondMapResp;
import com.hawk.game.protocol.YQZZ.YQZZDeclareWarUseResp;
import com.hawk.game.protocol.YQZZ.YQZZGaiLanResp;

public class YQZZPlayerModule extends PlayerModule {

	public YQZZPlayerModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		IYQZZPlayer gamer = YQZZRoomManager.getInstance().makesurePlayer(player.getId());
		if (gamer != null && gamer.getParent() != null) {
			boolean needClean = gamer.getParent().getPlayer(player.getId()) == null || gamer.getParent().isGameOver();
			if (needClean) {
				gamer.getData().unLockOriginalData();
				gamer.setYQZZRoomId("");
				gamer.setYQZZState(null);
				YQZZRoomManager.getInstance().invalidate(gamer);
				return true;
			}
		}

		if (Objects.nonNull(gamer)) {
			YQZZPlayer yqplayer = (YQZZPlayer) gamer;
			player.setYQZZState(YQZZState.GAMEING);
			player.setYQZZRoomId(yqplayer.getParent().getId());
			yqplayer.setSource(player);
			
			gamer.getParent().onPlayerLogin(gamer);

		}
		return super.onPlayerLogin();
	}

	@MessageHandler
	private void onJoinRoomMsg(YQZZJoinRoomMsg msg) {
		String armyStr = Joiner.on("|").join(player.getData().getArmyEntities());
		DungeonRedisLog.log(player.getId(), "roomId {} , army {}", player.getYQZZRoomId(), armyStr);
		msg.getBattleRoom().joinRoom(msg.getPlayer());
	}

	/** 结束, 记录胜负什么的 */
	@MessageHandler
	private void onQuitRoomMsg(YQZZQuitRoomMsg msg) {
		String armyStr = Joiner.on("|").join(player.getData().getArmyEntities());
		DungeonRedisLog.log(player.getId(), "roomId {} , army {}", msg.getRoomId(), armyStr);
	}
	
	/**玩家不在战场 请求小地图*/
	@ProtocolHandler(code = HP.code2.YQZZ_SECOND_MAP_C_VALUE)
	private void getSecondMap(HawkProtocol protocol) {
		YQZZGaiLanResp gailan = YQZZRoomManager.getInstance().getGaiLanResp();
		if (gailan == null) {
			player.sendError(HP.code2.YQZZ_SECOND_MAP_C_VALUE,
					Status.YQZZError.YQZZ_WAR_NOT_IN_BATTLE_TIME_VALUE, 0);
			return;
		}
		PBYQZZSecondMapResp.Builder resp = gailan.getSecondMap().toBuilder();
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_SECOND_MAP_S, resp));
	}

	
	/**查看宣战记录*/
	@ProtocolHandler(code = HP.code2.YQZZ_DECLARE_WAR_USE_C_VALUE)
	private void declareWarRecords(HawkProtocol protocol) {
		YQZZGaiLanResp gailan = YQZZRoomManager.getInstance().getGaiLanResp();
		if (gailan == null) {
			player.sendError(HP.code2.YQZZ_SECOND_MAP_C_VALUE,
					Status.YQZZError.YQZZ_WAR_NOT_IN_BATTLE_TIME_VALUE, 0);
			return;
		}
		YQZZDeclareWarUseResp.Builder resp = YQZZDeclareWarUseResp.newBuilder();
		for (YQZZDeclareWarUseResp zhanchangnei : gailan.getDeclearWarRecList()) {
			if (zhanchangnei.getGuildId().equals(player.getGuildId())) {
				resp = zhanchangnei.toBuilder();
				break;
			}
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_DECLARE_WAR_USE_S_VALUE, resp));
	}
}
