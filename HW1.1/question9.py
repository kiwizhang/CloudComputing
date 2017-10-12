#Compare the number of views of films in 2014 and 2015

from processLine import processLine
def compareYear():
    film2015 = 0
    film2014 = 0
    with open("output", "r") as f:
    #store the list of entries for each year
        for line in f:
            if "2014_film" in line[0]:
                film2014 = film2014 + 1
            if "2015_film" in line[0]:
                film2015 = film2015 + 1
    if film2014 > film2015:
        print "2014"
    else: 
        print "2015"
compareYear()
