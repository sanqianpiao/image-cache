package com.example;

import java.io.File;

/**
 * Caches images. The caching mechanism is left to the implementor. However, it should abide by the following contract:
 * <ul>
 *  <li>
 *    If an image doesn't exist in the cache, it will be downloaded and added when first leased
 *    (through {@link ImageCache#lease(String)}.
 *  </li>
 *  <li>
 *   Downloading an image or returning one from the cache should be transparent to the caller.
 *   i.e. Leasing an image (through {@link ImageCache#lease(String)}) will return a {@link File} reference to an image,
 *   regardless of its presences in the cache before the call to {@link ImageCache#lease(String)}.
 *  </li>
 *  <li>
 *   An image will exist in the cache (at least) until all leases have been released
 *   (through {@link ImageCache#release(String)})
 *  </li>
 * </ul>
 */
public interface ImageCache {
  /**
   * Downloads an image represented by {@code url} or returns a previously downloaded image. Regardless, until a leased
   * image is released, the file should exist on the file system for other processes to access.
   *
   * @param url the url of the image to download.
   * @return a reference to the downloaded image.
   */
  File lease(String url) throws ImageCacheException;

  /**
   * Releases an image from the cache. When an image is released, it is no longer safe for another process to access the
   * referenced image file as it may be deleted. When a released image file is deleted it up to the implementer.
   *
   * @param url the original url of the image that was leased.
   */
  void release(String url) throws ImageCacheException;

  class ImageCacheException extends Exception {
    ImageCacheException(String message, Throwable cause) {
      super(message, cause);
    }

    public ImageCacheException(String message) {
      super(message);
    }
  }
}
