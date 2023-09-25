import os
import networkx as nx
import networkx as nx
from pyvis.network import Network

def dot_to_interactive_html(dot_file_path):
    try:
        graph = nx.drawing.nx_agraph.read_dot(dot_file_path)
        nt = Network(notebook=False)
        nt.from_nx(graph)
        html_file_path = os.path.splitext(dot_file_path)[0] + ".html"
        nt.show(html_file_path)
    except Exception as e:
        print(f"Error: {e}")

def convert_dot_files_to_html(root_dir):
    for root, dirs, files in os.walk(root_dir):
        for file in files:
            if file.endswith(".dot"):
                dot_file_path = os.path.join(root, file)
                dot_to_interactive_html(dot_file_path)

if __name__ == '__main__':
    root_directory =  root_directory = os.getcwd()
    convert_dot_files_to_html(root_directory)
