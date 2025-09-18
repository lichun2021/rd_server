package com.hawk.activity.type.impl.heavenBlessing.cfg;

import com.google.common.collect.ImmutableList;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import java.util.ArrayList;
import java.util.List;

@HawkConfigManager.XmlResource(file = "activity/heaven_blessing/heaven_blessing_group_cfg.xml")
public class HeavenBlessingGroupCfg extends HawkConfigBase {

    /**
     * 唯一ID
     */
    @Id
    private final int id;
    /**
     * 档位配置
     */
    private final String levels;
    /**
     * 档位列表
     */
    private List<Integer> levelList;

    public HeavenBlessingGroupCfg(){
        id = 0;
        levels = "";
    }

    @Override
    protected boolean assemble() {
        List<Integer> tmp = new ArrayList<>();
        if(!HawkOSOperator.isEmptyString(this.levels)){
            String arr[] = this.levels.split("_");
            for(String str : arr){
                int num = Integer.parseInt(str);
                tmp.add(num);
            }
        }
        this.levelList = ImmutableList.copyOf(tmp);
        return true;
    }

    public List<Integer> getLevelList() {
        return levelList;
    }
}
