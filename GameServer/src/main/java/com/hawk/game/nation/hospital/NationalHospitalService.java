package com.hawk.game.nation.hospital;

/**
 * 国家医院服务类
 * 
 * @author lating
 *
 */
public class NationalHospitalService {

	private static final NationalHospitalService instance = new NationalHospitalService();
	
	private NationalHospitalService() {
	}
	
	public static NationalHospitalService getInstance() {
		return instance;
	}

}
