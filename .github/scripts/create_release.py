import os
import requests
from requests.adapters import HTTPAdapter
from requests.packages.urllib3.util.retry import Retry

github_token = os.environ.get("GITHUB_TOKEN")
repo_owner = "akoerner"
repo_name = "FortranAS"
release_tag = "v1.0"
release_name = "Release v1.0"
release_body = "Release notes for version 1.0"
artifact_path = "build/FortranAS-1.0.tar.gz"
artifact_path = "build/FortranAS-1.0.zip"

url = f"https://api.github.com/repos/{repo_owner}/{repo_name}/releases"
headers = {
    "Accept": "application/vnd.github.v3+json",
    "Authorization": f"Bearer {github_token}",
}
data = {
    "tag_name": release_tag,
    "name": release_name,
    "body": release_body,
}

response = requests.post(url, headers=headers, json=data)

if response.status_code == 201:
    release_id = response.json()["id"]

    upload_url = f"https://uploads.github.com/repos/{repo_owner}/{repo_name}/releases/{release_id}/assets"
    headers = {
        "Accept": "application/vnd.github.v3+json",
        "Authorization": f"Bearer {github_token}",
    }
    files = [("file", open(artifact_path, "rb"))]

    retries = Retry(total=5, backoff_factor=0.1, status_forcelist=[500, 502, 503, 504])
    session = requests.Session()
    session.mount('https://', HTTPAdapter(max_retries=retries))

    try:
        upload_response = session.post(upload_url, headers=headers, files=files)
        upload_response.raise_for_status()

        if upload_response.status_code == 201:
            print(f"Artifact {artifact_path} uploaded successfully!")
        else:
            print(f"Failed to upload artifact. Status code: {upload_response.status_code}, {upload_response.text}")

    except requests.exceptions.RequestException as e:
        print(f"Error uploading artifact: {e}")

else:
    print(f"Failed to create release. Status code: {response.status_code}, {response.text}")

