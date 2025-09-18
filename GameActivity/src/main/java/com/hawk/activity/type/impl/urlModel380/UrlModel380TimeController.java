package com.hawk.activity.type.impl.urlModel380;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.urlModel380.cfg.UrlModel380KVCfg;
import com.hawk.activity.type.impl.urlModel380.cfg.UrlModel380TimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import java.util.Optional;

public class UrlModel380TimeController extends ExceptCurrentTermTimeController {

    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return UrlModel380TimeCfg.class;
    }

    @Override
    public long getServerDelay() {
        UrlModel380KVCfg cfg = HawkConfigManager.getInstance().getKVInstance(UrlModel380KVCfg.class);
        if (cfg != null) {
            return cfg.getServerDelay();
        }
        return 0;
    }

    @Override
    protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
        UrlModel380KVCfg cfg = HawkConfigManager.getInstance().getKVInstance(UrlModel380KVCfg.class);
        long openTime = HawkTime.parseTime(cfg.getServerOpenTime());
        long endTime = HawkTime.parseTime(cfg.getServerEndTime());
        long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
        if (openTime <= serverOpenTime && serverOpenTime <= endTime) {
            return super.getTimeCfg(now);
        }
        return Optional.empty();
    }
}
