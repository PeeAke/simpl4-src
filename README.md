

[![alt text](https://raw.githubusercontent.com/ms123s/simpl4-deployed/master/etc/images/simpl4_logo.png  "simpl4 logo")] (http://www.simpl4.org) building
=================

If you're only interested in a installation, go to [*simpl4-deployed*](https://github.com/ms123s/simpl4-deployed) or [*see here*] (http://web.simpl4.org/repo/webdemo/start.html#links)


##Building simpl4##

####Requirement
* java jdk1.8.0  or openjdk 8
* git

----

####Cloning this repo
```bash
$ git clone https://github.com/ms123s/simpl4-src.git simpl4-src
```
----

####Going to sourcerepository and start the build
```bash
$ cd simpl4-src
$ gradlew
```
clone *simpl4-deploy*, parallel to *simpl4-src*   directory
Directory arrangment:  
simpl4-src  
simpl4-deploy

and now update the "deploy directory"
```bash
$ cd simpl4-src
$ gradlew deploy 
```
----

####Setup 
```bash
$ cd simpl4-deployed
$ bin/setup.sh -p port
```
----
####Start 
```bash
$ cd simpl4-deployed
$ bin/start.sh start
```
----
####Stop 
```bash
$ cd simpl4-deployed
$ bin/start.sh stop
```
----
####[*Website*](http://www.simpl4.org) and [*Demo-applications*](https://github.com/simpl4-apps?tab=repositories)

