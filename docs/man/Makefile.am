
htmldir = @ROOT_INST_DIR@/share/docs/man

html_DATA = \
  plfilemgr.html \
  plmaster.html \
  plnotify.html \
  pls.html \
  plui.html \
  realpath.html


$(html_DATA): %.html: %.html.src copyright license
	@echo Adding legal notices to: $@
	@awk -- '/@COPYRIGHT@/ { system("cat copyright") } \
               !/@COPYRIGHT@/ { print }' $< | \
        awk -- '/@LICENSE@/ { system("cat license") } \
               !/@LICENSE@/ { print }' > $@

%: ../legal/%.html
	@(cnt=`wc -l $< | awk '{print $$1}'`; \
         tail --lines=+9 $< | head --lines=$$(($$cnt-10)) > $@)

clean:
	rm -f $(html_DATA) 
	rm -f copyright license
