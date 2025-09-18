package com.hawk.game.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import org.hawk.os.HawkException;

/**
 * mysql表描述
 */
public class TableDescript {
	/**
	 * 字段名
	 */
	private String columeName;
	/**
	 * 类型
	 */
	private int type;
	/**
	 * 类型名
	 */
	private String typeName;
	/**
	 * 是否允许为空
	 */
	private boolean allowNull;
	/**
	 * 默认值
	 */
	private String defFaultValue;
	/**
	 * excel的中的所引值
	 */
	private int index;
	/**
	 * 备注
	 */
	private String Commonts;
	/**
	 * 长度
	 */
	private int length;
	/**
	 * 字符串的复合类型
	 */
	private String specialType;
	/**
	 * 是否是主键
	 */
	private boolean isPrimaryKey;
	/**
	 * 是否是唯一
	 */
	private boolean isUnique;
	/**
	 * 复合类型参数 主要用于数组
	 */
	private List<String> specialParam = new ArrayList<String>();

	public String getColumeName() {
		return columeName;
	}

	public void setColumeName(String columeName) {
		this.columeName = columeName;
	}

	public int getType() {
		return type;
	}

	public boolean isAllowNull() {
		return allowNull;
	}

	public String getDefFaultValue() {
		return defFaultValue;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setAllowNull(boolean allowNull) {
		this.allowNull = allowNull;
	}

	public void setDefFaultValue(String defFaultValue) {
		this.defFaultValue = defFaultValue;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getSpecialType() {
		return specialType;
	}

	public void setSpecialType(String specialType) {
		this.specialType = specialType;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public boolean isPrimaryKey() {
		return isPrimaryKey;
	}

	public void setPrimaryKey(boolean isPrimaryKey) {
		this.isPrimaryKey = isPrimaryKey;
	}

	public boolean isUnique() {
		return isUnique;
	}

	public void setUnique(boolean isUnique) {
		this.isUnique = isUnique;
	}

	public List<String> getSpecialParam() {
		return specialParam;
	}

	public void setSpecialParam(List<String> specialParam) {
		this.specialParam = specialParam;
	}

	public String getCommonts() {
		return Commonts;
	}

	public void setCommonts(String commonts) {
		if (commonts == null) {
			commonts = "";
		}
		String str[] = commonts.split("\\|");
		if (str.length > 1) {
			String param[] = str[0].split(",");

			for (int i = 0; i < param.length; i++) {
				if (i == 0) {
					specialType = param[0];
					continue;
				}

				specialParam.add(param[i]);
			}
			Commonts = str[1];
		} else if (str.length == 1 && commonts.indexOf("|") > 0) {
			String param[] = str[0].split(",");

			for (int i = 0; i < param.length; i++) {
				if (i == 0) {
					specialType = param[0];
					continue;
				}

				specialParam.add(param[i]);
			}
			commonts = "";
		} else {
			specialType = "";
			Commonts = commonts;
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		BeanInfo bi;
		try {
			bi = Introspector.getBeanInfo(this.getClass(), Object.class);
		} catch (IntrospectionException e) {
			HawkException.catchException(e);
			return "";
		}

		boolean flag = false;
		for (PropertyDescriptor pd : bi.getPropertyDescriptors()) {
			if (flag) {
				sb.append("  ");
			}

			try {
				sb.append(pd.getName() + "=" + pd.getReadMethod().invoke(this));
				flag = true;
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		sb.append("\n");
		return sb.toString();
	}
}
