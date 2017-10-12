count = 0
wordmap = {"b": "2", "a": "1", "c":"10"}
for i in wordmap:
	count = count + int(wordmap[i])
print count

sortedmap = sorted(wordmap.iteritems())
for key,value in sortedmap:
	print key + " " + str(value),
