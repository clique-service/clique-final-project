Clique: find your matches
-------------------------

![Clique](https://clique.hagever.com/logo.png)

# Technologies
- Java Backend w/[Vert.x](http://vertx.io)
- JavaScript front end (duh)
- [RethinkDB Database](http://rethinkdb.com)

# Installation
## Prerequisites
- [JDK 8](https://java.com/en/download/) or higher
- [Maven](https://maven.apache.org/)
- [RethinkDB](http://www.rethinkdb.com), but you can launch it on [Docker](http://docker.io) if you are on a windows machine. :sob:

## Default Environment Variables
    RETHINKDB_HOSTNAME="localhost"
    RETHINKDB_PORT="28015"
    RETHINKDB_DBNAME="clique_facebook_data"
    FACEBOOK_APP_ID="TEST_APP_ID"
    FACEBOOK_APP_SECRET="TEST_APP_SECRET"
    FACEBOOK_REDIRECT_URI="http://localhost:9000/auth/facebook/callback"

## Download and configure
Clone the project:

```bash
clone git@gitlab.com:clique/CliqueFinalProject.git
```

Then, download all the jars using your IDE or just use Maven

```bash
mvn clean compile package
```

You can run it all using

```
./start.sh
```

You can browse it in - http://localhost:9000

If you'd like to stop it, you can

```
./stop.sh
```
