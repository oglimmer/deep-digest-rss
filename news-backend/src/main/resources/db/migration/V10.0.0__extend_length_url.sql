alter table if exists feed_item_to_process modify column url text not null;

alter table if exists news modify column url text not null;

