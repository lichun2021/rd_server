package com.hawk.game.module.dayazhizhan.battleroom.worldpoint.highTower;

import com.hawk.game.module.dayazhizhan.battleroom.DYZZBattleRoom;
import com.hawk.game.module.dayazhizhan.battleroom.cfg.DYZZHighTowerCfg;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.IDYZZBuilding;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.highTower.state.IDYZZHighTowerState;
import com.hawk.game.protocol.DYZZ;
import com.hawk.game.protocol.World.WorldPointDetailPB;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointType;
import org.hawk.config.HawkConfigManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DYZZHighTower extends IDYZZBuilding {
    private IDYZZHighTowerState stateObj;
    private Map<String, Integer> winCountMap = new ConcurrentHashMap<>();
    private Map<String, Integer> atkAddMap = new ConcurrentHashMap<>();

    public DYZZHighTower(DYZZBattleRoom parent){
        super(parent);
    }

    @Override
    public boolean onTick() {
        return stateObj.onTick();
    }

    @Override
    public long getProtectedEndTime() {
        return stateObj.getProtectedEndTime();
    }

    public static DYZZHighTowerCfg getCfg() {
        return HawkConfigManager.getInstance().getKVInstance(DYZZHighTowerCfg.class);
    }

    @Override
    public WorldPointType getPointType() {
        return WorldPointType.DYZZ_HIGH_TOWER;
    }

    @Override
    public int getControlCountDown() {
        return getCfg().getControlCountDown();
    }

    public IDYZZHighTowerState getStateObj() {
        return stateObj;
    }

    public void setStateObj(IDYZZHighTowerState stateObj) {
        this.stateObj = stateObj;
        this.stateObj.init();
        getParent().worldPointUpdate(this);
    }

    @Override
    public WorldPointPB.Builder toBuilder(IDYZZPlayer viewer) {
        WorldPointPB.Builder builder = super.toBuilder(viewer);
        this.stateObj.fillBuilder(builder);
        return builder;
    }

    @Override
    public WorldPointDetailPB.Builder toDetailBuilder(IDYZZPlayer viewer) {
        WorldPointDetailPB.Builder builder = super.toDetailBuilder(viewer);
        this.stateObj.fillDetailBuilder(builder);
        builder.setDyzzHighTowerWinCount(winCountMap.getOrDefault(viewer.getDYZZGuildId(), 0));
        return builder;
    }

    @Override
    public DYZZ.DYZZBuildState getState() {
        return stateObj.getState();
    }

    @Override
    public String getGuildId() {
        if (getLeaderMarch() == null) {
            return "";
        }
        return getLeaderMarch().getParent().getDYZZGuildId();
    }

    @Override
    public String getGuildTag() {
        if (getLeaderMarch() == null) {
            return "";
        }
        return getLeaderMarch().getParent().getGuildTag();
    }

    @Override
    public int getGuildFlag() {
        if (getLeaderMarch() == null) {
            return 0;
        }
        return getLeaderMarch().getParent().getGuildFlag();
    }

    public void incWin(String guildid){
        int winCount = winCountMap.getOrDefault(guildid, 0);
        winCount++;
        winCountMap.put(guildid, winCount);
        DYZZHighTowerCfg cfg = getCfg();
        if(winCount <= cfg.getAtkAdd().size()){
            int atk = cfg.getAtkAdd().get(winCount - 1);
//            int curAtk = atkAddMap.getOrDefault(guildid, 0);
//            atkAddMap.put(guildid, curAtk + atk);
            atkAddMap.put(guildid, atk);
        }
    }

    public int getAtk(String guildid){
        return atkAddMap.getOrDefault(guildid, 0);
    }
}
