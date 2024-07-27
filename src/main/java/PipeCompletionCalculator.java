import java.util.ArrayList;
import java.util.List;

public class PipeCompletionCalculator {

    public static void main(String[] args) {
        // 示例使用：管段起点和终点的经纬度
        double startLatitude = 34.0522;
        double startLongitude = -118.2437;
        double endLatitude = 36.7783;
        double endLongitude = -119.4179;
        // 当前点位列表的经纬度
        double[][] currentPositions = {
                {34.0522, -118.2437}, // 当前点位1
                {36.7783, -119.4179}, // 当前点位2
                {35.0, -118.2}       // 当前点位3
        };

        CompletionResult completionResult = calculateCompletionRate(startLatitude, startLongitude, endLatitude, endLongitude, currentPositions);

        // 打印结果
        System.out.printf("最终完成量: %.2f km, 最终完成率: %.2f%%\n", completionResult.completionAmountKm, completionResult.completionRate);
        System.out.println("完成的管段点位信息：");
        for (Segment segment : completionResult.completedSegments) {
            System.out.printf("点位纬度: %.6f, 点位经度: %.6f, 完成量: %.2f km\n",
                    segment.latitude, segment.longitude, segment.completionAmountKm);
        }

    }

    public static CompletionResult calculateCompletionRate(double startLat, double startLon, double endLat, double endLon, double[][] currentPositions) {

        List<Segment> completedSegments = new ArrayList<>();

        double maxCompletionAmountKm = 0.0; // 记录最大完成量
        double maxCompletionRate = 0.0; // 记录最大完成率

        // 计算管段的中点
        double midLat = (startLat + endLat) / 2;
        double midLon = (startLon + endLon) / 2;

        // 计算管段的总长度
        double totalLengthKm = haversineDistance(startLat, startLon, endLat, endLon);

        System.out.printf("管段的长度： %.2f km \n", totalLengthKm);

        for (double[] currentPosition : currentPositions) {
            double currentLat = currentPosition[0];
            double currentLon = currentPosition[1];

            // 计算当前点位到管段中点的距离
            double distanceToMid = haversineDistance(midLat, midLon, currentLat, currentLon);

            // 根据当前点位与中点的距离来确定选择起点还是终点进行计算
            double referenceLat, referenceLon;
            if (distanceToMid < haversineDistance(midLat, midLon, startLat, startLon)) {
                referenceLat = startLat;
                referenceLon = startLon;
            } else {
                referenceLat = endLat;
                referenceLon = endLon;
            }

            // 计算当前点位到选定参考点的距离
            double distanceToReference = haversineDistance(referenceLat, referenceLon, currentLat, currentLon);

            // 更新最大完成量和完成率
            double completionAmountKm = Math.min(distanceToReference, totalLengthKm);
            maxCompletionAmountKm = Math.max(maxCompletionAmountKm, completionAmountKm);
            maxCompletionRate = Math.max(maxCompletionRate, (completionAmountKm / totalLengthKm) * 100);

            // 添加完成的管段点位信息
            completedSegments.add(new Segment(currentLat, currentLon, completionAmountKm));

        }

        // 确保最终完成率不超过100%
        maxCompletionRate = Math.min(maxCompletionRate, 100.0);


        // 创建并返回完成结果对象
        return new CompletionResult(maxCompletionAmountKm, maxCompletionRate, completedSegments);
    }


    // 完整的哈弗辛公式
    private static double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371.0; // 地球平均半径，单位为公里
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    // 完成的管段点位信息
    public static class Segment {
        double latitude;
        double longitude;
        double completionAmountKm;

        Segment(double latitude, double longitude, double completionAmountKm) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.completionAmountKm = completionAmountKm;
        }
    }

    // 完成结果
    public static class CompletionResult {
        double completionAmountKm;
        double completionRate;
        List<Segment> completedSegments;

        CompletionResult(double completionAmountKm, double completionRate, List<Segment> completedSegments) {
            this.completionAmountKm = completionAmountKm;
            this.completionRate = completionRate;
            this.completedSegments = completedSegments;
        }
    }
}