dc_status = False
lg_dnsname = 'cc51819vm'
dc_dnsname = 'cc538318vm'
if dc_status == False:
        test_url = 'http://cc51819vm.eastus.cloudapp.azure.com/test/horizontal?dns=cc538318vm.eastus.cloudapp.azure.com'
else:
    test_url = 'http://cc51819vm/test/horizontal/add?dns=cc538318vm'
dc_status = True

response_test = None
while response_test is None:
    
    try:
        req_test = urllib2.Request(test_url)
        response_test = urllib2.urlopen(req_test)
    except:
        pass

testpage = response_test.read()
test_number = testpage.split("<a href=")[1].split("=")[1].split('.')[1]
# try:
#     req = urllib2.Request(test_url)
#     response = urllib2.urlopen(req)
# except:
#     time.sleep(5)
#     try:
#         req = urllib2.Request(test_url)
#         response = urllib2.urlopen(req)
#     except:
#         time.sleep(5)
#         try:
#             req = urllib2.Request(test_url)
#             response = urllib2.urlopen(req)
#         except:
#             time.sleep(5)
            


# response = urllib2.urlopen(req)



log_url = 'http://' + lg_dnsname + '/log?name=test.' + test_number + '.log'
print log_url