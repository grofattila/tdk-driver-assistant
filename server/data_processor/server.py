from flask import Flask
from flask import Response
import os

app = Flask(__name__)


@app.route("/api/file/<path>", methods=['GET'])
def download_file (path = None):
    if path is None:
        return Response(status = 400)
    try:
        print(os.curdir + "/" +path)
        return open(os.curdir + "/" +path).read()
    except Exception as e:
        return Response(status = 400)

if __name__ == '__main__':
   app.run()