CREATE TABLE IF NOT EXISTS subtrees (
    uuid TEXT PRIMARY KEY,
    subtree_string TEXT NOT NULL,
    size INT,
    depth INT
);
