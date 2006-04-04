DROP TABLE IF EXISTS ITEM_FILE_ITEM;
CREATE TABLE ITEM_FILE_ITEM (
	ID_INTERNAL int NOT NULL auto_increment,
	KEY_ITEM int NOT NULL,
	KEY_FILE_ITEM int NOT NULL,
    PRIMARY KEY (ID_INTERNAL),
    UNIQUE (KEY_ITEM,KEY_FILE_ITEM)
)TYPE=InnoDB;