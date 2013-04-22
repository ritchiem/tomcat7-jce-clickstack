plugin_name = tomcat7-plugin
publish_bucket = cloudbees-clickstack
publish_repo = testing
publish_url = s3://$(publish_bucket)/$(publish_repo)/

deps = lib lib/tomcat7.zip java lib/genapp-setup-tomcat7.jar lib/java-mail.jar lib/jmxtrans-agent.jar 

pkg_files = control functions server setup lib java tomcat7-metrics.xml

include plugin.mk

tomcat7_ver = 7.0.39
tomcat7_url = http://mirror.nexcess.net/apache/tomcat/tomcat-7/v$(tomcat7_ver)/bin/apache-tomcat-$(tomcat7_ver).zip
tomcat7_md5 = 30c6adc5b537be4bd098a4b30b5385a5

java_mail_url = http://repo1.maven.org/maven2/javax/mail/mail/1.4.5/mail-1.4.5.jar
java_mail_md5 = ec6e4e5ebd85a221b395b8f3b37545e6

JAVA_SOURCES := $(shell find genapp-setup-tomcat7/src -name "*.java")
JAVA_JARS = $(shell find genapp-setup-tomcat7/target -name "*.jar")

lib:
	mkdir -p lib

lib/tomcat7.zip: lib
	curl -fLo lib/tomcat7.zip "$(tomcat7_url)"
	unzip -qd lib lib/tomcat7.zip
	rm -rf lib/apache-tomcat-$(tomcat7_ver)/webapps
	rm lib/tomcat7.zip
	cd lib/apache-tomcat-$(tomcat7_ver); zip -r ../tomcat7.zip *
	rm -rf lib/apache-tomcat-$(tomcat7_ver)A

lib/java-mail.jar: lib
	curl -fLo lib/java-mail.jar "$(java_mail_url)"       
	$(call check-md5,lib/java-mail.jar,$(java_mail_md5))

lib/genapp-setup-tomcat7.jar: $(JAVA_SOURCES) $(JAVA_JARS)
	cd genapp-setup-tomcat7; mvn clean test assembly:single;
	cp genapp-setup-tomcat7/target/genapp-setup-tomcat7-*-jar-with-dependencies.jar lib/genapp-setup-tomcat7.jar


jmxtrans_agent_ver = 1.0.0-20130422.184944-1
jmxtrans_agent_url = https://repository-jmxtrans.forge.cloudbees.com/snapshot/org/jmxtrans/agent/jmxtrans-agent/1.0.0-SNAPSHOT/jmxtrans-agent-1.0.0-20130422.184944-1.jar
jmxtrans_agent_md5 = 785b9313eee66d23850f8fc4634952ac

lib/jmxtrans-agent.jar:
	mkdir -p lib
	curl -fLo lib/jmxtrans-agent.jar "$(jmxtrans_agent_url)"
	$(call check-md5,lib/jmxtrans-agent.jar,$(jmxtrans_agent_md5))


java_plugin_gitrepo = git://github.com/CloudBees-community/java-clickstack.git

java:
	git clone $(java_plugin_gitrepo) java
	rm -rf java/.git
	cd java; make clean; make deps
