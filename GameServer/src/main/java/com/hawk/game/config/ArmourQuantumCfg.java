package com.hawk.game.config;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 量子槽位属性表
 */
@HawkConfigManager.XmlResource(file = "xml/armour_quantum_attr.xml")
public class ArmourQuantumCfg extends HawkConfigBase {
    @Id
    protected final int id;

    /**
     * 装备id
     */
    protected final int armourId;

    /**
     * 等级
     */
    protected final int level;

    /**
     * 固定属性
     */
    protected final String normalAttr;

    /**
     * 特殊属性
     */
    protected final String specialAttr1;
    protected final String specialAttr2;
    protected final String specialAttr3;

    /**
     * 属性(作用号)
     */
    private List<EffectObject> quantumEff;

    public ArmourQuantumCfg(){
        id = 0;
        armourId = 0;
        level = 0;
        normalAttr = "";
        specialAttr1 = "";
        specialAttr2 = "";
        specialAttr3 = "";
    }

    @Override
    protected boolean assemble() {
        quantumEff = new ArrayList<>();
        List<EffectObject> normalEff = SerializeHelper.stringToList(EffectObject.class, normalAttr, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, new ArrayList<>());
        normalEff.forEach(e -> e.setShowType(1));
        List<EffectObject> specialEff1 = SerializeHelper.stringToList(EffectObject.class, specialAttr1, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, new ArrayList<>());
        specialEff1.forEach(e -> e.setShowType(2));
        List<EffectObject> specialEff2 = SerializeHelper.stringToList(EffectObject.class, specialAttr2, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, new ArrayList<>());
        specialEff2.forEach(e -> e.setShowType(2));
        List<EffectObject> specialEff3 = SerializeHelper.stringToList(EffectObject.class, specialAttr3, SerializeHelper.BETWEEN_ITEMS, SerializeHelper.ATTRIBUTE_SPLIT, new ArrayList<>());
        specialEff3.forEach(e -> e.setShowType(2));
        quantumEff.addAll(normalEff);
        quantumEff.addAll(specialEff1);
        quantumEff.addAll(specialEff2);
        quantumEff.addAll(specialEff3);
        return true;
    }

    public int getId() {
        return id;
    }

    public int getArmourId() {
        return armourId;
    }

    public int getLevel() {
        return level;
    }

    public List<EffectObject> getQuantumEff() {
        return new ArrayList<>(quantumEff);
    }
}
