import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
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
import java.util.ArrayList;
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
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.util.StringConverter;
import javafx.scene.control.TextField;

public class visualise extends Application
{
    final Group root = new Group();
    final Xform axisGroup = new Xform();
    final Xform pointGroup = new Xform();
    final Xform space = new Xform();
    final PerspectiveCamera camera = new PerspectiveCamera(true);
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();

    private static final double CAMERA_INITIAL_X_ANGLE = 190.0;
    private static final double CAMERA_INITIAL_Y_ANGLE = 150.0;
    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 10000;

    private static final double MAX_ABS_COORDINATE = 10;
    private static final double HYDROGEN_ANGLE = 104.5;
    private static final double MOUSE_SPEED = 0.1;
    private static final double ROTATION_SPEED = 2.0;
    private static final double TRACK_SPEED = 0.3;

    private double cameraDistance = -30;
    private double cameraFieldOfView = 45;
    private double sphereRadius;
    private double scaleFactor;
    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private double mouseDeltaX;
    private double mouseDeltaY;
    private double xAngle;
    private double yAngle;
    private double[] originCenter = new double[3];

    private List<point> pointsList = null;
    private List<Sphere> spheresList = null;

    private Stage stage = null;
    private Box xAxis, yAxis, zAxis;
    private dataReader reader = null;
    private ScaleConfiguration sc = null;
    private Slider cameraDistanceSlider = null;
    private Slider fieldOfViewSlider = null;
    private Slider sphereSlider = null;

    private void buildCamera()
    {
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(camera);
        cameraXform2.setRotateZ(180.0);

        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateZ(cameraDistance);
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);

        camera.setFieldOfView(cameraFieldOfView);
    }

    private void resetCamera()
    {
        camera.setTranslateZ(cameraDistance);
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);

        camera.setFieldOfView(cameraFieldOfView);
    }

    private void moveCamera(double newX, double newY, double newZ)
    {
        cameraXform2.t.setX(newX);
        cameraXform2.t.setY(newY);
        cameraXform2.t.setZ(newZ);
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

        xAxis = new Box(3 * MAX_ABS_COORDINATE, 0.05, 0.05);
        yAxis = new Box(0.05, 3 * MAX_ABS_COORDINATE, 0.05);
        zAxis = new Box(0.05, 0.05, 3 * MAX_ABS_COORDINATE);

        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);

        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
        axisGroup.setVisible(true);
        space.getChildren().addAll(axisGroup);
    }

    private void moveAxes(double newX, double newY, double newZ)
    {
        xAxis.setTranslateX(newX);
        xAxis.setTranslateY(newY);
        xAxis.setTranslateZ(newZ);
        yAxis.setTranslateX(newX);
        yAxis.setTranslateY(newY);
        yAxis.setTranslateZ(newZ);
        zAxis.setTranslateX(newX);
        zAxis.setTranslateY(newY);
        zAxis.setTranslateZ(newZ);

        cameraXform.setPivot(newX, newY, newZ);
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
                yAngle = cameraXform.ry.getAngle();
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

                if (me.isControlDown() && me.isPrimaryButtonDown())
                {
                    cameraXform2.t.setX(cameraXform2.t.getX() + mouseDeltaX * MOUSE_SPEED * modifier * TRACK_SPEED);
                    cameraXform2.t.setY(cameraXform2.t.getY() + mouseDeltaY * MOUSE_SPEED * modifier * TRACK_SPEED);
                }

                else if (me.isPrimaryButtonDown())
                {
                    /* System.out.println("x: " + cameraXform.rx.getAngle()); */
                    /* System.out.println("y: " + cameraXform.ry.getAngle()); */

                    if ((xAngle % 360 > 90 && xAngle % 360 < 270) || (xAngle % 360 < 0 && xAngle % 360 + 360 > 90 && xAngle % 360 + 360 < 270))
                        cameraXform.ry.setAngle(cameraXform.ry.getAngle() + mouseDeltaX * MOUSE_SPEED * modifier * ROTATION_SPEED);
                    else
                        cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX * MOUSE_SPEED * modifier * ROTATION_SPEED);

                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY * MOUSE_SPEED * modifier * ROTATION_SPEED);
                }
            }
        });
    }


    private void buildPoints()
    {
        Xform pointsXform = new Xform();
        VBox box = new VBox();
        box.setStyle("-fx-background-color: white;");

        Popup pop = new Popup();
        pop.setAutoFix(false);
        pop.setHideOnEscape(true);
        pop.getContent().addAll(box);

        if (spheresList == null)
            spheresList = new ArrayList<Sphere> ();
        else
            spheresList.clear();

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.DARKRED);
        material.setSpecularColor(Color.RED);
        for (point p : pointsList)
        {
            int[] rgb = p.parseRGB();
            if (rgb != null)
            {
                material = new PhongMaterial();
                material.setDiffuseColor(Color.rgb(rgb[0], rgb[1], rgb[2]));
                material.setSpecularColor(Color.rgb(rgb[0], rgb[1], rgb[2]));
            }

            Xform pointXform = new Xform();
            Sphere pointSphere = new Sphere(sphereRadius);
            spheresList.add(pointSphere);
            pointSphere.setMaterial(material);
            pointSphere.setTranslateX(p.getX() * scaleFactor);
            pointSphere.setTranslateY(p.getY() * scaleFactor);
            pointSphere.setTranslateZ(p.getZ() * scaleFactor);

            pointsXform.getChildren().add(pointXform);
            pointXform.getChildren().add(pointSphere);

            pointXform.setOnMouseMoved(new EventHandler<MouseEvent>()
            {
                @Override
                public void handle(MouseEvent me)
                {
                    box.getChildren().clear();
                    double[] properties = p.getProperties();
                    double[] centerOfMass = sc.getOriginalCenter();
                    double[] newCenterOfMass = sc.getCenterOfMass();

                    Label labelX = new Label("x: " + (properties[0] + (newCenterOfMass[0] - centerOfMass[0]) / scaleFactor));
                    Label labelY = new Label("y: " + (properties[1] + (newCenterOfMass[1] - centerOfMass[1]) / scaleFactor));
                    Label labelZ = new Label("z: " + (properties[2] + (newCenterOfMass[2] - centerOfMass[2]) / scaleFactor));

                    box.getChildren().add(labelX);
                    box.getChildren().add(labelY);
                    box.getChildren().add(labelZ);

                    pop.setX(me.getSceneX());
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
        /* space.getChildren().addAll(pointGroup); */
    }

    private VBox buildLeftVbox(Stage stage)
    {
        VBox vbox = new VBox();
        HBox fileHbox = new HBox();
        HBox originHbox = new HBox();
        originHbox.setSpacing(5);
        fileHbox.setSpacing(8);
        vbox.setSpacing(8);
        vbox.setPrefWidth(300);
        vbox.setPadding(new Insets(10));

        Text title = new Text("Settings");
        Text cameraDistanceLabel = new Text("Camera Distance");
        Text fieldOfViewLabel = new Text("Field of View");
        Text sphereLabel = new Text("Sphere Radius");
        Text fileNameLabel = new Text("No File Chosen");
        Text originLabel = new Text("Override Origin");
        Text x = new Text("x:");
        Text y = new Text("y:");
        Text z = new Text("z:");

        cameraDistanceSlider = buildCameraDistanceSlider();
        fieldOfViewSlider = buildFieldOfViewSlider();
        sphereSlider = buildSphereSlider();

        CheckBox axesCheckBox = buildShowAxesCheckBox();
        CheckBox setOriginCheckBox = buildSetOriginCheckBox();
        Button openButton = new Button("Choose File...");
        Button buildButton = new Button("Build");
        Button updateButton = new Button("Update");
        FileChooser fileChooser = new FileChooser();
        TextField xTextField = new TextField();
        TextField yTextField = new TextField();
        TextField zTextField = new TextField();

        xTextField.setPrefColumnCount(3);
        yTextField.setPrefColumnCount(3);
        zTextField.setPrefColumnCount(3);

        reader = new dataReader(fileChooser, openButton, fileNameLabel, stage);
        buildVisualization(buildButton);
        bindListenerToOverrideOrigin(updateButton, xTextField, yTextField, zTextField);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        openButton.setMinWidth(105);

        originHbox.getChildren().addAll(x, xTextField, y, yTextField, z, zTextField, updateButton);
        fileHbox.getChildren().addAll(openButton, fileNameLabel);
        vbox.getChildren().addAll(title, cameraDistanceLabel, cameraDistanceSlider, fieldOfViewLabel, fieldOfViewSlider,
                                  sphereLabel, sphereSlider, originLabel, originHbox, setOriginCheckBox, axesCheckBox, fileHbox, buildButton);

        return vbox;
    }

    private Slider buildCameraDistanceSlider()
    {
        Slider slider = buildSlider();

        slider.valueProperty().addListener(new ChangeListener<Number>()
        {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val)
            {
                if (new_val.doubleValue() < 4.2)
                    camera.setTranslateZ((1 / (5.2 - new_val.doubleValue())) * cameraDistance);
                else
                    camera.setTranslateZ((new_val.doubleValue() - 3.2) * cameraDistance);
            }
        });

        return slider;
    }

    private Slider buildFieldOfViewSlider()
    {
        Slider slider = buildSlider();

        slider.valueProperty().addListener(new ChangeListener<Number>()
        {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val)
            {
                if (new_val.doubleValue() < 4.2)
                    camera.setFieldOfView((1 / (5.2 - new_val.doubleValue())) * cameraFieldOfView);
                else
                    camera.setFieldOfView((new_val.doubleValue() - 3.2) * cameraFieldOfView);
            }
        });

        return slider;
    }

    private Slider buildSphereSlider()
    {
        Slider slider = buildSlider();

        slider.valueProperty().addListener(new ChangeListener<Number>()
        {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val)
            {
                if (spheresList != null)
                {
                    for (Sphere sphere : spheresList)
                    {
                        if (new_val.doubleValue() < 4.2)
                            sphere.setRadius((1 / (5.2 - new_val.doubleValue())) * sphereRadius);
                        else
                            sphere.setRadius((new_val.doubleValue() - 3.2) * sphereRadius);
                    }
                }
            }
        });

        return slider;
    }

    private Slider buildSlider()
    {
        Slider slider = new Slider();
        slider.setMin(0.2);
        slider.setMax(8.2);
        slider.setValue(4.2);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(5);
        slider.setBlockIncrement(0.2);

        slider.setLabelFormatter(new StringConverter<Double>()
        {
            @Override
            public String toString(Double n)
            {
                for (int i = 1; i < 5 ; i++)
                {
                    if (n < i)
                    {
                        return "1/" + String.valueOf(6 - i);
                    }
                }

                if (n < 5)
                {
                    return "1";
                }

                for (int i = 6; i < 10 ; i++)
                {
                    if (n < i)
                    {
                        return String.valueOf(i - 4);
                    }
                }

                return "";
            }

            @Override
            public Double fromString(String s)
            {
                return Double.valueOf(s);
            }
        });

        return slider;
    }

    private void rebuildPoints(double x, double y, double z)
    {
        reset();
        int size = pointsList.size();
        double[] center = sc.getCenterOfMass();
        originCenter = center;

        for (int i = 0; i < size; i ++)
        {
            point p = pointsList.get(i);
            double newX = p.getX() + (x - center[0]) / scaleFactor;
            double newY = p.getY() + (y - center[1]) / scaleFactor;
            double newZ = p.getZ() + (z - center[2]) / scaleFactor;
            int color = p.getRGB();
            pointsList.set(i, new point(newX, newY, newZ, color));
        }

        sc = new ScaleConfiguration(pointsList, MAX_ABS_COORDINATE);
        sc.setCenterOfMass(x, y, z);

        scaleFactor = sc.getScaleFactor();
        sphereRadius = sc.getRadius();
        cameraDistance = sc.getCameraDistance();
        cameraFieldOfView = sc.getFieldOfView();

        /* buildCamera(); */
        /* buildAxes(); */
        buildPoints();
    }

    private void bindListenerToOverrideOrigin(Button button, TextField xTextField, TextField yTextField, TextField zTextField)
    {
        button.setOnAction(
            new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(final ActionEvent e)
            {
                if (spheresList != null)
                {
                    try
                    {
                        double x = Double.parseDouble(xTextField.getText());
                        double y = Double.parseDouble(yTextField.getText());
                        double z = Double.parseDouble(zTextField.getText());
                        rebuildPoints(x, y, z);
                    }
                    catch (NumberFormatException nfe)
                    {
                        buildAlertWindow("Please enter a valid origin and try again.");
                    }
                }
            }
        });
    }

    private CheckBox buildSetOriginCheckBox()
    {
        CheckBox cb = new CheckBox("Set Origin to Center Of Mass");

        cb.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            public void changed(ObservableValue<? extends Boolean> ov,
                                Boolean old_val, Boolean new_val)
            {
                if (spheresList != null)
                {
                    if (new_val)
                    {
                        /* double[] centerOfMass = sc.calculateCenterOfMass(); */
                        /* sc.moveCenterTo(centerOfMass[0], centerOfMass[1], centerOfMass[2]); */
                        /* moveAxes(centerOfMass[0], centerOfMass[1], centerOfMass[2]); */
                        /* moveCamera(centerOfMass[0], centerOfMass[1], centerOfMass[2]); */
                        rebuildPoints(0, 0, 0);
                    }
                    else
                    {
                        /* double[] oldOrigin = sc.getOriginalCenter(); */
                        /* sc.moveCenterTo(oldOrigin[0], oldOrigin[1], oldOrigin[2]); */
                        /* moveAxes(oldOrigin[0], oldOrigin[1], oldOrigin[2]); */
                        /* moveCamera(oldOrigin[0], oldOrigin[1], oldOrigin[2]); */
                        /* sc.moveCenterTo(0, 0, 0); */
                        /* moveAxes(0, 0, 0); */
                        /* moveCamer(0, 0, 0); */
                        if (Math.abs(originCenter[0]) > 1E-9 || Math.abs(originCenter[1]) > 1E-9 || Math.abs(originCenter[2]) > 1E-9)
                            rebuildPoints(originCenter[0], originCenter[1], originCenter[2]);
                    }
                }
            }
        });

        return cb;
    }

    private CheckBox buildShowAxesCheckBox()
    {
        CheckBox cb = new CheckBox("Show Axes");
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
        SubScene subScene = new SubScene(root, 1000, 768, true, SceneAntialiasing.BALANCED);
        subScene.setCamera(camera);
        subScene.setFill(Color.GREY);

        return subScene;
    }

    private void buildAlertWindow(String alertString)
    {
        Stage dialog = new Stage();
        Scene scene = new Scene(buildAlertBox(dialog, alertString), 300, 100);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(stage);
        dialog.setFullScreen(false);
        dialog.setResizable(false);
        dialog.setScene(scene);
        dialog.show();
    }

    private VBox buildAlertBox(Stage dialog, String alertText)
    {
        VBox vbox = new VBox();
        vbox.setSpacing(20);
        vbox.setPrefWidth(200);
        vbox.setPadding(new Insets(40));
        vbox.setAlignment(Pos.CENTER);

        Text alertLabel = new Text(40, 40, alertText);
        Button cancelAlert = new Button("OK");

        alertLabel.setFill(Color.web("red"));

        cancelAlert.setOnAction(
            new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(final ActionEvent e)
            {
                dialog.close();
            }
        });

        vbox.getChildren().add(alertLabel);
        vbox.getChildren().add(cancelAlert);

        return vbox;
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
                    buildAlertWindow("Fail to open file. Only .pcd format surpported.");
                }
                else
                {
                    reset();
                    pointsList = reader.getPoints();

                    sc = new ScaleConfiguration(pointsList, MAX_ABS_COORDINATE);

                    originCenter = sc.getCenterOfMass();
                    scaleFactor = sc.getScaleFactor();
                    sphereRadius = sc.getRadius();
                    cameraDistance = sc.getCameraDistance();
                    cameraFieldOfView = sc.getFieldOfView();

                    /* buildCamera(); */
                    /* buildAxes(); */
                    buildPoints();
                }
            }
        });
    }

    private void reset()
    {
        /* root.getChildren().clear(); */
        /* axisGroup.getChildren().clear(); */
        pointGroup.getChildren().clear();
        /* space.getChildren().clear(); */
        /* cameraXform.getChildren().clear(); */
        /* cameraXform2.getChildren().clear(); */

        /* root.getChildren().add(space); */
        resetCamera();
        moveAxes(0, 0, 0);

        cameraDistanceSlider.setValue(4.2);
        fieldOfViewSlider.setValue(4.2);
        sphereSlider.setValue(4.2);
    }

    @Override
    public void start(Stage primaryStage)
    {
        BorderPane borderPane = new BorderPane();
        root.getChildren().add(space);
        root.setDepthTest(DepthTest.ENABLE);
        stage = primaryStage;

        buildAxes();
        buildCamera();
        space.getChildren().addAll(pointGroup);

        borderPane.setCenter(buildSubScene());
        borderPane.setLeft(buildLeftVbox(stage));
        //borderPane.setLeft(new LeftVBox());
        Scene scene = new Scene(borderPane, 1200, 768, true);

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
