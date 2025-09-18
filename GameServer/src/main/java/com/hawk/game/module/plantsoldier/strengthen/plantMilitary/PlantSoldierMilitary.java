package com.hawk.game.module.plantsoldier.strengthen.plantMilitary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.hawk.game.module.plantsoldier.strengthen.plantMilitary.cfg.PlantSoldierMilitaryCfgV3;
import com.hawk.game.module.plantsoldier.strengthen.plantMilitary.cfg.PlantSoldierMilitaryChipCfg;
import org.hawk.config.HawkConfigManager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.module.plantsoldier.strengthen.PlantSoldierSchool;
import com.hawk.game.module.plantsoldier.strengthen.plantMilitary.cfg.PlantSoldierMilitaryCfg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantMilitary;
import com.hawk.game.protocol.PlantSoldierSchool.PBPlantMilitaryChip;
import com.hawk.game.service.BuildingService;

/**
 * 泰能兵军衔升阶
 *
 * @author Golden
 */
public class PlantSoldierMilitary {
    com.hawk.game.protocol.PlantSoldierSchool.PBPlantMilitaryType type = com.hawk.game.protocol.PlantSoldierSchool.PBPlantMilitaryType.PLANT_SOLDIER_MILITARY_2;
    /**
     * 是否已解锁
     */
    private boolean unlock;

    /**
     * 兵种类型ID
     */
    private int soldierType;

    /**
     * 部件
     */
    private ImmutableList<PlantSoldierMilitaryChip> chips;

    // ----------------------------------------------------------------------------------------//

    private PlantSoldierSchool parent;

    /**
     * 作用号是已加载
     */
    private boolean efvalLoad;

    /**
     * 做用号
     */
    private ImmutableMap<EffType, Integer> effValMap;

    /**
     * 战力
     */
    private int power;

    /**
     * 构造方法
     *
     * @param school
     */
    public PlantSoldierMilitary(PlantSoldierSchool school) {
        this.parent = school;
    }

    public SoldierType getSoldierType() {
        return SoldierType.valueOf(soldierType);
    }

    public void setSoldierType(int soldierType) {
        this.soldierType = soldierType;
    }

    public com.hawk.game.protocol.PlantSoldierSchool.PBPlantMilitaryType getType() {
        return type;
    }

    public void setType(com.hawk.game.protocol.PlantSoldierSchool.PBPlantMilitaryType type) {
        this.type = type;
    }

    public ImmutableList<PlantSoldierMilitaryChip> getChips() {
        return chips;
    }

    public PlantSoldierMilitaryChip getChip(int cfgId) {
        for (PlantSoldierMilitaryChip chip : chips) {
            if (chip.getCfgId() == cfgId) {
                return chip;
            }
        }
        return null;
    }

    public void setChips(ImmutableList<PlantSoldierMilitaryChip> chips) {
        this.chips = chips;
    }

    /**
     * 序列化
     *
     * @return
     */
    public String serializ() {
        JSONObject result = new JSONObject();
        result.put("unlock", unlock);
        result.put("soldierType", soldierType);
        JSONArray arr = new JSONArray();
        chips.stream().map(PlantSoldierMilitaryChip::serializ).forEach(arr::add);
        result.put("chips", arr);
        return result.toJSONString();
    }

    /**
     * 反序列化
     *
     * @param jsonstr
     */
    public void mergeFrom(String jsonstr) {
        JSONObject result = JSONObject.parseObject(jsonstr);
        this.soldierType = result.getIntValue("soldierType");
        this.unlock = result.getBooleanValue("unlock");
        List<PlantSoldierMilitaryChip> list = new ArrayList<>();
        JSONArray arr = result.getJSONArray("chips");
        arr.forEach(str -> {
            PlantSoldierMilitaryChip slot = new PlantSoldierMilitaryChip(this);
            slot.mergeFrom(str.toString());
            if(type == com.hawk.game.protocol.PlantSoldierSchool.PBPlantMilitaryType.PLANT_SOLDIER_MILITARY_2){
                int chipCfgId = slot.getCfgId();
                int level = chipCfgId % 100;
                if(chipCfgId > 600000 || level > 40){
                    int newchipCfgId = 500000 + chipCfgId % 100000;
                    if(level > 40){
                        newchipCfgId = newchipCfgId / 100 * 100 + 40;
                    }
                    slot.setCfgId(newchipCfgId);
                }
            }
            list.add(slot);
        });

        this.chips = ImmutableList.copyOf(list);
    }

    /**
     * 通知数据有变化
     */
    public void notifyChange() {
        efvalLoad = false;
        this.loadEffVal(); // 做号用变更,如删除技能
        parent.setChanged(true);
        if (!effValMap.isEmpty()) {
            Player player = getParent().getParent();
            player.getEffect().syncEffect(player, effValMap.keySet().toArray(new EffType[0]));
        }
    }

    public PlantSoldierSchool getParent() {
        return parent;
    }

    public int getEffVal(EffType eff) {
        if (!efvalLoad) {
            loadEffVal();
        }
        return effValMap.getOrDefault(eff, 0);
    }

    public boolean isUnlock() {
        return unlock;
    }

    public void setUnlock(boolean unlock) {
        this.unlock = unlock;
    }

    /**
     * 加载作用号(懒加载)
     */
    public void loadEffVal() {
        if (efvalLoad) {
            return;
        }
        Map<EffType, Integer> effmap = new HashMap<>();
        for (PlantSoldierMilitaryChip chip : chips) {
            for (Entry<EffType, Integer> ent : chip.getCfg().getEffectList().entrySet()) {
                effmap.merge(ent.getKey(), ent.getValue(), (v1, v2) -> v1 + v2);
            }
        }
        effValMap = ImmutableMap.copyOf(effmap);
        efvalLoad = true;
        this.power = power();
    }

    public int getPower() {
        return this.power;
    }

    private int power() {
        int result = 0;
        for (PlantSoldierMilitaryChip chip : chips) {
            result += chip.getCfg().getPower();
        }
        return result;
    }

    /**
     * 初始化部件
     */
    public void initChips(PlantSoldierMilitaryCfg cfg) {
        List<PlantSoldierMilitaryChip> list = new ArrayList<>();
        for (String chipId : cfg.getInitChips()) {
            PlantSoldierMilitaryChip slot = new PlantSoldierMilitaryChip(this);
            slot.setCfgId(Integer.parseInt(chipId));
            list.add(slot);
        }
        this.chips = ImmutableList.copyOf(list);
    }

    public PBPlantMilitary toPBobj() {
        PBPlantMilitary.Builder builder = PBPlantMilitary.newBuilder();
        builder.setSoldierType(getSoldierType());
        builder.setUnlock(unlock);
        for (PlantSoldierMilitaryChip chip : chips) {
            builder.addChips(PBPlantMilitaryChip.newBuilder().setCfgId(chip.getCfgId()));
        }
        return builder.build();
    }

    public PlantSoldierMilitaryCfg getCfg() {
        if(type == com.hawk.game.protocol.PlantSoldierSchool.PBPlantMilitaryType.PLANT_SOLDIER_MILITARY_3){
            return HawkConfigManager.getInstance().getConfigByKey(PlantSoldierMilitaryCfgV3.class, soldierType);
        }else {
            return HawkConfigManager.getInstance().getConfigByKey(PlantSoldierMilitaryCfg.class, soldierType);
        }

    }

    public boolean canUnlock() {
        // 判断前置建筑条件，即判断建筑是否已解锁
        if (!BuildingService.getInstance().checkFrontCondition(getParent().getParent(), getCfg().getFrontBuildIds(), null, 0)) {
            return false;
        }
        int strengthLevel = parent.getSoldierStrengthenByType(getSoldierType()).getPlantStrengthLevel();
        if (strengthLevel < getCfg().getFrontStrengthen()) {
            return false;
        }
        return true;
    }

    public int getMilitaryLevel() {
        int level = 0;
        for (PlantSoldierMilitaryChip chip : getChips()) {
            PlantSoldierMilitaryChipCfg nextCfg = chip.getNextCfg();
            if (nextCfg == null){
                level++;
            }
        }
        return level;
    }

}
