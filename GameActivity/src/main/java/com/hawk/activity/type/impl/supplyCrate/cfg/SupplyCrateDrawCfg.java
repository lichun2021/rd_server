package com.hawk.activity.type.impl.supplyCrate.cfg;

import com.google.common.collect.Range;
import com.google.common.collect.RangeMap;
import com.google.common.collect.TreeRangeMap;
import com.hawk.activity.type.impl.supplyCrate.entity.SupplyCrateItemObj;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* 本文件自动生成，重新生成会被覆盖，请手动保留自定义部分
*/
@HawkConfigManager.XmlResource(file = "activity/supply_crate/supply_crate_draw.xml")
public class SupplyCrateDrawCfg extends HawkConfigBase{
    @Id
    private final int id;
    private final String custom;
    private final String normal;
    private final String range;
    private final int doubleWeight;
    private final String useItemNum;

    private int minRound;
    private int maxRound;
    private List<SupplyCrateItemObj> normalList;
    private Map<Integer, SupplyCrateItemObj> indexToCustomMap;
    private List<Integer> costList;

    private static RangeMap<Integer, SupplyCrateDrawCfg> rangeMap = TreeRangeMap.create();

    public SupplyCrateDrawCfg(){
        this.id = 0;
        this.custom = "";
        this.normal = "";
        this.range = "";
        this.doubleWeight = 1000;
        this.useItemNum = "";
    }

    public static boolean doAssemble() {
        RangeMap<Integer, SupplyCrateDrawCfg> tmpRangeMap = TreeRangeMap.create();
        ConfigIterator<SupplyCrateDrawCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(SupplyCrateDrawCfg.class);
        for(SupplyCrateDrawCfg cfg : iterator){
            tmpRangeMap.put(Range.closed(cfg.minRound, cfg.maxRound), cfg);
        }
        rangeMap = tmpRangeMap;
        return true;
    }

    @Override
    protected boolean assemble() {
        String[] split = range.split("_");
        minRound = Integer.parseInt(split[0]);
        maxRound = Integer.parseInt(split[1]);
        split = normal.split(",");
        List<SupplyCrateItemObj> normalTmpList = new ArrayList<>();
        for(String normalItem : split){
            normalTmpList.add(SupplyCrateItemObj.crateNormalItem(normalItem));
        }
        normalList = normalTmpList;
        split = custom.split(",");
        Map<Integer, SupplyCrateItemObj> indexTmpMap = new HashMap<>();
        int index = 0;
        for(String customItem : split){
            index++;
            indexTmpMap.put(index, SupplyCrateItemObj.crateCustomItem(customItem));
        }
        indexToCustomMap = indexTmpMap;
        List<Integer> tmpCostList = SerializeHelper.stringToList(Integer.class, useItemNum, SerializeHelper.ATTRIBUTE_SPLIT);
        costList = tmpCostList;
        return true;
    }

    public int getId(){
        return this.id;
    }

    public String getCustom(){
        return this.custom;
    }

    public String getNormal(){
        return this.normal;
    }

    public String getRange(){
        return this.range;
    }

    public String getUseItemNum(){
        return this.useItemNum;
    }

    public List<SupplyCrateItemObj> getDrawList(int customIndex){
        List<SupplyCrateItemObj> drawList = new ArrayList<>();
        for(SupplyCrateItemObj item : normalList){
            drawList.add(item.clone());
        }
        drawList.add(indexToCustomMap.get(customIndex).clone());
        drawList.add(SupplyCrateItemObj.crateDoubleItem(doubleWeight));
        return drawList;
    }

    public SupplyCrateItemObj getCoustmItem(int customIndex){
        return indexToCustomMap.get(customIndex);
    }


    public static SupplyCrateDrawCfg getCfgByRound(int round){
        return rangeMap.get(round);
    }

    public int getCostCount(int index){
        if(index < 0 || index >= costList.size()){
            return 0;
        }
        return costList.get(index);
    }
}