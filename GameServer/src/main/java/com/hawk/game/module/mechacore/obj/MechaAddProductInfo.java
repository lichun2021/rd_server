package com.hawk.game.module.mechacore.obj;

import java.util.StringJoiner;

import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

/**
 * 机甲核心模块增产信息
 * 
 * @author lating
 */
public class MechaAddProductInfo {

	private int productAddCount;
	
	private long useItemTime;
	
	private int useItemId;
	
	private long loadTime;
	
	private int useCountDaily;

	public int getProductAddCount() {
		return productAddCount;
	}

	public void setProductAddCount(int productAddCount) {
		this.productAddCount = productAddCount;
	}
	
	/**
	 * count大于0增加，小于0是减少
	 * @param count
	 */
	public void updateProductAddCount(int count) {
		productAddCount += count;
		productAddCount = Math.max(productAddCount, 0);
	}
	
	public long getUseItemTime() {
		return useItemTime;
	}

	public void setUseItemTime(long useItemTime) {
		this.useItemTime = useItemTime;
	}

	public int getUseItemId() {
		return useItemId;
	}

	public void setUseItemId(int useItemId) {
		this.useItemId = useItemId;
	}

	public long getLoadTime() {
		return loadTime;
	}

	public void setLoadTime(long loadTime) {
		this.loadTime = loadTime;
	}
	
	public int getUseCountDaily() {
		return useCountDaily;
	}

	public void setUseCountDaily(int useCountDaily) {
		this.useCountDaily = useCountDaily;
	}
	
	public void incUseCountDaily(int add) {
		this.useCountDaily += add;
	}
	
	public String toString() {
		StringJoiner sj = new StringJoiner("|");
		sj.add(productAddCount+"").add(useItemTime+"").add(useItemId+"").add(useCountDaily+"");
		return sj.toString();
	}
	
	public static MechaAddProductInfo toObject(String str) {
		MechaAddProductInfo obj = new MechaAddProductInfo();
		obj.setLoadTime(HawkTime.getMillisecond());
		if (HawkOSOperator.isEmptyString(str)) {
			return obj;
		}
		
		String[] infos = str.split("\\|");
		obj.setProductAddCount(Integer.parseInt(infos[0]));
		obj.setUseItemTime(Long.parseLong(infos[1]));
		obj.setUseItemId(Integer.parseInt(infos[2]));
		obj.setUseCountDaily(Integer.parseInt(infos[3]));
		return obj;
	}
}
