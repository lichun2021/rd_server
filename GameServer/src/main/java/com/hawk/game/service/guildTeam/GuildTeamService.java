package com.hawk.game.service.guildTeam;

import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.Result;
import com.hawk.game.protocol.GuildTeam.*;
import com.hawk.log.Action;
import org.hawk.app.HawkAppObj;
import org.hawk.xid.HawkXID;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GuildTeamService extends HawkAppObj {
    Map<GuildTeamType, GuildTeamManagerBase> managerMap = new ConcurrentHashMap<>();

    private static GuildTeamService instance = null;

    public static GuildTeamService getInstance() {
        return instance;
    }

    public GuildTeamService(HawkXID xid) {
        super(xid);
        instance = this;
    }

    public boolean init(){
        for(GuildTeamEnum guildTeamEnum : GuildTeamEnum.values()){
            managerMap.put(guildTeamEnum.getType(), guildTeamEnum.getManager());
            guildTeamEnum.getManager().init();
        }
        return true;
    }

    public int checkGuildOperation(String guildId, String playerId) {
        for(GuildTeamEnum guildTeamEnum : GuildTeamEnum.values()){
        	int checkRlt = guildTeamEnum.getManager().checkGuildOperation(guildId, playerId);
            if(checkRlt != Result.SUCCESS_VALUE){
                return checkRlt;
            }
        }
        return Result.SUCCESS_VALUE;
    }

    public boolean checkTeamManager(Player player, GuildBattleTeamManagerReq req){
        return managerMap.get(req.getType()).checkTeamManager(player, req);
    }

    public boolean checkMemberManager(Player player, GuildBattleMemberManagerReq req){
        return managerMap.get(req.getType()).checkMemberManager(player, req);
    }

    public boolean checkTeamManagerCost(Player player, GuildBattleTeamManagerReq req){
        if(req.getOpt() != GuildTeamOpt.GT_TEAM_CREATE){
            return true;
        }
        ConsumeItems consume = ConsumeItems.valueOf();
        consume.addConsumeInfo(ItemInfo.valueListOf("10000_1001_200"));
        if(!consume.checkConsume(player)){
            return false;
        }
        consume.consumeAndPush(player, Action.XHJZ_SHOP_COST);
        return true;
    }


    public void onGuildDismiss(String guildId) {
        for(GuildTeamEnum guildTeamEnum : GuildTeamEnum.values()){
            guildTeamEnum.getManager().onGuildDismiss(guildId);
        }
    }

    public void onQuitGuild(Player player){
        for(GuildTeamEnum guildTeamEnum : GuildTeamEnum.values()){
            guildTeamEnum.getManager().onQuitGuild(player);
        }
    }

    public void teamManager(Player player, GuildBattleTeamManagerReq req){
        if(!checkTeamManager(player, req)){
            return;
        }
        managerMap.get(req.getType()).teamManager(player, req);
    }

    public void memberManager(Player player, GuildBattleMemberManagerReq req){
        if(!checkMemberManager(player, req)){
            return;
        }
        managerMap.get(req.getType()).memberManager(player, req);
    }

    public void teamList(Player player, GuildBattleTeamListReq req){
        managerMap.get(req.getType()).teamList(player, req);
    }

    public void memberList(Player player, GuildBattleMemberListReq req){
        managerMap.get(req.getType()).memberList(player, req);
    }

}
