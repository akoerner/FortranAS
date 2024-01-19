CREATE TABLE IF NOT EXISTS clones (
    uuid TEXT PRIMARY KEY,
    reference_uuid TEXT NOT NULL,
    candidate_uuid TEXT NOT NULL,
    bleu_score DOUBLE DECIMAL(3, 2) NOT NULL,
    cosine_similarity DOUBLE DECIMAL(3, 2) NOT NULL,
    sorensen_dice_coefficient DOUBLE DECIMAL(3, 2) NOT NULL,
    jaro_winkler_similarity DOUBLE DECIMAL(3, 2) NOT NULL

);
