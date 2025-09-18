package com.hawk.activity.type.impl.urlModel381;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.urlModel381.cfg.UrlModel381KVCfg;
import com.hawk.activity.type.impl.urlModel381.cfg.UrlModel381TimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import java.util.Optional;

public class UrlModel381TimeController extends ExceptCurrentTermTimeController {

    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return UrlModel381TimeCfg.class;
    }

    @Override
    public long getServerDelay() {
        UrlModel381KVCfg cfg = HawkConfigManager.getInstance().getKVInstance(UrlModel381KVCfg.class);
        if (cfg != null) {
            return cfg.getServerDelay();
        }
        return 0;
    }

    @Override
    protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
        UrlModel381KVCfg cfg = HawkConfigManager.getInstance().getKVInstance(UrlModel381KVCfg.class);
        long openTime = HawkTime.parseTime(cfg.getServerOpenTime());
        long endTime = HawkTime.parseTime(cfg.getServerEndTime());
        long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
        if (openTime <= serverOpenTime && serverOpenTime <= endTime) {
            return super.getTimeCfg(now);
        }
        return Optional.empty();
    }
}
