package com.hawk.game.module.homeland.map;

import com.hawk.game.util.GameUtil;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;

/**
 * 家园地图阻挡信息
 *
 * @author zhy
 */
public class HomeLandMapBlock {
    /**
     * 阻挡点id列表
     */
    private Set<Integer> stops = new TreeSet<>();

    public boolean init(int theme) {
        String map = theme + ".xml";
        return loadMapData(map);
    }

    /**
     * 加载预置数据
     *
     * @return
     */
    private boolean loadMapData(String map) {
        String filePath = HawkOSOperator.getWorkPath() + "xml/homeland_block" + map;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            // 使用 InputSource 和 StringReader 来读取字符串形式的XML
            Document document = builder.parse(Files.newInputStream(Paths.get(filePath)));
            document.getDocumentElement().normalize();
            // 获取所有名为 "data" 的节点
            NodeList nodeList = document.getElementsByTagName("data");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    // 从元素属性中读取 id, x, y
                    int x = Integer.parseInt(element.getAttribute("x"));
                    int y = Integer.parseInt(element.getAttribute("y"));
                    int pointId = GameUtil.combineXAndY(x, y);
                    stops.add(pointId);
                }
            }
            return true;
        } catch (Exception e) {
            HawkException.catchException(e);
        }
        return false;
    }

    /**
     * 是否是阻挡点
     *
     * @param x
     * @param y
     * @return
     */
    public boolean isStopPoint(int pointId) {
        return stops.contains(pointId);
    }

}
