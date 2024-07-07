import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

public class GeoVisualization extends JPanel {

    private final Point[] points;
    private final LineString[] lines;
    private final Polygon polygon;

    public GeoVisualization(Point[] points, LineString[] lines, Polygon polygon) {
        this.points = points;
        this.lines = lines;
        this.polygon = polygon;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // 绘制点
        g2d.setColor(Color.RED);
        for (Point point : points) {
            Coordinate coord = point.getCoordinate();
            Point2D screenCoord = toScreenCoordinate(coord);
            System.out.println("Point screen coordinates: " + screenCoord);
            g2d.fillOval((int) screenCoord.getX(), (int) screenCoord.getY(), 5, 5);
        }

        // 绘制线
        g2d.setColor(Color.BLUE);
        for (LineString line : lines) {
            Path2D path = new Path2D.Double();
            Coordinate[] coords = line.getCoordinates();
            Point2D startCoord = toScreenCoordinate(coords[0]);
            path.moveTo(startCoord.getX(), startCoord.getY());
            for (int i = 1; i < coords.length; i++) {
                Point2D screenCoord = toScreenCoordinate(coords[i]);
                path.lineTo(screenCoord.getX(), screenCoord.getY());
                System.out.println("Line point screen coordinates: " + screenCoord);
            }
            g2d.draw(path);
        }

        // 绘制多边形
        g2d.setColor(Color.GREEN);
        Path2D path = new Path2D.Double();
        Coordinate[] coords = polygon.getCoordinates();
        Point2D startCoord = toScreenCoordinate(coords[0]);
        path.moveTo(startCoord.getX(), startCoord.getY());
        for (int i = 1; i < coords.length; i++) {
            Point2D screenCoord = toScreenCoordinate(coords[i]);
            path.lineTo(screenCoord.getX(), screenCoord.getY());
            System.out.println("Polygon point screen coordinates: " + screenCoord);
        }
        path.closePath();
        g2d.draw(path);
    }

    // 将地理坐标转换为屏幕坐标
    private Point2D toScreenCoordinate(Coordinate coord) {
        // 缩放因子，用于将地理坐标转换为屏幕坐标
        double scale = 1e1;
        // X方向偏移，用于平移地理坐标
        double offsetX = 116;
        double x = (coord.x - offsetX) * scale;
        // Y方向偏移，用于平移地理坐标
        double offsetY = 39;
        double y = (offsetY - coord.y) * scale; // 注意：Y轴方向需要翻转
        return new Point2D.Double(x, y);
    }

    public static void main(String[] args) {
        // 创建一些示例点、线和多边形
        GeometryFactory geometryFactory = new GeometryFactory();

        Point[] points = new Point[]{
                geometryFactory.createPoint(new Coordinate(116.35399643112183, 39.92987395323626)),
                geometryFactory.createPoint(new Coordinate(116.3551980607605, 39.92990613974444)),
                geometryFactory.createPoint(new Coordinate(116.35642114807129, 39.92910147703997))
        };

        LineString[] lines = new LineString[]{
                geometryFactory.createLineString(new Coordinate[]{
                        new Coordinate(116.353127395401, 39.92865086592547),
                        new Coordinate(116.35405007530213, 39.928264627827325)
                }),
                geometryFactory.createLineString(new Coordinate[]{
                        new Coordinate(116.35468307662964, 39.927846203221),
                        new Coordinate(116.35520878959656, 39.92755652464739)
                })
        };

        Polygon polygon = geometryFactory.createPolygon(new Coordinate[]{
                new Coordinate(116.35259095359802, 39.93093610800616),
                new Coordinate(116.35918918777466, 39.93083954848162),
                new Coordinate(116.35961834121704, 39.926290521992364),
                new Coordinate(116.35226908851624, 39.92635489500872),
                new Coordinate(116.35259095359802, 39.93093610800616)
        });

        // 创建和显示GUI
        JFrame frame = new JFrame("GeoVisualization");
        GeoVisualization geoVisualization = new GeoVisualization(points, lines, polygon);
        frame.add(geoVisualization);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setVisible(true);
    }
}
