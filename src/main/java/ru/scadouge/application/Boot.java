package ru.scadouge.application;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class Boot {
    private static final Logger log = Logger.getLogger(Boot.class.getName());
    public static void main(String[] args) {

        Updater updater = new Updater(getApi());
        updater.update();
        launch(args);
    }

    private static void launch(String[] args) {
        log.info("start launcher");
        File launcher = new File("winter-launcher.jar");
        List<String> argsList = new ArrayList<>();
        argsList.add ("java");
        argsList.add ("-jar");
        argsList.add (launcher.getAbsolutePath());
        argsList.addAll(Arrays.asList(args));
        try {
            ProcessBuilder pb = new ProcessBuilder(argsList);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
//                log.info(line);
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getApi() {
        String api = null;
        Properties properties = null;
        try {
            properties = readPropertiesFile("application.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(properties != null)
            api = properties.getProperty("nexus.api");
        if(api == null)
            throw new RuntimeException("failed to start launcher: application.properties is null");
        return api;
    }

    private static Properties readPropertiesFile(String fileName) throws IOException {
        FileInputStream fis = null;
        Properties prop = null;
        try {
            fis = new FileInputStream(fileName);
            prop = new Properties();
            prop.load(fis);
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            assert fis != null;
            fis.close();
        }
        return prop;
    }
}
