package com.hawk.game.activity.impl.yurirevenge;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Status.SysError;
import com.hawk.game.protocol.YuriRevenge.GetYuriRankInfo;
import com.hawk.game.protocol.YuriRevenge.GetYuriRankInfoResp;
import com.hawk.game.protocol.YuriRevenge.YuriRevengePageInfoResp;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.log.Action;
import com.hawk.log.Source;

/**
 * 尤里复仇活动模块
 *
 * @author admin
 */
public class PlayerYuriRevengeModule extends PlayerModule {

	static final Logger logger = LoggerFactory.getLogger("Server");


	/**
	 * 构造函数
	 *
	 * @param player
	 */
	public PlayerYuriRevengeModule(Player player) {
		super(player);
		
	}
	
	
	@Override
	protected boolean onPlayerLogin() {
		return true;
	}


	/**
	 * 获取界面信息
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GET_YURI_REVENGE_PAGE_INFO_C_VALUE)
	public boolean onGetPageInfo(HawkProtocol protocol) {
		YuriRevengePageInfoResp.Builder resp = YuriRevengePageInfoResp.newBuilder();
		int result = YuriRevengeService.getInstance().onGetPageInfo(resp, player);
		if(result == SysError.SUCCESS_OK_VALUE){
			sendProtocol(HawkProtocol.valueOf(HP.code.GET_YURI_REVENGE_PAGE_INFO_S, resp));
			return true;
		}
		sendError(HP.code.GET_YURI_REVENGE_PAGE_INFO_C_VALUE, result);
		return false;
	}
	
	/**
	 * 开启尤里复仇活动战斗
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.OPEN_YURI_REVENGE_ACTIVITY_VALUE)
	public boolean onOpenActivity(HawkProtocol protocol) {
		//投递到世界线程处理
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.OPEN_YURI_REVENGE) {
			@Override
			public boolean onInvoke() {
				int result = YuriRevengeService.getInstance().onOpenActivity(player);
				if(result == Status.SysError.SUCCESS_OK_VALUE){
					player.responseSuccess(HP.code.OPEN_YURI_REVENGE_ACTIVITY_VALUE);
					BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.OPEN_YURI_REVENGE_FIGHT,
							Params.valueOf("guildId", player.getGuildId()));
					return true;
				}
				sendError(HP.code.OPEN_YURI_REVENGE_ACTIVITY_VALUE, result);
				return true;
			}
		});
		return true;
	}
	
	/**
	 * 获取排行榜信息
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.GET_YURI_REVENGE_RANK_INFO_C_VALUE)
	public boolean onGetRankInfo(HawkProtocol protocol){
		GetYuriRankInfo req = protocol.parseProtocol(GetYuriRankInfo.getDefaultInstance());
		GetYuriRankInfoResp.Builder resp = GetYuriRankInfoResp.newBuilder();
		int result = YuriRevengeService.getInstance().onGetRankInfo(req.getRankType(),resp, player);
		if(result == SysError.SUCCESS_OK_VALUE){
			sendProtocol(HawkProtocol.valueOf(HP.code.GET_YURI_REVENGE_RANK_INFO_S, resp));
			return true;
		}
		sendError(HP.code.GET_YURI_REVENGE_RANK_INFO_C_VALUE, result);
		return false;
	}
	
}