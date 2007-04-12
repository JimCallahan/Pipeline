
if FOUNDATION 

noinst_DATA = \
  pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.bin


if HAS_MAC

if HAS_WINDOWS
    RPMS = \
      RPMS/i686/pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plmaster-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plqueuemgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plmac-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plwin-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm
else 
    RPMS = \
      RPMS/i686/pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plmaster-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plqueuemgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plmac-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm
endif 

else

if HAS_WINDOWS 
  RPMS = \
    RPMS/i686/pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
    RPMS/i686/plmaster-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
    RPMS/i686/plqueuemgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
    RPMS/i686/plwin-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm
else 
  RPMS = \
    RPMS/i686/pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
    RPMS/i686/plmaster-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
    RPMS/i686/plqueuemgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm
endif

endif


MAC_SRC_DIR   = $(patsubst $(HOME)/%,/Users/$(USER)/%,$(top_srcdir))
MAC_BUILD_DIR = $(patsubst $(HOME)/%,/Users/$(USER)/%,$(shell pwd))

WIN_SRC_DIR   = $(patsubst $(HOME)/code-$(CUSTOMER)/%,/home/$(USER)/code/%,$(top_srcdir))
WIN_BUILD_DIR = $(patsubst $(HOME)/code-$(CUSTOMER)/%,/home/$(USER)/code/%,$(shell pwd))



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



pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.tgz: pipeline-install $(RPMS) 
	rm -rf pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@
	mkdir pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@
	cp $(RPMS) pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@
	tar -zcvf $@ pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@
	rm -rf pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@

pipeline-install: clean-rpms rpm-mkdirs
	rm -rf DEST/pipeline DEST/plmaster DEST/plqueuemgr
	DESTDIR=`pwd`/DEST/pipeline $(MAKE) -C .. install
	mkdir -p DEST/plmaster/@NODEDIR@
	mv DEST/pipeline/@NODEDIR@/* DEST/plmaster/@NODEDIR@
	mkdir -p DEST/plqueuemgr/@QUEUEDIR@


core-rpms: pipeline-install \
  RPMS/i686/pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
  RPMS/i686/plmaster-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
  RPMS/i686/plqueuemgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm

RPMS/i686/plmaster-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm: \
  SPECS/plmaster-@CUSTOMER@-@VERSION@.spec
	rpmbuild --rcfile rpmrc -bb SPECS/plmaster-@CUSTOMER@-@VERSION@.spec

SPECS/plmaster-@CUSTOMER@-@VERSION@.spec: plmaster.spec
	cp -f plmaster.spec $@


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


if HAS_MAC

mac-rpm: clean-mac-rpm RPMS/i686/plmac-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm

RPMS/i686/plmac-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm: \
  SPECS/plmac-@CUSTOMER@-@VERSION@.spec
	@echo "-----------------------------------------------------------------------------"
	@echo "  UPDATING: tadpole"
	@echo "-----------------------------------------------------------------------------"
	rm -rf DEST/plmac
	rsync -av --exclude-from=$(top_srcdir)/config/excluded --delete \
          $(top_srcdir)/ tadpole:$(MAC_SRC_DIR)
	@echo "-----------------------------------------------------------------------------"
	@echo "  BUILDING: tadpole"
	@echo "-----------------------------------------------------------------------------"
	ssh tadpole "source .bash_profile; $(MAKE) -C $(MAC_BUILD_DIR) all"
	rsync -av tadpole:$(MAC_BUILD_DIR)/DEST/plmac DEST
	rpmbuild --rcfile rpmrc -bb SPECS/plmac-@CUSTOMER@-@VERSION@.spec

SPECS/plmac-@CUSTOMER@-@VERSION@.spec: plmac.spec
	cp -f plmac.spec $@

endif


if HAS_WINDOWS

win-rpm: clean-win-rpm RPMS/i686/plwin-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm

RPMS/i686/plwin-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm: \
  SPECS/plwin-@CUSTOMER@-@VERSION@.spec
	@echo "-----------------------------------------------------------------------------"
	@echo "  UPDATING: lizard"
	@echo "-----------------------------------------------------------------------------"
	rm -rf DEST/plwin
	rsync -av --exclude-from=$(top_srcdir)/config/excluded --delete \
          $(top_srcdir)/ lizard:$(WIN_SRC_DIR)
	@echo "-----------------------------------------------------------------------------"
	@echo "  BUILDING: lizard"
	@echo "-----------------------------------------------------------------------------"
	ssh lizard "source .bash_profile; $(MAKE) -C $(WIN_BUILD_DIR) all"
	rsync -av lizard:$(WIN_BUILD_DIR)/DEST/plwin DEST
	rpmbuild --rcfile rpmrc -bb SPECS/plwin-@CUSTOMER@-@VERSION@.spec

SPECS/plwin-@CUSTOMER@-@VERSION@.spec: plwin.spec
	cp -f plwin.spec $@

endif



install: all
	mkdir -p ../../../../releases/pipeline
	$(INSTALL_PROGRAM) pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.bin \
          ../../../../releases/pipeline



clean: clean-rpms
	rm -rf BUILD DEST RPMS SPECS SOURCES SRPMS
	rm -f header pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.bin
	$(MAKE) -C ../ clean

endif


#---------------------------------------------------------------------------------------------

if IS_MAC_OS

all: clean-rpms rpm-mkdirs
	rm -rf DEST/plmac
	DESTDIR=`pwd`/DEST/plmac $(MAKE) -C .. install

clean: clean-rpms
	rm -rf BUILD DEST RPMS SPECS SOURCES SRPMS
	$(MAKE) -C ../ clean

endif 


if IS_WINDOWS

all: clean-rpms rpm-mkdirs
	rm -rf DEST/plwin
	DESTDIR=`pwd`/DEST/plwin $(MAKE) -C .. install

clean: clean-rpms
	rm -rf BUILD DEST RPMS SPECS SOURCES SRPMS
	$(MAKE) -C ../ clean

endif 


#---------------------------------------------------------------------------------------------

rpm-mkdirs: 
	mkdir -p BUILD DEST RPMS SPECS SOURCES SRPMS 

clean-rpms: 
	rm -f $(RPMS)

clean-mac-rpm: 
	rm -f RPMS/i686/plmac-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm

clean-win-rpm: 
	rm -f RPMS/i686/plwin-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm
