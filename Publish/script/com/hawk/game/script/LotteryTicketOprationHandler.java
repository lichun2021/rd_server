package com.hawk.game.script;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.xid.HawkXID;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.lotteryTicket.LotteryRecourse;
import com.hawk.activity.type.impl.lotteryTicket.LotteryRlt;
import com.hawk.activity.type.impl.lotteryTicket.LotteryTicketActivity;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.TechnologyCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.item.AwardItems;
import com.hawk.game.msg.PlayerAssembleMsg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Building.BuildingUpdateOperation;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Login.HPLogin;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.service.BuildingService;
import com.hawk.game.util.GsConst;
import com.hawk.log.Action;
import com.hawk.log.LogConst.PowerChangeReason;

/**
 * 创建联盟 localhost:8080/script/lotteryOp?opType=1-4
 * 
 * @author Jesse
 *
 */
public class LotteryTicketOprationHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			String opType = params.get("opType");
			String idper = "robot_puid_sucess";
			if (opType.equals("1")) {
				registRobot(idper);
			} else if (opType.equals("2")) {
				techUp(idper);
			} else if (opType.equals("3")) {
				doLottery(idper);
			} else if(opType.equals("4")){
				cal(idper);
			}
			
			return successResponse("");
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}

	public void registRobot(String playerIdPer) {
		int startId = 160;
		int count = 100;
		
		
		for (int i = startId; i < startId + count; i++) {
			try {
				String puid = playerIdPer + (i + 1);
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



	public void techUp(String playerIdPer) {
		Set<String> playerIds = GlobalData.getInstance().getAllPlayerIds();
		for (String playerId : playerIds) {
			AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(playerId);
			if(accountInfo==null){
				continue;
			}
			if(!accountInfo.getPuid().startsWith(playerIdPer)){
				continue;
			}
			if(accountInfo!=null){
				 accountInfo.setInBorn(false);
			}
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player == null) {
				continue;
			}
			Optional<ActivityBase> optional = ActivityManager.getInstance().
					getActivity(Activity.ActivityType.LOTTERY_TICKET_VALUE);
			
			LotteryTicketActivity activity = (LotteryTicketActivity)optional.get();
			
			int tnum = HawkTaskManager.getInstance().getThreadNum();
			int tid = player.getXid().getHashThread(tnum);
			HawkTaskManager.getInstance().postTask(new HawkTask() {

				@Override
				public Object run() {
					BuildingBaseEntity buildingEntity = player.getData().getBuildingEntityByType(BuildingType.CONSTRUCTION_FACTORY);
					if (buildingEntity == null) {
						BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, (BuildingType.CONSTRUCTION_FACTORY_VALUE * 100) + 1);
						buildingEntity = player.getData().createBuildingEntity(buildingCfg, "1", false);
						BuildingService.getInstance().createBuildingFinish(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY, HP.code.BUILDING_CREATE_PUSH_VALUE);
					}
					
					BuildingCfg oldBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
					BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, oldBuildCfg.getPostStage());
					int flag = 0;
					while (buildingCfg != null && buildingCfg.getLevel() <= 6 && flag < 6 && buildingCfg.getBuildType() == buildingEntity.getType()) {
						flag = buildingCfg.getLevel();
						BuildingService.getInstance().buildingUpgrade(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY);
						oldBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, buildingEntity.getBuildingCfgId());
						buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, oldBuildCfg.getPostStage());
					}
					
					ConfigIterator<TechnologyCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(TechnologyCfg.class);
					int lvlCnt = HawkRand.randInt(30);
					for (int i = 0; i <= lvlCnt; i++) {
						techLevelUp(player, cfgs.next());
					}
					activity.onPlayerLogin(playerId);
					AwardItems awardItems = AwardItems.valueOf("30000_21070026_200");
					awardItems.rewardTakeAffectAndPush(player, Action.GM_AWARD);
					int count =  player.getData().getItemNumByItemId(21070026);
					logger.info("LotteryTicketActivitysend, openId:{},playerId: {}, initCount: {}, ", accountInfo.getPuid(),player.getId(), count);
					return null;
				}},tid );
			
		}
			
			
		
		
	}
	
	
	public void doLottery(String playerIdPer) {
		Set<String> playerIds = GlobalData.getInstance().getAllPlayerIds();
		List<String> all = new ArrayList<>();
		for (String playerId : playerIds) {
			AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(playerId);
			if(accountInfo==null){
				continue;
			}
			if(!accountInfo.getPuid().startsWith(playerIdPer)){
				continue;
			}
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player == null) {
				continue;
			}
			all.add(playerId);
		}
		
		for (String playerId : playerIds) {
			AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(playerId);
			if(accountInfo==null){
				continue;
			}
			if(!accountInfo.getPuid().startsWith(playerIdPer)){
				continue;
			}
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player == null) {
				continue;
			}
			Optional<ActivityBase> optional = ActivityManager.getInstance().
					getActivity(Activity.ActivityType.LOTTERY_TICKET_VALUE);
			
			LotteryTicketActivity activity = (LotteryTicketActivity)optional.get();
			int tnum = HawkTaskManager.getInstance().getThreadNum();
			int aid = player.getXid().getHashThread(tnum);
			int count =  player.getData().getItemNumByItemId(21070026);
			HawkLog.logPrintln("LotteryTicketActivity initcount, openId:{},playerId: {}, initCount: {}, ", accountInfo.getPuid(),player.getId(), count);
			for(int r=0;r<300;r++){
			HawkTaskManager.getInstance().postTask(new HawkTask() {
				@Override
				public Object run() {
					
						int action = HawkRand.randInt(1,3);
						if(action == 1){
							activity.onPlayerLottery(playerId, 1);
						}else if(action == 2){
							int ran = HawkRand.randInt(0, all.size()-1);
							String tar = all.get(ran);
							if(tar.equals(playerId)){
								return null;
							}
							int ranCount = HawkRand.randInt(1, 10);
							activity.onPlayerAssistApply(playerId, tar, ranCount);
						}else if(action == 3){
							for(LotteryRecourse re : activity.lotteryRecourseMap.values()){
					    		if(re.getAssistId().equals(playerId)){
					    			activity.onPlayerAssistLottery(playerId, re.getId(), 1);
					    		}
					    	}
						}
					
					return null;
				}}, aid);
		}
			
		}
		
	}
	
	
	
	public void cal(String playerIdPer) {
		Set<String> playerIds = GlobalData.getInstance().getAllPlayerIds();
		Optional<ActivityBase> optional = ActivityManager.getInstance().
				getActivity(Activity.ActivityType.LOTTERY_TICKET_VALUE);
		
		LotteryTicketActivity activity = (LotteryTicketActivity)optional.get();
		Map<String, LotteryRecourse>  rmap = activity.lotteryRecourseMap;
		for (String playerId : playerIds) {
			AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(playerId);
			if(accountInfo==null){
				continue;
			}
			if(!accountInfo.getPuid().startsWith(playerIdPer)){
				continue;
			}
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player == null) {
				continue;
			}
			
			PlayerData playerData =player.getData();
			int count =  playerData.getItemNumByItemId(21070026);
			int assistCount = 0;
			for(LotteryRecourse recourse : rmap.values()){
				if(recourse.getSourceId().equals(playerId)){
					assistCount += recourse.getTicketCount();
				}
			}
			Map<String,LotteryRlt> dataMap = activity.getPlayerLotteryRecordData(playerId);
			int all = count + assistCount + dataMap.size();
			HawkLog.logPrintln("LotteryTicketActivity cal, openId:{},playerId: {}, count: {},assistCount:{},lotteryCount:{} ,all:{}",accountInfo.getPuid(), player.getId(), 
					count,assistCount,dataMap.size(),all);
			logger.info("LotteryTicketActivity cal, openId:{},playerId: {}, count: {},assistCount:{},lotteryCount:{} ,all:{}",accountInfo.getPuid(), player.getId(), 
					count,assistCount,dataMap.size(),all);
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
