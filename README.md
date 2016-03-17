# [Clique](https://clique.hagever.com): find your matches

# Technologies :star2::star::star2:
- Java Backend w/[Vert.x](http://vertx.io) :muscle:
- JavaScript front end (duh) :muscle:
- [RethinkDB Database](http://rethinkdb.com) :muscle::muscle::muscle::heart:

# Installation :computer:
## Prerequisites
- [JDK 8](https://java.com/en/download/) or higher
- [Maven](https://maven.apache.org/)
- [RethinkDB](http://www.rethinkdb.com), but you can launch it on [Docker](http://docker.io) if you are on a windows machine. :smirk:

## Default Environment Variables
    RETHINKDB_HOSTNAME="localhost"
    RETHINKDB_PORT="28015"
    RETHINKDB_DBNAME="clique_facebook_data"
    FACEBOOK_APP_ID="TEST_APP_ID"
    FACEBOOK_APP_SECRET="TEST_APP_SECRET"
    FACEBOOK_REDIRECT_URI="http://localhost:9000/auth/facebook/callback"

## Download :floppy_disk: and configure
Clone the project:

```bash
clone git@gitlab.com:clique/CliqueFinalProject.git
```

Then, download all the jars using your IDE or just use Maven :star:

```bash
mvn clean compile package
```

You can run it all using

```
./start.sh
```

You can browse it in - http://localhost:9000 :tada:

If you'd like to stop it, you can

```
./stop.sh
```
