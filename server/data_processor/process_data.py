#coding=utf-8
#import sys
from helper import *
from optparse import OptionParser
from deepnn import *
from xml_to_csv import *
from generate_tfrecords import *

default_video_lenghts = 10

# A script futtatásához szükséges paraméterek beolvasása 
parser = OptionParser() 

parser.add_option("-s", "--source", dest="source", 
                  help="folder to process") 
parser.add_option("-d", "--destination", dest="destination", 
                  help="destination folder") 
parser.add_option("-v", "--verbose", dest="verbose", 
                  help="Verbose to show (0=false,1=true)") 

(options, args) = parser.parse_args() 

verbose = int_to_bool(options.__getattribute__('verbose')) 
source_folder = options.__getattribute__('source') 
destination_folder = options.__getattribute__('destination') 

if verbose == True: 
    print("Source folder: " + str(source_folder)) 
if verbose == True: 
    print("Destination folder: " + str(destination_folder)) 

# Cél máppák inicializálása 
cutted_video_output_path = destination_folder + "/cutted_video_output" 
cutted_stabilized_video_output_path = destination_folder + "/cutted_stabilized_video_output" 
frames_output_path = destination_folder + "/frames" 

# Cél mappa létrehozása 
mkdir(destination_folder) 
# Vágott videók mappájának létrehozása  
mkdir(cutted_video_output_path) 
# Vágott és stabilizált videók mappájának létrehozása 
mkdir(cutted_stabilized_video_output_path) 
# Képkockák mappájának létrehozása  
mkdir(frames_output_path) 

# Videók felvágása megadott hosszúra 
cut_videos_to_parts(source_folder, cutted_video_output_path,'mp4', default_video_lenghts) 
# Feldarabolt videók stabilizálása 
stabilize_videos(cutted_video_output_path, cutted_stabilized_video_output_path) 
# Feldarabolt és stabilizált videók képkockává való feldarabolása 
videos_to_frames(cutted_video_output_path, frames_output_path, "mp4") 
# Képen lévő objektumokat tartalmzazó xml előállítása mély neurálsi hálózat segítségével 
generate_xml_files_with_nn(frames_output_path) 
# XML CSV-re való konvertálása, amely tartalmazza az összes képet leíró XML-t 
generate_csv_from_xml(frames_output_path)
# Tanításra alkalmas TFRecords elkészítése 
generate_tf_records(frames_output_path + "/labels.csv", frames_output_path + "/train.record") 