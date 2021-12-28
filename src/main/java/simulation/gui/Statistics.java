package simulation.gui;


import simulation.SimulationEngine;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Statistics {
    File fileCSV;
    public void saveToCSV(SimulationEngine engine, int engineNo, int chartValueNo) throws IOException {
        ArrayList<ArrayList<Double>> data = engine.getChartsInfo();
        ArrayList<String> columns = engine.getChartsOrder();
        if(engineNo == 1)
            fileCSV = new File("FoldedStatistics.csv");
        else
            fileCSV = new File("EdgedStatistics.csv");
        ArrayList<String[]> dataLine = new ArrayList<>();
        String[] S = new String[data.size()+1];
        S[0] = "Number of days:";
        for (int k = 0; k < data.size(); k++) {
            S[k+1] = columns.get(k);
        }
        dataLine.add(S);
        for(int i=chartValueNo;i<data.get(0).size();i++) {
            S = new String[data.size()+1];
            S[0] = String.valueOf(i-chartValueNo);
            for (int k = 0; k < data.size(); k++) {
                S[k+1] = String.valueOf(data.get(k).get(i));
            }
            dataLine.add(S);
        }
        Double[] average = engine.getChartsSummarize();
        S = new String[data.size()+1];
        S[0] = "Average";
        for (int k = 0; k < data.size(); k++) {
            S[k+1] = String.valueOf(average[k]);
        }
        dataLine.add(S);
        try (PrintWriter pw = new PrintWriter(fileCSV)) {
            dataLine.stream()
                    .map(this::convertToCSV)
                    .forEach(pw::println);
        }
    }

    public String convertToCSV(String[] data) {
        return Stream.of(data)
                .map(this::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

    public String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}
