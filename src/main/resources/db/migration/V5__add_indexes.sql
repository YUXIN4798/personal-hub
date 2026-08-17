ALTER TABLE projects
    ADD INDEX idx_projects_status_sort_order_id (status, sort_order, id);

ALTER TABLE resources
    ADD INDEX idx_resources_visibility_created_id (visibility, created_at, id),
    ADD INDEX idx_resources_visibility_category_created_id (visibility, category_id, created_at, id);

ALTER TABLE categories
    ADD INDEX idx_categories_type_sort_order_id (type, sort_order, id);
