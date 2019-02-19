import numpy as np
import os
import six.moves.urllib as urllib
import sys
import tarfile
import tensorflow as tf
import zipfile
from collections import defaultdict
from io import StringIO
from matplotlib import pyplot as plt
from PIL import Image
import xml.etree.cElementTree as et
from xml.dom import minidom
sys.path.append("..")
import label_map_util
from helper import load_image_into_numpy_array
import helper
import cv2
from PIL import Image
import timeit


def generate_xml_files_with_nn(test_images_path):
    """
    Bemeneti képekre futtat egy dtektálást majd ezeket egy XML fájlba menti ki.
    A modellt amivel a detektálást hajtja végra azt ki lehet cserélni nagyobb modellre.
    """
    # What model to download.
    MODEL_NAME = 'ssd_mobilenet_v1_coco_2017_11_17'
    MODEL_FILE = MODEL_NAME + '.tar.gz'
    DOWNLOAD_BASE = 'http://download.tensorflow.org/models/object_detection/'

    # Path to frozen detection graph. This is the actual model that is used for the object detection.
    PATH_TO_CKPT = MODEL_NAME + '/frozen_inference_graph.pb'

    # List of the strings that is used to add correct label for each box.
    PATH_TO_LABELS = os.path.join('data', 'mscoco_label_map.pbtxt')

    NUM_CLASSES = 90

    if not os.path.isdir("./ssd_mobilenet_v1_coco_2017_11_17"):
        opener = urllib.request.URLopener()
        opener.retrieve(DOWNLOAD_BASE + MODEL_FILE, MODEL_FILE)
        tar_file = tarfile.open(MODEL_FILE)
        for file in tar_file.getmembers():
            file_name = os.path.basename(file.name)
            if 'frozen_inference_graph.pb' in file_name:
                tar_file.extract(file, os.getcwd())

    detection_graph = tf.Graph()
    with detection_graph.as_default():
        od_graph_def = tf.GraphDef()
        with tf.gfile.GFile(PATH_TO_CKPT, 'rb') as fid:
            serialized_graph = fid.read()
            od_graph_def.ParseFromString(serialized_graph)
            tf.import_graph_def(od_graph_def, name='')

    label_map = label_map_util.load_labelmap(PATH_TO_LABELS)
    categories = label_map_util.convert_label_map_to_categories(
        label_map, max_num_classes=NUM_CLASSES, use_display_name=True)
    category_index = label_map_util.create_category_index(categories)

    # Size, in inches, of the output images.
    IMAGE_SIZE = (24, 16)

    i = 0
    size = helper.num_of_files_in_dir(test_images_path, "jpg")
    for image_path in helper.list_files(test_images_path, "jpg"):

        with detection_graph.as_default():
            with tf.Session() as sess:

                print("Status-------------> " + str(i) + "/" + str(size))
                i = i+1
                full_image_path = test_images_path + "/" + image_path
                print("Opening -> ", full_image_path)
                start = timeit.default_timer()
                image = Image.open(full_image_path)
                image_np = load_image_into_numpy_array(image)
                image_np_expanded = np.expand_dims(image_np, axis=0)
                all_detection_dict = run_inference_for_single_image(
                    sess, image_np, detection_graph)
                write_objects_detected_to_xml(get_valid_detections(
                    all_detection_dict), category_index, full_image_path)
                stop = timeit.default_timer()
                print('Detection and xml creation took: ' +
                      str(stop - start) + " sec")

def run_inference_for_single_image(session, image, graph):
    # Get handles to input and output tensors
    ops = tf.get_default_graph().get_operations()
    all_tensor_names = {
        output.name for op in ops for output in op.outputs}
    tensor_dict = {}
    for key in [
        'num_detections', 'detection_boxes', 'detection_scores',
        'detection_classes', 'detection_masks'
    ]:
        tensor_name = key + ':0'
        if tensor_name in all_tensor_names:
            tensor_dict[key] = tf.get_default_graph().get_tensor_by_name(
                tensor_name)
    if 'detection_masks' in tensor_dict:
        # The following processing is only for single image
        detection_boxes = tf.squeeze(
            tensor_dict['detection_boxes'], [0])
        detection_masks = tf.squeeze(
            tensor_dict['detection_masks'], [0])
        # Reframe is required to translate mask from box coordinates to image coordinates and fit the image size.
        real_num_detection = tf.cast(
            tensor_dict['num_detections'][0], tf.int32)
        detection_boxes = tf.slice(detection_boxes, [0, 0], [
            real_num_detection, -1])
        detection_masks = tf.slice(detection_masks, [0, 0, 0], [
            real_num_detection, -1, -1])
        detection_masks_reframed = utils_ops.reframe_box_masks_to_image_masks(
            detection_masks, detection_boxes, image.shape[0], image.shape[1])
        detection_masks_reframed = tf.cast(
            tf.greater(detection_masks_reframed, 0.5), tf.uint8)
        # Follow the convention by adding back the batch dimension
        tensor_dict['detection_masks'] = tf.expand_dims(
            detection_masks_reframed, 0)
    image_tensor = tf.get_default_graph().get_tensor_by_name('image_tensor:0')

    # Run inference
    output_dict = session.run(tensor_dict, feed_dict={
                        image_tensor: np.expand_dims(image, 0)})

    # all outputs are float32 numpy arrays, so convert types as appropriate
    output_dict['num_detections'] = int(
        output_dict['num_detections'][0])
    output_dict['detection_classes'] = output_dict[
        'detection_classes'][0].astype(np.uint8)
    output_dict['detection_boxes'] = output_dict['detection_boxes'][0]
    output_dict['detection_scores'] = output_dict['detection_scores'][0]
    if 'detection_masks' in output_dict:
        output_dict['detection_masks'] = output_dict['detection_masks'][0]
    return output_dict

def get_valid_detections(all_detetions):
    print("get_valid_detections")

    threshold = 0.5

    valid_detections = {}
    valid_detections['detection_boxes'] = []
    valid_detections['detection_classes'] = []
    valid_detections['detection_scores'] = []

    for i in range(all_detetions['detection_scores'].size):
        #  3 - car, 6 - bus, 10 - traffic light
        if all_detetions['detection_scores'][i] > threshold and (all_detetions['detection_classes'][i] == 3 or all_detetions['detection_classes'][i] == 6 or all_detetions['detection_classes'][i] == 10):
            print(
                str(all_detetions['detection_classes'][i]) + ", ", end='')
            print()
            valid_detections['detection_scores'].append(
                all_detetions['detection_scores'][i])
            valid_detections['detection_boxes'].append(
                all_detetions['detection_boxes'][i][:])
            valid_detections['detection_classes'].append(
                all_detetions['detection_classes'][i])
    return valid_detections

def write_objects_detected_to_xml(detection_dict, labels, image):
    print("write_objects_detected_to_xml")

    image_file = image.split("/")[-1]
    print("Image file : ", image_file)

    image_name = image_file.split('.')[0].split(".")[-1]
    print("Image file name: ", image_file)

    path_split = image.split('/')
    folder_name = "/" + "/".join(path_split[1:len(path_split)-1])
    print("Image containing folder: ", folder_name)

    img = Image.open(image)

    annotation = et.Element("annotation")
    folder = et.SubElement(annotation, "folder")
    filename = et.SubElement(annotation, "filename").text = image_file
    path = et.SubElement(annotation, "path").text = image
    size = et.SubElement(annotation, "size")

    et.SubElement(size, "width").text = str(img.width)
    et.SubElement(size, "height").text = str(img.height)

    for i in range(0, len(detection_dict['detection_scores'])):
        objekt = et.SubElement(annotation, "object")

        detected_clazz = detection_dict['detection_classes'][i]
        detected_object = labels[detected_clazz]
        et.SubElement(objekt, "name").text = detected_object['name']

        detection_boxes = detection_dict['detection_boxes']
        detection_box = detection_boxes[i]

        bndbox = et.SubElement(objekt, "bndbox")
        et.SubElement(bndbox, "xmin").text = str(
            (int)(detection_box[1] * img.width))
        et.SubElement(bndbox, "ymin").text = str(
            (int)(detection_box[0] * img.height))
        et.SubElement(bndbox, "xmax").text = str(
            (int)(detection_box[3] * img.width))
        et.SubElement(bndbox, "ymax").text = str(
            (int)(detection_box[2] * img.height))

    tree = et.ElementTree(annotation)
    xmlstr = minidom.parseString(et.tostring(
        annotation)).toprettyxml(indent="   ")

    with open(folder_name + "/" + image_name + ".xml", "w") as f:
        f.write(xmlstr)


