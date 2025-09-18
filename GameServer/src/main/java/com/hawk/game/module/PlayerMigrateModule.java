package com.hawk.game.module;

import org.eclipse.jetty.client.api.ContentResponse;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.log.HawkLog;
import org.hawk.net.http.HawkHttpUrlService;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tuple.HawkTuple3;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.ServerInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Player.PlayerMigrateOutReq;
import com.hawk.game.service.MigrateService;
import com.hawk.game.util.GameUtil;

public class PlayerMigrateModule extends PlayerModule {

	public PlayerMigrateModule(Player player) {
		super(player);
	}
	
	
	@ProtocolHandler(code = {HP.code.PLAYER_MIGRATE_OUT_REQ_VALUE})
	private void onPlayerMigrateOutReq(HawkProtocol protocol) {
		PlayerMigrateOutReq cparam = protocol.parseProtocol(PlayerMigrateOutReq.getDefaultInstance());
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
		MigrateService migrateService = MigrateService.getInstance();	
			@Override
			public Object run() {
				HawkTuple3<Integer, Player, ServerInfo> tuple3 = migrateService.isCanMigrate(player.getId(), cparam.getTargetServerId());
				if (tuple3.first != Status.SysError.SUCCESS_OK_VALUE) {
					player.sendError(protocol.getType(), tuple3.first, 0);
					
					return Boolean.FALSE;
				}
				
				int rlt = Status.Error.MIGRATE_SYSTEM_ERROR_VALUE;
				try {
					rlt = migrateService.migrateOutPlayer(tuple3.second, cparam.getTargetServerId());
					if (Status.SysError.SUCCESS_OK_VALUE == rlt) {
						rlt = Status.Error.MIGRATE_SYSTEM_ERROR_VALUE;
						String url = GameUtil.getImmgratePlayerURL(tuple3.third, tuple3.second.getId(), cparam.getTargetServerId());
						ContentResponse response = HawkHttpUrlService.getInstance().doGet(url, 3000);
						if (response == null) {
							HawkLog.errPrintln("call remote immigrate player error url:{}", url);
						} else {
							String str = response.getContentAsString();
							HawkLog.logPrintln("call remote immigrate player receive str:{}", str);
							JSONObject jsonObject = JSON.parseObject(str);
							rlt = jsonObject.getIntValue("code");							
						}						
					} else {
						player.sendError(protocol.getType(), rlt, 0);
					}
				} catch (Exception e) {
					HawkLog.errPrintln("migrate player error playerId:{}", tuple3.second.getId());
					HawkException.catchException(e, "player migrate out playerId:"+player.getId());
					player.sendError(protocol.getType(), Status.Error.MIGRATE_SYSTEM_ERROR_VALUE, 0);
					
					return Boolean.FALSE;
				} finally {
					//一定要保证保证失败的时候也能释放锁住的玩家.
					if (rlt == Status.SysError.SUCCESS_OK_VALUE) {
						migrateService.migrateOutFinish(tuple3.second);
					} else {
						migrateService.migrateOutError(tuple3.second, rlt);
					}
				}
							
				return Boolean.TRUE;
			}
		});
	}
}
