import urllib2

req = None
response = None
while req is None or response is None:
	req = urllib2.Request('http://cc36119vm.eastus.cloudapp.azure.com/log')
	response = urllib2.urlopen(req)


	pagecontent = response.read()
# print pagecontent

# pagecontent = '<!DOCTYPE html><html><head><title>MSB Load Generator</title></head><body><a href='/log?name=test.1454698455280.log'>Test</a> running. You are testing through your browser, so it won't be reported to MSB.</body></html>'
test_number = pagecontent.split("test.")[-1].split(".")[0]
print test_number
# print pagecontent.split("<a href=")[1].split("=")[1].split('.')[1]

# for i in reversed(xrange(len(pageline))):
# 	# print pageline[len(pageline) - 2].split(-1)
# 	print pageline[len(pageline) - 2].split()[-1]
# 	if pageline[i].startswith("[Minute"):
# 		print pageline[i+1].split("=")[1]
# 		break