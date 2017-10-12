#the most popular article in the filtered output
from processLine import processLine
import re

def cloud_computing():
    count = 0
    with open("output", "r") as f:       
    #search line by line for the ones containing "cloud" and "computing", and add the up
		for line in f:
			if re.search('cloud', line, re.IGNORECASE) and re.search('computing', line, re.IGNORECASE):
				count = count + 1
		print count
cloud_computing()