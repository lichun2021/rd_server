package com.hawk.game.script;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.*;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.order.entity.ActivityOrderEntity;
import com.hawk.activity.type.impl.seasonActivity.SeasonActivity;
import com.hawk.activity.type.impl.seasonActivity.cfg.SeasonGradeLevelCfg;
import com.hawk.activity.type.impl.seasonActivity.cfg.SeasonKingAwardCfg;
import com.hawk.activity.type.impl.seasonActivity.entity.SeasonActivityEntity;
import com.hawk.activity.type.impl.seasonActivity.rank.GuildSeasonKingGradeInfo;
import com.hawk.activity.type.impl.seasonActivity.rank.GuildSeasonKingGradeRank;
import com.hawk.activity.type.impl.seasonActivity.rank.GuildSeasonKingGradeRankProvider;
import com.hawk.game.GsConfig;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.Reward;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * localhost:8080/script/seasongm
 */

public class SeasonActivityTestHandler extends HawkScript {
    @Override
    public String action(Map<String, String> map, HawkScriptHttpInfo hawkScriptHttpInfo) {
        String opt = map.get("opt");
        if(HawkOSOperator.isEmptyString(map.get("opt"))){
           return "opt is null";
        }
        switch (opt){
            case "buildRankList":{
                return buildRankList(Integer.parseInt(map.get("count")));
            }
            case "buildGuildRankInfo":{
                return buildGuildRankInfo(map.get("guildid"));
            }
            case "addGuildGradeExp":{
                return addGuildGradeExp(map.get("guildid"), Integer.parseInt(map.get("exp")));
            }
            case "addPlayerOrderExp":{
                return addPlayerOrderExp(map.get("playerid"), Integer.parseInt(map.get("exp")));
            }
            case "testMail":{
                return testMail(map.get("playerid"), map);
            }
            case "testEvent":{
                return testEvent(map.get("playerid"), map);
            }
            case "setSeasonHonor":{
                return setSeasonHonor(map);
            }
            case "setStarAndConquer":{
                return setStarAndConquer(map);
            }
            default:{
                return "unknow opt";
            }
        }
    }

    public String setSeasonHonor(Map<String, String> map){
        String type = map.get("type");
        return successResponse("setSeasonHonor succese");
    }

    public String setStarAndConquer(Map<String, String> map){
        // 读文件
        List<String> infos = new ArrayList<>();
        try {
            HawkOSOperator.readTextFileLines("tmp/cross_star_conquer_info.txt", infos);
            if(infos.size() > 0){
                for (String info : infos) {
                    String serverId = info.split(",")[0];
                    String star = info.split(",")[1];
                    String conquer = info.split(",")[2];
                    RedisProxy.getInstance().getRedisSession().hSet("fortress_star", serverId, star);
                    RedisProxy.getInstance().getRedisSession().hSet("cross_conquer_rank", serverId, conquer);
                }
            }
        } catch (Exception e) {
            HawkException.catchException(e);
        }
        return successResponse("setSeasonHonor succese");
    }

    public String buildRankList(int count){
        Optional<SeasonActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SEASON_ACTIVITY.intValue());
        if (!opActivity.isPresent()) {
            return "";
        }
        SeasonActivity activity = opActivity.get();
        GuildSeasonKingGradeRankProvider provider = activity.getRankProvider();
        for(int i = 0; i < count; i++){
            GuildSeasonKingGradeRank rank = new GuildSeasonKingGradeRank();
            rank.setId("guild_"+i);
            rank.setScore(11000 + i);
            provider.insertIntoRank(rank);
            GuildSeasonKingGradeInfo info = new GuildSeasonKingGradeInfo();
            info.setGuildId("guild_"+i);
            info.setGuildName("gn_"+i);
            info.setGuildTag("gt_"+i);
            info.setGuildFlag(10);
            info.setGuildLeader("gl_"+i);
            info.setServerId(GsConfig.getInstance().getServerId());
            provider.updataGuildInfo("guild_"+i, info);
        }
        return successResponse("buildRankList succese");
    }

    public String buildGuildRankInfo(String guildId){
        Optional<SeasonActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SEASON_ACTIVITY.intValue());
        if (!opActivity.isPresent()) {
            return "activity is not open";
        }
        SeasonActivity activity = opActivity.get();
        GuildSeasonKingGradeRankProvider provider = activity.getRankProvider();
        GuildSeasonKingGradeRank rank = new GuildSeasonKingGradeRank();
        rank.setId(guildId);
        rank.setScore(12000);
        provider.insertIntoRank(rank);
        GuildSeasonKingGradeInfo info = new GuildSeasonKingGradeInfo();
        info.setGuildId(guildId);
        info.setGuildName("111");
        info.setGuildTag("111");
        info.setGuildFlag(10);
        info.setGuildLeader("指挥官41abe00");
        info.setServerId(GsConfig.getInstance().getServerId());
        provider.updataGuildInfo(guildId, info);
        return successResponse("buildGuildRankInfo succese");
    }

    public String addGuildGradeExp(String guildId, int exp){
        Optional<SeasonActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SEASON_ACTIVITY.intValue());
        if (!opActivity.isPresent()) {
            return "activity is not open";
        }
        SeasonActivity activity = opActivity.get();
        activity.addGuildGradeExp(guildId, exp);
        return successResponse("addGuildGradeExp succese");
    }

    public String addPlayerOrderExp(String playerId, int exp){
        Optional<SeasonActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SEASON_ACTIVITY.intValue());
        if (!opActivity.isPresent()) {
            return "activity is not open";
        }
        SeasonActivity activity = opActivity.get();
        Optional<SeasonActivityEntity> opEntity = activity.getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return "player data is null";
        }
        SeasonActivityEntity dataEntity = opEntity.get();
        activity.addExp(dataEntity, exp, -1, -1);
        return successResponse("addPlayerOrderExp succese");
    }

    public String testMail(String playerId, Map<String, String> map){
        switch (map.get("mailId")){
            case "SEASON_ACTIVITY_GRADE":{
                int grade = Integer.parseInt(map.get("grade"));
                SeasonGradeLevelCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SeasonGradeLevelCfg.class, grade);
                List<ItemInfo> items = new ArrayList<>();
                if (cfg.getNormalRewardList() != null) {
                    for (Reward.RewardItem.Builder activityItemInfo : cfg.getNormalRewardList()) {
                        items.add(new ItemInfo(activityItemInfo.getItemType(), activityItemInfo.getItemId(), (int) activityItemInfo.getItemCount()));
                    }
                }
                SystemMailService.getInstance().sendMail(MailParames.newBuilder()
                        .setPlayerId(playerId)
                        .setMailId(MailConst.MailId.SEASON_ACTIVITY_GRADE)
                        .setAwardStatus(Const.MailRewardStatus.NOT_GET)
                        .addContents(new Object[]{grade})
                        .addRewards(items)
                        .build());
            }
            break;
            case "SEASON_ACTIVITY_KING_RANK":{
                int rank = Integer.parseInt(map.get("rank"));
                SeasonKingAwardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SeasonKingAwardCfg.class, rank);
                List<ItemInfo> items = new ArrayList<>();
                if (cfg.getRewardList() != null) {
                    for (Reward.RewardItem.Builder activityItemInfo : cfg.getRewardList()) {
                        items.add(new ItemInfo(activityItemInfo.getItemType(), activityItemInfo.getItemId(), (int) activityItemInfo.getItemCount()));
                    }
                }
                SystemMailService.getInstance().sendMail(MailParames.newBuilder()
                        .setPlayerId(playerId)
                        .setMailId(MailConst.MailId.SEASON_ACTIVITY_KING_RANK)
                        .setAwardStatus(Const.MailRewardStatus.NOT_GET)
                        .addContents(new Object[]{ rank })
                        .addRewards(items)
                        .build());
            }
            break;
            case "SEASON_ACTIVITY_ORDER_REWARD":{
                SystemMailService.getInstance().sendMail(MailParames.newBuilder()
                        .setPlayerId(playerId)
                        .setMailId(MailConst.MailId.SEASON_ACTIVITY_ORDER_REWARD)
                        .setAwardStatus(Const.MailRewardStatus.NOT_GET)
                        .build());
            }
            break;
        }
        return successResponse("testMail succese");
    }

    public String testEvent(String playerId, Map<String, String> map){
        switch (map.get("event")){
            case "TWScoreEvent":{
                ActivityManager.getInstance().postEvent(new TWScoreEvent(playerId, 10000,true, HawkTime.getMillisecond()));
            }
            break;
            case "CWScoreEvent":{
                ActivityManager.getInstance().postEvent(new CWScoreEvent(playerId, 10000,true, true));
            }
            break;
            case "DYZZScoreEvent":{
                ActivityManager.getInstance().postEvent(new DYZZScoreEvent(playerId, 10000,HawkTime.getMillisecond(),true));
            }
            break;
            case "SWScoreEvent":{
                ActivityManager.getInstance().postEvent(new SWScoreEvent(playerId, 10000,10000,HawkTime.getMillisecond(),true));
            }
            break;
            case "YQZZScoreEvent":{
                ActivityManager.getInstance().postEvent(new YQZZScoreEvent(playerId, 10000,HawkTime.getMillisecond(),true));
            }
            case "CWGradeEvent":{
                ActivityManager.getInstance().postEvent(new CWGradeEvent(playerId, 1));
            }
            case "DYZZGradeEvent":{
                ActivityManager.getInstance().postEvent(new DYZZGradeEvent(playerId, 1, Integer.parseInt(map.get("seasonTerm"))));
            }
            break;
        }
        return successResponse("testEvent succese");
    }
}
