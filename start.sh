#!/bin/bash
java -jar ./EventAttendeesHandler/target/EventAttendeesHandler-1.0-SNAPSHOT-fat.jar start -cluster clique.verticles.EventAttendeesHandler
java -jar ./EventInterestedsHandler/target/EventInterestedsHandler-1.0-SNAPSHOT-fat.jar start -cluster clique.verticles.EventInterestedsHandler
java -jar ./EventMaybesHandler/target/EventMaybesHandler-1.0-SNAPSHOT-fat.jar start -cluster clique.verticles.EventMaybesHandler
java -jar ./LikePostsHandler/target/LikePostsHandler-1.0-SNAPSHOT-fat.jar start -cluster clique.verticles.LikePostsHandler
java -jar ./PostLikesHandler/target/PostLikesHandler-1.0-SNAPSHOT-fat.jar start -cluster clique.verticles.PostLikesHandler
java -jar ./SharedTableDataInsertionHandler/target/SharedTableDataInsertionHandler-1.0-SNAPSHOT-fat.jar start -cluster clique.verticles.SharedTableDataInsertionHandler
java -jar ./UserEventsHandler/target/UserEventsHandler-1.0-SNAPSHOT-fat.jar start -cluster clique.verticles.UserEventsHandler
java -jar ./UserInitHandler/target/UserInitHandler-1.0-SNAPSHOT-fat.jar start -cluster clique.verticles.UserInitHandler
java -jar ./UserLikesHandler/target/UserLikesHandler-1.0-SNAPSHOT-fat.jar start -cluster clique.verticles.UserLikesHandler
java -jar ./UserTaggedPlacesHandler/target/UserTaggedPlacesHandler-1.0-SNAPSHOT-fat.jar start -cluster clique.verticles.UserTokenHandler
java -jar ./UserTokenHandler/target/UserTokenHandler-1.0-SNAPSHOT-fat.jar start -cluster clique.verticles.UserTokenHandler
java -jar ./Web/target/Web-1.0-SNAPSHOT-fat.jar start -cluster clique.verticles.FacebookAuthenticate
