package com.pbs.edu.opticalspectraanalysis;

import java.io.FileReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.opencsv.CSVReader;
import com.zaxxer.hikari.HikariDataSource;
import javafx.concurrent.Task;
import static com.pbs.edu.opticalspectraanalysis.UIController.get_data_source;


public class ImportCSV extends Task<Void> {

    private static Path pathToFile = null;

    public ImportCSV(Path pathToFile) {
        ImportCSV.pathToFile = pathToFile;
    }


    @Override
    protected Void call() throws Exception {
        HikariDataSource dataSource = get_data_source();
        String filename = pathToFile.getFileName().toString();
        CSVReader reader = new CSVReader(new FileReader(String.valueOf(pathToFile)));
        List<String> elementList = new ArrayList<>();
        String[] line;
        while ((line = reader.readNext()) != null) {
            for (int i = 0; i < line.length; i++) {
                elementList.addAll(Arrays.asList(line[i].replaceAll("\\s*,\\s*", ",").split(",")));
            }
        }
        double CTRWL = Double.parseDouble(elementList.get(4));
        double SPAN = Double.parseDouble(elementList.get(6));
        double STARTWL = Double.parseDouble(elementList.get(8));
        double STOPWL = Double.parseDouble(elementList.get(10));
        int WLFREQ = Integer.parseInt(elementList.get(12).replace(" ", ""));
        double REFL = Double.parseDouble(elementList.get(14));
        double LSCL = Double.parseDouble(elementList.get(16));
        double BASEL = Double.parseDouble(elementList.get(18));
        double RESLN = Double.parseDouble(elementList.get(20));
        int AVG = Integer.parseInt(elementList.get(22).replace(" ", ""));
        int SMPLAUTO = Integer.parseInt(elementList.get(24).replace(" ", ""));
        int SMPL = Integer.parseInt(elementList.get(26).replace(" ", ""));
        double SMPLINTVL = Double.parseDouble(elementList.get(28));
        String SENSITIVITY = elementList.get(29);
        String TYPE = elementList.get(30);
        int LSUNT = Integer.parseInt(elementList.get(32).replace(" ", ""));
        String NMSKH = elementList.get(34);
        int OPTATT = Integer.parseInt(elementList.get(36).replace(" ", ""));
        int id_pomiar_start = 0;
        int id_dane = 0;
        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM dane ORDER BY id_dane DESC LIMIT 1";
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                id_dane = resultSet.getInt("id_dane") + 1;
                id_pomiar_start = resultSet.getInt("id_pomiar_end") + 1;
            }
        } catch (SQLException exception) {
            Notifications.setNotificationTransferToDatabaseFailed();
        }
        int id_pomiar_end = id_pomiar_start + SMPL - 1;
        try (Connection connection = dataSource.getConnection()) {
            String query = "INSERT INTO dane (`nazwa_pliku`,`id_pomiar_start`, `id_pomiar_end`, `CTRWL`, `SPAN`, `START_WL`, `STOP_WL`, `WLFREQ`, `REFL`, `LSCL`, `BASEL`, `RESLN`, `AVG`, `SMPLAUTO`, `SMPL`, `SMPLINTVL`, `SENSITIVITY`, `TYPE`, `LSUNT`, `NMSKH`,`OPTATT`, `ilosc_maximow`) VALUES ('" + filename + "','" + id_pomiar_start + "','" + id_pomiar_end + "','" + CTRWL + "','" + SPAN + "','" + STARTWL + "','" + STOPWL + "','" + WLFREQ + "','" + REFL + "','" + LSCL + "','" + BASEL + "','" + RESLN + "','" + AVG + "','" + SMPLAUTO + "','" + SMPL + "','" + SMPLINTVL + "','" + SENSITIVITY + "','" + TYPE + "','" + LSUNT + "','" + NMSKH + "','" + OPTATT + "','" + 0 + "')";
            Statement statement = connection.createStatement();
            statement.execute(query);
        } catch (SQLException exception) {
            Notifications.setNotificationTransferToDatabaseFailed();
        }
        int measurementId = id_pomiar_start;
        int maxValue = (SMPL - 1) * 2;

        double globalMaxX = 0;
        double globalMaxY = 0;
        int dataCounter = 0;
        int dataIndex = id_pomiar_end-id_pomiar_start+1;
        Data[] data = new Data[dataIndex];

        for (int i = 0; i <= maxValue; i = i + 2) {
            double x = Double.parseDouble(elementList.get(40 + i));
            double y = Double.parseDouble(elementList.get(41 + i));
            try (Connection connection = dataSource.getConnection()) {
                String query = "INSERT INTO pomiary (`id_pomiar`,`x`, `y`) VALUES ('" + measurementId + "','" + x + "','" + y + "')";
                Statement statement = connection.createStatement();
                statement.execute(query);
                if(y >= 0.00003) {
                    data[dataCounter] = new Data(measurementId, x, y);
                    dataCounter++;
                }
                updateProgress(i,maxValue);
            } catch (SQLException exception) {
                Notifications.setNotificationTransferToDatabaseFailed();
            }
            measurementId++;
        }

        try (Connection connection = dataSource.getConnection()) {
            dataIndex = dataCounter;
            id_pomiar_end = data[dataCounter-1].getIndex();
            String queryGetMaxY = "SELECT MAX(y) FROM pomiary WHERE id_pomiar >= " + id_pomiar_start + " AND id_pomiar <= " + id_pomiar_end;
            Statement statementGetMaxY = connection.createStatement();
            ResultSet resultSetGetMaxY = statementGetMaxY.executeQuery(queryGetMaxY);
            while(resultSetGetMaxY.next()) {
                globalMaxY = resultSetGetMaxY.getDouble("MAX(y)");
            }
            String queryGetMaxX = "SELECT * FROM pomiary WHERE id_pomiar >= " + id_pomiar_start + " AND id_pomiar <= " + id_pomiar_end + " AND y = " + globalMaxY;
            Statement statementGetMaxX = connection.createStatement();
            ResultSet resultSetGetMaxX = statementGetMaxX.executeQuery(queryGetMaxX);
            while(resultSetGetMaxX.next()) {
                globalMaxX = resultSetGetMaxX.getDouble("x");
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
        List<Double> minYValues = new ArrayList<>();
        List<Double> minXValues = new ArrayList<>();
        List<Double> maxXValues = new ArrayList<>();
        List<Double> maxYValues = new ArrayList<>();
        int globalMaxWavePositionIndex = 0;
        double Imin;
        double Imax;
        double yHelper = 0;
        updateMessage("Calculating and Uploading results !");
        for(int k = 0; k <= dataPointCounter*2; k++) {
            updateProgress(k,dataPointCounter* 2L);
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
                        String query_insert = "INSERT INTO MaxesAndMins (`id_dane`,`x`,`y`) VALUES ('" + id_dane + "','" + x + "','" + y + "')";
                        Statement statement_insert = connection.createStatement();
                        statement_insert.execute(query_insert);
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
                        if(dataPointCounter == 1) { x = globalMaxX; y = globalMaxY;}
                        maxXValues.add(x);
                        maxYValues.add(y);
                        String query_insert = "INSERT INTO MaxesAndMins (`id_dane`,`x`,`y`) VALUES ('" + id_dane + "','" + x + "','" + y + "')";
                        Statement statement_insert = connection.createStatement();
                        statement_insert.execute(query_insert);
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
        if(!max_right_position.equals(BigDecimal.valueOf(maxXValues.get(0)))) { x = (max_left_position.multiply(max_right_position).multiply(BigDecimal.valueOf(dataPointCounter))).divide((max_right_position.subtract(max_left_position)).multiply(BigDecimal.valueOf(2)), 4, RoundingMode.HALF_UP); }
        if(averageDistance.equals(BigDecimal.valueOf(0))) { averageDistance = BigDecimal.valueOf(0); }
        else {
            averageDistance = averageDistance.divide(BigDecimal.valueOf((long)dataPointCounter - (long)1), 4, RoundingMode.HALF_UP);
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
        try (Connection connection = dataSource.getConnection()) {
            String query = "INSERT INTO wyniki (`id_wynik`,`id_dane`,`V`, `x`, `global_max_X`, `global_max_Y`, `srednia_odl`, `odl_central_lewo`, `odl_central_prawo`, `absorpcja`) VALUES ('" + id_dane + "','" + id_dane + "','" + V.doubleValue() + "','" + x.doubleValue() + "','" + globalMaxX + "','" + globalMaxY + "','" + averageDistance.doubleValue() + "','" + central_left_distance + "','" + central_right_distance + "','" + Imin + "')";
            Statement statement = connection.createStatement();
            statement.execute(query);
            String query_2 = "UPDATE dane SET ilosc_maximow = " + dataPointCounter + " WHERE id_dane = " + id_dane + "";
            Statement statement2 = connection.createStatement();
            statement2.execute(query_2);
        } catch(SQLException exception) {
            Notifications.setNotificationConnectionToDatabaseFailed();
        }

        return null;
    }


}
