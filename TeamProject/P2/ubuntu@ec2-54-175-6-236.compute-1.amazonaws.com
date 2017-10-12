
import sys

for line in sys.stdin:
    temp = line.split(",")
    temp2 = temp[0].split("&")
    print temp2[1] + "&" + temp2[0] + temp[1],