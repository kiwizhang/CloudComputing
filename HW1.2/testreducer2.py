#!/usr/bin/env python2

from operator import itemgetter
import sys


current_word = None
current_count = 0
word = None
itemmap = {'word':'0','20151201':'0', '20151202':'0', '20151203':'0', '20151204':'0', '20151205':'0', '20151206':'0', '20151207':'0', '20151208':'0', '20151209':'0', '20151210':'0','20151211':'0', '20151212':'0', '20151213':'0',
'20151214':'0','20151215':'0', '20151216':'0', '20151217':'0','20151218':'0','20151219':'0', '20151220':'0', '20151221':'0', '20151222':'0','20151223':'0', '20151224':'0', 
'20151225':'0', '20151226':'0','20151227':'0', '20151228':'0','20151229':'0','20151230':'0', '20151231':'0'}
wordmap = itemmap.copy();

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
				
				wordmap[current_date] = current_count
						# item = current_date + "\t" + str(current_count)
						# print item,
					
				if current_word[:-8] !=  word:
					# print '\n',
					# sortedmap = sorted(wordmap.iteritems())			
					
					print wordmap['word']
					del wordmap['word']
					sortedmap = sorted(wordmap.iteritems())
					for key,value in sortedmap:
						print key + "\t" + str(value),
					wordmap = itemmap.copy()
					wordmap['word'] = word

				#write reuslt to STDOUT
			else: 
				wordmap['word'] = word
				
			current_count = count
			current_word = word + date
			current_date = date

