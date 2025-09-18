package com.hawk.game.module.dayazhizhan.battleroom.worldpoint.highTower.state;

import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.highTower.DYZZHighTower;
import com.hawk.game.protocol.DYZZ;
import com.hawk.game.protocol.World;

public abstract class IDYZZHighTowerState {
    private final DYZZHighTower parent;

    public IDYZZHighTowerState(DYZZHighTower parent) {
        this.parent = parent;
    }

    public void init() {
    }

    public abstract boolean onTick();

    public abstract DYZZ.DYZZBuildState getState();

    public DYZZHighTower getParent() {
        return parent;
    }

    public void fillBuilder(World.WorldPointPB.Builder builder) {
    }

    public void fillDetailBuilder(World.WorldPointDetailPB.Builder builder) {

    }

    public long getProtectedEndTime() {
        // TODO Auto-generated method stub
        return 0;
    }
}
