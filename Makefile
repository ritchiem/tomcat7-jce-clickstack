plugin_name = tomcat7-plugin
publish_bucket = cloudbees-clickstack
publish_repo = testing
publish_url = s3://$(publish_bucket)/$(publish_repo)/

deps = lib/tomcat7.zip java

pkg_files = control functions server setup lib java

include plugin.mk

tomcat7_ver = 7.0.35
tomcat7_url = http://mirror.nexcess.net/apache/tomcat/tomcat-7/v$(tomcat7_ver)/bin/apache-tomcat-$(tomcat7_ver).zip
tomcat7_md5 = 1c7a7869d86b74dddb0a22d15f020922

lib/tomcat7.zip:
	mkdir -p lib
	curl -fLo lib/tomcat7.zip "$(tomcat7_url)"
	echo "$(tomcat7_md5)  lib/tomcat7.zip" | md5sum --check
	unzip -qd lib lib/tomcat7.zip
	rm -rf lib/apache-tomcat-$(tomcat7_ver)/webapps
	rm lib/tomcat7.zip
	cd lib/apache-tomcat-$(tomcat7_ver); zip -r ../tomcat7.zip *
	rm -rf lib/apache-tomcat-$(tomcat7_ver)

java_plugin_gitrepo = git://github.com/CloudBees-community/java-clickstack.git

java:
	git clone $(java_plugin_gitrepo) java
	rm -rf java/.git
	cd java; make clean; make deps
