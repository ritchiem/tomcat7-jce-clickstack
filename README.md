# Tomcat 7 ClickStack

Tomcat 7 ClickStack for CloudBees PaaS. Deploy any Servlet2.x/3.x/JSP.

# Pre-requisite

* OpenJDK 6
* Bash shellA
* Make tools

# Build 

    $ make package

After successful build tomcat7-plugin.zip is created and can be uploaded to the CloudBees platform location by the CloudBees team.

# Package and Deploy Sample App

## Create application zip file
    $ cd example
    $ zip -r ../build/hello.zip hello/ 
    $ cd ..

## Deploy 

    $ bees app:deploy -a APP_ID -t tomcat7  build/hello.war


## TODOs
* Support injection of Database resources
* Add idle/active timeouts
* Add stats polling support

