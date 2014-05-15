public class point implements Comparable<point>
{
    private double x, y, z;
    private String[] properties = null;

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

    public String[] getProperties()
    {
        return getXYZProperties();
    }

    public String[] getXYZProperties()
    {
        if (properties != null)
            return properties;
        else
        {
            properties = new String[3];
            properties[0] = "x: " + this.x;
            properties[1] = "y: " + this.y;
            properties[2] = "z: " + this.z;
            return properties;
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


