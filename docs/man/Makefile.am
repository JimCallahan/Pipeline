
htmldir = @ROOT_INST_DIR@/share/docs/man

html_DATA = \
  plfilemgr.html \
  pljobmgr.html \
  plmaster.html \
  plplugin.html \
  plpluginmgr.html \
  plpython.html \
  plqueuemgr.html \
  plrestore.html \
  pls.html \
  plscript.html \
  plshakeio.html \
  plui.html \
  plvalidate.html \
  realpath.html



$(html_DATA): %.html: %.src.html copyright @LICENSE_DOC@
	@echo Adding legal notices to: $@
	@awk -- '/@COPYRIGHT@/ { system("cat copyright") } \
               !/@COPYRIGHT@/ { print }' $< | \
        awk -- '/@LICENSE@/ { system("cat @LICENSE_DOC@") } \
               !/@LICENSE@/ { print }' > $@

%: ../legal/%.html
	@(cnt=`wc -l $< | awk '{print $$1}'`; \
         tail --lines=+9 $< | head --lines=$$(($$cnt-10)) > $@)


# install-data-hook: checkbot.html checkbot-localhost.html

# checkbot.html checkbot-localhost.html: $(DESTDIR)@ROOT_INST_DIR@/share/docs/man/*.html
# 	@CHECKBOT@ $^

clean:
	rm -f $(html_DATA) 
	rm -f copyright @LICENSE_DOC@
	rm -f checkbot.html checkbot-localhost.html
