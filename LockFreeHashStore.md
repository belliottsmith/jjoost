## The Lock Free Hash Store ##

One of the coolest features of _jjoost_ is its `LockFreeHashStore`. The JDK `ConcurrentHashMap` has a magic "concurrency level" that many don't really understand, but which effectively aims to increase the average number of threads that may concurrently write to the map at any one time by dividing the table up into that many segments, each of which has its own write lock.

There are some theoretical problems with this approach (although it generally works very well), the main being that there is absolutely no guarantee that the elements being inserted into the map will all fall evenly into the different segments - if a smaller number of segments is taking the bulk of the load then the concurrency of the map declines. Add to this the fact that growing a given segment blocks out all writers for an extended period of time and you can see that whilst overall performance may be quite good, a given thread could be starved for quite some time.

Some properties of `ConcurrentHashMap` are:

  * Readers never block
  * Grows
    1. block writers to the segment
    1. occupy a single thread for their entire duration
    1. involve duplicating every item in the segment as well as the array; depending on the number of segments growing, memory utilisation can spiral
  * Writers block other writers to the segment when inserting or deleting
  * Deletions may not reclaim memory
  * It is difficult to get the exact size of the map because the size of all segments must be summed

Enter the `LockFreeHashStore`; some of its properties are:

  * Grows migrate each table index individually to a their new location; so
    1. the whole table is never made unavailable; only the buckets actively being migrated are made unavailable, and hence each for a much shorter time
    1. the old records are promptly made available for garbage collection
    1. all writers help with the grow operation to minimise the time taken overall _and by any single thread_
  * Inserts do not block writers or readers
  * Deletions block only those threads attempting to look past the item in its collision chain, and for only an instant; memory is made immediately available for garbage collection
  * Size can easily be determined as there is one global size; _or_ maintenance of the size can be turned off to further increase throughput; this is useful if the number of buckets is to remain static

A variant on the algorithm is planned for the near future which would offer similar levels of concurrency, but also maintain the "readers never block" property of the JDK `ConcurrentHashMap`.


---


### `LockFreeHashStore` - _under the hood_ ###

Most hash tables are implemented as an array of linked lists, where each linked list is a data entry plus a pointer to the rest of the list; the end of the list is indicated by this pointer having a value of `null`, and the empty list as such is simply a `null` entry in the array.

<sub>A regular hash map</sub>
![http://wiki.jjoost.googlecode.com/hg/images/diagrams/hashing/lfhs_regularmap_diagram.png](http://wiki.jjoost.googlecode.com/hg/images/diagrams/hashing/lfhs_regularmap_diagram.png)

The `LockFreeHashStore` supplements this by supporting compare and set operations for the `next` pointer of each entry in the linked list. Inserts may happen at any point in the list and are achieved by first setting the new node's `next` pointer to the `next` pointer of the node that will precede it in the list, and immediately performing a compare and set on this existing node's `next` from the value we have just given to our new node's `next` to the new node itself.

![http://wiki.jjoost.googlecode.com/hg/images/diagrams/hashing/lfhs_insertion.png](http://wiki.jjoost.googlecode.com/hg/images/diagrams/hashing/lfhs_insertion.png)

Deletions are then performed in two stages; if we are deleting a node `n`, first the `n.next` is set to a global flag node which indicates that the node is in the process of being deleted; we save the value of `n.next` that we overwrite and then compare and set the previous node's `next` pointer from `n` to this saved value. Finally we set `n.next` to another flag which indicates it has **been** deleted. Any threads that had reached this `DELETING_FLAG` through `n.next` for the duration of this brief activity will have put themselves to sleep, so once we have modified `n.next` to point to the `DELETED_FLAG` we wake them up again.

![http://wiki.jjoost.googlecode.com/hg/images/diagrams/hashing/lfhs_deletion.png](http://wiki.jjoost.googlecode.com/hg/images/diagrams/hashing/lfhs_deletion.png)

Finally, in order to support grows without impeding access to the map, the `LockFreeHashStore` also abstracts its array (table) of linked lists into an interface providing a few basic methods for reading and writing the head node for each linked list. Instead of doing so based on array index, it is performed based on the full hash code, and the implementing class is free to do what it wants with that. When the table is not growing the implementation is simply an array wrapper; however, when growing, the table maintains a bitmap of buckets that have been migrated and those that have not; readers are directed to either the old or new array, depending on if the bucket has been migrated; writers always go to the new array _and migrate the old bucket first if it has not already been done_. As soon as it begins migrating the bucket it replaces the head of the bucket in the old array with a flag node value we have called the `REHASHING_FLAG`; readers/writers that encounter this flag without the bitmap indicating that the bucket has been fully migrated will block until this has been done. Once a writer is co-opted into growing it will attempt to grow all proceeding buckets until it encounters one that has already been migrated, at which point it will return to its task of modifying the bucket it was intended for. This way all writers share some of the burden of growing the array so that no thread's workload is starved, nor are any threads starved from access to the map.

![http://wiki.jjoost.googlecode.com/hg/images/diagrams/hashing/lfhs_growing.png](http://wiki.jjoost.googlecode.com/hg/images/diagrams/hashing/lfhs_growing.png)