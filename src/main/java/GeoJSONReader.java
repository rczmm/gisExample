//import org.geotools.api.feature.Feature;
//import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.CRS;
import org.opengis.feature.Feature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.GeometryAttributeImpl;
import org.geotools.geojson.feature.FeatureJSON;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.geotools.geojson.geom.GeometryJSON;
import org.locationtech.jts.geom.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeoJSONReader {

    public static void main(String[] a) throws Exception {
        // 示例的geojson字符串
        String json = "{\n" +
                "  \"type\": \"FeatureCollection\",\n" +
                "  \"features\": [\n" +
                "    {\n" +
                "      \"type\": \"Feature\",\n" +
                "      \"properties\": {\n" +
                "      \"category\":\"调压柜\",\n" +
                "        \"name\": \"Point 1\"\n" +
                "      },\n" +
                "      \"geometry\": {\n" +
                "        \"type\": \"Point\",\n" +
                "        \"coordinates\": [116.35399643112183, 39.92987395323626]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"Feature\",\n" +
                "      \"properties\": {\n" +
                "      \"category\":\"调压柜\",\n" +
                "        \"name\": \"Point 2\"\n" +
                "      },\n" +
                "      \"geometry\": {\n" +
                "        \"type\": \"Point\",\n" +
                "        \"coordinates\": [116.3551980607605, 39.92990613974444]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"Feature\",\n" +
                "      \"properties\": {\n" +
                "      \"category\":\"我不是调压柜\",\n" +
                "        \"name\": \"Point 1\"\n" +
                "      },\n" +
                "      \"geometry\": {\n" +
                "        \"type\": \"Point\",\n" +
                "        \"coordinates\": [116.35642114807129, 39.92910147703997]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"Feature\",\n" +
                "\n" +
                "      \"properties\": {\n" +
                "      \"category\":\"高压线\",\n" +
                "        \"name\": \"Line 1\"\n" +
                "      },\n" +
                "      \"geometry\": {\n" +
                "        \"type\": \"LineString\",\n" +
                "        \"coordinates\": [\n" +
                "          [116.353127395401, 39.92865086592547],\n" +
                "          [116.35405007530213, 39.928264627827325]\n" +
                "        ]\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\": \"Feature\",\n" +
                "      \"properties\": {\n" +
                "      \"category\":\"高压线\",\n" +
                "        \"name\": \"Line 2\"\n" +
                "      },\n" +
                "      \"geometry\": {\n" +
                "        \"type\": \"LineString\",\n" +
                "        \"coordinates\": [\n" +
                "          [116.35468307662964, 39.927846203221],\n" +
                "          [116.35520878959656, 39.92755652464739]\n" +
                "          [116.35992947746277, 39.93219138182513]\n" +
                "          [116.36024275895184, 39.93711377298796]\n" +
                "          [116.35235922278811, 39.941705714821445]\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";


        // 指定GeometryJSON构造器，15位小数
        FeatureJSON fJson_15 = new FeatureJSON(new GeometryJSON(15));

        // 读取为FeatureCollection
        FeatureCollection featureCollection = fJson_15.readFeatureCollection(json);

        // 遍历FeatureCollection，打印每个Feature的信息
        try (FeatureIterator iterator = featureCollection.features()) {
            while (iterator.hasNext()) {
                Feature feature = iterator.next();
                GeometryAttributeImpl geom = (GeometryAttributeImpl) feature.getDefaultGeometryProperty();
                System.out.println("Geometry: " + geom);
                System.out.println("Geometry Type: " + geom.getValue().getGeometryType());
            }
        }

        // 定义查询区域的多边形（例如，使用一个简单的矩形）
        GeometryFactory geometryFactory = new GeometryFactory();
        Polygon polygon = geometryFactory.createPolygon(
                new GeometryFactory().createLinearRing(
                        new Coordinate[]{
                                new Coordinate(116.35259095359802, 39.93093610800616),
                                new Coordinate(116.35918918777466, 39.93083954848162),
                                new Coordinate(116.35961834121704, 39.926290521992364),
                                new Coordinate(116.35226908851624, 39.92635489500872),
                                new Coordinate(116.35259095359802, 39.93093610800616)
                        }
                ),
                null
        );


        // 初始化存储点数量和线长度的映射
        Map<String, Integer> pointCounts = new HashMap<>();
        Map<String, Double> lineLengths = new HashMap<>();
        List<LineString> intersectingLines = new ArrayList<>();


        // 遍历FeatureCollection，统计点数量和线长度
        try (SimpleFeatureIterator iterator = (SimpleFeatureIterator) featureCollection.features()) {
            while (iterator.hasNext()) {
                SimpleFeature feature = iterator.next();
                String category = (String) feature.getAttribute("category");
                Geometry geometry = (Geometry) feature.getDefaultGeometry();

                if (geometry.intersects(polygon)) {
                    // 统计点的数量
                    if (geometry.getGeometryType().equalsIgnoreCase("Point")) {
                        pointCounts.put(category, pointCounts.getOrDefault(category, 0) + 1);
                    } else if (geometry.getGeometryType().equalsIgnoreCase("LineString")) { // 统计线的长度
                        double length = geometry.getLength();
                        Geometry intersection = geometry.intersection(polygon);
                        double l = intersection.getLength();
                        lineLengths.put(category, lineLengths.getOrDefault(category, 0.0) + l);
                        if (!intersection.isEmpty()) {
                            intersectingLines.add((LineString) intersection);
                        }

                    }
                }
            }
        }

        // 打印结果
        System.out.println("不同分类的点的数量:");
        pointCounts.forEach((category, count) -> System.out.println(category + ": " + count));
        System.out.println("\n不同分类的线的长度:");
        lineLengths.forEach((category, length) -> System.out.println(category + ": " + length));
        // 打印交叉的线段
        System.out.println("区域内完整的线路信息:");
        for (LineString line : intersectingLines) {
            System.out.println(line);
        }


        // 将FeatureCollection写回GeoJSON字符串
        OutputStream outputStream = new ByteArrayOutputStream();
        fJson_15.writeFeatureCollection(featureCollection, outputStream);
//        System.out.println(outputStream);

        // 获取SimpleFeatureType
        SimpleFeatureType simpleFeatureType = (SimpleFeatureType) featureCollection.getSchema();
        // 第1个问题。坐标顺序与实际坐标顺序不符合
        CoordinateReferenceSystem crs = simpleFeatureType.getCoordinateReferenceSystem();
        System.out.println(CRS.getAxisOrder(crs));  //输出：NORTH_EAST

        //第2个问题。查看空间列名称
//        System.out.println(simpleFeatureType.getGeometryDescriptor().getLocalName());  //输出：geometry

        //第3个问题。坐标精度丢失
        //第4个问题。默认无坐标系和空值输出
        OutputStream oStream = new ByteArrayOutputStream();
        fJson_15.writeFeatureCollection(featureCollection, oStream);
        System.out.println(oStream);

        // 第5个问题。坐标变换问题，由坐标顺序引发
        SimpleFeatureIterator iterator = (SimpleFeatureIterator) featureCollection.features();
        SimpleFeature simpleFeature = iterator.next();
        Geometry geom = (Geometry) simpleFeature.getDefaultGeometry();
        iterator.close();
        System.out.println(geom.getArea());
    }

}
