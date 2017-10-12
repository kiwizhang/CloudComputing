#!/usr/bin/env python2

from operator import itemgetter
import sys
dateList = ['20151201', '20151202,' '20151203', '20151204', '20151205', '20151206', '20151207', '20151208', '20151209', '20151210','20151211', '20151212', '20151213',
'20151214','20151215', '20151216', '20151217','20151218','20151219', '20151220', '20151221', '20151222','20151223', '20151224', 
'20151225', '20151226','20151227', '20151228','20151229','20151230', '20151231']

current_word = None
current_count = 0
word = None

with open('testtext', "r") as f:

	for line in f:
		#remove leading and trailing white space
		line = line.strip()

		#parse the input
		word, date, count = line.split('\t')

		#convert count into int
		try: 
			count = int(count)
			len(date) == 8
		except ValueError:
			#if count is not a number, continue
			continue

		if current_word == word + date:
			current_count += count
		else:
			if current_word:
				#change word and date
				if (current_word[:-8] !=  word):
					print '\n'				
					print word,
				#write reuslt to STDOUT
				item = date + "\t" + str(current_count)
				print item,
			current_count = count
			current_word = word + date
			current_date = date

