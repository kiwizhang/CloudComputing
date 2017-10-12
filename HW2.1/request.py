import requests

r = requests.get('http://cc1466644vm.eastus.cloudapp.azure.com/test/horizontal/add?dns=cc4277232vm.eastus.cloudapp.azure.com')

print r.text