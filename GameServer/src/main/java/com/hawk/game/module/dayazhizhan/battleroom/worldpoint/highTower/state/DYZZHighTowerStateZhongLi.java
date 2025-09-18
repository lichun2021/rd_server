package com.hawk.game.module.dayazhizhan.battleroom.worldpoint.highTower.state;

import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.IDYZZWorldMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.highTower.DYZZHighTower;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.DYZZ.DYZZBuildState;
import com.hawk.game.service.chat.ChatParames;

public class DYZZHighTowerStateZhongLi extends IDYZZHighTowerState{

    public DYZZHighTowerStateZhongLi(DYZZHighTower parent){
        super(parent);
    }

    @Override
    public void init() {
        super.init();
        ChatParames parames = ChatParames.newBuilder().setChatType(Const.ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(Const.NoticeCfgId.DYZZ_337)
                .addParms(getParent().getX())
                .addParms(getParent().getY())
                .build();
        getParent().getParent().addWorldBroadcastMsg(parames);
    }

    @Override
    public boolean onTick() {
        IDYZZWorldMarch leaderMarch = getParent().getLeaderMarch();
        if (leaderMarch == null) {
            return true;
        }

        getParent().setStateObj(new DYZZHighTowerStateZhanLingZhong(getParent()));

        return true;
    }

    @Override
    public DYZZBuildState getState() {
        return DYZZBuildState.ZHONG_LI;
    }
}
