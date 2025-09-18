package com.hawk.activity.type.impl.guildBack;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.guildBack.cfg.GuildBackKvCfg;
import com.hawk.activity.type.impl.guildBack.cfg.GuildBackTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

public class GuildBackTimeController extends ExceptCurrentTermTimeController {
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return GuildBackTimeCfg.class;
    }

    @Override
    public long getServerDelay() {
        GuildBackKvCfg cfg = HawkConfigManager.getInstance().getKVInstance(GuildBackKvCfg.class);
        if(cfg != null){
            return cfg.getServerDelay();
        }
        return 0;
    }
}
