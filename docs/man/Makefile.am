
htmldir = @ROOT_INST_DIR@/share/docs/man

html_DATA = \
  plfilemgr.html \
  pljobmgr.html \
  plmaster.html \
  plnotify.html \
  plqueuemgr.html \
  pls.html \
  plui.html \
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


# install-data-hook: 
# 	rm -f checkbot.html checkbot-localhost.html
# 	@CHECKBOT@ $(DESTDIR)@ROOT_INST_DIR@/share/docs/man/*.html

clean:
	rm -f $(html_DATA) 
	rm -f copyright @LICENSE_DOC@
	rm -f checkbot.html checkbot-localhost.html
