package main.java.structures;

import java.lang.reflect.Array;

/**
 * A generic, resizable array implementation providing amortized O(1) append and O(1) random access
 * 
 * @param <E> the type of elements stored in this array
 */
public class DynamicArray<E> {
    private E[] elements;
    private int count; //Number of elements currently stored
    private static final int initialCapacity = 10; //The initial capacity of the array

    /**
     * Constructs a new DynamicArray with the default initial capacity
     */
    public DynamicArray() {
        elements = (E[]) new Object[initialCapacity];
        count = 0;
    }

    /**
     * Appends the given element to the end of the array, resizing if needed
     * 
     * @param element element to be added
     */
    public void add(E element) {
        //If we have run out of space, double the array size
        if (count >= elements.length) {
            E[] newElements = (E[]) new Object[elements.length * 2];

            //Copy existing elements into the larger array
            for (int i = 0; i < elements.length; i++) {
                newElements[i] = elements[i];
            }

            elements = newElements;
        }

        elements[count++] = element;
    }

    /**
     * Retrieves element at the specified index
     * 
     * @param index the index position of the element to return
     * @return  the element at the given index
     */
    public E get(int index) {
        return elements[index];
    }

    /**
     * Removes and returns the element at the specified index, shifting subsequent elements to the lef to fill the gap
     * 
     * @param index the position of the element to remove
     * @return  the removed element
     */
    public E remove(int index) {

        E removedElement = elements[index];

        for (int i = index; i < count - 1; i++) {
            elements[i] = elements[i +1];
        }

        elements[count - 1] = null;

        count--;
        return removedElement;
    }

    /**
     * Returns the current number of elements stored
     * 
     * @return  the size of the DynamicArray
     */
    public int size() {
        return count;
    }

    /**
     * Converts the contents of the DynamicArray into a plain array of specified type
     * 
     * @param class1 the class object of the array component type
     * @return  a new array containing all elements
     */
    public E[] toArray(Class<E> class1) {

        //Create generic array
        E[] returnArray= (E[]) Array.newInstance(class1, count);
        
        //Copy each element into the return array
        for (int i = 0; i < count; i++) {
            returnArray[i] = elements[i];
        }
    
        return returnArray;
    }

    /**
     * An interface you can implement in-line to filter your dynamic array
     */
    public static interface Filter<E> {
        boolean accept(E e);
    }

    /**
     * Produces a DynamicArray containing only those elements for which the given filter accepts
     * 
     * @param filter    a filter specifying which elements to keep
     * @return  a new DynamicArray of filtered elements
     */
    public DynamicArray<E> filter(Filter<? super E> filter) {
        DynamicArray<E> returnArray = new DynamicArray<>();
        
        //Apply the filter to each element
        for (int i = 0; i < count; i++) {
            E e = elements[i];
            if (filter.accept(e)) {
                returnArray.add(e);
            }
        }

        return returnArray;
    }
}

