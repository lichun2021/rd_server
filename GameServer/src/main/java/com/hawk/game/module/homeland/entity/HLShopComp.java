package com.hawk.game.module.homeland.entity;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.module.homeland.cfg.HomeLandGachaCfg;
import com.hawk.game.module.homeland.cfg.HomeLandGachaFixedCfg;
import com.hawk.game.module.homeland.cfg.HomeLandGachaPoolCfg;
import com.hawk.game.player.hero.SerializJsonStrAble;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class HLShopComp implements SerializJsonStrAble {
    private int drawTimes;
    private int dailyDrawTimes;
    //保底5
    private int pityCFor5;
    /**
     * 上一次收取时间
     */
    private long lastCollectRecruit;

    /**
     * 序列化
     */
    @Override
    public String serializ() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("drawTimes", getDrawTimes());
        jsonObject.put("dailyDrawTimes", getDailyDrawTimes());
        jsonObject.put("pityCFor5", getPityCFor5());
        jsonObject.put("lastCollectRecruit", getLastCollectRecruit());
        return jsonObject.toString();
    }

    @Override
    public String toString() {
        return "HLShopComp{" +
                "drawTimes=" + drawTimes +
                ", dailyDrawTimes=" + dailyDrawTimes +
                ", pityCFor5=" + pityCFor5 +
                '}';
    }

    @Override
    public void mergeFrom(String serialiedStr) {
        JSONObject obj = JSONObject.parseObject(serialiedStr);
        if (obj != null) {
            if (obj.containsKey("drawTimes")) {
                this.drawTimes = obj.getInteger("drawTimes");
            }
            if (obj.containsKey("dailyDrawTimes")) {
                this.dailyDrawTimes = obj.getInteger("dailyDrawTimes");
            }
            if (obj.containsKey("pityCFor5")) {
                this.pityCFor5 = obj.getInteger("pityCFor5");
            }
            if (obj.containsKey("lastCollectRecruit")) {
                this.lastCollectRecruit = obj.getLong("lastCollectRecruit");
            }
        }
    }

    public HomeLandGachaCfg findPool(int drawTimes) {
        ConfigIterator<HomeLandGachaCfg> poolCfg = HawkConfigManager.getInstance().getConfigIterator(HomeLandGachaCfg.class);
        List<HomeLandGachaCfg> allPool = poolCfg.stream().sorted(Comparator.comparingInt(HomeLandGachaCfg::getNeedCount)).collect(Collectors.toList());
        HomeLandGachaCfg result = null;
        for (HomeLandGachaCfg pool : allPool) {
            if (drawTimes >= pool.getNeedCount()) {
                result = pool;
            } else {
                break;
            }
        }
        if (result == null) {
            return HawkConfigManager.getInstance().getConfigByIndex(HomeLandGachaCfg.class, 0);
        }
        return result;
    }

    public int randomPool(List<HomeLandPoolWeight> poolWeight) {
        if (poolWeight == null || poolWeight.isEmpty()) {
            return 0;
        }
        int totalGroupWeight = poolWeight.stream().mapToInt(HomeLandPoolWeight::getWeight).sum();
        int randomGroupValue = new Random().nextInt(totalGroupWeight);
        HomeLandPoolWeight selectedGroup = null;
        int cumulativeGroupWeight = 0;
        for (HomeLandPoolWeight group : poolWeight) {
            cumulativeGroupWeight += group.getWeight();
            if (randomGroupValue < cumulativeGroupWeight) {
                selectedGroup = group;
                break;
            }
        }
        if (selectedGroup == null) {
            return 0;
        }
        return selectedGroup.getPoolId();
    }

    /**
     * The main draw method.
     * 核心抽奖方法。
     */
    public int gacha(int drawTimes, HomeLandGachaCfg gachaCfg) {
        // --- Rule 1: (固定次数保底检查) ---
        if (HomeLandGachaFixedCfg.hardPityRules().containsKey(drawTimes)) {
            List<HomeLandPoolWeight> pityPools = HomeLandGachaFixedCfg.hardPityRules().get(drawTimes);
            int poolId = randomPool(pityPools);
            HomeLandGachaPoolCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandGachaPoolCfg.class, poolId);
            if (poolCfg == null) {
                HawkLog.errPrintln("exchange fixed gacha failed drawTimes:{}", drawTimes, poolId);
                return 0;
            }
            return poolCfg.getReward();
        }
        // --- Rule 2:(品质5动态保底检查) ---
        double n = pityCFor5;
        double probability = Math.pow(n / gachaCfg.getGachaM(), gachaCfg.getGachaK());
        boolean quality5Won = new Random().nextDouble() < probability;
        if (quality5Won) {
            pityCFor5 = 0;
            int superPoolId = randomPool(gachaCfg.getSuperPoolWeight());
            HomeLandGachaPoolCfg superPoolCfg = HawkConfigManager.getInstance().getConfigByKey(HomeLandGachaPoolCfg.class, superPoolId);
            if (superPoolCfg == null) {
                HawkLog.errPrintln("exchange super gacha failed drawTimes:{}", drawTimes, superPoolId);
                return 0;
            }
            return superPoolCfg.getReward();
        }
        pityCFor5++;
        // --- Rule 3: (常规抽奖) ---
        int normalPoolId = randomPool(gachaCfg.getNormalPoolWeight());
        HomeLandGachaPoolCfg normalPool = HawkConfigManager.getInstance().getConfigByKey(HomeLandGachaPoolCfg.class, normalPoolId);
        if (normalPool == null) {
            HawkLog.errPrintln("exchange normal gacha failed drawTimes:{}", drawTimes, normalPoolId);
            return 0;
        }
        return normalPool.getReward();
    }

    public int getDrawTimes() {
        return drawTimes;
    }

    public int getDailyDrawTimes() {
        return dailyDrawTimes;
    }


    public void setDrawTimes(int drawTimes) {
        this.drawTimes = drawTimes;
    }

    public void setDailyDrawTimes(int dailyDrawTimes) {
        this.dailyDrawTimes = dailyDrawTimes;
    }


    public int getPityCFor5() {
        return pityCFor5;
    }

    public long getLastCollectRecruit() {
        return lastCollectRecruit;
    }

    public void setLastCollectRecruit(long lastCollectRecruit) {
        this.lastCollectRecruit = lastCollectRecruit;
    }

    public int getDrawCost() {
        HomeLandGachaCfg cfg = findPool(this.drawTimes);
        if (cfg == null) {
            HomeLandGachaCfg defaultCfg = HawkConfigManager.getInstance().getConfigByIndex(HomeLandGachaCfg.class, 0);
            return Math.toIntExact(defaultCfg.getCostItem().getCount());
        }
        return Math.toIntExact(cfg.getCostItem().getCount());
    }
}
