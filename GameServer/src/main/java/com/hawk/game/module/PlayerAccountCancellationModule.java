package com.hawk.game.module;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.cryption.HawkMd5;
import org.hawk.log.HawkLog;
import org.hawk.net.http.HawkHttpUrlService;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.uuid.HawkUUIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.config.AccountCancellationCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.invoker.AccountCancellationRpcInvoker;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Player.AccountCancellationInfoPush;
import com.hawk.game.protocol.Player.AccountCancellationReq;
import com.hawk.game.protocol.Player.AccountCancellationResp;
import com.hawk.game.protocol.Player.AccountCancellationState;
import com.hawk.game.protocol.President.OfficerType;
import com.hawk.game.protocol.Status.AccountCancellationError;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.RelationService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.l5.L5Helper;
import com.hawk.l5.L5Task;

/**
 * 账号注销模块
 * @author golden
 *
 */
public class PlayerAccountCancellationModule extends PlayerModule {

	public static Logger logger = LoggerFactory.getLogger("Server");

	public PlayerAccountCancellationModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {

		// 移除账号注销信息
		GlobalData.getInstance().rmAccountCancellationInfo(player.getId());
		
		AccountCancellationInfoPush.Builder builder = AccountCancellationInfoPush.newBuilder();
		long lastReqTime = RedisProxy.getInstance().getAccountCancellationCheckTime(player.getId());
		long remainTime = lastReqTime + ConstProperty.getInstance().getAccountCancReqCd() - HawkTime.getMillisecond();
		builder.setCdRemainTime(remainTime);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.ACCOUNT_CANCELLATION_INFO_PUSH, builder));

		return true;
	}

	/**
	 * 账号注销条件检测
	 */
	@ProtocolHandler(code = HP.code.ACCOUNT_CANCELLATION_PER_REQ_VALUE)
	private void onCheck(HawkProtocol protocol) {

		AccountCancellationResp.Builder builder = AccountCancellationResp.newBuilder();

		// CD 状态
		long lastReqTime = RedisProxy.getInstance().getAccountCancellationCheckTime(player.getId());
		if (lastReqTime + ConstProperty.getInstance().getAccountCancReqCd() > HawkTime.getMillisecond()) {
			builder.setSuccess(false);
			builder.setState(AccountCancellationState.REQ_CD_FAILED);
			builder.setApplicationTime(lastReqTime);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.ACCOUNT_CANCELLATION_PER_RESP, builder));
			return;
		}

		if (!onCheck(HP.code.ACCOUNT_CANCELLATION_PER_REQ_VALUE)) {
			RedisProxy.getInstance().updateAccountCancellationCheckTime(player.getId());
			return;
		}

		builder.setSuccess(true);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.ACCOUNT_CANCELLATION_PER_RESP, builder));
	}

	/**
	 * 注销
	 */
	@ProtocolHandler(code = HP.code.ACCOUNT_CANCELLATION_REQ_VALUE)
	private void doCancellation(HawkProtocol protocol) {

		AccountCancellationReq req = protocol.parseProtocol(AccountCancellationReq.getDefaultInstance());

		AccountCancellationResp.Builder builder = AccountCancellationResp.newBuilder();

		// CD 状态
		long lastReqTime = RedisProxy.getInstance().getAccountCancellationCheckTime(player.getId());
		if (lastReqTime + ConstProperty.getInstance().getAccountCancReqCd() > HawkTime.getMillisecond()) {
			builder.setSuccess(false);
			builder.setState(AccountCancellationState.REQ_CD_FAILED);
			builder.setApplicationTime(lastReqTime);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.ACCOUNT_CANCELLATION_PER_RESP, builder));
			return;
		}

		if (!onCheck(HP.code.ACCOUNT_CANCELLATION_REQ_VALUE)) {
			RedisProxy.getInstance().updateAccountCancellationCheckTime(player.getId());
			return;
		}
		
		// 进行实名验证
		int result = certification(req.getName(), req.getNum());

		if (result != 0) {
			builder.setSuccess(false);
			builder.setState(AccountCancellationState.CERTIFICATION_FAILED);
			logger.info("certification failed, result:{}, playerId:{}, name:{}, cardNum:{}", result, player.getId(), req.getName(), req.getNum());

		} else {
			builder.setSuccess(true);
			logger.info("certification success, playerId:{}, name:{}, cardNum:{}", player.getId(), req.getName(), req.getNum());

			RedisProxy.getInstance().updateAccountCancellationCheckTime(player.getId());
			GlobalData.getInstance().updateAccountCancellationInfo(player.getId());
		}

		builder.setApplicationTime(HawkTime.getMillisecond());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.ACCOUNT_CANCELLATION_RESP, builder));
	}

	/**
	 * 注销检测
	 */
	private boolean onCheck(int hp) {

		// 行军
		int marchCount = WorldMarchService.getInstance().getPlayerMarchCount(player.getId());
		if (marchCount > 0) {
			sendError(hp, AccountCancellationError.ACCOUNT_CANCE_MARCH_VALUE);
			return false;
		}
		
		// 有联盟不能注销
		String playerGuildId = GuildService.getInstance().getPlayerGuildId(player.getId());
		if (!HawkOSOperator.isEmptyString(playerGuildId)) {
			sendError(hp, AccountCancellationError.ACCOUNT_CANCE_HAS_GUILD_VALUE);
			return false;
		}

		// 跨服状态不能注销
		if (player.isCsPlayer()) {
			sendError(hp, AccountCancellationError.ACCOUNT_CANCE_CROSS_VALUE);
			return false;
		}

		// 国王不能注销
		if (PresidentFightService.getInstance().isPresidentPlayer(player.getId())) {
			sendError(hp, AccountCancellationError.ACCOUNT_CANCE_PRESIDENT_VALUE);
			return false;
		}

		// 有官职不能注销
		if (GameUtil.getOfficerId(player.getId()) != OfficerType.OFFICER_00_VALUE) {
			sendError(hp, AccountCancellationError.ACCOUNT_CANCE_OFFICER_VALUE);
			return false;
		}

		// 加入了军事学院不能注销
		if (player.hasCollege()) {
			sendError(hp, AccountCancellationError.ACCOUNT_CANCE_COLLEGE_VALUE);
			return false;
		}

		// 有守护关系不能注销
		if (RelationService.getInstance().hasGuarder(player.getId())) {
			sendError(hp, AccountCancellationError.ACCOUNT_CANCE_GUARDER_VALUE);
			return false;
		}
		
		return true;
	}

	/**
	 * 真正的注销
	 */
	@SuppressWarnings("deprecation")
	public void realCancellation() {
		
		// 日志
		logger.info("realCancellation, playerId:{}", player.getId());
		
		player.rpcCall(MsgId.ACCOUNT_CANCELLATION, RelationService.getInstance(), new AccountCancellationRpcInvoker(player));
		
		// 注销
		GameUtil.resetAccount(player);
		//添加凭证记录数据，便于后续查询
		RedisProxy.getInstance().getRedisSession().hSet("account_role_cancel:" + player.getOpenId(), player.getId(), player.getServerId());

		// 注销上报
		cancellationPush();
	}

	/**
	 * 认证
	 */
	public int certification(String name, String cardNumber) {

		if (AccountCancellationCfg.getInstance().isDebugMode()) {
			return 0;
		}

		// L5地址
		String[] certificationL5 = AccountCancellationCfg.getInstance().getCertificationL5();

		HawkTuple3<Integer, String, Object> retInfo = L5Helper.l5Task(Integer.parseInt(certificationL5[0]), Integer.parseInt(certificationL5[1]), 500, new L5Task() {

			@Override
			public HawkTuple2<Integer, Object> run(String host) {
				try {

					// 地址
					host = host.endsWith("/") ? host : (host + "/");
					// url
					String postUrl = String.format("http://%s%s", host, "dmfeature/4037/userCertIdCheck");
					// contentType
					String contentType = AccountCancellationCfg.getInstance().getCertificationContentType();
					// 服务名
					String destination = AccountCancellationCfg.getInstance().getCertificationDestination();
					// 名字
					String source = AccountCancellationCfg.getInstance().getCertificationName();
					// 时间戳
					String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
					// 随机数
					String nonce = HawkUUIDGenerator.genUUID().substring(0, 6).toLowerCase();
					// 秘钥
					String secret = AccountCancellationCfg.getInstance().getCertificationSecret();
					// 签名
					String signature = HawkMd5.makeMD5(destination + "," + source + "," + timestamp + "," + nonce + "," + secret).toLowerCase();

					// ----------------------- content ---------------------
					JSONObject content = new JSONObject();
					content.put("name", name);
					content.put("certId", cardNumber);
					content.put("context", "context");

					JSONObject authInfo = new JSONObject();

					// authUserType 微信1 手Q2
					int authUserType = 1;
					if (player.getChannel().toLowerCase().equals("qq")) {
						authUserType = 2;
					}
					authInfo.put("authUserType", authUserType);
					authInfo.put("authAppid", AccountCancellationCfg.getInstance().getAuthAppId(player.getChannel().toLowerCase()));
					authInfo.put("authUserId", player.getOpenId());
					
					String token = "";
					JSONArray array = (JSONArray)player.getPfTokenJson().get("token");
					for (int i = 0; i < array.size(); i++) {
						JSONObject json = JSONObject.parseObject(array.getString(i));
						if (json.getInteger("type") == 3 || json.getInteger("type") == 1) {
							token = json.getString("value");
						}
					}
					authInfo.put("authKey", token);
					content.put("authInfo", authInfo);

					JSONObject device = new JSONObject();
					device.put("outerIp", player.getClientIp());
					device.put("osSystem", player.getPlatform().toLowerCase());
					content.put("device", device);
					// ----------------------- content ---------------------

					// ----------------------- 日志 ---------------------

					logger.info("certification, url info, playerId:{}, url:{}", player.getId(), postUrl);

					logger.info("certification, content info, playerId:{}, content:{}", player.getId(), content.toJSONString());

					logger.info("certification, header info, playerId:{}, contentType:{}, destination:{}, source:{}, timestamp:{}, nonce:{}, signature:{}",
							player.getId(), contentType, destination, source, timestamp, nonce, signature);

					// ----------------------- 日志 ---------------------

					HttpClient client = HawkHttpUrlService.getInstance().getHttpClient();

					Request request = client.POST(postUrl).content(new StringContentProvider(content.toJSONString()));

					// header
					request.header("Connection", "close");
					request.header("Content-Type", contentType);
					request.header("X-Odp-Destination-Service", destination);
					request.header("X-Odp-Source-Service", source);
					request.header("X-Odp-Timestamp", timestamp);
					request.header("X-Odp-Nonce", nonce);
					request.header("X-Odp-Signature", signature);
					request.timeout(5000L, TimeUnit.MILLISECONDS);

					// 发请求
					ContentResponse response = request.send();

					return new HawkTuple2<Integer, Object>(0, response);

				} catch (TimeoutException e) {
					HawkLog.errPrintln("certification timeout, playerId:{}", player.getId());

				} catch (Exception e) {
					HawkException.catchException(e);
				}

				return new HawkTuple2<Integer, Object>(-1, null);
			}
		});

		String ret = "-1";
		try {
			String reqUrl = retInfo.second;
			ContentResponse httpResponse = (ContentResponse) retInfo.third;
			String content = httpResponse.getContentAsString();
			
			if (httpResponse == null || HawkOSOperator.isEmptyString(content)) {
				HawkLog.logPrintln("certification, req failed, playerId:{}, url:{}", player.getId(), reqUrl);
				return -1;
			}
			
			HawkLog.logPrintln("certification, status:{}", httpResponse.getStatus());
			HawkLog.logPrintln("certification, content:{}", content);
			
			ret = JSONObject.parseObject(content).getString("ret");
			if (HawkOSOperator.isEmptyString(ret)) {
				return -1;
			}
			
			int isCert = JSONObject.parseObject(content).getInteger("isCert");
			int isMatch = JSONObject.parseObject(content).getInteger("isMatch");
			if (isCert != 1 || isMatch != 1) {
				return -1;
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return Integer.valueOf(ret);
	}

	/**
	 * 注销上报
	 */
	public void cancellationPush() {
		if (AccountCancellationCfg.getInstance().isDebugMode()) {
			return;
		}

		// L5地址
		String[] certificationL5 = AccountCancellationCfg.getInstance().getCertificationL5();

		HawkTuple3<Integer, String, Object> retInfo = L5Helper.l5Task(Integer.parseInt(certificationL5[0]), Integer.parseInt(certificationL5[1]), 500, new L5Task() {

			@Override
			public HawkTuple2<Integer, Object> run(String host) {
				try {

					// 地址
					host = host.endsWith("/") ? host : (host + "/");
					// url
					String postUrl = String.format("http://%s%s", host, "dmfeature/4037/accountCloseNotify");
					// contentType
					String contentType = AccountCancellationCfg.getInstance().getClosePushContentType();
					// 服务名
					String destination = AccountCancellationCfg.getInstance().getClosePushDestination();
					// 名字
					String source = AccountCancellationCfg.getInstance().getCertificationName();
					// 时间戳
					String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
					// 随机数
					String nonce = HawkUUIDGenerator.genUUID().substring(0, 6).toLowerCase();
					// 秘钥
					String secret = AccountCancellationCfg.getInstance().getCertificationSecret();
					// 签名
					String signature = HawkMd5.makeMD5(destination + "," + source + "," + timestamp + "," + nonce + "," + secret).toLowerCase();

					// ----------------------- content ---------------------
					JSONObject content = new JSONObject();
					content.put("uin", player.getOpenId());
					content.put("uinType", "openId");
					content.put("gameId", GsConfig.getInstance().getGameId());
					content.put("zoneId", GsConfig.getInstance().getAreaId());
					content.put("closeTime", HawkTime.getMillisecond());
					content.put("appId", AccountCancellationCfg.getInstance().getAuthAppId(player.getChannel().toLowerCase()));
					// ----------------------- content ---------------------

					// ----------------------- 日志 ---------------------

					logger.info("certification close push, url info, playerId:{}, url:{}", player.getId(), postUrl);

					logger.info("certification close push, content info, playerId:{}, content:{}", player.getId(), content.toJSONString());

					logger.info("certification close push, header info, playerId:{}, contentType:{}, destination:{}, source:{}, timestamp:{}, nonce:{}, signature:{}",
							player.getId(), contentType, destination, source, timestamp, nonce, signature);

					// ----------------------- 日志 ---------------------

					HttpClient client = HawkHttpUrlService.getInstance().getHttpClient();

					Request request = client.POST(postUrl).content(new StringContentProvider(content.toJSONString()));

					// header
					request.header("Connection", "close");
					request.header("Content-Type", contentType);
					request.header("X-Odp-Destination-Service", destination);
					request.header("X-Odp-Source-Service", source);
					request.header("X-Odp-Timestamp", timestamp);
					request.header("X-Odp-Nonce", nonce);
					request.header("X-Odp-Signature", signature);
					request.timeout(5000L, TimeUnit.MILLISECONDS);

					// 发请求
					ContentResponse response = request.send();

					return new HawkTuple2<Integer, Object>(0, response);

				} catch (TimeoutException e) {
					HawkLog.errPrintln("certification timeout, playerId:{}", player.getId());

				} catch (Exception e) {
					HawkException.catchException(e);
				}

				return new HawkTuple2<Integer, Object>(-1, null);
			}
		});

		String reqUrl = retInfo.second;
		ContentResponse httpResponse = (ContentResponse) retInfo.third;
		String content = httpResponse != null ? httpResponse.getContentAsString() : "abc";
		HawkLog.logPrintln("certification close push, playerId:{}, openid: {}, req url:{}, response content: {}", player.getId(), player.getOpenId(), reqUrl, content);
	}
	
}
