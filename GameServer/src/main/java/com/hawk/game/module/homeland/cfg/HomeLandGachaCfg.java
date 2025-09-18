package com.hawk.game.module.homeland.cfg;

import com.hawk.game.item.ItemInfo;
import com.hawk.game.module.homeland.entity.HomeLandPoolWeight;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import java.util.ArrayList;
import java.util.List;

/**
 * 家园抽奖
 *
 * @author zhy
 */
@HawkConfigManager.XmlResource(file = "xml/homeland_gacha.xml")
public class HomeLandGachaCfg extends HawkConfigBase {
    @Id
    protected final int id;
    protected final int needCount;
    protected final String cost;
    protected final String normalPool;
    protected final String superPool;
    protected final int gachaK;
    protected final int gachaM;
    protected ItemInfo costItem;
    private final List<HomeLandPoolWeight> normalPoolWeight = new ArrayList<>();
    private final List<HomeLandPoolWeight> superPoolWeight = new ArrayList<>();
    public HomeLandGachaCfg() {
        id = 0;
        needCount = 0;
        cost = "";
        normalPool = "";
        superPool = "";
        gachaK = 0;
        gachaM = 0;
    }

    /**
     * 该建筑是否为主建筑
     *
     * @return
     */

    @Override
    protected boolean assemble() {
        if(!HawkOSOperator.isEmptyString(normalPool)){
            String[] poolInfo = normalPool.split(SerializeHelper.BETWEEN_ITEMS);
            for (String info : poolInfo) {
                String[] poolStr = info.split(SerializeHelper.ATTRIBUTE_SPLIT);
                int poolId = Integer.parseInt(poolStr[0]);
                int weight = Integer.parseInt(poolStr[1]);
                HomeLandPoolWeight poolWeight = new HomeLandPoolWeight();
                poolWeight.setPoolId(poolId);
                poolWeight.setWeight(weight);
                normalPoolWeight.add(poolWeight);
            }
        }
        if(!HawkOSOperator.isEmptyString(superPool)){
            String[] poolInfo = superPool.split(SerializeHelper.BETWEEN_ITEMS);
            for (String info : poolInfo) {
                String[] poolStr = info.split(SerializeHelper.ATTRIBUTE_SPLIT);
                int poolId = Integer.parseInt(poolStr[0]);
                int weight = Integer.parseInt(poolStr[1]);
                HomeLandPoolWeight poolWeight = new HomeLandPoolWeight();
                poolWeight.setPoolId(poolId);
                poolWeight.setWeight(weight);
                superPoolWeight.add(poolWeight);
            }
        }
        costItem = ItemInfo.valueOf(cost);
        return true;
    }

    @Override
    protected boolean checkValid() {
        return true;
    }

    public int getId() {
        return id;
    }

    public double getGachaM() {
        return gachaM / 10000.0;
    }

    public double getGachaK() {
        return gachaK / 10000.0;
    }

    public int getNeedCount() {
        return needCount;
    }

    public String getSuperPool() {
        return superPool;
    }

    public List<HomeLandPoolWeight> getNormalPoolWeight() {
        return normalPoolWeight;
    }

    public List<HomeLandPoolWeight> getSuperPoolWeight() {
        return superPoolWeight;
    }

    public ItemInfo getCostItem() {
        return costItem;
    }
}
