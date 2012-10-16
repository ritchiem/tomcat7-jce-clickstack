#
# 
# TODOs
# TODO: add md5 verify to tomcat download

build_dir = ./build
pkg_dir = ./build/plugin
tomcat_url="http://mirror.nexcess.net/apache/tomcat/tomcat-7/v7.0.30/bin/apache-tomcat-7.0.30.zip"
tomcat_md5=i3a1fd1825202631e6c43461fa018c4f6
compile:
	mkdir -p "$(build_dir)"

	@if [ -e tomcat7 ]; then \
	   echo "Skipping Tomcat download"; \
	else \
	   echo "Downloading Tomcat..."; \
	   curl -o "$(build_dir)/tomcat.zip" $(tomcat_url) ; \
	   unzip -d "$(build_dir)" "$(build_dir)/tomcat.zip"; \
	   mv "./apache-tomcat-7"* "./tomcat7"; \
	   rm -rf "./tomcat7/webapps"; \
	fi

package: compile
	rm -f "$(build_dir)/tomcat7-plugin.zip"
	zip -9 -r "$(build_dir)/tomcat7-plugin.zip" control functions java server setup tomcat7

clean:
	rm -rf "$(build_dir)"
	rm -rf "./tomcat7"

