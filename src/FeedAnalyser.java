import java.util.*;
import java.util.stream.Collectors;

/**
 * Class that implements the social media feed searches
 */
public class FeedAnalyser {

    private HashMap<String, ArrayList<FeedItem>>
            userPostsMap = new HashMap<>();

    private HashMap<String, ArrayList<Date>> userDatesMap = new HashMap<>();

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
            userPostsMap.computeIfAbsent(user, k -> new ArrayList<>())
                    .add(item);
            postsByUpvotes.add(item);
        }


        // sort in DESCENDING order of upvotes
        postsByUpvotes.sort((a, b) -> b.getUpvotes() - a.getUpvotes());

        // sort each user's feed item list. O(n log n)?
        userPostsMap.forEach((user, list) -> list.sort(
                (a, b) -> a.getDate().compareTo(b.getDate())));

        // map each sorted feed item to its date in the userDatesMap.
        for (Map.Entry<String, ArrayList<FeedItem>> entry
                : userPostsMap.entrySet()) {
            userDatesMap.put(entry.getKey(), new ArrayList<>(
                    entry.getValue().stream()
                            .map(FeedItem::getDate)
                            .collect(Collectors.toList())));
        }
    }

    /**
     * Returns the smallest i such that upperBound < list[i+1]. That is, the
     * index of the rightmost element <= upperBound.
     * @param list
     * @param bound
     * @param <T>
     * @return
     */
    // TODO: private this
    public static <T extends Comparable<T>> int findTightestBound(
            List<T> list, T bound, boolean upperBound) {
        if (list.size() == 1) {
            int mult = upperBound ? 1 : -1;
            return (mult * list.get(0).compareTo(bound)) <= 0 ? 0 : -mult;
        }

        int mid = list.size() / 2;
        if (list.get(mid).compareTo(bound) > 0) {
            // bound is in left half
            return findTightestBound(list.subList(0, mid), bound, upperBound);
        } else {
            // bound is in right half
            return mid + findTightestBound(
                    list.subList(mid, list.size()), bound, upperBound);
        }
    }

    public static <T extends Comparable<T>> int leastUpperBound(
            List<T> list, T upperBound) {
        return findTightestBound(list, upperBound, true);
    }

    public static <T extends Comparable<T>> int greatestLowerBound(
            List<T> list, T lowerBound) {
        return findTightestBound(list, lowerBound, false);
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
        List<FeedItem> userPosts = userPostsMap.get(username);
        List<Date> userDates = userDatesMap.get(username);
        int lower, upper;
        if (startDate != null) {
            lower = greatestLowerBound(userDates, startDate);
        } else {
            lower = 0;
        }
        if (endDate != null) {
            upper = leastUpperBound(userDates, endDate);
            if (upper < userDates.size())
                upper += 1; // because upper end is exclusive in subList()
        } else {
            upper = userDates.size();
        }
        return userPosts.subList(lower, upper);
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
        List<FeedItem> postsAfter = getPostsBetweenDates(username, searchDate, null);
        return postsAfter.size() > 0 ? postsAfter.get(0) : null;
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
        return null;
    }
}
