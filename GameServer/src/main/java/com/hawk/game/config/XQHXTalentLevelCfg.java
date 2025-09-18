package com.hawk.game.config;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.hawk.game.protocol.Const;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkOSOperator;

import java.util.HashMap;
import java.util.Map;

@HawkConfigManager.XmlResource(file = "xml/xqhx_talent_level.xml")
public class XQHXTalentLevelCfg extends HawkConfigBase {
    @Id
    private final int id;
    private final int level;
    private final int talentId;
    private final String effect;
    private final int point;

    private Map<Const.EffType, Integer> effectMap = new HashMap<>();

    private static Table<Integer, Integer, XQHXTalentLevelCfg> levelTable = HashBasedTable.create();

    public XQHXTalentLevelCfg(){
        this.id = 0;
        this.level = 0;
        this.talentId = 0;
        this.effect = "";
        this.point = 0;
    }

    @Override
    protected boolean assemble() {
        Map<Const.EffType, Integer> effectMapTmp = new HashMap<>();
        if (!HawkOSOperator.isEmptyString(effect)) {
            String[] array = effect.split(",");
            for (String val : array) {
                String[] info = val.split("_");
                effectMapTmp.put(Const.EffType.valueOf(Integer.parseInt(info[0])), Integer.parseInt(info[1]));
            }
        }

        this.effectMap = ImmutableMap.copyOf(effectMapTmp);
        return true;
    }

    public static boolean doAssemble() {
        Table<Integer, Integer, XQHXTalentLevelCfg> levelTableTmp = HashBasedTable.create();
        ConfigIterator<XQHXTalentLevelCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(XQHXTalentLevelCfg.class);
        for(XQHXTalentLevelCfg cfg : iterator){
            levelTableTmp.put(cfg.getTalentId(), cfg.getLevel(), cfg);
        }
        levelTable = ImmutableTable.copyOf(levelTableTmp);
        return true;
    }

    public int getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public int getTalentId() {
        return talentId;
    }

    public String getEffect() {
        return effect;
    }

    public int getPoint() {
        return point;
    }

    public Map<Const.EffType, Integer> getEffectMap() {
        return effectMap;
    }

    public static XQHXTalentLevelCfg getCfgByTalentIdAndLevel(int talentId, int level){
        return levelTable.get(talentId, level);
    }
}
