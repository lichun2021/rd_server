package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRand;

import com.hawk.game.cfgElement.RefreshAreaObject;
import com.hawk.game.protocol.Const.ResourceZone;

/**
 * 世界资源带对应资源等级配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/world_resource_area.xml")
public class WorldResourceAreaCfg extends HawkConfigBase {
	@Id
	protected final int id;
	
	protected final int openServiceTimeLowerLimit;
	protected final int openServiceTimeUpLimit;
	
	// 资源等级_权重,资源等级_权重
	protected final String resArea1;
	protected final String resArea2;
	protected final String resArea3;
	protected final String resArea4;
	protected final String resArea5;
	protected final String resArea6;
	protected final String blackArea;

	public List<RefreshAreaObject> resArray1;
	public List<RefreshAreaObject> resArray2;
	public List<RefreshAreaObject> resArray3;
	public List<RefreshAreaObject> resArray4;
	public List<RefreshAreaObject> resArray5;
	public List<RefreshAreaObject> resArray6;
	public List<RefreshAreaObject> blackArray;
	
	public WorldResourceAreaCfg() {
		id = 0;
		openServiceTimeLowerLimit = 0;
		openServiceTimeUpLimit = 0;
		resArea1 = null;
		resArea2 = null;
		resArea3 = null;
		resArea4 = null;
		resArea5 = null;
		resArea6 = null;
		blackArea = null;
	}

	public int getId() {
		return id;
	}

	public int getOpenServiceTimeLowerLimit() {
		return openServiceTimeLowerLimit;
	}

	public int getOpenServiceTimeUpLimit() {
		return openServiceTimeUpLimit;
	}

	public String getResArea1() {
		return resArea1;
	}

	public String getResArea2() {
		return resArea2;
	}

	public String getResArea3() {
		return resArea3;
	}

	public String getResArea4() {
		return resArea4;
	}

	public String getResArea5() {
		return resArea5;
	}

	public String getResArea6() {
		return resArea6;
	}

	public String getBlackArea() {
		return blackArea;
	}
	
	@Override
	protected boolean assemble() {
		// 第一资源带
		if (resArea1 != null && !"".equals(resArea1)) {
			String[] area = resArea1.trim().split(",");
			resArray1 = new ArrayList<RefreshAreaObject>();
			for (int i = 0; i < area.length; i++) {
				String[] keyWeight = area[i].split("_");
				RefreshAreaObject obj = new RefreshAreaObject(Integer.parseInt(keyWeight[0]), Integer.parseInt(keyWeight[1]));
				resArray1.add(obj);
			}
		}

		// 第2资源带
		if (resArea2 != null && !"".equals(resArea2)) {
			String[] area = resArea2.split(",");
			resArray2 = new ArrayList<RefreshAreaObject>();
			for (int i = 0; i < area.length; i++) {
				String[] keyWeight = area[i].split("_");
				RefreshAreaObject obj = new RefreshAreaObject(Integer.parseInt(keyWeight[0]), Integer.parseInt(keyWeight[1]));
				resArray2.add(obj);
			}
		}

		// 第3资源带
		if (resArea3 != null && !"".equals(resArea3)) {
			String[] area = resArea3.split(",");
			resArray3 = new ArrayList<RefreshAreaObject>();
			for (int i = 0; i < area.length; i++) {
				String[] keyWeight = area[i].split("_");
				RefreshAreaObject obj = new RefreshAreaObject(Integer.parseInt(keyWeight[0]), Integer.parseInt(keyWeight[1]));
				resArray3.add(obj);
			}
		}

		// 第4资源带
		if (resArea4 != null && !"".equals(resArea4)) {
			String[] area = resArea4.split(",");
			resArray4 = new ArrayList<RefreshAreaObject>();
			for (int i = 0; i < area.length; i++) {
				String[] keyWeight = area[i].split("_");
				RefreshAreaObject obj = new RefreshAreaObject(Integer.parseInt(keyWeight[0]), Integer.parseInt(keyWeight[1]));
				resArray4.add(obj);
			}
		}

		// 第5资源带
		if (resArea5 != null && !"".equals(resArea5)) {
			String[] area = resArea5.split(",");
			resArray5 = new ArrayList<RefreshAreaObject>();
			for (int i = 0; i < area.length; i++) {
				String[] keyWeight = area[i].split("_");
				RefreshAreaObject obj = new RefreshAreaObject(Integer.parseInt(keyWeight[0]), Integer.parseInt(keyWeight[1]));
				resArray5.add(obj);
			}
		}

		// 第6资源带
		if (resArea6 != null && !"".equals(resArea6)) {
			String[] area = resArea6.split(",");
			resArray6 = new ArrayList<RefreshAreaObject>();
			for (int i = 0; i < area.length; i++) {
				String[] keyWeight = area[i].split("_");
				RefreshAreaObject obj = new RefreshAreaObject(Integer.parseInt(keyWeight[0]), Integer.parseInt(keyWeight[1]));
				resArray6.add(obj);
			}
		}
		
		//黑土地资源带
		if(blackArea != null && !"".equals(blackArea)){
			String[] area = blackArea.split(",");
			blackArray = new ArrayList<RefreshAreaObject>();
			for (int i = 0; i < area.length; i++) {
				String[] keyWeight = area[i].split("_");
				RefreshAreaObject obj = new RefreshAreaObject(Integer.parseInt(keyWeight[0]), Integer.parseInt(keyWeight[1]));
				blackArray.add(obj);
			}
		}

		return true;
	}
	
	/**
	 * 随机资源权重
	 * 
	 * @param zone
	 * @return
	 */
	public int randomResourceLevel(int zone) {
		List<RefreshAreaObject> resArray = resArray3;
		switch (zone) {
		case ResourceZone.ZONE_1_VALUE:
			resArray = resArray1;
			break;
		case ResourceZone.ZONE_2_VALUE:
			resArray = resArray2;
			break;
		case ResourceZone.ZONE_3_VALUE:
			resArray = resArray3;
			break;
		case ResourceZone.ZONE_4_VALUE:
			resArray = resArray4;
			break;
		case ResourceZone.ZONE_5_VALUE:
			resArray = resArray5;
			break;
		case ResourceZone.ZONE_6_VALUE:
			resArray = resArray6;
			break;
		case ResourceZone.ZONE_BLACK_VALUE:
			resArray = blackArray;
			break;
		}
		List<Integer> weights = new ArrayList<Integer>();
		for (int i = 0; i < resArray.size(); i++) {
			weights.add(resArray.get(i).getWeight());
		}
		RefreshAreaObject resLevel = HawkRand.randomWeightObject(resArray, weights);
		return resLevel.getKey();
	}
}
