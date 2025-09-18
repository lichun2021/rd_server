package com.hawk.activity.type.impl.heavenBlessing.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.RangeMap;
import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "activity/heaven_blessing/heaven_blessing_vip_cfg.xml")
public class HeavenBlessingVipCfg extends HawkConfigBase {
    /**
     * vip等级
     */
    @Id
    private final int vip;

    /**
     * 付费挡位分组
     */
    private final String payGroup;
    /**
     * 付费值分组
     */
    private RangeMap<Integer, Integer> rangeMap;

    public HeavenBlessingVipCfg() {
        vip = 0;
        payGroup = "";
    }

    @Override
    protected boolean assemble() {
        try {
            //把字符串转换成RangeMap
            rangeMap = ImmutableRangeMap.copyOf(SerializeHelper.str2RangeMap(payGroup));
            return true;
        }catch (Exception e){
            HawkException.catchException(e, new Object[0]);
            return false;
        }
    }

    //根据付费获得对应的付费组id
    public int getGroupId(int payMoney){
        Integer value = rangeMap.get(payMoney);
        return value == null ? 0 : value;
    }
}
