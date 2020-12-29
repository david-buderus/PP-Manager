package ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import manager.DatabaseLoader;
import manager.Language;
import manager.LanguageUtility;
import manager.Utility;
import org.apache.commons.lang.exception.ExceptionUtils;
import ui.battle.BattleView;
import ui.map.MapView;
import ui.search.SearchOverview;
import ui.utility.InconsistencyView;
import ui.utility.InfoView;
import ui.utility.MemoryView;
import ui.utility.SQLView;
import ui.utility.helper.HelperOverview;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ManagerView extends View {

    protected StringProperty fileName;

    public ManagerView(Stage stage) {
        super(stage);
        this.fileName = new SimpleStringProperty();
        this.fileName.bind(LanguageUtility.getMessageProperty("manager.noFile"));

        stage.setTitle("P&P Manager");

        TabPane root = new TabPane();

        Tab startTab = new Tab();
        startTab.textProperty().bind(LanguageUtility.getMessageProperty("settings"));
        startTab.setClosable(false);
        root.getTabs().add(startTab);

        Tab battleTab = new BattleView(this);
        root.getTabs().add(battleTab);

        Tab itemTab = new SQLView(this);
        root.getTabs().add(itemTab);

        Tab searchOverviewTab = new SearchOverview(this);
        root.getTabs().add(searchOverviewTab);

        Tab helpOverviewTab = new HelperOverview(this);
        root.getTabs().add(helpOverviewTab);

        MemoryView memoryTab = new MemoryView(this);
        Utility.memoryView = memoryTab;
        root.getTabs().add(memoryTab);

        Tab mapTab = new MapView(this);
        root.getTabs().add(mapTab);

        Tab inconsistencyTab = new InconsistencyView(this);
        root.getTabs().add(inconsistencyTab);


        GridPane settingsPane = new GridPane();
        settingsPane.setPadding(new Insets(20, 20, 20, 20));
        settingsPane.setAlignment(Pos.CENTER);
        settingsPane.setHgap(10);
        settingsPane.setVgap(5);

        ColumnConstraints textColumn = new ColumnConstraints(250);
        textColumn.setFillWidth(true);
        settingsPane.getColumnConstraints().add(textColumn);
        ColumnConstraints settingsColumn = new ColumnConstraints(250);
        settingsColumn.setFillWidth(true);
        settingsPane.getColumnConstraints().add(settingsColumn);

        startTab.setContent(settingsPane);

        Label fileText = new Label();
        fileText.textProperty().bindBidirectional(fileName);
        settingsPane.add(fileText, 0, 0);

        Button loadButton = new Button();
        loadButton.textProperty().bind(LanguageUtility.getMessageProperty("load"));
        loadButton.setMaxWidth(Double.MAX_VALUE);
        loadButton.setOnAction(ev -> load());
        settingsPane.add(loadButton, 1, 0);

        Label languageText = new Label();
        languageText.textProperty().bind(LanguageUtility.getMessageProperty("language"));
        settingsPane.add(languageText, 0, 1);

        ComboBox<Language> languageBox = new ComboBox<>();
        languageBox.setMaxWidth(Double.MAX_VALUE);
        languageBox.setItems(FXCollections.observableArrayList(Language.values()));
        languageBox.getSelectionModel().select(LanguageUtility.language.get());
        LanguageUtility.language.bind(languageBox.getSelectionModel().selectedItemProperty());
        settingsPane.add(languageBox, 1, 1);


        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    private InfoView info;

    private void load() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().addAll(
                new ExtensionFilter("Accessdatei", "*.accdb"),
                new ExtensionFilter("Alle Dateien", "*.*"));
        File file = chooser.showOpenDialog(stage);

        if (file == null) {
            return;
        }

        load(file);
    }

    public void load(File file) {
        this.info = new InfoView("loadingError");
        this.fileName.bind(LanguageUtility.getMessageProperty("loading"));

        Service<Object> service = new Service<>() {
            @Override
            protected Task<Object> createTask() {
                return new Task<>() {
                    @Override
                    protected Object call() {

                        try (Connection connection = DriverManager.getConnection("jdbc:ucanaccess://" + file.getPath())) {
                            info.addAll(DatabaseLoader.loadDatabase(connection));
                        } catch (SQLException e) {
                            info.add(ExceptionUtils.getFullStackTrace(e));
                            this.cancel();
                        } catch (Exception e) {
                            info.add(ExceptionUtils.getFullStackTrace(e));
                            e.printStackTrace();
                        }
                        return null;
                    }
                };
            }
        };
        service.setOnSucceeded(ev -> {
            this.fileName.unbind();
            this.fileName.set(file.getName());
            if (!info.isEmpty()) {
                info.show();
            }
        });
        service.setOnCancelled(ev -> {
            this.fileName.bind(LanguageUtility.getMessageProperty("fileNotLoaded"));
            if (!info.isEmpty())
                info.show();
        });
        service.start();
    }
}
