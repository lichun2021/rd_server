package com.hawk.game.module;

import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.PBCommonMatch;
import com.hawk.game.service.commonMatch.CMWService;
import com.hawk.game.service.commonMatch.manager.ipml.XHJZSeasonManager;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerCMWModule extends PlayerModule {
    static Logger logger = LoggerFactory.getLogger("Server");
    /**
     * 构造函数
     *
     * @param player
     */
    public PlayerCMWModule(Player player) {
        super(player);
    }

    @Override
    protected boolean onPlayerLogin() {
        XHJZSeasonManager.getInstance().pageInfo(player, null);
        return true;
    }

    @ProtocolHandler(code = HP.code2.CMW_PAGE_INFO_REQ_VALUE)
    public void pageInfo(HawkProtocol hawkProtocol) {
        PBCommonMatch.PBCMWPageInfoReq req = hawkProtocol.parseProtocol(PBCommonMatch.PBCMWPageInfoReq.getDefaultInstance());
        CMWService.getInstance().pageInfo(player, req);
    }

    @ProtocolHandler(code = HP.code2.CMW_RANK_INFO_REQ_VALUE)
    public void rankInfo(HawkProtocol hawkProtocol) {
        PBCommonMatch.PBCMWRankInfoReq req = hawkProtocol.parseProtocol(PBCommonMatch.PBCMWRankInfoReq.getDefaultInstance());
        CMWService.getInstance().rankInfo(player, req);
    }

    @ProtocolHandler(code = HP.code2.CMW_BATTLE_INFO_REQ_VALUE)
    public void battleInfo(HawkProtocol hawkProtocol) {
        PBCommonMatch.PBCMWBattleInfoReq req = hawkProtocol.parseProtocol(PBCommonMatch.PBCMWBattleInfoReq.getDefaultInstance());
        CMWService.getInstance().battleInfo(player, req);
    }

    @ProtocolHandler(code = HP.code2.CMW_BATTLE_TIME_REQ_VALUE)
    public void timeInfo(HawkProtocol hawkProtocol) {
        PBCommonMatch.PBCMWBattleTimeReq req = hawkProtocol.parseProtocol(PBCommonMatch.PBCMWBattleTimeReq.getDefaultInstance());
        CMWService.getInstance().timeInfo(player, req);
    }

    @ProtocolHandler(code = HP.code2.CMW_BATTLE_TARGET_REQ_VALUE)
    public void targetInfo(HawkProtocol hawkProtocol) {
        PBCommonMatch.PBCMWBattleTargetReq req = hawkProtocol.parseProtocol(PBCommonMatch.PBCMWBattleTargetReq.getDefaultInstance());
        CMWService.getInstance().targetInfo(player, req);
    }
}
