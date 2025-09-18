package com.hawk.gamelog;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * tencent接入日志信息
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "cfg/logTable.xml")
public class LogTableCfg extends HawkConfigBase {
	@Id
	protected final String logType;
	
	protected final String tableName;
	
	protected final String attribute;
	
	protected List<String> attrList;
	
	private static Set<String> logTypeSet = new HashSet<String>();
	
	public LogTableCfg() {
		logType = "";
		tableName = "";
		attribute = "";
	}

	public List<String> getAttrList() {
		return attrList;
	}

	public void setAttrList(List<String> attrList) {
		this.attrList = attrList;
	}

	public String getLogType() {
		return logType;
	}

	public String getTableName() {
		return tableName;
	}

	public String getAttribute() {
		return attribute;
	}

	@Override
	protected boolean assemble() {
		if(HawkOSOperator.isEmptyString(attribute)) {
			return false;
		}
		attrList = new ArrayList<String>();
		String[] attrArray = attribute.split(",");
		for(String attr : attrArray) {
			attrList.add(attr.trim());
		}
		
		logTypeSet.add(logType);
		
		return true;
	}
	
	/**
	 * 判断日志类型是否存在
	 * 
	 * @param logType
	 * @return
	 */
	public static boolean isExist(String logType) {
		return logTypeSet.contains(logType);
	}
}
