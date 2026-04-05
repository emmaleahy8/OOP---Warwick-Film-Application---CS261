package stores;

import java.time.LocalDateTime;

import interfaces.IMovies;
import interfaces.IRatings;
import main.java.structures.DynamicArray;
import structures.*;

public class Ratings implements IRatings {
    Stores stores;

    private HashMap<Integer, DynamicArray<RatingEntry>> ratingsByMovie;
    private HashMap<Integer, DynamicArray<RatingEntry>> ratingsByUser;
    private IMovies moviesStore;
    private int totalRatings = 0;

    private class RatingEntry {
        int userid;
        int movieid;
        float rating;
        LocalDateTime timestamp;

        public RatingEntry(int userid, int movieid, float rating, LocalDateTime timestamp) {
            this.userid = userid;
            this.movieid = movieid;
            this.rating = rating;
            this.timestamp = timestamp;
        }
    }

    /**
     * The constructor for the Ratings data store. This is where you should
     * initialise your data structures.
     * @param stores An object storing all the different key stores,
     *               including itself
     */
    public Ratings(Stores stores) {
        this.stores = stores;
        // TODO Add initialisation of data structure here
        this.moviesStore = stores.getMovies();
        this.ratingsByMovie = new HashMap<>();
        this.ratingsByUser = new HashMap<>();
    }

    /**
     * Adds a rating to the data structure. The rating is made unique by its user ID
     * and its movie ID
     * 
     * @param userID    The user ID
     * @param movieID   The movie ID
     * @param rating    The rating gave to the film by this user (between 0 and 5
     *                  inclusive)
     * @param timestamp The time at which the rating was made
     * @return TRUE if the data able to be added, FALSE otherwise
     */
    @Override
    public boolean add(int userid, int movieid, float rating, LocalDateTime timestamp) {
        DynamicArray<RatingEntry> movieRatings = ratingsByMovie.get(movieid);

        //Check if this rating already exists
        if (movieRatings != null) {
            for (int i = 0; i < movieRatings.size(); i++) {
                if (movieRatings.get(i).userid == userid) {
                    return false; //Dont re-add the rating
                }
            }
        }

        //Create a new rating entry
        RatingEntry entry = new RatingEntry(userid, movieid, rating, timestamp);

        //Add entry to ratingsByMovie
        if (movieRatings == null) {
            movieRatings = new DynamicArray<>();
            ratingsByMovie.add(movieid, movieRatings);
        }
        movieRatings.add(entry);

        //Add entry to ratingsByUser
        DynamicArray<RatingEntry> userRatings = ratingsByUser.get(userid);
        if (userRatings == null) {
            userRatings = new DynamicArray<>();
            ratingsByUser.add(userid, userRatings);
        }
        userRatings.add(entry);

        totalRatings++;
        return true;
    }

    /**
     * Removes a given rating, using the user ID and the movie ID as the unique
     * identifier
     * 
     * @param userID  The user ID
     * @param movieID The movie ID
     * @return TRUE if the data was removed successfully, FALSE otherwise
     */
    @Override
    public boolean remove(int userid, int movieid) {
        DynamicArray<RatingEntry> movieRatings = ratingsByMovie.get(movieid);

        //If there are no rating entries for this movie, return false
        if (movieRatings == null) {
            return false;
        }

        boolean removed = false; //Update when entry removed from ratingsByMovie

        //Remove entry from ratingsByMovie
        for (int i = 0; i < movieRatings.size(); i++) {
            if (movieRatings.get(i).userid == userid) {
                movieRatings.remove(i);
                removed = true;
                break;
            }
        }
        
        if (!removed) return false; //If rating wasnt there to remove, return false

        //Remove entry from ratingsByUser
        DynamicArray<RatingEntry> userRatings = ratingsByUser.get(userid);

        for (int i = 0; i < userRatings.size(); i++) {
            if (userRatings.get(i).movieid == movieid) {
                userRatings.remove(i);
                break; 
            }
        }

        totalRatings--;
        return true;
    }

    /**
     * Sets a rating for a given user ID and movie ID. Therefore, should the given
     * user have already rated the given movie, the new data should overwrite the
     * existing rating. However, if the given user has not already rated the given
     * movie, then this rating should be added to the data structure
     * 
     * @param userID    The user ID
     * @param movieID   The movie ID
     * @param rating    The new rating to be given to the film by this user (between
     *                  0 and 5 inclusive)
     * @param timestamp The time at which the new rating was made
     * @return TRUE if the data able to be added/updated, FALSE otherwise
     */
    @Override
    public boolean set(int userid, int movieid, float rating, LocalDateTime timestamp) {
        
        DynamicArray<RatingEntry> movieRatings = ratingsByMovie.get(movieid);
        
        //Check if there is already an entry and update it if so
        if (movieRatings != null) {
            for (int i = 0; i < movieRatings.size(); i++) {
                RatingEntry entry = movieRatings.get(i);

                if (entry.userid == userid) {
                    entry.rating = rating; //Update the rating
                    entry.timestamp = timestamp; //Update the timestamp
                    
                    return true; //Update complete
                } 
            }
        }

        //If no entry from that user for that film is found, add a new rating
        return add(userid, movieid, rating, timestamp);
    }

    /**
     * Get all the ratings for a given film
     * 
     * @param movieID The movie ID
     * @return An array of ratings. If there are no ratings or the film cannot be
     *         found in Ratings, then return an empty array
     */
    @Override
    public float[] getMovieRatings(int movieid) {
        DynamicArray<RatingEntry> movieRatings = ratingsByMovie.get(movieid);

        //If there is no entry or ratings for this movie, return an empty float[]
        if (movieRatings == null || movieRatings.size() == 0) {
            return new float[0];
        }

        float[] returnArray = new float[movieRatings.size()]; //Create a new array with enough spaces for the movie ratings
        
        //Insert each rating into the return array
        for (int i = 0; i < movieRatings.size(); i++) {
            returnArray[i] = movieRatings.get(i).rating;
        }

        return returnArray;
    }

    /**
     * Get all the ratings for a given user
     * 
     * @param userID The user ID
     * @return An array of ratings. If there are no ratings or the user cannot be
     *         found in Ratings, then return an empty array
     */
    @Override
    public float[] getUserRatings(int userid) {
        DynamicArray<RatingEntry> userRatings = ratingsByUser.get(userid);

        //If there is no entry or ratings for this user, return an empty float[]
        if (userRatings == null || userRatings.size() == 0) {
            return new float[0];
        }

        float[] returnArray = new float[userRatings.size()]; //Create a new array with enough spaces for the user ratings

        //Insert each rating into the return array
        for (int i = 0; i < userRatings.size(); i++) {
            returnArray[i] = userRatings.get(i).rating;
        }

        return returnArray;
    }

    /**
     * Get the average rating for a given film
     * 
     * @param movieID The movie ID
     * @return Produces the average rating for a given film. 
     *         If the film cannot be found in Ratings, but does exist in the Movies store, return 0.0f. 
     *         If the film cannot be found in Ratings or Movies stores, return -1.0f.
     */
    @Override
    public float getMovieAverageRating(int movieid) {
        DynamicArray<RatingEntry> movieRatings = ratingsByMovie.get(movieid); //Get all the movie ratings for the given movie
        IMovies moviesStore = stores.getMovies();
        float total = 0;

        //If the movie is not in our moviesStore and there are no ratings for the movie, return -1.0f
        if (moviesStore.getTitle(movieid) == null && movieRatings == null) {
            return -1.0f;
        }

        //If the movie is in the moviesStore, but does not have any ratings, return 0.0f
        if (moviesStore.getTitle(movieid) != null && movieRatings == null) {
            return 0.0f;
        }

        //Otherwise, add all of the ratings for the movie together
        for (int i = 0; i < movieRatings.size(); i++) {
            total += movieRatings.get(i).rating;
        }

        return (total / movieRatings.size()); //The return value is all of the ratings for the movie added together divided by the number of ratings 
    }

    /**
     * Get the average rating for a given user
     * 
     * @param userID The user ID
     * @return Produces the average rating for a given user. If the user cannot be
     *         found in Ratings, or there are no rating, return -1.0f
     */
    @Override
    public float getUserAverageRating(int userid) {
        DynamicArray<RatingEntry> userRatings = ratingsByUser.get(userid); //Get all of the ratings by a given user
        float total = 0;

        //If the user has no ratings, return -1.0f
        if (userRatings == null || userRatings.size() == 0) {
            return -1.0f;
        }

        //Otherwise, add all of the ratings for the given user together
        for (int i = 0; i < userRatings.size(); i++) {
            total += userRatings.get(i).rating;
        }

        return (total / userRatings.size()); //The return value is all of the ratings for the user added together divided by the number of ratings 
    }

    /**
     * Gets the top N movies with the most ratings, in order from most to least
     * 
     * @param num The number of movies that should be returned
     * @return A sorted array of movie IDs with the most ratings. The array should be
     *         no larger than num. If there are less than num movies in the store,
     *         then the array should be the same length as the number of movies in Ratings
     */
    @Override
    public int[] getMostRatedMovies(int num) {
        Integer[] movieIDs = ratingsByMovie.keysAsIntegers(); //Get a list of all of the movie IDs
        int totalMovies = movieIDs.length; //Get the total number of movies

        //If there are no movies in our store, return an empty int[]
        if (totalMovies == 0) {
            return new int[0];
        }

        //Use our quicksort algorithm to sort the movies in order (most to least ratings)
        quickSortIntegers(movieIDs, 0, totalMovies - 1, new ValueGetter() {
            public float getValue(int movieid) {
                return getMovieRatings(movieid).length;
            }
        });

        int returnArraySize = Math.min(totalMovies, num); //The number of movies to return is equal to totalMovies if totalMovies < num
        int[] returnArray = new int[returnArraySize]; //Create an array of size the same as the number of movies to be returned

        //Add the first n movies in our sorted movieIDs array to the return array
        for (int i = 0; i < returnArraySize; i++) {
            returnArray[i] = movieIDs[i];
        }

        return returnArray; 

    }

    /**
     * Gets the top N users with the most ratings, in order from most to least
     * 
     * @param num The number of users that should be returned
     * @return A sorted array of user IDs with the most ratings. The array should be
     *         no larger than num. If there are less than num users in the store,
     *         then the array should be the same length as the number of users in Ratings
     */
    @Override
    public int[] getMostRatedUsers(int num) {
        // TODO Implement this function

        Integer[] userIDs = ratingsByUser.keysAsIntegers(); //Get a list of all of the user IDs
        int totalUsers = userIDs.length; //Get the total number of users

        //If there are no users in our store, return an empty int[]
        if (totalUsers == 0) {
            return new int[0];
        }

        //Use our quicksort algorithm to sort the users in order (most to least ratings)
        quickSortIntegers(userIDs, 0, totalUsers - 1, new ValueGetter() {
            public float getValue(int userid) {
                return getUserRatings(userid).length;
            }
        });

        int returnArraySize = Math.min(totalUsers, num); //The number of users to return is equal to totalUsers if totalUsers < num
        int[] returnArray = new int[returnArraySize]; //Create an array of size the same as the number of users to be returned

        //Add the first n users in our sorted userIDs array to the return array
        for (int i = 0; i < returnArraySize; i++) {
            returnArray[i] = userIDs[i];
        }

        return returnArray;
    }

    /**     
    * Get the number of ratings that a movie has
     * 
     * @param movieid The movie id to be found
     * @return The number of ratings the specified movie has. 
     *         If the movie exists in the Movies store, but there are no ratings for it, then return 0. 
     *         If the movie does not exist in the Ratings or Movies store, then return -1.
     */
    @Override
    public int getNumRatings(int movieid) {
        // TODO Implement this function

        DynamicArray<RatingEntry> movieRatings = ratingsByMovie.get(movieid); //Get all the ratings for the given movie
        IMovies moviesStore = stores.getMovies();

        //If the movie is not in our moviesStore and there are no ratings for the movie, return -1
        if (moviesStore.getTitle(movieid) == null && movieRatings == null) {
            return -1;
        }

        //If the movie is in our movies store but has no ratings, return 0
        if (moviesStore.getTitle(movieid) != null && (movieRatings == null || movieRatings.size() == 0)) {
            return 0;
        }

        return movieRatings.size(); //Otherwise, return the number of ratings in our store

    }

    /**
     * Get the highest average rated film IDs, in order of there average rating
     * (hightst first).
     * 
     * @param numResults The maximum number of results to be returned
     * @return An array of the film IDs with the highest average ratings, highest
     *         first. If there are less than num movies in the store,
     *         then the array should be the same length as the number of movies in Ratings
     */
    @Override
    public int[] getTopAverageRatedMovies(int numResults) {
        Integer[] movieIDs = ratingsByMovie.keysAsIntegers(); //Get all of the movie ids in our Ratings store
        int totalMovies = movieIDs.length; //Get the total number of movies

        //If there are no movies, return an empty int[]
        if (totalMovies == 0) {
            return new int[0];
        }

        //Use our quicksort algorithm to sort the movies in order (highest to lowest average rating)
        quickSortIntegers(movieIDs, 0, totalMovies - 1, new ValueGetter() {
            public float getValue(int movieid) {
                return getMovieAverageRating(movieid);
            }
        });

        int returnArraySize = Math.min(totalMovies, numResults); //The number of movies to return is equal to totalMovies if totalMovies < num
        int[] returnArray = new int[returnArraySize]; //Create an array of size the same as the number of movies to be returned

        //Add the first n movies in our sorted movieIDs array to the return array
        for (int i = 0; i < returnArraySize; i++) {
            returnArray[i] = movieIDs[i];
        }

        return returnArray;
    }

    /**
     * Gets the number of ratings in the data structure
     * 
     * @return The number of ratings in the data structure
     */
    @Override
    public int size() {
        return totalRatings; //Our counter keeps track of the total ratings as they are added and removed
    }

    /**
     * Sorts a subarray of integers in place, in decending order of the value returned by the provided ValueGetter
     * 
     * @param array the array of Integers to be sorted
     * @param low   the starting index of the subarray to sort
     * @param high  the ending index of the subarray to sort
     * @param getter a callback that returns the float value to compare for each key
     */
    private void quickSortIntegers(Integer[] array, int low, int high, ValueGetter getter) {
        //Conquer - recursively apply the same process to the left and right subarrays

        //Only proceed if there is more than one element to sort
        if (low < high) {
            //Partition the array and get the pivot position
            int pivotIndex = divideIntegers(array, low, high, getter);
            //Recursively sort elements before the pivot
            quickSortIntegers(array, low, pivotIndex - 1, getter);
            //Recursively sort elements after the pivot
            quickSortIntegers(array, pivotIndex + 1, high, getter);
        }
    }

    /**
     * Partitions a subarray around a pivot element chosen as array[high]
     * Elements with value >= pivotValue are moved to the left, others to the right
     * 
     * @param array the array being sorted
     * @param low   the starting index of the subarray
     * @param high  the ending index - pivot is taken from here
     * @param getter    a callback that returns the float vlaue to compare for each key
     * @return  the final index of the pivot element after partition
     */
    private int divideIntegers(Integer[] array, int low, int high, ValueGetter getter) {
        Integer pivot = array[high];
        float pivotValue = getter.getValue(pivot);
        int i = low - 1;

        for (int j = low;j < high; j++) {
            float currentValue = getter.getValue(array[j]);
            //If current is greater than or equal to pivot, swap current and pivot (to put in decreasing order)
            if (currentValue >= pivotValue) {
                i++;
                swap(array, i, j);
            }
        }

        swap(array, i + 1, high);
        return (i + 1);
    }
    
    /**
     * Swaps two elements in the array
     */
    private void swap(Integer[] array, int i, int j) {
        Integer temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    /**
     * Functional interface for retrieving a comparison vlaue given a key
     * Implementations should return the metric by which sorting should occur
     */
    private interface ValueGetter {
        float getValue(int key);
    }

}
