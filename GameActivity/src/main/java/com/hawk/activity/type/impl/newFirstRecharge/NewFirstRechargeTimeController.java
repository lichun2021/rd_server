package com.hawk.activity.type.impl.newFirstRecharge;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.newFirstRecharge.cfg.NewFirstRechargeKVCfg;
import com.hawk.activity.type.impl.newFirstRecharge.cfg.NewFirstRechargeTimeCfg;

/**
 * 新首充时间管理器
 */
public class NewFirstRechargeTimeController extends JoinCurrentTermTimeController {
    /**
     * 获得时间配置
     * @return 时间配置
     */
    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return NewFirstRechargeTimeCfg.class;
    }

    /**
     * 新服延期时间
     * @return 新服延期时间
     */
    @Override
    public long getServerDelay() {
        //活动总配置
        NewFirstRechargeKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(NewFirstRechargeKVCfg.class);
        //如果不为空直接返回配置
        if (cfg != null) {
            return cfg.getServerDelay();
        }
        //为空就返回0
        return 0;
    }
    
	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		Optional<IActivityTimeCfg> tcfgop = super.getTimeCfg(now);
		if (tcfgop.isPresent() && now > getEndTimeByTermId(tcfgop.get().getTermId())) {
			return Optional.empty();
		}
		return tcfgop;
	}
	
	@Override
	public long getEndTimeByTermId(int termId, String playerId){
		return getEndTimeByTermId(termId);
	}
    
    @Override
	public long getEndTimeByTermId(int termId) {
		NewFirstRechargeKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(NewFirstRechargeKVCfg.class);
		if (cfg.getResetTime() > 0) {
			long time1 = getForerver2Limit45Start();
			return time1 + cfg.getResetTime();
		}
		return super.getEndTimeByTermId(termId);
	}
}
