#!/bin/bash
java -jar ./EventAttendeesHandler/target/EventAttendeesHandler-1.0-SNAPSHOT-fat.jar start -cluster
java -jar ./EventInterestedsHandler/target/EventInterestedsHandler-1.0-SNAPSHOT-fat.jar start -cluster
java -jar ./EventMaybesHandler/target/EventMaybesHandler-1.0-SNAPSHOT-fat.jar start -cluster
java -jar ./LikePostsHandler/target/LikePostsHandler-1.0-SNAPSHOT-fat.jar start -cluster
java -jar ./Main/target/Main-1.0-SNAPSHOT-fat.jar start -cluster
java -jar ./PostLikesHandler/target/PostLikesHandler-1.0-SNAPSHOT-fat.jar start -cluster
java -jar ./Shared/target/Shared-1.0-SNAPSHOT-fat.jar start -cluster
java -jar ./SharedTableDataInsertionHandler/target/SharedTableDataInsertionHandler-1.0-SNAPSHOT-fat.jar start -cluster
java -jar ./target/clique-1.0-SNAPSHOT-fat.jar start -cluster
java -jar ./UserEventsHandler/target/UserEventsHandler-1.0-SNAPSHOT-fat.jar start -cluster
java -jar ./UserInitHandler/target/UserInitHandler-1.0-SNAPSHOT-fat.jar start -cluster
java -jar ./UserLikesHandler/target/UserLikesHandler-1.0-SNAPSHOT-fat.jar start -cluster
java -jar ./UserTaggedPlacesHandler/target/UserTaggedPlacesHandler-1.0-SNAPSHOT-fat.jar start -cluster
java -jar ./UserTokenHandler/target/UserTokenHandler-1.0-SNAPSHOT-fat.jar start -cluster
java -jar ./Web/target/Web-1.0-SNAPSHOT-fat.jar start -cluster
