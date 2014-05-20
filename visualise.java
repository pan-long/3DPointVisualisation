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
import java.util.List;
import java.util.ArrayList;
import javafx.scene.SubScene;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.geometry.Insets;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.stage.Modality;
import javafx.scene.control.TextField;
import javafx.scene.CacheHint;

public class visualise extends Application
{
    final Group root = new Group();
    final Xform axisGroup = new Xform();
    private Xform pointGroup = new Xform();
    final Xform space = new Xform();
    final PerspectiveCamera camera = new PerspectiveCamera(true);
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();

    private static final double CAMERA_INITIAL_X_ANGLE = 190.0;
    private static final double CAMERA_INITIAL_Y_ANGLE = 150.0;
    private static final double CAMERA_NEAR_CLIP       = 0.1;
    private static final double CAMERA_FAR_CLIP        = 10000;
    private static final double MAX_ABS_COORDINATE     = 10;
    private static final double HYDROGEN_ANGLE         = 104.5;
    private static final double MOUSE_SPEED            = 0.1;
    private static final double ROTATION_SPEED         = 2.0;
    private static final double TRACK_SPEED            = 0.3;

    private double cameraDistance = -40;
    private double cameraFieldOfView = 35;
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
    private double[] originCenter, currentCenter;

    private List<point> pointsList   = null;
    private List<Sphere> spheresList = null;

    private Stage stage           = null;
    private LeftVBox leftVBox     = null;
    private Box xAxis             = null;
    private Box yAxis             = null;
    private Box zAxis             = null;
    private dataReader reader     = null;
    private ScaleConfiguration sc = null;

    private void buildCamera()
    {
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        cameraXform3.setRotateZ(180.0);

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

                    /* space.ry.setAngle(space.ry.getAngle() - mouseDeltaX * MOUSE_SPEED * modifier * ROTATION_SPEED); */
                    /* space.rx.setAngle(space.rx.getAngle() + mouseDeltaY * MOUSE_SPEED * modifier * ROTATION_SPEED); */
                }
            }
        });
    }


    private void buildPoints()
    {
        leftVBox.updateSetOriginCheckBox(false);

        //Xform pointsXform = new Xform();
        VBox box = new VBox();
        Label labelX = new Label();
        Label labelY = new Label();
        Label labelZ = new Label();
        box.getChildren().add(labelX);
        box.getChildren().add(labelY);
        box.getChildren().add(labelZ);

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

            // Xform pointXform = new Xform();
            // pointXform.setCache(true);
            // pointXform.setCacheHint(CacheHint.SPEED);

            Sphere pointSphere = new Sphere(sphereRadius);
            pointSphere.setCache(true);
            pointSphere.setCacheHint(CacheHint.SPEED);

            spheresList.add(pointSphere);
            pointSphere.setMaterial(material);
            pointSphere.setTranslateX(p.getX() * scaleFactor);
            pointSphere.setTranslateY(p.getY() * scaleFactor);
            pointSphere.setTranslateZ(p.getZ() * scaleFactor);

            // pointsXform.getChildren().add(pointXform);
            // pointXform.getChildren().add(pointSphere);

            pointSphere.setOnMouseMoved(new EventHandler<MouseEvent>()
            {
                @Override
                public void handle(MouseEvent me)
                {
                    double[] properties = p.getProperties();
                    double[] centerOfMass = sc.getOriginalCenter();
                    double[] newCenterOfMass = sc.getCenterOfMass();

                    labelX.setText("x: " + (properties[0] + (newCenterOfMass[0] - centerOfMass[0]) / scaleFactor));
                    labelY.setText("y: " + (properties[1] + (newCenterOfMass[1] - centerOfMass[1]) / scaleFactor));
                    labelZ.setText("z: " + (properties[2] + (newCenterOfMass[2] - centerOfMass[2]) / scaleFactor));

                    pop.setX(me.getSceneX());
                    pop.setY(me.getSceneY() - pop.getHeight() / 2.0) ;

                    pop.show(stage);
                }
            });

            pointSphere.setOnMouseExited(new EventHandler<MouseEvent>()
            {
                @Override
                public void handle(MouseEvent me)
                {
                    if (pop.isShowing())
                        pop.hide();
                }
            });

            pointGroup.getChildren().add(pointSphere);
        }

        // pointGroup.getChildren().add(pointsXform);
        /* space.getChildren().addAll(pointGroup); */
    }

    private void bindListenerToCameraDistanceSlider()
    {
        Slider slider = leftVBox.getCameraDistanceSlider();

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
    }

    private void bindListenerToFieldOfViewSlider()
    {
        Slider slider = leftVBox.getFieldOfViewSlider();

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
    }

    private void bindListenerToSphereSlider()
    {
        Slider slider = leftVBox.getSphereSlider();

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
    }

    private void rebuildPoints(double x, double y, double z)
    {
        pointGroup.t.setX(pointGroup.t.getX() + x - currentCenter[0]);
        pointGroup.t.setY(pointGroup.t.getY() + y - currentCenter[1]);
        pointGroup.t.setZ(pointGroup.t.getZ() + z - currentCenter[2]);

        originCenter = currentCenter;
        currentCenter = new double[] {x, y, z};
    }

    private void bindListenersToUpdateOriginButton()
    {
        Button button = leftVBox.getUpdateButton();

        button.setOnAction(
            new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(final ActionEvent e)
            {
                if (spheresList != null)
                {
                    leftVBox.updateSetOriginCheckBox(false);

                    double[] newOrigin = leftVBox.getNewOrigin();
                    if (newOrigin != null)
                    {
                        rebuildPoints(newOrigin[0], newOrigin[1], newOrigin[2]);
                    }
                    else
                    {
                        buildAlertWindow("Please enter a valid origin and try again.");
                    }

                }
            }
        });
    }

    private CheckBox bindListenersToSetOriginCheckBox()
    {
        CheckBox cb = leftVBox.getSetOriginCheckBox();

        cb.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            public void changed(ObservableValue<? extends Boolean> ov,
                                Boolean old_val, Boolean new_val)
            {
                if (spheresList != null)
                {
                    if (new_val)
                    {
                        rebuildPoints(0, 0, 0);
                    }
                    else
                    {
                        rebuildPoints(originCenter[0], originCenter[1], originCenter[2]);
                    }
                }
            }
        });

        return cb;
    }

    private void bindListenersToFileChooser()
    {
        Text fileNameLabel = leftVBox.getFileNameLabel();
        Button openButton = leftVBox.getOpenButton();
        FileChooser fileChooser = leftVBox.getFileChooser();
        reader = new dataReader(fileChooser, openButton, fileNameLabel, stage);
    }

    private void bindListenersToUI()
    {
        bindListenerToCameraDistanceSlider();
        bindListenerToFieldOfViewSlider();
        bindListenerToSphereSlider();
        bindListenersToUpdateOriginButton();
        bindListenersToSetOriginCheckBox();
        bindListenersToShowAxesCheckBox();
        bindListenersToFileChooser();
        bindListenersToBuildButton();
    }

    private void bindListenersToShowAxesCheckBox()
    {
        CheckBox cb = leftVBox.getAxesCheckBox();

        cb.selectedProperty().addListener(new ChangeListener<Boolean>()
        {
            public void changed(ObservableValue<? extends Boolean> ov,
                                Boolean old_val, Boolean new_val)
            {
                axisGroup.setVisible(new_val);
            }
        });
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

    private void bindListenersToBuildButton()
    {
        Button button = leftVBox.getBuildButton();

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

                    scaleFactor = sc.getScaleFactor();
                    sphereRadius = sc.getRadius();
                    cameraDistance = sc.getCameraDistance();
                    cameraFieldOfView = sc.getFieldOfView();

                    /* buildCamera(); */
                    /* buildAxes(); */
                    buildPoints();

                    currentCenter = sc.getCenterOfMass();
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

        leftVBox.setCameraDistanceSliderValue(4.2);
        leftVBox.setFieldOfViewSliderValue(4.2);
        leftVBox.setSphereSliderValue(4.2);
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

        pointGroup.setCache(true);
        pointGroup.setCacheHint(CacheHint.SPEED);

        space.setCache(true);
        space.setCacheHint(CacheHint.SPEED);

        root.setCache(true);
        root.setCacheHint(CacheHint.SPEED);

        space.getChildren().addAll(pointGroup);

        borderPane.setCenter(buildSubScene());
        leftVBox = new LeftVBox();
        bindListenersToUI();
        borderPane.setLeft(leftVBox);
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


