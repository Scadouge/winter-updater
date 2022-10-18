package ru.scadouge.application;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.logging.Logger;

public class Updater {
    private static final Logger log = Logger.getLogger(Updater.class.getName());
    private final String api;
    private final static String GAME = "launcher";

    public Updater(String api) {
        this.api = api;
    }

    public void update() {
        compareFileAndDownload("winter-launcher.jar");
    }

    private File downloadFile(String remoteFile, String filePath) throws IOException {
        log.info("download file " + GAME + ":" + remoteFile + " to " + filePath);
        String url = String.format("http://%s/download?game=%s&file=%s", getApi(), GAME, remoteFile);
        File file = new File(filePath);
        FileUtils.copyURLToFile(new URL(url), file);
        return file;
    }

    private String getHash(String remoteFile) {
        String hash = null;
        String url = String.format("http://%s/md5?game=%s&file=%s", getApi(), GAME, remoteFile);
        log.info("get hash from " + url);

        try {
            HttpGet request = new HttpGet(url);
            CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            hash = EntityUtils.toString(entity);
            if(hash == null || hash.isEmpty()) {
                log.info("remote file return null hash " + GAME + ":" + remoteFile);
                hash = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("hash: " + hash);
        return hash;
    }

    public boolean compareFileAndDownload(String shortPath) {
        File localFile  = new File(shortPath);
        //new File(localFile.getParent()).mkdirs();
        String localHash = null;
        if(localFile.exists()) {
            log.info("local path: " + localFile.toPath());
            try (InputStream is = Files.newInputStream(localFile.toPath())) {
                localHash = org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String remotePath = shortPath.replace("\\", "/");
        String remoteHash = getHash(remotePath);
        log.info("start compare md5 \n remote " + remotePath + ":" + remoteHash +
                "\n local " + localFile.getPath() + ":" + localHash);
        if(remoteHash != null) {
            if(localHash == null || !localHash.equals(remoteHash)) {
                log.info("remote file is not equal to local file");
                try {
                    downloadFile(remotePath, localFile.getPath());
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(localHash != null && localHash.equals(remoteHash))
                log.info("remote file is equal to local file");
        } else
            log.info("remote hash is null, cancel download");
        return false;
    }

    public String getApi() {
        return api;
    }
}
