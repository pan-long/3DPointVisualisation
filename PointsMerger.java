import java.util.ArrayList;
import java.util.List;


class PointsMerger {
    private List<point> pointsList = null;
    private List<point> mergedPointsList = null;
    private ScaleConfiguration sc = null;
    private double scaleFactor = 0;
    private double radius = 0;
    private double minDis = 0;

    public PointsMerger(List<point> points){
        pointsList = points;
        sc = new ScaleConfiguration(points, 10);
        mergedPointsList = new ArrayList<point> ();
        scaleFactor = sc.getScaleFactor();
        radius = sc.getRadius();
        minDis = 4 * radius;
    }

    public List<point> getMergedPoints(){
        for (point p : pointsList){
            if (mergedPointsList.isEmpty())
                mergedPointsList.add(p);
            else{
                boolean isMerged = false;
                for (point mergedPoint : mergedPointsList){
                    if (p.disTo(mergedPoint) * scaleFactor < minDis){
                        isMerged = true;
                        break;
                    }
                }

                if (!isMerged)
                    mergedPointsList.add(p);
            }
        }

        return mergedPointsList;
    }
}
