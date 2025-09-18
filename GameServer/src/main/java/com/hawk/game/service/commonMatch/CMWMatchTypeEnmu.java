package com.hawk.game.service.commonMatch;

import com.hawk.game.protocol.PBCommonMatch;
import com.hawk.game.service.commonMatch.manager.CMWManagerBase;
import com.hawk.game.service.commonMatch.manager.ipml.XHJZSeasonManager;

public enum CMWMatchTypeEnmu {
    XHJZ_SEASON(PBCommonMatch.PBCMWMatchType.XHJZ_SEASON, XHJZSeasonManager.getInstance()),
    ;

    private PBCommonMatch.PBCMWMatchType type;
    private CMWManagerBase manager;

    CMWMatchTypeEnmu(PBCommonMatch.PBCMWMatchType type, CMWManagerBase manager) {
        this.type = type;
        this.manager = manager;
    }

    public PBCommonMatch.PBCMWMatchType getType() {
        return type;
    }

    public CMWManagerBase getManager() {
        return manager;
    }
}
