package com.hawk.serialize.string;

public class DataArray {
	
	private String[] datas;
	
	private int index = 0;
	
	private int size;

	public DataArray(String[] datas) {
		this.datas = datas;
		this.size = datas.length;
	}

	public int getInt() {
		if (index >= size) {
			return 0;
		}
		if (index >= datas.length) {
			return 0;
		}
		String value = datas[index++];
		if ("".equals(value)) {
			return 0;
		}
		return Integer.valueOf(value);
	}
	
	public long getLong() {
		if (index >= size) {
			return 0;
		}
		if (index >= datas.length) {
			return 0;
		}
		String value = datas[index++];
		if ("".equals(value)) {
			return 0;
		}
		return Long.valueOf(value);
	}

	public String getString() {
		if (index >=  size) {
			return "";
		}
		if (index >= datas.length) {
			return "";
		}
		String value = datas[index++];
		return value;
	}

	public void setSize(int size) {
		this.size = size;
	}

}
