import sys
import md5
import re

if len(sys.argv) != 5:
  sys.exit('usage: genSerialVersionUID site-profile top-srcdir source target');

profile   = sys.argv[1]
topsrcdir = sys.argv[2]
spath     = sys.argv[3]
tpath     = sys.argv[4]

cname = spath[len(topsrcdir) + 11 : len(spath)]

print ('Profile = ' + profile);
print ('Class = ' + cname)

md5 = md5.new()
md5.update(profile)
md5.update(cname)
hex = md5.hexdigest()

print ('CheckSum = ' + hex)

uid = int('0x' + hex, 16) >> 64

print ('SerialVersionUID = ' + str(uid) + 'L')
print


prog = re.compile('private static final long serialVersionUID')

source = open(spath, 'rU')
target = open(tpath, 'w')
try:
  for line in source:
    if prog.search(line):
      target.write('  private static final long serialVersionUID = ' + str(uid) + 'L;\n')
      print 'match'
    else:
      target.write(line)
finally:
  source.close()
  target.close()




