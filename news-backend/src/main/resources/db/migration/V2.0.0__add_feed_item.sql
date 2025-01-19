create table feed
(
    created_on datetime(6)  not null,
    id         bigint auto_increment
        primary key,
    title      varchar(255) not null,
    url        varchar(255) not null
);

create table feed_item_to_process
(
    created_on    datetime(6)                                  not null,
    feed_id       bigint                                       not null,
    id            bigint auto_increment
        primary key,
    updated_on    datetime(6)                                  not null,
    ref_id        varchar(255)                                 not null,
    title         varchar(255)                                 not null,
    url           varchar(255)                                 not null,
    process_state enum ('DONE', 'ERROR', 'IN_PROGRESS', 'NEW') not null,
    constraint UKqq528yv0hp5asschk1c0j6yml
        unique (ref_id),
    constraint FKooyif7a1yt9yp78sv8syo41ue
        foreign key (feed_id) references feed (id)
);

alter table news
    add column feed_id bigint null,
    add column original_feed_item_id bigint null;

insert into feed (created_on, title, url) values (now(), 'heise', 'https://www.heise.de/rss/heise-atom.xml');
update news set feed_id = (select id from feed where title = 'heise');

insert into feed_item_to_process
    (created_on, feed_id, updated_on, ref_id, title, url, process_state)
values (now(), (select id from feed where title = 'heise'), now(), 'legacy', 'legacy', 'legacy', 'DONE');
update news set original_feed_item_id = (select id from feed_item_to_process where ref_id = 'legacy');

alter table news
    modify column feed_id bigint not null,
    modify column original_feed_item_id bigint not null,
    add constraint FK427c1n2b9pxdi2iv4j3ik98ew foreign key (original_feed_item_id) references feed_item_to_process (id),
    add constraint FKd48cwwuqwbi8twwn4rpe0vpuu foreign key (feed_id) references feed (id);

ALTER TABLE news
    DROP COLUMN ref_id;

insert into feed (created_on, title, url) values (now(), 'spiegel', 'https://www.spiegel.de/schlagzeilen/index.rss');
