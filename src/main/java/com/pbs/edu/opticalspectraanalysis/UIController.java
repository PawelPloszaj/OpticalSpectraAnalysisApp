package com.pbs.edu.opticalspectraanalysis;


import com.opencsv.CSVWriter;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.math.RoundingMode;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.dialog.ProgressDialog;
import java.math.BigDecimal;
import java.util.Properties;

import static com.pbs.edu.opticalspectraanalysis.Notifications.*;
import static javafx.scene.paint.Color.rgb;

import com.zaxxer.hikari.*;

public class UIController {
    File selectedFile;
    Stage stage;
    static int numberOfIndexes;
    @FXML private Button SelectFile;
    @FXML private Label FilePathLabel;
    @FXML private Button UploadFile;
    @FXML private ComboBox<String> ChooseData;
    @FXML private Button CalculateButton;
    @FXML private Spinner<Integer> spinner_maxes;
    @FXML private Spinner<Double> spinner_wl_start;
    @FXML private Spinner<Double> spinner_wl_stop;
    @FXML private Spinner<Double> spinner_n;
    @FXML private TableView ResultsTableView;
    private final ObservableList<ObservableList> results_data = FXCollections.observableArrayList();
    @FXML private MenuItem DownloadResultsPDF;
    @FXML private MenuItem DownloadResultsCSV;
    @FXML private MenuItem ShowChart;
    @FXML private MenuItem DownloadChart;
    @FXML private MenuItem ShowChartMaxAndMins;
    @FXML private MenuItem DownloadChartMaxAndMins;
    @FXML private MenuItem DeleteRecordResults;
    @FXML private Tab UploadDataGroup;
    @FXML private Tab CalculateResultsGroup;
    @FXML private Tab HistoryGroup;
    @FXML private Label spinner_maxes_label;
    @FXML private Label spinner_wl_start_label;
    @FXML private Label spinner_wl_stop_label;
    @FXML private TableView HistoryTableView;
    @FXML private MenuItem DownloadAsPDF;
    @FXML private MenuItem DownloadAsCSV;
    @FXML private MenuItem ShowChartUserParameters;
    @FXML private MenuItem DownloadChartUserParameters;
    @FXML private MenuItem ShowChartUserParametersMaxAndMins;
    @FXML private MenuItem DownloadChartUserParametersMaxAndMins;
    @FXML private MenuItem DeleteRecordUserParameters;
    @FXML private TableView HistoryTableViewFiles;
    @FXML private MenuItem DownloadAsPDF_2;
    @FXML private MenuItem DownloadAsCSV_2;
    @FXML private MenuItem ShowChartUserParameters_2;
    @FXML private MenuItem DownloadChartUserParameters_2;
    @FXML private MenuItem ShowChartUserParametersMaxAndMins_2;
    @FXML private MenuItem DownloadChartUserParametersMaxAndMins_2;
    @FXML private TableView DataTableView;
    @FXML private MenuItem DeleteRecord;
    @FXML private Button SelectDownloadPath;
    @FXML private Label CurrentDownloadPath;
    @FXML private ComboBox<String> ChooseMethod;

    // Define variable that stores Path where we download charts and results as pdf or csv
    String SavePath;

    /* Create Database Connection Instance */
    static Properties properties = new Properties();
    static InputStream inputStream = UIController.class.getClassLoader().getResourceAsStream("hikari.properties");
    static HikariConfig config;

    static {
        try {
            properties.load(inputStream);
            config = new HikariConfig(properties);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static HikariDataSource dataSource = new HikariDataSource(config);

    public static HikariDataSource get_data_source() {
        return dataSource;
    }

    /* </Database connection> */

    /* Define Functions */
    public void UpdateTableViewData(TableView tableView, String SQL, boolean results_session_data) {
        // Create an ObservableList to store the rows of data that will be displayed in the TableView
        ObservableList<ObservableList> history_data = FXCollections.observableArrayList();
        try (Connection connection = dataSource.getConnection()) {
            // Execute an SQL query to select all rows
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(SQL);

            // Clear the columns of the TableView and the history_data list
            tableView.getColumns().clear();
            history_data.clear();

            // Iterate through the columns in the result set, creating a TableColumn for each one
            for(int i=0 ; i<resultSet.getMetaData().getColumnCount(); i++){
                final int j = i;
                TableColumn col = new TableColumn(resultSet.getMetaData().getColumnName(i+1));
                // Set the cell value factory for the TableColumn to a callback that returns the value of the cell as an ObservableValue
                col.setCellValueFactory((Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>) param ->
                        new SimpleStringProperty(param.getValue().get(j).toString()));
                tableView.getColumns().addAll(col);
            }

            // Iterate through the rows of the result set, adding each row to the history_data list as an ObservableList of cells
            while(resultSet.next()) {
                ObservableList<String> row = FXCollections.observableArrayList();
                for(int i=1; i<=resultSet.getMetaData().getColumnCount(); i++) {
                    row.add(resultSet.getString(i));
                }
                if(results_session_data) {
                    results_data.add(row);
                } else {
                    history_data.add(row);
                }
            }

            // Set the history_data list as the items for the TableView, causing the data to be displayed in the table
            if(results_session_data) {
                tableView.setItems(results_data);
            } else {
                tableView.setItems(history_data);
            }
        }catch(Exception e) {

        }
    }

    public void setStage(Stage stageRef) {
        stage = stageRef;
    }

    private void SetComboBoxData() {
        ChooseData.getItems().clear();
        List<String> items = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM dane ORDER BY id_dane DESC";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while(resultSet.next()) {
                String name = resultSet.getString("nazwa_pliku");
                items.add(name);
            }
        } catch(SQLException exception) {
            setNotificationConnectionToDatabaseFailed();
        }
        ChooseData.getItems().setAll(items);
        ChooseData.getSelectionModel().selectFirst();
        ChooseData.setOnAction(e -> refresh());

        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT MAX(id_dane) FROM dane";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while(resultSet.next()) {
                numberOfIndexes =  resultSet.getInt("MAX(id_dane)");
            }
        } catch(SQLException exception) {
            setNotificationConnectionToDatabaseFailed();
        }
    }

    private void SetComboBoxDataAfterUpload(String filename) {
        ChooseData.getItems().clear();
        List<String> items = new ArrayList<>();
        items.add(filename);
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM dane ORDER BY id_dane DESC";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while(resultSet.next()) {
                String name = resultSet.getString("nazwa_pliku");
                items.add(name);
            }
        } catch(SQLException exception) {
            setNotificationConnectionToDatabaseFailed();
            items.clear();
        }
        ChooseData.getItems().setAll(items);
        ChooseData.getSelectionModel().selectFirst();

        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT MAX(id_dane) FROM dane";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while(resultSet.next()) {
                numberOfIndexes =  resultSet.getInt("MAX(id_dane)") + 1;
            }
        } catch(SQLException exception) {
            setNotificationConnectionToDatabaseFailed();
        }
    }

    private void DownloadFile(String link, String fileName, String extension) throws IOException {
        URL url = new URL(link);
        String downloadFileLocation = SavePath + fileName + extension;
        InputStream inputStream = url.openStream();
        OutputStream fileOutputStream = new FileOutputStream(downloadFileLocation);
        int ch;
        while ((ch = inputStream.read()) != -1) {
            fileOutputStream.write(ch);
        }
        inputStream.close();
        fileOutputStream.close();
        if(extension == ".jpg") {
            Notifications.setNotificationDownloadComplete(downloadFileLocation);
        } else if(extension == ".pdf") {
            Notifications.setNotificationDownloadResultsPDFComplete(downloadFileLocation);
        }
    }

    void refresh_calculate_group() {
        int chooseMethod_index = ChooseMethod.getSelectionModel().getSelectedIndex();
        if(chooseMethod_index == 0) {
            // Enable needed fields

            // Disable the rest
            spinner_maxes.setDisable(true);
            spinner_maxes.setVisible(false);
            spinner_wl_start.setDisable(true);
            spinner_wl_start.setVisible(false);
            spinner_wl_stop.setDisable(true);
            spinner_wl_stop.setVisible(false);
            spinner_maxes_label.setDisable(true);
            spinner_maxes_label.setVisible(false);
            spinner_wl_start_label.setDisable(true);
            spinner_wl_start_label.setVisible(false);
            spinner_wl_stop_label.setDisable(true);
            spinner_wl_stop_label.setVisible(false);
        } else if(chooseMethod_index == 1) {
            // Enable needed fields
            spinner_maxes.setDisable(false);
            spinner_maxes.setVisible(true);
            spinner_maxes_label.setDisable(false);
            spinner_maxes_label.setVisible(true);

            // Disable the rest
            spinner_wl_start.setDisable(true);
            spinner_wl_start.setVisible(false);
            spinner_wl_stop.setDisable(true);
            spinner_wl_stop.setVisible(false);
            spinner_wl_start_label.setDisable(true);
            spinner_wl_start_label.setVisible(false);
            spinner_wl_stop_label.setDisable(true);
            spinner_wl_stop_label.setVisible(false);
        } else if(chooseMethod_index == 2) {
            // Enable needed fields
            spinner_wl_start.setDisable(false);
            spinner_wl_start.setVisible(true);
            spinner_wl_stop.setDisable(false);
            spinner_wl_stop.setVisible(true);
            spinner_wl_start_label.setDisable(false);
            spinner_wl_start_label.setVisible(true);
            spinner_wl_stop_label.setDisable(false);
            spinner_wl_stop_label.setVisible(true);

            // Disable the rest
            spinner_maxes_label.setDisable(true);
            spinner_maxes_label.setVisible(false);
            spinner_maxes.setDisable(true);
            spinner_maxes.setVisible(false);
        }
    }

    void refresh() {
        int id_dane = numberOfIndexes - ChooseData.getSelectionModel().getSelectedIndex();
        int ilosc_maximow = 0;
        double START_WL = 0;
        double STOP_WL = 0;
        double SMPLINTVL = 0;
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM dane WHERE id_dane = " + id_dane;
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while(resultSet.next()) {
                START_WL = resultSet.getDouble("START_WL");
                STOP_WL = resultSet.getDouble("STOP_WL");
                SMPLINTVL = resultSet.getDouble("SMPLINTVL");
                ilosc_maximow = resultSet.getInt("ilosc_maximow");
            }
        } catch(SQLException exception) {
            Notifications.setNotificationConnectionToDatabaseFailed();
        }
        int maxes_val = 1;
        if(ilosc_maximow == 0) { maxes_val = 0; }
        spinner_maxes.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(
                        maxes_val,
                        ilosc_maximow,
                        ilosc_maximow
                )
        );
        spinner_wl_start.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(
                        START_WL,
                        STOP_WL,
                        START_WL,
                        SMPLINTVL
                )
        );
        spinner_wl_stop.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(
                        START_WL,
                        STOP_WL,
                        STOP_WL,
                        SMPLINTVL
                )
        );
        spinner_n.setValueFactory(
                new SpinnerValueFactory.DoubleSpinnerValueFactory(
                        0,
                        10,
                        1,
                        0.1
                )
        );

        spinner_maxes.setEditable(true);

        int finalIlosc_maximow = ilosc_maximow;
        StringConverter<Integer> integerStringConverter = spinner_maxes.getValueFactory().getConverter();
        StringConverter<Integer> integerStringConverter2 = new StringConverter<>() {
            @Override
            public Integer fromString(String value) {
                try {
                    return integerStringConverter.fromString(value);
                } catch (NumberFormatException numberFormatException) {
                    return finalIlosc_maximow;
                }
            }

            @Override
            public String toString(Integer value) {
                return integerStringConverter.toString(value);
            }
        };
        spinner_maxes.getValueFactory().setConverter(integerStringConverter2);


        spinner_wl_start.setEditable(true);

        double finalSTART_WL = START_WL;
        double finalSTOP_WL = STOP_WL;
        double finalSMPLINTVL = SMPLINTVL;
        StringConverter<Double> doubleStringConverter_WL_START = spinner_wl_start.getValueFactory().getConverter();
        StringConverter<Double> doubleStringConverter_WL_START_2 = new StringConverter<>() {
            @Override
            public Double fromString(String value) {
                try {
                    return doubleStringConverter_WL_START.fromString(value);
                } catch (RuntimeException runtimeException) {
                    return finalSTART_WL;
                }
            }

            @Override
            public String toString(Double value) {
                return doubleStringConverter_WL_START.toString(value);
            }
        };
        spinner_wl_start.getValueFactory().setConverter(doubleStringConverter_WL_START_2);

        spinner_wl_stop.setEditable(true);
        StringConverter<Double> doubleStringConverter_WL_STOP = spinner_wl_stop.getValueFactory().getConverter();
        StringConverter<Double> doubleStringConverter_WL_STOP_2 = new StringConverter<>() {
            @Override
            public Double fromString(String value) {
                try {
                    return doubleStringConverter_WL_STOP.fromString(value);
                } catch (RuntimeException re) {
                    return finalSTOP_WL;
                }
            }

            @Override
            public String toString(Double value) {
                return doubleStringConverter_WL_STOP.toString(value);
            }
        };
        spinner_wl_stop.getValueFactory().setConverter(doubleStringConverter_WL_STOP_2);

        spinner_wl_start.getValueFactory().valueProperty().addListener(e ->
                spinner_wl_stop.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(
                        spinner_wl_start.getValue(),
                        finalSTOP_WL,
                        finalSTOP_WL,
                        finalSMPLINTVL
                ))
        );

        spinner_wl_start.getValueFactory().valueProperty().addListener(e ->
                spinner_wl_stop.getValueFactory().setConverter(doubleStringConverter_WL_STOP_2)
        );

        spinner_n.setEditable(true);
        StringConverter<Double> doubleStringConverter_n = spinner_n.getValueFactory().getConverter();
        StringConverter<Double> doubleStringConverter_n_2 = new StringConverter<>() {
            @Override
            public Double fromString(String value) {
                try {
                    return doubleStringConverter_n.fromString(value);
                } catch (RuntimeException re) {
                    return 1.0;
                }
            }

            @Override
            public String toString(Double value) {
                return doubleStringConverter_n.toString(value);
            }
        };
        spinner_n.getValueFactory().setConverter(doubleStringConverter_n_2);

    }

    /* </Define Functions> */

    @FXML
    public void initialize() {
        /* Initial Setup */
        // Create instance of our class Configuration letting us access functions later on
        Configuration configuration = new Configuration();

        // Set Path to download data
        SavePath = configuration.getSavePathValue();

        // Create list that contains names for our method selection ComboBox then set it and select first on the list
        List<String> items = Arrays.asList("File Data","Maxes Amount","Wave Length");
        ChooseMethod.getItems().setAll(items);
        ChooseMethod.getSelectionModel().selectFirst();

        // After choosing different method from the list, change fields that we need to calculate results
        ChooseMethod.setOnAction(e -> refresh_calculate_group());

        // Set ComboBox data and choose first record
        SetComboBoxData();

        // Inital set limitations for our spinners that depends on chosen data set from ChooseMethod ComboBox
        refresh();

        // Disable button because we want to let user upload only when he chose correct file first
        UploadFile.setDisable(true);

        // Create FileChooser for SelectFile button to let user choose only CSV file with data to upload
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv", "*.CSV")
        );

        // Build data for all files table
        UpdateTableViewData(DataTableView, "SELECT * from dane ORDER BY id_dane DESC", false);

        // Show actual Path as text on label
        CurrentDownloadPath.setText(SavePath);

        // Set SelectionMode for TableViews
        ResultsTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        HistoryTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        HistoryTableViewFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        /* End of initial setup */

        /* Connect Frontend with Backend */

        SelectFile.setOnAction(e -> {
            selectedFile = fileChooser.showOpenDialog(stage);
            if(selectedFile != null) {
                FilePathLabel.setText(selectedFile.toString());
                fileChooser.setInitialDirectory(new File(selectedFile.getAbsoluteFile().getParent()));
                UploadFile.setDisable(false);
            } else {
                FilePathLabel.setText("");
                UploadFile.setDisable(true);
            }
        });

        UploadFile.setOnAction(e -> {
            UploadFile.setDisable(true);
            String filename = selectedFile.toPath().getFileName().toString();
            SetComboBoxDataAfterUpload(filename);
            ImportCSV importCSV = new ImportCSV(selectedFile.toPath());
            ProgressDialog progressDialog = new ProgressDialog(importCSV);
            progressDialog.getDialogPane().setStyle("-fx-base:rgba(60, 63, 65, 255)");
            progressDialog.setContentText("Uploading data from file " + selectedFile.toPath().getFileName().toString());
            progressDialog.setTitle("Data Upload In Progress");
            progressDialog.setHeaderText("Data is Uploading ...");
            progressDialog.initModality(Modality.NONE);
            progressDialog.setOnCloseRequest(dialogEvent -> {
                setNotificationTransferToDatabaseComplete(filename);
                SetComboBoxData();
                UpdateTableViewData(DataTableView, "SELECT * from dane ORDER BY id_dane DESC", false);
                refresh();
            });
            Thread th = new Thread(importCSV);
            th.setDaemon(true);
            th.start();
            FilePathLabel.setText("");
            progressDialog.showAndWait();
        });

        ShowChart.setOnAction(e -> {
            Object row = ResultsTableView.getSelectionModel().getSelectedItems().get(0);
            String id_wynik_p = row.toString().split(",")[0].substring(1); //id_wynik_p of selected record
            int id_dane = 0;
            double WL_START = 0;
            double WL_STOP = 0;
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM wyniki_user_parameters WHERE id_wynik_p = " + id_wynik_p;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    id_dane = resultSet.getInt("id_dane");
                    WL_START = resultSet.getDouble("WL_START");
                    WL_STOP = resultSet.getDouble("WL_STOP");
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            }
            String link = "http://pawelploszaj.pl/wykres.php?id_dane="+ id_dane + "&WL_START=" + WL_START + "&WL_STOP=" + WL_STOP;
            Image img = new Image(link);
            ImageView imageView = new ImageView(img);
            StackPane secondaryLayout = new StackPane();
            secondaryLayout.getChildren().add(imageView);
            Scene secondScene = new Scene(secondaryLayout, 600, 400);
            secondScene.getRoot().setStyle("-fx-base:rgba(60, 63, 65, 255)");
            Stage newWindow = new Stage();
            newWindow.setTitle("Chart - id_wynik_p=" + id_wynik_p);
            newWindow.setScene(secondScene);
            newWindow.setResizable(false);
            newWindow.initModality(Modality.NONE);
            newWindow.setX(stage.getX() + 200);
            newWindow.setY(stage.getY() + 100);
            newWindow.show();
        });

        DownloadChart.setOnAction(e -> {
            int i = Integer.parseInt(null);
            int a = i + 2;
            Object row = ResultsTableView.getSelectionModel().getSelectedItems().get(0);
            String id_wynik_p = row.toString().split(",")[0].substring(1); //id_wynik_p of selected record
            int id_dane = 0;
            double WL_START = 0;
            double WL_STOP = 0;
            String time = "";
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM wyniki_user_parameters WHERE id_wynik_p = " + id_wynik_p;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    id_dane = resultSet.getInt("id_dane");
                    WL_START = resultSet.getDouble("WL_START");
                    WL_STOP = resultSet.getDouble("WL_STOP");
                    time = String.valueOf(resultSet.getTimestamp("datetime")).replaceAll(":","-");
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            }
            String link = "http://pawelploszaj.pl/wykres_download.php?id_dane="+ id_dane + "&WL_START=" + WL_START + "&WL_STOP=" + WL_STOP;
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM dane WHERE id_dane = " + id_dane;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    String name = resultSet.getString("nazwa_pliku");
                    String fileName = File.separator + name + "-" + time;
                    DownloadFile(link, fileName, ".jpg");
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            } catch (IOException ex) {
                Notifications.setNotificationDownloadFailed();
                ex.printStackTrace();
            }
        });

        DownloadResultsPDF.setOnAction(e -> {
            Object row2 = ResultsTableView.getSelectionModel().getSelectedItems().get(0);
            String id_wynik_p = row2.toString().split(",")[0].substring(1); //id_wynik_p of selected record
            String link = "http://pawelploszaj.pl/pdf_user_parameters.php?id_wynik_p[0]="+ id_wynik_p;
            boolean compare = false;
            if(ResultsTableView.getSelectionModel().getSelectedItems().stream().count() >= 2) {
                compare = true;
                int selected = (int) ResultsTableView.getSelectionModel().getSelectedItems().stream().count();
                for(int i=1;i<selected;i++) {
                    Object row = ResultsTableView.getSelectionModel().getSelectedItems().get(i);
                    String id_wynik_p_2 = row.toString().split(",")[0].substring(1);
                    link = link + "&id_wynik_p[" + i + "]=" + id_wynik_p_2;
                }
            }
            int id_dane = 0;
            String time = "";
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM wyniki_user_parameters WHERE id_wynik_p = " + id_wynik_p;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    id_dane = resultSet.getInt("id_dane");
                    time = String.valueOf(resultSet.getTimestamp("datetime")).replaceAll(":","-");
                }
                String query_2 = "SELECT * FROM dane WHERE id_dane = " + id_dane;
                Statement statement_2 = connection.createStatement();
                ResultSet resultSet_2 = statement_2.executeQuery(query_2);
                while(resultSet_2.next()) {
                    String name = resultSet_2.getString("nazwa_pliku");
                    if(compare) {
                        String fileName = File.separator + "Compare-" + name + "-" + time;
                        DownloadFile(link, fileName, ".pdf");
                    } else {
                        String fileName = File.separator + name + "-" + time;
                        DownloadFile(link, fileName, ".pdf");
                    }
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            } catch (IOException ex) {
                Notifications.setNotificationDownloadFailed();
                ex.printStackTrace();
            }
        });

        DownloadResultsCSV.setOnAction(e -> {
            Object row = ResultsTableView.getSelectionModel().getSelectedItems().get(0);
            String id_wynik_p = row.toString().split(",")[0].substring(1); //id_wynik_p of selected record
            int id_dane = 0;
            int ILOSC_MAXIMOW = 0;
            double WL_START = 0;
            double WL_STOP = 0;
            double n = 0;
            double V = 0;
            double x = 0;
            double global_max_X = 0;
            double global_max_Y = 0;
            double srednia_odl = 0;
            double odl_central_lewo = 0;
            double odl_central_prawo = 0;
            double absorpcja = 0;
            String POMIAR_NA_PODSTAWIE = "";
            String datetime = "";
            String time = "";
            String fileName = "";
            String name = "";
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM wyniki_user_parameters WHERE id_wynik_p = " + id_wynik_p;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    id_dane = resultSet.getInt("id_dane");
                    ILOSC_MAXIMOW = resultSet.getInt("ILOSC_MAXIMOW");
                    WL_START = resultSet.getDouble("WL_START");
                    WL_STOP = resultSet.getDouble("WL_STOP");
                    n = resultSet.getDouble("n");
                    V = resultSet.getDouble("V");
                    x = resultSet.getDouble("x");
                    global_max_X = resultSet.getDouble("global_max_X");
                    global_max_Y = resultSet.getDouble("global_max_Y");
                    srednia_odl = resultSet.getDouble("srednia_odl");
                    odl_central_lewo = resultSet.getDouble("odl_central_lewo");
                    odl_central_prawo = resultSet.getDouble("odl_central_prawo");
                    absorpcja = resultSet.getDouble("absorpcja");
                    POMIAR_NA_PODSTAWIE = resultSet.getString("POMIAR_NA_PODSTAWIE");
                    datetime = String.valueOf(resultSet.getTimestamp("datetime"));
                    time = String.valueOf(resultSet.getTimestamp("datetime")).replaceAll(":","-");
                }
                String query_2 = "SELECT * FROM dane WHERE id_dane = " + id_dane;
                Statement statement_2 = connection.createStatement();
                ResultSet resultSet_2 = statement_2.executeQuery(query_2);
                while(resultSet_2.next()) {
                    name = resultSet_2.getString("nazwa_pliku");
                    fileName = name + "-" + time;
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            }
            String downloadFileLocation = SavePath + File.separator + fileName + ".csv";
            try (Connection connection = dataSource.getConnection()) {
                String SQL = "SELECT * from wyniki_user_parameters ORDER BY id_wynik_p DESC";
                ResultSet rs = connection.createStatement().executeQuery(SQL);
                String[] arrayList = new String[rs.getMetaData().getColumnCount()];
                String[] row2 = {name, String.valueOf(id_dane), String.valueOf(ILOSC_MAXIMOW), String.valueOf(WL_START), String.valueOf(WL_STOP), String.valueOf(n), String.valueOf(V), String.valueOf(x), String.valueOf(global_max_X), String.valueOf(global_max_Y), String.valueOf(srednia_odl), String.valueOf(odl_central_lewo), String.valueOf(odl_central_prawo), String.valueOf(absorpcja),POMIAR_NA_PODSTAWIE,datetime};
                arrayList[0] = "nazwa_pliku";
                for (int i = 1; i < rs.getMetaData().getColumnCount(); i++) {
                    arrayList[i] = rs.getMetaData().getColumnName(i + 1);
                }
                List<String[]> csvData = new ArrayList<>();
                csvData.add(arrayList);
                csvData.add(row2);
                try (CSVWriter writer = new CSVWriter(new FileWriter(downloadFileLocation))) {
                    writer.writeAll(csvData);
                    setNotificationDownloadResultsCSVComplete(downloadFileLocation);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    setNotificationDownloadFailed();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                setNotificationConnectionToDatabaseFailed();
            }
        });

        ShowChartMaxAndMins.setOnAction(e -> {
            Object row = ResultsTableView.getSelectionModel().getSelectedItems().get(0);
            String id_wynik_p = row.toString().split(",")[0].substring(1); //id_wynik_p of selected record
            int id_dane = 0;
            double WL_START = 0;
            double WL_STOP = 0;
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM wyniki_user_parameters WHERE id_wynik_p = " + id_wynik_p;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    id_dane = resultSet.getInt("id_dane");
                    WL_START = resultSet.getDouble("WL_START");
                    WL_STOP = resultSet.getDouble("WL_STOP");
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            }
            String link = "http://pawelploszaj.pl/wykres_max_mins.php?id_dane="+ id_dane + "&WL_START=" + WL_START + "&WL_STOP=" + WL_STOP;
            Image img = new Image(link);
            ImageView imageView = new ImageView(img);
            StackPane secondaryLayout = new StackPane();
            secondaryLayout.getChildren().add(imageView);
            Scene secondScene = new Scene(secondaryLayout, 600, 400);
            secondScene.getRoot().setStyle("-fx-base:rgba(60, 63, 65, 255)");
            Stage newWindow = new Stage();
            newWindow.setTitle("Chart - id_wynik_p=" + id_wynik_p);
            newWindow.setScene(secondScene);
            newWindow.setResizable(false);
            newWindow.initModality(Modality.NONE);
            newWindow.setX(stage.getX() + 200);
            newWindow.setY(stage.getY() + 100);
            newWindow.show();
        });

        DownloadChartMaxAndMins.setOnAction(e -> {
            Object row = ResultsTableView.getSelectionModel().getSelectedItems().get(0);
            String id_wynik_p = row.toString().split(",")[0].substring(1); //id_wynik_p of selected record
            int id_dane = 0;
            double WL_START = 0;
            double WL_STOP = 0;
            String time = "";
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM wyniki_user_parameters WHERE id_wynik_p = " + id_wynik_p;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    id_dane = resultSet.getInt("id_dane");
                    WL_START = resultSet.getDouble("WL_START");
                    WL_STOP = resultSet.getDouble("WL_STOP");
                    time = String.valueOf(resultSet.getTimestamp("datetime")).replaceAll(":","-");
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            }
            String link = "http://pawelploszaj.pl/wykres_max_mins_download.php?id_dane="+ id_dane + "&WL_START=" + WL_START + "&WL_STOP=" + WL_STOP;
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM dane WHERE id_dane = " + id_dane;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    String name = resultSet.getString("nazwa_pliku") + "-MaxAndMins";
                    String fileName = File.separator + name + "-" + time;
                    DownloadFile(link,fileName, ".jpg");
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            } catch (IOException ex) {
                Notifications.setNotificationDownloadFailed();
                ex.printStackTrace();
            }
        });

        DeleteRecordResults.setOnAction(e -> {
            Object row = ResultsTableView.getSelectionModel().getSelectedItems().get(0);
            String id_wynik_p = row.toString().split(",")[0].substring(1);
            try (Connection connection = dataSource.getConnection()) {
                String query = "DELETE FROM wyniki_user_parameters WHERE id_wynik_p = " + id_wynik_p;
                Statement statement = connection.createStatement();
                statement.execute(query);
                String query_2 = "ALTER TABLE wyniki_user_parameters AUTO_INCREMENT = 1";
                Statement statement_2 = connection.createStatement();
                statement_2.execute(query_2);
            } catch(SQLException exception) {
                Notifications.setNotificationConnectionToDatabaseFailed();
            }
            ResultsTableView.getItems().remove(row);
            setNotificationDeletedRecordSucessfully();
        });

        CalculateButton.setOnAction(e -> {
            StackPane secondaryLayout = new StackPane();
            ProgressIndicator progressIndicator = new ProgressIndicator(-1);
            Text progressText = new Text("\n\n\n\n\n\n\nCalculating results from file " + ChooseData.getSelectionModel().getSelectedItem() + "\n               Method: " + ChooseMethod.getSelectionModel().getSelectedItem());
            progressText.setFill(rgb(125,249,255));
            secondaryLayout.getChildren().add(progressIndicator);
            secondaryLayout.getChildren().add(progressText);
            Scene secondScene = new Scene(secondaryLayout, 300, 190);
            secondScene.getRoot().setStyle("-fx-base:rgba(60, 63, 65, 255)");
            Stage progressWindow = new Stage();
            progressWindow.setTitle("Calculating - " + ChooseData.getSelectionModel().getSelectedItem());
            progressWindow.setScene(secondScene);
            progressWindow.setResizable(false);
            progressWindow.initModality(Modality.NONE);
            progressWindow.setX(stage.getX() + 200);
            progressWindow.setY(stage.getY() + 100);
            Runnable runnable = () -> {
                Platform.runLater(progressWindow::show);
                double globalMaxX = 0;
                double globalMaxY = 0;
                int dataId = numberOfIndexes - ChooseData.getSelectionModel().getSelectedIndex();
                String method = "File Data";
                double parameterN = 1;
                if(spinner_n.getValue() != null) { parameterN = spinner_n.getValue(); } // get n parameter value
                boolean WL_Method = false;
                boolean MaxesAmount_Method = false;
                double wlStartValue = 0;
                double wlStopValue = 0;
                if(ChooseMethod.getSelectionModel().getSelectedItem() == "Wave Length") {
                    WL_Method = true;
                    method = "WL_START & WL_STOP";
                    wlStartValue = spinner_wl_start.getValue();
                    wlStopValue = spinner_wl_stop.getValue();
                }
                int maxCount = 0;
                if(ChooseMethod.getSelectionModel().getSelectedItem() == "Maxes Amount") {
                    MaxesAmount_Method = true;
                    method = "ILOSC_MAXIMOW";
                    maxCount = spinner_maxes.getValue();
                }
                int id_pomiar_start = 0;
                int id_pomiar_end = 0;
                int dataCounter = 0;
                double START_WL = 0;
                double STOP_WL = 0;
                int SMPL = 0;
                try (Connection connection = dataSource.getConnection()) {
                    String query = "SELECT * FROM dane WHERE id_dane = " + dataId;
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(query);
                    while(resultSet.next()) {
                        id_pomiar_start = resultSet.getInt("id_pomiar_start");
                        id_pomiar_end = resultSet.getInt("id_pomiar_end");
                        START_WL = resultSet.getDouble("START_WL");
                        STOP_WL = resultSet.getDouble("STOP_WL");
                        SMPL = resultSet.getInt("SMPL");
                    }
                } catch(SQLException exception) {
                    Notifications.setNotificationConnectionToDatabaseFailed();
                }

                if(WL_Method) {
                    START_WL = wlStartValue;
                    STOP_WL = wlStopValue;
                    int startFilteredIndex = 0;
                    int stopFilteredIndex = 0;
                    try (Connection connection = dataSource.getConnection()) {
                        String query = "SELECT * FROM pomiary WHERE id_pomiar >= " + id_pomiar_start + " AND id_pomiar <= " + id_pomiar_end + " AND x=" + wlStartValue;
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery(query);
                        while (resultSet.next()) {
                            startFilteredIndex = resultSet.getInt("id_pomiar");
                        }
                    } catch (SQLException exception) {
                        Notifications.setNotificationConnectionToDatabaseFailed();
                    }
                    try (Connection connection = dataSource.getConnection()) {
                        String query = "SELECT * FROM pomiary WHERE id_pomiar >= " + id_pomiar_start + " AND id_pomiar <= " + id_pomiar_end + " AND x=" + wlStopValue;
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery(query);
                        while (resultSet.next()) {
                            stopFilteredIndex = resultSet.getInt("id_pomiar");
                        }
                    } catch (SQLException exception) {
                        Notifications.setNotificationConnectionToDatabaseFailed();
                    }
                    id_pomiar_start = startFilteredIndex;
                    id_pomiar_end = stopFilteredIndex;
                }

                int dataIndex = id_pomiar_end-id_pomiar_start+1;
                Data[] data = new Data[dataIndex];
                try (Connection connection = dataSource.getConnection()) {
                    String query = "SELECT * FROM pomiary WHERE id_pomiar >= " + id_pomiar_start + " AND id_pomiar <= " + id_pomiar_end;
                    Statement statement = connection.createStatement();
                    ResultSet resultSet = statement.executeQuery(query);
                    while(resultSet.next()) {
                        int index = resultSet.getInt("id_pomiar");
                        double x = resultSet.getDouble("x");
                        double y = resultSet.getDouble("y");
                        if(y >= 0.00003) {
                            data[dataCounter] = new Data(index, x, y);
                            dataCounter++;
                        }
                    }
                    dataIndex = dataCounter;
                    id_pomiar_end = data[dataCounter-1].getIndex();
                    String queryMaxY = "SELECT MAX(y) FROM pomiary WHERE id_pomiar >= " + id_pomiar_start + " AND id_pomiar <= " + id_pomiar_end;
                    Statement statementMaxY = connection.createStatement();
                    ResultSet resultSetMaxY = statementMaxY.executeQuery(queryMaxY);
                    while(resultSetMaxY.next()) {
                        globalMaxY = resultSetMaxY.getDouble("MAX(y)");
                    }
                    String queryMaxX = "SELECT * FROM pomiary WHERE id_pomiar >= " + id_pomiar_start + " AND id_pomiar <= " + id_pomiar_end + " AND y = " + globalMaxY;
                    Statement statementMaxX = connection.createStatement();
                    ResultSet resultSetMaxX = statementMaxX.executeQuery(queryMaxX);
                    while(resultSetMaxX.next()) {
                        globalMaxX = resultSetMaxX.getDouble("x");
                    }
                } catch(SQLException exception) {
                    Notifications.setNotificationConnectionToDatabaseFailed();
                }
                boolean lookingForMax = false;
                boolean lookingForMin = true;
                double lastYhelper = 9999.99;
                int lastindexhelper = -30;
                List<Integer> waveIndexData = new ArrayList<>();
                int dataPointCounter = 0;
                if(SMPL == 1001) {
                    for (int a = 0; a < dataCounter; a++) {
                        if(MaxesAmount_Method) {
                            if(dataPointCounter == maxCount && !lookingForMin)
                            {
                                lookingForMax = false;
                            }
                        }
                        if (lookingForMax) {
                            if (a + 1 < dataIndex && a + 2 < dataIndex && a + 3 < dataIndex && a + 4 < dataIndex && a + 5 < dataIndex) {
                                if (data[a].getY() > data[a + 1].getY() && data[a].getY() > data[a + 2].getY() && data[a].getY() > data[a + 3].getY() && data[a].getY() > data[a + 4].getY() && data[a].getY() > data[a + 5].getY()) {
                                    if(data[a].getY() >= lastYhelper) {
                                        waveIndexData.add(data[a].getIndex());
                                        lastYhelper = data[a].getY() * 0.83;
                                        dataPointCounter++;
                                        lookingForMax = false;
                                        lookingForMin = true;
                                    }
                                }
                            }
                        } else if (lookingForMin) {
                            if (a + 1 < dataIndex && a + 2 < dataIndex && a + 3 < dataIndex && a + 4 < dataIndex && a + 5 < dataIndex) {
                                if (data[a].getY() < data[a + 1].getY() && data[a].getY() < data[a + 2].getY() && data[a].getY() < data[a + 3].getY() && data[a].getY() < data[a + 4].getY() && data[a].getY() < data[a + 5].getY()) {
                                    if(data[a].getY() <= lastYhelper) {
                                        waveIndexData.add(data[a].getIndex());
                                        lastYhelper = data[a].getY() * 1.20;
                                        lookingForMin = false;
                                        lookingForMax = true;
                                    }
                                }
                            } else {
                                double y = 0;
                                int idx = 0;
                                try (Connection connection = dataSource.getConnection()) {
                                    String query = "SELECT MIN(y) FROM pomiary WHERE id_pomiar >= " + data[a].getIndex() + " AND id_pomiar <= " + id_pomiar_end;
                                    Statement statement = connection.createStatement();
                                    ResultSet resultSet = statement.executeQuery(query);
                                    while (resultSet.next()) {
                                        y = resultSet.getDouble("MIN(Y)");
                                    }
                                    String query_sec = "SELECT * FROM pomiary WHERE id_pomiar >= " + data[a].getIndex() + " AND id_pomiar <= " + id_pomiar_end + " AND y=" + y;
                                    Statement statement_sec = connection.createStatement();
                                    ResultSet resultSet_sec = statement_sec.executeQuery(query_sec);
                                    while (resultSet_sec.next()) {
                                        idx = resultSet_sec.getInt("id_pomiar");
                                    }
                                } catch (SQLException exception) {
                                    Notifications.setNotificationConnectionToDatabaseFailed();
                                }
                                waveIndexData.add(idx);
                                lookingForMin = false;
                                lookingForMax = true;
                            }
                        }
                    }
                } else if(SMPL == 2001) {
                    for (int a = 0; a < dataCounter; a++) {
                        if(MaxesAmount_Method) {
                            if(dataPointCounter == maxCount && !lookingForMin)
                            {
                                lookingForMax = false;
                            }
                        }
                        if (lookingForMax) {
                            if (a + 1 < dataIndex && a + 2 < dataIndex && a + 3 < dataIndex && a + 4 < dataIndex && a + 5 < dataIndex && a + 6 < dataIndex && a + 7 < dataIndex && a + 8 < dataIndex && a + 9 < dataIndex && a + 10 < dataIndex) {
                                if (data[a].getY() > data[a + 1].getY() && data[a].getY() > data[a + 2].getY() && data[a].getY() > data[a + 3].getY() && data[a].getY() > data[a + 4].getY() && data[a].getY() > data[a + 5].getY() && data[a].getY() > data[a + 6].getY() && data[a].getY() > data[a + 7].getY() && data[a].getY() > data[a + 8].getY() && data[a].getY() > data[a + 9].getY() && data[a].getY() > data[a + 10].getY()) {
                                    if(data[a].getY() >= lastYhelper) {
                                        waveIndexData.add(data[a].getIndex());
                                        lastYhelper = data[a].getY() * 0.85;
                                        dataPointCounter++;
                                        lookingForMax = false;
                                        lookingForMin = true;
                                    }
                                }
                            }
                        } else if (lookingForMin) {
                            if (a + 1 < dataIndex && a + 2 < dataIndex && a + 3 < dataIndex && a + 4 < dataIndex && a + 5 < dataIndex && a + 6 < dataIndex && a + 7 < dataIndex && a + 8 < dataIndex && a + 9 < dataIndex && a + 10 < dataIndex) {
                                if (data[a].getY() < data[a + 1].getY() && data[a].getY() < data[a + 2].getY() && data[a].getY() < data[a + 3].getY() && data[a].getY() < data[a + 4].getY() && data[a].getY() < data[a + 5].getY() && data[a].getY() < data[a + 6].getY() && data[a].getY() < data[a + 7].getY() && data[a].getY() < data[a + 8].getY() && data[a].getY() < data[a + 9].getY() && data[a].getY() < data[a + 10].getY()) {
                                    if(data[a].getY() <= lastYhelper) {
                                        waveIndexData.add(data[a].getIndex());
                                        lastYhelper = data[a].getY() * 1.15;
                                        lookingForMin = false;
                                        lookingForMax = true;
                                    }
                                }
                            } else {
                                double y = 0;
                                int idx = 0;
                                try (Connection connection = dataSource.getConnection()) {
                                    String query = "SELECT MIN(y) FROM pomiary WHERE id_pomiar >= " + data[a].getIndex() + " AND id_pomiar <= " + id_pomiar_end;
                                    Statement statement = connection.createStatement();
                                    ResultSet resultSet = statement.executeQuery(query);
                                    while (resultSet.next()) {
                                        y = resultSet.getDouble("MIN(Y)");
                                    }
                                    String query_sec = "SELECT * FROM pomiary WHERE id_pomiar >= " + data[a].getIndex() + " AND id_pomiar <= " + id_pomiar_end + " AND y=" + y;
                                    Statement statement_sec = connection.createStatement();
                                    ResultSet resultSet_sec = statement_sec.executeQuery(query_sec);
                                    while (resultSet_sec.next()) {
                                        idx = resultSet_sec.getInt("id_pomiar");
                                    }
                                } catch (SQLException exception) {
                                    Notifications.setNotificationConnectionToDatabaseFailed();
                                }
                                waveIndexData.add(idx);
                                lookingForMin = false;
                                lookingForMax = true;
                            }
                        }
                    }
                } else {
                    for (int a = 0; a < dataCounter; a++) {
                        if(MaxesAmount_Method) {
                            if(dataPointCounter == maxCount && !lookingForMin)
                            {
                                lookingForMax = false;
                            }
                        }
                        if (lookingForMax) {
                            if (a + 1 < dataIndex && a + 2 < dataIndex && a + 3 < dataIndex && a + 4 < dataIndex && a + 5 < dataIndex && a + 6 < dataIndex && a + 7 < dataIndex && a + 8 < dataIndex && a + 9 < dataIndex && a + 10 < dataIndex && a + 11 < dataIndex && a + 12 < dataIndex && a + 13 < dataIndex && a + 14 < dataIndex && a + 15 < dataIndex && a + 16 < dataIndex && a + 17 < dataIndex && a + 18 < dataIndex && a + 19 < dataIndex && a + 20 < dataIndex && a + 21 < dataIndex && a + 22 < dataIndex && a + 23 < dataIndex && a + 24 < dataIndex && a + 25 < dataIndex && a + 26 < dataIndex && a + 27 < dataIndex && a + 28 < dataIndex && a + 29 < dataIndex && a + 30 < dataIndex) {
                                if (data[a].getY() > data[a + 1].getY() && data[a].getY() > data[a + 2].getY() && data[a].getY() > data[a + 3].getY() && data[a].getY() > data[a + 4].getY() && data[a].getY() > data[a + 5].getY() && data[a].getY() > data[a + 6].getY() && data[a].getY() > data[a + 7].getY() && data[a].getY() > data[a + 8].getY() && data[a].getY() > data[a + 9].getY() && data[a].getY() > data[a + 10].getY() && data[a].getY() > data[a + 11].getY() && data[a].getY() > data[a + 12].getY() && data[a].getY() > data[a + 13].getY() && data[a].getY() > data[a + 14].getY() && data[a].getY() > data[a + 15].getY() && data[a].getY() > data[a + 16].getY() && data[a].getY() > data[a + 17].getY() && data[a].getY() > data[a + 18].getY() && data[a].getY() > data[a + 19].getY() && data[a].getY() > data[a + 20].getY() && data[a].getY() > data[a + 21].getY() && data[a].getY() > data[a + 22].getY() && data[a].getY() > data[a + 23].getY() && data[a].getY() > data[a + 24].getY() && data[a].getY() > data[a + 25].getY() && data[a].getY() > data[a + 26].getY() && data[a].getY() > data[a + 27].getY() && data[a].getY() > data[a + 28].getY() && data[a].getY() > data[a + 29].getY() && data[a].getY() > data[a + 30].getY()) {
                                    if(data[a].getY() >= lastYhelper && (data[a].getIndex()-lastindexhelper>=30)) {
                                        waveIndexData.add(data[a].getIndex());
                                        lastYhelper = data[a].getY() * 0.80;
                                        lastindexhelper = data[a].getIndex();
                                        dataPointCounter++;
                                        lookingForMax = false;
                                        lookingForMin = true;
                                    }
                                }
                            }
                        } else if (lookingForMin) {
                            if (a + 1 < dataIndex && a + 2 < dataIndex && a + 3 < dataIndex && a + 4 < dataIndex && a + 5 < dataIndex && a + 6 < dataIndex && a + 7 < dataIndex && a + 8 < dataIndex && a + 9 < dataIndex && a + 10 < dataIndex && a + 11 < dataIndex && a + 12 < dataIndex && a + 13 < dataIndex && a + 14 < dataIndex && a + 15 < dataIndex && a + 16 < dataIndex && a + 17 < dataIndex && a + 18 < dataIndex && a + 19 < dataIndex && a + 20 < dataIndex && a + 21 < dataIndex && a + 22 < dataIndex && a + 23 < dataIndex && a + 24 < dataIndex && a + 25 < dataIndex && a + 26 < dataIndex && a + 27 < dataIndex && a + 28 < dataIndex && a + 29 < dataIndex && a + 30 < dataIndex) {
                                if (data[a].getY() < data[a + 1].getY() && data[a].getY() < data[a + 2].getY() && data[a].getY() < data[a + 3].getY() && data[a].getY() < data[a + 4].getY() && data[a].getY() < data[a + 5].getY() && data[a].getY() < data[a + 6].getY() && data[a].getY() < data[a + 7].getY() && data[a].getY() < data[a + 8].getY() && data[a].getY() < data[a + 9].getY() && data[a].getY() < data[a + 10].getY() && data[a].getY() < data[a + 11].getY() && data[a].getY() < data[a + 12].getY() && data[a].getY() < data[a + 13].getY() && data[a].getY() < data[a + 14].getY() && data[a].getY() < data[a + 15].getY() && data[a].getY() < data[a + 16].getY() && data[a].getY() < data[a + 17].getY() && data[a].getY() < data[a + 18].getY() && data[a].getY() < data[a + 19].getY() && data[a].getY() < data[a + 20].getY() && data[a].getY() < data[a + 21].getY() && data[a].getY() < data[a + 22].getY() && data[a].getY() < data[a + 23].getY() && data[a].getY() < data[a + 24].getY() && data[a].getY() < data[a + 25].getY() && data[a].getY() < data[a + 26].getY() && data[a].getY() < data[a + 27].getY() && data[a].getY() < data[a + 28].getY() && data[a].getY() < data[a + 29].getY() && data[a].getY() < data[a + 30].getY()) {
                                    if(data[a].getY() <= lastYhelper  && (data[a].getIndex()-lastindexhelper>=30)) {
                                        waveIndexData.add(data[a].getIndex());
                                        lastYhelper = data[a].getY() * 1.20;
                                        lastindexhelper = data[a].getIndex();
                                        lookingForMin = false;
                                        lookingForMax = true;
                                    }
                                }
                            } else {
                                double y = 0;
                                int idx = 0;
                                try (Connection connection = dataSource.getConnection()) {
                                    String query = "SELECT MIN(y) FROM pomiary WHERE id_pomiar >= " + data[a].getIndex() + " AND id_pomiar <= " + id_pomiar_end;
                                    Statement statement = connection.createStatement();
                                    ResultSet resultSet = statement.executeQuery(query);
                                    while (resultSet.next()) {
                                        y = resultSet.getDouble("MIN(Y)");
                                    }
                                    String query_sec = "SELECT * FROM pomiary WHERE id_pomiar >= " + data[a].getIndex() + " AND id_pomiar <= " + id_pomiar_end + " AND y=" + y;
                                    Statement statement_sec = connection.createStatement();
                                    ResultSet resultSet_sec = statement_sec.executeQuery(query_sec);
                                    while (resultSet_sec.next()) {
                                        idx = resultSet_sec.getInt("id_pomiar");
                                    }
                                } catch (SQLException exception) {
                                    Notifications.setNotificationConnectionToDatabaseFailed();
                                }
                                waveIndexData.add(idx);
                                lookingForMin = false;
                                lookingForMax = true;
                            }
                        }
                    }
                }
                if(MaxesAmount_Method) {
                    int index_start_filtered = waveIndexData.get(0);
                    int index_stop_filtered = waveIndexData.get(waveIndexData.size()-1);
                    try (Connection connection = dataSource.getConnection()) {
                        String query = "SELECT * FROM pomiary WHERE id_pomiar= " + index_start_filtered;
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery(query);
                        while(resultSet.next()) {
                            START_WL = resultSet.getDouble("x");
                        }
                    } catch(SQLException exception) {
                        Notifications.setNotificationConnectionToDatabaseFailed();
                    }
                    try (Connection connection = dataSource.getConnection()) {
                        String query = "SELECT * FROM pomiary WHERE id_pomiar= " + index_stop_filtered;
                        Statement statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery(query);
                        while(resultSet.next()) {
                            STOP_WL = resultSet.getDouble("x");
                        }
                    } catch(SQLException exception) {
                        Notifications.setNotificationConnectionToDatabaseFailed();
                    }
                    try (Connection connection = dataSource.getConnection()) {
                        String queryMaxY = "SELECT MAX(y) FROM pomiary WHERE id_pomiar >= " + index_start_filtered + " AND id_pomiar <= " + index_stop_filtered;
                        Statement statementMaxY = connection.createStatement();
                        ResultSet resultSetMaxY = statementMaxY.executeQuery(queryMaxY);
                        while(resultSetMaxY.next()) {
                            globalMaxY = resultSetMaxY.getDouble("MAX(y)");
                        }
                        String queryMaxX = "SELECT * FROM pomiary WHERE id_pomiar >= " + index_start_filtered + " AND id_pomiar <= " + index_stop_filtered + " AND y = " + globalMaxY;
                        Statement statementMaxX = connection.createStatement();
                        ResultSet resultSetMaxX = statementMaxX.executeQuery(queryMaxX);
                        while(resultSetMaxX.next()) {
                            globalMaxX = resultSetMaxX.getDouble("x");
                        }
                    } catch(SQLException exception) {
                        Notifications.setNotificationConnectionToDatabaseFailed();
                    }
                }

                List<Double> minYValues = new ArrayList<>();
                List<Double> minXValues = new ArrayList<>();
                List<Double> maxXValues = new ArrayList<>();
                int globalMaxWavePositionIndex = 0;
                double Imin;
                double Imax;
                double yHelper = 0;
                for(int k = 0; k <= dataPointCounter*2; k++) {
                    if(k%2 == 0) {
                        try (Connection connection = dataSource.getConnection()) {
                            String query = "SELECT * FROM pomiary WHERE id_pomiar= " + waveIndexData.get(k);
                            Statement statement = connection.createStatement();
                            ResultSet resultSet = statement.executeQuery(query);
                            while(resultSet.next()) {
                                double y = resultSet.getDouble("y");
                                double x = resultSet.getDouble("x");
                                minXValues.add(x);
                                minYValues.add(y);
                            }
                        } catch(SQLException exception) {
                            Notifications.setNotificationConnectionToDatabaseFailed();
                        }
                    }
                    else {
                        try (Connection connection = dataSource.getConnection()) {
                            String query = "SELECT * FROM pomiary WHERE id_pomiar= " + waveIndexData.get(k);
                            Statement statement = connection.createStatement();
                            ResultSet resultSet = statement.executeQuery(query);
                            while(resultSet.next()) {
                                double x = resultSet.getDouble("x");
                                double y = resultSet.getDouble("y");
                                if(dataPointCounter == 1) { x = globalMaxX; }
                                maxXValues.add(x);
                                if (x == globalMaxX || y>yHelper) {
                                    yHelper = y;
                                    globalMaxWavePositionIndex = maxXValues.size() - 1;
                                }
                            }
                        } catch(SQLException exception) {
                            Notifications.setNotificationConnectionToDatabaseFailed();
                        }
                    }
                }
                Imax = globalMaxY;
                if(minYValues.get(globalMaxWavePositionIndex) < minYValues.get(globalMaxWavePositionIndex+1)) {
                    Imin = minYValues.get(globalMaxWavePositionIndex);
                } else {
                    Imin = minYValues.get(globalMaxWavePositionIndex+1);
                }
                BigDecimal ImaxValueDecimal = BigDecimal.valueOf(Imax);
                BigDecimal IminValueDecimal = BigDecimal.valueOf(Imin);
                BigDecimal V = ImaxValueDecimal.subtract(IminValueDecimal).divide(ImaxValueDecimal.add(IminValueDecimal), 4, RoundingMode.HALF_UP);
                BigDecimal averageDistance = BigDecimal.valueOf(0);
                for(int s = 1; s<dataPointCounter;s++) {
                    BigDecimal firstVal = BigDecimal.valueOf(maxXValues.get(s));
                    BigDecimal secondVal = BigDecimal.valueOf(maxXValues.get(s-1));
                    averageDistance = averageDistance.add(firstVal.subtract(secondVal));
                }
                BigDecimal max_left_position = BigDecimal.valueOf(maxXValues.get(0));
                BigDecimal max_right_position = BigDecimal.valueOf(maxXValues.get(maxXValues.size()-1));
                BigDecimal x = BigDecimal.valueOf(0);
                if(!max_right_position.equals(BigDecimal.valueOf(maxXValues.get(0)))) { x = (max_left_position.multiply(max_right_position).multiply(BigDecimal.valueOf(dataPointCounter))).divide((max_right_position.subtract(max_left_position)).multiply(BigDecimal.valueOf(2 * parameterN)), 4, RoundingMode.HALF_UP); }
                if(averageDistance.equals(BigDecimal.valueOf(0))) { averageDistance = BigDecimal.valueOf(0); }
                else {
                    averageDistance = averageDistance.divide(BigDecimal.valueOf(dataPointCounter - 1), 4, RoundingMode.HALF_UP);
                }
                double central_left_distance = 0;
                if(maxXValues.size() > globalMaxWavePositionIndex - 1) {
                    if (globalMaxWavePositionIndex != 0) {
                        central_left_distance = BigDecimal.valueOf(maxXValues.get(globalMaxWavePositionIndex)).subtract(BigDecimal.valueOf(maxXValues.get(globalMaxWavePositionIndex - 1))).doubleValue();
                    }
                }
                double central_right_distance = 0;
                if(maxXValues.size() > globalMaxWavePositionIndex + 1) {
                    central_right_distance = BigDecimal.valueOf(maxXValues.get(globalMaxWavePositionIndex + 1)).subtract(BigDecimal.valueOf(maxXValues.get(globalMaxWavePositionIndex))).doubleValue();
                }
                int id_wynik_p = 0;
                try (Connection connection = dataSource.getConnection()) {
                    String query = "INSERT INTO wyniki_user_parameters (`id_dane`,`ILOSC_MAXIMOW`,`WL_START`,`WL_STOP`,`n`,`V`, `x`, `global_max_X`, `global_max_Y`, `srednia_odl`, `odl_central_lewo`, `odl_central_prawo`, `absorpcja`,`POMIAR_NA_PODSTAWIE`) VALUES ('" + dataId + "','" + dataPointCounter + "','" + START_WL + "','" + STOP_WL + "','" + parameterN + "','" + V.doubleValue() + "','" + x.doubleValue() + "','" + globalMaxX + "','" + globalMaxY + "','" + averageDistance.doubleValue() + "','" + central_left_distance + "','" + central_right_distance + "','" + Imin +  "','" + method +"')";
                    Statement statement = connection.createStatement();
                    statement.execute(query);

                    String query_2 = "SELECT * FROM wyniki_user_parameters ORDER BY id_wynik_p DESC LIMIT 1";
                    Statement statement_2 = connection.createStatement();
                    ResultSet resultSet = statement_2.executeQuery(query_2);
                    while(resultSet.next()) {
                        id_wynik_p = resultSet.getInt("id_wynik_p");
                    }
                    int finalId_wynik_p = id_wynik_p;
                    Platform.runLater(() -> {
                        String SQL = "SELECT * from wyniki_user_parameters WHERE id_wynik_p =" + finalId_wynik_p;
                        UpdateTableViewData(ResultsTableView, SQL, true);
                        UpdateTableViewData(HistoryTableView, "SELECT * from wyniki_user_parameters ORDER BY id_wynik_p DESC", false);
                        Notifications.setNotificationCalculateResultsSucessfully(finalId_wynik_p);
                        progressWindow.close();
                    });
                } catch(SQLException exception) {
                    Notifications.setNotificationConnectionToDatabaseFailed();
                }

            };
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            thread.start();
        });

        UploadDataGroup.setOnSelectionChanged(e -> {
            UpdateTableViewData(DataTableView, "SELECT * from dane ORDER BY id_dane DESC", false);
        });

        CalculateResultsGroup.setOnSelectionChanged(e -> refresh_calculate_group());

        HistoryGroup.setOnSelectionChanged(e -> {
            UpdateTableViewData(HistoryTableView, "SELECT * from wyniki_user_parameters ORDER BY id_wynik_p DESC", false);
            UpdateTableViewData(HistoryTableViewFiles, "SELECT * from wyniki ORDER BY id_wynik DESC", false);
        });

        DownloadAsPDF.setOnAction(e -> {
            Object row2 = HistoryTableView.getSelectionModel().getSelectedItems().get(0);
            String id_wynik_p = row2.toString().split(",")[0].substring(1); //id_wynik_p of selected record
            String link = "http://pawelploszaj.pl/pdf_user_parameters.php?id_wynik_p[0]="+ id_wynik_p;
            boolean compare = false;
            if(HistoryTableView.getSelectionModel().getSelectedItems().stream().count() >= 2) {
                compare = true;
                int selected = (int) HistoryTableView.getSelectionModel().getSelectedItems().stream().count();
                for(int i=1;i<selected;i++) {
                    Object row = HistoryTableView.getSelectionModel().getSelectedItems().get(i);
                    String id_wynik_p_2 = row.toString().split(",")[0].substring(1);
                    link = link + "&id_wynik_p[" + i + "]=" + id_wynik_p_2;
                }
            }
            int id_dane = 0;
            String time = "";
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM wyniki_user_parameters WHERE id_wynik_p = " + id_wynik_p;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    id_dane = resultSet.getInt("id_dane");
                    time = String.valueOf(resultSet.getTimestamp("datetime")).replaceAll(":","-");
                }
                String query_2 = "SELECT * FROM dane WHERE id_dane = " + id_dane;
                Statement statement_2 = connection.createStatement();
                ResultSet resultSet_2 = statement_2.executeQuery(query_2);
                while(resultSet_2.next()) {
                    String name = resultSet_2.getString("nazwa_pliku");
                    if(compare) {
                        String fileName = File.separator + "Compare-" + name + "-" + time;
                        DownloadFile(link, fileName, ".pdf");
                    } else {
                        String fileName = File.separator + name + "-" + time;
                        DownloadFile(link, fileName, ".pdf");
                    }
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            } catch (IOException ex) {
                Notifications.setNotificationDownloadFailed();
                ex.printStackTrace();
            }
        });

        DownloadAsCSV.setOnAction(e -> {
            Object row = HistoryTableView.getSelectionModel().getSelectedItems().get(0);
            String id_wynik_p = row.toString().split(",")[0].substring(1); //id_wynik_p of selected record
            int id_dane = 0;
            int ILOSC_MAXIMOW = 0;
            double WL_START = 0;
            double WL_STOP = 0;
            double n = 0;
            double V = 0;
            double x = 0;
            double global_max_X = 0;
            double global_max_Y = 0;
            double srednia_odl = 0;
            double odl_central_lewo = 0;
            double odl_central_prawo = 0;
            double absorpcja = 0;
            String POMIAR_NA_PODSTAWIE = "";
            String datetime = "";
            String time = "";
            String fileName = "";
            String name = "";
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM wyniki_user_parameters WHERE id_wynik_p = " + id_wynik_p;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    id_dane = resultSet.getInt("id_dane");
                    ILOSC_MAXIMOW = resultSet.getInt("ILOSC_MAXIMOW");
                    WL_START = resultSet.getDouble("WL_START");
                    WL_STOP = resultSet.getDouble("WL_STOP");
                    n = resultSet.getDouble("n");
                    V = resultSet.getDouble("V");
                    x = resultSet.getDouble("x");
                    global_max_X = resultSet.getDouble("global_max_X");
                    global_max_Y = resultSet.getDouble("global_max_Y");
                    srednia_odl = resultSet.getDouble("srednia_odl");
                    odl_central_lewo = resultSet.getDouble("odl_central_lewo");
                    odl_central_prawo = resultSet.getDouble("odl_central_prawo");
                    absorpcja = resultSet.getDouble("absorpcja");
                    POMIAR_NA_PODSTAWIE = resultSet.getString("POMIAR_NA_PODSTAWIE");
                    datetime = String.valueOf(resultSet.getTimestamp("datetime"));
                    time = String.valueOf(resultSet.getTimestamp("datetime")).replaceAll(":","-");
                }
                String query_2 = "SELECT * FROM dane WHERE id_dane = " + id_dane;
                Statement statement_2 = connection.createStatement();
                ResultSet resultSet_2 = statement_2.executeQuery(query_2);
                while(resultSet_2.next()) {
                    name = resultSet_2.getString("nazwa_pliku");
                    fileName = name + "-" + time;
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            }
            String downloadFileLocation = SavePath + File.separator + fileName + ".csv";
            try (Connection connection = dataSource.getConnection()) {
                String SQL = "SELECT * from wyniki_user_parameters ORDER BY id_wynik_p DESC";
                ResultSet rs = connection.createStatement().executeQuery(SQL);
                String[] arrayList = new String[rs.getMetaData().getColumnCount()];
                String[] row2 = {name, String.valueOf(id_dane), String.valueOf(ILOSC_MAXIMOW), String.valueOf(WL_START), String.valueOf(WL_STOP), String.valueOf(n), String.valueOf(V), String.valueOf(x), String.valueOf(global_max_X), String.valueOf(global_max_Y), String.valueOf(srednia_odl), String.valueOf(odl_central_lewo), String.valueOf(odl_central_prawo), String.valueOf(absorpcja),POMIAR_NA_PODSTAWIE,datetime};
                arrayList[0] = "nazwa_pliku";
                for (int i = 1; i < rs.getMetaData().getColumnCount(); i++) {
                    arrayList[i] = rs.getMetaData().getColumnName(i + 1);
                }
                List<String[]> csvData = new ArrayList<>();
                csvData.add(arrayList);
                csvData.add(row2);
                try (CSVWriter writer = new CSVWriter(new FileWriter(downloadFileLocation))) {
                    writer.writeAll(csvData);
                    setNotificationDownloadResultsCSVComplete(downloadFileLocation);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    setNotificationDownloadFailed();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                setNotificationConnectionToDatabaseFailed();
            }
        });

        ShowChartUserParameters.setOnAction(e -> {
            Object row = HistoryTableView.getSelectionModel().getSelectedItems().get(0);
            String id_wynik_p = row.toString().split(",")[0].substring(1); //id_wynik_p of selected record
            int id_dane = 0;
            double WL_START = 0;
            double WL_STOP = 0;
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM wyniki_user_parameters WHERE id_wynik_p = " + id_wynik_p;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    id_dane = resultSet.getInt("id_dane");
                    WL_START = resultSet.getDouble("WL_START");
                    WL_STOP = resultSet.getDouble("WL_STOP");
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            }
            String link = "http://pawelploszaj.pl/wykres.php?id_dane="+ id_dane + "&WL_START=" + WL_START + "&WL_STOP=" + WL_STOP;
            Image img = new Image(link);
            ImageView imageView = new ImageView(img);
            StackPane secondaryLayout = new StackPane();
            secondaryLayout.getChildren().add(imageView);
            Scene secondScene = new Scene(secondaryLayout, 600, 400);
            secondScene.getRoot().setStyle("-fx-base:rgba(60, 63, 65, 255)");
            Stage newWindow = new Stage();
            newWindow.setTitle("Chart - id_wynik_p=" + id_wynik_p);
            newWindow.setScene(secondScene);
            newWindow.setResizable(false);
            newWindow.initModality(Modality.NONE);
            newWindow.setX(stage.getX() + 200);
            newWindow.setY(stage.getY() + 100);
            newWindow.show();
        });

        DownloadChartUserParameters.setOnAction(e -> {
            Object row = HistoryTableView.getSelectionModel().getSelectedItems().get(0);
            String id_wynik_p = row.toString().split(",")[0].substring(1); //id_wynik_p of selected record
            int id_dane = 0;
            double WL_START = 0;
            double WL_STOP = 0;
            String time = "";
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM wyniki_user_parameters WHERE id_wynik_p = " + id_wynik_p;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    id_dane = resultSet.getInt("id_dane");
                    WL_START = resultSet.getDouble("WL_START");
                    WL_STOP = resultSet.getDouble("WL_STOP");
                    time = String.valueOf(resultSet.getTimestamp("datetime")).replaceAll(":","-");
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            }
            String link = "http://pawelploszaj.pl/wykres_download.php?id_dane="+ id_dane + "&WL_START=" + WL_START + "&WL_STOP=" + WL_STOP;
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM dane WHERE id_dane = " + id_dane;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    String name = resultSet.getString("nazwa_pliku");
                    String fileName = File.separator + name + "-" + time;
                    DownloadFile(link, fileName, ".jpg");
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            } catch (IOException ex) {
                Notifications.setNotificationDownloadFailed();
                ex.printStackTrace();
            }
        });

        ShowChartUserParametersMaxAndMins.setOnAction(e -> {
            Object row = HistoryTableView.getSelectionModel().getSelectedItems().get(0);
            String id_wynik_p = row.toString().split(",")[0].substring(1); //id_wynik_p of selected record
            int id_dane = 0;
            double WL_START = 0;
            double WL_STOP = 0;
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM wyniki_user_parameters WHERE id_wynik_p = " + id_wynik_p;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    id_dane = resultSet.getInt("id_dane");
                    WL_START = resultSet.getDouble("WL_START");
                    WL_STOP = resultSet.getDouble("WL_STOP");
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            }
            String link = "http://pawelploszaj.pl/wykres_max_mins.php?id_dane="+ id_dane + "&WL_START=" + WL_START + "&WL_STOP=" + WL_STOP;
            Image img = new Image(link);
            ImageView imageView = new ImageView(img);
            StackPane secondaryLayout = new StackPane();
            secondaryLayout.getChildren().add(imageView);
            Scene secondScene = new Scene(secondaryLayout, 600, 400);
            secondScene.getRoot().setStyle("-fx-base:rgba(60, 63, 65, 255)");
            Stage newWindow = new Stage();
            newWindow.setTitle("Chart - id_wynik_p=" + id_wynik_p);
            newWindow.setScene(secondScene);
            newWindow.setResizable(false);
            newWindow.initModality(Modality.NONE);
            newWindow.setX(stage.getX() + 200);
            newWindow.setY(stage.getY() + 100);
            newWindow.show();
        });

        DownloadChartUserParametersMaxAndMins.setOnAction(e -> {
            Object row = HistoryTableView.getSelectionModel().getSelectedItems().get(0);
            String id_wynik_p = row.toString().split(",")[0].substring(1); //id_wynik_p of selected record
            int id_dane = 0;
            double WL_START = 0;
            double WL_STOP = 0;
            String time = "";
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM wyniki_user_parameters WHERE id_wynik_p = " + id_wynik_p;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    id_dane = resultSet.getInt("id_dane");
                    WL_START = resultSet.getDouble("WL_START");
                    WL_STOP = resultSet.getDouble("WL_STOP");
                    time = String.valueOf(resultSet.getTimestamp("datetime")).replaceAll(":","-");
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            }
            String link = "http://pawelploszaj.pl/wykres_max_mins_download.php?id_dane="+ id_dane + "&WL_START=" + WL_START + "&WL_STOP=" + WL_STOP;
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM dane WHERE id_dane = " + id_dane;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    String name = resultSet.getString("nazwa_pliku") + "-MaxAndMins";
                    String fileName = File.separator + name + "-" + time;
                    DownloadFile(link, fileName, ".jpg");
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            } catch (IOException ex) {
                Notifications.setNotificationDownloadFailed();
                ex.printStackTrace();
            }
        });

        DeleteRecordUserParameters.setOnAction(e -> {
            Object row = HistoryTableView.getSelectionModel().getSelectedItems().get(0);
            String id_wynik_p = row.toString().split(",")[0].substring(1);
            try (Connection connection = dataSource.getConnection()) {
                String query = "DELETE FROM wyniki_user_parameters WHERE id_wynik_p = " + id_wynik_p;
                Statement statement = connection.createStatement();
                statement.execute(query);
                String query_2 = "ALTER TABLE wyniki_user_parameters AUTO_INCREMENT = 1";
                Statement statement_2 = connection.createStatement();
                statement_2.execute(query_2);
            } catch(SQLException exception) {
                Notifications.setNotificationConnectionToDatabaseFailed();
            }
            UpdateTableViewData(HistoryTableView, "SELECT * from wyniki_user_parameters ORDER BY id_wynik_p DESC", false);
            ResultsTableView.getItems().remove(row);
            setNotificationDeletedRecordSucessfully();
        });

        DownloadAsPDF_2.setOnAction(e -> {
            Object row2 = HistoryTableViewFiles.getSelectionModel().getSelectedItems().get(0);
            String id_wynik = row2.toString().split(",")[0].substring(1); //id_wynik_p of selected record
            String link = "http://pawelploszaj.pl/pdf.php?id_wynik[0]="+ id_wynik;
            boolean compare = false;
            if(HistoryTableViewFiles.getSelectionModel().getSelectedItems().stream().count() >= 2) {
                compare = true;
                int selected = (int) HistoryTableViewFiles.getSelectionModel().getSelectedItems().stream().count();
                for(int i=1;i<selected;i++) {
                    Object row = HistoryTableViewFiles.getSelectionModel().getSelectedItems().get(i);
                    String id_wynik_2 = row.toString().split(",")[0].substring(1);
                    link = link + "&id_wynik[" + i + "]=" + id_wynik_2;
                }
            }
            int id_dane = 0;
            String time = "";
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM wyniki WHERE id_wynik = " + id_wynik;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    id_dane = resultSet.getInt("id_dane");
                    time = String.valueOf(resultSet.getTimestamp("datetime")).replaceAll(":","-");
                }
                String query_2 = "SELECT * FROM dane WHERE id_dane = " + id_dane;
                Statement statement_2 = connection.createStatement();
                ResultSet resultSet_2 = statement_2.executeQuery(query_2);
                while(resultSet_2.next()) {
                    String name = resultSet_2.getString("nazwa_pliku");
                    if(compare) {
                        String fileName = File.separator + "Compare-" + name + "-" + time;
                        DownloadFile(link, fileName, ".pdf");
                    } else {
                        String fileName = File.separator + name + "-" + time;
                        DownloadFile(link, fileName, ".pdf");
                    }
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            } catch (IOException ex) {
                Notifications.setNotificationDownloadFailed();
                ex.printStackTrace();
            }
        });

        DownloadAsCSV_2.setOnAction(e -> {
            Object row = HistoryTableViewFiles.getSelectionModel().getSelectedItems().get(0);
            String id_wynik = row.toString().split(",")[0].substring(1); //id_wynik_p of selected record
            int id_dane = 0;
            double V = 0;
            double x = 0;
            double global_max_X = 0;
            double global_max_Y = 0;
            double srednia_odl = 0;
            double odl_central_lewo = 0;
            double odl_central_prawo = 0;
            double absorpcja = 0;
            String datetime = "";
            String time = "";
            String fileName = "";
            String name = "";
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM wyniki WHERE id_wynik = " + id_wynik;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    id_dane = resultSet.getInt("id_dane");
                    V = resultSet.getDouble("V");
                    x = resultSet.getDouble("x");
                    global_max_X = resultSet.getDouble("global_max_X");
                    global_max_Y = resultSet.getDouble("global_max_Y");
                    srednia_odl = resultSet.getDouble("srednia_odl");
                    odl_central_lewo = resultSet.getDouble("odl_central_lewo");
                    odl_central_prawo = resultSet.getDouble("odl_central_prawo");
                    absorpcja = resultSet.getDouble("absorpcja");
                    datetime = String.valueOf(resultSet.getTimestamp("datetime"));
                    time = String.valueOf(resultSet.getTimestamp("datetime")).replaceAll(":","-");
                }
                String query_2 = "SELECT * FROM dane WHERE id_dane = " + id_dane;
                Statement statement_2 = connection.createStatement();
                ResultSet resultSet_2 = statement_2.executeQuery(query_2);
                while(resultSet_2.next()) {
                    name = resultSet_2.getString("nazwa_pliku");
                    fileName = name + "-" + time;
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            }
            String downloadFileLocation = SavePath + File.separator + fileName + ".csv";
            try (Connection connection = dataSource.getConnection()) {
                String SQL = "SELECT * from wyniki ORDER BY id_wynik DESC";
                ResultSet rs = connection.createStatement().executeQuery(SQL);
                String[] arrayList = new String[rs.getMetaData().getColumnCount()];
                String[] row2 = {name, String.valueOf(id_dane), String.valueOf(V), String.valueOf(x), String.valueOf(global_max_X), String.valueOf(global_max_Y), String.valueOf(srednia_odl), String.valueOf(odl_central_lewo), String.valueOf(odl_central_prawo), String.valueOf(absorpcja) ,datetime};
                arrayList[0] = "nazwa_pliku";
                for (int i = 1; i < rs.getMetaData().getColumnCount(); i++) {
                    arrayList[i] = rs.getMetaData().getColumnName(i + 1);
                }
                List<String[]> csvData = new ArrayList<>();
                csvData.add(arrayList);
                csvData.add(row2);
                try (CSVWriter writer = new CSVWriter(new FileWriter(downloadFileLocation))) {
                    writer.writeAll(csvData);
                    setNotificationDownloadResultsCSVComplete(downloadFileLocation);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    setNotificationDownloadFailed();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                setNotificationConnectionToDatabaseFailed();
            }
        });

        ShowChartUserParameters_2.setOnAction(e -> {
            Object row = HistoryTableViewFiles.getSelectionModel().getSelectedItems().get(0);
            String id_wynik = row.toString().split(",")[0].substring(1); //id_wynik_p of selected record
            int id_dane = 0;
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM wyniki WHERE id_wynik = " + id_wynik;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    id_dane = resultSet.getInt("id_dane");
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            }
            String link = "http://pawelploszaj.pl/wykres.php?id_dane="+ id_dane;
            Image img = new Image(link);
            ImageView imageView = new ImageView(img);
            StackPane secondaryLayout = new StackPane();
            secondaryLayout.getChildren().add(imageView);
            Scene secondScene = new Scene(secondaryLayout, 600, 400);
            secondScene.getRoot().setStyle("-fx-base:rgba(60, 63, 65, 255)");
            Stage newWindow = new Stage();
            newWindow.setTitle("Chart - id_wynik=" + id_wynik);
            newWindow.setScene(secondScene);
            newWindow.setResizable(false);
            newWindow.initModality(Modality.NONE);
            newWindow.setX(stage.getX() + 200);
            newWindow.setY(stage.getY() + 100);
            newWindow.show();
        });

        DownloadChartUserParameters_2.setOnAction(e -> {
            Object row = HistoryTableViewFiles.getSelectionModel().getSelectedItems().get(0);
            String id_wynik = row.toString().split(",")[0].substring(1); //id_wynik_p of selected record
            int id_dane = 0;
            String time = "";
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM wyniki WHERE id_wynik = " + id_wynik;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    id_dane = resultSet.getInt("id_dane");
                    time = String.valueOf(resultSet.getTimestamp("datetime")).replaceAll(":","-");
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            }
            String link = "http://pawelploszaj.pl/wykres_download.php?id_dane="+ id_dane;
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM dane WHERE id_dane = " + id_dane;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    String name = resultSet.getString("nazwa_pliku");
                    String fileName = File.separator + name + "-" + time;
                    DownloadFile(link, fileName, ".jpg");
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            } catch (IOException ex) {
                Notifications.setNotificationDownloadFailed();
                ex.printStackTrace();
            }
        });

        ShowChartUserParametersMaxAndMins_2.setOnAction(e -> {
            Object row = HistoryTableViewFiles.getSelectionModel().getSelectedItems().get(0);
            String id_wynik = row.toString().split(",")[0].substring(1); //id_wynik_p of selected record
            int id_dane = 0;
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM wyniki WHERE id_wynik = " + id_wynik;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    id_dane = resultSet.getInt("id_dane");
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            }
            String link = "http://pawelploszaj.pl/wykres_max_mins.php?id_dane="+ id_dane;
            Image img = new Image(link);
            ImageView imageView = new ImageView(img);
            StackPane secondaryLayout = new StackPane();
            secondaryLayout.getChildren().add(imageView);
            Scene secondScene = new Scene(secondaryLayout, 600, 400);
            secondScene.getRoot().setStyle("-fx-base:rgba(60, 63, 65, 255)");
            Stage newWindow = new Stage();
            newWindow.setTitle("Chart - id_wynik=" + id_wynik);
            newWindow.setScene(secondScene);
            newWindow.setResizable(false);
            newWindow.initModality(Modality.NONE);
            newWindow.setX(stage.getX() + 200);
            newWindow.setY(stage.getY() + 100);
            newWindow.show();
        });

        DownloadChartUserParametersMaxAndMins_2.setOnAction(e -> {
            Object row = HistoryTableViewFiles.getSelectionModel().getSelectedItems().get(0);
            String id_wynik = row.toString().split(",")[0].substring(1); //id_wynik_p of selected record
            int id_dane = 0;
            String time = "";
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM wyniki WHERE id_wynik = " + id_wynik;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    id_dane = resultSet.getInt("id_dane");
                    time = String.valueOf(resultSet.getTimestamp("datetime")).replaceAll(":","-");
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            }
            String link = "http://pawelploszaj.pl/wykres_max_mins_download.php?id_dane="+ id_dane;
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM dane WHERE id_dane = " + id_dane;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    String name = resultSet.getString("nazwa_pliku") + "-MaxAndMins";
                    String fileName = File.separator + name + "-" + time;
                    DownloadFile(link, fileName, ".jpg");
                }
            } catch(SQLException exception) {
                Notifications.setNotificationDownloadFailed();
            } catch (IOException ex) {
                Notifications.setNotificationDownloadFailed();
                ex.printStackTrace();
            }
        });

        DeleteRecord.setOnAction(e -> {
            Object row = DataTableView.getSelectionModel().getSelectedItems().get(0);
            String id_dane = row.toString().split(",")[0].substring(1);
            int id_pomiar_start = 0;
            int id_pomiar_end = 0;
            try (Connection connection = dataSource.getConnection()) {
                String query = "SELECT * FROM dane WHERE id_dane = " + id_dane;
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);
                while(resultSet.next()) {
                    id_pomiar_start = resultSet.getInt("id_pomiar_start");
                    id_pomiar_end = resultSet.getInt("id_pomiar_end");
                }
                String query_2 = "DELETE FROM dane WHERE id_dane = " + id_dane;
                Statement statement_2 = connection.createStatement();
                statement_2.execute(query_2);
                String query_3 = "ALTER TABLE dane AUTO_INCREMENT = 1";
                Statement statement_3 = connection.createStatement();
                statement_3.execute(query_3);
                String query_4 = "DELETE FROM pomiary WHERE id_pomiar >= " + id_pomiar_start + " AND id_pomiar<= " + id_pomiar_end;
                Statement statement_4 = connection.createStatement();
                statement_4.execute(query_4);
                String query_5 = "ALTER TABLE pomiary AUTO_INCREMENT = 1";
                Statement statement_5 = connection.createStatement();
                statement_5.execute(query_5);
                String query_6 = "DELETE FROM wyniki WHERE id_dane = " + id_dane;
                Statement statement_6 = connection.createStatement();
                statement_6.execute(query_6);
                String query_7 = "ALTER TABLE wyniki AUTO_INCREMENT = 1";
                Statement statement_7 = connection.createStatement();
                statement_7.execute(query_7);
                String query_8 = "SELECT * FROM wyniki_user_parameters WHERE id_dane = " + id_dane;
                Statement statement_8 = connection.createStatement();
                ResultSet resultSet_8 = statement_8.executeQuery(query_8);
                while(resultSet_8.next()) {
                    ObservableList<String> row_delete = FXCollections.observableArrayList();
                    for(int i=1 ; i<=resultSet_8.getMetaData().getColumnCount(); i++) {
                        row_delete.add(resultSet_8.getString(i));
                    }
                    ResultsTableView.getItems().remove(row_delete);
                }
                String query_9 = "DELETE FROM wyniki_user_parameters WHERE id_dane = " + id_dane;
                Statement statement_9 = connection.createStatement();
                statement_9.execute(query_9);
                String query_10 = "ALTER TABLE wyniki_user_parameters AUTO_INCREMENT = 1";
                Statement statement_10 = connection.createStatement();
                statement_10.execute(query_10);

                String query_11 = "DELETE FROM MaxesAndMins WHERE id_dane = " + id_dane;
                Statement statement_11 = connection.createStatement();
                statement_11.execute(query_11);
                String query_12 = "ALTER TABLE MaxesAndMins AUTO_INCREMENT = 1";
                Statement statement_12 = connection.createStatement();
                statement_12.execute(query_12);
            } catch(SQLException exception) {
                Notifications.setNotificationConnectionToDatabaseFailed();
            }
            UpdateTableViewData(DataTableView, "SELECT * from dane ORDER BY id_dane DESC", false);
            SetComboBoxData();
            refresh();
            setNotificationDeletedRecordSucessfully();
        });

        SelectDownloadPath.setOnAction(e -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File file = new File(SavePath);
            if(file.exists()) {
                directoryChooser.setInitialDirectory(file);
            } else {
                configuration.setSavePath(System.getProperty("user.home"));
                SavePath = System.getProperty("user.home");
                CurrentDownloadPath.setText(SavePath);
                File fileTemp = new File(SavePath);
                directoryChooser.setInitialDirectory(fileTemp);
            }
            File selectedDirectory = directoryChooser.showDialog(stage);
            if(selectedDirectory != null && !selectedDirectory.canWrite()) {
                setNotificationNoAccessToWriteFolder(selectedDirectory.getPath());
            }
            else if(selectedDirectory != null) {
                configuration.setSavePath(selectedDirectory.getPath());
                SavePath = selectedDirectory.getPath();
                CurrentDownloadPath.setText(SavePath);
                setNotificationSuccesfullChangeDownloadFolder(selectedDirectory.getPath());
            }
        });

    }

}