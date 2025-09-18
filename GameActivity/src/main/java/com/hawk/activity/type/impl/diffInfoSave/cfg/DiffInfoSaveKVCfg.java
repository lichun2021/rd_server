package com.hawk.activity.type.impl.diffInfoSave.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/save_gold/save_cfg.xml")
public class DiffInfoSaveKVCfg extends HawkConfigBase {
    /**
     * 起服延迟开放时间
     */
    private final int serverDelay;

    /**
     * 任务递增进度值
     */
    private final int incremental;

    private final int taskCount;
    private final long resetTime;
    public DiffInfoSaveKVCfg(){
        serverDelay = 0;
        incremental = 10;
        taskCount = 0;
        resetTime = 0;
    }

    public long getServerDelay() {
        return serverDelay * 1000l;
    }

    public int getIncremental() {
        return incremental;
    }

    public int getTaskCount() {
        return taskCount;
    }

	public long getResetTime() {
		return resetTime * 1000l;
	}
}
