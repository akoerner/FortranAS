CREATE TABLE IF NOT EXISTS fortran_files (
    uuid VARCHAR(36) PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    sha256sum CHAR(64) NOT NULL,
    line_count INT,
    abstract_syntax_tree_node_count INT,
    parse_tree_node_count INT,
    token_count INT
);
