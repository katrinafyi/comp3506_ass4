import java.util.*;
import java.util.stream.Collectors;

/**
 * Class that implements the social media feed searches
 */
public class FeedAnalyser {

    private HashMap<String, TreeMap<Date, List<FeedItem>>>
            userPostsMap = new HashMap<>();

    private ArrayList<FeedItem> postsById = new ArrayList<>();
    private ArrayList<FeedItem> postsByUpvotes = new ArrayList<>();
    private int upvotesIndex = 0;

    /**
     * Loads social media feed data from a file
     *
     * @param filename the file to load from
     */
    public FeedAnalyser(String filename) {
        Iterator<FeedItem> iter = new Util.FileIterator(filename);
        while (iter.hasNext()) {
            FeedItem item = iter.next();
            String user = item.getUsername();
            userPostsMap.computeIfAbsent(user, k -> new TreeMap<>())
                    .computeIfAbsent(item.getDate(), d -> new ArrayList<>())
                    .add(item);
            postsByUpvotes.add(item);
            postsById.add(item);
        }

        // sort in ASCENDING order of ID
        postsById.sort(Comparator.comparingLong(FeedItem::getId));

        // sort in DESCENDING order of upvotes
        postsByUpvotes.sort((a, b) -> b.getUpvotes() - a.getUpvotes());
    }

    /**
     * Return all feed items posted by the given username between startDate and endDate (inclusive)
     * If startDate is null, items from the beginning of the history should be included
     * If endDate is null, items until the end of the history should be included
     * The resulting list should be ordered by the date of each FeedItem
     * If no items that meet the criteria can be found, the empty list should be returned
     *
     * @param username the user to search the posts of
     * @param startDate the date to start searching from
     * @param endDate the date to stop searching at
     * @return a list of FeedItems made by username between startDate and endDate
     *
     * @require username != null
     * @ensure result != null
     */
    public List<FeedItem> getPostsBetweenDates(String username, Date startDate, Date endDate) {
        TreeMap<Date, List<FeedItem>> userPosts = userPostsMap.get(username);
        if (userPosts == null) // user has no posts.
            return Collections.emptyList();

        Map<Date, List<FeedItem>> matchedItems;
        if (startDate == null && endDate == null) {
            matchedItems = userPosts;
        } else if (startDate == null) {
            // get all items before endDate
            matchedItems = userPosts.headMap(endDate, true);
        } else if (endDate == null) {
            // get items after startDate
            matchedItems = userPosts.tailMap(startDate, true);
        } else {
            // get items bounded by startDate and endDate.
            matchedItems = userPosts.subMap(startDate, true, endDate, true);
        }

        List<FeedItem> results = new ArrayList<>();
        for (List<FeedItem> dateList : matchedItems.values()) {
            // collect all dates together into a list of results. will be
            // ordered by date because TreeMap is ordered by date.
            results.addAll(dateList);
        }
        return results;
    }

    /**
     * Return the first feed item posted by the given username at or after searchDate
     * That is, the feed item closest to searchDate that is greater than or equal to searchDate
     * If no items that meet the criteria can be found, null should be returned
     *
     * @param username the user to search the posts of
     * @param searchDate the date to start searching from
     * @return the first FeedItem made by username at or after searchDate
     *
     * @require username != null && searchDate != null
     */
    public FeedItem getPostAfterDate(String username, Date searchDate) {
        Map.Entry<Date, List<FeedItem>> entry = userPostsMap.get(username)
                .ceilingEntry(searchDate);
        if (entry == null || entry.getValue().size() == 0)
            return null; // no items matched
        return entry.getValue().get(0); // get first element with that date
    }

    /**
     * Return the feed item with the highest upvote
     * Subsequent calls should return the next highest item
     *     i.e. the nth call to this method should return the item with the nth highest upvote
     * Posts with equal upvote counts can be returned in any order
     *
     * @return the feed item with the nth highest upvote value,
     *      where n is the number of calls to this method
     * @throws NoSuchElementException if all items in the feed have already been returned
     *      by this method
     */
    public FeedItem getHighestUpvote() throws NoSuchElementException {
        return postsByUpvotes.get(upvotesIndex++);
    }

    /**
     * Returns the lowest index at which substring pattern begins in text
     * (or else -1).
     *
     * This is taken from Goodrich, Tamassia and Goldwasser's sample code
     * for the textbook Data Structures and Algorithms in Java, Sixth Edition.
     * The program is licensed under the GPLv3.
     *
     * @param text Text to search in, as a char array.
     * @param pattern Pattern so search for, as char array.
     * @return Lowest index of match or -1 if no match.
     */
    public static int findBoyerMoore(char[] text, char[] pattern) {
        int n = text.length;
        int m = pattern.length;
        if (m == 0) return 0;                            // trivial search for empty string
        Map<Character,Integer> last = new HashMap<>();   // the 'last' map
        for (int i=0; i < n; i++)
            last.put(text[i], -1);               // set -1 as default for all text characters
        for (int k=0; k < m; k++)
            last.put(pattern[k], k);             // rightmost occurrence in pattern is last
        // start with the end of the pattern aligned at index m-1 of the text
        int i = m-1;                                     // an index into the text
        int k = m-1;                                     // an index into the pattern
        while (i < n) {
            if (text[i] == pattern[k]) {                   // a matching character
                if (k == 0) return i;                        // entire pattern has been found
                i--;                                         // otherwise, examine previous
                k--;                                         // characters of text/pattern
            } else {
                i += m - Math.min(k, 1 + last.get(text[i])); // case analysis for jump step
                k = m - 1;                                   // restart at end of pattern
            }
        }
        return -1;                                       // pattern was never found
    }

    /**
     * Return all feed items containing the specific pattern in the content field
     * Case should not be ignored, eg. the pattern "hi" should not be matched in the text "Hi there"
     * The resulting list should be ordered by FeedItem ID
     * If the pattern cannot be matched in any content fields the empty list should be returned
     *
     * @param pattern the substring pattern to search for
     * @return all feed items containing the pattern string
     *
     * @require pattern != null && pattern.length() > 0
     * @ensure result != null
     */
    public List<FeedItem> getPostsWithText(String pattern) {
        char[] patternArray = pattern.toCharArray();
        List<FeedItem> results = new ArrayList<>();

        for (FeedItem item : postsById) {
            if (findBoyerMoore(item.getContent().toCharArray(),
                    patternArray) != -1) {
                results.add(item);
            }
        }

        return results;
    }
}
