plugin_name = tomcat7-plugin
publish_repo = dev
publish_url = s3://cloudbees-clickstack/$(publish_repo)/

deps = lib/tomcat7.zip java

pkg_files = control functions server setup lib java

tomcat7_ver = 7.0.32
tomcat7_url = http://mirror.nexcess.net/apache/tomcat/tomcat-7/v$(tomcat7_ver)/bin/apache-tomcat-$(tomcat7_ver).zip
tomcat7_md5 = 97f3a8cc86c6c98213c4c988ee8a8f25

java_plugin_gitrepo = git://github.com/CloudBees-community/java-clickstack.git

include plugin.mk

lib/tomcat7.zip:
	mkdir -p lib
	curl -fLo lib/tomcat7.zip "$(tomcat7_url)"
	echo "$(tomcat7_md5)  lib/tomcat7.zip" | md5sum --check
	unzip -qd lib lib/tomcat7.zip
	rm -rf lib/apache-tomcat-$(tomcat7_ver)/webapps
	rm lib/tomcat7.zip
	cd lib/apache-tomcat-$(tomcat7_ver); zip -r ../tomcat7.zip *
	rm -rf lib/apache-tomcat-$(tomcat7_ver)

java:
	git clone $(java_plugin_gitrepo) java
	rm -rf java/.git
	cd java; make clean; make deps
