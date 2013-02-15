# Tomcat 7 ClickStack

To use: 

    bees app:deploy -t tomcat7 -a APP_ID WAR_FILE

Tomcat 7 ClickStack for CloudBees PaaS. Deploy any Servlet2.x/3.x/JSP.

# Pre-requisite

* OpenJDK 6
* Bash shellA
* Make tools
* Apache Maven

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

# Testing the plugin on CloudBees

You can deploy the tomcat7-plugin.zip to S3 using the following command:

    $ make publish_repo=dev publish

If you don't have S3 creds or tools setup, follow the instructions in the publish error messages.

Once the plugin is published to a public URL, you can update an app to use it with the CloudBees SDK:

    $ bees app:deploy -a APP_ID -t tomcat7 -RPLUGIN.SRC.tomcat7=URL_TO_YOUR_PLUGIN_ZIP PATH_TO_WARFILE


## TODOs
* Support injection of Database resources
* Support injection of Mail resources
* Add idle/active timeouts
* Add stats polling support
* Add private app support (perhaps via router instead?)
* Add mysql driver jar to Tomcat lib (TBD)

