package com.hawk.game.gmscript;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.game.GsConfig;
import com.hawk.game.crossactivity.season.CrossActivitySeasonService;
import com.hawk.game.crossactivity.season.CrossSeaonStateData;
import com.hawk.game.crossactivity.season.CrossSeasonServerData;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.HeroArchivesEntity;
import com.hawk.game.entity.LifetimeCardEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.module.mechacore.entity.MechaCoreEntity;
import com.hawk.game.module.toucai.entity.MedalEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.cache.PlayerDataKey;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.SysOpService;
import com.hawk.game.service.cyborgWar.CWTeamData;
import com.hawk.game.service.cyborgWar.CyborgWarRedis;
import com.hawk.game.service.cyborgWar.CyborgWarService;
import com.hawk.game.service.starwars.StarWarsOfficerService;
import com.hawk.game.service.xqhxWar.XQHXWarService;

/**
 * 用来动态调整
 * 
 * localhost:8080/script/sysop?op=09071500&playerId=
 *
 * @author hawk
 */
public class SysOperationHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		String opType = params.get("op");
		if (HawkOSOperator.isEmptyString(opType)) {
			return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "op type null");
		}

		/*
		 * 常驻脚本执行
		 * 常驻脚本opType用数字,和线上脚本区分开
		 */
		if (SysOpService.getInstance().sysop(params, opType)) {
			return HawkScript.successResponse("op service success");
		}

		if (opType.equals("202508111420")) {
			fixData();
			return HawkScript.successResponse("op success, opType:" + opType);
		}
		
		if (opType.equals("202508221010")) {
			xqhxWarRankUpdate();
			return HawkScript.successResponse("op success, opType:" + opType);
		}
		
		if (opType.equals("202508221212")) {
			cbwarGuildTeamCheck(params);
			return HawkScript.successResponse("op success, opType:" + opType);
		}
		
		
		if (opType.equals("202508221831")) {
			playerActivityDupAchieveDataFix(params);
			return HawkScript.successResponse("op success, opType:" + opType);
		}
		
		if (opType.equals("202508251010")) {
			starWarOfficeDataDel();
			return HawkScript.successResponse("op success, opType:" + opType);
		}
		
		if (opType.equals("202508252020")) {
			StarWarsOfficerService.getInstance().loadOrReloadOfficer();
			return HawkScript.successResponse("op success, opType:" + opType);
		}
		
		if (opType.equals("202509021510")) {
			fixCrossSeasonScore();
			return HawkScript.successResponse("op success, opType:" + opType);
		}
		
		
		
		return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "opType not found");
	}
	
	
	/**
	 * 设置航海赛季分数
	 * @param params
	 */
	protected void fixCrossSeasonScore(){
		int score = 1100;
		String serverId = GsConfig.getInstance().getServerId();
		CrossSeaonStateData state = CrossActivitySeasonService.getInstance().getCrossSeaonStateData();
		int season = state.getSeason();
		CrossSeasonServerData data = CrossSeasonServerData.loadData(season, serverId);
		if(Objects.isNull(data)){
			HawkLog.logPrintln("fixCrossSeasonScore-CrossSeasonServerData-null-{},serverId");
			return;
		}
		int bef = data.getScore();
		data.setScore(score);
		data.saveData();
		HawkLog.logPrintln("fixCrossSeasonScore-CrossSeasonServerData-over-{}-{}-{}",serverId,bef,data.getScore());
	}
	
	
	/**
	 * 清除一下大帝战数据
	 */
	protected void starWarOfficeDataDel() {
		try {
			//清除官职
			Method methodClearOfficer = HawkOSOperator.getClassMethod(StarWarsOfficerService.getInstance(), "clearOfficerInfo");
			methodClearOfficer.invoke(StarWarsOfficerService.getInstance());
			HawkLog.logPrintln("starWarOfficeDataDel-clearOfficerInfo suc");
		} catch (Exception e) {
			HawkLog.logPrintln("starWarOfficeDataDel-clearOfficerInfo err");
		}
		
		try {
			//清除礼包
			Method methodClearGift = HawkOSOperator.getClassMethod(StarWarsOfficerService.getInstance(), "clearGiftInfo");
			methodClearGift.invoke(StarWarsOfficerService.getInstance());
			HawkLog.logPrintln("starWarOfficeDataDel-methodClearGift suc");
		} catch (Exception e) {
			HawkLog.logPrintln("starWarOfficeDataDel-methodClearGift err");
		}
		HawkLog.logPrintln("starWarOfficeDataDel-over");
		
	}
	
	/**
	 * 修复活动中的重复成就
	 * @param params
	 */
	protected void playerActivityDupAchieveDataFix(Map<String, String> params) {
        String playerId = params.get("playerId");
        int activityId = Integer.parseInt(params.get("activityId"));
        String listName = params.get("listName");
        if (HawkOSOperator.isEmptyString(playerId)) {
            return;
        }
        Player player = GlobalData.getInstance().makesurePlayer(playerId);
        if (player == null) {
                return;
        }

        HawkThreadPool threadPool = HawkTaskManager.getInstance().getTaskExecutor();
        int threadIdx = Math.abs(player.getXid().hashCode() % threadPool.getThreadNum());
        HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
                @SuppressWarnings("unchecked")
				@Override
                public Object run() {
                	Optional<ActivityBase> opActivity = ActivityManager.getInstance().getGameActivityByType(activityId);
                	if(!opActivity.isPresent()){
                		return null;
                	}
                	 
                	ActivityBase activity = opActivity.get();
 	                Optional<HawkDBEntity> entityOp = activity.getPlayerDataEntity(playerId);
 	                if(!entityOp.isPresent()){
 	                	 return null;
 	                }
 	                
 	                HawkDBEntity entity = entityOp.get();
 	                ActivityType atype = ActivityType.getType(activityId);
 	                if(Objects.isNull(atype)){
	                	 return null;
	                }
 	                Field[] fs = entity.getClass().getDeclaredFields();
 	                List<AchieveItem> alist = null;
 	                for(Field field : fs){
 	                	if(!field.getName().equals(listName)){
 	                		continue;
 	                	}
 	                	
 	                	field.setAccessible(true);
 	                	try {
							alist= (List<AchieveItem>) field.get(entity);
						} catch (Exception e) {
							e.printStackTrace();
						}
 	                	break;
 	                }
 	                if(Objects.isNull(alist)){
 	                	return null;
 	                }
 	                Map<Integer, AchieveItem> itemMap = new HashMap<>();
                    for (AchieveItem item : alist) {
                       AchieveItem oldItem = itemMap.get(item.getAchieveId());
                       if (oldItem == null || item.getState() > oldItem.getState()) {
                    	   itemMap.put(item.getAchieveId(), item);
                       }
                    }
                    alist.clear();
                    alist.addAll(itemMap.values());
                    entity.notifyUpdate();
                    return null;
                }
         }, threadIdx);
	}


	
	/**
	 * 刷新一下先驱的排行榜
	 */
	public void xqhxWarRankUpdate(){
		XQHXWarService.getInstance().doTeamRank(true);
	}
	
	/**
	 * 检查一下赛博小队问题
	 */
	public void cbwarGuildTeamCheck(Map<String, String> params){
		String guildId = params.get("guildId");
		if(HawkOSOperator.isEmptyString(guildId)){
			HawkLog.logPrintln("cbwarGuildTeamCheck guildId null");
			return;
		}
		GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(guildId);
		HawkLog.logPrintln("cbwarGuildTeamCheck guildObj null:{}",guildId);
		if(Objects.isNull(guildObj)){
			return;
		}
		List<String> teams = CyborgWarRedis.getInstance().getCWGuildTeams(guildId);
		
		List<String> tlist = new ArrayList<>();
		for(String tid : teams){
			//小队信息不存在了，直接删除
			CWTeamData tdata = CyborgWarRedis.getInstance().getCWTeamData(tid);
			if(Objects.isNull(tdata)){
				CyborgWarRedis.getInstance().removeCWGuildTeam(guildId, tid);
				continue;
			}
			tlist.add(tid);
		}
		CyborgWarService.guildTeams.put(guildId, tlist);
	}
	
	/**
	 * 工单号】：25080110363484188301
	【游戏名称】：红警OL
	【游戏账号】：478146112
	【OPENID/GOP】：FEF80FC9804EE3642D7A316393BA41AB
	【游戏大区】：QQ安卓-115区
	【问题描述】：玩家反馈由于迁服导致服务器崩溃而统一回档，回档后造成账号数据错误，机甲核心和星耀能量站等级被清零，还请@海燕 老师看一下
	 */
	private void fixData() {
		String playerId = "flc-12fv4s-4";
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			HawkLog.logPrintln("fixData fail playerId : {} not found!!!!", playerId);
			return;
		}
		if (GlobalData.getInstance().isOnline(playerId)) {
			player.kickout(Status.Error.MIGRATE_FINISH_VALUE, true, "");
		}
		
		List<MechaCoreEntity> entities = HawkDBManager.getInstance().query("from MechaCoreEntity where playerId = ? and invalid = 0", playerId);
		if (entities.size() == 2) {
			for (MechaCoreEntity objData : entities) {
				if (objData.getId().equals("fij-3w6kd1-1")) {
					player.getData().getDataCache().update(PlayerDataKey.MechaCoreEntity, objData);
					HawkLog.logPrintln("fixData MechaCoreEntity playerId : {} OK!!!!", playerId);
				}
				if (objData.getId().equals("fij-418wdx-1")) {
					objData.delete();
				}
			}
		}

//		String[] keyArr = new String[] { "HeroArchivesEntity", "LifetimeCardEntity", "MedalEntity" };
//		immgration(playerId, keyArr);
		
		HeroArchivesEntity archivesEntity = player.getData().getHeroArchivesEntity();
		archivesEntity.setArchives("1058:4|1059:5|1065:4|1003:5|1004:2|1036:4|1037:5|1069:4|1042:5|1075:3|1077:3|1046:5|1018:5|1051:4|1021:5|1054:4");
		archivesEntity.afterRead();
		
		LifetimeCardEntity card = player.getData().getLifetimeCardEntity();
		card.setAdvancedEndTime(0);
		card.setWeekAwardTime(88);
		card.setCommonUnlockTime(1700756982939L);
		card.setFreeEndTime(1700874069753L);
		card.setMonthAwardTime(21);
		card.afterRead();
		
		MedalEntity medal = player.getData().getMedalEntity();
		medal.setDailyRefresh(0);
		medal.setRefreshCool(1753838504523L);
		medal.setRefreshStr("{\"rand\":[\"flg-132pw2-5\",\"flg-138a64-o\",\"flo-14nkt8-b\"],\"friend\":[\"flc-12fvlv-q\",\"flb-128ze4-i\",\"flo-14luvc-2\",\"fln-14jztu-q\",\"fli-13h8pu-5\",\"flc-12c5lz-w\",\"flo-14qur1-7\",\"flc-12foyf-6\",\"flc-12ep04-m\",\"flh-13az24-7\",\"flh-13f2k1-1a\",\"flo-14ra7v-c\",\"flo-14o4t3-5\",\"fli-13hxyy-k\"],\"enemy\":[\"flo-14r90o-e\",\"fli-13frf6-c\",\"fln-14iwn1-2\",\"flb-125s4d-i\",\"flh-13fb21-m\"]}");
		medal.setDailyReward(21);
		medal.setLastRefreshDay(212);
		medal.setCollectStr("[\"{\\\"rf\\\":10201,\\\"pf\\\":15900001,\\\"start\\\":1753884734172,\\\"index\\\":0,\\\"end\\\":1753899134172}\",\"{\\\"rf\\\":10201,\\\"pf\\\":15900001,\\\"start\\\":1753884734910,\\\"index\\\":1,\\\"end\\\":1753899134910}\",\"{\\\"rf\\\":10203,\\\"pf\\\":15900001,\\\"start\\\":1753884735821,\\\"index\\\":2,\\\"end\\\":1753899135821}\",\"{\\\"rf\\\":0,\\\"pf\\\":0,\\\"start\\\":0,\\\"index\\\":3,\\\"end\\\":0}\"]");
		medal.setExp(7200);
		medal.afterRead();
		HawkLog.logPrintln("fixData MechaCoreEntity playerId : {} OK!!!!", playerId);
	}
	
}