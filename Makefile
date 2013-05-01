JC=javac
web/WEB-INF/classes/oidrelay/%.class: src/oidrelay/%.java
	$(JC) -cp src:$$JETTY_LIB -d web/WEB-INF/classes $<

all:: war

war: classes
	jar cf dist/oidrelay.war -C web .

CLASSES = \
 web/WEB-INF/classes/oidrelay/PerUserTokenFactory.class \
 web/WEB-INF/classes/oidrelay/GetFromPostServlet.class \
 web/WEB-INF/classes/oidrelay/TestServlet.class

classes: $(CLASSES)

clean::
	$(RM) dist/oidrelay.war
	$(RM) web/WEB-INF/classes/*.class

