create table d_syndication (
	_id INTEGER PRIMARY KEY autoincrement,
	syn_name text NOT NULL,	
	syn_url text NOT NULL,
	syn_website_url text NOT NULL,
	syn_is_active INTEGER NOT NULL,
	syn_number_click INTEGER NOT NULL,
	syn_last_extract_time datetime NOT NULL,
	syn_creation_date datetime NOT NULL
);

create table d_publication (
	_id INTEGER PRIMARY KEY autoincrement,
	pub_link text NOT NULL,
	pub_title text NOT NULL,
	pub_already_read integer,
	pub_publication text,
	pub_publication_date datetime NOT NULL,
	syn_syndication_id INTEGER NOT NULL,
	FOREIGN KEY(syn_syndication_id) REFERENCES d_syndication( _id)
);

create table d_categorie (
	_id INTEGER PRIMARY KEY autoincrement,
	cat_name text NOT NULL,
);

create table d_categorie_syndication (
	_id INTEGER PRIMARY KEY autoincrement,
	cas_categorie_id INTEGER NOT NULL,
	syn_syndication_id INTEGER NOT NULL,
	FOREIGN KEY(cas_categorie_id) REFERENCES d_categorie( _id),
	FOREIGN KEY(syn_syndication_id) REFERENCES d_syndication( _id)
);