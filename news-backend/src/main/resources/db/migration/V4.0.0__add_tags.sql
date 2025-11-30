alter table news
    add column advertising boolean;

create table tag_group
(
    id         bigint auto_increment
        primary key,
    created_on date         not null,
    title      varchar(255) not null
);

create table tags
(
    id   bigint auto_increment
        primary key,
    text varchar(255) null
);

create table news_tags
(
    news_id bigint not null,
    tags_id bigint not null,
    primary key (news_id, tags_id),
    constraint FK30dfp9jff63kipjrue2s4931i
        foreign key (tags_id) references tags (id),
    constraint FKi06sdgpsvq2oxtharq5q1rc3x
        foreign key (news_id) references news (id)
);

create table tag_group_tags
(
    tag_group_id bigint not null,
    tags_id      bigint not null,
    constraint UKh65wfdwb53nx40tjbqdd26wko
        unique (tags_id),
    constraint FKf4l3kwe7ky9w51nhe045yvo2g
        foreign key (tag_group_id) references tag_group (id),
    constraint FKhx1di0tc9w7cqdriua5qtykxm
        foreign key (tags_id) references tags (id)
);
