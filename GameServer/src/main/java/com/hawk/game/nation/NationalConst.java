package com.hawk.game.nation;

public class NationalConst {

	/**
	 * 存储国家医院信息的redisKey
	 */
	public static final String NATION_HOSPITAL_SETTING = "nation_hospital_setting";
	public static final String TSZZ_NATION_HOSPITAL_SETTING = "tszz_nation_hospital_setting";
	/**
	 * 国家仓库资源数量redisKey
	 */
	public static final String NATIONAL_WAREHOUSE_RESOURCE = "national_warehouse_resource";
	/**
	 * 国家仓库资源捐献记录存储redisKey
	 */
	public static final String NATIONAL_WAREHOUSE_DONATE = "national_warehouse_donate";
	/**
	 * 个人在国家商店购买数据存储redisKey
	 */
	public static final String NATIONAL_WAREHOUSE_SHOP = "national_warehouse_shop";
	/**
	 * 国家建筑资助金条次数
	 */
	public static final String NATION_BUILD_SUPPORT = "nation_build_support_times";
	/**
	 * 国家医院普通死兵
	 */
	public static final int NATION_HOSPITAL_SOLDIER = 0;
	/**
	 * 国家医院统帅之战死兵
	 */
	public static final int NATION_HOSPITAL_TSZZ_SOLDIER = 1;
	
	/**
	 * 国家库存金条的变动记录类型
	 */
	public static enum NationalDiamondRecordType {
		DONATE(0),   // 资助增加
		CONSUME(1);  // 支出减少
		
		int type;
		
		NationalDiamondRecordType(int type) {
			this.type = type;
		}
		
		public int intVal() {
			return type;
		}
	}
	
	
	public static String formatTime(Long ms) {
	    Integer ss = 1000;
	    Integer mi = ss * 60;
	    Integer hh = mi * 60;
	    Integer dd = hh * 24;

	    Long day = ms / dd;
	    Long hour = (ms - day * dd) / hh;
	    Long minute = (ms - day * dd - hour * hh) / mi;
	    Long second = (ms - day * dd - hour * hh - minute * mi) / ss;
	    
	    StringBuffer sb = new StringBuffer();
	    if(day > 0){
	    	 sb.append(day).append("d").append(" ");
	    }
	    sb.append(convertTimeStr(hour)).append(":").append(convertTimeStr(minute)).append(":").append(convertTimeStr(second));
	   
	    return sb.toString();
	}

	public static String convertTimeStr(long t){
		if(t < 10) {
			return "0" + t;
		}
		return "" + t;
	}
	
}
