## What is _jjoost_? ##
_jjoost_ aims to be a _**complete replacement**_ to the existing Java Collections API and all projects that extend it. The aim is to provide a better, faster, configurable and more functionally complete feature set.

_jjoost_ is currently in pre-alpha condition; i.e. the majority of functionality has been provided but neither documented nor tested, nor had community input. If you would like to take a look at what has been produced, please access the Mercurial repository for the latest source; if you have any opinion on aspects of this project or other input we would love to hear from you (see below).


---

### Why create _jjoost_ when google-collections and apache commons exist? ###
Because they all have certain properties that are undesirable. The certain property most in question being that _they are all based on the original JDK Collections API_. So much effort is spent ensuring modifications are compatible with this API that only half of any possible benefit is ever really explored.

---

### What is special about _jjoost_? ###

On top of redesigning the collections interfaces, completely new implementations have been provided. Due to a better separation of concerns, with barely any extra code, _jjoost_ supports a **substantially** increased number of kinds of collection.

#### Some of the best features of _jjoost_ are ####

  * `MultiMap`, `ListMap`, `Map` and even `BiMap` are all interchangeable via the `AnyMap` common interface. The same is true for `MultiSet`, `Set` and `AnySet`.

  * Lock Free `MultiMap`, `ListMap` and `Map` - providing greater parallelism than the JDK `ConcurrentHashMap` and, to our knowledge, the **only** concurrent multi maps for Java available in a free library (see LockFreeHashStore for details)

  * Concurrent implementations of `LinkedHashMap` and `IdentityHashMap` supporting all map types

  * `MultiMap` and `ListMap` have both "Inline" and "Nested" hash based implementations - other libraries support only the latter (see HashMaps for details)

  * `MultiSet` has "Counting" and "Nested" varieties (the latter saving the actual instances of each duplicate value rather than just the number of duplicates) - other libraries provide only the former

  * `BiMap` supports _One-To-One_, _Many-To-Many_, _One-To-Many_ (and more) two way lookups

  * Even more functionality planned for our ordered map replacements

<br>
<br>
<hr />
<h2>Interested in helping out?</h2>
Anyone interested in helping out: we are actively interested in finding people (anyone!) interested in helping out - there's lots of work to do in every area: unit testing, documentation, wiki authoring and, obviously, coding.<br>
<br>
If you have a strong opinion about how collections libraries should be build, get in touch and have a say in how we put together <i>jjoost</i>. It is still in its early stages so is open to even major changes if they are warranted.<br>
<br>
If you are interested in understanding a bit more about the algorithms which underpin all of our applications then this project is an excellent way to do that. The eventual aim is to provide multiple implementations for every kind of map (at least two more kinds of hash table implementations are planned and four or more base types of ordered collection).<br>
<br>
Or if perhaps you are a recent graduate, and are looking to get some practical experience and perhaps something to put on your CV, give me a shout<br>
<br>
Please send me an email at benedict at jjoost dot org if you are interested.