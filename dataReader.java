import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;

class dataReader
{
    private FileChooser fileChooser;
    private Button button;
    private List<point> points;

    public dataReader(FileChooser fileChooser, Button button){
        this.fileChooser = fileChooser;
        this.button = button;
    }

    public List<point> getPoints(file)
    {
        return points != null ? points : null;
    }

    private void bindListenerToButton()
    {
        button.setOnAction(
            new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent e) {
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        openFile(file);
                    }
                }
            });
    }

    private void openFile(File file)
    {
        points = new ArrayList<point>();;
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            int numOfPoints = Integer.parseInt(reader.readLine());
            for (int i = 0; i < numOfPoints; i++)
            {
                String[] coordinates = reader.readLine().split(" ");
                double x = Double.parseDouble(coordinates[0]);
                double y = Double.parseDouble(coordinates[1]);
                double z = Double.parseDouble(coordinates[2]);
                points.add(new point(x, y, z));
            }
            reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}






