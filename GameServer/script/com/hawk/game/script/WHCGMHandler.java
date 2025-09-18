package com.hawk.game.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.hawk.activity.type.impl.guildBack.GuildBackActivity;
import com.hawk.activity.type.impl.newStart.NewStartActivity;
import com.hawk.activity.type.impl.pddActivity.PDDActivity;
import com.hawk.activity.type.impl.supplyCrate.SupplyCrateActivity;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.service.commonMatch.CMWService;
import com.hawk.game.service.tblyTeam.TBLYSeasonService;
import com.hawk.game.service.tblyTeam.TBLYWarService;
import com.hawk.game.service.xhjzWar.XHJZWarService;
import com.hawk.game.service.xqhxWar.XQHXWarService;
import com.hawk.game.world.service.WorldPointService;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.googlecode.protobuf.format.JsonFormat;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.WHCGMEvent;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.changeServer.ChangeServerActivity;
import com.hawk.activity.type.impl.rank.ActivityRankContext;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.activity.type.impl.seasonActivity.rank.GuildSeasonKingGradeRank;
import com.hawk.activity.type.impl.seasonActivity.rank.GuildSeasonKingGradeRankProvider;
import com.hawk.game.GsConfig;
import com.hawk.game.cfgElement.ArmourEffObject;
import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.config.ArmourChargeLabCfg;
import com.hawk.game.config.ArmourConstCfg;
import com.hawk.game.config.ArmourStarConsumeCfg;
import com.hawk.game.config.SuperSoldierEnergyCfg;
import com.hawk.game.crossactivity.season.CrossActivitySeasonService;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.player.supersoldier.energy.ISuperSoldierEnergy;
import com.hawk.game.president.PresidentCity;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.president.model.President;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Script;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.RandomUtil;
import com.hawk.log.LogConst;

public class WHCGMHandler  extends HawkScript {
    @Override
    public String action(Map<String, String> map, HawkScriptHttpInfo hawkScriptHttpInfo) {
        if (!GsConfig.getInstance().isDebug()) {
            return "不是测试环境";
        }
        String opt = map.getOrDefault("opt", "");
        switch (opt) {
            case "setProprietary": {
                ActivityGlobalRedis.getInstance().hset("PROPRIETARY_SERVER", map.get("serverId"),map.get("isProprietary"));
                return "setProprietary success";
            }
            case "getProprietaryList": {
                Map<String, String> serverMap = ActivityGlobalRedis.getInstance().hgetAll("PROPRIETARY_SERVER");
                String rlt = "";
                for(String serverId : serverMap.keySet()){
                    String value = serverMap.get(serverId);
                    if("1".equals(value)){
                        value = "专服";
                    }else {
                        value = "普通服";
                    }
                    String tmp = serverId + "\t"+value+"<br />";
                    rlt+=tmp;
                }
                return "getProprietaryList success<br />"+rlt;
            }
            case "setTBLYRank": {
                RedisProxy.getInstance().getRedisSession().hSet("tlw_guild_total_rank:"+map.get("season"),map.get("guildId"),map.get("rank"));
                return "setTBLYRank success";
            }
            case "setSeasonRank": {
                //获得排行榜管理器
                GuildSeasonKingGradeRankProvider provider = getRankProvider();
                //构建排行数据
                GuildSeasonKingGradeRank rank1 = new GuildSeasonKingGradeRank();
                //设置联盟id
                rank1.setId(map.get("guild1"));
                //设置积分
                rank1.setScore(Long.parseLong(map.get("score")));
                //进入排行
                provider.insertIntoRank(rank1);
                //构建排行数据
                GuildSeasonKingGradeRank rank2 = new GuildSeasonKingGradeRank();
                //设置联盟id
                rank2.setId(map.get("guild2"));
                //设置积分
                rank2.setScore(Long.parseLong(map.get("score")));
                //进入排行
                provider.insertIntoRank(rank2);
                return "setSeasonRank success";
            }
            case "setPresident":{
                Player player = GlobalData.getInstance().makesurePlayer(map.get("playerId"));
                if(HawkOSOperator.isEmptyString(player.getGuildId())){
                    return "need join guild";
                }
                //判断当前有没有国王，没有国王就设置国王
                if(HawkOSOperator.isEmptyString(PresidentFightService.getInstance().getPresidentPlayerId())){
                    //设置当前号为国王
                    PresidentCity city = PresidentFightService.getInstance().getPresidentCity();
                    if(city.getPresident() == null){
                        city.setPresident(new President());
                    }
                    city.chanagePresident(player);
                    return "setPresident success";
                }else {
                    return "haa king";
                }
            }
            case "clearPresident":{
                //清除国王
                PresidentFightService.getInstance().getPresidentCity().clearPresident();
                return "setPresident success";
            }
            case "addRewardFlag":{
                GuildService.getInstance().addRewardFlag(map.get("guildId"), Integer.parseInt(map.get("flagId")));
                return "addRewardFlag success";
            }
            case "armourQuantumMax":{
                Player player = GlobalData.getInstance().scriptMakesurePlayer(map);
                if (player == null) {
                    return HawkScript.failedResponse(Script.ScriptError.ACCOUNT_NOT_EXIST_VALUE, map.toString());
                }
                List<ArmourEntity> armourEntityList = player.getData().getArmourEntityList();
                for(ArmourEntity armour : armourEntityList){
                    int quality = armour.getQuality();
                    // 此品质不能升星
                    if (!ArmourConstCfg.getInstance().canQualityQuantum(quality)) {
                        continue;
                    }
                    armour.setLevel(45);
                    // 此等级不能升级量子槽位
                    int level = armour.getLevel();
                    if (!ArmourConstCfg.getInstance().canLevelQuantum(level)) {
                        continue;
                    }
                    while (armour.getQuantum() < ArmourConstCfg.getInstance().getQuantumRedLevel()){
                        armour.addQuantum();
                    }
                    armour.notifyUpdate();
                    // 推单条装备信息
                    player.getPush().syncArmourInfo(armour);
                }
                // 刷新作用号
                player.getEffect().resetEffectArmour(player);
                // 刷新战力
                player.refreshPowerElectric(LogConst.PowerChangeReason.ARMOUR_CHANGE);
                return "armourQuantumMax success";
            }
            case "armourStarAttrMax":{
                Player player = GlobalData.getInstance().scriptMakesurePlayer(map);
                if (player == null) {
                    return HawkScript.failedResponse(Script.ScriptError.ACCOUNT_NOT_EXIST_VALUE, map.toString());
                }
                List<ArmourEntity> armourEntityList = player.getData().getArmourEntityList();
                for(ArmourEntity armour : armourEntityList){
                    // 此品质不能升星
                    int quality = armour.getQuality();
                    if (!ArmourConstCfg.getInstance().canQualityStar(quality)) {
                        continue;
                    }
                    // 此等级不能升星
                    int level = armour.getLevel();
                    if (!ArmourConstCfg.getInstance().canLevelStar(level)) {
                        continue;
                    }
                    while (armour.getStar() < 40){
                        armour.addStar();
                    }
                    // 装备星级
                    int star = armour.getStar();
                    ArmourStarConsumeCfg starCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourStarConsumeCfg.class, star);
                    if (starCfg == null) {
                        continue;
                    }
                    // 超出最大解锁条目数量
                    int beforeChargeCount = armour.getStarEff().size();
                    while (beforeChargeCount < starCfg.getUnlockCharging()) {
                        // 已经有了的充能条目
                        Set<Integer> alreadyChargeType = new HashSet<>();
                        for (ArmourEffObject starEff : armour.getStarEff()) {
                            ArmourChargeLabCfg chargeLabCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourChargeLabCfg.class, starEff.getAttrId());
                            if (chargeLabCfg != null) {
                                alreadyChargeType.add(chargeLabCfg.getChargingLabel());
                            }
                            // 替换属性也不能重复
                            if (starEff.getReplaceAttrId() != 0) {
                                chargeLabCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourChargeLabCfg.class, starEff.getReplaceAttrId());
                                if (chargeLabCfg != null) {
                                    alreadyChargeType.add(chargeLabCfg.getChargingLabel());
                                }
                            }
                        }

                        // 取出所有可以参与随机的条目
                        List<ArmourChargeLabCfg> randCfgs = new ArrayList<>();
                        ConfigIterator<ArmourChargeLabCfg> chargeLabCfgIter = HawkConfigManager.getInstance().getConfigIterator(ArmourChargeLabCfg.class);
                        while (chargeLabCfgIter.hasNext()) {
                            ArmourChargeLabCfg chargeLabCfg = chargeLabCfgIter.next();
                            if (alreadyChargeType.contains(chargeLabCfg.getChargingLabel())) {
                                continue;
                            }
                            randCfgs.add(chargeLabCfg);
                        }

                        // 随机属性
                        ArmourChargeLabCfg randCfg = RandomUtil.random(randCfgs);
                        EffectObject attributeValue = randCfg.getAttributeValue();
                        ArmourEffObject armourEff = new ArmourEffObject(randCfg.getId(), attributeValue.getEffectType(), attributeValue.getEffectValue());
                        armourEff.setRate(randCfg.getDefaultProgress());
                        armour.addStarEff(armourEff);
                        beforeChargeCount = armour.getStarEff().size();
                    }
                    for (ArmourEffObject starEff : armour.getStarEff()) {
                        starEff.setBreakthrough(2);
                        // 设置充能进度
                        starEff.setRate(ArmourConstCfg.getInstance().getChargeRedLimit());
                    }
                    armour.notifyUpdate();
                    // 推单条装备信息
                    player.getPush().syncArmourInfo(armour);
                }
                // 刷新作用号
                player.getEffect().resetEffectArmour(player);
                // 刷新战力
                player.refreshPowerElectric(LogConst.PowerChangeReason.ARMOUR_CHANGE);
                // 更新泰能装备外显
                WorldPointService.getInstance().updateEquipStarShow(player);
                return "armourStarAttrMax success";
            }
            case "changeServerNext":{
                Optional<ChangeServerActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.CHANGE_SVR_ACTIVITY.intValue());
                if (opActivity.isPresent()) {
                    opActivity.get().next();
                }
                return "changeServerNext success";
            }
            case "luckGetGoldTen":{
                int count = Integer.parseInt(map.get("count"));
                Set<String> playerIds = GlobalData.getInstance().getAllPlayerIds();
                for(int i = 0; i < count; i++){
                    for (String playerId : playerIds) {
                        Player player = GlobalData.getInstance().makesurePlayer(playerId);
                        if (player == null) {
                            HawkLog.logPrintln("WHCGMHandler luckGetGoldTen error, playerId:{}", playerId);
                            continue;
                        }
                        ActivityManager.getInstance().postEvent(new WHCGMEvent(playerId, WHCGMEvent.LUCK_GET_GOLD_DRAW_TEN));
                    }
                }
                return "luckGetGoldTen success";
            }
            case "luckGetGoldGetServerRecord":{
                String termId = map.get("termId");
                int count = Integer.parseInt(map.get("count"));
                String key = "LUCK_GET_GOLD:" + termId  + ":SERVER_RECORD";
                StringBuilder sbuilder = new StringBuilder();
                List<byte[]> records = ActivityGlobalRedis.getInstance().getRedisSession().lRange(key.getBytes(), 0, count, 0);
                for (byte[] bytes : records) {
                    try {
                        Activity.LuckGetGoldDrawRecord.Builder record = Activity.LuckGetGoldDrawRecord.newBuilder();
                        record.mergeFrom(bytes);
                        sbuilder.append(JsonFormat.printToString(record.build())).append("\r\n</br>");;
                    }catch (Exception e){
                        HawkException.catchException(e);
                    }
                }
                return sbuilder + "luckGetGoldGetServerRecord success";
            }
            case "superSoldierEnergy": {
            	String playerId = map.get("playerId");
            	int soldier = Integer.parseInt(map.get("soldier"));
            	int energyId = Integer.parseInt(map.get("energyId"));
            	int cfgId = Integer.parseInt(map.get("cfgId"));
            	
            	// 玩家
            	Player player = GlobalData.getInstance().makesurePlayer(playerId);
            	if (player == null) {
            		return "player not found";
            	}
            	// 机甲
        		Optional<SuperSoldier> soldierOp = player.getSuperSoldierByCfgId(soldier);
        		if (!soldierOp.isPresent()) {
        			return "super soldier not found";
        		}
        		SuperSoldier superSoldier = soldierOp.get();
        		
        		// 解锁机甲赋能
        		superSoldier.getSoldierEnergy().unlockEnergy();

        		// 设置赋能等级
        		ISuperSoldierEnergy energy = superSoldier.getSoldierEnergy().getEnergy(energyId);
        		if (energy == null) {
        			return "super soldier not found";
        		}
        		energy.gmSetLevel(cfgId);
        		superSoldier.notifyChange();
            	return "ok";
            }
            case "superSoldierEnergyMax": {
            	String playerId = map.get("playerId");
            	// 玩家
            	Player player = GlobalData.getInstance().makesurePlayer(playerId);
            	if (player == null) {
            		return "player not found";
            	}
            	// 机甲
            	List<SuperSoldier> allSuperSoldier = player.getAllSuperSoldier();
            	for (SuperSoldier superSoldier : allSuperSoldier) {
            		superSoldier.getSoldierEnergy().unlockEnergy();
            		ConfigIterator<SuperSoldierEnergyCfg> ecfgit = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierEnergyCfg.class);
            		Map<Integer, SuperSoldierEnergyCfg> configMap = new HashMap<>();
            		for (SuperSoldierEnergyCfg ecfg : ecfgit) {
            			if (ecfg.getSupersoldierId() == superSoldier.getCfgId()) {
            				configMap.merge(ecfg.getEnablingPosition(), ecfg, (v1, v2) -> v1.getEnablingLevel() > v2.getEnablingLevel() ? v1 : v2);
            			}
            		}
            		for (ISuperSoldierEnergy energy : superSoldier.getSoldierEnergy().getEnergys()) {
            			energy.gmSetLevel(configMap.get(energy.getPos()).getId());
            		}
            		superSoldier.notifyChange();
            	}
        		
            	return "ok";
            }
            case "pddExpireOrder": {
                int count = Integer.parseInt(map.get("count"));
                int cfgId = Integer.parseInt(map.get("cfgId"));
                Optional<PDDActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.PDD_ACTIVITY.intValue());
                if (opActivity.isPresent()) {
                    opActivity.get().gmAddExpireOrder(count, cfgId);
                }
                return "pddExpireOrder ok";
            }
            case "testEvent":{
                Set<String> playerIds = GlobalData.getInstance().getOnlinePlayerIds();
                for (String playerId : playerIds) {
                    Player player = GlobalData.getInstance().makesurePlayer(playerId);
                    if (player == null) {
                        HawkLog.logPrintln("WHCGMHandler luckGetGoldTen error, playerId:{}", playerId);
                        continue;
                    }
                    ActivityManager.getInstance().postEvent(new WHCGMEvent(playerId, WHCGMEvent.LUCK_GET_GOLD_DRAW_TEN));
                }
                return "testEvent success";
            }
            case "SupplyCrateActivity":{
                Optional<SupplyCrateActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SUPPLY_CRATE.intValue());
                if (opActivity.isPresent()) {
                    opActivity.get().gm(map);
                }
                return "SupplyCrateActivity success";
            }
            case "XHJZGM":{
                return XHJZWarService.getInstance().gm(map);
            }
            case "XQHXGM":{
                return XQHXWarService.getInstance().gm(map);
            }
            case "TBLYGM":{
                return TBLYWarService.getInstance().gm(map);
            }
            case "TBLYSEASONGM":{
                return TBLYSeasonService.getInstance().gm(map);
            }
            case "CMWGM":{
                return CMWService.getInstance().gm(map);
            }
            case "GuildBackActivity":{
                Optional<GuildBackActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.GUILD_BACK.intValue());
                if (opActivity.isPresent()) {
                    return opActivity.get().gm(map);
                }
                return "GuildBackActivity success";
            }
            case "NewStartActivity":{
                Optional<NewStartActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.NEW_START.intValue());
                if (opActivity.isPresent()) {
                    return opActivity.get().gm(map);
                }
                return "NewStartActivity success";
            }
            case "NewStartActivityActive":{
                String playerId = map.get("playerId");
                // 玩家
                Player player = GlobalData.getInstance().makesurePlayer(playerId);
                CrossService.getInstance().newStartCheck(player);
                return "NewStartActivityActive success";
            }
            case "CROSSSEASON":{
            	return CrossActivitySeasonService.getInstance().doGm(map);
            }
            default:{
                return "未知操作";
            }
        }
    }

    public GuildSeasonKingGradeRankProvider getRankProvider(){
        return (GuildSeasonKingGradeRankProvider) ActivityRankContext.getRankProvider(ActivityRankType.GUILD_SEASON_KING_GRADE_RANK, GuildSeasonKingGradeRank.class);
    }
}
