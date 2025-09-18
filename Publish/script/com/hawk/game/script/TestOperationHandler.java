package com.hawk.game.script;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.constant.ObjType;
import com.hawk.activity.event.impl.AddTavernScoreGMEvent;
import com.hawk.activity.event.impl.PlanetScoreAddEvent;
import com.hawk.activity.msg.GmCloseActivityMsg;
import com.hawk.activity.type.impl.inviteMerge.InviteMergeActivity;
import com.hawk.activity.type.impl.mechacoreexplore.CoreExploreActivity;
import com.hawk.activity.type.impl.mergecompetition.MergeCompetitionConst;
import com.hawk.game.world.service.WorldNianService;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.xid.HawkXID;
import com.hawk.game.GsConfig;
import com.hawk.game.config.GuildScienceMainCfg;
import com.hawk.game.config.ManhattanBaseLevelCfg;
import com.hawk.game.config.ManhattanBaseStageCfg;
import com.hawk.game.config.ManhattanSWLevelCfg;
import com.hawk.game.config.ManhattanSWStageCfg;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.GuildScienceEntity;
import com.hawk.game.entity.RechargeEntity;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.idipscript.util.IdipUtil.Switch;
import com.hawk.game.module.mechacore.PlayerMechaCore;
import com.hawk.game.module.mechacore.cfg.MechaCoreModuleCfg;
import com.hawk.game.module.mechacore.cfg.MechaCoreRankLevelCfg;
import com.hawk.game.module.mechacore.cfg.MechaCoreShowCfg;
import com.hawk.game.module.mechacore.cfg.MechaCoreTechLevelCfg;
import com.hawk.game.module.mechacore.entity.MechaCoreModuleEntity;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.tech.NationTechCenter;
import com.hawk.game.player.Player;
import com.hawk.game.player.manhattan.PlayerManhattan;
import com.hawk.game.player.manhattan.PlayerManhattanModule;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Activity.CEBuyPickReq;
import com.hawk.game.protocol.Activity.CEObstacleRemoveType;
import com.hawk.game.protocol.Activity.CERemoveObstacleReq;
import com.hawk.game.protocol.Activity.CEShopExchangeReq;
import com.hawk.game.protocol.Activity.CETechOperReq;
import com.hawk.game.protocol.Activity.CEZoneBoxRewardReq;
import com.hawk.game.protocol.Activity.ShareProperityBindRoleReq;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.GuildScience.GuildScienceInfo;
import com.hawk.game.protocol.GuildScience.GuildScienceInfoSync;
import com.hawk.game.protocol.IDIP.NoticeMode;
import com.hawk.game.protocol.IDIP.NoticeType;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.protocol.Player.AccountCancellationResp;
import com.hawk.game.protocol.Player.CrossPlayerStruct;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.Schedule.ScheduleDeleteReq;
import com.hawk.game.protocol.Schedule.ScheduleUpdateReq;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsOfficerStruct;
import com.hawk.game.protocol.Status.IdipMsgCode;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.rank.RankService;
import com.hawk.game.recharge.RechargeType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.ImmgrationService;
import com.hawk.game.service.MergeService;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.SysOpService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.starwars.StarWarsActivityService;
import com.hawk.game.service.starwars.StarWarsOfficerService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.ControlerModule;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.log.LogConst.PowerChangeReason;
import redis.clients.jedis.Jedis;

/**
 * 用来动态调整（测试环境使用）
 * 
 * 往国家医院添加统帅之战死兵 
 * http://localhost:8080/script/testOp?op=addTszzSoldierToNationHospital&playerId=&armys=id1_count1,id2_count2,....
 * 
 * 往国家医院添加普通死兵
 * http://localhost:8080/script/testOp?op=addSoldierToNationHospital&playerId=&armys=id1_count1,id2_count2,....
 * 
 * 设定霸主
 * http://localhost:8080/script/testOp?op=genWorldKing&playerName=lat001
 *
 * http://localhost:8080/script/testOp?op=removeIcon&playerId=1aat-3mvonb-1&itemId=21070039
 *
 * @author hawk
 */
public class TestOperationHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		String opType = params.get("op");
		String result = testOp(params);
		if (result != null) {
			return result;
		}
		
		if (opType.equals("inviteMergeBack")) {
			String server1 = params.get("server1");
			String server2 = params.get("server2");
			if (HawkOSOperator.isEmptyString(server1) || HawkOSOperator.isEmptyString(server2)) {
				return HawkScript.failedResponse(-1, "server1 或 server2参数缺失");
			}
			Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.INVITE_MERGE_VALUE);
			InviteMergeActivity activity = (InviteMergeActivity) opActivity.get();
			activity.inviteMergeStateBack(server1, server2);
			return HawkScript.successResponse("inviteMergeBack");
		}
		
		if (opType.equals("accountCancel")) {
			return testAccountCancel(params);
		}

		if (opType.equals("getNianK")) {
			return ""+WorldNianService.getInstance().nianK;
		}
		if (opType.equals("clearEmptyGuild")) {
			MergeService.clearEmptyGuild();
			return HawkScript.successResponse("clearEmptyGuild");
		}
		
		if (opType.equals("activity369Skip")) {
			return activity369Skip(params);
		}
		
		if (opType.equals("immigration")) {
			SysOpService.getInstance().batchSendGuildAward();
			return HawkScript.successResponse("immigration");
		}
		
		if (opType.equals("chargeShowControl")) {
			return chargeShowControl(params);
		}
		
		return HawkScript.failedResponse(-1, "该脚本命令不存在：" + opType);
	}
	
	private String chargeShowControl(Map<String, String> params) {
		String playerId = params.get("playerId");
		String chargeShow = params.getOrDefault("chargeShow", "0");
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			return HawkScript.failedResponse(-1, "参数错误，不存在对应玩家");
		}
		
		int val = 0;
		String key = GsConst.CHARGE_SHOW + ":" + player.getId();
		if (chargeShow.equals("0")) {
			val = 1;
			RedisProxy.getInstance().getRedisSession().setString(key, "1");
		} else {
			val = 0;
		}
		if (player.isActiveOnline()) {
			CustomDataEntity entity = player.getData().getCustomDataEntity(GsConst.CHARGE_SHOW);
			if (entity == null) {
				entity = player.getData().createCustomDataEntity(GsConst.CHARGE_SHOW, val, "");
			} else {
				entity.setValue(val);
			}
			player.getPush().syncCustomData();
		}
		
		HawkLog.logPrintln("idip chargeShowControl playerId: {}, local server: {}", player.getId(), GsConfig.getInstance().getServerId());
		return HawkScript.successResponse("chargeShowControl");
	}
	
	/**
	 * testOp指令
	 * @param params
	 * @return
	 */
	private String testOp(Map<String, String> params) {
		String opType = params.get("op");
		if ("clientProtocol".equals(opType)) {
			return clientProtocol(params);
		}
		
		// 发送idip公告消息
		if ("sendIdipNotice".equals(opType)) {
			sendIdipNotice(params);
			return HawkScript.successResponse("sendIdipNotice");
		}
		
		// 往国家医院添加统帅之战死兵     http://localhost:8080/script/testOp?op=addTszzSoldierToNationHospital&playerId=&armys=id1_count1,id2_count2,....
		if ("addTszzSoldierToNationHospital".equals(opType)) {
			SysOpService.getInstance().addNationalHopitalTszzSoldier(params);
			return HawkScript.successResponse("addTszzSoldierToNationHospital");
		}
		
		// 往国家医院添加普通死兵  http://localhost:8080/script/testOp?op=addSoldierToNationHospital&playerId=&armys=id1_count1,id2_count2,....
		if ("addSoldierToNationHospital".equals(opType)) {
			SysOpService.getInstance().addNationalHopitalSoldier(params);
			return HawkScript.successResponse("addSoldierToNationHospital");
		}
		
		// 开启国王战
		if ("presidentWar".equals(opType)) {
			return HawkScript.successResponse(startPresidentWar());
		}
		
		// 将pointId转换成点坐标
		if ("splitXAndY".equals(opType)) {
			int[] xy = GameUtil.splitXAndY(Integer.parseInt(params.getOrDefault("point", "0")));
			return HawkScript.successResponse("result: x: " + xy[0] + ", y: " + xy[1]);
		}
		
		// 活动开关控制
		if ("activityControl".equals(opType)) {
			int switchVal = Integer.parseInt(params.getOrDefault("switchVal", "0"));
			int activityId = Integer.parseInt(params.getOrDefault("activityId", "316"));
			IdipUtil.systemSwitchControl(switchVal, ControlerModule.ACTIVITY, activityId);
			if (switchVal == Switch.OFF) {
				HawkTaskManager.getInstance().postMsg(HawkXID.valueOf(ObjType.MANAGER, ObjType.ID_SERVER_ACTIVITY), GmCloseActivityMsg.valueOf(activityId));
			}
			return HawkScript.successResponse("activityControl");
		}
		
		// 复制已存在的玩家的数据
		if ("copyPlayer".equals(opType)) {
			SysOpService.getInstance().copyPlayerData();
			return HawkScript.successResponse("copyPlayer");
		}
		
		// http://localhost:8080/script/testOp?op=copyPlayerRedis&playerId=12kx-2vgz51-1&sourcePlayerIds=12kx-2vq6nh-1
		// 复制账号player的redis数据（基础数据只复制了mysql的数据）
		if ("copyPlayerRedis".equals(opType)) {
			SysOpService.getInstance().copyPlayerRedis(params);
			return HawkScript.successResponse("copyPlayerRedis");
		}
		
		// 清除账号数据复制的标识（使得一个已经复制过目标数据的角色还能再次复制）
		if ("clearCopyPlayerPlayer".equals(opType)) {
			SysOpService.getInstance().clearCopyPlayerPlayer(params);
			return HawkScript.successResponse("clearCopyPlayerPlayer");
		}
		
		// 复制账号的数据处理（即将母账号数据中的个人标识信息替换成子账号的信息）
		if ("copyDataProc".equals(opType)) {
			String direct = params.getOrDefault("direct", "0");
			String result = SysOpService.getInstance().copyDataProc(Integer.parseInt(direct));
			return HawkOSOperator.isEmptyString(result) ? HawkScript.successResponse("success") : HawkScript.failedResponse(-1, result);
		}
		
		// 移除城点（玩家上线后重新落地）
		if ("removeCity".equals(opType)) {
			return SysOpService.getInstance().removeCity(params);
		}
		
		if ("removePlayerNameCache".equals(opType)) {
			GameUtil.removePlayerNameInfo(params.get("name"));
			return HawkScript.successResponse("removePlayerNameCache");
		}
		
		// 产生霸主
		if ("genWorldKing".equals(opType)){
			return genWorldKing(params);
		}
		
		// 周年庆庆典基金等活动，依赖日常任务积分的，通过脚本添加积分
		if("addTavernScore".equals(opType)){
			Player targetPlayer = GlobalData.getInstance().scriptMakesurePlayer(params);
			int addScore=Integer.parseInt(params.getOrDefault("addScore", "100"));
			if (targetPlayer == null) {
				return HawkScript.failedResponse(-1, "参数错误，不存在对应玩家");
			}
			ActivityManager.getInstance().postEvent(new AddTavernScoreGMEvent(targetPlayer.getId(), addScore, 0));
			return HawkScript.successResponse("addTavernScore");
		}
		
		// 联盟科技升级
		if ("guildScienceUp".equals(opType)) {
			return guildScienceUp(params.get("guildId"));
		}
		
		// 加保护罩
		if ("addCityShield".equals(opType)) {
			return addCityShield(Integer.parseInt(params.getOrDefault("minutes", "14400")));
		}
		
		// 守护对象信息查询测试
		if ("guard".equals(opType)) {
			String playerId = params.get("playerId");
			String targetId = RelationService.getInstance().getGuardPlayer(playerId);
			Player tarPlayer = GlobalData.getInstance().makesurePlayer(targetId);
			if (tarPlayer != null) {
				JSONObject json = new JSONObject();
				json.put("tarPlayerId", tarPlayer.getId());
				json.put("tarOpenid", tarPlayer.getOpenId());
				json.put("tarName", tarPlayer.getName());
				return HawkScript.successResponse(json.toJSONString());
			} else {
				return HawkScript.successResponse("no guard target");
			}
		}
		
		// 查询本服的大区ID，主要是提供给品管同学使用
		if ("areaId".equals(opType)) {
			JSONObject json = new JSONObject();
			json.put("areaId", GsConfig.getInstance().getAreaId());
			json.put("serverId", GsConfig.getInstance().getServerId());
			return HawkScript.successResponse(json.toJSONString());
		}
		
		//星能探索积分添加 http://localhost:8080/script/testOp?op=planetScoreAdd&playerId=1aat-3hoxt3-1&scoreAdd=5000
		if (opType.equals("planetScoreAdd")) {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(-1, "参数错误，不存在对应玩家");
			}
			
			int score = Integer.parseInt(params.getOrDefault("scoreAdd", "500"));
			int add2Person = Integer.parseInt(params.getOrDefault("add2Person", "0"));
			ActivityManager.getInstance().postEvent(new PlanetScoreAddEvent(player.getId(), score, add2Person > 0));
			return HawkScript.successResponse("planetScoreAdd success");
		}
		
		if (opType.equals("queryNianK")) {
			int result = RedisProxy.getInstance().getNianLastK(GsConfig.getInstance().getServerId());
			return HawkScript.successResponse("nianLastK = " + result);
		}
		
		if (opType.equals("70")) {
			changeNationTech(Integer.parseInt(params.get("value")));
			return HawkScript.successResponse("70");
		}
		
		if (opType.equals("71")) {
			finishNationTech(Integer.parseInt(params.get("techId")), Integer.parseInt(params.get("level")));
			return HawkScript.successResponse("71");
		}
		
		//充值额度查询，包括充值金条数、直购金条数
		if (opType.equals("rechargeQuery")) {
			return rechargeQuery(params);
		}
		
		if(opType.equals("refreshRank")) {
			for(RankType rankType : GsConst.PERSONAL_RANK_TYPE) {
				RankService.getInstance().refreshRank(rankType);
			}
			return HawkScript.successResponse("refreshRank success");
		}
		
		/**
		 * 移除个人活动相关排行榜数据
		 */
		if(opType.equals("removePlayerRank")) {
			return removePlayerRank(params);
		}
		
		if (opType.equals("killMonsterLevel")) {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(-1, "对应玩家不存在");
			}
			int level = Integer.parseInt(params.getOrDefault("level", "1"));
			int oldLevel = player.getData().getMonsterEntity().getMaxLevel();
			player.getData().getMonsterEntity().setMaxLevel(level);
			player.getPush().syncMaxMonsterLevel();
			return HawkScript.successResponse("killMonsterLevel update, oldLevel: " + oldLevel + ", newLevel: " + level);
		}
		
		if(opType.equals("queryGuildPlat")) {
			String guildId = params.get("guildId");
			GuildInfoObject obj = GuildService.getInstance().getGuildInfoObject(guildId);
			if (obj == null) {
				return HawkScript.failedResponse(-1, "联盟不存在");
			} 
			return HawkScript.successResponse("leaderId: " + obj.getLeaderId() + ", leaderName: " + obj.getLeaderName() + ", leaderPlatform: " + obj.getEntity().getLeaderPlatform());
		}
		
		/** 添加机甲核心模块  */
		// http://localhost:8080/script/testOp?op=mechacoreModuleAdd&playerId=1aat-3syuot-1
		if (opType.equals("mechacoreModuleAdd")) {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(-1, "对应玩家不存在");
			}
			mechacoreModuleAdd(player);
			return HawkScript.successResponse("mechacoreModuleAdd succ");
		}
		
		/** 清空机甲核心模块  */
		// http://localhost:8080/script/testOp?op=mechacoreModuleClear&playerId=1aat-3syuot-1
		if (opType.equals("mechacoreModuleClear")) {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(-1, "对应玩家不存在");
			}
			mechacoreModuleClear(player);
			return HawkScript.successResponse("mechacoreModuleClear succ");
		}
		
		/** 机甲核心科技一键满级 */
		if (opType.equals("mechacoreTechUpMax")) {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(-1, "对应玩家不存在");
			}
			
			int threadNum = HawkTaskManager.getInstance().getThreadNum();
			HawkTaskManager.getInstance().postTask(new HawkTask() {
				@Override
				public Object run() {
					mechacoreTechUpMax(player);
					return null;
				}
				
			}, player.getXid().getHashThread(threadNum));
			return HawkScript.successResponse("mechacoreTechUpMax succ");
		}
		
		if(opType.equals("mergeCompete")) {
			String serverId = params.get("serverId");
			String mergeTime = params.get("mergeTimeSecond");
			if (HawkOSOperator.isEmptyString(mergeTime) || HawkOSOperator.isEmptyString(serverId)) {
				return HawkScript.failedResponse(-1, "参数错误");
			}
			int timeLong = Integer.parseInt(mergeTime);
			if (timeLong * 1000L <= HawkTime.getMillisecond()) {
				return HawkScript.failedResponse(-1, "合服时间参数错误");
			}
			RedisProxy.getInstance().getRedisSession().hSet(MergeCompetitionConst.MERGE_SERVER_INFO, serverId, mergeTime);
			return HawkScript.successResponse("mergeCompete succ");
		}
		
		if(opType.equals("mergeCompeteRedisClear")) {
			try (Jedis redis = RedisProxy.getInstance().getRedisSession().getJedis()) {
				Set<String> keys = redis.keys("activity368*");
				for (String key : keys) {
					redis.del(key);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
			return HawkScript.successResponse("mergeCompeteRedisClear succ");
		}
		
		//超武底座一键满级
		if (opType.equals("manhattanBaseMaxLv")) {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(-1, "对应玩家不存在");
			}
			manhattanBaseMaxLv(player);
			return HawkScript.successResponse("manhattanBaseMaxLv");
		}
		
		//超武一键满级
		if (opType.equals("manhattanSWMaxLv")) {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(-1, "对应玩家不存在");
			}
			manhattanSWMaxLv(player);
			return HawkScript.successResponse("manhattanSWMaxLv");
		}
		
		return null;
	}
	
	/**
	 * 超武底座一键满级
	 * @param player
	 */
	private void manhattanBaseMaxLv(Player player) {
		PlayerManhattan base = player.getManhattanBase();
		if (base == null) {
			HawkLog.errPrintln("testop manhattanBaseMaxLv base null, playerId: {}", player.getId());
			return;
		}
		
		int threadNum = HawkTaskManager.getInstance().getThreadNum();
		HawkTaskManager.getInstance().postTask(new HawkTask() {
			@Override
			public Object run() {
				try {
					ManhattanBaseStageCfg config = ManhattanBaseStageCfg.getConfigByStage(base.getStage() + 1);
					while (config != null) {
						base.stageUpgrade();
						base.notifyChange();
						config = ManhattanBaseStageCfg.getConfigByStage(base.getStage() + 1);
					}
					
					for (int posId = 1; posId <= 3; posId++) {
						ManhattanBaseLevelCfg levelConfig = ManhattanBaseLevelCfg.getConfig(posId, base.getPosLevel(posId) + 1);
						while (levelConfig != null) {
							base.levelUpgrade(posId);
							base.notifyChange();
							levelConfig = ManhattanBaseLevelCfg.getConfig(posId, base.getPosLevel(posId) + 1);
						}
					}
					
					PlayerManhattanModule module = player.getModule(GsConst.ModuleType.MANHATTAN);
					module.syncManhattanInfo();
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				return null;
			}
		}, player.getXid().getHashThread(threadNum));
		
	}
	
	/**
	 * 超级武器一键满级
	 * @param player
	 */
	private void manhattanSWMaxLv(Player player) {
		List<PlayerManhattan> list = player.getAllManhattanSW();
		if (list.isEmpty()) {
			return;
		}
		
		int threadNum = HawkTaskManager.getInstance().getThreadNum();
		HawkTaskManager.getInstance().postTask(new HawkTask() {
			@Override
			public Object run() {
				try {
					for (PlayerManhattan manhattan : list) {
						int swId = manhattan.getSWCfgId();
						ManhattanSWStageCfg config = ManhattanSWStageCfg.getConfig(swId, manhattan.getStage() + 1);
						while (config != null) {
							manhattan.stageUpgrade();
							manhattan.notifyChange();
							config = ManhattanSWStageCfg.getConfig(swId, manhattan.getStage() + 1);
						}
						
						for (int posId = 1; posId <= 4; posId++) {
							ManhattanSWLevelCfg lvConfig = ManhattanSWLevelCfg.getConfig(swId, posId, manhattan.getPosLevel(posId) + 1);
							while (lvConfig != null) {
								manhattan.levelUpgrade(posId);
								manhattan.notifyChange();
								lvConfig = ManhattanSWLevelCfg.getConfig(swId, posId, manhattan.getPosLevel(posId) + 1);
							}
						}
					}
					
					PlayerManhattanModule module = player.getModule(GsConst.ModuleType.MANHATTAN);
					module.syncManhattanInfo();
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				return null;
			}
		}, player.getXid().getHashThread(threadNum));
		
	}
	
	
	/**
	 * 核心勘探活动行数跳跃
	 * @param params
	 * @return
	 */
	public String activity369Skip(Map<String, String> params) {
		Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
		if (player == null) {
			return HawkScript.failedResponse(-1, "角色不存在");
		}
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.MECHA_CORE_EXPLORE_VALUE);
		if (!opActivity.isPresent() || !opActivity.get().isOpening(player.getId())) {
			return HawkScript.failedResponse(-1, "该玩家的369活动还未开启");
		}
		
		int line = Integer.parseInt(params.get("line"));
		CoreExploreActivity activity = (CoreExploreActivity) opActivity.get();
		activity.skipToLineGM(player.getId(), line);
		return HawkScript.successResponse("activity369Skip");
	}
	
	/**
	 * 移除个人活动相关排行榜数据
	 * @param params
	 * @return
	 */
	private String removePlayerRank(Map<String, String> params) {
		String playerId = params.get("playerId");
		if (HawkOSOperator.isEmptyString(playerId)) {
			return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, params.toString());
		}

		try {
			Method method = HawkOSOperator.getClassMethod(ImmgrationService.getInstance(), "clearActivityInfo", String.class);
			method.invoke(ImmgrationService.getInstance(), playerId);
		} catch (Exception e) {
			HawkException.catchException(e);
			return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, e.getMessage());
		}
		return HawkScript.successResponse("removePlayerRank success");
	}
	
	/**
	 * 查询充值数
	 * @param params
	 * @return
	 */
	private String rechargeQuery(Map<String, String> params) {
		Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
		if (player == null) {
			return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
		}
		
//		//军魂传承功能累计充值金条数
//		Map<String, Integer> rechargeTotalMap = RedisProxy.getInstance().getAllRoleRechargeTotal(player.getOpenId());
//		json.put("inheriteRechargeTotal", rechargeTotalMap.getOrDefault(player.getId(), 0)); 

		int diamonds = player.getPlayerBaseEntity().getSaveAmtTotal();
		int payGiftTotal = 0; 
		List<RechargeEntity> entities = player.getData().getPlayerRechargeEntities();
		payGiftTotal = entities.stream().filter(e -> e.getType() == RechargeType.GIFT).mapToInt(e -> e.getPayMoney()).sum();
		
//		List<ZSetRechargeInfo> recordList = player.getData().getRechargeRecordByTime(0, HawkTime.getMillisecond());
//		for (ZSetRechargeInfo record : recordList) {
//			if (record.getType() == RechargeType.GIFT) {
//				payGiftTotal += record.getPayMoney();
//			}
//		}

		StringBuilder sb = new StringBuilder();
		sb.append(" <br> ").append("allRechargeDiamonds（直购+直充）: ").append(diamonds + payGiftTotal)
		  .append(" <br> ").append("diamondRechargeTotal（直充）: ").append(diamonds)
		  .append(" <br> ").append("giftRechargeTotal（直购）: ").append(payGiftTotal)
		  .append(" <br> ");

		return HawkScript.successResponse(sb.toString());
	}
	
	private void finishNationTech(int techId, int level) {
		NationTechCenter center = (NationTechCenter)NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_TECH_CENTER);
		if (center == null || center.getLevel() <= 0) {
			return;
		}
		center.updateNationTechInfo(techId, level);
	}
	
	private void changeNationTech(int value) {
		if (value <= 0) {
			return;
		}
		NationTechCenter center = (NationTechCenter)NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_TECH_CENTER);
		if (center == null || center.getLevel() <= 0) {
			return;
		}
		center.changeNationTechValue(value);
	}
	
	/**
	 * 给全服添加保护罩
	 * @param minutes
	 * @return
	 */
	private String addCityShield(int minutes) {
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				List<AccountInfo> accountList = new ArrayList<>();
				GlobalData.getInstance().getAccountList(accountList);
				long newShieldTime = HawkTime.getMillisecond() + minutes * HawkTime.MINUTE_MILLI_SECONDS;
				for (AccountInfo account : accountList) {
					Player player = GlobalData.getInstance().makesurePlayer(account.getPlayerId());
					if (player == null) {
						continue;
					}
					
					long oldShieldTime = player.getData().getCityShieldTime();
					if (newShieldTime > oldShieldTime) {
						StatusDataEntity addStatusBuff = player.getData().addStatusBuff(EffType.CITY_SHIELD_VALUE, newShieldTime);
						if (addStatusBuff != null) {
							WorldPlayerService.getInstance().updateWorldPointProtected(player.getId(), addStatusBuff.getEndTime());
							player.getPush().syncPlayerStatusInfo(false, addStatusBuff);
						}
						
						HawkLog.logPrintln("testOp add city shield success, playerId: {}, endTime: {}", player.getId(), HawkTime.formatTime(player.getData().getCityShieldTime()));
					}
				}
				return null;
			}
		});
		
		return HawkScript.successResponse("succ");
	}
	
	/**
	 * 联盟科技升级   http://localhost:8080/script/testOp?op=guildScienceUp&guildId=1aat-336eav-1
	 * @param guildId
	 */
	private String guildScienceUp(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return HawkScript.failedResponse(-1, "联盟id参数为空不合法");
		}
		
		GuildInfoObject object = GuildService.getInstance().getGuildInfoObject(guildId);
		if (object == null) {
			return HawkScript.failedResponse(-1, "对应联盟不存在");
		}
		
		List<GuildScienceEntity> lvlUpList = new ArrayList<>();
		ConfigIterator<GuildScienceMainCfg> it = HawkConfigManager.getInstance().getConfigIterator(GuildScienceMainCfg.class);
		for(GuildScienceMainCfg mcfg : it){
			GuildScienceEntity scienceEntity = GuildService.getInstance().getGuildScience(guildId, mcfg.getId());
			if (scienceEntity == null){
				scienceEntity = new GuildScienceEntity();
				scienceEntity.setGuildId(guildId);
				scienceEntity.setScienceId(mcfg.getId());
				HawkDBManager.getInstance().create(scienceEntity);
				GuildService.getInstance().getGuildScienceList(guildId).add(scienceEntity);
			}
			while(!GuildService.getInstance().isScienceMaxLvl(scienceEntity)){
				scienceEntity.setLevel(scienceEntity.getLevel() + 1);
			}
			
			lvlUpList.add(scienceEntity);
		}
		
		GuildService.getInstance().calcTechEffec(guildId);
		GuildService.getInstance().syncGuildTechEffect(guildId);
		// 科技信息同步
		GuildScienceInfoSync.Builder builder = GuildScienceInfoSync.newBuilder();
		for (GuildScienceEntity entity : lvlUpList) {
			builder.addScienceInfo(buildGuildScienceInfo(entity));
		}
		
		Method method = HawkOSOperator.getClassMethod(GuildService.getInstance(), "getGuildScienceFloor", String.class);
		try {
			int floor = (int) method.invoke(GuildService.getInstance(), guildId);
			builder.setScienceFloor(floor);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		GuildService.getInstance().broadcastProtocol(guildId, HawkProtocol.valueOf(HP.code.GUILD_SCIENCE_INFO_SYNC_S, builder));
		GuildService.getInstance().broadcastGuildInfo(guildId);
		return HawkScript.successResponse("succ");
	}
	
	/**
	 * 联盟科技升级相关
	 * @param science
	 * @return
	 */
	private GuildScienceInfo.Builder buildGuildScienceInfo(GuildScienceEntity science) {
		GuildScienceInfo.Builder scienceInfo = GuildScienceInfo.newBuilder();
		scienceInfo.setScienceId(science.getScienceId());
		scienceInfo.setLevel(science.getLevel());
		scienceInfo.setStar(science.getStar());
		scienceInfo.setDonate(science.getDonate());
		scienceInfo.setRecommend(science.isRecommend());
		if (science.getFinishTime() > 0) {
			scienceInfo.setFinishTime(science.getFinishTime());
		}
		scienceInfo.setLimitOpenTime(science.getOpenLimitTime());
		return scienceInfo;
	}
	
	/**
	 * 产生霸主：需要改xml/star_wars_part.xml
	 * 
	 * @return
	 */
	private String genWorldKing(Map<String, String> params) {
		Player targetPlayer = GlobalData.getInstance().scriptMakesurePlayer(params);
		if (targetPlayer == null) {
			return HawkScript.failedResponse(-1, "参数错误，不存在对应玩家");
		}
		
		if (!GlobalData.getInstance().isLocalPlayer(targetPlayer.getId())) {
			return HawkScript.failedResponse(-1, "不是本服玩家，不可设定霸主");
		}
		
		if (!targetPlayer.hasGuild()) {
			return HawkScript.failedResponse(-1, "玩家未加入联盟，不可设定霸主");
		}
		
		if (!GuildService.getInstance().isGuildLeader(targetPlayer.getId())) {
			return HawkScript.failedResponse(-1, "只有盟主才能设定为霸主");
		}
		
		int part = GsConst.StarWarsConst.WORLD_PART;
		int team = GsConst.StarWarsConst.TEAM_NONE;
		int worldKingOfficerId = StarWarsOfficerService.getInstance().getKingOfficerIdByPart(part, team);
		CrossPlayerStruct.Builder crossPlayerBuilder = BuilderUtil.buildCrossPlayer(targetPlayer);
		StarWarsOfficerStruct.Builder officerBuilder = StarWarsOfficerService.getInstance().buildStarWarsOfficerStruct(crossPlayerBuilder, part, worldKingOfficerId);
		//这里是二次设置时间了,所以这里我们修改一下时间
		officerBuilder.setEndSetTime(HawkTime.getSeconds() - 1);
		officerBuilder.setState(GsConst.StarWarsConst.UPDATED);
		RedisProxy.getInstance().updateStarWarsOfficer(part, team, officerBuilder.build(), 0);
		// 重新reload
		StarWarsOfficerService.getInstance().loadOrReloadOfficer();
		int termId = StarWarsActivityService.getInstance().getTermId();
		StarWarsOfficerService.getInstance().createStarWarsKingRecord(crossPlayerBuilder, termId, part, team);
		@SuppressWarnings("deprecation")
		String mainId = GlobalData.getInstance().getMainServerId(targetPlayer.getServerId());
		MailParames.Builder mailParames = MailParames.newBuilder();
		mailParames.setMailId(MailId.SW_WORLD_KING_CHANGE);
		mailParames.addContents(mainId, targetPlayer.getGuildTag(), targetPlayer.getName());
		SystemMailService.getInstance().addGlobalMail(mailParames.build(), HawkTime.getMillisecond(), HawkTime.DAY_MILLI_SECONDS * 10);
		
		StarWarsOfficerService.getInstance().synStarWarsOfficer(targetPlayer);
		ChatParames.Builder chatBuilder = ChatParames.newBuilder();
		chatBuilder.setKey(NoticeCfgId.CHANGE_STAR_WARS_KING);
		chatBuilder.setChatType(ChatType.SPECIAL_BROADCAST);
		chatBuilder.addParms(mainId);
		chatBuilder.addParms(targetPlayer.getGuildTag());
		chatBuilder.addParms(targetPlayer.getName());
		ChatService.getInstance().addWorldBroadcastMsg(chatBuilder.build());			
		StarWarsOfficerService.getInstance().synEffect(targetPlayer, Arrays.asList(worldKingOfficerId));
		return HawkScript.successResponse("succ");
	}
	
	
	/**
	 * 开启国王战
	 */
	private String startPresidentWar() {
		if (PresidentFightService.getInstance().isFightPeriod()) {
			return "already fight yet";
		}
		
		long oldStartTime = PresidentFightService.getInstance().getPresidentCity().getStartTime();
		long newStartTime = HawkTime.getMillisecond() + 60000L;
		PresidentFightService.getInstance().getPresidentCity().setStartTime(Math.min(oldStartTime, newStartTime));
		return "president war is going to start after 1 minute";
	}
	
	/**
	 * 发送idip通知消息
	 * @param params
	 */
	protected void sendIdipNotice(Map<String, String> params) {
		Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
		if (player == null) {
			return;
		}
		
		player.sendIdipNotice(NoticeType.BAN_MSG, NoticeMode.NOTICE_MSG, 0, IdipMsgCode.IDIP_BAN_MSG_RELEASE_VALUE);
	}
	
	/**
	 * 发起注销账号
	 * @param params
	 */
	private String testAccountCancel(Map<String, String> params) {
		Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
		if (player == null) {
			return HawkScript.failedResponse(-1, "角色不存在");
		}
		
		AccountCancellationResp.Builder builder = AccountCancellationResp.newBuilder();
		builder.setSuccess(true);
		HawkLog.logPrintln("testOp certification success, playerId:{}", player.getId());

		RedisProxy.getInstance().updateAccountCancellationCheckTime(player.getId());
		GlobalData.getInstance().updateAccountCancellationInfo(player.getId());

		builder.setApplicationTime(HawkTime.getMillisecond());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.ACCOUNT_CANCELLATION_RESP, builder));
		return HawkScript.successResponse("succ");
	}
	
	/**
	 * 添加核心机甲模块
	 * @param player
	 */
	private void mechacoreModuleAdd(Player player) {
		int threadNum = HawkTaskManager.getInstance().getThreadNum();
		HawkTaskManager.getInstance().postTask(new HawkTask() {
			@Override
			public Object run() {
				player.getPlayerMechaCore().gachaStart();
				ConfigIterator<MechaCoreModuleCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(MechaCoreModuleCfg.class);
				while (iterator.hasNext()) {
					MechaCoreModuleCfg cfg = iterator.next();
					player.getPlayerMechaCore().addModule(cfg.getId());
				}
				player.getPlayerMechaCore().gachaEnd();
				return null;
			}
			
		}, player.getXid().getHashThread(threadNum));
	}
	
	/**
	 * 清空机甲核心模块
	 * @param player
	 */
	private void mechacoreModuleClear(Player player) {
		int threadNum = HawkTaskManager.getInstance().getThreadNum();
		HawkTaskManager.getInstance().postTask(new HawkTask() {
			@Override
			public Object run() {
				List<MechaCoreModuleEntity> removeList = new ArrayList<>();
				List<MechaCoreModuleEntity> moduleList = player.getData().getMechaCoreModuleEntityList();
				for (MechaCoreModuleEntity module : moduleList) {
					if (!module.isLoaded()) { //已经装载到槽位上的模块不能分解
						removeList.add(module);
					}
				}
				
				if (!removeList.isEmpty()) {
					moduleList.removeAll(removeList);
					removeList.forEach(e -> e.delete(true));
					player.getPlayerMechaCore().syncAllModuleInfo();
				}
				return null;
			}
			
		}, player.getXid().getHashThread(threadNum));
	}
	
	/**
	 * 机甲核心科技一键满级
	 * @param player
	 */
	private void mechacoreTechUpMax(Player player) {
		PlayerMechaCore mechacore = player.getPlayerMechaCore();
		//科技升级
		for(int techType : MechaCoreTechLevelCfg.getTypeTechs()) {
			int oldLevel = mechacore.getTechLevel(techType);
			int newLevel = oldLevel + 1;
			MechaCoreTechLevelCfg config = MechaCoreTechLevelCfg.getCfgByLevel(techType, newLevel);
			while (config != null) {
				mechacore.getTechLevelCfgMap().put(techType, config.getId());
				newLevel++;
				config = MechaCoreTechLevelCfg.getCfgByLevel(techType, newLevel);
			}
			mechacore.getEntity().notifyUpdate();
		}
		
		//科技升阶
		int oldLevel = mechacore.getRankLevel();
		int newLevel = oldLevel + 1;
		MechaCoreRankLevelCfg config = MechaCoreRankLevelCfg.getCfgByLevel(newLevel);
		while (config != null) {
			mechacore.getEntity().setRankLevel(newLevel);
			newLevel++;
			config = MechaCoreRankLevelCfg.getCfgByLevel(newLevel);
		}
		
		//外显添加
		boolean update = false;
		ConfigIterator<MechaCoreShowCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(MechaCoreShowCfg.class);
		while (iterator.hasNext()) {
			MechaCoreShowCfg showCfg = iterator.next();
			if (mechacore.getUnlockShowSet().contains(showCfg.getId())) {
				continue;
			}
			if (showCfg.getRankLevelLimit() > 0 && showCfg.getRankLevelLimit() <= newLevel) {
				update = true;
				mechacore.getUnlockShowSet().add(showCfg.getId());
			}
		}
		if (update) {
			mechacore.syncWorldPoint();
		}
		
		mechacore.getEntity();
		mechacore.syncMechaCoreInfo(true);
		mechacore.notifyChange(PowerChangeReason.MECHA_CORE_TECH);
	}
	
	/**
	 * 模拟客户端请求
	 * @param params
	 */
	private String clientProtocol(Map<String, String> params) {
		String playerId = params.get("playerId");
		String[] playerIds = playerId.split(",");
		Player player = GlobalData.getInstance().makesurePlayer(playerIds[0]);
		if (player == null) {
			return HawkScript.failedResponse(-1, "参数错误，不存在对应玩家");
		}
		
		int oper = Integer.parseInt(params.getOrDefault("oper", "0"));
		switch(oper){
			case HP.code2.SCHEDULE_UPDATE_C_VALUE:
				ScheduleUpdateReq.Builder req = ScheduleUpdateReq.newBuilder();
				req.setUuid("1aat-414kzx-1");
				req.setType(9);
				req.setPosX(150);
				req.setPosY(300);
				req.setTitle("lakuang");
				req.setStartTime(1753797540000L);
				req.setContinues(1800);
				sendProtocol(player, HawkProtocol.valueOf(HP.code2.SCHEDULE_UPDATE_C, req));
				break;
			case HP.code2.SCHEDULE_DELETE_C_VALUE:
				ScheduleDeleteReq.Builder req1 = ScheduleDeleteReq.newBuilder();
				req1.addUuid("1aat-414kzx-1");
				sendProtocol(player, HawkProtocol.valueOf(HP.code2.SCHEDULE_DELETE_C, req1));
				break;
			case HP.code2.CORE_EXPLORE_REMOVE_OBSTACLE_C_VALUE:
				CERemoveObstacleReq.Builder builder1 = CERemoveObstacleReq.newBuilder();
				builder1.setLine(Integer.parseInt(params.getOrDefault("line", "0")));
				builder1.setColumn(Integer.parseInt(params.getOrDefault("column", "0")));
				builder1.setType(CEObstacleRemoveType.valueOf(Integer.parseInt(params.getOrDefault("type", "0"))));
				sendProtocol(player, HawkProtocol.valueOf(HP.code2.CORE_EXPLORE_REMOVE_OBSTACLE_C, builder1));
				break;
			case HP.code2.CORE_EXPLORE_BOX_REWARD_C_VALUE:
				CEZoneBoxRewardReq.Builder builder2 = CEZoneBoxRewardReq.newBuilder();
				builder2.setLine(Integer.parseInt(params.getOrDefault("line", "0")));
				builder2.setColumn(Integer.parseInt(params.getOrDefault("column", "0")));
				builder2.setTimes(Integer.parseInt(params.getOrDefault("times", "0")));
				sendProtocol(player, HawkProtocol.valueOf(HP.code2.CORE_EXPLORE_BOX_REWARD_C, builder2));
				break;
			case HP.code2.CORE_EXPLORE_BUY_PICK_C_VALUE:
				CEBuyPickReq.Builder builder3 = CEBuyPickReq.newBuilder();
				builder3.setCount(Integer.parseInt(params.getOrDefault("count", "0")));
				sendProtocol(player, HawkProtocol.valueOf(HP.code2.CORE_EXPLORE_BUY_PICK_C, builder3));
				break;
			case HP.code2.CORE_EXPLORE_TECH_OPER_C_VALUE:
				CETechOperReq.Builder builder4 = CETechOperReq.newBuilder();
				builder4.setTechId(Integer.parseInt(params.getOrDefault("techId", "0")));
				sendProtocol(player, HawkProtocol.valueOf(HP.code2.CORE_EXPLORE_TECH_OPER_C, builder4));
				break;
			case HP.code2.CORE_EXPLORE_EXCHANGE_C_VALUE:
				CEShopExchangeReq.Builder builder5 = CEShopExchangeReq.newBuilder();
				builder5.setShopId(Integer.parseInt(params.getOrDefault("shopId", "0")));
				builder5.setCount(Integer.parseInt(params.getOrDefault("count", "0")));
				sendProtocol(player, HawkProtocol.valueOf(HP.code2.CORE_EXPLORE_EXCHANGE_C, builder5));
				break;
			case HP.code2.SHARE_PROS_BIND_ROLE_C_VALUE:
				ShareProperityBindRoleReq.Builder builder6 = ShareProperityBindRoleReq.newBuilder();
				builder6.setPlayerId(params.get("tarPlayerId"));
				sendProtocol(player, HawkProtocol.valueOf(HP.code2.SHARE_PROS_BIND_ROLE_C, builder6));
				break;
			default:
				break;
		}
		
		return HawkScript.successResponse("clientProtocol");
	}
	
	private void sendProtocol(Player player, HawkProtocol protocol) {
		try {
			Field field = HawkOSOperator.getClassField(protocol, "session");
			field.setAccessible(true);
			field.set(protocol, player.getSession());
			player.onProtocol(protocol);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
}