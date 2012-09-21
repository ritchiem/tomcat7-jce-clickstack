build_dir = ./build
pkg_dir = ./build/plugin
tomcat_url="file:///Users/spike/Downloads/apache-tomcat-7.0.29.zip"

compile:
	mkdir -p $(build_dir)
	cp -rf control $(pkg_dir)/
	cp -rf server $(pkg_dir)/
	cp -rf setup $(pkg_dir)/

	mkdir -p $(pkg_dir)/runtime
	@if [ -e $(pkg_dir)/runtime/apache-tomcat-7 ]; then \
	   echo "Skipping Tomcat download"; \
	else \
	   echo "Downloading Tomcat..."; \
	   curl $(tomcat_url) > $(build_dir)/tomcat.zip; \
	   unzip -d $(pkg_dir)/runtime/ $(build_dir)/tomcat.zip; \
	   mv $(pkg_dir)/runtime/* $(pkg_dir)/runtime/tomcat7; \
	fi

clean:
	rm -rf $(build_dir)

