package com.hawk.game.idipscript.security;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.uuid.HawkUUIDGenerator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.Status;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 禁止参与排行榜(AQ)
 *
 * localhost:8080/script/idip/4141
 *
 * @param Partition
 *            小区Id
 * @param OpenId
 *            用户openId
 * @param LeaguaId
 *            联盟id
 * @param IsZero
 *            是否清零
 * @param Type 榜单类型 (type==0表示全选，type>0时，type-1与RankType一一对应 )
 * @param BanRankTime
 *            禁止参与排行时长
 * @param BanReason
 *            禁止参与排行原因
 * 
 * @author Jesse
 */
@HawkScript.Declare(id = "idip/4141")
public class BanJoinRankHandler extends IdipScriptHandler {

	// 全选
	private static final int ALL = 0;
	// 数组中元素的顺序必须和RankType保持一致
	private static final int[] BAN_RANK_MSG = {MsgId.BAN_PLAYER_FIGHT_RANK, MsgId.BAN_PLAYER_KILL_RANK, MsgId.BAN_PLAYER_CASTLE_RANK, 
			MsgId.BAN_PLAYER_GRADE_RANK, MsgId.BAN_GUILD_FIGHT_RANK, MsgId.BAN_GUILD_KILL_RANK};

	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		int type = request.getJSONObject("body").getInteger("Type");
		if (type == ALL) {
			banJoinAllRank(request, result);
			return result;
		}
		
		type -= 1;
		RankType rankType = RankType.valueOf(type); 
		if (rankType == null) {
			result.getHead().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getHead().put("RetErrMsg", "Type param error");
			return result;
		}
		
		banJoinRank(request, result, BAN_RANK_MSG[type], rankType);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		return result;
	}
	
	/**
	 * 禁止参与所有的排行榜
	 * 
	 * @param request
	 * @param result
	 * @return
	 */
	private void banJoinAllRank(JSONObject request, IdipResult result) {
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		RankService.getInstance().dealMsg(MsgId.ALL_RANK_BAN, new AllRankBanMsgInvoker(player, request));
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
	}
	
	/**
	 * 禁止参与排行榜
	 * 
	 * @param request
	 * @param result
	 * @param msgId
	 * @param rankType
	 */
	private void banJoinRank(JSONObject request, IdipResult result, int msgId, RankType rankType) {
		if (rankType == RankType.ALLIANCE_KILL_ENEMY_KEY || rankType == RankType.ALLIANCE_FIGHT_KEY) {
			banJoinGuildRank(request, result, msgId, rankType);
			return;
		}
		
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return;
		}
		
		RankService.getInstance().dealMsg(msgId, new BanRankMsgInvoker(player, rankType, player.getId(), request));			
		// 处理完毕将玩家强制离线
		if (player.isActiveOnline()) {
			player.kickout(Status.IdipMsgCode.IDIP_BAN_JOIN_RANK_OFFLINE_VALUE, true, null);
		}
	}
	
	/**
	 * 禁止参与联盟排行榜
	 * 
	 * @param request
	 * @param result
	 * @param msgId
	 * @param rankType
	 */
	private void banJoinGuildRank(JSONObject request, IdipResult result, int msgId, RankType rankType) {
		String numGuildId = request.getJSONObject("body").getString("LeagueId");
		String guildId = HawkUUIDGenerator.longUUID2Str(Long.parseLong(numGuildId));
		GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(guildId);
		if (guildObj != null) {
			RankService.getInstance().dealMsg(msgId, new BanRankMsgInvoker(null, rankType, guildId, request));
		}
	}
	
	/**
	 * 封禁排行榜信息，个人的或联盟的
	 * 
	 * @author lating
	 *
	 */
	public static class BanRankMsgInvoker extends HawkMsgInvoker {
		private Player player;
		private RankType rankType;
		private String targetId;
		private JSONObject request;
		
		public BanRankMsgInvoker(Player player, RankType rankType, String targetId, JSONObject request) {
			this.player = player;
			this.targetId = targetId;
			this.rankType = rankType;
			this.request = request;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			boolean isZero = request.getJSONObject("body").getBoolean("IsZero");
			int banTime = request.getJSONObject("body").getInteger("BanRankTime");
			long forbidEndTime = HawkTime.getMillisecond() + banTime * 1000L;
			String banRankContent = request.getJSONObject("body").getString("BanRankContent");
			
			GlobalData.getInstance().addBanRankInfo(targetId, rankType.name().toLowerCase(), IdipUtil.decode(banRankContent), forbidEndTime);
			RankService.getInstance().banJoinRank(targetId, rankType, forbidEndTime, isZero, player);
			LogUtil.logIdipSensitivity(player, request, 0, banTime);
			return true;
		}
	}
	
	/**
	 * 封禁所有的排行榜信息
	 * 
	 * @author lating
	 *
	 */
	public static class AllRankBanMsgInvoker extends HawkMsgInvoker {
		private Player player;
		private JSONObject request;
		
		public AllRankBanMsgInvoker(Player player, JSONObject request) {
			this.player = player;
			this.request = request;
		}
		
		@Override
		public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
			boolean isZero = request.getJSONObject("body").getBoolean("IsZero");
			int banTime = request.getJSONObject("body").getInteger("BanRankTime");
			long banEndTime = HawkTime.getMillisecond() + banTime * 1000L;
			String banRankContent = request.getJSONObject("body").getString("BanRankContent");
			String banReason = IdipUtil.decode(banRankContent);
			
			if (player != null) {
				for(RankType rankType : GsConst.PERSONAL_RANK_TYPE) {
					GlobalData.getInstance().addBanRankInfo(player.getId(), rankType.name().toLowerCase(), banReason, banEndTime);
					RankService.getInstance().banJoinRank(player.getId(), rankType, banEndTime, isZero, player);
				}
				
				// 处理完毕将玩家强制离线
				if (player.isActiveOnline()) {
					player.kickout(Status.IdipMsgCode.IDIP_BAN_JOIN_RANK_OFFLINE_VALUE, true, null);
				}
			}
			
			
			final String leagueId = request.getJSONObject("body").getString("LeagueId");
			GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(leagueId);
			if (guildObj != null) {
				for (RankType rankType : GsConst.GUILD_RANK_TYPE) {
					GlobalData.getInstance().addBanRankInfo(leagueId, rankType.name().toLowerCase(), banReason, banEndTime);
					RankService.getInstance().banJoinRank(leagueId, rankType, banEndTime, isZero, null);
				}
			}
			
			LogUtil.logIdipSensitivity(player, request, 0, banTime);
			
			return true;
		}
	}
}
