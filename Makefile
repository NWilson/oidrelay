JC=javac
web/WEB-INF/classes/%.class: src/%.java
	$(JC) -cp $$JETTY_LIB -d web/WEB-INF/classes $<

all:: war

war: classes
	jar cf dist/oidrelay.war -C web .

classes: web/WEB-INF/classes/RelayServlet.class

clean::
	$(RM) dist/oidrelay.war
	$(RM) web/WEB-INF/classes/*.class

