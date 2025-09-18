package com.hawk.game.invoker.guildBackActivity;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.guildBack.GuildBackActivity;
import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import java.util.List;
import java.util.Optional;

public class GuildBackDropInvoker extends HawkMsgInvoker {
    private List<String> playerIds;

    public GuildBackDropInvoker(List<String> playerIds) {
        this.playerIds = playerIds;
    }

    @Override
    public boolean onMessage(HawkAppObj hawkAppObj, HawkMsg hawkMsg) {
        Optional<GuildBackActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.GUILD_BACK.intValue());
        if (!opActivity.isPresent()) {
            return false;
        }
        GuildBackActivity activity = opActivity.get();
        activity.teamBattledrop(playerIds);
        return true;
    }
}
