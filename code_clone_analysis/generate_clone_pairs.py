import sqlite3

import os
import json

import sqlite3
import shutil
import os
import tempfile

from statistics import mean, median, mode, stdev
from collections import Counter


script_path = os.path.abspath(__file__)


def get_statistics(clones, keys):
    result_list = []

    for key in keys:
        [ json.dumps(clone, indent=4)for clone in clones]
        values = [clone[key] for clone in clones]

        mean_value = "{:.2f}".format(mean(values))
        median_value = "{:.2f}".format(median(values))
        mode_value = mode(values)
        max_value = max(values)
        min_value = min(values)
        std_dev_value = "{:.2f}".format(stdev(values))


        key_stats = {
            "key": key,
            "mean": mean_value,
            "median": median_value,
            "mode": mode_value,
            "max": max_value,
            "min": min_value,
            "std": std_dev_value
        }

        result_list.append(key_stats)

    return result_list




clone_file_template = '''{header}: {file}
Start character index:   {char_start_index}
End character index:     {char_stop_index}
Character count:         {character_count}
Start line index:        {start_line_index}
Stop line index:         {stop_line_index}
Line count:              {line_count}
Subtree size:            {subtree_size}
Subtree depth:           {subtree_depth}
Subtree string:          {subtree_string}

BLEU score:              {bleu_score}
Jaro-Winkler similarity: {jaro_winkler_similarity}

Source code:

<<<<<<< START EXCERPT
{source_code}
>>>>>>> END EXCERPT
'''


def create_directory(directory_path):
    """
    Create a directory at the specified path.

    Parameters:
    - directory_path (str): The path of the directory to be created.
    """
    try:
        os.makedirs(directory_path)
        print(f"Directory created at {directory_path}")
    except FileExistsError:
        print(f"Directory already exists at {directory_path}")
    except Exception as e:
        print(f"Error: {e}")


def create_file(file_path, contents):
    """
    Create a file at the specified path with the given contents.

    Parameters:
    - file_path (str): The path of the file to be created.
    - contents (str): The contents to be written to the file.
    """
    try:
        with open(file_path, 'w', encoding='utf-8') as file:
            file.write(contents)
        print(f"File created at {file_path}")
    except Exception as e:
        print(f"Error: {e}")


def create_clone_files(reference, candidate, reference_subtree, candidate_subtree, clone_dict):
    reference_char_start_index = reference['char_start_index']
    reference_char_stop_index = reference['char_stop_index']
    reference_character_count = reference_char_stop_index - reference_char_start_index
    reference_file = reference['fortran_file']
    candidate_char_start_index = candidate['char_start_index']
    candidate_char_stop_index = candidate['char_stop_index']
    candidate_character_count = candidate_char_stop_index - candidate_char_start_index
    candidate_file = candidate['fortran_file']

    reference_source_code = get_chars(reference_file, reference_char_start_index, reference_char_stop_index + 1)
    reference_clone_file = clone_file_template.format(
        header="Reference file(left)",
        file= fortranas_dir + "/" +  reference_file,
        char_start_index=reference_char_start_index,
        char_stop_index=reference_char_stop_index,
        character_count=reference_character_count,
        start_line_index=reference['start_line_index'],
        stop_line_index=reference['stop_line_index'],
        line_count=reference['line_count'],
        subtree_size=reference_subtree['size'],
        subtree_depth=reference_subtree['depth'],
        subtree_string=reference_subtree['subtree_string'],
        bleu_score=clone_dict['bleu_score'],
        jaro_winkler_similarity=clone_dict['jaro_winkler_similarity'],
        source_code=reference_source_code
    )
    candidate_source_code = get_chars(candidate_file, candidate_char_start_index, candidate_char_stop_index + 1)
    candidate_clone_file = clone_file_template.format(
        header="Candidate file(right)",
        file= fortranas_dir + "/" +  candidate_file,
        char_start_index=candidate_char_start_index,
        char_stop_index=candidate_char_stop_index,
        character_count=candidate_character_count,
        start_line_index=candidate['start_line_index'],
        stop_line_index=candidate['stop_line_index'],
        line_count=candidate['line_count'],
        subtree_size=candidate_subtree['size'],
        subtree_depth=candidate_subtree['depth'],
        subtree_string=candidate_subtree['subtree_string'],
        bleu_score=clone_dict['bleu_score'],
        jaro_winkler_similarity=clone_dict['jaro_winkler_similarity'],
        source_code=candidate_source_code
    )
    reference_clone_file_name=output_directory + "/references/" + "{:0>4.2f}".format(clone_dict['bleu_score']) + "_" + reference['uuid'] + "__" + candidate['uuid'] + ".txt"
    candidate_clone_file_name=output_directory + "/candidates/" + "{:0>4.2f}".format(clone_dict['bleu_score']) + "_" + reference['uuid'] + "__" + candidate['uuid'] + ".txt"
    create_file(reference_clone_file_name, reference_clone_file)
    create_file(candidate_clone_file_name, candidate_clone_file)


def memcache_open(source_db_path):
    """
    Place sql database in memory.

    Parameters:
    - source_db_path (str): The path to the sqlite3 database the file.

    Returns:
    - connection: sqlite connection object of sqlite database loaded into memory
    """

    global memcache_dir
    memcache_dir = tempfile.mkdtemp(dir='/dev/shm')

    temp_db_path = os.path.join(memcache_dir, 'temp_database.db')

    shutil.copy2(source_db_path, temp_db_path)

    temp_conn = sqlite3.connect(temp_db_path)

    def close():
        temp_conn.close()
        shutil.rmtree(memcache_dir)
    return temp_conn


def memcache_close():
    shutil.rmtree(memcache_dir)


def get_chars(file_path, start_index, stop_index):
    """
    Get the contents of a file from start_index to stop_index as a string.

    Parameters:
    - file_path (str): The path to the file.
    - start_index (int): The starting character index.
    - stop_index (int): The stopping character index.

    Returns:
    - str: The contents of the file from start_index to stop_index.
    """
    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            file_contents = file.read()
            result = file_contents[start_index:stop_index]
            return result
    except FileNotFoundError:
        return f"Error: File not found at path {file_path}"
    except Exception as e:
        return f"Error: {e}"


def row_to_dict(sql_row, column_names):
    """
    Convert an SQL row (tuple) to a dictionary using given column names.

    Parameters:
    - sql_row: An SQL row (tuple) object.
    - column_names: A sequence containing column names.

    Returns:
    - A dictionary representation of the SQL row.
    """
    if sql_row is None or column_names is None:
        return None

    row_dict = {column: value for column, value in zip(column_names, sql_row)}

    return row_dict


def rows_to_list(rows, column_names):
    """
    Convert SQL rows to a list of dictionaries.

    Parameters:
    - rows: A list of tuples representing SQL rows.
    - column_names: A list of column names corresponding to the order in the rows.

    Returns:
    - A list of dictionaries, where each dictionary represents a row.
    """
    result = [dict(zip(column_names, row)) for row in rows]
    return result

def get_line_numbers(node):
    """
    Returns extracted lines from a file defined in the node dictionary

    Parameters:
    - nodes: A list of tuples representing SQL rows.

    Returns:
    - The extracted set of lines from the file node['fortran_file'] between the
      node['char_start_index'] and node['char_stop_index'].
    """
    result = {'start_line': None, 'stop_line': None, 'line_count': None}

    try:
        file_path = node.get('fortran_file')
        start_char_index = node.get('char_start_index')
        stop_char_index = node.get('char_stop_index')

        with open(file_path, 'r') as file:
            lines = file.readlines()

            start_line = 1
            start_index = 0
            while start_index < start_char_index:
                start_index += len(lines[start_line - 1])
                start_line += 1

            stop_line = start_line
            stop_index = start_index
            while stop_index < stop_char_index:
                stop_index += len(lines[stop_line - 1])
                stop_line += 1

            result['start_line'] = start_line
            result['stop_line'] = stop_line
            result['line_count'] = stop_line - start_line + 1

    except FileNotFoundError:
        print(f"Error: File '{file_path}' not found.")
    except Exception as e:
        print(f"An error occurred: {e}")

    return result


def get_bleu_score_extrema(database_path):

    connection = sqlite3.connect(f"file:{database_path}?mode=ro", uri=True)
    cursor = connection.cursor()
    select_query = '''
    SELECT MIN(bleu_score) AS min_bleu_score, MAX(bleu_score) AS max_bleu_score
    FROM clones;
    '''
    cursor.execute(select_query)
    result = cursor.fetchone()
    connection.close()
    return {"min_bleu_score": result[0], "max_bleu_score": result[1]}

def get_clone_count(database_path, bleu_score_lower_limit, bleu_score_upper_limit):

    connection = sqlite3.connect(f"file:{database_path}?mode=ro", uri=True)
    cursor = connection.cursor()
    count_query = '''
    SELECT COUNT(*) 
    FROM clones 
    WHERE bleu_score >= ? AND bleu_score <= ?;
    '''
    cursor.execute(count_query, (bleu_score_lower_limit, bleu_score_upper_limit))
    result = cursor.fetchone()[0]
    connection.close()
    return result


def get_clones(database_path, bleu_score_lower_limit, bleu_score_upper_limit, limit, offset):

    #connection = memcache_open(database_path)
    print(database_path)
    connection = sqlite3.connect(f"file:{database_path}?mode=ro", uri=True)
    cursor = connection.cursor()
    clones = []
    try:
        query = f"""
        SELECT * 
        FROM clones 
        WHERE bleu_score >= {bleu_score_lower_limit} AND bleu_score <= {bleu_score_upper_limit} 
        ORDER BY bleu_score DESC 
        LIMIT {limit} OFFSET {offset};
        """
        cursor.execute(query)

        clone_column_names = [description[0] for description in cursor.description]

        clone_rows = cursor.fetchall()
        for clone_row in clone_rows:

            clone_dict = row_to_dict(clone_row, clone_column_names)

            cursor.execute("SELECT * FROM subtrees WHERE uuid = ?", (clone_dict['reference_uuid'],))
            column_names = [column[0] for column in cursor.description]
            reference_subtree = row_to_dict(cursor.fetchone(), column_names)
            column_names = None

            cursor.execute("SELECT * FROM subtrees WHERE uuid = ?", (clone_dict['candidate_uuid'],))
            column_names = [column[0] for column in cursor.description]
            candidate_subtree = row_to_dict(cursor.fetchone(), column_names)
            column_names = None

            #cursor.execute("SELECT * FROM nodes WHERE subtree_uuid = ?", (reference_subtree['uuid'],))
            threshold_line_count = 0
            if not reference_subtree:
                continue
            cursor.execute("SELECT * FROM nodes WHERE subtree_uuid = ? AND line_count > ?", (reference_subtree['uuid'], threshold_line_count))
            reference_nodes = cursor.fetchall()
            if not reference_nodes:
                continue
            column_names = [column[0] for column in cursor.description]
            reference_nodes = rows_to_list(reference_nodes, column_names)
            column_names = None

            if not candidate_subtree:
                continue
            cursor.execute("SELECT * FROM nodes WHERE subtree_uuid = ? AND line_count > ?", (candidate_subtree['uuid'], threshold_line_count))
            candidate_nodes = cursor.fetchall()
            if not candidate_nodes:
                continue
            column_names = [column[0] for column in cursor.description]
            candidate_nodes = rows_to_list(candidate_nodes, column_names)
            column_names = None

            #print(json.dumps(clone_dict, indent=4))
            for reference in reference_nodes:
                for candidate in candidate_nodes:
                    if reference['fortran_file'] == candidate['fortran_file']:
                        continue

                    if not reference or not candidate or not clone_dict:
                        continue
                    reference['char_count'] = reference['char_stop_index'] - reference['char_start_index']
                    #reference['line_count'] = reference['stop_line_index'] - reference['stop_line_index'] + 1
                    candidate['char_count'] = candidate['char_stop_index'] - candidate['char_start_index']
                    #candidate['line_count'] = candidate['stop_line_index'] - candidate['stop_line_index'] + 1
                    if reference['char_count'] == 0:
                        continue
                    if candidate['char_count'] == 0:
                        continue

                    if reference['fortran_file'] == candidate['fortran_file']:
                        continue
                    #reference.update(get_line_numbers(reference))
                    #candidate.update(get_line_numbers(candidate))

                    #if reference['line_count'] == 1 or candidate['line_count'] == 1:
                    #    continue
                    create_clone_files(reference, candidate, reference_subtree, candidate_subtree, clone_dict)
                    clone = {
                        "reference_node_uuid": reference['uuid'],
                        "reference_char_start_index": reference['char_start_index'],
                        "reference_char_stop_index": reference['char_stop_index'],
                        "reference_char_count": reference['char_count'],
                        "reference_start_line_number": reference['start_line_index'],
                        "reference_stop_line_number": reference['stop_line_index'],
                        "reference_line_count": reference['line_count'],
                        "reference_file": reference['fortran_file'],
                        "candidate_node_uuid": candidate['uuid'],
                        "candidate_char_start_index": candidate['char_start_index'],
                        "candidate_char_stop_index": candidate['char_stop_index'],
                        "candidate_char_count": candidate['char_count'],
                        "candidate_start_line_index": candidate['start_line_index'],
                        "candidate_stop_line_index": candidate['stop_line_index'],
                        "candidate_line_count": candidate['line_count'],
                        "candidate_file": candidate['fortran_file'],
                        "reference_subtree_size": reference_subtree['size'],
                        "reference_subtree_depth": reference_subtree['depth'],
                        "reference_subtree_string": reference_subtree['subtree_string'],
                        "candidate_subtree_size": candidate_subtree['size'],
                        "candidate_subtree_depth": candidate_subtree['depth'],
                        "candidate_subtree_string": candidate_subtree['subtree_string'],
                        "bleu_score": clone_dict['bleu_score'], 
                        "jaro_winkler_similarity": clone_dict['jaro_winkler_similarity'] 
                    }
                    clone.update({key: value for key, value in clone_dict.items() if key not in ["uuid", "reference_uuid", "candidate_uuid"]})

                    clones.append(clone)
                    if len(clones) > 3:
                        break
 
                    clone_dict = None
    except sqlite3.Error as e:
        print(f"Error: {e}")

    finally:
        connection.close()
        #memcache_close()

    return clones

def insert_clones(clones, database_path):
    conn = sqlite3.connect(database_path)

    cursor = conn.cursor()

    insert_query = '''
    INSERT INTO clones (
        reference_node_uuid,
        reference_char_start_index,
        reference_char_stop_index,
        reference_char_count,
        reference_start_line_number,
        reference_stop_line_number,
        reference_line_count,
        reference_file,
        candidate_node_uuid,
        candidate_char_start_index,
        candidate_char_stop_index,
        candidate_char_count,
        candidate_start_line_index,
        candidate_stop_line_index,
        candidate_line_count,
        candidate_file,
        reference_subtree_size,
        reference_subtree_depth,
        reference_subtree_string,
        candidate_subtree_size,
        candidate_subtree_depth,
        candidate_subtree_string,
        bleu_score,
        jaro_winkler_similarity
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
    '''

    for clone in clones:
        cursor.execute(insert_query, (
            clone["reference_node_uuid"],
            clone["reference_char_start_index"],
            clone["reference_char_stop_index"],
            clone["reference_char_count"],
            clone["reference_start_line_number"],
            clone["reference_stop_line_number"],
            clone["reference_line_count"],
            clone["reference_file"],
            clone["candidate_node_uuid"],
            clone["candidate_char_start_index"],
            clone["candidate_char_stop_index"],
            clone["candidate_char_count"],
            clone["candidate_start_line_index"],
            clone["candidate_stop_line_index"],
            clone["candidate_line_count"],
            clone["candidate_file"],
            clone["reference_subtree_size"],
            clone["reference_subtree_depth"],
            clone["reference_subtree_string"],
            clone["candidate_subtree_size"],
            clone["candidate_subtree_depth"],
            clone["candidate_subtree_string"],
            clone["bleu_score"],
            clone["jaro_winkler_similarity"]
        ))

    conn.commit()
    conn.close()

def create_clone_db(database_path):
    conn = sqlite3.connect(database_path)
    
    cursor = conn.cursor()
    
    create_table_query = '''
    CREATE TABLE IF NOT EXISTS clones (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        reference_node_uuid TEXT,
        reference_char_start_index INTEGER,
        reference_char_stop_index INTEGER,
        reference_char_count INTEGER,
        reference_start_line_number INTEGER,
        reference_stop_line_number INTEGER,
        reference_line_count INTEGER,
        reference_file TEXT,
        candidate_node_uuid TEXT,
        candidate_char_start_index INTEGER,
        candidate_char_stop_index INTEGER,
        candidate_char_count INTEGER,
        candidate_start_line_index INTEGER,
        candidate_stop_line_index INTEGER,
        candidate_line_count INTEGER,
        candidate_file TEXT,
        reference_subtree_size INTEGER,
        reference_subtree_depth INTEGER,
        reference_subtree_string TEXT,
        candidate_subtree_size INTEGER,
        candidate_subtree_depth INTEGER,
        candidate_subtree_string TEXT,
        bleu_score REAL,
        jaro_winkler_similarity REAL
    );
    '''

    cursor.execute(create_table_query)
    
    conn.commit()
    conn.close()


def walk_clones(input_database, output_database, min_bleu_score):
    limit = 1000
    print("pulling bleu score extrema...")
    max_bleu_score = 1.0

    step = 0.01
    print("Creating clone database...")
    create_clone_db(output_database)
    print("Walking clones...")

    forward_iteration = True

    if forward_iteration:
        start = int((max_bleu_score + step) * 100)
        end = int(min_bleu_score * 100) - 1
        step = -int(step * 100)
    else:
        start = int(min_bleu_score * 100)
        end = int((max_bleu_score + step) * 100)
        step = int(step * 100)

    for bleu_score in range(start, end, step):
        current_bleu_score = bleu_score / 100.0
        if current_bleu_score > 1.0:
            continue
        print("current bleu score: " + str(current_bleu_score))
        clone_count = get_clone_count(input_database, current_bleu_score, current_bleu_score)
        print("  clone count: " + str(clone_count))
        for start_index in range(0, clone_count, limit):
            if start_index == 0:
                offset = 0
            else:
                offset = min(start_index + limit, clone_count)
            clones = get_clones(input_database, current_bleu_score, current_bleu_score, limit, offset)
            insert_clones(clones, output_database)

script_path = os.path.abspath(__file__)
parent_directory = os.path.dirname(script_path)
fortranas_dir = os.path.dirname(parent_directory)


output_directory = fortranas_dir + "/output/clones"
clone_database = output_directory + "/clones.json"
output_database = output_directory + "/clones.sqlite3"
clone_file = output_directory + "/clones.json"
nodes_file = output_directory + "/nodes.json"
input_database = fortranas_dir + "/FortranAS.sqlite3"
create_directory(output_directory)
create_directory(output_directory + "/references")
create_directory(output_directory + "/candidates")


def get_config_value(config_file_path, config_key):
    with open(config_file_path, 'r') as file:
        for line in file:
            line = line.strip()
            if '=' in line:
                key, value = map(str.strip, line.split('='))
 
                if key == config_key:
                    return value

config_file_path =  fortranas_dir + '/FortranAS.conf'
minimum_bleu_score = get_config_value(config_file_path, "minimum_bleu_score")



walk_clones(input_database, output_database, float(minimum_bleu_score))
