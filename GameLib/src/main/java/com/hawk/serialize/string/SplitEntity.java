package com.hawk.serialize.string;

import java.util.List;

public interface SplitEntity {

	SplitEntity newInstance();

	void serializeData(List<Object> dataList);

	void fullData(DataArray dataArray);

}
