if IS_UNIX

if FOUNDATION 

noinst_DATA = \
  pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.bin


if PL_LOCAL

if HAS_MAC

if HAS_WINDOWS
    RPMS = \
      RPMS/i686/pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plmaster-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plpluginmgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plqueuemgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/pllocal-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plmac-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plwin-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm
else 
    RPMS = \
      RPMS/i686/pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plmaster-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plpluginmgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plqueuemgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/pllocal-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plmac-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm
endif

else !HAS_MAC

if HAS_WINDOWS 
  RPMS = \
    RPMS/i686/pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
    RPMS/i686/plmaster-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
    RPMS/i686/plpluginmgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
    RPMS/i686/plqueuemgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
    RPMS/i686/pllocal-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
    RPMS/i686/plwin-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm
else 
  RPMS = \
    RPMS/i686/pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
    RPMS/i686/plmaster-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
    RPMS/i686/plpluginmgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
    RPMS/i686/plqueuemgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
    RPMS/i686/pllocal-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm
endif

endif !HAS_MAC

else !PL_LOCAL

if HAS_MAC

if HAS_WINDOWS
    RPMS = \
      RPMS/i686/pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plmaster-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plpluginmgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plqueuemgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plmac-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plwin-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm
else 
    RPMS = \
      RPMS/i686/pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plmaster-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plpluginmgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plqueuemgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
      RPMS/i686/plmac-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm
endif 

else !HAS_MAC

if HAS_WINDOWS 
  RPMS = \
    RPMS/i686/pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
    RPMS/i686/plmaster-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
    RPMS/i686/plpluginmgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
    RPMS/i686/plqueuemgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
    RPMS/i686/plwin-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm
else 
  RPMS = \
    RPMS/i686/pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
    RPMS/i686/plmaster-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
    RPMS/i686/plpluginmgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
    RPMS/i686/plqueuemgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm
endif

endif !HAS_MAC

endif !PL_LOCAL


if PL_LOCAL_ALL
  STUDIOS = icvfx radar SCEACSG TheO
else
  STUDIOS = $(CUSTOMER) 
endif


# RPMBUILD = /bin/bash @RPM_ROOT@/rpmbuild-helper
RPMBUILD = rpmbuild --rcfile=rpmrc --define='_topdir @RPM_ROOT@'


MAC_SRC_DIR = \
  $(patsubst @BUILD_USER_HOME@/%,/Users/@BUILD_USER@/%,@top_srcdir@)

MAC_BUILD_DIR = \
  $(patsubst @BUILD_USER_HOME@/%,/Users/@BUILD_USER@/%,@RPM_ROOT@)

WIN_SRC_DIR = \
  $(patsubst @BUILD_USER_HOME@/code-@CUSTOMER@/%,/home/Jim/code-@CUSTOMER@/%,@top_srcdir@)

WIN_BUILD_DIR = \
  $(patsubst @BUILD_USER_HOME@/%,/home/Jim/%,@RPM_ROOT@)

WIN32_BUILD_DIR = $(patsubst %/debug/rpm,%/debug-x86/rpm,$(WIN_BUILD_DIR))
WIN64_BUILD_DIR = $(patsubst %/debug/rpm,%/debug-x86_64/rpm,$(WIN_BUILD_DIR))


API_JAR = DEST/pipeline/@ROOT_INST_DIR@/lib/api.jar


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
	cp $(RPMS) pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@
	tar -zcvf $@ pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@
	rm -rf pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@

pipeline-install: clean-rpms rpm-mkdirs
	rm -rf DEST/pipeline DEST/plmaster DEST/plpluginmgr DEST/plqueuemgr DEST/pllocal
	rm -rf pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@
	DESTDIR=`pwd`/DEST/pipeline $(MAKE) -C .. install
	mkdir -p DEST/plmaster/@NODEDIR@
	mv DEST/pipeline/@NODEDIR@/* DEST/plmaster/@NODEDIR@
	mkdir -p DEST/plmaster/@NODEDIR@-backup/plmaster
	mkdir -p DEST/plpluginmgr/@PL_PLUGIN_DIRECTORY@
	mkdir -p DEST/plpluginmgr/@PL_PLUGIN_DIRECTORY@-backup/plpluginmgr
	mv DEST/pipeline/plugins DEST/plpluginmgr/@PL_PLUGIN_DIRECTORY@/
	mkdir -p DEST/plqueuemgr/@QUEUEDIR@
	mkdir -p DEST/plqueuemgr/@QUEUEDIR@-backup/plqueuemgr
	ln -s pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@ \
              DEST/pipeline/@ROOT_INST_PARENT_DIR@/pipeline-latest
	mkdir -p DEST/pllocal/@ROOT_INST_PARENT_DIR@/pipeline-local
	@if test -d DEST/pipeline/@ROOT_INST_PARENT_DIR@/pipeline-local; \
        then \
          echo mv DEST/pipeline/@ROOT_INST_PARENT_DIR@/pipeline-local/* \
             DEST/pllocal/@ROOT_INST_PARENT_DIR@/pipeline-local; \
          mv DEST/pipeline/@ROOT_INST_PARENT_DIR@/pipeline-local/* \
             DEST/pllocal/@ROOT_INST_PARENT_DIR@/pipeline-local; \
          for studio in $(STUDIOS); \
          do \
            echo ln -s ../../../src \
              DEST/pllocal/@ROOT_INST_PARENT_DIR@/pipeline-local/build/eclipse/$$studio/src;\
            ln -s ../../../src \
              DEST/pllocal/@ROOT_INST_PARENT_DIR@/pipeline-local/build/eclipse/$$studio/src;\
            echo ln -s ../../../src \
              DEST/pllocal/@ROOT_INST_PARENT_DIR@/pipeline-local/build/make/$$studio/src;\
            ln -s ../../../src \
              DEST/pllocal/@ROOT_INST_PARENT_DIR@/pipeline-local/build/make/$$studio/src;\
          done; \
        fi
	mkdir pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@
	cp $(API_JAR) pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@
	@echo "Packing Docs..."
	tar -zcf pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@/docs.tgz \
            -C DEST/pipeline/@ROOT_INST_DIR@/share docs
	@echo "Packing Examples..."
	tar -zcf pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@/examples.tgz \
            -C DEST/pipeline/@ROOT_INST_DIR@ examples


if PL_LOCAL

core-rpms: pipeline-install \
  RPMS/i686/pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
  RPMS/i686/plmaster-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
  RPMS/i686/plpluginmgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
  RPMS/i686/plqueuemgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
  RPMS/i686/pllocal-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm

else !PL_LOCAL

core-rpms: pipeline-install \
  RPMS/i686/pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
  RPMS/i686/plmaster-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
  RPMS/i686/plpluginmgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm \
  RPMS/i686/plqueuemgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm

endif !PL_LOCAL


RPMS/i686/plmaster-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm: \
  SPECS/plmaster-@CUSTOMER@-@VERSION@.spec
	$(RPMBUILD) --buildroot=@RPM_ROOT@/DEST/plmaster -bb \
          SPECS/plmaster-@CUSTOMER@-@VERSION@.spec

SPECS/plmaster-@CUSTOMER@-@VERSION@.spec: plmaster.spec
	cp -f plmaster.spec $@


RPMS/i686/pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm: \
  native-only SPECS/pipeline-@CUSTOMER@-@VERSION@.spec 
	$(RPMBUILD) --buildroot=@RPM_ROOT@/DEST/pipeline -bb \
          SPECS/pipeline-@CUSTOMER@-@VERSION@.spec

if HAS_X86_SUBPASS

native-only: 
	@echo "-----------------------------------------------------------------------------"
	@echo "  BUILDING (native only): @NATIVE_ARCHOPTS@"
	@echo "-----------------------------------------------------------------------------"
	$(MAKE) -C ../../debug-native/rpm all
	rsync -av ../../debug-native/rpm/DEST/unix-native/ DEST/pipeline

.FORCE: SPECS/pipeline-@CUSTOMER@-@VERSION@.spec
SPECS/pipeline-@CUSTOMER@-@VERSION@.spec: \
  pipeline.spec.A pipeline.spec.B pipeline.spec.C pipeline.spec.D pipeline.spec.E \
  pipeline.spec.F pipeline.spec.G
	rm pipeline.spec.B pipeline.spec.D pipeline.spec.F
	rsync -av ../../debug-native/rpm/pipeline.spec.B pipeline.spec.B
	rsync -av ../../debug-native/rpm/pipeline.spec.D pipeline.spec.D
	rsync -av ../../debug-native/rpm/pipeline.spec.F pipeline.spec.F
	cat $+ > $@



else !HAS_X86_SUBPASS

native-only:

SPECS/pipeline-@CUSTOMER@-@VERSION@.spec: \
  pipeline.spec.A pipeline.spec.C pipeline.spec.E pipeline.spec.G
	cat $+ > $@

endif !HAS_X86_SUBPASS


RPMS/i686/plqueuemgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm: \
  SPECS/plqueuemgr-@CUSTOMER@-@VERSION@.spec
	$(RPMBUILD) --buildroot=@RPM_ROOT@/DEST/plqueuemgr -bb \
          SPECS/plqueuemgr-@CUSTOMER@-@VERSION@.spec

SPECS/plqueuemgr-@CUSTOMER@-@VERSION@.spec: plqueuemgr.spec
	cp -f plqueuemgr.spec $@


RPMS/i686/plpluginmgr-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm: \
  SPECS/plpluginmgr-@CUSTOMER@-@VERSION@.spec
	$(RPMBUILD) --buildroot=@RPM_ROOT@/DEST/plpluginmgr -bb \
          SPECS/plpluginmgr-@CUSTOMER@-@VERSION@.spec

SPECS/plpluginmgr-@CUSTOMER@-@VERSION@.spec: plpluginmgr.spec
	cp -f plpluginmgr.spec $@

if PL_LOCAL

RPMS/i686/pllocal-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm: \
  SPECS/pllocal-@CUSTOMER@-@VERSION@.spec
	$(RPMBUILD) --buildroot=@RPM_ROOT@/DEST/pllocal -bb \
          SPECS/pllocal-@CUSTOMER@-@VERSION@.spec

SPECS/pllocal-@CUSTOMER@-@VERSION@.spec: pllocal.spec
	cp -f pllocal.spec $@

endif PL_LOCAL




if HAS_MAC

mac-rpm: clean-mac-rpm RPMS/i686/plmac-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm

RPMS/i686/plmac-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm: \
  SPECS/plmac-@CUSTOMER@-@VERSION@.spec
	rm -rf DEST/plmac
	@echo "-----------------------------------------------------------------------------"
	@echo "  UPDATING: bullfrog"
	@echo "-----------------------------------------------------------------------------"
	rsync -av --exclude-from=$(top_srcdir)/config/excluded --delete \
          $(top_srcdir)/ bullfrog:$(MAC_SRC_DIR)
	@echo "-----------------------------------------------------------------------------"
	@echo "  BUILDING: bullfrog"
	@echo "-----------------------------------------------------------------------------"
	ssh bullfrog "source .bash_profile; $(MAKE) -C $(MAC_BUILD_DIR) all"
	rsync -av bullfrog:$(MAC_BUILD_DIR)/DEST/plmac DEST
	$(RPMBUILD) --buildroot=@RPM_ROOT@/DEST/plmac -bb \
          SPECS/plmac-@CUSTOMER@-@VERSION@.spec

SPECS/plmac-@CUSTOMER@-@VERSION@.spec: plmac.spec
	cp -f plmac.spec $@

endif HAS_MAC


if HAS_WINDOWS

win-rpm: clean-win-rpm RPMS/i686/plwin-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm

RPMS/i686/plwin-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm: \
  SPECS/plwin-@CUSTOMER@-@VERSION@.spec
	@echo "-----------------------------------------------------------------------------"
	@echo "  UPDATING: raptor"
	@echo "-----------------------------------------------------------------------------"
	rm -rf DEST/plwin
	rsync -av --exclude-from=$(top_srcdir)/config/excluded --delete \
          $(top_srcdir)/ raptor:$(WIN_SRC_DIR)
	@echo "-----------------------------------------------------------------------------"
	@echo "  BUILDING (x86): raptor"
	@echo "-----------------------------------------------------------------------------"
	ssh raptor "source .bash_profile; $(MAKE) -C $(WIN32_BUILD_DIR) all"
	rsync -av raptor:$(WIN32_BUILD_DIR)/DEST/plwin DEST
	rsync -v raptor:$(WIN32_BUILD_DIR)/../../JobMgr.msi \
          JobMgr-@CUSTOMER@-@WIN_SERVICE_VERSION@-@PROFILE_STAMP@-x86.msi
	@echo "-----------------------------------------------------------------------------"
	@echo "  BUILDING (x86_64): raptor"
	@echo "-----------------------------------------------------------------------------"
	ssh raptor "source .bash_profile; $(MAKE) -C $(WIN64_BUILD_DIR) all"
	rsync -av raptor:$(WIN64_BUILD_DIR)/DEST/plwin DEST
	rsync -v raptor:$(WIN64_BUILD_DIR)/../../JobMgr.msi \
          JobMgr-@CUSTOMER@-@WIN_SERVICE_VERSION@-@PROFILE_STAMP@-x86_64.msi
	@echo "-----------------------------------------------------------------------------"
	@echo "  PACKAGING..."
	@echo "-----------------------------------------------------------------------------"
	$(RPMBUILD) --buildroot=@RPM_ROOT@/DEST/plwin -bb \
          SPECS/plwin-@CUSTOMER@-@VERSION@.spec

SPECS/plwin-@CUSTOMER@-@VERSION@.spec: plwin.spec
	cp -f plwin.spec $@

install-win-service:
	$(INSTALL_DATA) \
          @RPM_ROOT@/JobMgr-@CUSTOMER@-@WIN_SERVICE_VERSION@-@PROFILE_STAMP@-x86.msi \
          ../../../../releases/pipeline
	$(INSTALL_DATA) \
          @RPM_ROOT@/JobMgr-@CUSTOMER@-@WIN_SERVICE_VERSION@-@PROFILE_STAMP@-x86_64.msi \
          ../../../../releases/pipeline

else !HAS_WINDOWS

install-win-service:

endif !HAS_WINDOWS



install: all install-win-service 
	mkdir -p ../../../../releases/pipeline
	$(INSTALL_PROGRAM) pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.bin \
          ../../../../releases/pipeline




clean: clean-rpms
	rm -rf BUILD DEST RPMS SPECS SOURCES SRPMS
	rm -f header pipeline-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.bin
	$(MAKE) -C ../ clean


else !FOUNDATION

native-specs: pipeline.spec.B pipeline.spec.D pipeline.spec.F

all: clean-rpms rpm-mkdirs native-specs
	rm -rf DEST/unix-native
	DESTDIR=`pwd`/DEST/unix-native $(MAKE) -C .. install

clean: clean-rpms
	rm -rf BUILD DEST RPMS SPECS SOURCES SRPMS
	$(MAKE) -C ../ clean

endif !FOUNDATION

endif IS_UNIX


#---------------------------------------------------------------------------------------------

if IS_MAC_OS

all: clean-rpms rpm-mkdirs
	rm -rf DEST/plmac
	DESTDIR=`pwd`/DEST/plmac $(MAKE) -C .. install

clean: clean-rpms
	rm -rf BUILD DEST RPMS SPECS SOURCES SRPMS
	$(MAKE) -C ../ clean

endif IS_MAC_OS


#---------------------------------------------------------------------------------------------

if IS_WINDOWS

all: clean-rpms rpm-mkdirs
	rm -rf DEST/plwin
	DESTDIR=`pwd`/DEST/plwin $(MAKE) -C .. install

clean: clean-rpms
	rm -rf BUILD DEST RPMS SPECS SOURCES SRPMS
	$(MAKE) -C ../ clean

endif IS_WINDOWS


#---------------------------------------------------------------------------------------------

rpm-mkdirs: 
	mkdir -p BUILD DEST RPMS SPECS SOURCES SRPMS 

clean-rpms: 
	rm -f $(RPMS)

clean-mac-rpm: 
	rm -f RPMS/i686/plmac-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm

clean-win-rpm: 
	rm -f RPMS/i686/plwin-@CUSTOMER@-@VERSION@-@PROFILE_STAMP@.i686.rpm
