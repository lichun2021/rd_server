package com.hawk.activity.type.impl.developFast.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 实力飞升礼包配置
 */
@HawkConfigManager.XmlResource(file = "activity/develop_fast/develop_fast_authority.xml")
public class DevelopFastAuthorityCfg extends HawkConfigBase {
    /** id*/
    @Id
    private final int id;
    /** 养成线类型*/
    private final int category;
    /** buffId*/
    private final int buffId;
    /** 安卓礼包id*/
    private final int android;
    /** ios礼包id*/
    private final int ios;

    /**
     * 构造函数
     */
    public DevelopFastAuthorityCfg(){
        id = 0;
        category = 0;
        buffId = 0;
        android = 0;
        ios = 0;
    }

    /**
     * 获得配置id
     * @return 配置id
     */
    public int getId() {
        return id;
    }

    /**
     * 获得养成线类型
     * @return 养成线类型
     */
    public int getCategory() {
        return category;
    }

    /**
     * 获得buffid
     * @return buffid
     */
    public int getBuffId() {
        return buffId;
    }

    /**
     * 获得安卓礼包id
     * @return 安卓礼包id
     */
    public int getAndroid() {
        return android;
    }

    /**
     * 获得ios礼包id
     * @return ios礼包id
     */
    public int getIos() {
        return ios;
    }
}
