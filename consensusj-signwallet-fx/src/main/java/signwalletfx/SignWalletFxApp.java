package signwalletfx;

import com.github.sarxos.webcam.Webcam;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.consensusj.airgap.fx.camera.CameraService;
import org.consensusj.supernautfx.FxmlLoaderFactory;
import org.consensusj.supernautfx.SupernautFxApp;
import org.consensusj.supernautfx.SupernautFxLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeoutException;

/**
 * Main class for Signing Wallet FX that uses SupernautFx and therefore does not have
 * to subclass javafx.application.Application.
 */
@Singleton
public class SignWalletFxApp implements SupernautFxApp {
    private static final int GET_WEBCAMS_TIMEOUT = 1000;   // Timeout in milliseconds

    private static final Logger log = LoggerFactory.getLogger(SignWalletFxApp.class);
    private static final String APP_NAME = "ConsensusJ Signing Wallet";
    static final String mainFxmlResName = "main.fxml";
    static final String mainCssResName = "signer.css";

    private final FxmlLoaderFactory loaderFactory;

    public CameraService cameraService;
    private Stage primaryStage;
    private SignWalletFxMainWindowController mainWindowController;

    public SignWalletFxApp(FxmlLoaderFactory loaderFactory) {
        this.loaderFactory = loaderFactory;
    }

    @Override
    public void init() {
        // note this is in init as it **must not** be called on the FX Application Thread:
        Webcam camera = null;
        try {
            camera = Webcam.getWebcams(GET_WEBCAMS_TIMEOUT).get(0);
        } catch (TimeoutException toex) {
            log.warn("No Webcam found within {} ms", GET_WEBCAMS_TIMEOUT);
        }
        if (camera != null) {
            cameraService = new CameraService(camera);
        }
    }

    @Override
    public void start(final Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        primaryStage.setTitle(APP_NAME);
        mainWindowController = startMainWindow(primaryStage, mainFxmlResName, mainCssResName);
        primaryStage.show();
    }

    public SignWalletFxMainWindowController startMainWindow(Stage primaryStage, String fxmlName, String cssName) throws IOException {
        // Load the GUI. The MainWindowController class will be automagically created and wired up.
        // Note that the location URL returned from getResource() will be in the package of the concrete subclass
        URL location = getClass().getResource(fxmlName);
        FXMLLoader loader = getFxmlLoaderFactory().get(location);
        Pane mainUI = loader.load();
        SignWalletFxMainWindowController mainWindowController = loader.getController();

        Scene scene = mainWindowController.controllerStart(mainUI, cssName);
        primaryStage.setScene(scene);

        return mainWindowController;
    }

    public FxmlLoaderFactory getFxmlLoaderFactory() {
        return loaderFactory;
    }
    
    public static void main(String[] args) {
        SupernautFxLauncher.superLaunch(SignWalletFxApp.class, args);
    }
}
