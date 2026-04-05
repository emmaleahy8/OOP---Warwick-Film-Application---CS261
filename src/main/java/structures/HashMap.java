package stores;

import interfaces.IMap;

// This line allows us to cast our object to type (E) without any warnings.
// For further detais, please see: http://docs.oracle.com/javase/1.5.0/docs/api/java/lang/SuppressWarnings.html
@SuppressWarnings("unchecked") 
public class HashMap<K extends Comparable<K>,V> implements IMap<K,V> {

    protected KeyValuePairLinkedList[] table; //Array of buckets, each bucket is a linked list of (key, value) pairs
    private static final float LOAD_FACTOR = 0.75f; //Maximum load factor before resizing
    private int size = 0; //Number of (key, value) pairs currently stored
    
    /**
     * Default constructor
     */
    public HashMap() {
        // For very simple hashing, primes reduce collisions 
        this(1307);
    }
    
    /**
     * Constructs a HashMap with the given initial bucket count
     * @param size initial number of buckets
     */
    public HashMap(int size) {
        table = new KeyValuePairLinkedList[size];
        initTable();
    }

    /**
     * Returns the number of comparisons required to find an element using Linear Search
     * @param key the key of the value to be found
     * @return  the number of comparisons needed to find the element
     */
    public int find(K key) {
        int index = Math.abs(hash(key)) % table.length; //Get the index of the value to be found by hashing the key
        int count = 0; //A counter to count how many comparisons are needed

        KeyValuePairLinkedList<K,V> linkedlist = table[index]; //Get the linked list for the bucket in which the value is stored
        ListElement<KeyValuePair<K,V>> element = linkedlist.getHead(); //The first element to look at

        //Look through all the elements in the linked list, checking if they have the same key as the given key
        for (int i = 0; i < linkedlist.size(); i++) {
            count++;
            if (element.getValue().getKey().equals(key)) {
                return count;
            }

            element = element.getNext();
        }
        
        return count;
    }
    
    /**
     * Initialises each bucket to an empty linked list
     */
    protected void initTable() {
        for(int i = 0; i < table.length; i++) {
            table[i] = new KeyValuePairLinkedList<>();
        }
    }
    
    /**
     * Gets the hash code for a given key
     * @param key the key to be hashed
     * @return the hashed key
     */
    protected int hash(K key) {
        int code = key.hashCode();
        return code;    
    }
    

    /**
     * Adds or updates the value for a given key
     * Resizes the table if load factor threshold is reached
     */
    public void add(K key, V value) {
        //Resize if we are exceeding the load factor
        if ((size + 1) > table.length * LOAD_FACTOR) {
            resize();
        }

        //Find the appropriate bucket
        int hash_code = hash(key);
        int location = Math.abs(hash_code) % table.length;
                
        table[location].add(key,value); //Add the (key, value) pair to the bucket's linked list
        size++;
    }

    /**
     * Increases the bucket count to double plus one and rehashes all existing entries
     */
    public void resize() {
        int newBucketCount = table.length * 2 + 1; //+1 to stay away from even numbers for less collisions
        KeyValuePairLinkedList<K,V>[] oldTable = table;

        //Build new table
        table = new KeyValuePairLinkedList[newBucketCount];
        initTable();

        //Rehash every existing entry into the new table
        for (KeyValuePairLinkedList<K,V> bucket : oldTable) {
            for (ListElement<KeyValuePair<K,V>> e = bucket.getHead(); e != null; e = e.getNext()) {
                KeyValuePair<K,V> entry = e.getValue();
                add(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Retrieves the value associated with a given key
     * 
     * @param key the key to look up
     * @return the associated value (or null if not found)
     */
    public V get(K key) {
        int hash_code = hash(key);
        int location = Math.abs(hash_code) % table.length;
        
        KeyValuePair<K,V> ptr = table[location].get(key);
        
        if (ptr == null) {
            return null;
        }

        return (V) ptr.getValue();
    }

    /**
     * Removes the mapping for a given key (if present)
     * @param key the key to remove
     */
    public void remove(K key) {
        int index = Math.abs(hash(key)) % table.length;
        KeyValuePairLinkedList<K, V> bucket = table[index];
        
        // Retrieve the key-value pair for the key
        KeyValuePair<K, V> kvp = bucket.get(key);
        
        // If found, remove it from the linked list
        if (kvp != null) {
            bucket.remove(kvp);
            size--;
        }
    }

    /**
     * Counts the total number of (key, value) pairs across all buckets
     * @return
     */
    public int totalKeys() {
        int total = 0;

        for (int i = 0; i < table.length; i++) {
            total += table[i].size();
        }

        return total;
    }

    /**
     * Returns all keys as an Integer[] array
     * Only supported when K is Integer 
     * 
     * @return an Integer[] array of all keys
     */
    public Integer[] keysAsIntegers() {
        Integer[] arrayOfKeys = new Integer[totalKeys()]; //Create Integer[] of the same size as there are keys in the HashMap
        int position = 0;

        //For each element in the HashMap, copy it to the Integer[] array
        for (int i = 0; i < table.length; i++) {
            ListElement<KeyValuePair<K, V>> current = table[i].getHead();

            while (current != null) {
                K key = current.getValue().getKey();

                //If the key is not an Integer (or instance of it), throw exception
                if (!(key instanceof Integer)) {
                    throw new UnsupportedOperationException("keysAsIntegers() only supported when K is Integer; found " + key.getClass().getSimpleName());
                }
                arrayOfKeys[position++] = (Integer) key;
                current = current.getNext();
            }
        }

        return arrayOfKeys;
    }

    /**
     * Returns all keys as an int[] array
     * Only supported when K is Integer 
     * 
     * @return an int[] array of all keysn
     */
    public int[] keysAsints() {
        int[] arrayOfKeys = new int[totalKeys()]; //Create int[] of the same size as there are keys in the HashMap
        int position = 0;

        //For each element in the HashMap, copy it to the int[] array
        for (int i = 0; i < table.length; i++) {
            ListElement<KeyValuePair<K, V>> current = table[i].getHead();

            while (current != null) {
                K key = current.getValue().getKey();

                //If the key is not an Integer (or instance of it), throw exception
                if (!(key instanceof Integer)) {
                    throw new UnsupportedOperationException("keysAsints() only supported when K is Integer; found " + key.getClass().getSimpleName());
                }
                arrayOfKeys[position++] = (Integer) key;
                current = current.getNext();
            }
        }
        
        return arrayOfKeys;

    }

}
