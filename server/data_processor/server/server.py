from flask import Flask
from flask import Response
import os

app = Flask(__name__)

def list_files(directory, extension):
    return (f for f in os.listdir(directory) if f.endswith('.' + extension))


@app.route("/api/file/<path>", methods=['GET'])
def download_file (path = None):
    if path is None:
        return Response(status = 400)
    try:
        print(path)
        print(os.getcwd())
        return open(os.getcwd() + "/server/" + path, "rb").read()
    except Exception as e:
        print(e)
        return Response(status = 400)

if __name__ == '__main__':
   app.run()