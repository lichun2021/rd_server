package com.hawk.activity.type.impl.guildBack.cfg;

import com.google.common.collect.ImmutableMap;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import java.util.HashMap;
import java.util.Map;

/**
* 本文件自动生成，重新生成会被覆盖，请手动保留自定义部分
*/
@HawkConfigManager.XmlResource(file = "activity/guild_back/guild_back_buff.xml")
public class GuildBackBuffCfg extends HawkConfigBase{
    @Id
    private final int id;
    private final int count;
    private final String effect;

    private Map<Integer, Integer> effMap = new HashMap<>();

    public GuildBackBuffCfg(){
        this.id = 0;
        this.count = 0;
        this.effect = "";
    }

    @Override
    protected boolean assemble() {
        Map<Integer, Integer> tmp = new HashMap<>();
        String[] split = effect.split(SerializeHelper.BETWEEN_ITEMS);
        for (String s: split) {
            String[] split1 = s.split(SerializeHelper.ATTRIBUTE_SPLIT);
            tmp.put(Integer.parseInt(split1[0]), Integer.parseInt(split1[1]));
        }
        effMap = ImmutableMap.copyOf(tmp);
        return true;
    }

    public int getId(){
        return this.id;
    }

    public int getCount(){
        return this.count;
    }

    public String getEffect(){
        return this.effect;
    }

    public Map<Integer, Integer> getEffMap() {
        return effMap;
    }
}