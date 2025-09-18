package com.hawk.game.tsssdk.invoker;

import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.PresidentConstCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.YQZZWar;
import com.hawk.game.service.SearchService;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import org.hawk.net.protocol.HawkProtocol;

import java.util.*;

@Category(scene = GameMsgCategory.YQZZ_SEARCH_MEMBER)
public class YQZZSearchMemberInvoker implements TsssdkInvoker {
    @Override
    public int invoke(Player player, int result, String name, int protocol, String callback) {
        if (result != 0) {
            player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
            return 0;
        }
        int type = Integer.parseInt(callback);
        searchMemeber(player, name, type);
        return 0;
    }

    public void searchMemeber(Player player, String name, int type) {
        int maxCount = PresidentConstCfg.getInstance().getSearchMaxCount();
        List<String> idList = SearchService.getInstance().searchPlayerByNameIgnore(name, 0, 0, maxCount, new ArrayList<>(), ConstProperty.getInstance().getSearchPrecise() > 0);
        synMember(player, new HashSet<>(idList));
    }

    private void synMember(Player player, Set<String> playerIds) {
        YQZZWar.YQZZSearchResp.Builder builder = YQZZWar.YQZZSearchResp.newBuilder();
        for (String playerId : playerIds) {
            Player searchPlayer = GlobalData.getInstance().makesurePlayer(playerId);
            if (null != searchPlayer) {
                YQZZWar.YQZZMiniPlayerMsg.Builder miniPlayer = YQZZWar.YQZZMiniPlayerMsg.newBuilder();
                miniPlayer.setPlayerId(searchPlayer.getId());
                miniPlayer.setPlayerName(searchPlayer.getName());
                miniPlayer.setIcon(searchPlayer.getIcon());
                miniPlayer.setPfIcon(searchPlayer.getPfIcon() == null ? "" : searchPlayer.getPfIcon());
                miniPlayer.setGuildName(searchPlayer.getGuildName());
                miniPlayer.setPower(searchPlayer.getPower());
                builder.addPlayerMsg(miniPlayer);
            }
        }
        player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_SEARCH_RESP, builder));
    }
}
