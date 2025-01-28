SET FOREIGN_KEY_CHECKS = 0;
DROP INDEX UKh65wfdwb53nx40tjbqdd26wko ON tag_group_tags;
CREATE INDEX UKh65wfdwb53nx40tjbqdd26wko ON tag_group_tags (tags_id);
SET FOREIGN_KEY_CHECKS = 1;
