
noinst_DATA = \
  pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.bin


pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.bin: header
	@echo "Building Shell Archive: " $@
	@rm -f $@
	@cat header pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.tgz > $@
	@chmod 555 $@

header: pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.tgz \
  headerA headerB $(top_srcdir)/docs/legal/@LICENSE_DOC@.txt 
	@echo "Building Install Header..."
	@rm -f $@
	@(checksum=`md5sum pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.tgz`; \
          alen=`wc -l $(srcdir)/headerA | awk '{print($$1)}'`; \
          blen=`wc -l headerB | awk '{print($$1)}'`; \
          llen=`wc -l $(top_srcdir)/docs/legal/@LICENSE_DOC@.txt | awk '{print($$1)}'`; \
          hlen=$$(($$alen + $$blen + $$llen + 1)); \
          cat $(srcdir)/headerA > header; \
          cat $(top_srcdir)/docs/legal/@LICENSE_DOC@.txt >> header; \
          sed -e "s/@CHECKSUM@/$$checksum/g" headerB | \
          sed -e "s/@HEADER_LENGTH@/$$hlen/g" >> header)



pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.tgz: pipeline-install \
  RPMS/i686/plmaster-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
  RPMS/i686/plqueuemgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
  RPMS/i686/pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
  SRPMS/plnotify-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.src.rpm
	rm -rf pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@
	mkdir pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@
	cp RPMS/i686/plmaster-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
           RPMS/i686/plqueuemgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
           RPMS/i686/pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
           SRPMS/plnotify-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.src.rpm \
           pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@
	tar -zcvf $@ pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@
	rm -rf pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@

pipeline-install: rpm-mkdirs
	rm -rf DEST/pipeline DEST/plmaster DEST/plqueuemgr
	DESTDIR=`pwd`/DEST/pipeline $(MAKE) -C ../src install
	DESTDIR=`pwd`/DEST/pipeline $(MAKE) -C ../docs install
	mkdir -p DEST/plmaster DEST/plqueuemgr/usr/share/pipeline
	mv DEST/pipeline/usr DEST/plmaster
	DESTDIR=`pwd`/SOURCES $(MAKE) -C ../src/standalone install

rpm-mkdirs:
	mkdir -p BUILD DEST RPMS SPECS SOURCES SRPMS 



RPMS/i686/pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm: \
  SPECS/pipeline-@CUSTOMER@-@VERSION@.spec
	rpmbuild --rcfile rpmrc -bb SPECS/pipeline-@CUSTOMER@-@VERSION@.spec

SPECS/pipeline-@CUSTOMER@-@VERSION@.spec: pipeline.spec
	cp -f pipeline.spec $@



RPMS/i686/plqueuemgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm: \
  SPECS/plqueuemgr-@CUSTOMER@-@VERSION@.spec
	rpmbuild --rcfile rpmrc -bb SPECS/plqueuemgr-@CUSTOMER@-@VERSION@.spec

SPECS/plqueuemgr-@CUSTOMER@-@VERSION@.spec: plqueuemgr.spec
	cp -f plqueuemgr.spec $@



RPMS/i686/plmaster-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm: \
  SPECS/plmaster-@CUSTOMER@-@VERSION@.spec
	rpmbuild --rcfile rpmrc -bb SPECS/plmaster-@CUSTOMER@-@VERSION@.spec

SPECS/plmaster-@CUSTOMER@-@VERSION@.spec: plmaster.spec
	cp -f plmaster.spec $@



SRPMS/plnotify-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.src.rpm: \
  SPECS/plnotify-@CUSTOMER@-@VERSION@.spec \
  SOURCES/plnotify-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.tgz
	rpmbuild --rcfile rpmrc -ba --clean SPECS/plnotify-@CUSTOMER@-@VERSION@.spec

SPECS/plnotify-@CUSTOMER@-@VERSION@.spec: plnotify.spec
	cp -f plnotify.spec $@



install: all
	mkdir -p ../../../../releases/pipeline
	$(INSTALL_DATA) pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.bin \
          ../../../../releases/pipeline



clean: clean-rpms
	rm -rf BUILD DEST RPMS SPECS SOURCES SRPMS
	rm -f header pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.bin
	$(MAKE) -C ../ clean

clean-rpms: 
	rm -f RPMS/i686/pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm
	rm -f RPMS/i686/plqueuemgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm
	rm -f RPMS/i686/plmaster-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm
	rm -f SRPMS/plnotify-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.src.rpm

