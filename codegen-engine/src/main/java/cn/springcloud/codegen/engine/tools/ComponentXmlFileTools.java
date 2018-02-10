package cn.springcloud.codegen.engine.tools;

import cn.springcloud.codegen.engine.entity.ComponentMetadata;
import cn.springcloud.codegen.engine.entity.ConfigParams;
import cn.springcloud.codegen.engine.entity.GeneratorMetadata;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.*;
import java.util.*;

/**
 * @author Vincent.
 * @createdOn 2018/01/29 22:45
 * xml解析工具类
 */
public class ComponentXmlFileTools {

    /**
     * 默认type类型
     */
    private static final String DEFAULT_TYPE_VALUE = "DEFAULT";

    public static ComponentMetadata parseToMetadata(Element root) {
        ComponentMetadata componentMetadata = new ComponentMetadata();
        Attribute attribute = root.attribute("id");
        componentMetadata.setComponentId(attribute == null ? "" : attribute.getValue());
        String type = getComponentType(root);
        componentMetadata.setComponentType(type);
        List<GeneratorMetadata> generatorMetadataList = new ArrayList<>();
        List<?> elements = root.elements();
        if (elements.size() != 0) {
            // 有子元素
            Iterator<?> iterator = elements.iterator();
            List<Object> list = new ArrayList<Object>();
            while (iterator.hasNext()) {
                Element elem = (Element) iterator.next();
                list.add(parseElement(elem));
            }
            JSONArray jsonArray = (JSONArray) JsonTools.objectToJson(list);
            for (int i = 0; i < jsonArray.size(); i++){
                GeneratorMetadata generatorMetadata = JsonTools.parseObjectByGenericity(jsonArray.getString(i), GeneratorMetadata.class);
                generatorMetadataList.add(generatorMetadata);
            }
        }
        componentMetadata.setGeneratorData(generatorMetadataList);
        return componentMetadata;
    }

    private static String getComponentType(Element root) {
        Attribute attribute = root.attribute("type");
        if (attribute == null || StringUtils.isBlank(attribute.getValue())){
            return DEFAULT_TYPE_VALUE;
        }else {
            return attribute.getValue();
        }
    }

    public static Object parseElement(Element element) {
        List<?> elements = element.elements();

        if (elements.size() == 0) {
            Attribute value = element.attribute("value");
            return value == null ? element.getTextTrim() : value.getValue();
        } else {
            Map<String, Object> data = new HashMap<String, Object>();
            Iterator<?> iterator = elements.iterator();
            while (iterator.hasNext()) {
                Element elem = (Element) iterator.next();
                Attribute key = elem.attribute("key");
                data.put(key == null ? elem.getName() : key.getValue(), parseElement(elem));
            }
            return data;
        }
    }

    /**
     * 返回读取到的对象信息
     * @param filePath
     * @return
     */
    public static ComponentMetadata readXmlFile(String filePath){

        InputStream in = null;
        try {

            SAXReader reader = new SAXReader();
            File file = new File(filePath);
            in = new FileInputStream(file);
            Document document = reader.read(in);
            Element root = document.getRootElement();

            return parseToMetadata(root);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {

            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return  null;
    }

    /**
     * 返回读取到的对象信息
     * @param in 文件流
     * @return
     */
    public static ComponentMetadata readXmlFile(InputStream in){

        try {

            SAXReader reader = new SAXReader();
            Document document = reader.read(in);
            Element root = document.getRootElement();

            return parseToMetadata(root);
        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {

            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return  null;
    }

}
