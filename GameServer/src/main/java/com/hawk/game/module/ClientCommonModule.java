package com.hawk.game.module;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.cryption.HawkMd5;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.ActivityTimeLimitBuy.TimeLimitLogInfo;
import com.hawk.game.protocol.Common.HPClientLogRecordReq;
import com.hawk.game.protocol.Common.LogType;
import com.hawk.game.protocol.ConfigCheck.CfgCheckReq;
import com.hawk.game.protocol.ConfigCheck.CfgCheckResp;
import com.hawk.game.protocol.ConfigCheck.CfgInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SysProtocol.ClientEventReport;
import com.hawk.game.protocol.SysProtocol.ClientTlogReq;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.ActivityClickType;
import com.hawk.log.LogConst.ClickEventType;

/**
 * 客户端相关业务支撑模块
 *
 * @author Jesse
 */
public class ClientCommonModule extends PlayerModule {
	static final Logger logger = LoggerFactory.getLogger("Client");

	/**
	 * 配置MD5值<name, Md5>
	 */
	private static Map<String, String> cfgMd5s = new HashMap<>();

	/**
	 * 构造函数
	 *
	 * @param player
	 */
	public ClientCommonModule(Player player) {
		super(player);
	}

	/**
	 * 客户端配置校验,仅限debug模式下处理
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.CFG_CHECK_C_VALUE)
	private boolean clientCfgCheck(HawkProtocol protocol) {
		if (!GsConfig.getInstance().isDebug()) {
			return false;
		}

		CfgCheckReq req = protocol.parseProtocol(CfgCheckReq.getDefaultInstance());
		List<CfgInfo> cfgs = req.getCfgInfoList();
		CfgCheckResp.Builder resp = CfgCheckResp.newBuilder();

		if (cfgs != null && cfgs.size() > 0) {
			String cfgPath = HawkOSOperator.getWorkPath() + "xml/";
			for (CfgInfo cfg : cfgs) {
				String cfgName = cfg.getName();
				String cfgMd5 = cfg.getMd5();
				if (HawkOSOperator.isEmptyString(cfgMd5)) {
					logger.debug("client cfg md5 is empty cfgName:{}", cfgName);
					continue;
				}

				String cfgMd5Server = cfgMd5s.get(cfgName);
				if (HawkOSOperator.isEmptyString(cfgMd5Server)) {
					cfgMd5Server = HawkMd5.makeMD5(new File(cfgPath + cfgName));
					if (!HawkOSOperator.isEmptyString(cfgMd5Server)) {
						cfgMd5s.put(cfgName, cfgMd5Server);
					} else {
						logger.debug("server cfg md5 is empty cfgName: {}", cfgName);
						continue;
					}
				}

				if (!cfgMd5Server.equals(cfgMd5)) {
					resp.addCfgName(cfgName);
				}
			}
		}

		player.sendProtocol(HawkProtocol.valueOf(HP.code.CFG_CHECK_S_VALUE, resp));
		return true;
	}

	/**
	 * 客户端日志存储(仅限Debug模式下可用)
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.CLIENT_LOG_RECORD_C_VALUE)
	private boolean clientLogRecord(HawkProtocol protocol) {
		HPClientLogRecordReq req = protocol.parseProtocol(HPClientLogRecordReq.getDefaultInstance());
		if (!GsConfig.getInstance().isClientAnalyzer()) {
			return false;
		}
		
		if (req.getLogType().equals(LogType.DEBUG_LOG)) {
			logger.debug("client log record: playerId: {}, logInfo: {}", player.getId(), req.getLogInfo());
		}
		return true;
	}

	/**
	 * 客户配置拉取
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.CLIENT_CONFIG_C_VALUE)
	private boolean clientCfgReq(HawkProtocol protocol) {
		player.getPush().syncClientCfg();
		return true;
	}

	/**
	 * 客户端事件上报
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.CLIENT_ENENT_REPORT_VALUE)
	private boolean clientEventReport(HawkProtocol protocol) {
		ClientEventReport req = protocol.parseProtocol(ClientEventReport.getDefaultInstance());
		// 数据超出限制则不处理
		if (protocol.getSize() > GameConstCfg.getInstance().getClientEventSizeLimit()) {
			return false;
		}
		try{
			switch (req.getType()) {
				case ACTIVITY_BUTTON_CLICK: {  // 活动点击事件上报
					String args = req.getArgs();
					String[] argList = args.split("_");
					LogUtil.logActivityClickFlow(player, ActivityClickType.PARTICIPATION_CLICK, argList);
					break;
				}
				case FIRST_RECHARGE_NOTICE: {  // 首充点击事件上报
					LogUtil.logFirstRechargeFlow(player, req.getArgs());
					break;
				}
				case PUSH: {  // 客户端推送数据存储
					saveClietPushEvent(req.getArgs());
					break;
				}
				case ENTER_GIFT: {  // 点击进入超值礼包事件
					LogUtil.logEnterGift(player);
					break;
				}
				case GAME_VIDEO: {  // 真人视频点击事件
					String[] args = req.getArgs().split("_");
					LogUtil.logGameVideo(player, args[0], args.length > 1 ? args[1] : "0");
					break;
				}
				case ENTER_EVA_HOTLINE:  // 伊娃热线相关
				case QUERY_QUESTION_EVA:
				case STRATEGY_CLICK_EVA: {
					LogUtil.logEvaHotlineEvent(player, req.getType().getNumber(), req.getArgs());
					break;
				}
				case WARRIOR_KING_ENTER:{  //英雄试炼进入
					String[] args = req.getArgs().split("_");
					if(null != args && args.length == 1){
						LogUtil.logWarriorKingEnterEvent(player, req.getType().getNumber(), args[0]);
					}
					break;			
				}
				case WARRIOR_KING_END:{ // 英雄试炼完成
					String[] args = req.getArgs().split("_");
					if(null != args && args.length == 2){
						LogUtil.logWarriorKingEndEvent(player, req.getType().getNumber(), args[0], args[1]);
					}
					break;
				}
				case MECHA: {  // 机甲剧情
					LogUtil.logMechaScenario(player, Integer.parseInt(req.getArgs()));
				}
				case FRIEND_ENTRANCE_CLICK: { // 密友入口点击
					LogUtil.logClickEvent(player, ClickEventType.FRIEND_ENTRANCE_CLICK);
				}
				
				case SCENE_ENTRANCE_CLICK: {  // 场景入口点击
					if (!HawkOSOperator.isEmptyString(req.getArgs())) {
						LogUtil.logScenarioEntrance(player, Integer.parseInt(req.getArgs()));
					} else {
						HawkLog.errPrintln("scene_entrance_click report error, playerId: {}", player.getId());
					}
				}
				
				default:
					break;
			}		
		}catch(Exception e){
			HawkException.catchException(e);
		}

		
		logger.info("client event report, playerId: {}, platForm: {}, channel: {}, deviceId: {}, type: {}, args: {}", player.getId(), player.getPlatform(), player.getChannel(), player.getDeviceId(), req.getType(), req.getArgs());
		return true;
	}
	
	/**
	 * 客户端推送数据存储
	 * @param args
	 */
	private void saveClietPushEvent(String args) {
		try {
			JSONObject pushData = JSONObject.parseObject(args);
			if (pushData == null) {
				return;
			}
			
			for (String pushKey : pushData.keySet()) {
				if (!pushKey.equals("register") && !pushKey.equals("notify") && !pushKey.equals("click")) {
					continue;
				}
				
				RedisProxy.getInstance().savePushData(pushKey, pushData.getIntValue(pushKey));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 调用xmlreload脚本时清空配置mds缓存
	 */
	public static void onXmlReload() {
		cfgMd5s.clear();
	}
	
	/**
	 * 限时抢购客户端打点记录
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.TIMELIMIT_LOG_REQ_VALUE)
	private boolean timeLimitClientLog(HawkProtocol protocol) {
		TimeLimitLogInfo req = protocol.parseProtocol(TimeLimitLogInfo.getDefaultInstance());
		int type = req.getLogType();
		int activityId = req.getActivityId();
		int goodsId = req.getGoodsId();
		int diamonds = player.getDiamonds();
		int rechargeId = req.getRechargeId();
		LogUtil.logTimeLimitClientLog(player, type, activityId, goodsId, diamonds, rechargeId);
		return true;
	}
	
	/**
	 * 客户端打点日志上报
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.CLIENT_TLOG_REQ_VALUE)
	private boolean clientTlogReq(HawkProtocol protocol) {
		ClientTlogReq req = protocol.parseProtocol(ClientTlogReq.getDefaultInstance());
		String logType = req.getLogType();
		String logInfo = req.getLogInfo();
		LogUtil.logClientTlog(player, logType, logInfo);
		player.responseSuccess(protocol.getType());
		return true;
	}
}