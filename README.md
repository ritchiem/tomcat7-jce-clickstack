# Tomcat 7 ClickStack

To use: 

    bees app:deploy -t tomcat7 -a APP_ID WAR_FILE

Tomcat 7 ClickStack for CloudBees PaaS. Deploy any Servlet2.x/3.x/JSP.

# Pre-requisite

* OpenJDK 6
* Bash shellA
* Make tools

# Build 

    $ make

After successful build tomcat7-plugin.zip is created and can be uploaded to the CloudBees platform location by the CloudBees team.

# Local development

Note: You should be familiar with developing ClickStacks using the genapp system first. \[[see docs](http://genapp-docs.cloudbees.com/quickstart.html)\]

* Build the plugin project using make to prepare for use in local app deploys
* In plugins\_home, add a symlink to the tomcat7-clickstack/pkg dir named 'tomcat7'

  $ ln -s tomcat7-clickstack/pkg PLUGINS\_HOME/tomcat7

* In your metadata.json, you can now reference the stack using the name 'tomcat7'

    { "app": {  "plugins": ["tomcat7"] } }


## TODOs
* Support injection of Database resources
* Add idle/active timeouts
* Add stats polling support

