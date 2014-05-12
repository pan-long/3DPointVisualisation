import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import java.util.List;
import java.util.Scanner;

public class visualise extends Application{
    final Group root = new Group();
    final Xform axisGroup = new Xform();
    final Xform pointGroup = new Xform();
    final Xform space = new Xform();
    final PerspectiveCamera camera = new PerspectiveCamera(true);
    final Xform cameraXform = new Xform();

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

    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private double mouseDeltaX;
    private double mouseDeltaY;

    private void buildCamera(){
        root.getChildren().add(cameraXform);

        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);

        camera.setFieldOfView(1);
    }

    private void buildAxes(){
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);

        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);

        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);

        final Box xAxis = new Box(AXIS_LENGTH, 1, 1);
        final Box yAxis = new Box(1, AXIS_LENGTH, 1);
        final Box zAxis = new Box(1, 1, AXIS_LENGTH);

        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);

        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
        axisGroup.setVisible(false);
        space.getChildren().addAll(axisGroup);
    }

    private void handleMouse(Scene scene, final Node root){
        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me){
                mousePosX = me.getSceneX();
                mousePosX = me.getSceneY();
                mouseOldX = me.getSceneX();
                mouseOldY = me.getSceneY();
            }
        });

        scene.setOnMouseDragged(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent me){
                mouseOldX = mousePosX;
                mouseOldY = mouseOldY;
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseDeltaX = mousePosX - mouseOldX;
                mouseDeltaY = mousePosY - mouseOldY;

                double modifier = 1.0;

                if (me.isPrimaryButtonDown()){
                    cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX*MOUSE_SPEED*modifier*ROTATION_SPEED);
                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() - mouseDeltaY*MOUSE_SPEED*modifier*ROTATION_SPEED);
                }
            }
        });
    }

    private void buildPoints(List<point> points){
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);

        Xform pointsXform = new Xform();
        
        for (point p : points){
            Xform pointXform = new Xform();
            Sphere pointSphere = new Sphere(1.0);
            pointSphere.setMaterial(redMaterial);
            pointSphere.setTranslateX(p.getX());
            pointSphere.setTranslateY(p.getY());
            pointSphere.setTranslateZ(p.getZ());

            pointsXform.getChildren().add(pointXform);
            pointXform.getChildren().add(pointSphere);
        }

        pointGroup.getChildren().add(pointsXform);
        space.getChildren().addAll(pointGroup);
    }

    @Override
        public void start(Stage primaryStage){
            root.getChildren().add(space);
            root.setDepthTest(DepthTest.ENABLE);

            /* Scanner sr = new Scanner(System.in); */
            /* String filename = sr.next(); */
            String filename = "data.PCD";

            dataReader dr = new dataReader(filename);

            List<point> pointsList = dr.getPoints();

            buildCamera();
            buildAxes();
            buildPoints(pointsList);

            Scene scene = new Scene(root, 1024, 768, true);
            scene.setFill(Color.GREY);
            handleMouse(scene, space);

            primaryStage.setTitle("3D Point Visualisation");
            primaryStage.setScene(scene);
            primaryStage.show();

            scene.setCamera(camera);
        }

    public static void main(String[] args){
        launch(args);
    }
}
