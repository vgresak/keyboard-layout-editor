package cz.gresak.keyboardeditor.service.impl;

import com.google.gson.Gson;
import cz.gresak.keyboardeditor.model.KeyboardModel;
import cz.gresak.keyboardeditor.service.api.KeyboardModelLoader;
import cz.gresak.keyboardeditor.service.api.PredefinedKeyboardModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class KeyboardModelLoaderImpl implements KeyboardModelLoader {

    @Override
    public KeyboardModel load(PredefinedKeyboardModel model) {
        try (InputStream is = getClass().getResourceAsStream(model.getPath())) {
            return load(is);
        } catch (IOException e) {
            throw new KeyboardModelLoaderException(e);
        }
    }

    @Override
    public KeyboardModel load(File file) {
        try (InputStream is = new FileInputStream(file)) {
            return load(is);
        } catch (IOException e) {
            throw new KeyboardModelLoaderException(e);
        }
    }

    private KeyboardModel load(InputStream is) throws IOException {
        try (Reader reader = new BufferedReader(new InputStreamReader(is))) {
            Gson gson = new Gson();
            return gson.fromJson(reader, KeyboardModel.class);
        }
    }

}
