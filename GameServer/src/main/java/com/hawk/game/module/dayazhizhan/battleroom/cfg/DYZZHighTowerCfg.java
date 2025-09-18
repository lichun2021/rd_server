package com.hawk.game.module.dayazhizhan.battleroom.cfg;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;

import java.util.ArrayList;
import java.util.List;

@HawkConfigManager.KVResource(file = "xml/dyzz_high_tower.xml")
public class DYZZHighTowerCfg extends HawkConfigBase {

    private final int redis;
    // # 刷新点 x_y, 半径为偶数 x,y相加必须为奇数
    private final String refreshPoint;// = 43_42,58_59,60_61,75_50
    // # 占领倒计时 /秒
    private final int controlCountDown;// = 30
    private final String coolDown;
    private final String atkAddList;

    private ImmutableList<HawkTuple2<Integer, Integer>> refreshPointList;
    private HawkTuple2<Integer, Integer> coolDownArr;
    private ImmutableList<Integer> atkAdd;
    public DYZZHighTowerCfg() {
        redis = 3;
        refreshPoint = "43_42,58_59";
        controlCountDown = 30;
        coolDown = "60_120";
        atkAddList = "2,4,6,8";
    }

    @Override
    protected boolean assemble() {
        {
            List<HawkTuple2<Integer, Integer>> lsit = new ArrayList<>();
            for (String xy : refreshPoint.trim().split("\\,")) {
                String[] x_y = xy.split("_");
                int[] pos = new int[2];
                pos[0] = NumberUtils.toInt(x_y[0]);
                pos[1] = NumberUtils.toInt(x_y[1]);
                lsit.add(HawkTuples.tuple(pos[0], pos[1]));
            }
            refreshPointList = ImmutableList.copyOf(lsit);
        }

        {
            String[] x_y = coolDown.split("_");
            int[] pos = new int[2];
            pos[0] = NumberUtils.toInt(x_y[0]);
            pos[1] = NumberUtils.toInt(x_y[1]);
            coolDownArr = HawkTuples.tuple(pos[0], pos[1]);
        }

        {
            List<Integer> list = new ArrayList<>();
            for(String atk : atkAddList.trim().split(",")){
                list.add(NumberUtils.toInt(atk));
            }
            atkAdd = ImmutableList.copyOf(list);
        }
        return super.assemble();
    }

    public ImmutableList<HawkTuple2<Integer, Integer>> getRefreshPointList() {
        return refreshPointList;
    }


    public String getRefreshPoint() {
        return refreshPoint;
    }

    public int getControlCountDown() {
        return controlCountDown;
    }

    public int getRedis() {
        return redis;
    }

    public String getCoolDown() {
        return coolDown;
    }

    public HawkTuple2<Integer, Integer> getCoolDownArr() {
        return coolDownArr;
    }

    public ImmutableList<Integer> getAtkAdd() {
        return atkAdd;
    }
}
