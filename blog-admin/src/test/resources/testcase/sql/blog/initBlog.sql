delete from blog;
insert into blog (blog_id, blog_title, user_id, blog_desc, blog_content, category_id, category_name, blog_status, blog_tags, thumbnail, view_count, creat_time, update_time, is_top, enable_comment, is_delete, sub_url)
VALUES
(1, 'test', 11111111, 'test', 'test', 1, 'test', 0, 'test', 'test', 1, '2020-01-01 00:00:00', '2020-01-01 00:00:00', 0, 0, 0, 'test');