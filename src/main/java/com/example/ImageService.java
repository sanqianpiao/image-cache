package com.example;

public interface ImageService {
  /**
   * Downloads the image referenced by {@code url}.
   *
   * @param url the image to download.
   * @return the image data.
   */
  byte[] get(String url);
}
