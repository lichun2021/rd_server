package com.hawk.activity.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ActivityEntityGenIndex {

    public static void main(String[] args) {
    	List<String> entityList = getEntityList();
    	System.out.println(String.format("file count:{%d}", entityList.size()));
    	
        for (int i = 0; i < entityList.size(); i++) {
        	genIndex(entityList.get(i));
        	System.out.println(String.format("gen index finish index:{%d}", i));
        }
    }

    /**
     * 生成索引
     */
    private static void genIndex(String entityPath) {
        // 临时文件
        File tempFile = new File("temp.txt");
        
        // 插入字符串
        insertIndex(tempFile, entityPath);        
        
        // 将临时文件覆盖原文件
        tempFile.renameTo(new File(entityPath));
        
        // temp文件写入entity
        replaceFileContent("temp.txt", entityPath);
        
        // 删除文件
        tempFile.delete();
    }
    
    /**
     * 插入import
     * @param tempFile
     * @param fileName
     * @param matchString
     * @param insertString
     */
    private static void insertIndex(File tempFile, String fileName) {
        try {
            // 创建文件读取器
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            FileWriter fileWriter = new FileWriter(tempFile);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            int index = 1;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                // 检查是否包含匹配字符串
                if (line.contains("@Column")) {
                	// 在匹配字符串的上一行插入字符串
                	String _str = String.format("    @IndexProp(id = %d)", index);
                    bufferedWriter.write(_str + "\n");
                    index ++;
                }
                
                // 检查是否包含匹配字符串
                if (line.contains("persistence.Entity") || line.contains("import javax.persistence.*;")) {
                	// 在匹配字符串的上一行插入字符串
                	String _str = String.format("import org.hawk.annotation.IndexProp;");
                    bufferedWriter.write(_str + "\n");
                }
                
                // 将当前行写入临时文件
                bufferedWriter.write(line + "\n");
            }
            
            // 关闭读写器
            bufferedReader.close();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取所有entity
     * @return
     */
    private static List<String> getEntityList() {
    	File dir = new File("src/main/java/com/hawk/activity/type");
        
        List<String> pathList = new ArrayList<>();
        listFiles(dir, pathList);
        
        List<String> entityList = new ArrayList<>();
        for (String path : pathList) {
        	if (!path.contains("Entity")) {
        		continue;
        	}
        	entityList.add(path);
        }
        return entityList;
    }
    
    /**
     * 递归获取目录下的文件
     * @param file
     * @param pathList
     */
    private static void listFiles(File file, List<String> pathList) {
        if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            if (fileList != null) {
                for (File f : fileList) {
                	listFiles(f, pathList);
                }
            }
        }
        pathList.add(file.getAbsolutePath());
    }
    
    /**
     * 替换文件内容 把A写入B
     * @param fileA
     * @param fileB
     */
    private static void replaceFileContent(String fileA, String fileB) {
        try {
            // 创建文件读取器和写入器
            FileReader fileReader = new FileReader(fileA);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            FileWriter fileWriter = new FileWriter(fileB);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            // 逐行写入文件A的内容到文件B
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                bufferedWriter.write(line + "\n");
            }

            // 关闭读写器
            bufferedReader.close();
            bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
