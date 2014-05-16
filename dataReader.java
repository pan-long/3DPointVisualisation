import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;

class dataReader
{
    private Button button;
    private Label label;
    private FileChooser fileChooser;
    private Stage stage;
    private List<point> points;

    public dataReader(FileChooser fileChooser, Button button, Label label, Stage stage){
        this.fileChooser = fileChooser;
        this.button = button;
        this.label = label;
        this.stage = stage;

        bindListenerToButton();
    }

    public dataReader(String filename)
    {
        openFile(new File(filename));
    }

    public List<point> getPoints()
    {
        return points;
    }

    private void bindListenerToButton()
    {
        button.setOnAction(
            new EventHandler<ActionEvent>() {
                @Override
                public void handle(final ActionEvent e) {
                    configureFileChooser(fileChooser);
                    File file = fileChooser.showOpenDialog(stage);
                    if (file != null) {
                        label.setText(file.getName());
                        openFile(file);
                    }
                }
            });
    }

    private void configureFileChooser(
        final FileChooser fileChooser) {                   
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Point Cloud Data(PCD)", "*.pcd")
            );
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
                
                if (coordinates.length == 4) {
                    int color = (int)Double.parseDouble(coordinates[3]);
                    points.add(new point(x, y, z, color));   
                } else {
                    points.add(new point(x, y, z));
                }
            }
            reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
