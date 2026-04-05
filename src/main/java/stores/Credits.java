package stores;

import structures.*;
import main.java.structures.DynamicArray;

import interfaces.ICredits;

public class Credits implements ICredits{
    Stores stores;

    HashMap<Integer, CreditsEntry> credits;
    HashMap<Integer, DynamicArray<Integer>> eachCastsFilms;
    HashMap<Integer, DynamicArray<Integer>> eachCrewsFilms;
    int movieCount = 0;

    private class CreditsEntry {
        int id;
        CastCredit[] cast;
        CrewCredit[] crew;
    }

    /**
     * The constructor for the Credits data store. This is where you should
     * initialise your data structures.
     * 
     * @param stores An object storing all the different key stores, 
     *               including itself
     */
    public Credits (Stores stores) {
        this.stores = stores;
        this.credits = new HashMap<>();
        this.eachCastsFilms = new HashMap<>();
        this.eachCrewsFilms = new HashMap<>();
    }

    //A method to get an array of all the movie IDs stored in our credits
    public int[] getMovieIDs() {
        int[] allIDs = credits.keysAsints();
        return allIDs;
    }

    /**
     * Adds data about the people who worked on a given film. The movie ID should be
     * unique
     * 
     * @param cast An array of all cast members that starred in the given film
     * @param crew An array of all crew members that worked on a given film
     * @param id   The (unique) movie ID
     * @return TRUE if the data able to be added, FALSE otherwise
     */
    @Override
    public boolean add(CastCredit[] cast, CrewCredit[] crew, int id) {
        //If the credit entry is already stored, do not readd it
        if (credits.get(id) != null) {
            return false;
        }

        //Create a CreditsEntry object to be added
        CreditsEntry creditInfo = new CreditsEntry();
        creditInfo.id = id;
        creditInfo.cast = cast;
        creditInfo.crew = crew;

        credits.add(id, creditInfo); //Add the credit entry to our credits HashMap

        //Update the secondary index for cast members
        if (cast != null) {
            //Iterate through each member of the cast
            for (CastCredit castMember : cast) {
                DynamicArray<Integer> filmList = eachCastsFilms.get(castMember.getID()); //Retriece the film list for the cast member

                //If no films have been added yet, create new film list and add it to the hashmap
                if (filmList == null) {
                    filmList = new DynamicArray<>();
                    eachCastsFilms.add(castMember.getID(), filmList);
                }
                
                //Add the film id to the cast member's film list
                filmList.add(id);
            }
        }

        //Update the secondary index for crew members
        if (crew != null) {
            //Iterate through each member of the crew
            for (CrewCredit crewMember : crew) {
                DynamicArray<Integer> filmList = eachCrewsFilms.get(crewMember.getID()); //Retriece the film list for the crew member

                //If no films have been added yet, create new film list and add it to the hashmap
                if (filmList == null) {
                    filmList = new DynamicArray<>();
                    eachCrewsFilms.add(crewMember.getID(), filmList);
                }
                
                //Add the film id to the cast member's film list
                filmList.add(id);
            }
        }

        movieCount++; //Increase our movie counter
        return true;
    }

    /**
     * Remove a given films data from the data structure
     * 
     * @param id The movie ID
     * @return TRUE if the data was removed, FALSE otherwise
     */
    @Override
    public boolean remove(int id) {
        CreditsEntry creditInfo = credits.get(id); //Get the credit info about the particular movie

        //If there is no credit info stored for this movie, there is nothing to remove
        if (creditInfo == null) {
            return false;
        }

        //Update the secondary index for crew members
        if (creditInfo.cast != null) {
            //Iterate through the cast members
            for (CastCredit castMember : creditInfo.cast) {
                DynamicArray<Integer> filmList = eachCastsFilms.get(castMember.getID());
                filmList.remove((Integer) id);
            }
        }

        //Update the secondary index for crew members
        if (creditInfo.crew != null) {
            //Iterate through the crew members
            for (CrewCredit crewMember : creditInfo.crew) {
                DynamicArray<Integer> filmList = eachCrewsFilms.get(crewMember.getID());
                filmList.remove((Integer) id);
            }
        }

        credits.remove(id); //Remove the credit entry from our main HashMap
        movieCount--; //Decrease the counter
        return true;
    }

    /**
     * Gets all the cast members for a given film
     * 
     * @param filmID The movie ID
     * @return An array of CastCredit objects, one for each member of cast that is 
     *         in the given film. The cast members should be in "order" order. If
     *         there is no cast members attached to a film, or the film cannot be 
     *         found in Credits, then return an empty array
     */
    @Override
    public CastCredit[] getFilmCast(int filmID) {
        CreditsEntry creditInfo = credits.get(filmID); //Get the CreditEntry object for the given film
        int castSize = sizeOfCast(filmID); //Get the number of cast members in the given film

        //If there are no credit entries for this film, return an empty CastCredit[]
        if (creditInfo == null || castSize == 0) {
            return new CastCredit[0];
        }

        CastCredit[] orderedCast = new CastCredit[castSize]; //Create a new array with as many spaces as there are cast members in the given film

        //Add each cast member to our orderedCast array
        for (int i = 0; i < castSize; i++) {
            orderedCast[i] = creditInfo.cast[i];
        }

        //Place our cast members in "order" order using insertion sort
        for (int i = 1; i < castSize; i++) {
            CastCredit current = orderedCast[i];
            int j = i - 1;

            //Shift higher-order elements to the right to make room for current
            while (j >= 0 && orderedCast[j].getOrder() > current.getOrder()) {
                orderedCast[j + 1] = orderedCast[j];
                j--;
            }
            //Insert current into its sorted position
            orderedCast[j + 1] = current;
        }

        return orderedCast;
    }

    /**
     * Gets all the crew members for a given film
     * 
     * @param filmID The movie ID
     * @return An array of CrewCredit objects, one for each member of crew that is
     *         in the given film. The crew members should be in "id" order (not "elementID"). If there 
     *         is no crew members attached to a film, or the film cannot be found in Credits, 
     *         then return an empty array
     */
    @Override
    public CrewCredit[] getFilmCrew(int filmID) {
        CreditsEntry creditInfo = credits.get(filmID); //Get the CreditEntry object for the given film
        int crewSize = sizeOfCrew(filmID); //Get the number of crew members in the given film

        //If there are no credit entries for this film, return an empty CastCredit[]
        if (creditInfo == null || crewSize == 0) {
            return new CrewCredit[0];
        }

        CrewCredit[] orderedCrew = new CrewCredit[crewSize]; //Create a new array with as many spaces as there are crew members in the given film

        //Add each crew member to our orderedCast array
        for (int i = 0; i < crewSize; i++) {
            orderedCrew[i] = creditInfo.crew[i];
        }

        //Place our crew members in id order using insertion sort
        for (int i = 1; i < crewSize; i++) {
            CrewCredit current = orderedCrew[i];
            int j = i - 1;

            //Shift higher-order elements to the right to make room for current
            while (j >= 0 && orderedCrew[j].getID() > current.getID()) {
                orderedCrew[j + 1] = orderedCrew[j];
                j--;
            }
            //Insert current into its sorted position
            orderedCrew[j + 1] = current;
        }

        return orderedCrew;
    }

    /**
     * Gets the number of cast that worked on a given film
     * 
     * @param filmID The movie ID
     * @return The number of cast member that worked on a given film. If the film
     *         cannot be found in Credits, then return -1
     */
    @Override
    public int sizeOfCast(int filmID) {
        CreditsEntry creditInfo = credits.get(filmID); //Get CreditsEntry object for the given film

        //If there are no credit entries for the given film, return -1
        if (creditInfo == null) {
            return -1;
        }

        //If there are no cast for the given film, return 0
        if (creditInfo.cast == null) {
            return 0;
        }

        return creditInfo.cast.length; //Otherwise, return the number of cast in the film
    }

    /**
     * Gets the number of crew that worked on a given film
     * 
     * @param filmID The movie ID
     * @return The number of crew member that worked on a given film. If the film
     *         cannot be found in Credits, then return -1
     */
    @Override
    public int sizeOfCrew(int filmID) {
        CreditsEntry creditInfo = credits.get(filmID); //Get the CreditEntry object for the given film

        //If there are no credit entries for the film, return -1
        if (creditInfo == null) {
            return -1;
        }

        //If there are no crew members for the film, return 0
        if (creditInfo.crew == null) {
            return 0;
        }

        return creditInfo.crew.length; //Otherwise, return the number of crew members in the film
    }

    /**
     * Gets a list of all unique cast members present in the data structure
     * 
     * @return An array of all unique cast members as Person objects. If there are 
     *         no cast members, then return an empty array
     */
    @Override
    public Person[] getUniqueCast() {
        DynamicArray<Person> uniqueCast = new DynamicArray<>();
        
        Integer[] filmIDs = credits.keysAsIntegers();

        //Iterate over each film's credits entry
        for (int filmID : filmIDs) {
            CreditsEntry creditInfo = credits.get(filmID);

            if (creditInfo != null && creditInfo.cast != null) {
                //Iterate over each cast credit
                for (int i = 0; i < creditInfo.cast.length; i++) {
                    CastCredit castMember = creditInfo.cast[i];

                    //Skip any null slots
                    if (castMember == null) {
                        continue;
                    }

                    Person person = new Person(castMember.getID(), castMember.getName(), castMember.getProfilePath());
                
                    //Check if the person is already in our unique list
                    boolean alreadyAdded = false;

                    for (int j = 0; j < uniqueCast.size(); j++) {
                        Person person2 = uniqueCast.get(j);

                        if (person2.getID() == person.getID()) {
                            alreadyAdded = true;
                            break;
                        }
                    }
                    //If the person has not already been added, add them
                    if (!alreadyAdded) {
                        uniqueCast.add(person);
                    }
                
                }
            }
        }

        return uniqueCast.toArray(Person.class); //Convert the DynamicArray to a Person[] and return 
    }

    /**
     * Gets a list of all unique crew members present in the data structure
     * 
     * @return An array of all unique crew members as Person objects. If there are
     *         no crew members, then return an empty array
     */
    @Override
    public Person[] getUniqueCrew() {
        DynamicArray<Person> uniqueCrew = new DynamicArray<>();
        
        Integer[] filmIDs = credits.keysAsIntegers();

        //Iterate over each film's credits entry
        for (int filmID : filmIDs) {
            CreditsEntry creditInfo = credits.get(filmID);

            if (creditInfo != null && creditInfo.crew != null) {
                //Iterate over each crew credit
                for (int i = 0; i < creditInfo.crew.length; i++) {
                    CrewCredit crewMember = creditInfo.crew[i];

                    //Skip any null slots
                    if (crewMember == null) {
                        continue;
                    }

                    Person person = new Person(crewMember.getID(), crewMember.getName(), crewMember.getProfilePath());
                
                    //Check if the person is already in our unique list
                    boolean alreadyAdded = false;

                    for (int j = 0; j < uniqueCrew.size(); j++) {
                        Person person2 = uniqueCrew.get(j);

                        if (person2.getID() == person.getID()) {
                            alreadyAdded = true;
                            break;
                        }
                    }
                    //If they have not already been added, add them
                    if (!alreadyAdded) {
                        uniqueCrew.add(person);
                    }
                }
            }
        }

        return uniqueCrew.toArray(Person.class); //Convert the DynamicArray to a Person[] and return it
    }

    /**
     * Get all the cast members that have the given string within their name
     * 
     * @param cast The string that needs to be found
     * @return An array of unique Person objects of all cast members that have the 
     *         requested string in their name. If there are no matches, return an 
     *         empty array
     */
    @Override
    public Person[] findCast(String cast) {
        //If the given String is empty, return an empty Person[]
        if (cast == null) {
            return new Person[0];
        }

        //Create a DynamicArray of all unique cast
        String lowerCast = cast.toLowerCase(); //Convert the String to lower case for case-insensitive searching
        DynamicArray<Person> uniqueCast = new DynamicArray<>();

        for (Person person : getUniqueCast()) {
            uniqueCast.add(person);
        }

        //Use our DynamicArray's filter() method to filter out the matches using an in-line interface
        DynamicArray<Person> matches = uniqueCast.filter(new DynamicArray.Filter<Person>() {
            public boolean accept(Person p) {
                return p.getName().toLowerCase().contains(lowerCast);
            }
        });

        return matches.toArray(Person.class); //Convert the DynamicArray to a Person[]

    }

    /**
     * Get all the crew members that have the given string within their name
     * 
     * @param crew The string that needs to be found
     * @return An array of unique Person objects of all crew members that have the 
     *         requested string in their name. If there are no matches, return an 
     *         empty array
     */
    @Override
    public Person[] findCrew(String crew) {
        //If the given String is empty, return an empty Person[]
        if (crew == null) {
            return new Person[0];
        }

        //Create a DynamicArray of all unique crew
        String lowerCrew = crew.toLowerCase(); //Convert the String to lower case for case-insensitive searching
        DynamicArray<Person> uniqueCrew = new DynamicArray<>();

        for (Person person : getUniqueCrew()) {
            uniqueCrew.add(person);
        }

        //Use our DynamicArray's filter() method to filter out the matches using an in-line interface
        DynamicArray<Person> matches = uniqueCrew.filter(new DynamicArray.Filter<Person>() {
            public boolean accept(Person p) {
                return p.getName().toLowerCase().contains(lowerCrew);
            }
        });

        return matches.toArray(Person.class); //Convert the DynamicArray to a Person[]
    }

    /**
     * Gets the Person object corresponding to the cast ID
     * 
     * @param castID The cast ID of the person to be found
     * @return The Person object corresponding to the cast ID provided. 
     *         If a person cannot be found, then return null
     */
    @Override
    public Person getCast(int castID) {
        Person[] uniqueCast = getUniqueCast();

        //Search through the list of unique cast members to find the cast member with the matching ID
        for (int i = 0; i < uniqueCast.length; i++) {
            if (uniqueCast[i].getID() == castID) {
                return uniqueCast[i];
            }
        }

        return null; //If the cast member cannot be found, return null
    }

    /**
     * Gets the Person object corresponding to the crew ID
     * 
     * @param crewID The crew ID of the person to be found
     * @return The Person object corresponding to the crew ID provided. 
     *         If a person cannot be found, then return null
     */
    @Override
    public Person getCrew(int crewID){
        Person[] uniqueCrew = getUniqueCrew();

        //Search through the list of unique crew members to find the crew member with the matching ID
        for (int i = 0; i < uniqueCrew.length; i++) {
            if (uniqueCrew[i].getID() == crewID) {
                return uniqueCrew[i];
            }
        }

        return null; //If the crew member cannot be found, return null
    }

    
    /**
     * Get an array of film IDs where the cast member has starred in
     * 
     * @param castID The cast ID of the person
     * @return An array of all the films the member of cast has starred
     *         in. If there are no films attached to the cast member, 
     *         then return an empty array
     */
    @Override
    public int[] getCastFilms(int castID){
        DynamicArray<Integer> filmList = eachCastsFilms.get(castID); //Get the list of films the given cast member is in

        //If the cast member has not been in any films, return an empty int[]
        if (filmList == null) {
            return new int[0];
        }

        //Convert DynamicArray to int[] array
        int[] returnArray = new int[filmList.size()];
        for (int i = 0; i < filmList.size(); i++) {
            returnArray[i] = filmList.get(i);
        }

        return returnArray;
    }

    /**
     * Get an array of film IDs where the crew member has starred in
     * 
     * @param crewID The crew ID of the person
     * @return An array of all the films the member of crew has starred
     *         in. If there are no films attached to the crew member, 
     *         then return an empty array
     */
    @Override
    public int[] getCrewFilms(int crewID) {
        DynamicArray<Integer> filmList = eachCrewsFilms.get(crewID); //Get the list of films the given crew member is in

        //If the crew member has not been in any films, return an empty int[]
        if (filmList == null) {
            return new int[0];
        }

        //Convert dynamic array to int[] array
        int[] returnArray = new int[filmList.size()];
        for (int i = 0; i < filmList.size(); i++) {
            returnArray[i] = filmList.get(i);
        }

        return returnArray;
    }

    /**
     * Get the films that this cast member stars in (in the top 3 cast
     * members/top 3 billing). This is determined by the order field in
     * the CastCredit class
     * 
     * @param castID The cast ID of the cast member to be searched for
     * @return An array of film IDs where the the cast member stars in.
     *         If there are no films where the cast member has starred in,
     *         or the cast member does not exist, return an empty array
     */
    @Override
    public int[] getCastStarsInFilms(int castID){
        int[] castsFilms = getCastFilms(castID); //Get all the films the cast member appears in

        //If the cast member is not in any films, return an empty int[]
        if (castsFilms == null || castsFilms.length == 0) {
            return new int[0];
        }

        DynamicArray<Integer> returnArray = new DynamicArray<>();

        //Examine each film the cast member appears in
        for (int filmID : castsFilms) {
            CreditsEntry creditInfo = credits.get(filmID); //Fetch the full credit entry for this film

            //Iterate through the cast array for the film 
            for (int i = 0; i < creditInfo.cast.length; i++) {
                CastCredit castMember = creditInfo.cast[i];
                //When we locate the matching cast member...
                if (castMember.getID() == castID) {
                    //...check if their bulling ordder is 1, 2, or 3
                    if (castMember.getOrder() <= 3) {
                        //If so, add this film to the return array
                        returnArray.add(filmID);
                    }
                    //Stop scanning further - each cast member appears only once per film
                    break;
                }
            }
        }

        //Concert the DynamicArray to int[]
        int[] newReturnArray = new int[returnArray.size()];  
        for (int i = 0; i < returnArray.size(); i++) {
            newReturnArray[i] = returnArray.get(i);
        }

        return newReturnArray;
    }
    
    /**
     * Get Person objects for cast members who have appeared in the most
     * films. If the cast member has multiple roles within the film, then
     * they would get a credit per role played. For example, if a cast
     * member performed as 2 roles in the same film, then this would count
     * as 2 credits. The list should be ordered by the highest to lowest number of credits.
     * 
     * @param numResults The maximum number of elements that should be returned
     * @return An array of Person objects corresponding to the cast members
     *         with the most credits, ordered by the highest number of credits.
     *         If there are less cast members that the number required, then the
     *         list should be the same number of cast members found.
     */
    @Override
    public Person[] getMostCastCredits(int numResults) {
        Person[] allCast = getUniqueCast(); //Get a list of all the cast members 
        int totalCast = allCast.length; //Get the number of cast memembers in our stores

        //If there are no cast members, return an empty Person[]
        if (totalCast == 0) {
            return new Person[0];
        }

        //Sort cast in decreasing order of number of cast credits
        quickSortPersons(allCast, 0, totalCast - 1);

        int arraySize = Math.min(totalCast, numResults);
        Person[] returnArray = new Person[arraySize];

        //The first "numResults" Persons in the sorted cast list have the most credits
        for (int i = 0; i < arraySize; i++) {
            returnArray[i] = allCast[i];
        }

        return returnArray;
    }

    /**
     * Get the number of credits for a given cast member. If the cast member has
     * multiple roles within the film, then they would get a credit per role
     * played. For example, if a cast member performed as 2 roles in the same film,
     * then this would count as 2 credits.
     * 
     * @param castID A cast ID representing the cast member to be found
     * @return The number of credits the given cast member has. If the cast member
     *         cannot be found, return -1
     */
    @Override
    public int getNumCastCredits(int castID) {
        DynamicArray<Integer> filmList = eachCastsFilms.get(castID); //Get the list of films the given cast member appears in

        //If the cast member cannot be found, return -1
        if (filmList == null) {
            return -1;
        }

        return filmList.size(); //Otherwise return the number of credits for the given cast member
    }

    /**
     * Gets the number of films stored in this data structure
     * 
     * @return The number of films in the data structure
     */
    @Override
    public int size() {
        return movieCount; //Our counter keeps track of the number of films in our store
    }

    /**
     * Sorts a subarray of Integers in place, in descending order of the value returned by the provided ValueGetter
     * 
     * @param persons the array of People objects to be sorted
     * @param low   the starting index of the subarray to sort
     * @param high  the ending index of the subarray to sort
     */
    private void quickSortPersons(Person[] persons, int low, int high) {
        //Conquer - recursively apply the same process to the left and right subarrays
        
        //Only proceed if there is more than one element to sort
        if (low < high) {
            //Partition the array and get the pivot position
            int pivotIndex = divide(persons, low, high);
            //Recursively sort elements before the pivot
            quickSortPersons(persons, low, pivotIndex - 1);
            //Recursively sort elements after the pivot
            quickSortPersons(persons, pivotIndex + 1, high);
        }
    }

    /**
     * Partitions a subarray around a pivot element chosen as array[high]
     * Elements with value >= pivotValue are moved to the left, others to the right
     * 
     * @param array  the array being sorted
     * @param low    the starting index of the subarray
     * @param high   the ending index, pivot is taken from here
     * @param getter a callback that returns the float value to compare for each key
     * @return the final index of the pivot element after partition
     */
    private int divide(Person[] persons, int low, int high) {
        Person pivot = persons[high];
        int pivotCredits = getNumCastCredits(pivot.getID());
        int i = low - 1;

        //Iterate through the subarray, swapping elements greater than or equal
        for (int j = low;j < high; j++) {
            int currentCredits = getNumCastCredits(persons[j].getID());
            //If current has more (or equal) credits, swap current and pivot (to put in decreasing order)
            if (currentCredits >= pivotCredits) {
                i++;
                swap(persons, i, j);
            }
        }

        //Place the pivot in its correct sorted position
        swap(persons, i + 1, high);
        return (i + 1);
    }

   /**
     * Swaps two elements in the array
     *
     * @param array the array containing the elements
     * @param i     the index of the first element to swap
     * @param j     the index of the second element to swap
     */
    private void swap(Person[] persons, int i, int j) {
        Person temp = persons[i];
        persons[i] = persons [j];
        persons[j] = temp;
    }
}
