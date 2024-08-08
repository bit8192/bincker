drop table if exists blog;
create table blog(
                     id integer not null primary key autoincrement,
                     title varchar(255),
                     file_path varchar(1024),
                     sort int default null,
                     hits bigint default 0,
                     shares bigint default 0,
                     file_last_modified timestamp not null default current_timestamp,
                     created_time timestamp not null default current_timestamp,
                     updated_time timestamp not null default current_timestamp,
                     deleted tinyint(1) not null default 0
);