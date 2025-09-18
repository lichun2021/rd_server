package com.hawk.game.module.homeland.cfg;

import com.hawk.game.module.homeland.entity.HomeLandPoolWeight;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 家园抽奖固定次数
 *
 * @author zhy
 */
@HawkConfigManager.XmlResource(file = "xml/homeland_gacha_fixed.xml")
public class HomeLandGachaFixedCfg extends HawkConfigBase {
    @Id
    protected final int id;
    protected final int number;
    protected final String homeland_gacha_pool;
    public static Map<Integer,List<HomeLandPoolWeight>> gachaFixedMap = new HashMap<>();
    private final List<HomeLandPoolWeight> poolWeights = new ArrayList<>();
    public HomeLandGachaFixedCfg() {
        id = 0;
        number = 0;
        homeland_gacha_pool = "";
    }

    /**
     * 该建筑是否为主建筑
     *
     * @return
     */

    @Override
    protected boolean assemble() {
        if(!HawkOSOperator.isEmptyString(homeland_gacha_pool)){
            String[] poolInfo = homeland_gacha_pool.split(SerializeHelper.BETWEEN_ITEMS);
            for (String info : poolInfo) {
                String[] poolStr = info.split(SerializeHelper.ATTRIBUTE_SPLIT);
                int poolId = Integer.parseInt(poolStr[0]);
                int weight = Integer.parseInt(poolStr[1]);
                HomeLandPoolWeight poolWeight = new HomeLandPoolWeight();
                poolWeight.setPoolId(poolId);
                poolWeight.setWeight(weight);
                poolWeights.add(poolWeight);
            }
        }
        gachaFixedMap.put(number, poolWeights);
        return true;
    }

    public static Map<Integer, List<HomeLandPoolWeight>> hardPityRules() {
        return gachaFixedMap;
    }

    public int getNumber() {
        return number;
    }

    @Override
    protected boolean checkValid() {
        return true;
    }

    public int getId() {
        return id;
    }
}
