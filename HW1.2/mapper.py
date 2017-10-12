#!/usr/bin/env python

import sys

import os
saveList = []
# filename = os.environ['map_input_file']
filename = os.environ["mapreduce_map_input_file"]
# filename = 'pagecounts-20151201-090000'
date = filename.split("-")[-2]

def processLine():
	wordList = ['Media:', 'Special:', 'Talk:', 'User:', 'User_talk:', 'Project:', 'Project_talk:', 'File:','File_talk:'
	,'MediaWiki:','MediaWiki_talk:','Template:','Template_talk:','Help:','Help_talk:','Category:','Category_talk:','Portal:','Wikipedia:',
	'Wikipedia_talk:']
	removalList = ['404_error/', 'Main_Page', 'Hypertext_Transfer_Protocol','Search']
	extensionList = ['.jpg', '.gif', '.png', '.JPG', '.GIF', '.PNG', '.txt', '.ico']

	# with open(filename, "r") as f:
	for line in sys.stdin:
		temp = line.split()
		#those with 4 columns and those whose project name start with "en"
		if len(temp) == 4 and temp[0] == "en":
		#first letter of title is uppercase letter or non-letter
			if temp[1][0].isupper() or not temp[1][0].isalpha():
			#last four letters of title are not in extentionList, and title is not the same as removalList
				if temp[1][-4:] not in extensionList and temp[1] not in removalList:
					#title does not start with word in wordList
					i = 0
					for k in wordList: 
						if not temp[1].startswith(k):
							i = i + 1
						else:
							break
						if i == 20:
							print temp[1] + '\t' + date + temp[2]
	

processLine()