# ConstructR-Redis #
[![Build Status](https://travis-ci.org/everpeace/constructr-redis.svg?branch=master)](https://travis-ci.org/everpeace/constructr-redis)

This library enables to use [Redis](https://redis.io/) as cluster coordinator in a [ConstructR](https://github.com/hseeberger/constructr) based node.

[ConstructR](https://github.com/hseeberger/constructr) aims at cluster bootstrapping (construction) by using a coordination service and provides etcd as the default one. By means of this library, you will be able to use [Redis](https://redis.io/) as coordination service instead.

You will need to add the following dependency in your `build.sbt` in addition to the core ConstructR ones:

```
resolvers += Resolver.bintrayRepo("everpeace", "maven")

libraryDependencies += "com.github.everpeace" %% "constructr-coordination-redis" % "0.0.3",
```


## Configuration ##

Check [this section](https://github.com/hseeberger/constructr#coordination) in ConstructR for general information about configuration.

Check [reference.conf](constructr-coordination-redis/src/main/resources/reference.conf) for redis related configurations.

## Testing

Requirements:
  - redis needs to be running, e.g. via ` docker run --name some-redis -p 6379:6379 -d redis`

Run tests:
  - `sbt test`

## Contribution policy ##

Contributions via GitHub pull requests are gladly accepted from their original author. Along with any pull requests, please state that the contribution is your original work and that you license the work to the project under the project's open source license. Whether or not you state this explicitly, by submitting any copyrighted material via pull request, email, or other means you agree to license the material under the project's open source license and warrant that you have the legal authority to do so.

Please make sure to follow these conventions:
- For each contribution there must be a ticket (GitHub issue) with a short descriptive name, e.g. "Respect seed-nodes configuration setting"
- Work should happen in a branch named "ISSUE-DESCRIPTION", e.g. "32-respect-seed-nodes"
- Before a PR can be merged, all commits must be squashed into one with its message made up from the ticket name and the ticket id, e.g. "Respect seed-nodes configuration setting (closes #32)"

## License ##

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").