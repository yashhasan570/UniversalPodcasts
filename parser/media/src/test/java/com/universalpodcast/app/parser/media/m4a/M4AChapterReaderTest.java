package com.universalpodcast.app.parser.media.m4a;

import com.universalpodcast.app.model.feed.Chapter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class M4AChapterReaderTest {

    @Test
    public void testFiles() throws IOException {
        testFile();
    }

    public void testFile() throws IOException {
        InputStream inputStream = getClass().getClassLoader()
                .getResource("nero-chapters.m4a").openStream();
        M4AChapterReader reader = new M4AChapterReader(inputStream);
        reader.readInputStream();
        List<Chapter> chapters = reader.getChapters();

        assertEquals(4, chapters.size());

        assertEquals(0, chapters.get(0).getStart());
        assertEquals(3000, chapters.get(1).getStart());
        assertEquals(6000, chapters.get(2).getStart());
        assertEquals(9000, chapters.get(3).getStart());

        assertEquals("Chapter 1 - ❤️😊", chapters.get(0).getTitle());
        assertEquals("Chapter 2 - ßöÄ", chapters.get(1).getTitle());
        assertEquals("Chapter 3 - 爱", chapters.get(2).getTitle());
        assertEquals("Chapter 4", chapters.get(3).getTitle());
    }
}
