ALTER TABLE feed_item_to_process
    MODIFY COLUMN process_state enum ('DONE', 'ERROR', 'IN_PROGRESS', 'NEW', 'FAILED') NOT NULL;

ALTER TABLE feed_item_to_process
    ADD COLUMN failure_count INT NOT NULL DEFAULT 0;
