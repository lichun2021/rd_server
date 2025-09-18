package com.hawk.game.idipscript.online;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicInteger;

import com.hawk.game.invoker.xhjz.XHJZWarChangeNameMsgInvoker;
import com.hawk.game.service.xhjzWar.XHJZWarService;
import org.hawk.app.HawkApp;
import org.hawk.callback.HawkCallback;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.uuid.HawkUUIDGenerator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsApp;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.CustomKeyCfg;
import com.hawk.game.config.CyborgConstCfg;
import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.config.PresidentConstCfg;
import com.hawk.game.config.VipCfg;
import com.hawk.game.config.VipSuperCfg;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.PlayerRelationEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.manor.GuildManorObj;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.invoker.PlayerChangeNameMsgInvoker;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.module.PlayerWorldModule;
import com.hawk.game.player.Player;
import com.hawk.game.president.PresidentOfficier;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.GuildManager.GuildSign;
import com.hawk.game.protocol.GuildManor.GuildManorList;
import com.hawk.game.protocol.Mail.UpdateChatRoom;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.MailConst.SysMsgType;
import com.hawk.game.protocol.RedisMail.ChatData;
import com.hawk.game.protocol.RedisMail.ChatRoomData;
import com.hawk.game.protocol.RedisMail.MemberData;
import com.hawk.game.protocol.World.HPWorldFavoriteSync;
import com.hawk.game.protocol.World.PlayerPresetMarchInfo;
import com.hawk.game.protocol.World.WorldFavoritePB;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MailService;
import com.hawk.game.service.RelationService;
import com.hawk.game.service.SearchService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.cyborgWar.CyborgWarService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.PersonalMailService;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.tiberium.TiberiumWarService;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GuildUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.ChangeContentType;
import com.hawk.game.util.GsConst.RelationType;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.log.Action;
import com.hawk.log.Source;
import com.hawk.log.LogConst.LogMsgType;
import com.hawk.tsssdk.util.UicNameDataInfo;

/**
 * 修改玩家文本信息（AQ） -- 10282833
 *
 * localhost:8081/idip/4437
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4437")
public class DoChangeTextHandler extends IdipScriptHandler {
	
	static final int PLAYER_NAME         = 1; // 个人昵称
	static final int SIGNATURE           = 2; // 个性签名
	static final int GUILD_NAME          = 3; // 联盟名字
	static final int GUILD_NOTICE        = 4; // 联盟公告
	static final int GUILD_MANOR         = 5; // 联盟堡垒名字
	static final int GUILD_ANNOUNCE      = 6; // 联盟宣言
	static final int GUILD_SIGN          = 7; // 联盟标记
	static final int GUILD_LEVELNAME     = 8; // 联盟阶级
	static final int GUILD_TAG           = 9; // 联盟简称
	static final int KING_NOTICE         = 10;// 国王公告
	static final int CYBOR_TEAM_NAME     = 11;// 赛博队伍名
	static final int TIBERIUM_TEAM_NAME  = 12;// 赛博队伍名
	static final int CHAT_ROOM_NAME      = 13;// 群聊名
	static final int FRIEND_TXT          = 14;// 好友备注
	static final int ENERGY_MATRIX       = 15;// 能量矩阵
	static final int MARCH_PRSET_NAME    = 16;// 部队编队
	static final int EQUIP_MARSHALLING   = 17;// 装备编组
	static final int WORLD_FAVORITE      = 18;// 世界收藏
	
	static final int ALL                 = 99;// 全部（太多太杂，全部修改基本不可能，这里也不支持）
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		// 修改原因（提示语）
		String reason = request.getJSONObject("body").getString("OperateReason");
		// 联盟ID
		String guildId = request.getJSONObject("body").getString("AllianceId");
		// 修改类型（1、个人昵称,2、个性签名,3、联盟名字,4、联盟公告,5、联盟堡垒名字,6、联盟宣言,7、联盟标记,8、联盟阶级,9、联盟简称,10、国王公告,11、赛博队伍名,12、泰伯利亚队伍名,13、群聊名,14、好友备注,15、能量矩阵,16、部队编队,17、装备编组,18、世界收藏,99全部）
		int type = request.getJSONObject("body").getIntValue("Type");
		// 修改后内容输入
		String content = request.getJSONObject("body").getString("Content");
		String param = request.getJSONObject("body").getString("ParamId");  
		
		reason = IdipUtil.decode(reason);
		content = IdipUtil.decode(content);
		// 修改文本
		HawkTuple2<Integer, String> tuple = changeText(result, player, type, content, guildId, param);
		// 操作成功
		if (tuple.first == 0) {
			LogUtil.logIdipSensitivity(player, request, 0, 0);
			// 发提示邮件
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
					.setPlayerId(player.getId())
					.setMailId(MailId.SYSTEM_NOTICE)
					.setAwardStatus(MailRewardStatus.NOT_GET)
					.addSubTitles("系统修改文本")
					.addContents(reason)
					.build());
		}
		
		result.getBody().put("Result", tuple.first);
		result.getBody().put("RetMsg", tuple.second);
		result.getBody().put("OperId", HawkTime.getSeconds());
		return result;
	}
	
	/**
	 * 修改文本
	 * @param result
	 * @param player
	 * @param type
	 * @param text
	 * @param guildId
	 * @return
	 */
	private HawkTuple2<Integer, String> changeText(IdipResult result, Player player, int type, String text, String guildId, String param) {
		switch(type) {
		case PLAYER_NAME:
			return changePlayerName(result, player, text);
		case SIGNATURE:
			return changePlayerSignature(result, player, text);
		case GUILD_NAME:
			return changeGuildName(result, guildId, text);
		case GUILD_NOTICE:
			return changeGuildNotice(result, guildId, text);
		case GUILD_MANOR:
			return changeGuildManorName(result, player, guildId, text, param);
		case GUILD_ANNOUNCE:
			return changeGuildAnnounce(result, guildId, text);
		case GUILD_SIGN:
			return changeGuildSign(result, player, guildId, text, param);  // 联盟标记id：1-6
		case GUILD_LEVELNAME:
			return changeGuildLevelName(result, guildId, text, param);  // 阶级编号：1-5
		case GUILD_TAG:
			return changeGuildTag(result, guildId, text);
		case KING_NOTICE:
			return changeKingNotice(result, player, text);
		case CYBOR_TEAM_NAME:
			return changeCyborTeamName(result, player, text, param);
		case TIBERIUM_TEAM_NAME:
			return changeTiberTeamName(result, player, text, param);
		case CHAT_ROOM_NAME:
			return changeChatRoomName(result, player, text, param); // roomId: 从tlog的SecTalkFlow表中取title字段
		case FRIEND_TXT:
			return changeFriendRemark(result, player, text, param); // 好友的角色id
		case ENERGY_MATRIX:
			return changeLaboratoryRouteName(result, player, text, param); // 能量矩阵编号：1-8
		case MARCH_PRSET_NAME:
			return changeMarchPresetName(result, player, text, param); // 部队编队的编号: 1_11, 10号编队是给决斗队列用的，界面中看到的10号编队，其实对应的是11号编队
		case EQUIP_MARSHALLING:
			return changeEquipGroupName(result, player, text, param); // suitType: ArmourSuitType枚举的number
		case WORLD_FAVORITE:
			return changeWorldFavorite(result, player, text, param); // favoriteId: 从tlog的SecTalkFlow表中取title字段
		default:
			break;
		}
		
		return new HawkTuple2<Integer, String>(IdipConst.SysError.API_EXCEPTION, "Type param not support");
	}
	
	/**
	 * 玩家改名
	 * 
	 * @param result
	 * @param player
	 * @param name
	 * @return
	 */
	private HawkTuple2<Integer, String> changePlayerName(IdipResult result, Player player, String name) {
		int errCode = GameUtil.tryOccupyPlayerName(player.getId(), player.getPuid(), name);
		if (errCode != Status.SysError.SUCCESS_OK_VALUE || GsApp.getInstance().getWordFilter().hasWord(name)) {
			HawkLog.logPrintln("script change name failed, playerId: {}, tarName: {}, errCode: {}", player.getId(), name, errCode);
			return new HawkTuple2<Integer, String>(IdipConst.SysError.API_EXCEPTION, "name check failed");
		}
		
		AtomicInteger checkResult = new AtomicInteger(-1);
		int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
		GameTssService.getInstance().wordUicNameCheck(name, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				UicNameDataInfo dataInfo = (UicNameDataInfo) args;
				if (dataInfo.msg_result_flag != 0) {
					checkResult.set(1);
					HawkLog.logPrintln("script change name failed, playerId: {}, tarName: {}, result: {}", player.getId(), name, result);
				} else {
					checkResult.set(0);
					changeName(player, name, Action.PLAYER_CHANGE_NAME);
					LogUtil.logSecTalkFlow(player, null, LogMsgType.CHANGE_NAME, "", name);
				}
				
				return 0;
			}
		}, threadIdx);
		
		IdipUtil.wait4AsyncProccess(result, checkResult, "change name failed");
		return new HawkTuple2<Integer, String>(result.getBody().getIntValue("Result"), result.getBody().getString("RetMsg"));
	}
	
	/**
	 * 改名
	 * 
	 * @param name
	 * @param action
	 * @param protoType
	 */
	@SuppressWarnings("deprecation")
	private void changeName(Player player, String name, Action action) {
		// 删除老名字信息
		GameUtil.removePlayerNameInfo(player.getEntity().getName());
		String oriName = player.getEntity().getName();
		
		// 设置当前名字
		player.getEntity().setName(name);
		RedisProxy.getInstance().updateChangeContentTime(player.getId(), ChangeContentType.CHANGE_ROLE_NAME, HawkApp.getInstance().getCurrentTime());
		//改名日志
		BehaviorLogger.log4Service(player, Source.ATTR_CHANGE, action, Params.valueOf("formerName", oriName), Params.valueOf("curName", name));
		
		// 修改全局数据管理器的名字信息
		GlobalData.getInstance().updateAccountInfo(player.getPuid(), player.getServerId(), player.getId(), 
				player.getEntity().getForbidenTime(), name);
		
		// 改玩家联盟信息
		GuildService.getInstance().dealMsg(MsgId.PLAYER_CHANGE_NAME, new PlayerChangeNameMsgInvoker(player));
		XHJZWarService.getInstance().dealMsg(MsgId.XHJZ_NAME_REFRESH, new XHJZWarChangeNameMsgInvoker(player.getId(), player.getName()));
		
		
		// 回复前端请求
		player.getPush().syncPlayerInfo();
		
		// 更新城点数据
		WorldPlayerService.getInstance().updatePlayerPointInfo(player.getId(), player.getName(),
				player.getCityLevel(), player.getIcon(), player.getData().getPersonalProtectVals());
		
		// 更新搜索服务信息
		SearchService.getInstance().removePlayerInfo(oriName);
		SearchService.getInstance().addPlayerInfo(name, player.getId(), true);
		
		SearchService.getInstance().removePlayerNameLow(oriName, player.getId());
		SearchService.getInstance().addPlayerNameLow(name, player.getId());
	}
	
	/**
	 * 修改玩家签名
	 * @param result
	 * @param player
	 * @param signature
	 * @return
	 */
	private HawkTuple2<Integer, String> changePlayerSignature(IdipResult result, Player player, String signature) {
		if (!GameUtil.canSignatureUse(signature)) {
			return new HawkTuple2<Integer, String>(IdipConst.SysError.PARAM_ERROR, "signature illegal");
		}
		
		AtomicInteger checkResult = new AtomicInteger(-1);
		int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
		GameTssService.getInstance().wordUicNameCheck(signature, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				UicNameDataInfo dataInfo = (UicNameDataInfo) args;
				if (dataInfo.msg_result_flag != 0) {
					checkResult.set(1);
					HawkLog.logPrintln("change signature failed, playerId: {}, signature: {}, result: {}", player.getId(), signature, dataInfo.msg_result_flag);
				} else {
					checkResult.set(0);
					WorldPointService.getInstance().updatePlayerSignature(player.getId(), signature);
					GameUtil.notifyDressShow(player.getId());
					RedisProxy.getInstance().updateChangeContentTime(player.getId(), ChangeContentType.CHANGE_SIGNATURE, HawkApp.getInstance().getCurrentTime());
					HawkLog.logPrintln("change signature success, playerId: {}, signature: {}", player.getId(), signature);
				}
				
				return 0;
			}
		}, threadIdx);
		
		IdipUtil.wait4AsyncProccess(result, checkResult, "change signature failed");
		return new HawkTuple2<Integer, String>(result.getBody().getIntValue("Result"), result.getBody().getString("RetMsg"));
	}
	
	/**
	 * 修改联盟名称
	 * 
	 * @param result
	 * @param guildId
	 * @param guildName
	 */
	private HawkTuple2<Integer, String> changeGuildName(IdipResult result, String guildId, String guildName) {
		GuildInfoObject guildInfo = getGuildObject(guildId);
		if (guildInfo == null) {
			return new HawkTuple2<Integer, String>(IdipConst.SysError.ALLIANCE_NOT_FOUND, "guild not exist");
		}
		
		AtomicInteger checkResult = new AtomicInteger(-1);
		int threadIdx = Math.abs(HawkRand.randInt() % HawkTaskManager.getInstance().getThreadNum());
		GameTssService.getInstance().wordUicNameCheck(guildName, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				UicNameDataInfo dataInfo = (UicNameDataInfo) args;
				if (dataInfo.msg_result_flag != 0) {
					checkResult.set(1);
					HawkLog.logPrintln("script change guild name failed, guildId: {}, tarName: {}, result: {}", guildId, guildName, dataInfo.msg_result_flag);
				} else {
					int retCode = GuildService.getInstance().onChangeGuildName(guildName, guildId);
					checkResult.set(retCode);
					RedisProxy.getInstance().updateChangeContentTime(guildId, ChangeContentType.CHANGE_GUILD_NAME, HawkApp.getInstance().getCurrentTime());
					HawkLog.logPrintln("script change guildName success, guildId: {}, guildName: {}, result: {}", guildId, guildName, retCode);
				}

				return 0;
			}
		}, threadIdx);
		
		IdipUtil.wait4AsyncProccess(result, checkResult, "alliance name change failed");
		return new HawkTuple2<Integer, String>(result.getBody().getIntValue("Result"), result.getBody().getString("RetMsg"));
	}
	
	/**
	 * 修改联盟通告
	 * 
	 * @param result
	 * @param guildId
	 * @param content
	 * @return
	 */
	private HawkTuple2<Integer, String> changeGuildNotice(IdipResult result, String guildId, String content) {
		GuildInfoObject guildInfo = getGuildObject(guildId);
		if (guildInfo == null) {
			return new HawkTuple2<Integer, String>(IdipConst.SysError.ALLIANCE_NOT_FOUND, "guild not exist");
		}
		
		if (!GuildUtil.checkNotice(content)) {
			return new HawkTuple2<Integer, String>(IdipConst.SysError.PARAM_ERROR, "guild notice illegal");
		}
		
		AtomicInteger checkResult = new AtomicInteger(-1);
		int threadIdx = Math.abs(HawkRand.randInt() % HawkTaskManager.getInstance().getThreadNum());
		GameTssService.getInstance().wordUicNameCheck(content, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				UicNameDataInfo dataInfo = (UicNameDataInfo) args;
				if (dataInfo.msg_result_flag != 0) {
					checkResult.set(1);
					HawkLog.logPrintln("script change guild notice failed, guildId: {}, content: {}, result: {}", guildId, content, dataInfo.msg_result_flag);
				} else {
					int retCode = GuildService.getInstance().onPostGuildNotice(content, guildId);
					checkResult.set(retCode);
					RedisProxy.getInstance().updateChangeContentTime(guildId, ChangeContentType.CHANGE_GUILD_NAME, HawkApp.getInstance().getCurrentTime());
					HawkLog.logPrintln("script change guild notice success, guildId: {}, content: {}, result: {}", guildId, content, retCode);
				}

				return 0;
			}
		}, threadIdx);
		
		IdipUtil.wait4AsyncProccess(result, checkResult, "alliance notice change failed");
		return new HawkTuple2<Integer, String>(result.getBody().getIntValue("Result"), result.getBody().getString("RetMsg"));
	}
	
	/**
	 * 修改联盟宣言
	 * 
	 * @param result
	 * @param guildId
	 * @param content
	 * @return
	 */
	private HawkTuple2<Integer, String> changeGuildAnnounce(IdipResult result, String guildId, String content) {
		GuildInfoObject guildInfo = getGuildObject(guildId);
		if (guildInfo == null) {
			return new HawkTuple2<Integer, String>(IdipConst.SysError.ALLIANCE_NOT_FOUND, "guild not exist");
		}
		
		if (!GuildUtil.checkAnnouncement(content)) {
			return new HawkTuple2<Integer, String>(IdipConst.SysError.PARAM_ERROR, "guild annoucement illegal");
		}
		
		AtomicInteger checkResult = new AtomicInteger(-1);
		int threadIdx = Math.abs(HawkRand.randInt() % HawkTaskManager.getInstance().getThreadNum());
		GameTssService.getInstance().wordUicNameCheck(content, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				UicNameDataInfo dataInfo = (UicNameDataInfo) args;
				if (dataInfo.msg_result_flag != 0) {
					checkResult.set(1);
					HawkLog.logPrintln("script change guild annoucement failed, guildId: {}, content: {}, result: {}", guildId, content, dataInfo.msg_result_flag);
				} else {
					int retCode = GuildService.getInstance().onPostGuildAnnouncement(content, guildId);
					checkResult.set(retCode);
					RedisProxy.getInstance().updateChangeContentTime(guildId, ChangeContentType.CHANGE_GUILD_NAME, HawkApp.getInstance().getCurrentTime());
					HawkLog.logPrintln("script change guild annoucement success, guildId: {}, content: {}, result: {}", guildId, content, retCode);
				}

				return 0;
			}
		}, threadIdx);
		
		IdipUtil.wait4AsyncProccess(result, checkResult, "alliance annoucement change failed");
		return new HawkTuple2<Integer, String>(result.getBody().getIntValue("Result"), result.getBody().getString("RetMsg"));
	}
	
	/**
	 * 修改联盟标记
	 * 
	 * @param result
	 * @param guildId
	 * @param content
	 * @return
	 */
	private HawkTuple2<Integer, String> changeGuildSign(IdipResult result, Player player, String guildId, String content, String param) {
		if (player.isCsPlayer()) {
			return new HawkTuple2<Integer, String>(IdipConst.SysError.API_EXCEPTION, "player cross server");
		}
		
		GuildInfoObject guildInfo = getGuildObject(guildId);
		if (guildInfo == null) {
			return new HawkTuple2<Integer, String>(IdipConst.SysError.ALLIANCE_NOT_FOUND, "guild not exist");
		}
		
		int signId = Integer.parseInt(param);
		
		Map<Integer, GuildSign> guildSignMap = GuildService.getInstance().getGuildSignMap(guildId);
		if (guildSignMap == null || !guildSignMap.containsKey(signId)) {
			return new HawkTuple2<Integer, String>(IdipConst.SysError.PARAM_ERROR, "guild sign not exist");
		}
		
		// 标记信息超长
		if(content.length() > GuildConstProperty.getInstance().getAllianceSignExplainLen()){
			return new HawkTuple2<Integer, String>(IdipConst.SysError.PARAM_ERROR, "guild sign length too long");
		}
		
		AtomicInteger checkResult = new AtomicInteger(-1);
		int threadIdx = Math.abs(HawkRand.randInt() % HawkTaskManager.getInstance().getThreadNum());
		GameTssService.getInstance().wordUicNameCheck(content, new HawkCallback() {
			@SuppressWarnings("deprecation")
			@Override
			public int invoke(Object args) {
				UicNameDataInfo dataInfo = (UicNameDataInfo) args;
				if (dataInfo.msg_result_flag != 0) {
					checkResult.set(1);
					HawkLog.logPrintln("script change guild sign failed, guildId: {}, content: {}, result: {}", guildId, content, dataInfo.msg_result_flag);
				} else {
					checkResult.set(0);
					GuildSign.Builder guildSign = guildSignMap.get(signId).toBuilder();
					guildSign.setInfo(content);
					GuildSign obj = guildSign.build();
					guildSignMap.put(signId, obj);
					LocalRedis.getInstance().addGuildSign(guildId, obj);
					// 同步联盟信息
					GuildService.getInstance().broadcastGuildInfo(guildId);
					//联盟红点
					GuildService.getInstance().notifyGuildFavouriteRedPoint(guildId, GsConst.GuildFavourite.TYPE_GUILD_SIGN, 0);
					// 发送联盟消息
					ChatService.getInstance().addWorldBroadcastMsg(ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.ALLIANCE_SIGN_NOTICE,
							player, guildSign.getPosX(), guildSign.getPosY(), guildSign.getId(), guildSign.getInfo());
					HawkLog.logPrintln("script change guild sign success, guildId: {}, signId: {}, content: {}", guildId, signId, content);
				}
				
				return 0;
			}
		}, threadIdx);
		
		IdipUtil.wait4AsyncProccess(result, checkResult, "alliance sign change failed");
		return new HawkTuple2<Integer, String>(result.getBody().getIntValue("Result"), result.getBody().getString("RetMsg"));
	}
	
	/**
	 * 修改联盟阶级名称
	 * 
	 * @param result
	 * @param guildId
	 * @param levelName
	 * @param level
	 * @return
	 */
	private HawkTuple2<Integer, String> changeGuildLevelName(IdipResult result, String guildId, String levelName, String param) {
		String[] levelArr = param.split(",");
		for (String levelStr : levelArr) {
			int level = Integer.parseInt(levelStr.trim());
			if (level < 1 || level > 5) {
				return new HawkTuple2<Integer, String>(IdipConst.SysError.PARAM_ERROR, "guild level illegal");
			}
		}
		
		GuildInfoObject guildInfo = getGuildObject(guildId);
		if (guildInfo == null) {
			return new HawkTuple2<Integer, String>(IdipConst.SysError.ALLIANCE_NOT_FOUND, "guild not exist");
		}
		
		if (!GuildUtil.checkGuildLevelName(levelName)) {
			return new HawkTuple2<Integer, String>(IdipConst.SysError.PARAM_ERROR, "guild levelname illegal");
		}
		
		AtomicInteger checkResult = new AtomicInteger(-1);
		int threadIdx = Math.abs(HawkRand.randInt() % HawkTaskManager.getInstance().getThreadNum());
		GameTssService.getInstance().wordUicNameCheck(levelName, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				UicNameDataInfo dataInfo = (UicNameDataInfo) args;
				if (dataInfo.msg_result_flag != 0) {
					checkResult.set(1);
					HawkLog.logPrintln("script change guild levelname failed, guildId: {}, levelName: {}, result: {}", guildId, levelName, dataInfo.msg_result_flag);
				} else {
					String[] names = new String[5];
					for (String levelStr : levelArr) {
						names[Integer.parseInt(levelStr.trim()) - 1] = levelName;
					}
					int result = GuildService.getInstance().onChangeLevelName(guildId, names);
					checkResult.set(result);
					HawkLog.logPrintln("script change guild levelname success, guildId: {}, levelName: {}, result: {}", guildId, levelName, result);
				}
				
				return 0;
			}
		}, threadIdx);
		
		IdipUtil.wait4AsyncProccess(result, checkResult, "change guild levelname failed");
		return new HawkTuple2<Integer, String>(result.getBody().getIntValue("Result"), result.getBody().getString("RetMsg"));
	}
	
	/**
	 * 修改联盟简称
	 * @param result
	 * @param guildId
	 * @param guildTag
	 * @return
	 */
	private HawkTuple2<Integer, String> changeGuildTag(IdipResult result, String guildId, String guildTag) {
		GuildInfoObject guildInfo = getGuildObject(guildId);
		if (guildInfo == null) {
			return new HawkTuple2<Integer, String>(IdipConst.SysError.ALLIANCE_NOT_FOUND, "guild not exist");
		}
		
		AtomicInteger checkResult = new AtomicInteger(-1);
		int threadIdx = Math.abs(HawkRand.randInt() % HawkTaskManager.getInstance().getThreadNum());
		GameTssService.getInstance().wordUicNameCheck(guildTag, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				UicNameDataInfo dataInfo = (UicNameDataInfo) args;
				if (dataInfo.msg_result_flag != 0) {
					checkResult.set(1);
					HawkLog.logPrintln("script change guild tag failed, guildId: {}, guildTag: {}, result: {}", guildId, guildTag, dataInfo.msg_result_flag);
				} else {
					int retCode = GuildService.getInstance().onChangeGuildTag(guildTag, guildId);
					checkResult.set(retCode);
					HawkLog.logPrintln("script change guild tag success, guildId: {}, guildTag: {}, result: {}", guildId, guildTag, retCode);
				}
				
				return 0;
			}
		}, threadIdx);
		
		IdipUtil.wait4AsyncProccess(result, checkResult, "change guild tag failed");
		return new HawkTuple2<Integer, String>(result.getBody().getIntValue("Result"), result.getBody().getString("RetMsg"));
	}
	
	/**
	 * 修改联盟堡垒名称
	 * 
	 * @param result
	 * @param guildId
	 * @param name
	 * @return
	 */
	private HawkTuple2<Integer, String> changeGuildManorName(IdipResult result, Player player, String guildId, String name, String param) {
		GuildInfoObject guildInfo = getGuildObject(guildId);
		if (guildInfo == null) {
			return new HawkTuple2<Integer, String>(IdipConst.SysError.ALLIANCE_NOT_FOUND, "guild not exist");
		}
		
		int op = GuildUtil.checkGuildManorName(name);
		if (op != Status.SysError.SUCCESS_OK_VALUE) {
			return new HawkTuple2<Integer, String>(IdipConst.SysError.PARAM_ERROR, "guild manor name check failed");
		}
		
		String[] indexArr = param.split(",");
		GuildManorObj[] manors = new GuildManorObj[indexArr.length]; 
		int i = 0;
		for (String index : indexArr) {
			GuildManorObj manor = GuildManorService.getInstance().getManorByIdx(guildId, Integer.parseInt(index.trim()));
			if (manor == null) {
				return new HawkTuple2<Integer, String>(IdipConst.SysError.PARAM_ERROR, "guild manor not exist");
			}
			
			manors[i++] = manor;
		}
		
        AtomicInteger checkResult = new AtomicInteger(-1);
		int threadIdx = Math.abs(HawkRand.randInt() % HawkTaskManager.getInstance().getThreadNum());
		GameTssService.getInstance().wordUicNameCheck(name, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				UicNameDataInfo dataInfo = (UicNameDataInfo) args;
				if (dataInfo.msg_result_flag != 0) {
					checkResult.set(1);
					HawkLog.logPrintln("change guild manor name failed, guildId: {}, tarName: {}, result: {}", guildId, name, dataInfo.msg_result_flag);
					return 0;
				}
				
				checkResult.set(0);
				for (GuildManorObj manor : manors) {
					manor.changeManorName(name);
				}
				RedisProxy.getInstance().updateChangeContentTime(guildId, ChangeContentType.CHANGE_GUILD_MANOR_NAME, HawkApp.getInstance().getCurrentTime());
				
				//推送变化消息
				GuildManorList.Builder builder = GuildManorList.newBuilder();
				//领地哨塔列表
				GuildManorService.getInstance().makeManorBastion(builder, guildId);
				//广播消息
				GuildService.getInstance().broadcastProtocol(guildId, HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_S_VALUE, builder));
				
				HawkLog.logPrintln("change guild manor name succ, guildId: {}, tarName: {}", guildId, name);
				if (player.isActiveOnline()) {
					player.responseSuccess(HP.code.CHANGE_MANOR_NAMES_C_VALUE);
				}
				
				return 0;
			}
		}, threadIdx);
		
		IdipUtil.wait4AsyncProccess(result, checkResult, "guild manor name change failed");
		return new HawkTuple2<Integer, String>(result.getBody().getIntValue("Result"), result.getBody().getString("RetMsg"));
	}
	
	/**
	 * 获取联盟对象
	 * 
	 * @param guildId
	 * @return
	 */
	private GuildInfoObject getGuildObject(String guildId) {
		GuildInfoObject guildInfo = GuildService.getInstance().getGuildInfoObject(guildId);
		if (guildInfo != null) {
			return guildInfo;
		}
		
		try {
			guildId = HawkUUIDGenerator.longUUID2Str(Long.parseLong(guildId));
			guildInfo = GuildService.getInstance().getGuildInfoObject(guildId);
		} catch(Exception e) {
			HawkException.catchException(e);
		}
		
		return guildInfo;
	}
	
	/**
	 * 修改国王公告
	 * 
	 * @param result
	 * @param player
	 * @param content
	 * @return
	 */
	private HawkTuple2<Integer, String> changeKingNotice(IdipResult result, Player player, String content) {
		if (content.length() > PresidentConstCfg.getInstance().getManifestoLength()) {
			return new HawkTuple2<Integer, String>(IdipConst.SysError.PARAM_ERROR, "king notice too long");
		}
		
		AtomicInteger checkResult = new AtomicInteger(-1);
		int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
		GameTssService.getInstance().wordUicNameCheck(content, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				UicNameDataInfo dataInfo = (UicNameDataInfo) args;
				if (dataInfo.msg_result_flag != 0) {
					checkResult.set(1);
					HawkLog.logPrintln("change king notice failed, playerId: {}, content: {}, result: {}", player.getId(), content, dataInfo.msg_result_flag);
				} else {
					int retCode = PresidentOfficier.getInstance().onPresidentManifestoUpdate(player, content);
					checkResult.set(retCode);
				}
				
				return 0;
			}
		}, threadIdx);
		
		IdipUtil.wait4AsyncProccess(result, checkResult, "change king notice failed");
		return new HawkTuple2<Integer, String>(result.getBody().getIntValue("Result"), result.getBody().getString("RetMsg"));
	}
	
	/**
	 * 修改赛博战队名称 
	 * 
	 * @param result
	 * @param player
	 * @param name
	 * @param teamIndex
	 * @return
	 */
	private HawkTuple2<Integer, String> changeCyborTeamName(IdipResult result, Player player, String name, String teamId) {
		int nameLength = GameUtil.getStringLength(name);
		HawkTuple2<Integer, Integer> nameLimit = CyborgConstCfg.getInstance().getNameSize();
		if (nameLength < nameLimit.first || nameLength > nameLimit.second) {
			return new HawkTuple2<Integer, String>(IdipConst.SysError.PARAM_ERROR, "cybor team name too long");
		}
		
		AtomicInteger checkResult = new AtomicInteger(-1);
		int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
		GameTssService.getInstance().wordUicNameCheck(name, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				UicNameDataInfo dataInfo = (UicNameDataInfo) args;
				if (dataInfo.msg_result_flag != 0) {
					checkResult.set(1);
					HawkLog.logPrintln("change cybor teamName failed, playerId: {}, content: {}, result: {}", player.getId(), name, dataInfo.msg_result_flag);
				} else {
					int operationResult = CyborgWarService.getInstance().onEditTeamName(player, teamId, name);
					checkResult.set(operationResult);
				}
				
				return 0;
			}
		}, threadIdx);
		
		IdipUtil.wait4AsyncProccess(result, checkResult, "change cybor team name failed");
		return new HawkTuple2<Integer, String>(result.getBody().getIntValue("Result"), result.getBody().getString("RetMsg"));
	}
	
	/**
	 * 修改泰伯利亚战队名称 
	 * 
	 * @param result
	 * @param player
	 * @param name
	 * @return
	 */
	private HawkTuple2<Integer, String> changeTiberTeamName(IdipResult result, Player player, String name, String param) {
		int teamIndex = Integer.parseInt(param);
		AtomicInteger checkResult = new AtomicInteger(-1);
		int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
		GameTssService.getInstance().wordUicNameCheck(name, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				UicNameDataInfo dataInfo = (UicNameDataInfo) args;
				if (dataInfo.msg_result_flag != 0) {
					checkResult.set(1);
					HawkLog.logPrintln("change tiberium teamName failed, playerId: {}, content: {}, result: {}", player.getId(), name, dataInfo.msg_result_flag);
				} else {
					int resultCode = TiberiumWarService.getInstance().onEditTeamName(player, teamIndex, name, 0);
					checkResult.set(resultCode);
				}
				
				return 0;
			}
		}, threadIdx);
		
		IdipUtil.wait4AsyncProccess(result, checkResult, "change tiberium team name failed");
		return new HawkTuple2<Integer, String>(result.getBody().getIntValue("Result"), result.getBody().getString("RetMsg"));
	}
	
	/**
	 * 修改聊天室名称 
	 * 
	 * @param result
	 * @param player
	 * @param name
	 * @param roomId
	 * @return
	 */
	private HawkTuple2<Integer, String> changeChatRoomName(IdipResult result, Player player, String name, String roomId) {
		ChatRoomData.Builder dataBuilder = LocalRedis.getInstance().getChatRoomData(roomId);
		if (dataBuilder == null) {
			return new HawkTuple2<Integer, String>(IdipConst.SysError.PARAM_ERROR, "chatroom not exist");
		}
		
		AtomicInteger checkResult = new AtomicInteger(-1);
		int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
		GameTssService.getInstance().wordUicNameCheck(name, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				UicNameDataInfo dataInfo = (UicNameDataInfo) args;
				if (dataInfo.msg_result_flag != 0) {
					checkResult.set(1);
					HawkLog.logPrintln("changeChatRoomName failed, playerId: {}, content: {}, result: {}", player.getId(), name, dataInfo.msg_result_flag);
					return 0;
				}
				
				checkResult.set(0);
				// 修改聊天室名称
				dataBuilder.setName(name);
				// 通知聊天室成员推送改名
				List<MemberData> memberDatas = dataBuilder.getMembersList();
				for (MemberData memberData : memberDatas) {
					String playerId = memberData.getPlayerId();
					Player _player = GlobalData.getInstance().getActivePlayer(playerId);
					if (_player == null) {
						continue;
					}

					UpdateChatRoom.Builder builder = UpdateChatRoom.newBuilder();
					builder.setUuid(roomId);
					builder.setName(name);
					_player.sendProtocol(HawkProtocol.valueOf(HP.code.MAIL_UPDATE_CHATROOM_S_VALUE, builder));
				}
				
				// 更新聊天室信息
				LocalRedis.getInstance().addChatRoomData(roomId, dataBuilder);
				// 添加聊天室聊天数据
				String msg = SysMsgType.CHANGE_CHATROOM_VALUE + "_" + player.getName() + "_" + name;
				ChatData chatData = MailService.getInstance().addChatMessage(player, roomId, null, msg);
				// 给玩家发送聊天消息
				for (MemberData memberData : memberDatas) {
					PersonalMailService.getInstance().sendChat(roomId, memberData.getPlayerId(), null, chatData, HawkTime.getMillisecond());
				}
				
				return 0;
			}
		}, threadIdx);
		
		IdipUtil.wait4AsyncProccess(result, checkResult, "change chatroom name failed");
		return new HawkTuple2<Integer, String>(result.getBody().getIntValue("Result"), result.getBody().getString("RetMsg"));
	}
	
	/**
	 * 修改好友备注  
	 * 
	 * @param result
	 * @param player
	 * @param content
	 * @param targetId
	 * @return
	 */
	private HawkTuple2<Integer, String> changeFriendRemark(IdipResult result, Player player, String content, String friendPlayerId) {
		PlayerRelationEntity playerRelationEntity = RelationService.getInstance().getPlayerRelationEntity(player.getId(), friendPlayerId);
		if (playerRelationEntity == null || playerRelationEntity.getType() != RelationType.FRIEND) {
			return new HawkTuple2<Integer, String>(IdipConst.SysError.PARAM_ERROR, "not exist this friend");
		}
		
		AtomicInteger checkResult = new AtomicInteger(-1);
		int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
		GameTssService.getInstance().wordUicNameCheck(content, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				UicNameDataInfo dataInfo = (UicNameDataInfo) args;
				if (dataInfo.msg_result_flag != 0) {
					checkResult.set(1);
					HawkLog.logPrintln("change friend remark failed, playerId: {}, content: {}, result: {}", player.getId(), content, dataInfo.msg_result_flag);
				} else {
					checkResult.set(0);
					PlayerRelationEntity playerRelationEntity = RelationService.getInstance().getPlayerRelationEntity(player.getId(), friendPlayerId);
					playerRelationEntity.setRemark(content);
					LogUtil.logSecTalkFlow(player, null, LogMsgType.FRIEND_REMARK_CHANGE, "", content);
				}
				
				return 0;
			}
		}, threadIdx);
		
		IdipUtil.wait4AsyncProccess(result, checkResult, "change friend remark failed");
		return new HawkTuple2<Integer, String>(result.getBody().getIntValue("Result"), result.getBody().getString("RetMsg"));
	}
	
	/**
	 * 修改能量矩阵名称
	 * 
	 * @param result
	 * @param player
	 * @param name
	 * @return
	 */
	private HawkTuple2<Integer, String> changeLaboratoryRouteName(IdipResult result, Player player, String name, String param) {
		CustomDataEntity customDataEntity = player.getData().getCustomDataEntity(CustomKeyCfg.getLaboratoryRouteNameKey());
		if (customDataEntity == null) {
			customDataEntity = player.getData().createCustomDataEntity(CustomKeyCfg.getLaboratoryRouteNameKey(), 0, "1号矩阵|2号矩阵|3号矩阵|4号矩阵|5号矩阵|6号矩阵|7号矩阵|8号矩阵");
		}
		
		final CustomDataEntity entity = customDataEntity;
		int index = Integer.parseInt(param);
		
		String[] routeNames = entity.getArg().split("\\|");
		if (index <= 0 || index > routeNames.length) {
			return new HawkTuple2<Integer, String>(IdipConst.SysError.PARAM_ERROR, "index illegal");
		}

		AtomicInteger checkResult = new AtomicInteger(-1);
		int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
		GameTssService.getInstance().wordUicNameCheck(name, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				UicNameDataInfo dataInfo = (UicNameDataInfo) args;
				if (dataInfo.msg_result_flag != 0) {
					checkResult.set(1);
					HawkLog.logPrintln("changeLaboratoryRouteName failed, playerId: {}, content: {}, result: {}", player.getId(), name, dataInfo.msg_result_flag);
				} else {
					checkResult.set(0);
					routeNames[index - 1] = name;
					StringJoiner sj = new StringJoiner("|");
					Arrays.stream(routeNames).forEach(e -> sj.add(e));
					entity.setArg(sj.toString());
					if (player.isActiveOnline()) {
						player.getPush().syncCustomData();
					}
				}
				
				return 0;
			}
		}, threadIdx);
		
		IdipUtil.wait4AsyncProccess(result, checkResult, "change energy matrix name failed");
		return new HawkTuple2<Integer, String>(result.getBody().getIntValue("Result"), result.getBody().getString("RetMsg"));
	}
	
	/**
	 * 修改编队名称
	 * 
	 * @param result
	 * @param player
	 * @param content
	 * @param idx
	 * @return
	 */
	private HawkTuple2<Integer, String> changeMarchPresetName(IdipResult result, Player player, String content, String param) {
		int res = GuildUtil.checkPresetMarchName(content);
		if (res != Status.SysError.SUCCESS_OK_VALUE) {
			return new HawkTuple2<Integer, String>(IdipConst.SysError.PARAM_ERROR, "march preset name illegal");
		}
		
		int idx = Integer.parseInt(param);
		int presetNum = ConstProperty.getInstance().getIniTroopTeamNum();
		int vipLevel = player.getVipLevel();
		if (vipLevel > 0) {
			VipCfg cfg = HawkConfigManager.getInstance().getConfigByKey(VipCfg.class, vipLevel);
			presetNum = cfg != null ? cfg.getFormation() : presetNum; 
		}
		
		VipSuperCfg vipSuperCfg = HawkConfigManager.getInstance().getConfigByKey(VipSuperCfg.class, player.getActivatedVipSuperLevel());
		if (vipSuperCfg != null) {
			presetNum += vipSuperCfg.getFormation();
		}
		
		int index = idx - 1;
		int newIndex = idx > GsConst.DUEL_INDEX ? index - 1: index;
		if (index < 0 || newIndex >= presetNum) {
			return new HawkTuple2<Integer, String>(IdipConst.SysError.PARAM_ERROR, "ParamId illegal");
		}
		
		AtomicInteger checkResult = new AtomicInteger(-1);
		int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
		GameTssService.getInstance().wordUicNameCheck(content, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				UicNameDataInfo dataInfo = (UicNameDataInfo) args;
				if (dataInfo.msg_result_flag != 0) {
					checkResult.set(1);
					HawkLog.logPrintln("change march preset name failed, playerId: {}, content: {}, result: {}", player.getId(), content, dataInfo.msg_result_flag);
				} else {
					checkResult.set(0);
					JSONArray arr = new JSONArray(GsConst.MAX_PRESET_SIZE);
					String presetMarchStr = RedisProxy.getInstance().getPlayerPresetWorldMarch(player.getId());
					if (presetMarchStr != null) {
						arr.addAll(JSONArray.parseArray(presetMarchStr));
					}
					
					int index = idx - 1;
					JSONObject obj = new JSONObject();
					if (arr.size() >= idx && arr.getJSONObject(index) != null) {
						obj = arr.getJSONObject(index);
					}
					
					obj.put("name", content);
					arr.set(index, obj);
					RedisProxy.getInstance().addPlayerPresetWorldMarch(player.getId(), arr);
					
					PlayerWorldModule module = player.getModule(GsConst.ModuleType.WORLD_MODULE);
					PlayerPresetMarchInfo.Builder infos = module.makeMarchPresetBuilder();
					player.sendProtocol(HawkProtocol.valueOf(HP.code.MARCH_PRESET_NAME_CHANGE_S, infos));
					LogUtil.logSecTalkFlow(player, null, LogMsgType.WORLD_MARCH_PRESET, "", content);
				}
				
				return 0;
			}
		}, threadIdx);
		
		IdipUtil.wait4AsyncProccess(result, checkResult, "change march preset name failed");
		return new HawkTuple2<Integer, String>(result.getBody().getIntValue("Result"), result.getBody().getString("RetMsg"));
	}
	
	
	/**
	 * 修改装备编组名称
	 * 
	 * @param result
	 * @param player
	 * @param content
	 * @param suitType
	 * @return
	 */
	private HawkTuple2<Integer, String> changeEquipGroupName(IdipResult result, Player player, String content, String param) {
		int suitType = Integer.parseInt(param);
		if (ArmourSuitType.valueOf(suitType) == null) {
			return new HawkTuple2<Integer, String>(IdipConst.SysError.PARAM_ERROR, "ParamId illegal");
		}
		
		AtomicInteger checkResult = new AtomicInteger(-1);
		int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
		GameTssService.getInstance().wordUicNameCheck(content, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				UicNameDataInfo dataInfo = (UicNameDataInfo) args;
				if (dataInfo.msg_result_flag != 0) {
					checkResult.set(1);
					HawkLog.logPrintln("change equip groupName failed, playerId: {}, content: {}, result: {}", player.getId(), content, dataInfo.msg_result_flag);
				} else {
					checkResult.set(0);
					RedisProxy.getInstance().setArmourSuitName(player.getId(), suitType, content);
					player.getPush().syncArmourSuitInfo();
				}
				
				return 0;
			}
		}, threadIdx);
		
		IdipUtil.wait4AsyncProccess(result, checkResult, "change equip group name failed");
		return new HawkTuple2<Integer, String>(result.getBody().getIntValue("Result"), result.getBody().getString("RetMsg"));
	}
	
	/**
	 * 修改玩家世界点收藏
	 * 
	 * @param result
	 * @param player
	 * @param signature
	 * @return
	 */
	private HawkTuple2<Integer, String> changeWorldFavorite(IdipResult result, Player player, String content, String param) {
		List<WorldFavoritePB.Builder> favoriteList = LocalRedis.getInstance().getWorldFavorite(player.getId());
		Optional<WorldFavoritePB.Builder> optional = favoriteList.stream().filter(e -> e.getFavoriteId().equals(param)).findAny();
		if (!optional.isPresent()) {
			return new HawkTuple2<Integer, String>(IdipConst.SysError.PARAM_ERROR, "favorite of ParamId not found");
		}
		
		WorldFavoritePB.Builder favorivatePB = optional.get();
		AtomicInteger checkResult = new AtomicInteger(-1);
		int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
		GameTssService.getInstance().wordUicNameCheck(content, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				UicNameDataInfo dataInfo = (UicNameDataInfo) args;
				if (dataInfo.msg_result_flag != 0) {
					checkResult.set(1);
					HawkLog.logPrintln("change world favorite failed, playerId: {}, content: {}, result: {}", player.getId(), content, dataInfo.msg_result_flag);
				} else {
					checkResult.set(0);
					favorivatePB.setUpdateTime(HawkTime.getMillisecond());
					favorivatePB.setName(content);
					int expireTime = player.isCsPlayer() ? CrossActivityService.getInstance().getCrossKeyExpireTime() : 0;
					LocalRedis.getInstance().addWorldFavorite(player.getId(), favorivatePB, expireTime);
					// 同步
					if (player.isActiveOnline()) {
						HPWorldFavoriteSync.Builder builder = HPWorldFavoriteSync.newBuilder();
						builder.addFavorites(favorivatePB);
						builder.setSynType(1);
						player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_FAVORITE_SYNC_S, builder));
					}
				}
				
				return 0;
			}
		}, threadIdx);
		
		IdipUtil.wait4AsyncProccess(result, checkResult, "change world favorite failed");
		return new HawkTuple2<Integer, String>(result.getBody().getIntValue("Result"), result.getBody().getString("RetMsg"));
	}
	
}


