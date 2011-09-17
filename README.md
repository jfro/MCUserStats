# UserStats

This plugin will track players signing on & off, logging when they signed on or off along with how long they've played.  Currently only logged to SQLite, MySQL, or MongoDB but other DBs could be done by creating new providers extending DataProvider.

This was started as a companion plugin to my minecraft server website software: [minecraft-server-info](https://github.com/jfro/minecraft-server-info)

## Requirements

* [CraftBukkit](http://bukkit.org/) 1134+
* [MongoDB](http://www.mongodb.org/) (mongo.jar should reside in server's lib/ folder) Download latest mongo.jar from [https://github.com/mongodb/mongo-java-driver/downloads](https://github.com/mongodb/mongo-java-driver/downloads)
* or [MySQL](http://www.mysql.com/) (Currently expects [mysql-connector-java-bin.jar](http://dev.mysql.com/downloads/connector/j/) in server's lib/ folder)
* or [SQLite](http://www.sqlite.org/) (Expects [sqlitejdbc-v056.jar](http://www.zentus.com/sqlitejdbc/) in server's lib folder)

## Todo

* More testing
* More stats (digging, damage, etc.)
