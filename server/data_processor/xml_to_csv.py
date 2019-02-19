import os
import glob
import pandas as pd
import xml.etree.ElementTree as ET
from helper import *

def xml_to_csv(path):
    """
    XML-ből a tanító CSV előállítása.
    """

    xml_list = []
    print(path)
    for xml_file in list_files(path , "xml"):
        file = path + "/"+ xml_file
        print(file)
        tree = ET.parse(file)
        root = tree.getroot()
        for member in root.findall('object'):
            value = (path + "/" + root.find('filename').text,
                     int(root.find('size')[0].text),
                     int(root.find('size')[1].text), 
                     member[0].text,
                     int(member[1][0].text),
                     int(member[1][1].text),
                     int(member[1][2].text),
                     int(member[1][3].text)
                     )
            xml_list.append(value)
    column_name = ['filename', 'width', 'height', 'class', 'xmin', 'ymin', 'xmax', 'ymax']
    xml_df = pd.DataFrame(xml_list, columns=column_name)
    return xml_df

def generate_csv_from_xml(xml_path):
    output_path= xml_path + "/" + 'labels.csv'
    print(output_path)
    xml_df = xml_to_csv(xml_path)
    xml_df.to_csv(output_path ,index=None)
    print('Successfully converted xml to csv.')