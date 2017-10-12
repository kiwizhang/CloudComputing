DROP TABLE IF EXISTS `15619`.`q4`;
CREATE TABLE  `15619`.`q4` (
  `tweetid` bigint(20) PRIMARY KEY NOT NULL,
  `userid` TEXT,
  `username` TEXT,
  `timestamp` TEXT,
  `text` TEXT,
  `hashtag` TEXT,
  `ip` TEXT,
  `coordinates` TEXT,
  `repliedby` TEXT,
  `reply_count` TEXT,
  `mentioned` TEXT,
  `mentioned_count` TEXT,
  `favoritedby` TEXT,
  `favorite_count` TEXT,
  `useragent` TEXT,
  `filter_level` TEXT,
  `lang` TEXT
)