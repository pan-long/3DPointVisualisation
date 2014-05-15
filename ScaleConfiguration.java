import java.util.Collections;
import java.util.List;

class ScaleConfiguration {
    private final double cameraDistance = -100;
    private final double fieldOfView = 45;

    private List<point> pointsList = null;
    private double maxAbsCoor = 0;
    private double scaleFactor = 0;
    private double radius = 0;

    public ScaleConfiguration(List<point> pointsList, double maxAbsCoor)
    {
        this.pointsList = pointsList;
        Collections.sort(this.pointsList);

        this.maxAbsCoor = maxAbsCoor;
        this.scaleFactor = calculateScaleFactor();
        this.radius = (calculateMinDis(0, this.pointsList.size() - 1) / 2) * this.scaleFactor;
    }

    public double getScaleFactor()
    {
        return this.scaleFactor;
    }

    private double calculateScaleFactor()
    {
        double max = 0.0;
        for (int i = 0; i < pointsList.size(); i ++)
        {
            max = Math.max(max, Math.abs(pointsList.get(i).getX()));
            max = Math.max(max, Math.abs(pointsList.get(i).getY()));
            max = Math.max(max, Math.abs(pointsList.get(i).getZ()));
        }

        return maxAbsCoor / max;
    }

    public double getRadius()
    {
        return this.radius;
    }

    private double calculateMinDis(int start, int end)
    {
        if (start >= end)
            return 0;
        else
        {
            int middle = (start + end) / 2;
            double d1 = calculateMinDis(start, middle);
            double d2 = calculateMinDis(middle + 1, end);

            double d3 = Double.MAX_VALUE;
            for (int i = start; i <= middle; i ++)
                for (int j = middle + 1; j <= end; j ++)
                    if (pointsList.get(i).getX() - pointsList.get(j).getX() <= d3)
                    {
                        double dis = pointsList.get(i).disTo(pointsList.get(j));
                        if (dis > 0)
                            d3 = Math.min(d3, dis);
                    }

            double minD = Double.MAX_VALUE;
            if (d1 > 0)
                minD = Math.min(minD, d1);
            if (d2 > 0)
                minD = Math.min(minD, d2);
            if (d3 > 0)
                minD = Math.min(minD, d3);

            return minD;
        }
    }

    public double getCameraDistance(){
        return this.cameraDistance;
    }

    public double getFieldOfView(){
        return this.fieldOfView;
    }
}
