package com.alphatica.genotick.data;


import com.alphatica.genotick.ui.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class FileSystemDataLoader implements DataLoader {
    private final String [] paths;
    private final UserOutput output = UserInputOutputFactory.getUserOutput();
    public FileSystemDataLoader(String... args) {
        paths = args;
    }

    @Override
    public MainAppData createRobotData() throws DataException {
        return loadData();
    }

    private MainAppData loadData() {
        MainAppData data = new MainAppData();
        String extension = ".csv";
        List<String> names = DataUtils.listFiles(extension,paths);
        if(names == null) {
            throw new DataException("Unable to list files");
        }
        for (String name : names) {
            output.infoMessage("Reading file " + name);
            data.addDataSet(createDataSet(name));
        }
        if(data.isEmpty()) {
            throw new DataException("No files to read!");
        }
        return data;

    }
    private DataSet createDataSet(String name) {
        try(BufferedReader br = new BufferedReader(new FileReader(new File(name)))) {
            List<Number []> lines = DataUtils.createLineList(br);
            output.infoMessage("Got " + lines.size() + " lines");
            return new DataSet(lines,name);
        } catch (IOException  | DataException e) {
            DataException de = new DataException("Unable to process file: " + name);
            de.initCause(e);
            throw de;
        }
    }
}


