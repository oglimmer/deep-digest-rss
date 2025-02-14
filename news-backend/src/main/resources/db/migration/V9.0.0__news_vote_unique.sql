
alter table news_vote
    add constraint unique_vote unique (news_id, user_id, vote_date);