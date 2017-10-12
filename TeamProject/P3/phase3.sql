DROP TABLE IF EXISTS `15619`.`q4`;
CREATE TABLE  `15619`.`q4` (
  `tweetid` bigint(20) unsigned NOT NULL,
  `userid` text,
  `username` text,
  `timestamp` text,
  `text` text,
  `hashtag` text,
  `ip` text,
  `coordinates` text,
  `repliedby` text,
  `reply_count` text,
  `mentioned` text,
  `mentioned_count` text,
  `favoritedby` text,
  `favorite_count` text,
  `useragent` text,
  `filter_level` text,
  `lang` text,
  PRIMARY KEY (`tweetid`)
)