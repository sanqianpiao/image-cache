package com.example;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ImageCacheImpl implements ImageCache {

    private final ImageService imageService;
    private String cacheDir;
    private final ConcurrentMap<String, Integer> leaseCount = new ConcurrentHashMap<>();

    public ImageCacheImpl(ImageService imageService, String cacheDir) {
        this.imageService = imageService;
        this.cacheDir = cacheDir;

        if (new File(this.cacheDir).exists() == false) {
            throw new IllegalArgumentException("Cache directory does not exist: " + this.cacheDir);
        }
    }

    private String filename(String url) {
        try {
            return URLEncoder.encode(url, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private File locateCacheFile(String filename) {
        return new File(cacheDir + filename);
    }

    @Override
    public File lease(String url) throws ImageCacheException {
        String filename = filename(url);
        leaseCount.compute(filename, (k, v) -> v == null ? 1 : v + 1);

        File file = locateCacheFile(filename);
        if (file.exists()) return file;

        synchronized (this) {
            if (file.exists()) return file;

            byte[] bytes = imageService.get(url);
            try {
                Files.write(bytes, file);
            } catch (IOException e) {
                e.printStackTrace();
                throw new ImageCacheException("Writing Cache File Failed. url: " + url);
            }
        }

        return file;
    }

    @Override
    public void release(String url) throws ImageCacheException {
        String filename = filename(url);
        if (leaseCount.containsKey(filename) == false)
            throw new ImageCacheException("Cached File Does Not Exist. url: " + url);

        synchronized (this) {
            leaseCount.compute(filename, (k, v) -> v - 1);
            if (leaseCount.get(filename) == 0) {
                leaseCount.remove(filename);
                File file = locateCacheFile(filename);
                file.delete();
            }
        }
    }
}
