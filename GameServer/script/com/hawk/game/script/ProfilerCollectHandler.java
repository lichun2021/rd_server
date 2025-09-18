package com.hawk.game.script;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.FileSystemUsage;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.NetFlags;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.CentralProcessor.TickType;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HWPartition;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.hardware.NetworkIF;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;

/**
 * 系统、硬件信息采集
 * 
 * localhost:8080/script/profiler
 *
 * @author hawk
 */
public class ProfilerCollectHandler extends HawkScript {
	
	private static final Logger logger = LoggerFactory.getLogger("Profile");
	
	static DecimalFormat decimalFormat = new DecimalFormat("0.00");

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
	    SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();

        // 内存信息
        printMemory(hal.getMemory());
        // cpu信息
        printCpu(hal.getProcessor());
        // 磁盘
        //printDisks(hal.getDiskStores());
        // 文件系统
        //printFileSystem(os.getFileSystem());
        // 综合磁盘、文件系统信息
        doDiskMonitor(hal.getDiskStores(), os.getFileSystem());
        // 网络
        printNetworkInterfaces(hal.getNetworkIFs());
        //
        //printSigar();
        
		return HawkScript.successResponse("succ");
	}
	
	/**
	 * 内存信息
	 * @param memory
	 */
    private void printMemory(GlobalMemory memory) {
    	logger.info("");
    	logger.info("------------------ memory --------------------");
    	String total = decimalFormat.format(memory.getTotal() * 1D / 1024 / 1024 / 1024) + "G";
    	String free = decimalFormat.format(memory.getAvailable() * 1D / 1024 / 1024 / 1024) + "G";
    	String used = decimalFormat.format((memory.getTotal() - memory.getAvailable()) * 1D / 1024 / 1024 / 1024) + "G";
    	logger.info("memory total: {}, free: {}, used: {}", total, free, used);
    }

    /**
     * cpu信息
     * @param processor
     */
    private void printCpu(CentralProcessor processor) {
    	logger.info("");
    	logger.info("------------------ cpu --------------------");
    	long[][] prePCLInfo = processor.getProcessorCpuLoadTicks();
    	double[] result = processor.getProcessorCpuLoad(300);
    	long[][] afterPCLInfo = processor.getProcessorCpuLoadTicks();
    	logger.info("cpu cores, getProcessorCpuLoad: {}, Logical count: {}, Physical count: {}", result.length, processor.getLogicalProcessorCount(), processor.getPhysicalProcessorCount());
    	for (int i = 0; i < result.length; i++) {
    		logger.info("cpu{}: {}", i+1, result[i] * 100);
    	}
    	
    	logger.info("");
    	for (int i = 0; i < afterPCLInfo.length; i++) {
    		long[] pre = prePCLInfo[i];
    		long[] after = afterPCLInfo[i];
    		long user = after[TickType.USER.getIndex()] - pre[TickType.USER.getIndex()];
            long nice = after[TickType.NICE.getIndex()] - pre[TickType.NICE.getIndex()];
            long sys = after[TickType.SYSTEM.getIndex()] - pre[TickType.SYSTEM.getIndex()];
            long idle = after[TickType.IDLE.getIndex()] - pre[TickType.IDLE.getIndex()];
            long iowait = after[TickType.IOWAIT.getIndex()] - pre[TickType.IOWAIT.getIndex()];
            long irq = after[TickType.IRQ.getIndex()] - pre[TickType.IRQ.getIndex()];
            long softirq = after[TickType.SOFTIRQ.getIndex()] - pre[TickType.SOFTIRQ.getIndex()];
            long steal = after[TickType.STEAL.getIndex()] - pre[TickType.STEAL.getIndex()];
            long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;
            double userDouble = 100d * user / totalCpu;
            double sysDouble = 100d * sys / totalCpu;
            double idleDouble = 100d * idle / totalCpu;
            logger.info("cpu{}, occupy: {}, sys: {}, user: {}, idle: {}", i+1, sysDouble + userDouble, sysDouble, userDouble, idleDouble);
    	}
    	
    	logger.info("");
    	logger.info("sigar cpu: ");
    	Sigar sigar = null;
    	try {
    		sigar = new Sigar();
    		CpuPerc[] cpus = sigar.getCpuPercList();
    		for (int i = 0; cpus != null && i < cpus.length; i ++) {
    			logger.info("cpu{}, occupy: {}, sys: {}, user: {}, idle: {}", (i+1), cpus[i].getCombined() * 100, cpus[i].getSys() * 100, cpus[i].getUser() * 100, cpus[i].getIdle() * 100);
    		}
    	} catch (Exception e) {
    		HawkException.catchException(e);
    	} finally {
    		if (sigar != null) {
 				sigar.close();
 				sigar = null;
 			}
    	}
    }
    
    /**
     * 磁盘、文件系统综合信息
     * 
     * @param list
     * @param fileSystem
     */
    private void doDiskMonitor(List<HWDiskStore> list, FileSystem fileSystem) {
    	logger.info("");
     	logger.info("------------------ doDiskMonitor --------------------");
     	
     	 Map<String, JSONObject> fileSysInfoMap = new HashMap<>();
     	 for (OSFileStore fs : fileSystem.getFileStores()) {
         	JSONObject json = new JSONObject();
         	json.put("name", fs.getName());
         	json.put("mount", fs.getMount());
         	json.put("totalSpace", fs.getTotalSpace());
         	json.put("freeSpace", fs.getFreeSpace());
         	json.put("usableSpace", fs.getUsableSpace());
         	json.put("description", fs.getDescription());
         	json.put("type", fs.getType());
         	json.put("freeInodes", fs.getFreeInodes());
         	json.put("totalInodes", fs.getTotalInodes());
         	json.put("volume", fs.getVolume());
         	json.put("logicalVolume", fs.getLogicalVolume());
         	fileSysInfoMap.put(HawkOSOperator.isEmptyString(fs.getVolume()) ? ("volume-" + fs.getName()) : fs.getVolume(), json);
         }
     	 
     	Map<String, JSONObject> diskInfoMap = new HashMap<>();
     	for (HWDiskStore disk : list) {
     		List<HWPartition> partitions = disk.getPartitions();
     		if (partitions.isEmpty()) {
     			JSONObject json = new JSONObject();
         		json.put("name", disk.getName());
         		json.put("readBytes", disk.getReadBytes());
         		json.put("writeBytes", disk.getWriteBytes());
         		json.put("reads", disk.getReads());
         		json.put("writes", disk.getWrites());
         		json.put("model", disk.getModel());
     			diskInfoMap.put(disk.getName(), json);
     		} else {
     			 for (HWPartition part : partitions) {
     				JSONObject json = new JSONObject();
     	     		json.put("name", disk.getName());
     	     		json.put("readBytes", disk.getReadBytes());
     	     		json.put("writeBytes", disk.getWriteBytes());
     	     		json.put("reads", disk.getReads());
     	     		json.put("writes", disk.getWrites());
     	     		json.put("model", disk.getModel());
     	     		
     	     		json.put("partition", part.getName());
     	     		json.put("identification", part.getIdentification());
     	     		json.put("mountPoint", part.getMountPoint());
     	     		json.put("type", part.getType());
     	     		json.put("size", part.getSize());
     				diskInfoMap.put(part.getIdentification(), json);
                 }
     		}
        }
     	
     	for (Entry<String, JSONObject> entry : fileSysInfoMap.entrySet()) {
     		JSONObject diskInfo = diskInfoMap.get(entry.getKey());
     		if (diskInfo == null) {
     			continue;
     		}
     		
     		JSONObject fileInfo = entry.getValue();
     		long totalSpace = fileInfo.getLongValue("totalSpace");
     		long freeSpace = fileInfo.getLongValue("freeSpace");
     		double usePercent = (totalSpace - freeSpace) * 1D / totalSpace;
     		logger.info("type: {}, name: {}, mountDir: {}, diskTotal: {}, diskFree: {}, diskUsed: {}, readBytes: {}, writeBytes: {}, usePercent: {}", 
     				fileInfo.getString("type"), entry.getKey(), fileInfo.getString("mount"), totalSpace/1024, freeSpace/1024, (totalSpace - freeSpace)/1024, 
     				diskInfo.getString("readBytes"), diskInfo.getString("writeBytes"), decimalFormat.format(usePercent));
     	}
     	
    }

    /**
     * 磁盘信息
     * @param list
     */
    protected void printDisks(List<HWDiskStore> list) {
    	logger.info("");
    	logger.info("------------------ Checking Disks --------------------");
        logger.info("Disks:");
        List<String> diskInfos = new ArrayList<>();
        List<String> partitionInfos = new ArrayList<>();
        for (HWDiskStore disk : list) {
        	// 如果存在分区，以分区的identification为key；否则以disk的name为key
        	StringBuilder sb = new StringBuilder();
        	sb.append("diskName: ").append(disk.getName())
        	  .append(", readBytes: ").append(disk.getReadBytes())
        	  .append(", writeBytes: ").append(disk.getWriteBytes())
        	  .append("model: ").append(disk.getModel())
        	  .append("reads: ").append(disk.getReads())
        	  .append("writes: ").append(disk.getWrites())
        	  .append("size: ").append(disk.getSize())
        	  .append("serial: ").append(disk.getSerial())
        	  .append("timeStamp: ").append(disk.getTimeStamp())
        	  .append("transferTime: ").append(disk.getTransferTime())
        	  .append("currentQueueLength: ").append(disk.getCurrentQueueLength());
        	diskInfos.add(sb.toString());
        	
            logger.info(" " + disk.toString());
            List<HWPartition> partitions = disk.getPartitions();
            for (HWPartition part : partitions) {
                logger.info("  |-- " + part.toString());
                StringBuilder sb1 = new StringBuilder();
                sb1.append("partition name: ").append(part.getName())
                .append("identification: ").append(part.getIdentification())
                .append("major: ").append(part.getMajor())
                .append("minor: ").append(part.getMinor())
                .append("mountPoint: ").append(part.getMountPoint())
                .append("size: ").append(part.getSize())
                .append("type: ").append(part.getType())
                .append("uuid: ").append(part.getUuid());
                partitionInfos.add(sb1.toString());
            }
        }
        
        logger.info("");
        diskInfos.forEach(e -> logger.info(e));
        logger.info("");
        partitionInfos.forEach(e -> logger.info(e));
    }

    /**
     * 文件系统信息
     * @param fileSystem
     */
    protected void printFileSystem(FileSystem fileSystem) {
        logger.info("");
    	logger.info("------------------ File System --------------------");
        logger.info(String.format(" File Descriptors: %d/%d", fileSystem.getOpenFileDescriptors(), fileSystem.getMaxFileDescriptors()));

        // 以 volume的volumn为key
        List<String> storeInfos = new ArrayList<>();
        for (OSFileStore fs : fileSystem.getFileStores()) {
        	StringBuilder sb = new StringBuilder();
        	sb.append("file storeName: ").append(fs.getName())
        	  .append(", mount: ").append(fs.getMount())
        	  .append(", totalSpace: ").append(fs.getTotalSpace())
        	  .append(", freeSpace: ").append(fs.getFreeSpace())
        	  .append(", usableSpace: ").append(fs.getUsableSpace())
        	  .append(", description: ").append(fs.getDescription())
        	  .append(", type: ").append(fs.getType())
        	  .append(", freeInodes: ").append(fs.getFreeInodes())
        	  .append(", totalInodes: ").append(fs.getTotalInodes())
        	  .append(", volume: ").append(fs.getVolume())
        	  .append(", logicalVolume: ").append(fs.getLogicalVolume());
        	storeInfos.add(sb.toString());
        	
            // /dev/sda1 (Local Disk) [ext4] 9.2 GiB of 9.8 GiB free (94.1%), 655.3 K of 655.4 K files free (100.0%) is /dev/sda1  and is mounted at /boot
            logger.info(String.format(" %s (%s) [%s] %s of %s free (%.1f%%), %s of %s files free (%.1f%%) is %s " + (fs.getLogicalVolume() != null && fs.getLogicalVolume().length() > 0 ? "[%s]" : "%s") + " and is mounted at %s",
                    fs.getName(), 
                    fs.getDescription().isEmpty() ? "file system" : fs.getDescription(), 
                    fs.getType(),
                    FormatUtil.formatBytes(fs.getUsableSpace()), 
                    FormatUtil.formatBytes(fs.getTotalSpace()), 
                    100d * fs.getUsableSpace() / fs.getTotalSpace(),
                    FormatUtil.formatValue(fs.getFreeInodes(), ""), 
                    FormatUtil.formatValue(fs.getTotalInodes(), ""),
                    100d * fs.getFreeInodes() / fs.getTotalInodes(), 
                    fs.getVolume(), 
                    fs.getLogicalVolume(),
                    fs.getMount()));
        }
        
        logger.info("");
        storeInfos.forEach(e -> logger.info(e));
    }
    
    /**
     * 网络信息
     * @param list
     */
    protected void printNetworkInterfaces(List<NetworkIF> list) {
    	logger.info("");
     	logger.info("------------------ Checking Network interfaces --------------------");
        StringBuilder sb = new StringBuilder("Network Interfaces:");
        if (list.isEmpty()) {
            sb.append(" Unknown");
        } else {
            for (NetworkIF net : list) {
            	logger.info("name: {}, displayName: {}, hwaddr: {}, address: {}, TxBytes: {}, RxBytes: {}, TxPackets: {}, RxPackets: {}", net.getName(), net.getDisplayName(), net.getMacaddr(), 
            			net.getIPv4addr().length > 0 ? net.getIPv4addr()[0] : "", net.getBytesSent(), net.getBytesRecv(), net.getPacketsSent(), net.getPacketsRecv());
                sb.append("\n ").append(net.toString());
            }
        }
        logger.info(sb.toString());
    }
    
    /**
     * sigar
     */
    protected void printSigar() {
    	 logger.info("");
         logger.info("------------------ sigar output info --------------------");
         Sigar sigar = null;
         try {
 			sigar = new Sigar();
 			/** sigar 内存  */
 			Mem mem = sigar.getMem();
 			String total = decimalFormat.format(mem.getTotal() * 1D / 1024 / 1024 / 1024) + "G";
 	    	String free = decimalFormat.format(mem.getFree() * 1D / 1024 / 1024 / 1024) + "G";
 	    	String used = decimalFormat.format(mem.getUsed() * 1D / 1024 / 1024 / 1024) + "G";
 			logger.info("sigar memory, sigarTotal: {}, sigarFree: {}, sigarUsed: {}", total, free, used);
 			
 		    /** sigar cpu */
 			logger.info("");
 			CpuPerc[] cpus = sigar.getCpuPercList();
			for (int i = 0; cpus != null && i < cpus.length; i ++) {
				logger.info("sigar cpu{}, occupy: {}, sys: {}, user: {}, idle: {}", (i+1), cpus[i].getCombined() * 100, cpus[i].getSys() * 100, cpus[i].getUser() * 100, cpus[i].getIdle() * 100);
			}
			
 			/** sigar磁盘  */
			logger.info("");
 			org.hyperic.sigar.FileSystem fslist[] = sigar.getFileSystemList();  
 			for (int i = 0; i < fslist.length; i++) {  
 				org.hyperic.sigar.FileSystem fs = fslist[i];
 				// type == 2 表示的是磁盘
 				if (fs.getType() != 2) {
 					continue;
 				}
 				
 				FileSystemUsage usage = sigar.getFileSystemUsage(fs.getDirName());
 				logger.info("sigar disk, type: {}, name: {}, mountDir: {}, diskTotal: {}, diskFree: {}, diskUsed: {}, readBytes: {}, writeBytes: {}, usePercent: {}", fs.getType() + "-" + fs.getTypeName() + "-" + fs.getSysTypeName(),
 						fs.getDevName(), fs.getDirName(), usage.getTotal(), usage.getFree(), usage.getUsed(), usage.getDiskReadBytes(), usage.getDiskWriteBytes(), usage.getUsePercent());
 			} 
 			
 			/** sigar net */
 			logger.info("");
 			String[] ifNames = sigar.getNetInterfaceList();
			if (ifNames == null) {
				return;
			}
			
			for (int i = 0; i < ifNames.length; i ++) {
				try {
					NetInterfaceConfig ifconfig = sigar.getNetInterfaceConfig(ifNames[i]);
					if (NetFlags.LOOPBACK_ADDRESS.equals(ifconfig.getAddress())
							|| NetFlags.ANY_ADDR.equals(ifconfig.getAddress())
							|| (ifconfig.getFlags() & NetFlags.IFF_LOOPBACK) != 0
							|| (ifconfig.getFlags() & 1L) <= 0L
							|| NetFlags.NULL_HWADDR.equals(ifconfig.getHwaddr())) {
						continue;
					}
					
					NetInterfaceStat ifstat = sigar.getNetInterfaceStat(ifNames[i]);
					logger.info("sigar net, hwaddr: {}, address: {}, TxBytes: {}, RxBytes: {}, TxPackets: {}, RxPackets: {}", ifconfig.getHwaddr(), 
							ifconfig.getAddress(), ifstat.getTxBytes(), ifstat.getRxBytes(), ifstat.getTxPackets(), ifstat.getRxPackets());
					
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
 		} catch (Exception e) {
 			HawkException.catchException(e);
 		} finally {
 			if (sigar != null) {
 				sigar.close();
 				sigar = null;
 			}
 		}
    }

	
}