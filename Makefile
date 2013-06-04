plugin_name = tomcat7-plugin
publish_bucket = cloudbees-clickstack
publish_repo = testing
publish_url = s3://$(publish_bucket)/$(publish_repo)/

deps = lib lib/tomcat7.zip lib/jmxtrans-agent.jar java

pkg_files = control functions server setup lib java conf

include plugin.mk

lib:
	mkdir -p lib

tomcat7_ver = 7.0.40
tomcat7_url = http://mirror.nexcess.net/apache/tomcat/tomcat-7/v$(tomcat7_ver)/bin/apache-tomcat-$(tomcat7_ver).zip
tomcat7_md5 = 676f8798168ebc9cf21da6804ee2fb53

lib/tomcat7.zip: lib lib/genapp-setup-tomcat7.jar
	curl -fLo lib/tomcat7.zip "$(tomcat7_url)"
	unzip -qd lib lib/tomcat7.zip
	rm -rf lib/apache-tomcat-$(tomcat7_ver)/webapps
	rm lib/tomcat7.zip
	cd lib/apache-tomcat-$(tomcat7_ver); \
	zip -rqy ../tomcat7.zip *
	rm -rf lib/apache-tomcat-$(tomcat7_ver)

JAVA_SOURCES := $(shell find genapp-setup-tomcat7/src -name "*.java")
JAVA_JARS = $(shell find genapp-setup-tomcat7/target -name "*.jar")

lib/genapp-setup-tomcat7.jar: $(JAVA_SOURCES) $(JAVA_JARS) lib
	cd genapp-setup-tomcat7; \
	mvn -q clean test assembly:single; \
	cd target; \
	cp genapp-setup-tomcat7-*-jar-with-dependencies.jar \
	$(CURDIR)/lib/genapp-setup-tomcat7.jar

jmxtrans_agent_ver = 1.0.0
jmxtrans_agent_url = http://repo1.maven.org/maven2/org/jmxtrans/agent/jmxtrans-agent/$(jmxtrans_agent_ver)/jmxtrans-agent-$(jmxtrans_agent_ver).jar
jmxtrans_agent_md5 = 9dd2bdd2adb7df9dbae093a2c6b08678

lib/jmxtrans-agent.jar: lib
	mkdir -p lib
	curl -fLo lib/jmxtrans-agent.jar "$(jmxtrans_agent_url)"
	$(call check-md5,lib/jmxtrans-agent.jar,$(jmxtrans_agent_md5))

java_plugin_gitrepo = git://github.com/CloudBees-community/java-clickstack.git

java:
	git clone $(java_plugin_gitrepo) java
	rm -rf java/.git
	cd java; make clean; make deps
