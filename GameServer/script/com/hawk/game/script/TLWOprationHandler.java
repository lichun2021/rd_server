package com.hawk.game.script;

import java.util.Map;
import java.util.Set;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.xid.HawkXID;

import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.config.TechnologyCfg;
import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.guild.GuildCreateObj;
import com.hawk.game.invoker.GuildCreateRpcInvoker;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.msg.PlayerAssembleMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.protocol.TiberiumWar.TWPlayerManage;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.tiberium.TiberiumWarService;
import com.hawk.game.util.GsConst;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.LogConst.PowerChangeReason;

/**
 * 创建联盟 localhost:8080/script/tlwOp?opType=1-4
 * 
 * @author Jesse
 *
 */
public class TLWOprationHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			String opType = params.get("opType");
			if (opType.equals("1")) {
				registRobot();
			} else if (opType.equals("2")) {
				techUp();
			} else if (opType.equals("3")) {
				int count = Integer.parseInt(params.get("count"));
				createGuild(count);
			} else if (opType.equals("4")) {
				signUp();
			}
			
			return successResponse("");
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}

	public void registRobot() {
		int startId = 1;
		int count = 500;
		for (int i = startId; i < startId + count; i++) {
			try {
				String puid = "robot_puid_" + (i + 1);
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
				builder.setServerId(GsConfig.getInstance().getServerId());
				builder.setDeviceId(puid);
	
				HawkSession session = new HawkSession(null);
				session.setAppObject(new Player(null));
				if (GsApp.getInstance().doLoginProcess(session, HawkProtocol.valueOf(HP.code.LOGIN_C_VALUE, builder), HawkTime.getMillisecond())) {
					AccountInfo accountInfo = GlobalData.getInstance().getAccountInfo(puid, GsConfig.getInstance().getServerId());
					if (accountInfo != null) {
						// 加载数据
						accountInfo.setInBorn(false);
						HawkXID xid = HawkXID.valueOf(GsConst.ObjType.PLAYER, accountInfo.getPlayerId());
						Player player = (Player) GsApp.getInstance().queryObject(xid).getImpl();
						PlayerData playerData = GlobalData.getInstance().getPlayerData(accountInfo.getPlayerId(), true);
						player.updateData(playerData);
	
						// 投递消息
						HawkApp.getInstance().postMsg(player, PlayerAssembleMsg.valueOf(builder.build(), session));
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

	public void createGuild(int count) {
		int createCount = 0;
		Set<String> playerIds = GlobalData.getInstance().getAllPlayerIds();
		for (String playerId : playerIds) {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player == null) {
				HawkLog.logPrintln("TLWGuildCreatHandler error, playerId:{}", playerId);
				continue;
			}
			if (player.hasGuild()) {
				continue;
			}
			if(createCount > count){
				break;
			}
			String guildName = GlobalData.getInstance().randomPlayerName().replaceFirst("指挥官", "");
			String tag = guildName.substring(0, 3);

			GuildCreateObj obj = new GuildCreateObj(guildName, tag, 10000, ConsumeItems.valueOf());
			obj.randomTag();
			player.rpcCall(MsgId.GUILD_CREATE, GuildService.getInstance(), new GuildCreateRpcInvoker(player, obj));
			createCount ++;
		}
	}

	public void signUp() {
		Set<String> playerIds = GlobalData.getInstance().getAllPlayerIds();
		for (String playerId : playerIds) {
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (!player.hasGuild()) {
				continue;
			}
			TWPlayerManage.Builder req = TWPlayerManage.newBuilder();
			req.setPlayerId(playerId);
			req.setType(1);
			TiberiumWarService.getInstance().updateMemberList(player, req.build());
		}
	}

	public void techUp() {
		Set<String> playerIds = GlobalData.getInstance().getAllPlayerIds();
		for (String playerId : playerIds) {
			AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(playerId);
			if(accountInfo!=null){
				 accountInfo.setInBorn(false);
			}
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player == null) {
				continue;
			}
			ConfigIterator<TechnologyCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(TechnologyCfg.class);
			int lvlCnt = HawkRand.randInt(30);
			for (int i = 0; i <= lvlCnt; i++) {
				techLevelUp(player, cfgs.next());
			}
		}
	}

	/**
	 * 科技升级
	 * 
	 * @param techId
	 * @return
	 */
	private boolean techLevelUp(Player player, TechnologyCfg cfg) {
		int techId = cfg.getTechId();
		TechnologyEntity entity = player.getData().getTechEntityByTechId(techId);
		if (entity == null) {
			entity = player.getData().createTechnologyEntity(cfg);
		}

		player.getData().getPlayerEffect().addEffectTech(player, entity);
		entity.setLevel(cfg.getLevel());
		entity.setResearching(false);
		player.getPush().syncTechnologyLevelUpFinish(entity.getCfgId());
		player.refreshPowerElectric(PowerChangeReason.TECH_LVUP);

		// 如果科技解锁技能,则推送科技技能信息
		if (cfg.getTechSkill() > 0) {
			player.getPush().syncTechSkillInfo();
		}

		return true;
	}
}
