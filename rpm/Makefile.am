
noinst_DATA = \
  RPMS/i686/pipeline-@DOMAIN@-@VERSION@-1.i686.rpm \
  pipeline-@DOMAIN@-@VERSION@-i686-rpm.bin


pipeline-@DOMAIN@-@VERSION@-i686-rpm.bin: \
  RPMS/i686/pipeline-@DOMAIN@-@VERSION@-1.i686.rpm header
	rm -f $@
	cat header RPMS/i686/pipeline-@DOMAIN@-@VERSION@-1.i686.rpm > $@
	chmod 555 $@

header: RPMS/i686/pipeline-@DOMAIN@-@VERSION@-1.i686.rpm install-header
	(cd RPMS/i686; checksum=`md5sum pipeline-@DOMAIN@-@VERSION@-1.i686.rpm`; cd ../../; \
         sed -e "s/@CHECKSUM@/$$checksum/g" install-header > header)

RPMS/i686/pipeline-@DOMAIN@-@VERSION@-1.i686.rpm: SPECS/pipeline-@DOMAIN@-@VERSION@.spec
	rpmbuild --rcfile rpmrc -bb SPECS/pipeline-@DOMAIN@-@VERSION@.spec

SPECS/pipeline-@DOMAIN@-@VERSION@.spec: rpm-prep pipeline.spec
	cp -f pipeline.spec $@

rpm-prep: rpm-mkdirs rpm-install

rpm-mkdirs:
	mkdir -p BUILD RPMS SPECS

rpm-install:
	DESTDIR=`pwd`/BUILD $(MAKE) -C ../src install
	DESTDIR=`pwd`/BUILD $(MAKE) -C ../data install
	DESTDIR=`pwd`/BUILD $(MAKE) -C ../docs install

clean:
	rm -rf BUILD RPMS SPECS

