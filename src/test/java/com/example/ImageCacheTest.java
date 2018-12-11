package com.example;

import com.google.common.io.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class ImageCacheTest {
    private ImageCache imageCache;

    private ImageService mockImageService;

    private File tempDir;

    @Before
    public void setUp() {
        mockImageService = mock(ImageService.class);
        when(mockImageService.get(any())).thenReturn(new byte[0]);

        tempDir = Files.createTempDir();
        System.out.println(tempDir);

        imageCache = new ImageCacheImpl(mockImageService, tempDir.getAbsolutePath());
    }

    @After
    public void tearDown() {
        tempDir.delete();
    }

    @Test
    public void leaseReturnsFromCacheWhenPresent() throws Exception {
        String imageUrl = "http://canva-interview.com/image.png";

        // populate the cache
        File image1 = imageCache.lease(imageUrl);

        // request the same image a second time
        File image2 = imageCache.lease(imageUrl);

        // ensure that the resolver was only called once with this url
        verify(mockImageService, times(1)).get(imageUrl);
        // and that each subsequent call to #lease returned the same File reference for the cached image
        assertEquals(image1, image2);

        // release the image as many times as leased to enable cleanup
        imageCache.release(imageUrl);
        imageCache.release(imageUrl);
    }

    @Test
    public void leaseDoesNotReturnTheSameImageForDifferentUrls() throws Exception {
        String image1Url = "http://canva-interview.com/image1.png";
        File image1 = imageCache.lease(image1Url);
        String image2Url = "http://canva-interview.com/image2.png";
        File image2 = imageCache.lease(image2Url);

        assertNotEquals(image1, image2);

        // release the leased images to enable cleanup
        imageCache.release(image1Url);
        imageCache.release(image2Url);
    }
}
