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
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.stage.StageStyle;

public class visualise extends Application
{
    private Group root = new Group();
    private Xform axisGroup = new Xform();
    private Xform pointGroup = new Xform();
    private Xform space = new Xform();
    private PerspectiveCamera camera = new PerspectiveCamera(true);
    private Xform cameraXform = new Xform();

    private static final double CAMERA_INITIAL_X_ANGLE = 165.0;
    private static final double CAMERA_INITIAL_Y_ANGLE = 210.0;
    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 10000;

    private static final double MAX_ABS_COORDINATE = 10;
    private static final double HYDROGEN_ANGLE = 104.5;
    private static final double MOUSE_SPEED = 0.1;
    private static final double ROTATION_SPEED = 2.0;
    private static final double TRACK_SPEED = 0.3;

    private double cameraDistance;
    private double cameraFieldOfView;
    private double sphereRadius;
    private double scaleFactor;
    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private double mouseDeltaX;
    private double mouseDeltaY;
    private double xAngle;

    private List<point> pointsList = null;

    private Stage stage = null;
    private dataReader reader = null;

    private void buildCamera()
    {
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(camera);
        cameraXform.setRotateZ(180.0);

        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateZ(cameraDistance);
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);

        camera.setFieldOfView(cameraFieldOfView);
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

        final Box xAxis = new Box(3 * MAX_ABS_COORDINATE, sphereRadius, sphereRadius);
        final Box yAxis = new Box(sphereRadius, 3 * MAX_ABS_COORDINATE, sphereRadius);
        final Box zAxis = new Box(sphereRadius, sphereRadius, 3 * MAX_ABS_COORDINATE);

        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);

        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
        axisGroup.setVisible(true);
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
                xAngle = cameraXform.rx.getAngle();
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
                    if (((xAngle % 360 > 0 && xAngle % 360 < 90) || (xAngle % 360 < 0 && xAngle % 360 + 360 < 90)) 
                        || (xAngle % 360 > 270 || (xAngle % 360 < 0 && xAngle % 360 + 360 > 270)))
                        cameraXform.ry.setAngle(cameraXform.ry.getAngle() + mouseDeltaX * MOUSE_SPEED * modifier * ROTATION_SPEED);
                    else
                        cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX * MOUSE_SPEED * modifier * ROTATION_SPEED);

                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() - mouseDeltaY * MOUSE_SPEED * modifier * ROTATION_SPEED);
                }
            }
        });
    }


    private void buildPoints()
    {
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);

        Xform pointsXform = new Xform();

        for (point p : pointsList)
        {
            Xform pointXform = new Xform();
            Sphere pointSphere = new Sphere(sphereRadius);
            pointSphere.setMaterial(redMaterial);
            pointSphere.setTranslateX(p.getX() * scaleFactor);
            pointSphere.setTranslateY(p.getY() * scaleFactor);
            pointSphere.setTranslateZ(p.getZ() * scaleFactor);

            pointsXform.getChildren().add(pointXform);
            pointXform.getChildren().add(pointSphere);

            VBox box = new VBox();
            box.setStyle("-fx-background-color: white;");

            String[] properties = p.getProperties();
            for (String str : properties)
            {
                Label label = new Label(str);
                box.getChildren().add(label);
            }

            Popup pop = new Popup();

            pop.setAutoFix(false);
            pop.setHideOnEscape(true);
            pop.getContent().addAll(box);

            pointXform.setOnMouseMoved(new EventHandler<MouseEvent>()
            {
                @Override
                public void handle(MouseEvent me)
                {
                    pop.setX(me.getSceneX() + pop.getWidth());
                    pop.setY(me.getSceneY() - pop.getHeight() / 2.0) ;

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

    private VBox buildLeftVbox(Stage stage)
    {
        VBox vbox = new VBox();
        vbox.setSpacing(8);
        vbox.setPrefWidth(200);
        vbox.setPadding(new Insets(10));
        
        Text title = new Text("Settings");
        Text cameraDistanceLabel = new Text("Camera Distance");
        Text fielOfViewLabel = new Text("Field of View");
        Label fileNameLabel = new Label("No File Chosen");

        Slider cameraDistanceSlider = buildCameraDistanceSlider();
        Slider fieldOfViewSlider = buildFieldOfViewSlider();
        CheckBox axesCheckBox = buildShowAxesCheckBox();
        Button openButton = new Button("Choose File...");
        Button buildButton = new Button("Build");
        FileChooser fileChooser = new FileChooser();

        reader = new dataReader(fileChooser, openButton, fileNameLabel, stage);
        buildVisualization(buildButton);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        vbox.getChildren().add(title);
        vbox.getChildren().add(cameraDistanceLabel);
        vbox.getChildren().add(cameraDistanceSlider);
        vbox.getChildren().add(fielOfViewLabel);
        vbox.getChildren().add(fieldOfViewSlider);
        vbox.getChildren().add(axesCheckBox);
        vbox.getChildren().add(openButton);
        vbox.getChildren().add(fileNameLabel);
        vbox.getChildren().add(buildButton);

        return vbox;
    }

    private Slider buildCameraDistanceSlider()
    {
        Slider slider = new Slider();
        slider.setMin(0.25);
        slider.setMax(1.75);
        slider.setValue(1);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(0.5);
        slider.setMinorTickCount(5);
        slider.setBlockIncrement(0.05);

        slider.valueProperty().addListener(new ChangeListener<Number>()
        {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val)
            {
                camera.setTranslateZ(new_val.doubleValue() * cameraDistance);
            }
        });

        return slider;
    }

    private Slider buildFieldOfViewSlider()
    {
        Slider slider = new Slider();
        slider.setMin(0.25);
        slider.setMax(1.75);
        slider.setValue(1);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(0.5);
        slider.setMinorTickCount(5);
        slider.setBlockIncrement(0.05);

        slider.valueProperty().addListener(new ChangeListener<Number>()
        {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val)
            {
                camera.setFieldOfView(new_val.doubleValue() * cameraFieldOfView);
            }
        });

        return slider;
    }

    private CheckBox buildShowAxesCheckBox()
    {
        CheckBox cb = new CheckBox();
        cb.setText("Show Axes");
        cb.setSelected(true);

        cb.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            public void changed(ObservableValue<? extends Boolean> ov,
                                Boolean old_val, Boolean new_val)
            {
                axisGroup.setVisible(new_val);
            }
        });

        return cb;
    }

    private SubScene buildSubScene()
    {
        SubScene subScene = new SubScene(root, 900, 768, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        subScene.setFill(Color.GREY);

        return subScene;
    }

    private void buildVisualization(Button button)
    {
        button.setOnAction(
            new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(final ActionEvent e)
            {
                if (reader.getPoints() == null)
                {
                    Stage dialog = new Stage();
                    //dialog.initStyle(StageStyle.TRANSPARENT);
                    Scene scene = new Scene(new Group(new Text(100, 50, "Hello World!")));
                    dialog.setScene(scene);
                    dialog.show();
                }
                else
                {
                    reset();
                    pointsList = reader.getPoints();

                    ScaleConfiguration sc = new ScaleConfiguration(pointsList, MAX_ABS_COORDINATE);

                    scaleFactor = sc.getScaleFactor();
                    sphereRadius = sc.getRadius();
                    cameraDistance = sc.getCameraDistance();
                    cameraFieldOfView = sc.getFieldOfView();

                    buildCamera();
                    buildAxes();
                    buildPoints();
                }
            }
        });
    }

    private void reset()
    {
        Group root = new Group();
        Xform axisGroup = new Xform();
        Xform pointGroup = new Xform();
        Xform space = new Xform();
        PerspectiveCamera camera = new PerspectiveCamera(true);
        Xform cameraXform = new Xform();

        root.getChildren().add(space);
        root.setDepthTest(DepthTest.ENABLE);
    }

    @Override
    public void start(Stage primaryStage)
    {
        BorderPane borderPane = new BorderPane();
        root.getChildren().add(space);
        root.setDepthTest(DepthTest.ENABLE);
        stage = primaryStage;

        String filename = "data.PCD";

        reader = new dataReader(filename);
        pointsList = reader.getPoints();

        ScaleConfiguration sc = new ScaleConfiguration(pointsList, MAX_ABS_COORDINATE);

        scaleFactor = sc.getScaleFactor();
        sphereRadius = sc.getRadius();
        cameraDistance = sc.getCameraDistance();
        cameraFieldOfView = sc.getFieldOfView();

        buildCamera();
        buildAxes();
        buildPoints();

        borderPane.setCenter(buildSubScene());
        borderPane.setLeft(buildLeftVbox(stage));
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


