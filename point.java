import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

public class point implements Comparable<point>
{
    private double x, y, z;
    private VBox popupBox = null;

    public point(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX()
    {
        return this.x;
    }

    public double getY()
    {
        return this.y;
    }

    public double getZ()
    {
        return this.z;
    }

    public VBox getBox()
    {
        return getXYZBox();
    }

    public VBox getXYZBox()
    {
        if (popupBox != null)
            return popupBox;
        else
        {
            popupBox = new VBox();
            Label x = new Label();
            x.setText("x: " + x);
            Label y = new Label();
            y.setText("y: " + y);
            Label z = new Label();
            z.setText("z: " + z);

            popupBox.getChildren().addAll(x, y, z);
            popupBox.setStyle("-fx-background-color: white;");

            return popupBox;
        }
    }

    @Override
    public int compareTo(point other)
    {
        if (other == null)
            return 1;
        else if (this.x > other.getX())
            return 1;
        else if (this.x < other.getX())
            return -1;
        else if (this.y > other.getY())
            return 1;
        else if (this.y < other.getY())
            return -1;
        else if (this.z > other.getZ())
            return 1;
        else if (this.z < other.getZ())
            return -1;
        else
            return 0;
    }

    public double disTo(point other)
    {
        if (other == null)
            return 0;
        else
            return Math.sqrt((this.x - other.getX()) * (this.x - other.getX()) + (this.y - other.getY()) * (this.y - other.getY()) + (this.z - other.getZ()) * (this.z - other.getZ()));
    }
}


