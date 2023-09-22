package com.onyx.darie.calin.gentleglowonyxboox.storage;

import com.google.gson.Gson;
import com.onyx.darie.calin.gentleglowonyxboox.util.Result;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class FileStorage<T> implements Storage<T> {
    public FileStorage(File directory, String fileName) {
        this.directory = directory;
        this.fileName = fileName;
    }

    public Result save(T data) {
        try {
            writeFile(getFile(), json.toJson(data));
            return Result.success();
        }
        catch (IOException e){
            return Result.error(e.toString());
        }
    }

    public Result<T> loadOrDefault(T defaultValue) {
        File file = getFile();
        if (! file.exists())
            return Result.success(defaultValue);

        byte[] bytes;
        try {
            bytes = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            return Result.error(e.toString());
        }
        String dataAsJson = new String(bytes);
        return Result.success(json.fromJson(dataAsJson, defaultValue.getClass()));
    }

    void writeFile(File file, String data) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file, false)) {
            byte[] contents = data.getBytes();
            out.write(contents);
            out.flush();
        }
    }

    private File getFile() {
        return new File(directory, fileName);
    }

    private final Gson json = new Gson();
    private final File directory;
    private final String fileName;
}
