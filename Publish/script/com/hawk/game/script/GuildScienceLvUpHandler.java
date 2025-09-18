package com.hawk.game.script;

import com.hawk.game.GsConfig;
import com.hawk.game.entity.GuildScienceEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.service.GuildService;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import java.util.List;
import java.util.Map;

/**
 * 联盟科技一键满级
 * localhost:8080/script/guildScience?playerId=?
 */
public class GuildScienceLvUpHandler extends HawkScript {

    @Override
    public String action(Map<String, String> params, HawkScriptHttpInfo hawkScriptHttpInfo) {

        if (!GsConfig.getInstance().isDebug()) {
            return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "is not debug");
        }
        GuildService service = GuildService.getInstance();
        Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
        if (player == null) {
            return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "player not exist");
        }
        try{
            service.scienceLvUp(player.getGuildId());
        }catch (Exception e){
            HawkException.catchException(e);
            return HawkException.formatStackMsg(e);
        }
        return HawkScript.successResponse("ok");
    }
}
