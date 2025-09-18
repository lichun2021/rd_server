package com.hawk.game.config;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.item.ItemInfo;
import com.hawk.serialize.string.SerializeHelper;
import org.apache.commons.lang.math.IEEE754rUtils;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;

import java.util.ArrayList;
import java.util.List;

/**
 * 终身卡
 *
 * @author Golden
 */
@HawkConfigManager.KVResource(file = "xml/lifetime_card_const.xml")
public class LifetimeCardCfg extends HawkConfigBase {

    /**
     * 单例
     */
    private static LifetimeCardCfg instance = null;


    /**
     * 大本解锁等级
     */
    protected final int unlockCityLevel;

    /**
     * 免费体验时间(s)
     */
    protected final long freeTime;

    /**
     * 老账号免费体验时间(s)
     */
    protected final long oldFreeTime;

    /**
     * 解锁奖励
     */
    protected final String unlockAward;

    /**
     * 每周奖励
     */
    protected final String weekAward;

    /**
     * 每月奖励
     */
    protected final int monthAward;

    /**
     * 终身特权(普通作用号)
     */
    protected final String commonEff;

    /**
     * 进阶特权(高级作用号)
     */
    protected final String advanceEff;

    /**
     * 进阶卡持续时间(s)
     */
    protected final int advancedContinue;

    /**
     * 陨晶战场加速道具
     */
    protected final String itemId;

    /**
     * 陨晶战场加速道具价格
     */
    protected final String price;

    /**
     * 玩家注册时间判断条件
     */
    private final String startTime;

    private final int goFace;

    private final int lateFace;

    private final int advanceRewardDay;

    /**
     * 普通作用号
     */
    private List<EffectObject> commonEffList;

    /**
     * 高级作用号
     */
    private List<EffectObject> advanceEffList;

    public static LifetimeCardCfg getInstance() {
        return instance;
    }

    public LifetimeCardCfg() {
        instance = this;
        unlockCityLevel = 0;
        freeTime = 0;
        oldFreeTime = 0;
        unlockAward = "";
        weekAward = "";
        monthAward = 0;
        commonEff = "";
        advanceEff = "";
        advancedContinue = 0;
        price = "";
        itemId = "";
        startTime = "";
        goFace = 0;
        lateFace = 0;
        advanceRewardDay = 0;
    }

    public int getUnlockCityLevel() {
        return unlockCityLevel;
    }

    public long getFreeTime() {
        return freeTime * 1000L;
    }

    public long getOldFreeTime() {
        return oldFreeTime * 1000L;
    }

    public String getUnlockAward() {
        return unlockAward;
    }

    public List<ItemInfo> getWeekAward() {
        return ItemInfo.valueListOf(weekAward);
    }

    public List<ItemInfo> getMonthAward() {
        AwardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, monthAward);
        String award = cfg.getAward();
        String[] split = award.split(SerializeHelper.BETWEEN_ITEMS);
        List<ItemInfo> infos = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        for (String s:split){
            ItemInfo itemInfo = ItemInfo.valueOf(s.substring(0, s.lastIndexOf(SerializeHelper.ATTRIBUTE_SPLIT)));
            infos.add(itemInfo);
            weights.add(Integer.parseInt(s.substring(s.lastIndexOf(SerializeHelper.ATTRIBUTE_SPLIT) + 1)));
        }
        return HawkRand.randomWeightObject(infos, weights, 1);
    }

    public long getAdvancedContinue() {
        return advancedContinue * 1000L;
    }

    public List<EffectObject> getCommonEffList() {
        return new ArrayList<>(commonEffList);
    }

    public List<EffectObject> getAdvanceEffList() {
        return new ArrayList<>(advanceEffList);
    }

    public String getItemId() {
        return itemId;
    }

    public String getPrice() {
        return price;
    }

    public String getStartTime() {
        return startTime;
    }

    public long getStartTimeMill() {
        return HawkTime.parseTime(startTime);
    }

    public int getGoFace() {
        return goFace;
    }

    public int getLateFace() {
        return lateFace;
    }

    public int getAdvanceRewardDay() {
        return advanceRewardDay;
    }

    @Override
    protected boolean assemble() {
        commonEffList = stringToList(commonEff);
        advanceEffList = stringToList(advanceEff);

        return true;
    }

    public List<EffectObject> stringToList(String str) {

        List<EffectObject> list = new ArrayList<>();
        String[] split = str.split(SerializeHelper.BETWEEN_ITEMS);
        for (String s: split){
            String[] split1 = s.split(SerializeHelper.ELEMENT_SPLIT);
            for (String s1: split1){
                String[] split2 = s1.split(SerializeHelper.ATTRIBUTE_SPLIT);
                EffectObject effectObject = new EffectObject(Integer.parseInt(split2[0]), Integer.parseInt(split2[1]));
                list.add(effectObject);
            }
        }

        return list;
    }
}
