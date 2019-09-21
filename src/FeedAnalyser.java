import java.util.*;
import java.util.stream.Collectors;

/**
 * Class that implements the social media feed searches
 */
public class FeedAnalyser {

    private HashMap<String, ArrayList<FeedItem>>
            userPostsMap = new HashMap<>();

    private HashMap<String, ArrayList<Date>> userDatesMap = new HashMap<>();

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
            userPostsMap.computeIfAbsent(user, k -> new ArrayList<>())
                    .add(item);
            postsByUpvotes.add(item);
            postsById.add(item);
        }

        // sort in ASCENDING order of ID
        postsById.sort(Comparator.comparingLong(FeedItem::getId));

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


    private static Map<Character, int[]> buildBadCharMap(String pattern) {
        Map<Character, int[]> map = new HashMap<>();
        char[] charArray = pattern.toCharArray();
        Set<Character> charSet = new HashSet<>();
        for (char value : charArray)
            charSet.add(value);

        // for each possible character in the pattern
        for (Character c : charSet) {
            int[] shifts = new int[charArray.length];
            map.put(c, shifts);
            // for each bad character position. that is, if we see this char
            // c at position i
            for (int i = charArray.length-1; i >= 0; i--) {
                shifts[i] = -1; // shift past the string
                // iterate backwards to find the next shift
                for (int j = i-1; j >= 0; j--) {
                    if (charArray[j] == c) {
                        shifts[i] = j;
                        break;
                    }
                }
            }
        }
        return map;
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
        Map<Character, int[]> badCharMap = buildBadCharMap(pattern);
        List<FeedItem> results = new LinkedList<>();

        char[] patt = pattern.toCharArray();
        for (FeedItem item : postsById) {
//            char[] text = item.getContent().toCharArray();
            char[] text = "GCTTCTGCTACCTTTTGCGCGCGCGCGGAA".toCharArray();
            int position = 0;
            boolean mismatch;
            while (position + patt.length <= text.length) {
                System.out.println("testing position: " + position);
                mismatch = false;
                for (int i = patt.length-1; i >= 0; i--) {
                    if (patt[i] != text[position+i]) {
                        int[] shifts = badCharMap.get(text[position+i]);
                        if (shifts == null) {
                            // char in text does not appear in pattern.
                            // shift until end of pattern is past this point.
                            position += i + 1;
                        } else {
                            position += i - shifts[i];
                        }
                        mismatch = true;
                        break;
                    }
                }
                if (!mismatch) {
                    System.out.println("matched!");
                    break;
                }
            }
            break; // testing
        }
        return null;
    }
}
