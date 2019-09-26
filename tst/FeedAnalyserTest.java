import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class FeedAnalyserTest {

    private FeedAnalyser sampleAnalyser;
    private static FeedItem[] sampleFeed;
    private FeedAnalyser analyser2;

    static {
        sampleFeed = new FeedItem[11];
        Iterator<FeedItem> iter = new Util.FileIterator("tst/feed-sample.csv");
        while (iter.hasNext()) {
            FeedItem next = iter.next();
            sampleFeed[(int)next.getId()] = next;
        }
    }

    @Before
    public void setup() {
        sampleAnalyser = new FeedAnalyser("tst/feed-sample.csv");
        analyser2 = new FeedAnalyser("tst/feed-sample2.csv");
    }

    @Test
    public void testLeastUpperBound() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
        List<Integer> list2 = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 50, 60);
        assertEquals(3, FeedAnalyser.leastUpperBound(list, 4));
        assertEquals(4, FeedAnalyser.leastUpperBound(list, 5));
        assertEquals(0, FeedAnalyser.leastUpperBound(list, 1));
        assertEquals(6, FeedAnalyser.leastUpperBound(list, 100));

        assertEquals(8, FeedAnalyser.leastUpperBound(list2, 100));
        assertEquals(7, FeedAnalyser.leastUpperBound(list2, 59));
        assertEquals(8, FeedAnalyser.leastUpperBound(list2, 60));
        assertEquals(6, FeedAnalyser.leastUpperBound(list2, 8));
        assertEquals(-1, FeedAnalyser.leastUpperBound(list2, 0));

        assertEquals(0, FeedAnalyser.greatestLowerBound(list, 1));
        assertEquals(0, FeedAnalyser.greatestLowerBound(list, -10));
        assertEquals(1, FeedAnalyser.greatestLowerBound(list, 2));
        assertEquals(7, FeedAnalyser.greatestLowerBound(list2, 49));

        List<Integer> listOne = Arrays.asList(2);
        assertEquals(1, FeedAnalyser.greatestLowerBound(listOne, 49));
        assertEquals(0, FeedAnalyser.greatestLowerBound(listOne, 2));
        assertEquals(0, FeedAnalyser.greatestLowerBound(listOne, -1));

        List<Integer> listThree = Arrays.asList(1, 3, 5);
        assertEquals(1, FeedAnalyser.leastUpperBound(listThree, 3));
        assertEquals(1, FeedAnalyser.greatestLowerBound(listThree, 3));
        assertEquals(0, FeedAnalyser.leastUpperBound(listThree, 2));
        assertEquals(1, FeedAnalyser.greatestLowerBound(listThree, 2));
    }

    @Test(timeout=1000)
    public void testGetPostsBetweenDates() {
        assertEquals(Collections.singletonList(sampleFeed[6]),
                sampleAnalyser.getPostsBetweenDates("tom",
                        Util.parseDate("03/01/2019 12:00:00"),
                        Util.parseDate("03/01/2019 14:00:00")));

        assertEquals(Arrays.asList(sampleFeed[5], sampleFeed[7]),
                sampleAnalyser.getPostsBetweenDates("emily",
                        Util.parseDate("03/01/2019 12:00:00"),
                        Util.parseDate("03/01/2019 14:00:00")));

        assertEquals(new ArrayList<>(),
                sampleAnalyser.getPostsBetweenDates("emily",
                        Util.parseDate("03/01/2019 12:00:00"),
                        Util.parseDate("03/01/2019 12:00:01")));


    }

    @Test(timeout=1000)
    public void testGetPostAfterDate() {
        assertEquals(sampleFeed[1],
                sampleAnalyser.getPostAfterDate("james.gvr",
                        Util.parseDate("01/01/2019 07:00:00")));

        assertEquals(sampleFeed[8],
                sampleAnalyser.getPostAfterDate("hob",
                        Util.parseDate("01/01/2019 07:00:00")));
    }

    @Test(timeout=1000)
    public void testGetHighestUpvote() {
        assertEquals(sampleFeed[8], sampleAnalyser.getHighestUpvote());
        assertEquals(sampleFeed[9], sampleAnalyser.getHighestUpvote());
    }

    @Test(timeout=1000)
    public void testGetPostsWithText() {
        assertEquals(Collections.singletonList(sampleFeed[2]),
                sampleAnalyser.getPostsWithText("jiaozi"));

        assertEquals(Arrays.asList(sampleFeed[4], sampleFeed[5], sampleFeed[9]),
                sampleAnalyser.getPostsWithText("no"));
    }

    @Test
    public void testBoyerMoore() {
        sampleAnalyser.getPostsWithText("CCTTTTGC");
        sampleAnalyser.getPostsWithText("T");
    }

    @Test
    public void testDate() {
        System.out.println(Arrays.toString(analyser2.getPostsBetweenDates("james.gvr",
                Util.parseDate("04/01/2019 10:00:00"),
                Util.parseDate("04/01/2019 10:01:00")).toArray()));
    }
}
