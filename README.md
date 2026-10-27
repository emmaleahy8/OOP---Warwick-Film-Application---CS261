# Warwick+

A Java application that stores and queries a large film dataset efficiently, built on data structures I implemented from scratch. This was my coursework for CS126 Design of Information Structures at the University of Warwick.

Warwick+ presents films, the cast and crew who worked on them, and user ratings, all loaded from a set of CSV files. The framework and UI were provided; my task was to design and implement the data structures behind three of the data stores — `Movies`, `Credits` and `Ratings` — and the query functions that run over them.

The main constraint was that I couldn't use any of Java's built-in data structures, searching algorithms or sorting algorithms (including anything in `java.util`). Everything used to store and retrieve the data is my own.

## What I implemented

I implemented data structures that are efficient for fast key-based lookup, dynamic resizing (to accommodate our growing dataset) and good memory usage. I decided the best data structures for this were hash maps for lookup and dynamic arrays for resizable lists, with quicksort for the queries that need sorting.

### HashMap

I implemented a generic `HashMap<K, V>`, which can be used over any key type K (bounded by `Comparable<K>`) and any value type V, so I could use it as my primary key-value store in all three classes.

Each pair is placed into a bucket by hashing the key and reducing it modulus the table length. I chose our table length to be 1307, a prime number, to spread out keys and reduce collision probability. When multiple keys hash to the same bucket, they are stored in that bucket's linked list of key-value pairs, and lookups and removals then scan the list — this strategy is known as separate chaining. I used separate chaining rather than open addressing as it is more memory efficient (we only store what we insert, plus the bucket heads, rather than reserving the whole array up front) and because deletions are much easier.

Adding, removing and retrieving an element are all O(1) on average, and O(n) in the worst case where every entry ends up in the same bucket. I selected a load factor of 0.75 — when the HashMap is 75% full, we resize to `2 × originalsize + 1`, which keeps the table length odd and the collision probability low. A single resize takes O(n), but as this only happens after Ω(m) more inserts, the amortised cost per insert stays O(1). My HashMap also has two methods, `keysAsIntegers()` and `keysAsInts()`, so I don't have to duplicate the key-extraction logic across stores.

### DynamicArray

I implemented my own `DynamicArray`, as it can be resized when needed, has amortised O(1) appends and O(1) random access so we can index straight into the list, and is type-safe so it integrates seamlessly with all of our stores. It resizes by doubling its length whenever it fills up, which keeps the amortised cost of each `add()` at O(1) while keeping the memory overhead relatively low. Removing an element shifts every later element one slot to the left, which is O(n) in the worst case, but this keeps the array contiguous, which is crucial for indexing by position and iteration.

I made my DynamicArray unique with two generic methods, `toArray(...)` and `filter(...)`. Across Movies, Credits and Ratings I often need to pull out all the stored items as a plain array, so `toArray(Class<E> class1)` returns the DynamicArray as an array of any given class. My `filter(Filter<? super E> filter)` method gives every store a way to build a DynamicArray of a subset of the original, for any element type, without rewriting the core loop every time — the filter specification is passed in-line.

### QuickSort

I used quicksort in both the Credits and Ratings stores. I decided to write a specialised quicksort for each class rather than one generic version, as they sort quite different things — in Credits we sort `Person` objects by how many cast credits they have, and in Ratings we sort ids by how many ratings they have. Quicksort gives us in-place, average-case O(n log n) performance with very little extra memory, so it scales smoothly as the lists grow, and by always partitioning entries to the front I can stop recursing once the first N elements are in place, which saves work when N < n.

## How it fits together in each store

**Movies.** The Movies store uses a `HashMap<Integer, MovieEntry>` as the primary index for all film data, keyed by each film's unique TMDB id. This gives O(1) average-time lookups for `getTitle(id)`, `getOverview(id)`, `getReleaseDate(id)` and so on, so every query except `findFilms()` happens in constant time. I also keep a secondary HashMap for film collections by id, as not every film is in a collection, so it's much quicker to access them directly than to traverse the whole map each time. DynamicArrays store the production companies/countries and the film ids inside each `FilmCollection`, and `findFilms(searchTerm)` uses `filter(...)` to find only the films whose title, original title or overview contains the term.

**Credits.** I used three HashMaps in the Credits store — one storing each film's credits by film id, and two secondary indexes, one for cast members and one for crew members, giving O(1) lookup of each person's films. I did this rather than keeping a single `HashMap<Integer, CreditsEntry>`, as cast and crew members can appear in multiple films, so otherwise getting the films a person has been in would mean traversing the whole map and each entry's cast/crew list. The cost is a little extra work per insert, but every later query drops from scanning the whole dataset to a single lookup plus an O(k) copy. `findCast(...)` and `findCrew(...)` reuse the generic `filter(...)` method.

**Ratings.** For the Ratings store I again used a main and a secondary HashMap — one for ratings by movie id and one for ratings by user id — so `getMovieRatings` and `getUserRatings` are an O(1) lookup plus an O(k) copy instead of scanning every rating. `getMostRatedMovies(num)` and `getMostRatedUsers(num)` quicksort the relevant list of ids by rating count.

The trade-off throughout is the same: by paying a little extra work and space at insert time, every later query drops from scanning the entire dataset to a single hash lookup plus a small copy — which is exactly what we want as the store grows into thousands of films and tens of thousands of credits.

## Building and running

Requires **Java 21**.

| | Build | Run | Test |
|---|---|---|---|
| Linux / macOS | `./gradlew build` | `./gradlew run` | `./gradlew test` |
| Windows | `./gradlew.bat build` | `./gradlew.bat run` | `./gradlew.bat test` |

Run these from the top-level directory (the one with `src` in it). `run` launches the UI; `test` runs the JUnit suite and writes a report to `build/reports/test/test/index.html`. The functions called by the UI are the same ones used in the tests, so if the tests pass the UI works too.

## Project structure

```
data/                     # CSV datasets (movies, credits, ratings, keywords) — provided
src/main/java/
  interfaces/             # data-set interfaces — provided
  stores/                 # Movies, Credits, Ratings (mine) + provided support classes
  structures/             # my data structures (HashMap, DynamicArray, ...)
src/test/                 # JUnit tests — provided
```

## Academic integrity

This was assessed coursework. If you're currently taking CS126 or a similar module, please don't copy it.
