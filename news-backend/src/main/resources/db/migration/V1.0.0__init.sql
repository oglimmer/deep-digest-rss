create table news
(
    created_on datetime(6)  not null,
    id         bigint auto_increment
        primary key,
    ref_id     varchar(255) not null,
    url        varchar(255) not null,
    text       mediumtext   not null,
    title      varchar(255) not null,
    constraint UK29kt4mh5b8sn42l3nq9bmtk66
        unique (ref_id)
);
