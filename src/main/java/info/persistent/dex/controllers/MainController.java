package info.persistent.dex.controllers;

import com.android.dexdeps.DexData;
import com.android.dexdeps.DexDataException;
import com.sun.deploy.util.StringUtils;
import info.persistent.dex.Constants;
import info.persistent.dex.DexMethodCounts;
import info.persistent.dex.models.DexNode;
import info.persistent.dex.utils.DexUtils;
import info.persistent.dex.utils.UIUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created by Vasiliy on 21.11.2015
 */
public class MainController implements Initializable {

    @FXML public AnchorPane apRoot;
    @FXML public VBox vbMenuContent;
    @FXML public TabPane tpTabs;
    @FXML public Label lblDnD;
    @FXML public Pane pnTabsDrop;
    @FXML public ProgressIndicator piLoading;
    @FXML public VBox vbLoading;
    @FXML public MenuItem miOpen;
    @FXML public MenuItem miSaveAs;
    @FXML public MenuItem miExit;
    @FXML public Menu menuRecent;

    private Scene scene = null;
    private final TabClosed tabClosedListener = new TabClosed();
    //private final TreeItemClick treeItemClick = new TreeItemClick();

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        UIUtils.checkInjects(apRoot, vbMenuContent, tpTabs, lblDnD, pnTabsDrop, vbLoading, piLoading, miOpen, miSaveAs, miExit, menuRecent);
        UIUtils.bindSize(apRoot, vbMenuContent);
        UIUtils.bindSize(vbMenuContent, pnTabsDrop);
        UIUtils.bindSize(pnTabsDrop, lblDnD);
        UIUtils.bindSize(pnTabsDrop, vbLoading);
        UIUtils.bindSize(pnTabsDrop, tpTabs);
        lblDnD.setBackground(new Background(new BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        vbLoading.setBackground(new Background(new BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        lblDnD.setFont(Font.font(30));
        lblDnD.setTextFill(Color.web("#4a4a4a"));
        tpTabs.setBackground(new Background(new BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY)));
        vbMenuContent.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
        tpTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        vbLoading.setVisible(false);

        miSaveAs.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        miExit.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));
        miOpen.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        miSaveAs.setDisable(true);
        miExit.setOnAction(new MenuExit());
        miSaveAs.setOnAction(new MenuSaveAs());
        miOpen.setOnAction(new MenuOpen());

        PreferenceController prefs = PreferenceController.getInstance();
        List<String> recentFiles = Arrays.asList(prefs.getString("recent", "").split("\n"));
        updateRecentMenu(recentFiles);
    }

    public void setScene(Scene scene) {
        this.scene = scene;
        initScene();
    }

    private void initScene(){
        scene.setOnDragOver(new DragOver());
        scene.setOnDragDropped(new DragDropped());
    }

    private void updateRecentMenu(List<String> recentFiles){
        if(recentFiles.size() == 0 ){
            MenuItem item = new MenuItem("No recent files");
            item.setDisable(true);
            menuRecent.getItems().add(item);
        } else {
            for(String file : recentFiles){
                MenuItem item = new MenuItem(file);
                item.setOnAction(new MenuReopen(file));
                menuRecent.getItems().add(item);
            }
        }
    }

    private class Processing extends Thread{

        private final List<String> _files;
        private final List<String> failedFiles = new ArrayList<String>();

        public Processing(File[] files) {
            _files = DexUtils.collectFiles(files);
        }

        @Override public void run(){
            final List<Tab> tabs = new ArrayList<Tab>();
            for (String file : _files) {
                File f = new File(file);
                try {
                    RandomAccessFile raf = DexUtils.openInputFile(file);
                    DexData dexData = new DexData(raf);
                    dexData.load();
                    final DexNode dexNode = DexMethodCounts.generate(
                            dexData,
                            Constants.DEFAULT_INCLUDE_CLASSES,
                            "",
                            Constants.DEFAULT_MAX_DEPTH,
                            Constants.DEFAULT_FILTER);

                    final Tab tab = new Tab(f.getName());
                    tab.setClosable(true);
                    tabs.add(tab);
                    tab.setUserData(dexNode);
                } catch (final IOException ioe) {
                    failedFiles.add(f.getName());
                } catch (final DexDataException dde) {
                    failedFiles.add(f.getName());
                }
            }
            Platform.runLater(new Runnable() {
                @Override public void run() {
                    onPostExecute(tabs, failedFiles);
                }
            });
        }

        private void onPostExecute(List<Tab> tabs, List<String> failedFiles){
            vbLoading.setVisible(false);
            for(Tab tab : tabs) {
                fillTab((DexNode) tab.getUserData(), tab);
                tpTabs.getTabs().add(tab);
            }

            if(tpTabs.getTabs().size() > 0)
                tpTabs.getSelectionModel().select(0);
            lblDnD.setVisible(tpTabs.getTabs().size() == 0);
            miSaveAs.setDisable(tpTabs.getTabs().size() == 0);
            if (failedFiles.size() > 0) {
                StringBuilder sb = new StringBuilder("Next files was not be processed:\n");
                for(String name : failedFiles)
                    sb.append(name).append("\n");
                UIUtils.errorBox(sb.toString());
            }

            PreferenceController prefs = PreferenceController.getInstance();
            List<String> recentFiles = Arrays.asList(prefs.getString("recent", "").split("\n"));
            for(String file : _files){
                boolean isFail = false;
                for(String failedFile : failedFiles){
                    if(failedFile.equals(file)) {
                        isFail = true;
                        break;
                    }
                }
                if(!isFail){
                    recentFiles.remove(file);
                    recentFiles.add(0, file);
                }
            }
            prefs.setString("recent", StringUtils.join(recentFiles, "\n"));
            updateRecentMenu(recentFiles);

        }

        private void fillTab(DexNode dexNode, Tab tab){
            TreeItem<String> rootNode = new TreeItem<String>("Total: " + dexNode.count());
            for (String name : dexNode.childs().navigableKeySet()) {
                DexNode child = dexNode.childs().get(name);
                fillNode(rootNode, child, name);
            }
            VBox tabRoot = new VBox();
            rootNode.setExpanded(true);
            TreeView<String> treeView = new TreeView<String>(rootNode);
            UIUtils.bindSize(tabRoot, treeView);
            tabRoot.getChildren().add(treeView);
            tab.setContent(tabRoot);
            tab.setOnClosed(tabClosedListener);
            treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            //treeView.getSelectionModel().selectedItemProperty().addListener(treeItemClick);
        }

        private void fillNode(TreeItem<String> treeNode, DexNode dexNode, String name){
            TreeItem<String> branchNode = new TreeItem<String>((name.isEmpty() ? "Root" : name) + ": " + dexNode.count());
            for (String childName : dexNode.childs().navigableKeySet()) {
                DexNode child = dexNode.childs().get(childName);
                fillNode(branchNode, child, childName);
            }
            treeNode.getChildren().add(branchNode);
        }

    }


    private class MenuReopen implements EventHandler<ActionEvent> {
        private final String _file;
        public MenuReopen(String file) {
            _file = file;
        }

        @Override public void handle(ActionEvent event) {
            vbLoading.setVisible(true);
            new Processing(new File[]{new File(_file)}).start();
            System.exit(0);
        }
    }

    private class MenuExit implements EventHandler<ActionEvent> {
        @Override public void handle(ActionEvent event) {
            System.exit(0);
        }
    }

    private class MenuSaveAs implements EventHandler<ActionEvent> {
        @Override public void handle(ActionEvent event) {
            DexNode dexNode = (DexNode) tpTabs.getSelectionModel().getSelectedItem().getUserData();
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Text file", "*.*");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
            File file = fileChooser.showSaveDialog(scene.getWindow());
            if(file != null){
                try {
                    FileWriter fw = new FileWriter(file);
                    dexNode.outputToFile(fw, "");
                    fw.close();
                } catch (IOException e){
                    e.printStackTrace();
                    UIUtils.errorBox("Save failed: " + e.getMessage());
                }
            }
        }
    }

    private class MenuOpen implements EventHandler<ActionEvent> {
        @Override public void handle(ActionEvent event) {
            FileChooser fileChooser = new FileChooser();
            // Set extension filter
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("APK files", "*.*");
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

            List<File> files = fileChooser.showOpenMultipleDialog(scene.getWindow());

            if (files != null && files.size() > 0) {
                vbLoading.setVisible(true);
                //tpTabs.getTabs().clear();
                new Processing(files.toArray(new File[files.size()])).start();
            }
        }
    }

    private class TabClosed implements EventHandler<Event> {
        @Override public void handle(Event event) {
            lblDnD.setVisible(tpTabs.getTabs().size() == 0);
            miSaveAs.setDisable(tpTabs.getTabs().size() == 0);
        }
    }

    private class DragDropped implements EventHandler<DragEvent> {
        @Override public void handle(DragEvent event) {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                vbLoading.setVisible(true);
                //tpTabs.getTabs().clear();
                success = true;
                File[] files = db.getFiles().toArray(new File[db.getFiles().size()]);
                new Processing(files).start();
            }
            event.setDropCompleted(success);
            event.consume();
        }
    }

    private class DragOver implements EventHandler<DragEvent> {
        @Override public void handle(DragEvent event) {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                event.acceptTransferModes(TransferMode.ANY);
            } else {
                event.consume();
            }
        }
    }

}
