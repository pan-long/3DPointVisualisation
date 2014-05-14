import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import javafx.stage.Popup;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import java.util.Collections;
import java.util.List;
import javafx.scene.SubScene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import java.util.Scanner;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;

public class visualise extends Application
{
    final Group root = new Group();
    final Xform axisGroup = new Xform();
    final Xform pointGroup = new Xform();
    final Xform space = new Xform();
    final PerspectiveCamera camera = new PerspectiveCamera(true);
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();
    final Slider scaleSlider = buildScaleSlider();
    
    private static final double CAMERA_INITIAL_DISTANCE = -450;
    private static final double CAMERA_INITIAL_X_ANGLE = 70.0;
    private static final double CAMERA_INITIAL_Y_ANGLE = 320.0;
    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 10000.0;
    private static final double AXIS_LENGTH = 250.0;
    private static final double HYDROGEN_ANGLE = 104.5;
    private static final double MOUSE_SPEED = 0.1;
    private static final double ROTATION_SPEED = 2.0;
    private static final double TRACK_SPEED = 0.3;
    private static final double SPHERE_RADIUS = 0.01;

    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private double mouseDeltaX;
    private double mouseDeltaY;

    private List<point> pointsList = null;

    private Stage stage = null;

    private Slider buildScaleSlider()
    {
        Slider slider = new Slider();
        slider.setMin(0);
        slider.setMax(100);
        slider.setValue(40);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(50);
        slider.setMinorTickCount(5);
        slider.setBlockIncrement(10);

        return slider;
    }

    private void buildCamera()
    {
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        cameraXform3.setRotateZ(180.0);

        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);

        camera.setFieldOfView(0.3);
    }

    private void buildAxes()
    {
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);

        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);

        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);

        final Box xAxis = new Box(AXIS_LENGTH, 0.01, 0.01);
        final Box yAxis = new Box(0.01, AXIS_LENGTH, 0.01);
        final Box zAxis = new Box(0.01, 0.01, AXIS_LENGTH);

        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);

        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
        axisGroup.setVisible(false);
        space.getChildren().addAll(axisGroup);
    }

    private void handleMouse(Scene scene, final Node root)
    {
        scene.setOnMousePressed(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent me)
            {
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseOldX = me.getSceneX();
                mouseOldY = me.getSceneY();
            }
        });

        scene.setOnMouseDragged(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent me)
            {
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseDeltaX = mousePosX - mouseOldX;
                mouseDeltaY = mousePosY - mouseOldY;

                double modifier = 1.0;

                if (me.isPrimaryButtonDown())
                {
                    cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX * MOUSE_SPEED * modifier * ROTATION_SPEED);
                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() - mouseDeltaY * MOUSE_SPEED * modifier * ROTATION_SPEED);
                }
            }
        });
    }

    private double getMinDis(int start, int end)
    {
        if (start >= end)
            /* return Double.MAX_VALUE; */
            return 0;
        else
        {
            int middle = (start + end) / 2;
            double d1 = getMinDis(start, middle);
            double d2 = getMinDis(middle + 1, end);

            double d3 = Double.MAX_VALUE;
            for (int i = start; i <= middle; i ++)
                for (int j = middle + 1; j <= end; j ++)
                    if (pointsList.get(i).getX() - pointsList.get(j).getX() <= d3)
                    {
                        double dis = pointsList.get(i).disTo(pointsList.get(j));
                        if (dis > 0.0)
                            d3 = Math.min(d3, dis);
                    }

            /* double minD = Math.min(d1, d2); */
            /* minD = Math.min(minD, d3); */

            /* return minD; */

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

    private void buildPoints(double radius)
    {
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);

        Xform pointsXform = new Xform();

        for (point p : pointsList)
        {
            Xform pointXform = new Xform();
            Sphere pointSphere = new Sphere(radius);
            pointSphere.setMaterial(redMaterial);
            pointSphere.setTranslateX(p.getX());
            pointSphere.setTranslateY(p.getY());
            pointSphere.setTranslateZ(p.getZ());

            pointsXform.getChildren().add(pointXform);
            pointXform.getChildren().add(pointSphere);

            VBox box = new VBox();
            Label x = new Label();
            x.setText("x: " + p.getX());
            Label y = new Label();
            y.setText("y: " + p.getY());
            Label z = new Label();
            z.setText("z: " + p.getZ());

            box.getChildren().addAll(x, y, z);

            Popup pop = new Popup();

            pop.setAutoFix(false);
            pop.setHideOnEscape(true);
            pop.getContent().addAll(box);

            pointXform.setOnMouseMoved(new EventHandler<MouseEvent>()
            {
                @Override
                public void handle(MouseEvent me)
                {
                    pop.setX(me.getSceneX());
                    pop.setY(me.getSceneY());
                    pop.show(stage);
                }
            });

            pointXform.setOnMouseExited(new EventHandler<MouseEvent>()
            {
                @Override
                public void handle(MouseEvent me)
                {
                    if (pop.isShowing())
                        pop.hide();
                }
            });
        }

        pointGroup.getChildren().add(pointsXform);
        space.getChildren().addAll(pointGroup);
    }

    private VBox buildLeftVbox(){
        VBox vbox = new VBox();
        vbox.setPadding(new Insets(10)); 
        vbox.setSpacing(8); 

        Text title = new Text("Settings");
        Text scaleSliderText = new Text("Scale");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        vbox.getChildren().add(title);
        vbox.getChildren().add(scaleSliderText);
        vbox.getChildren().add(scaleSlider);

        return vbox;
    }

    private SubScene buildSubScene(){
       SubScene subScene = new SubScene(root, 900, 768, true, SceneAntialiasing.DISABLED);
        subScene.setCamera(camera);
        subScene.setFill(Color.GREY);
        
        return subScene; 
    }

    @Override
    public void start(Stage primaryStage)
    {
        BorderPane borderPane = new BorderPane();
        root.getChildren().add(space);
        root.setDepthTest(DepthTest.ENABLE);
        stage = primaryStage;

        /* Scanner sr = new Scanner(System.in); */
        /* String filename = sr.next(); */
        String filename = "data.PCD";

        dataReader dr = new dataReader(filename);

        pointsList = dr.getPoints();

        Collections.sort(pointsList);
        double radius = getMinDis(0, pointsList.size() - 1) / 2;
        if (radius < 1E-9)
            radius = SPHERE_RADIUS;
        radius = 0.01;

        buildCamera();
        buildAxes();
        buildPoints(radius);

        borderPane.setCenter(buildSubScene());
        borderPane.setLeft(buildLeftVbox());
        Scene scene = new Scene(borderPane, 1024, 768, true);
        
        handleMouse(scene, space);

        primaryStage.setTitle("3D Point Visualisation");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}

