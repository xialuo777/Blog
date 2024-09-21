delete from blog_comment;
insert into blog_comment
(comment_id, blog_id, commentator, commentator_id, comment_body, comment_create_time, last_id, is_deleted) VALUES
(1, 1, 'test', 11111111, 'test', '2020-01-01 00:00:00', 0, 0)