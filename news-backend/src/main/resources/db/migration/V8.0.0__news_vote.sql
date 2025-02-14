create table news_vote (
    id bigint auto_increment primary key,
    news_id bigint not null,
    user_id bigint not null,
    vote_date date not null,
    constraint fk_news_vote_news_id
        foreign key (news_id) references news(id),
    constraint fk_news_vote_user_id
        foreign key (user_id) references users(id)
);

alter table users
    add column timezone varchar(255) not null;
