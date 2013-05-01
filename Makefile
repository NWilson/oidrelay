JC=javac
web/WEB-INF/classes/oidrelay/%.class: src/oidrelay/%.java dist/servlet-api.jar
	@mkdir -p web/WEB-INF/classes/oidrelay
	$(JC) -g -cp src:dist/servlet-api.jar -d web/WEB-INF/classes $<

all:: war

war: classes
	@mkdir -p dist
	jar cf dist/oidrelay.war -C web .

dist/servlet-api.jar:
	@mkdir -p dist
	@printf %s "Looking for the servlet API jar..."
	@if [ -n "$$SERVLET_LIB" ]; then \
	  API="`echo "$$SERVLET_LIB"|sed -e '/^\//!s/^/..\//'`"; \
	  echo " found at $$API"; \
	  ln -s "$$API" dist/servlet-api.jar; \
	else echo " please specify SERVLET_LIB"; echo exit 1; fi

CLASSES = \
 web/WEB-INF/classes/oidrelay/TestServlet.class

classes: $(CLASSES)

clean::
	$(RM) dist/oidrelay.war
	$(RM) web/WEB-INF/classes/oidrelay/*.class

