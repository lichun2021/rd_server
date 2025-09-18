package com.hawk.game.script;

import java.util.Map;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.xid.HawkXID;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.util.GsConst;

/**
 * 生成机器人的脚本
 *
 * localhost:8080/script/robotgen?count=?&startId=?
 *
 * startId: 起始始Id(可选)
 * count: 生成数量
 * openIdPrefix: openid前缀
 *
 * @author hawk
 *
 */
public class RobotGenerateHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		if (!GsConfig.getInstance().isDebug()) {
			return HawkScript.failedResponse(SCRIPT_ERROR, "");
		}
		
		int startId = 1;
		if (params.containsKey("startId")) {
			startId = Integer.valueOf(params.get("startId"));
		}

		int count = 0;
		if (params.containsKey("count")) {
			count = Integer.valueOf(params.get("count"));
		}

		if (count <= 0 || startId <= 0) {
			return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "robot failed count: " + count + " startId: " + startId);
		}
		
		String openIdPrefix = params.getOrDefault("openIdPrefix", "robot_puid_");

		int successCnt = 0;
		for (int i = startId; i < startId + count; i++) {
			try {
				String puid = openIdPrefix + (i + 1);
				// 构造登录协议对象
				HPLogin.Builder builder = HPLogin.newBuilder();
				builder.setCountry("cn");
				builder.setChannel("guest");
				builder.setLang("zh-CN");
				builder.setPlatform("android");
				builder.setVersion("1.0.0.0");
				builder.setPfToken("da870ef7cf996eb6");
				builder.setPhoneInfo("{\"deviceMode\":\"win32\",\"mobileNetISP\":\"0\",\"mobileNetType\":\"0\"}\n");
				builder.setPuid(puid);
				builder.setRobot(1);
				builder.setServerId(GsConfig.getInstance().getServerId());
				builder.setDeviceId(puid);

				HawkSession session = new HawkSession(null);
				session.setAppObject(new Player(null));
				if (GsApp.getInstance().doLoginProcess(session, HawkProtocol.valueOf(HP.code.LOGIN_C_VALUE, builder), HawkTime.getMillisecond())) {
					successCnt++;
					AccountInfo accountInfo = GlobalData.getInstance().getAccountInfo(puid + "#android", GsConfig.getInstance().getServerId());
					if (accountInfo != null) {
						// 加载数据
						accountInfo.setInBorn(false);
						HawkXID xid = HawkXID.valueOf(GsConst.ObjType.PLAYER, accountInfo.getPlayerId());
						Player player = (Player) GsApp.getInstance().queryObject(xid).getImpl();
						PlayerData playerData = GlobalData.getInstance().getPlayerData(accountInfo.getPlayerId(), true);
						player.updateData(playerData);
						
						// 投递消息
						//HawkApp.getInstance().postMsg(player, PlayerAssembleMsg.valueOf(builder.build(), session));
						int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
						HawkTaskManager.getInstance().postTask(new HawkTask() {
							@Override
							public Object run() {
								player.onRobotAssembleMsg();
								player.onRobotLoginMsg();
								return null;
							}
						}, threadIdx);
						
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		return HawkScript.successResponse("robot success count: " + successCnt);
	}

}
