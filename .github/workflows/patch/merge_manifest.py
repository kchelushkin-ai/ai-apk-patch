#!/usr/bin/env python3 import sys, xml.etree.ElementTree as ET mf, pkg, label = sys.argv[1], sys.argv[2], sys.argv[3] ET.register_namespace(‘android’,“http://schemas.android.com/apk/res/android”) ns={‘android’:“http://schemas.android.com/apk/res/android”} tree=ET.parse(mf); root=tree.getroot()
INTERNET permission
has_net = any(e.get(‘{http://schemas.android.com/apk/res/android}name’)==‘android.permission.INTERNET’ for e in root.findall(‘uses-permission’))
if not has_net:
ET.SubElement(root,‘uses-permission’,{‘{http://schemas.android.com/apk/res/android}name’:‘android.permission.INTERNET’})

Activity
app = root.find(‘application’)
act = ET.SubElement(app,‘activity’,{‘{http://schemas.android.com/apk/res/android}name’:f’{pkg}.ai.AiActivity’,‘{http://schemas.android.com/apk/res/android}exported’:‘true’,‘{http://schemas.android.com/apk/res/android}label’:label})
intent = ET.SubElement(act,‘intent-filter’)
ET.SubElement(intent,‘action’,{‘{http://schemas.android.com/apk/res/android}name’:‘android.intent.action.MAIN’})
ET.SubElement(intent,‘category’,{‘{http://schemas.android.com/apk/res/android}name’:‘android.intent.category.LAUNCHER’})
tree.write(mf,encoding=‘utf-8’,xml_declaration=True)
