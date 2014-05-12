import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


class dataReader{
	public List<point> getPoints(String filePath){
		List<point> points = new ArrayList<point>();;
		try{
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			int numOfPoints = Integer.parseInt(reader.readLine());
			for(int i=0; i<numOfPoints; i++) {
				String[] coordinates = reader.readLine().split(" ");
				double x = Double.parseDouble(coordinates[0]);
				double y = Double.parseDouble(coordinates[1]);
				double z = Double.parseDouble(coordinates[2]);
				points.add(new point(x, y, z));
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return points;
	}
}