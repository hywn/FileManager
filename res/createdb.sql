BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS `tags` (
	`id`	INTEGER,
	`title`	TEXT
);
CREATE TABLE IF NOT EXISTS `tagged` (
	`path`	TEXT,
	`tag`	NUMERIC
);
CREATE TABLE IF NOT EXISTS `files` (
	`path`	TEXT,
	`title`	TEXT,
	`size`	INTEGER,
	`datecreated`	INTEGER
);
COMMIT;