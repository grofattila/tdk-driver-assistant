#coding=utf-8
import os
import cv2
import numpy as np
from vidstab import VidStab
from moviepy.editor import *
from moviepy.video.io.ffmpeg_tools import ffmpeg_extract_subclip


"""
Megadott mappában a paraméterben átvett kiterjesztésű fájlokat adja vissza.
"""
def list_files(directory, extension):
    return (f for f in os.listdir(directory) if f.endswith('.' + extension))

"""
Megadott mappában a praméterben lévő fájl típusok számát adja meg.
"""
def num_of_files_in_dir(directory, extension):
    return len([f for f in os.listdir(directory) if f.endswith('.' + extension)])



"""
Bool konvertálás int-ről.
"""
def int_to_bool(value):
    if value == '0':
        return False
    return True

"""
Új mappa létrehozása.
"""
def mkdir(new_dir_path):
    try:
        os.makedirs(new_dir_path)
    except OSError:
        print("Creation of the directory %s failed" % new_dir_path)
    else:
        print("Successfully created the directory %s" % new_dir_path)
    return True

"""
Videót vág fel megadott hoszzúságúra.
"""
def cut_video(start, end, source, target):
    ffmpeg_extract_subclip(source, start, end, target)

"""
Egy mappában lévő videókat megadott hosszuságúra vág fel.
"""
def cut_videos_to_parts(source_folder, cutted_video_output_path, type, default_video_lenghts):
    for video_name in list_files(source_folder, type):
        video_to_be_cutted = source_folder + '/' + video_name
        print("Video to be cutted: " + video_name)

        cut_video_to_parts(video_to_be_cutted,
                           cutted_video_output_path, default_video_lenghts)


"""
Egy megadott videót adott hosszúságú szeletkre vág fel és ment el a megadott elérési útra.
"""
def cut_video_to_parts(source, destination_folder, length):
    print("cut_video_to_parts")

    video = VideoFileClip(source)
    counter = 0
    duration = int(video.duration)

    print(duration)

    for i in range(0, duration - length, length):
        cut_video(i, i + length, source, destination_folder + '/' +
                  os.path.basename(source).split('.')[-2] + str(counter) + ".mp4")
        counter += 1

"""
Adott mappa összes videóját stabilizálja és a megadott helyre menti.
"""
def stabilize_videos(source, destination):
    for file in list_files(source, 'mp4'):
        stabilize_video(source, file, destination)

"""
Egy adott videó stabilizál és menti a megadott elérési útra.
"""
def stabilize_video(source, video, destination_folder):
    input_video_path = source + '/' + video
    output_file_path = destination_folder + "/" + \
        os.path.basename(video).split('.')[-2] + "_stabilized.mp4"

    print("Video to be stabilized:" + input_video_path)

    stabilizer=VidStab(kp_method='FAST')
    stabilizer.stabilize(input_path=input_video_path,
                         output_path=output_file_path,
                         border_type='black')

    print("Output file is ready: " + output_file_path)

def videos_to_frames(input_path, output_path, video_extension):
    for video in list_files(input_path, video_extension):
        video_to_frames(video, input_path, output_path, video_extension)


def video_to_frames(file, input_path, output_path, video_extension):
    print("file: " + file)
    print("input path" + input_path)
    print("output path" + output_path)


    video = input_path + "/" + file
    print("video path" + video)
    video = cv2.VideoCapture(video)

    success, image = video.read()
    count = 0

    while success:
        new_file_name = output_path  +"/" +file.split(".")[0] +"_frame%d.jpg" % count
        cv2.imwrite(new_file_name, image)
        success, image = video.read()
        count += 1
    
    print("Nr. of pictures generated ", count)

def load_image_into_numpy_array(image):
    (im_width, im_height) = image.size
    return np.array(image.getdata()).reshape((im_height, im_width, 3)).astype(np.uint8)