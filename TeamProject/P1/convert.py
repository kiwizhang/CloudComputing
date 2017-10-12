import sys
fi = open( "part-00001", "r" )

for line in fi:
    sys.stdout.write(line[:-2] + '\n')