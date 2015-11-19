# Introduction #

Whereas the JDK provides a total of four hash map implementations, _jjoost_ provides at least twenty - perhaps forty or even more if you were to base this on functionality rather than explicit classes. This sounds more complicated than it really is, though - the large number is attributable to the fact that in _jjoost_ the hash map implementations (which provide the recognisable put(key, val) etc. methods) have been decoupled from the `HashStore` implementation (which provides lower level operations common to all hash based classes), and each is more customizable than their JDK equivalent; when you multiply the various options together you quickly get to a large number of possible combinations.

For ease these various options are stitched together for you, using either a `MapMaker` or one of the convenience classes (e.g. `SerialScalarHashMap`). Skip to _[Putting it all together](HashMaps#Putting_it_all_together.md)_ below for more information.

## In Detail ##
### The user friendly bits ###
_jjoost_ provides three kinds of unordered map. Each of these interfaces is a variant of `ArbitraryMap` where most of their methods are declared, so the three can be used interchangeably
  * `ScalarMap` - permitting each key to map to at most one value (equivalent to a JDK `Map`)
  * `MultiMap` - permitting keys to map to multiple values, but any particular value at most once per key
  * `ListMap` - permitting each key to map to an arbitrary combination of values

_jjoost_ provides five types of hash based implementations of these:
  * `ScalarHashMap`
  * `InlineMultiHashMap`
  * `InlineListHashMap`
  * `NestedMultiMap` - when backed by a `ScalarHashMap`
  * `NestedListMap` when backed by a `ScalarHashMap`


![http://wiki.jjoost.googlecode.com/hg/images/diagrams/hashing/scalarhashmap_collisions.png](http://wiki.jjoost.googlecode.com/hg/images/diagrams/hashing/scalarhashmap_collisions.png) <br>
<img src='http://wiki.jjoost.googlecode.com/hg/images/diagrams/hashing/inlinemultihashmap_collisions.png' /> <img src='http://wiki.jjoost.googlecode.com/hg/images/diagrams/hashing/nestedmultihashmap_collisions.png' /> <br>
<img src='http://wiki.jjoost.googlecode.com/hg/images/diagrams/hashing/inlinelisthashmap_collisions.png' /> <img src='http://wiki.jjoost.googlecode.com/hg/images/diagrams/hashing/nestedlisthashmap_collisions.png' /> <br>

<code>InlineMultiHashMap</code> and <code>NestedMultiMap</code> both implement the same interface, however they achieve their ends quite differently. Whereas <code>NestedMultiMap</code> is a mapping from key to a heavy-weight object (e.g. a hash set) containing a set of values, <code>InlineMultiHashMap</code> simply stores multiple key->value mappings in the same way that a regular hash map would resolve hash conflicts, i.e. in a linked list chain within the hash structure itself. This has a few benefits over the alternative<br>
<ol><li>For a map where the number of duplicate keys is low (say the average key repetition is 2 to 3 times), the memory requirements are lower - when there are no key repetitions, memory utilisation is the same as a regular hash map<br>
</li><li>These inline maps can and are easily made concurrent, simply by changing the underlying <code>HashStore</code> they are backed by</li></ol>

The downside of course is that for a large number of key repetitions performance begins to degrade, so it is a poor choice if you are expecting a small number of keys and large number of values.<br>
<br>
<br>
<h3>The heavy lifting</h3>
These hash maps all implement their functionality against the interface <code>HashStore</code> which defines a common set of methods that can be used to implement all the hash based structures in <i>jjoost</i>. As a result, by simply defining a new <code>HashStore</code> you instantly implement a range of hash maps and hash sets for no extra effort.<br>
<br>
There are currently four <code>HashStore</code> implementations; namely<br>
<ul><li><code>SerialHashStore</code>
</li><li><code>LinkedSerialHashStore</code> - an extension of <code>SerialHashStore</code>
</li><li><code>LockFreeHashStore</code> (<a href='LockFreeHashStore.md'>wiki</a>)<br>
</li><li><code>LinkedLockFreeHashStore</code> - an extension of <code>LockFreeHashStore</code></li></ul>

The first two of these, when combined with <code>ScalarHashMap</code> provide the <i>jjoost</i> equivalents to <code>HashMap</code> and <code>LinkedHashMap</code> respectively. The <code>LockFreeHashStore</code> and <code>LinkedLockFreeHashStore</code> provide equivalent functionality but with concurrency guarantees. The beauty is that just one "hash map" is implementing all four "different" maps. In fact we already see functionality (besides multi maps etc) that the JDK doesn't provide, as there is no <code>LinkedConcurrentHashMap</code> provided by Sun - just a <code>ConcurrentHashMap</code>.<br>
<br>
<h2>Putting it all together</h2>

With so many combinations available to you (and more options to come), it would be a bit difficult to just start deciding which class you want and creating it yourself, so we have provided a <code>MapMaker</code> class which helps to make the task a little easier.<br>
<br>
<h4>Making Hash Maps With A MapMaker</h4>
This is an example use of a <code>MapMaker</code> setting all of its options to their defaults, for creating a <code>ScalarMap</code>

<pre><code><br>
MapMaker.&lt;K, V&gt;hash()<br>
  .initialCapacity(16)<br>
  .loadFactor(0.75f)<br>
  .type(HashStoreType.serial())<br>
  .keyEq(Equalities.object())<br>
  .valEq(Equalities.object())<br>
  .rehasher(Rehashers.jdkHashmapRehasher())<br>
  .defaultsTo(null)<br>
  .newScalarMap() ;<br>
<br>
// is equivalent to<br>
<br>
MapMaker.&lt;K, V&gt;hash().newScalarMap() ;<br>
<br>
</code></pre>

Let's break this down...<br>
<ul><li>Initially we get a <code>HashMapMaker</code> by calling the static <code>hash()</code> method in the <code>MapMaker</code> class, providing the generic type arguments for the map we want out at the end.<br>
</li><li>Then, we have the two familiar hash map options - the (minimum) initial capacity of the map, and the load factor (the maximum ratio of elements to table size before the table is grown).<br>
</li><li>Now, we have a more interesting option: the <i>type</i> of <code>HashStore</code> - we want to back the map by (currently one of the four discussed in the previous section)</li></ul>

... and finally we get to the juicy customisation options for <i>jjoost</i> hash maps.<br>
<br>
To begin with, all <i>jjoost</i> hash based collections permit you to provide your own classes defining custom <code>hashCode()</code> and <code>equals()</code> functionality by implementing the <code>Equality</code> interface. This makes <code>IdentityHashMap</code> a trivial couple of extra lines of code, not a whole separate class, and in general it means we can avoid creating a host of wrapping objects when such custom functionality is necessary.<br>
<br>
The <code>Equalities</code> class provides a range of default implementations of the <code>Equality</code>. If you want to create a concurrent linked identity hash map, for instance (something definitely not supported by the JDK API!), you could provide the following options:<br>
<br>
<pre><code>MapMaker.&lt;K, V&gt;hash()<br>
  .type(HashStoreType.linkedLockFree())<br>
  .keyEq(Equalities.identity())<br>
  .newScalarMap() ;<br>
</code></pre>

Also, the <b>rehashing</b> algorithm used by maps can be defined. Inside the JDK <code>HashMap</code> and <code>ConcurrentHashMap</code> different algorithms are used to "rehash" the hash provided by your code in order to improve the performance of poor hashes. In <i>jjoost</i> you can select the algorithm to use for this process, or stop this from happening altogether if you know your hash to be of a high quality. The following is an example of this:<br>
<br>
<pre><code>MapMaker.&lt;K, V&gt;hash()<br>
  .rehasher(Rehashers.identity())<br>
  .newScalarMap() ;<br>
</code></pre>

Finally, <i>jjoost</i> permits the user to specify a default value <code>Function&lt;K, V&gt;</code> or <code>Factory&lt;V&gt;</code> for use in a <code>ScalarMap</code>. When the get() method is called and the key has no associated value, this function/factory will be used to generate a value which will be inserted and returned. Unlike in google-collections this is performed as part of the regular insertion process in one pass; in a concurrent collection this reduces the likelihood of spuriously creating a value that is not used.<br>
<br>
<h4>Making Multi and List Hash Maps With MapMaker</h4>

There is also one last option for <code>MultiMap</code> and <code>ListMap</code> construction - the nesting type. This is optionally provided to the <code>newMultiMap()</code> or <code>newListMap()</code> method call, as below:<br>
<br>
<pre><code>MapMaker.&lt;K, V&gt;hash().newMultiMap()<br>
MapMaker.&lt;K, V&gt;hash().newMultiMap(MultiMapNesting.inline())<br>
MapMaker.&lt;K, V&gt;hash().newMultiMap(<br>
    MultiMapNesting.nested(<br>
        SetMaker.&lt;V&gt;hash().newListSetFactory()))<br>
<br>
MapMaker.&lt;K, V&gt;hash().newListMap()<br>
MapMaker.&lt;K, V&gt;hash().newListMap(ListMapNesting.inline())<br>
MapMaker.&lt;K, V&gt;hash().newListMap(<br>
    ListMapNesting.nested(<br>
        SetMaker.&lt;V&gt;hash().newListSetFactory()))<br>
</code></pre>

The first two lines of each are equivalent, as inline "nesting" is the default behaviour. If nesting is preferred then you must provide a <code>Factory</code> which yields a set of the correct type. In the near future there will be a default nested option which will provide a suitable factory for you, however the code above shows how you can easily create one from a hash <code>SetMaker</code> (which supports the same options as the hash <code>MapMaker</code>)